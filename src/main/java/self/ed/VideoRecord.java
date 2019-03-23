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

import static self.ed.util.FileUtils.buildOutFile;

public class VideoRecord {
    private SimpleStringProperty path = new SimpleStringProperty();
    private SimpleLongProperty duration = new SimpleLongProperty();
    private SimpleLongProperty size = new SimpleLongProperty();
    private SimpleIntegerProperty width = new SimpleIntegerProperty();
    private SimpleIntegerProperty height = new SimpleIntegerProperty();
    private SimpleDoubleProperty progress = new SimpleDoubleProperty(0);
    private File inFile;
    private File outFile;
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

    public File getInFile() {
        return inFile;
    }

    public void setInFile(File inFile) {
        this.inFile = inFile;
    }

    public File getOutFile() {
        return outFile;
    }

    public void setOutFile(File outFile) {
        this.outFile = outFile;
    }

    public static VideoRecord newInstance(File inDir, String path, File outDir) {
        try {
            File inFile = inDir.toPath().resolve(path).toFile();
            File outFile = buildOutFile(outDir, path);
            FFprobe ffprobe = new FFprobe();
            FFmpegProbeResult probeResult = ffprobe.probe(inFile.getAbsolutePath());
            FFmpegFormat format = probeResult.getFormat();
            FFmpegStream stream = probeResult.getStreams().get(0);

            VideoRecord record = new VideoRecord();
            record.setPath(path);
            record.setDuration((long) format.duration);
            record.setSize(inFile.length());
            record.setWidth(stream.width);
            record.setHeight(stream.height);
            record.setInFile(inFile);
            record.setOutFile(outFile);
            return record;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
