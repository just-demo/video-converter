package self.ed;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static self.ed.util.FormatUtils.*;

public class ConverterCLI {
    public static void main(String[] args) throws IOException {
        String inFile = "/home/pc/Desktop/=test-data=/dummy23/V90323-110350.mp4";
        String outFile = convert(inFile);
        System.out.println(inFile);
        print(inFile);
        System.out.println(outFile);
        print(outFile);
    }

    private static String convert(String inFile) throws IOException {
        Path outDir = Paths.get(inFile).getParent();
        int outWidth = 640;
        int outHeight = 480;
        String outSuffix = "_" + outWidth + "x" + outHeight;
        String outFile = outDir.resolve(getBaseName(inFile) + outSuffix + "." + getExtension(inFile)).toString();

        FFmpeg ffmpeg = new FFmpeg();
        FFprobe ffprobe = new FFprobe();
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(inFile)
                .overrideOutputFiles(true)
                .addOutput(outFile)
                .setVideoResolution(outWidth, outHeight)
                .setAudioCodec("copy")
                .done();

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        long inDurationNanos = (long) (1_000_000_000 * ffprobe.probe(inFile).getFormat().duration);

        FFmpegJob job = executor.createJob(builder, progress -> {
            long percentage = 100 * progress.out_time_ns / inDurationNanos;
            System.out.println("[" + percentage + "%] " + formatTimeNanos(progress.out_time_ns));
        });

        job.run();
        return outFile;
    }

    private static void print(String file) throws IOException {
        FFprobe ffprobe = new FFprobe();
        FFmpegProbeResult probeResult = ffprobe.probe(file);
        FFmpegFormat format = probeResult.getFormat();
        FFmpegStream stream = probeResult.getStreams().get(0);
        long size = new File(file).length();
        System.out.println(stream.width + "x" + stream.height + " " + formatTimeSeconds((long) format.duration) + " " + formatFileSize(size));
    }
}
