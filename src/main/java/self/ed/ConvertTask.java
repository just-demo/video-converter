package self.ed;

import javafx.application.Platform;
import javafx.concurrent.Task;

public class ConvertTask extends Task<Void> {
    private VideoRecord record;

    public ConvertTask(VideoRecord record) {
        this.record = record;
    }

    @Override
    public Void call() {
        System.out.println("Converting: " + record.getSourceFile() + " -> " + record.getTargetFile());
        Converter.convert(
                record.getSourceFile().getAbsolutePath(),
                record.getTargetFile().getAbsolutePath(),
                (done, total) -> {
                    // not binding progress property because we need more control on it
                    this.updateProgress(done, total);
                    Platform.runLater(() -> record.setProgress(this.getProgress()));
                }
        );
        return null;
    }

    public VideoRecord getRecord() {
        return record;
    }
}
