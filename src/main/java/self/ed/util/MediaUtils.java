package self.ed.util;

import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import self.ed.VideoInfo;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import static net.bramp.ffmpeg.probe.FFmpegStream.CodecType.VIDEO;
import static self.ed.util.FFmpegUtils.ffprobe;
import static self.ed.util.FFmpegUtils.getStream;

public class MediaUtils {
    public static VideoInfo videoInfo(String file) {
        try {
            FFprobe ffprobe = ffprobe();
            FFmpegProbeResult info = ffprobe.probe(file);
            FFmpegFormat format = info.getFormat();
            FFmpegStream video = getStream(info, VIDEO);
            return new VideoInfo(
                    (long) format.duration,
                    video.width,
                    video.height,
                    new File(file).length()
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
