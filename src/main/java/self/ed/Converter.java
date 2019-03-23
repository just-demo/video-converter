package self.ed;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.function.BiConsumer;

import static java.nio.file.Files.createDirectories;
import static self.ed.util.Constants.TARGET_HEIGHT;
import static self.ed.util.Constants.TARGET_WIDTH;

public class Converter {
    public static void convert(String inFile, String outFile, BiConsumer<Long, Long> progressListener) {
        try {
            createDirectories(Paths.get(outFile).getParent());
            FFmpeg ffmpeg = new FFmpeg();
            FFprobe ffprobe = new FFprobe();
            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(inFile)
                    .overrideOutputFiles(true)
                    .addOutput(outFile)
                    .setVideoResolution(TARGET_WIDTH, TARGET_HEIGHT)
                    .setAudioCodec("copy")
                    .done();

            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
            long total = (long) (1000 * ffprobe.probe(inFile).getFormat().duration);

            FFmpegJob job = executor.createJob(builder, progress ->
                    progressListener.accept(progress.out_time_ns / 1000_000, total));

            job.run();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
