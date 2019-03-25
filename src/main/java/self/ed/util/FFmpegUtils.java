package self.ed.util;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.ProcessFunction;
import net.bramp.ffmpeg.RunProcessFunction;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;

import java.io.IOException;

public class FFmpegUtils {
    public static FFprobe ffprobe() throws IOException {
        return ffprobe(new RunProcessFunction());
    }

    public static FFmpeg ffmpeg(ProcessFunction runFunction) throws IOException {
        return new FFmpeg(runFunction);
    }

    public static FFprobe ffprobe(ProcessFunction runFunction) throws IOException {
        return new FFprobe(runFunction);
    }

    public static FFmpegStream getStream(FFmpegProbeResult info, FFmpegStream.CodecType type) {
        // Normally video stream goes first, but not always.
        // Also sometimes there are multiple video steams and the first one seems to be the desired one.
        return info.getStreams().stream()
                .filter(stream -> type.equals(stream.codec_type))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot retrieve " + type + " stream"));
    }
}
