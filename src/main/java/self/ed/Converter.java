package self.ed;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.function.BiConsumer;

import static java.lang.Integer.parseInt;
import static java.nio.file.Files.createDirectories;
import static java.util.Optional.ofNullable;
import static self.ed.util.Constants.TARGET_HEIGHT;

public class Converter {
    public static void convert(String inFile, String outFile, BiConsumer<Long, Long> progressListener) {
        try {
            createDirectories(Paths.get(outFile).getParent());
            FFmpeg ffmpeg = new FFmpeg();
            FFprobe ffprobe = new FFprobe();

            FFmpegProbeResult sourceInfo = ffprobe.probe(inFile);
            FFmpegStream inStream = sourceInfo.getStreams().get(0);
            long inDuration = (long) (1000 * ffprobe.probe(inFile).getFormat().duration);
            Rectangle outResolution = parseRatio(inStream.display_aspect_ratio);
            outResolution.scaleToHeight(TARGET_HEIGHT);
            ofNullable(inStream.tags.get("rotate"))
                    .map(Long::valueOf)
                    .filter(rotate -> rotate % 180 != 0)
                    .ifPresent(ignored -> outResolution.rotate());

            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(inFile)
                    .overrideOutputFiles(true)
                    .addOutput(outFile)
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
