package self.ed;

import net.bramp.ffmpeg.*;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static java.lang.Integer.parseInt;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.deleteIfExists;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.SECONDS;
import static net.bramp.ffmpeg.probe.FFmpegStream.CodecType.AUDIO;
import static net.bramp.ffmpeg.probe.FFmpegStream.CodecType.VIDEO;
import static self.ed.util.Constants.TARGET_RESOLUTION;
import static self.ed.util.FFmpegUtils.*;

public class ConverterProcess {
    private List<Process> processes = new ArrayList<>();
    private String sourceFile;
    private String targetFile;
    private BiConsumer<Long, Long> progressListener;
    private volatile boolean stopped;

    public ConverterProcess(String sourceFile, String targetFile, BiConsumer<Long, Long> progressListener) {
        this.sourceFile = sourceFile;
        this.targetFile = targetFile;
        this.progressListener = progressListener;
    }

    public void start() {
        try {
            createDirectories(Paths.get(targetFile).getParent());
            ProcessFunction processFactory = new RunProcessFunction() {
                @Override
                public Process run(List<String> args) throws IOException {
                    Process process = super.run(args);
                    processes.add(process);
                    return process;
                }
            };
            FFmpeg ffmpeg = ffmpeg(processFactory);
            FFprobe ffprobe = ffprobe(processFactory);

            FFmpegProbeResult sourceInfo = ffprobe.probe(sourceFile);
            FFmpegStream sourceVideo = getStream(sourceInfo, VIDEO);
            FFmpegStream sourceAudio = getStream(sourceInfo, AUDIO);
            long sourceDuration = (long) (1000 * sourceInfo.getFormat().duration);
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
            FFmpegJob job = executor.createJob(builder, progress -> {
                // to make sure progress is not updated by this listener after is it explicitly reset to zero
                if (!stopped) {
                    progressListener.accept(progress.out_time_ns / 1000_000, sourceDuration);
                }
            });

            job.run();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void stop() throws InterruptedException, IOException {
        stopped = true;
        // TODO: what if processes list is concurrently modified and a new process is added in the middle
        for (Process process : processes) {
            if (process.isAlive()) {
                process.destroyForcibly();
                if (!process.waitFor(10, SECONDS)) {
                    throw new RuntimeException("Waiting time elapsed before background process has exited");
                }
            }
        }
        deleteIfExists(Paths.get(targetFile));
    }

    private Rectangle parseRatio(String ratio) {
        if (ratio == null) {
            return new Rectangle(0, 0);
        }
        String[] parts = ratio.split(":");
        return new Rectangle(parseInt(parts[0]), parseInt(parts[1]));
    }
}
