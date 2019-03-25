package self.ed;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import self.ed.javafx.MultiPropertyValueFactory;
import self.ed.util.FormatUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.scene.paint.Color.GREEN;
import static javafx.scene.paint.Color.RED;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static self.ed.VideoRecord.PROGRESS_DONE;
import static self.ed.javafx.CustomFormatCellFactory.*;
import static self.ed.util.FileUtils.buildTargetDir;
import static self.ed.util.FileUtils.listFiles;
import static self.ed.util.FormatUtils.formatCompressionRatio;
import static self.ed.util.FormatUtils.formatFileSize;

public class ConverterUI extends Application {
    private final ObservableList<VideoRecord> files = observableArrayList();
    private final List<Task> tasks = new ArrayList<>();
    private final SimpleObjectProperty<File> sourceDir = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<File> targetDir = new SimpleObjectProperty<>();
    private final Button sourceButton = buildButton("Input...");
    private final Button targetButton = buildButton("Output...");
    private final Button startButton = buildButton("Start");
    private final Button stopButton = buildButton("Stop");
    private final Button refreshButton = buildButton("Refresh");
    private final ProgressIndicator progressIndicator = new ProgressIndicator();
    private final Label info = new Label();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Bulk Video Converter");

        BorderPane layout = new BorderPane();
        layout.setTop(buildControlPane(stage));
        layout.setCenter(buildRecordTable());

