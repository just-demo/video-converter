package self.ed;

import javafx.application.Platform;
import javafx.concurrent.Task;

import static self.ed.VideoRecord.PROGRESS_DONE;

public class ConverterTask extends Task<Void> {
    private VideoRecord record;
    private Runnable onComplete;

    public ConverterTask(VideoRecord record, Runnable onComplete) {
        this.record = record;
        this.onComplete = onComplete;
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
        record.setProgress(PROGRESS_DONE);
        Platform.runLater(onComplete);
        return null;
    }

    public VideoRecord getRecord() {
        return record;
    }
}
