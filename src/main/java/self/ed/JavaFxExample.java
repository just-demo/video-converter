package self.ed;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class JavaFxExample extends Application {

    private int counter = 0;

    public static void main(String[] args) {
        launch();
    }

    private void openFile(File file) {
        System.out.println(file);
    }

    public void start(Stage stage) {
        // See https://docs.oracle.com/javase/8/javafx/layout-tutorial/builtin_layouts.htm#JFXLY102
        stage.setTitle("Video Converter");

        Label message = new Label();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select...");
        Button openButton = new Button("Input...");

        openButton.setOnAction(e -> {
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                openFile(selectedFile);
                message.setText(selectedFile.toString());
            }
        });

        Button startButton = new Button("Start!");
        startButton.setOnAction(e -> message.setText("Started: " + ++counter));

        VBox buttons = new VBox(openButton, startButton);
        BorderPane layout = new BorderPane();
        layout.setTop(buttons);
        layout.setBottom(message);

        stage.setScene(new Scene(layout, 250, 250));
        stage.show();
    }
}
