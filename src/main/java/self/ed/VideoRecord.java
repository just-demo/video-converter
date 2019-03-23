package self.ed;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

public class VideoRecord {
    private SimpleStringProperty path = new SimpleStringProperty();
    private SimpleLongProperty duration = new SimpleLongProperty();
    private SimpleLongProperty size = new SimpleLongProperty();
    private SimpleIntegerProperty width = new SimpleIntegerProperty();
    private SimpleIntegerProperty height = new SimpleIntegerProperty();
    private SimpleDoubleProperty progress = new SimpleDoubleProperty(0);
    private File file;
    private Task task;

    public String getPath() {
        return path.get();
    }

    public void setPath(String path) {
        this.path.set(path);
    }

    public long getDuration() {
        return duration.get();
    }

    public void setDuration(long duration) {
        this.duration.set(duration);
    }

    public long getSize() {
        return size.get();
    }

    public void setSize(long size) {
        this.size.set(size);
    }

    public int getWidth() {
        return width.get();
    }

    public void setWidth(int width) {
        this.width.set(width);
    }

    public int getHeight() {
        return height.get();
    }

    public void setHeight(int height) {
        this.height.set(height);
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

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public static VideoRecord newInstance(File dir, String path) {
        try {
            File file = dir.toPath().resolve(path).toFile();
            FFprobe ffprobe = new FFprobe();
            FFmpegProbeResult probeResult = ffprobe.probe(file.getAbsolutePath());
            FFmpegFormat format = probeResult.getFormat();
            FFmpegStream stream = probeResult.getStreams().get(0);

            VideoRecord record = new VideoRecord();
            record.setPath(path);
            record.setDuration((long) format.duration);
            record.setSize(file.length());
            record.setWidth(stream.width);
            record.setHeight(stream.height);
            record.setFile(file);
            return record;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
