package self.ed;

import javafx.application.Platform;
import javafx.concurrent.Task;

import static self.ed.VideoRecord.PROGRESS_DONE;
import static self.ed.VideoRecord.PROGRESS_ZERO;

public class ConverterTask extends Task<Void> {
    private VideoRecord record;
    private Runnable onComplete;
    private ConverterProcess converter;

    public ConverterTask(VideoRecord record, Runnable onComplete) {
        this.record = record;
        this.onComplete = onComplete;
    }

    @Override
    public Void call() {
        System.out.println("Converting: " + record.getSourceFile() + " -> " + record.getTargetFile());
        converter = new ConverterProcess(
                record.getSourceFile().getAbsolutePath(),
                record.getTargetFile().getAbsolutePath(),
                (done, total) -> {
                    // not binding progress property because we need more control on it
                    this.updateProgress(done, total);
                    Platform.runLater(() -> record.setProgress(this.getProgress()));
                }
        );
        converter.start();
        record.setProgress(PROGRESS_DONE);
        Platform.runLater(onComplete);
        return null;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (converter != null) {
            try {
                converter.stop();
            } catch (Exception e) {
                e.printStackTrace();
                record.setError(e.getMessage());
            }
        }
        boolean result = super.cancel(mayInterruptIfRunning);
        record.setProgress(PROGRESS_ZERO);
        return result;
    }
}
