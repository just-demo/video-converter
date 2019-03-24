package self.ed;

import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
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
import self.ed.javafx.CustomFormatCellFactory;
import self.ed.javafx.MultiPropertyValueFactory;
import self.ed.util.FormatUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static javafx.collections.FXCollections.observableArrayList;
import static self.ed.VideoRecord.PROGRESS_DONE;
import static self.ed.VideoRecord.PROGRESS_NOT_STARTED;
import static self.ed.javafx.CustomFormatCellFactory.alignRight;
import static self.ed.util.FileUtils.buildTargetDir;
import static self.ed.util.FileUtils.listFiles;
import static self.ed.util.FormatUtils.formatFileSize;

public class ConverterUI extends Application {
    private final ObservableList<VideoRecord> files = observableArrayList();
    private final List<ConvertTask> tasks = new ArrayList<>();
    private final SimpleObjectProperty<File> sourceDir = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<File> targetDir = new SimpleObjectProperty<>();
    private final Label info = new Label();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        // https://docs.oracle.com/javase/8/javafx/layout-tutorial/builtin_layouts.htm#JFXLY102
        // https://docs.oracle.com/javase/8/javafx/interoperability-tutorial/concurrency.htm
        stage.setTitle("Bulk Video Converter");

        BorderPane layout = new BorderPane();
        layout.setTop(buildInputPane(stage));
        layout.setCenter(buildRecordTable());

        stage.setScene(new Scene(layout, 1000, 500));
        stage.show();

        // TODO: revert
        sourceDir.set(new File("/dummy"));
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        // TODO: why is there still a background process?
        stopAll();
    }

    private Pane buildInputPane(Stage stage) {
        Label sourcePath = new Label();
        Label targetPath = new Label();
        sourceDir.addListener((ChangeListener<? super File>) (observable, oldValue, newValue) -> {
            sourcePath.setText(newValue.getAbsolutePath());
            targetDir.set(buildTargetDir(newValue));
        });
        targetDir.addListener((ChangeListener<? super File>) (observable, oldValue, newValue) -> {
            targetPath.setText(newValue.getAbsolutePath());
            loadFiles();
        });

        DirectoryChooser directoryChooser = new DirectoryChooser();

        Button sourceButton = buildButton("Input...");
        sourceButton.setOnAction(e -> ofNullable(directoryChooser.showDialog(stage)).ifPresent(sourceDir::set));

        Button targetButton = buildButton("Output...");
        targetButton.setOnAction(e -> ofNullable(directoryChooser.showDialog(stage)).ifPresent(targetDir::set));

        Button startButton = new Button("Start");
        startButton.setOnAction(e -> startAll());

        Button stopButton = new Button("Stop");
        stopButton.setOnAction(e -> stopAll());

        return new VBox(5,
                new HBox(10, sourceButton, sourcePath),
                new HBox(10, targetButton, targetPath),
                new HBox(10, startButton, stopButton, info)
        );
    }

    private TableView buildRecordTable() {
        TableColumn<VideoRecord, String> path = new TableColumn<>("Path");
        path.setMinWidth(300);
        path.setCellValueFactory(new PropertyValueFactory<>("path"));

        TableColumn<VideoRecord, Long> duration = new TableColumn<>("Duration");
        duration.setMinWidth(100);
        duration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        duration.setCellFactory((CustomFormatCellFactory<VideoRecord, Long>) FormatUtils::formatTimeSeconds);

        TableColumn<VideoRecord, List<Long>> sourceResolution = new TableColumn<>("Resolution");
        sourceResolution.setMinWidth(100);
        sourceResolution.setCellValueFactory(new MultiPropertyValueFactory<>("sourceWidth", "sourceHeight"));
        sourceResolution.setCellFactory((CustomFormatCellFactory<VideoRecord, List<Long>>) FormatUtils::formatDimensions);

        TableColumn<VideoRecord, Long> sourceSize = new TableColumn<>("Size");
        sourceSize.setMinWidth(100);
        sourceSize.setCellValueFactory(new PropertyValueFactory<>("sourceSize"));
        sourceSize.setCellFactory(alignRight((CustomFormatCellFactory<VideoRecord, Long>) FormatUtils::formatFileSize));

        TableColumn<VideoRecord, Double> progress = new TableColumn<>("Progress");
        progress.setMinWidth(100);
        progress.setCellValueFactory(new PropertyValueFactory<>("progress"));
        progress.setCellFactory(ProgressBarTableCell.forTableColumn());

        TableColumn<VideoRecord, Long> targetSize = new TableColumn<>("Size");
        targetSize.setMinWidth(100);
        targetSize.setCellValueFactory(new PropertyValueFactory<>("targetSize"));
        targetSize.setCellFactory(alignRight((CustomFormatCellFactory<VideoRecord, Long>) size -> size == 0 ? "" : formatFileSize(size)));

        TableView<VideoRecord> table = new TableView<>(files);
        table.getColumns().addAll(path, duration, sourceResolution, sourceSize, progress, targetSize);
        return table;
    }

    private Button buildButton(String text) {
        Button button = new Button(text);
        button.setMinWidth(80);
        return button;
    }

    private void loadFiles() {
        info("Collecting files...");
        files.clear();
        listFiles(sourceDir.get()).stream()
                .map(path -> VideoRecord.newInstance(sourceDir.get(), path, targetDir.get()))
                .collect(toCollection(() -> files));
    }

    private void info(String message) {
        System.out.println(new Date() + ": " + message);
        info.setText(message);
    }

    private synchronized void startAll() {
        info("Processing...");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        files.stream()
                .filter(file -> file.getProgress() != PROGRESS_DONE)
                .map(ConvertTask::new)
                .peek(executor::execute)
                .collect(toCollection(() -> tasks));
    }

    private synchronized void stopAll() {
        info("Stopping...");
        tasks.stream()
                .filter(task -> !task.isDone())
                .forEach(task -> {
                    task.cancel();
                    task.getRecord().setProgress(PROGRESS_NOT_STARTED);
                });
        tasks.clear();
    }
}