        stage.setScene(new Scene(layout, 1000, 500));
        stage.show();
        //sourceDir.set(new File(""));
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        // TODO: why is there still a background process?
        stopAll();
    }

    private Pane buildControlPane(Stage stage) {
        Label sourceLabel = new Label();
        Label targetLabel = new Label();
        sourceDir.addListener((ChangeListener<? super File>) (observable, oldValue, newValue) -> {
            sourceLabel.setText(newValue.getAbsolutePath());
            targetDir.set(buildTargetDir(newValue));
            enable(targetButton);
        });
        targetDir.addListener((ChangeListener<? super File>) (observable, oldValue, newValue) -> {
            targetLabel.setText(newValue.getAbsolutePath());
            loadFiles();
        });

        DirectoryChooser directoryChooser = new DirectoryChooser();
        sourceButton.setOnAction(e -> ofNullable(directoryChooser.showDialog(stage)).ifPresent(sourceDir::set));
        targetButton.setOnAction(e -> ofNullable(directoryChooser.showDialog(stage)).ifPresent(targetDir::set));
        startButton.setOnAction(e -> startAll());
        stopButton.setOnAction(e -> stopAll());
        refreshButton.setOnAction(e -> loadFiles());

        progressIndicator.setVisible(false);

        BorderPane control = new BorderPane();
        control.setLeft(
                new VBox(5,
                        new HBox(10, sourceButton, sourceLabel),
                        new HBox(10, targetButton, targetLabel),
                        new HBox(10, startButton, stopButton, info))
        );
        control.setRight(new VBox(5, progressIndicator, refreshButton));
        enable(sourceButton);
        return control;
    }

    private TableView buildRecordTable() {
        TableColumn<VideoRecord, String> path = new TableColumn<>("Path");
        path.setMinWidth(300);
        path.setCellValueFactory(new PropertyValueFactory<>("path"));

        TableColumn<VideoRecord, Long> duration = new TableColumn<>("Duration");
        duration.setMinWidth(100);
        duration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        duration.setCellFactory(format(FormatUtils::formatTimeSeconds));

        TableColumn<VideoRecord, List<Long>> sourceResolution = new TableColumn<>("Resolution");
        sourceResolution.setMinWidth(100);
        sourceResolution.setCellValueFactory(new MultiPropertyValueFactory<>("sourceWidth", "sourceHeight"));
        sourceResolution.setCellFactory(format(FormatUtils::formatDimensions));

        TableColumn<VideoRecord, Long> sourceSize = new TableColumn<>("Size");
        sourceSize.setMinWidth(100);
        sourceSize.setCellValueFactory(new PropertyValueFactory<>("sourceSize"));
        sourceSize.setCellFactory(alignRight(format(FormatUtils::formatFileSize)));

        TableColumn<VideoRecord, Double> progress = new TableColumn<>("Progress");
        progress.setMinWidth(100);
        progress.setCellValueFactory(new PropertyValueFactory<>("progress"));
        progress.setCellFactory(ProgressBarTableCell.forTableColumn());

        TableColumn<VideoRecord, Long> targetSize = new TableColumn<>("Size");
        targetSize.setMinWidth(100);
        targetSize.setCellValueFactory(new PropertyValueFactory<>("targetSize"));
        targetSize.setCellFactory(alignRight(format(size -> size == 0 ? "" : formatFileSize(size))));

        TableColumn<VideoRecord, Double> compression = new TableColumn<>("Ratio");
        compression.setMinWidth(100);
        compression.setCellValueFactory(new PropertyValueFactory<>("compression"));
        compression.setCellFactory(alignRight(decorate(
                format(ratio -> ratio == 0 ? "" : formatCompressionRatio(ratio)),
                cell -> ofNullable(cell.getItem()).ifPresent(ratio -> cell.setTextFill(ratio < 1 ? GREEN : RED)))
        ));

        TableColumn<VideoRecord, String> error = new TableColumn<>("Error");
        error.setMinWidth(100);
        error.setCellValueFactory(new PropertyValueFactory<>("error"));
        error.setCellFactory(decorate(identity(), cell -> cell.setTextFill(RED)));

        TableView<VideoRecord> table = new TableView<>(files);
        table.getColumns().addAll(path, duration, sourceResolution, sourceSize, progress, targetSize, compression, error);
        return table;
    }

    private Button buildButton(String text) {
        Button button = new Button(text);
        button.setMinWidth(80);
        return button;
    }

    private synchronized void loadFiles() {
        info("Collecting...");
        enable();
        files.clear();
        progressIndicator.setVisible(false);
        new Thread(new Task<Void>() {
            @Override
            protected Void call() {
                List<String> sourceFiles = listFiles(sourceDir.get());
                for (String path : sourceFiles) {
                    // TODO: Platform.runLater is not reliable because it may be run with a delay, e.g. after files.add has been called thereby showing a higher number than expected
                    Platform.runLater(() -> info("Loading... " + (files.size() + 1) + "/" + sourceFiles.size()));
                    files.add(VideoRecord.newInstance(sourceDir.get(), path, targetDir.get()));
                }
                // enabling before updateConvertProgress because the latter may disable start button if everything is converted
                enable(sourceButton, targetButton, startButton, refreshButton);
                Platform.runLater(() -> {
                    info(EMPTY);
                    updateConvertProgress();
                    progressIndicator.setVisible(true);
                });
                return null;
            }
        }).start();
    }

    private synchronized void startAll() {
        info("Converting...");
        enable(stopButton);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        files.stream()
                .filter(file -> file.getProgress() != PROGRESS_DONE)
                .map(file -> new ConverterTask(file, this::updateConvertProgress))
                .peek(executor::execute)
                .collect(toCollection(() -> tasks));
        updateConvertProgress();
    }

    private void updateConvertProgress() {
        long done = files.stream().filter(file -> file.getProgress() == PROGRESS_DONE)
                .mapToLong(VideoRecord::getSourceSize)
                .sum();
        long total = files.stream()
                .mapToLong(VideoRecord::getSourceSize)
                .sum();
        progressIndicator.setProgress((double) done / total);
        if (done == total) {
            info(EMPTY);
            enable(sourceButton, targetButton, refreshButton);
        }
    }

    private synchronized void stopAll() {
        enable();
        info("Stopping...");
        tasks.stream()
                .filter(task -> !task.isDone())
                .forEach(Task::cancel);
        tasks.clear();
        info(EMPTY);
        enable(sourceButton, targetButton, startButton, refreshButton);
    }

    private void enable(Button... buttons) {
        List<Button> enabled = asList(buttons);
        asList(sourceButton, targetButton, startButton, stopButton, refreshButton)
                .forEach(button -> button.setDisable(!enabled.contains(button)));
    }

    private void info(String message) {
        System.out.println(new Date() + ": " + message);
        info.setText(message);
    }
}
