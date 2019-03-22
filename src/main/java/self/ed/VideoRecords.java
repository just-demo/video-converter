package self.ed;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import self.ed.javafx.CustomFormatCellFactory;
import self.ed.javafx.MultiPropertyValueFactory;
import self.ed.util.FormatUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static javafx.collections.FXCollections.observableArrayList;
import static self.ed.util.ThreadUtils.randomSleep;

public class VideoRecords extends Application {
    private final ObservableList<VideoRecord> files = observableArrayList(
            new VideoRecord("/test/path/1", 121, 10 * 1024 * 1024, 10, 10),
            new VideoRecord("/test/path/2", 122, 20 * 1024 * 1024, 20, 10),
            new VideoRecord("/test/path/3", 123, 30 * 1024 * 1024, 20, 20)
    );

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Video Record Table");
        stage.setWidth(1000);
        stage.setHeight(500);

        TableColumn<VideoRecord, String> path = new TableColumn<>("Path");
        path.setMinWidth(500);
        path.setCellValueFactory(new PropertyValueFactory<>("path"));

        TableColumn<VideoRecord, Long> duration = new TableColumn<>("Duration");
        duration.setMinWidth(100);
        duration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        duration.setCellFactory((CustomFormatCellFactory<VideoRecord, Long>) FormatUtils::formatTimeSeconds);

        TableColumn<VideoRecord, Long> size = new TableColumn<>("Size");
        size.setMinWidth(100);
        size.setCellValueFactory(new PropertyValueFactory<>("size"));
        size.setCellFactory((CustomFormatCellFactory<VideoRecord, Long>) FormatUtils::formatFileSize);

        TableColumn<VideoRecord, List<Long>> resolution = new TableColumn<>("Resolution");
        resolution.setMinWidth(100);
        resolution.setCellValueFactory(new MultiPropertyValueFactory<>("width", "height"));
        resolution.setCellFactory((CustomFormatCellFactory<VideoRecord, List<Long>>) FormatUtils::formatDimensions);

        TableColumn<VideoRecord, Double> progress = new TableColumn<>("Progress");
        progress.setMinWidth(100);
        progress.setCellValueFactory(new PropertyValueFactory<>("progress"));
        progress.setCellFactory(ProgressBarTableCell.forTableColumn());

        TableView<VideoRecord> table = new TableView<>(files);
        table.getColumns().addAll(path, duration, size, resolution, progress);

        Button startButton = new Button("Start");
        startButton.setOnAction(e -> startAll());

        Button stopButton = new Button("Stop");
        stopButton.setOnAction(e -> stopAll());

        ProgressIndicator indicator = new ProgressIndicator(0);

        Scene scene = new Scene(new VBox(10,
                new HBox(10, startButton, stopButton, indicator),
                table
        ));
        stage.setScene(scene);
        stage.show();
    }

    private void startAll() {
        System.out.println("Starting...");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        files.stream().map(this::createTask).forEach(executor::execute);
    }

    private void stopAll() {
        System.out.println("Stopping...");
        files.stream().map(VideoRecord::getTask).filter(Objects::nonNull).forEach(task -> task.cancel(false));
    }

    private Task createTask(VideoRecord record) {
        Task task = new Task<Void>() {
            {
                resetProgress();
            }
            @Override
            public Void call() {
                int max = 50;
                for (int i = 1; i <= max; i++) {
                    if (isCancelled()) {
                        resetProgress();
                        System.out.println("Breaking...");
                        break;
                    }
                    randomSleep();
                    updateProgress(i, max);
                }
                return null;
            }

            private void resetProgress() {
                updateProgress(0, 0);
            }
        };
        record.progressProperty().bind(task.progressProperty());
        record.setTask(task);
        return task;
    }
}