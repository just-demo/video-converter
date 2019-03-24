package self.ed;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import self.ed.util.MathUtils;

import java.io.File;

import static self.ed.Converter.getFileInfo;
import static self.ed.util.FileUtils.buildTargetFile;

public class VideoRecord {
    public static int PROGRESS_ZERO = 0;
    public static int PROGRESS_DONE = 1;
    private SimpleStringProperty path = new SimpleStringProperty();
    private SimpleLongProperty duration = new SimpleLongProperty();
    private SimpleIntegerProperty sourceWidth = new SimpleIntegerProperty();
    private SimpleIntegerProperty sourceHeight = new SimpleIntegerProperty();
    private SimpleLongProperty sourceSize = new SimpleLongProperty();
    private SimpleIntegerProperty targetWidth = new SimpleIntegerProperty();
    private SimpleIntegerProperty targetHeight = new SimpleIntegerProperty();
    private SimpleLongProperty targetSize = new SimpleLongProperty();
    private SimpleDoubleProperty compression = new SimpleDoubleProperty();
    private SimpleDoubleProperty progress = new SimpleDoubleProperty(PROGRESS_ZERO);
    private File sourceFile;
    private File targetFile;

    public VideoRecord() {
        ChangeListener<? super Number> calculateCompression = (observable, oldValue, newValue) ->
                compression.set(MathUtils.divide(targetSize.get(), sourceSize.get()));
        sourceSize.addListener(calculateCompression);
        targetSize.addListener(calculateCompression);
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
        File sourceFile = sourceDir.toPath().resolve(path).toFile();
        File targetFile = buildTargetFile(targetDir, path);
        FileInfo sourceInfo = getFileInfo(sourceFile.getAbsolutePath());

        VideoRecord record = new VideoRecord();
        record.setPath(path);
        record.setDuration(sourceInfo.getDuration());
        record.setSourceSize(sourceInfo.getSize());
        record.setSourceWidth(sourceInfo.getWidth());
        record.setSourceHeight(sourceInfo.getHeight());
        record.setSourceFile(sourceFile);
        record.setTargetFile(targetFile);
        record.progressProperty().addListener((ChangeListener<? super Number>) (observable, oldValue, newValue) -> {
            if (newValue.doubleValue() == PROGRESS_DONE) {
                record.setTargetSize(targetFile.length());
            }
        });
        if (targetFile.exists()) {
            record.setProgress(1);
        }
        return record;
    }
}
