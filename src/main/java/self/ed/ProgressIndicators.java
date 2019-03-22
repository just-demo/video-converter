package self.ed;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.Random;
import java.util.stream.Stream;

public class ProgressIndicators extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        ProgressIndicator[] indicators = Stream.generate(() -> new ProgressIndicator(0))
                .limit(10)
                .toArray(ProgressIndicator[]::new);
        Pane root = new HBox(indicators);
        stage.setScene(new Scene(root, 300, 300));

        for (ProgressIndicator indicator: indicators) {
            new Thread(() -> {
                for (int i = 1; i <= 100; i++) {
                    sleep();
                    double progress = 0.01 * i;
                    Platform.runLater(() -> indicator.setProgress(progress));
                }
            }).start();
        }

        stage.show();

    }

    private void sleep() {
        try {
            Thread.sleep(new Random().nextInt(100));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}