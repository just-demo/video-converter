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
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.function.BiConsumer;

import static java.lang.Integer.parseInt;
import static java.nio.file.Files.createDirectories;
import static java.util.Optional.ofNullable;
import static self.ed.util.Constants.TARGET_HEIGHT;

public class Converter {
    public static void convert(String sourceFile, String targetFile, BiConsumer<Long, Long> progressListener) {
        try {
            createDirectories(Paths.get(targetFile).getParent());
            FFmpeg ffmpeg = new FFmpeg();
            FFprobe ffprobe = new FFprobe();

            FFmpegProbeResult sourceInfo = ffprobe.probe(sourceFile);
            FFmpegStream inStream = sourceInfo.getStreams().get(0);
            long inDuration = (long) (1000 * ffprobe.probe(sourceFile).getFormat().duration);
            Rectangle outResolution = parseRatio(inStream.display_aspect_ratio);
            outResolution.scaleToHeight(TARGET_HEIGHT);
            ofNullable(inStream.tags.get("rotate"))
                    .map(Long::valueOf)
                    .filter(rotate -> rotate % 180 != 0)
                    .ifPresent(ignored -> outResolution.rotate());

            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(sourceFile)
                    .overrideOutputFiles(true)
                    .addOutput(targetFile)
                    .setVideoResolution(outResolution.getWidth(), outResolution.getHeight())
                    .setAudioCodec("copy")
                    .done();

            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
            FFmpegJob job = executor.createJob(builder, progress ->
                    progressListener.accept(progress.out_time_ns / 1000_000, inDuration));

            job.run();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static FileInfo getFileInfo(String file) {
        try {
            FFprobe ffprobe = new FFprobe();
            FFmpegProbeResult info = ffprobe.probe(file);
            FFmpegFormat format = info.getFormat();
            FFmpegStream stream = info.getStreams().get(0);
            return new FileInfo(
                    (long) format.duration,
                    stream.width,
                    stream.height,
                    new File(file).length()
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Rectangle parseRatio(String ratio) {
        String[] parts = ratio.split(":");
        return new Rectangle(parseInt(parts[0]), parseInt(parts[1]));
    }

//    public static void main(String[] args) throws IOException {
//        File inDir = new File("");
//        File outDir = new File("");
//        FFprobe ffprobe = new FFprobe();
//        for (String file : self.ed.util.FileUtils.listFiles(inDir)) {
//            String inFile = inDir.toPath().resolve(file).toFile().getAbsolutePath();
//            String outFile = outDir.toPath().resolve(file).toFile().getAbsolutePath().replace(".mp4", ".json");
//            FFmpegProbeResult result = ffprobe.probe(inFile);
//            writeStringToFile(new File(outFile), toJson(result), UTF_8);
//        }
//    }
}
