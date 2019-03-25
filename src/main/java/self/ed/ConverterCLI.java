package self.ed;

import java.nio.file.Path;
import java.nio.file.Paths;

import static self.ed.util.FileUtils.buildTargetFile;
import static self.ed.util.FormatUtils.formatFileSize;
import static self.ed.util.FormatUtils.formatTimeSeconds;
import static self.ed.util.MediaUtils.videoInfo;

public class ConverterCLI {
    public static void main(String[] args) {
        String sourceFile = args[0];
        String targetFile = convert(sourceFile);
        System.out.println(sourceFile);
        print(sourceFile);
        System.out.println(targetFile);
        print(targetFile);
    }

    private static String convert(String sourceFile) {
        Path outDir = Paths.get(sourceFile).getParent();
        String targetFile = buildTargetFile(outDir.toFile(), sourceFile).getAbsolutePath();
        new ConverterProcess(sourceFile, targetFile, (current, total) -> {
            long percentage = 100 * current / total;
            System.out.println("[" + percentage + "%]");
        }).start();
        return targetFile;
    }

    private static void print(String file) {
        VideoInfo fileInfo = videoInfo(file);
        System.out.println(fileInfo.getWidth() + "x" + fileInfo.getHeight() + " " + formatTimeSeconds(fileInfo.getDuration()) + " " + formatFileSize(fileInfo.getSize()));
    }
}
