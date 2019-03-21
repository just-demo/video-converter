package self.ed;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static self.ed.util.FileUtils.listFiles;

public class ConverterUI extends Application {

    private List<VideoFile> files = new ArrayList<>();
    private Label info = new Label();
    private Label error = new Label();

    public static void main(String[] args) {
        launch();
    }

    public void start(Stage stage) {
        // See https://docs.oracle.com/javase/8/javafx/layout-tutorial/builtin_layouts.htm#JFXLY102
        stage.setTitle("Bulk Video Converter");

        BorderPane layout = new BorderPane();
        layout.setTop(buildInputPane(stage));
        layout.setBottom(error);

        stage.setScene(new Scene(layout, 1000, 500));
        stage.show();
    }

    private Pane buildInputPane(Stage stage) {
        Label sourcePath = new Label();
        Label targetPath = new Label();

        DirectoryChooser directoryChooser = new DirectoryChooser();

        Button sourceButton = buildButton("Input...");
        sourceButton.setOnAction(e -> ofNullable(directoryChooser.showDialog(stage)).ifPresent(file -> {
            sourcePath.setText(file.getAbsolutePath());
            if (isEmpty(targetPath.getText())) {
                targetPath.setText(sourcePath.getText());
            }
            loadFiles(file);
        }));

        Button targetButton = buildButton("Output...");
        targetButton.setOnAction(e -> ofNullable(directoryChooser.showDialog(stage)).ifPresent(file -> {
            targetPath.setText(file.getAbsolutePath());
        }));

        Button startButton = buildButton("Start");
        startButton.setOnAction(e -> info("Processing..."));

        return new VBox( 5,
                new HBox(10, sourceButton, sourcePath),
                new HBox(10, targetButton, targetPath),
                new HBox(10, startButton, info)
        );
    }

    private Button buildButton(String text) {
        Button button = new Button(text);
        button.setMinWidth(80);
        return button;
    }

    private void loadFiles(File root) {
        info("Collecting files...");
        List<File> files = listFiles(root);
        AtomicLong counter = new AtomicLong();
        this.files.clear();
    }

    private void info(String message) {
        System.out.println(new Date() + ": " + message);
        info.setText(message);
    }

    private void error(String message) {
        error.setText(message);
    }
}
