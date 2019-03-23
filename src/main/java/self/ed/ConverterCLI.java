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

import static self.ed.util.FileUtils.buildOutFile;
import static self.ed.util.FormatUtils.*;

public class ConverterCLI {
    public static void main(String[] args) throws IOException {
        String inFile = args[0];
        String outFile = convert(inFile);
        System.out.println(inFile);
        print(inFile);
        System.out.println(outFile);
        print(outFile);
    }

    private static String convert(String inFile) throws IOException {
        Path outDir = Paths.get(inFile).getParent();
        String outFile = buildOutFile(outDir.toFile(), inFile).getAbsolutePath();
        Converter.convert(inFile, outFile, (current, total) -> {
            long percentage = 100 * current / total;
            System.out.println("[" + percentage + "%]");
        });
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
