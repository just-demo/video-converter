package self.ed;

import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

public class VideoFile {
    private String name;
    private String path;
    private long duration;
    private long size;
    private int width;
    private int height;

    public VideoFile(File file) {
        try {
            size = file.length();
            path = file.getAbsolutePath();
            name = FilenameUtils.getName(path);
            FFprobe ffprobe = new FFprobe();
            FFmpegProbeResult probeResult = ffprobe.probe(path);
            FFmpegFormat format = probeResult.getFormat();
            FFmpegStream stream = probeResult.getStreams().get(0);
            width = stream.width;
            height = stream.height;
            duration = (long) format.duration;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public long getDuration() {
        return duration;
    }

    public long getSize() {
        return size;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}