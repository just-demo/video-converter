package self.ed;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import self.ed.javafx.CustomFormatCellFactory;
import self.ed.javafx.MultiPropertyValueFactory;
import self.ed.util.FormatUtils;

import java.util.List;

import static javafx.collections.FXCollections.observableArrayList;
import static self.ed.util.ThreadUtils.randomSleep;

public class VideoRecords extends Application {
    private final ObservableList<VideoRecord> data = observableArrayList(
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

        TableView<VideoRecord> table = new TableView<>(data);
        table.getColumns().addAll(path, duration, size, resolution);

        Button startButton = new Button("Start");
        ProgressIndicator progress = new ProgressIndicator(0);
        // ProgressBar progress = new ProgressBar(0);
        startButton.setOnAction(e -> convertAll(progress));

        Scene scene = new Scene(new VBox(10,
                new HBox(10, startButton, progress),
                table
        ));
        stage.setScene(scene);
        stage.show();
    }

    private void convertAll(ProgressIndicator progress) {
        System.out.println("Converting...");
        Task task = new Task<Void>() {
            @Override
            public Void call() {
                int max = 100;
                for (int i = 1; i <= max; i++) {
                    if (isCancelled()) {
                        break;
                    }
                    randomSleep();
                    updateProgress(i, max);
                }
                return null;
            }
        };

        progress.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
    }

}