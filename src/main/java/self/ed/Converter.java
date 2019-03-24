package self.ed;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import net.bramp.ffmpeg.probe.FFmpegStream.CodecType;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.function.BiConsumer;

import static java.lang.Integer.parseInt;
import static java.nio.file.Files.createDirectories;
import static java.util.Optional.ofNullable;
import static net.bramp.ffmpeg.probe.FFmpegStream.CodecType.AUDIO;
import static net.bramp.ffmpeg.probe.FFmpegStream.CodecType.VIDEO;
import static self.ed.util.Constants.TARGET_RESOLUTION;

public class Converter {
    public static void convert(String sourceFile, String targetFile, BiConsumer<Long, Long> progressListener) {
        try {
            createDirectories(Paths.get(targetFile).getParent());
            FFmpeg ffmpeg = new FFmpeg();
            FFprobe ffprobe = new FFprobe();

            FFmpegProbeResult sourceInfo = ffprobe.probe(sourceFile);
            FFmpegStream sourceVideo = getStream(sourceInfo, VIDEO);
            FFmpegStream sourceAudio = getStream(sourceInfo, AUDIO);
            long sourceDuration = (long) (1000 * ffprobe.probe(sourceFile).getFormat().duration);
            Rectangle targetResolution = parseRatio(sourceVideo.display_aspect_ratio);
            if (targetResolution.getWidth() == 0 || targetResolution.getHeight() == 0) {
                targetResolution.setWidth(sourceVideo.width);
                targetResolution.setHeight(sourceVideo.height);
            }
            targetResolution.scaleTo(TARGET_RESOLUTION);
            ofNullable(sourceVideo.tags.get("rotate"))
                    .map(Long::valueOf)
                    .filter(rotate -> rotate % 180 != 0)
                    .ifPresent(ignored -> targetResolution.rotate());

            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(sourceFile)
                    .overrideOutputFiles(true)
                    .addOutput(targetFile)
                    .setVideoResolution(targetResolution.getWidth(), targetResolution.getHeight())
                    // ffmpeg had problem copying audio of amr_nb type
                    .setAudioCodec("amr_nb".equals(sourceAudio.codec_name) ? "aac" : "copy")
                    .done();

            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
            FFmpegJob job = executor.createJob(builder, progress ->
                    progressListener.accept(progress.out_time_ns / 1000_000, sourceDuration));

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
            FFmpegStream video = getStream(info, VIDEO);
            return new FileInfo(
                    (long) format.duration,
                    video.width,
                    video.height,
                    new File(file).length()
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static FFmpegStream getStream(FFmpegProbeResult info, CodecType type) {
        // Normally video stream goes first, but not always.
        // Also sometimes there are multiple video steams and the first one seems to be the desired one.
        return info.getStreams().stream()
                .filter(stream -> type.equals(stream.codec_type))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot retrieve " + type + " stream"));
    }

    private static Rectangle parseRatio(String ratio) {
        String[] parts = ratio.split(":");
        return new Rectangle(parseInt(parts[0]), parseInt(parts[1]));
    }

//    public static void main(String[] args) throws Exception {
//        File dir = new File("");
//        List<String> sourceFiles = listFiles(dir);
//        System.out.println(sourceFiles.size());
//
//        Map<String, Long> codecs = new HashMap<>();
//        FFprobe ffprobe = new FFprobe();
//        AtomicLong counter = new AtomicLong();
//        for (String file : sourceFiles) {
//            System.out.println(counter.incrementAndGet());
//            String sourceFile = dir.toPath().resolve(file).toFile().getAbsolutePath();
//            FFmpegProbeResult result = ffprobe.probe(sourceFile);
//            String codec = getStream(result, AUDIO).codec_name;
//            codecs.put(codec, ofNullable(codecs.get(codec)).orElse(0L) + 1);
//        }
//        System.out.println(JsonUtils.toJson(codecs));
//    }

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
