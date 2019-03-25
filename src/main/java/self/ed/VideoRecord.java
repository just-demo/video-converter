package self.ed;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import self.ed.util.MathUtils;

import java.io.File;

import static java.util.Optional.ofNullable;
import static self.ed.util.FileUtils.buildTargetFile;
import static self.ed.util.MediaUtils.videoInfo;

public class VideoRecord {
    public static int PROGRESS_ZERO = 0;
    public static int PROGRESS_DONE = 1;
    private final SimpleStringProperty path = new SimpleStringProperty();
    private final SimpleLongProperty duration = new SimpleLongProperty();
    private final SimpleIntegerProperty sourceWidth = new SimpleIntegerProperty();
    private final SimpleIntegerProperty sourceHeight = new SimpleIntegerProperty();
    private final SimpleLongProperty sourceSize = new SimpleLongProperty();
    private final SimpleIntegerProperty targetWidth = new SimpleIntegerProperty();
    private final SimpleIntegerProperty targetHeight = new SimpleIntegerProperty();
    private final SimpleLongProperty targetSize = new SimpleLongProperty();
    private final SimpleDoubleProperty compression = new SimpleDoubleProperty();
    private final SimpleDoubleProperty progress = new SimpleDoubleProperty(PROGRESS_ZERO);
    private final SimpleStringProperty error = new SimpleStringProperty();
    private File sourceFile;
    private File targetFile;

    public VideoRecord() {
        ChangeListener<? super Number> calculateCompression = (observable, oldValue, newValue) ->
                compression.set(MathUtils.divide(targetSize.get(), sourceSize.get()));
        sourceSize.addListener(calculateCompression);
        targetSize.addListener(calculateCompression);
        progress.addListener((ChangeListener<? super Number>) (observable, oldValue, newValue) -> {
            if (progress.get() == PROGRESS_DONE) {
                ofNullable(targetFile).map(File::length).ifPresent(this::setTargetSize);
            }
        });
    }

    public static void main(String[] args) {
        System.out.println(new SimpleDoubleProperty().get());
    }

    public String getPath() {
        return path.get();
    }

    public SimpleStringProperty pathProperty() {
        return path;
    }

    public void setPath(String path) {
        this.path.set(path);
    }

    public long getDuration() {
        return duration.get();
    }

    public SimpleLongProperty durationProperty() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration.set(duration);
    }

    public int getSourceWidth() {
        return sourceWidth.get();
    }

    public SimpleIntegerProperty sourceWidthProperty() {
        return sourceWidth;
    }

    public void setSourceWidth(int sourceWidth) {
        this.sourceWidth.set(sourceWidth);
    }

    public int getSourceHeight() {
        return sourceHeight.get();
    }

    public SimpleIntegerProperty sourceHeightProperty() {
        return sourceHeight;
    }

    public void setSourceHeight(int sourceHeight) {
        this.sourceHeight.set(sourceHeight);
    }

    public long getSourceSize() {
        return sourceSize.get();
    }

    public SimpleLongProperty sourceSizeProperty() {
        return sourceSize;
    }

    public void setSourceSize(long sourceSize) {
        this.sourceSize.set(sourceSize);
    }

    public int getTargetWidth() {
        return targetWidth.get();
    }

    public SimpleIntegerProperty targetWidthProperty() {
        return targetWidth;
    }

    public void setTargetWidth(int targetWidth) {
        this.targetWidth.set(targetWidth);
    }

    public int getTargetHeight() {
        return targetHeight.get();
    }

    public SimpleIntegerProperty targetHeightProperty() {
        return targetHeight;
    }

    public void setTargetHeight(int targetHeight) {
        this.targetHeight.set(targetHeight);
    }

    public long getTargetSize() {
        return targetSize.get();
    }

    public SimpleLongProperty targetSizeProperty() {
        return targetSize;
    }

    public void setTargetSize(long targetSize) {
        this.targetSize.set(targetSize);
    }

    public double getCompression() {
        return compression.get();
    }

    public SimpleDoubleProperty compressionProperty() {
        return compression;
    }

    public void setCompression(double compression) {
        this.compression.set(compression);
    }

    public double getProgress() {
        return progress.get();
    }

    public SimpleDoubleProperty progressProperty() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress.set(progress);
    }

    public String getError() {
        return error.get();
    }

    public SimpleStringProperty errorProperty() {
        return error;
    }

    public void setError(String error) {
        this.error.set(error);
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    public File getTargetFile() {
        return targetFile;
    }

    public void setTargetFile(File targetFile) {
        this.targetFile = targetFile;
    }

    public static VideoRecord newInstance(File sourceDir, String path, File targetDir) {
        VideoRecord record = new VideoRecord();
        try {
            record.setPath(path);
            File sourceFile = sourceDir.toPath().resolve(path).toFile();
            File targetFile = buildTargetFile(targetDir, path);
            record.setTargetFile(targetFile);
            record.setSourceFile(sourceFile);
            VideoInfo sourceInfo = videoInfo(sourceFile.getAbsolutePath());
            record.setDuration(sourceInfo.getDuration());
            record.setSourceSize(sourceInfo.getSize());
            record.setSourceWidth(sourceInfo.getWidth());
            record.setSourceHeight(sourceInfo.getHeight());
            if (targetFile.exists()) {
                record.setProgress(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            record.setError(e.getMessage());
        }
        return record;
    }
}
