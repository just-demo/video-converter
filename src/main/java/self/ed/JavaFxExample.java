package self.ed;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class JavaFxExample extends Application {

    private int counter = 0;
    private Label message = new Label();

    public static void main(String[] args) {
        launch();
    }

    public void start(Stage stage) {
        // See https://docs.oracle.com/javase/8/javafx/layout-tutorial/builtin_layouts.htm#JFXLY102
        stage.setTitle("Video Converter");

        FileChooser fileChooser = new FileChooser();
        Button fileButton = new Button("File...");
        fileButton.setOnAction(e -> openFile(fileChooser.showOpenDialog(stage)));

        DirectoryChooser folderChooser = new DirectoryChooser();
        Button folderButton = new Button("Folder...");
        folderButton.setOnAction(e -> openFile(folderChooser.showDialog(stage)));

        Button startButton = new Button("Start!");
        startButton.setOnAction(e -> message.setText("Started: " + ++counter));

        VBox buttons = new VBox(fileButton, folderButton, startButton);
        BorderPane layout = new BorderPane();
        layout.setTop(buttons);
        layout.setBottom(message);

        stage.setScene(new Scene(layout, 500, 250));
        stage.show();
    }

    private void openFile(File file) {
        if (file != null) {
            System.out.println(file);
            message.setText(file.toString());
        }
    }
}
