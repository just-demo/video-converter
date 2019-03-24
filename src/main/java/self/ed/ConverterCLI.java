package self.ed;

import java.nio.file.Path;
import java.nio.file.Paths;

import static self.ed.Converter.getFileInfo;
import static self.ed.util.FileUtils.buildTargetFile;
import static self.ed.util.FormatUtils.formatFileSize;
import static self.ed.util.FormatUtils.formatTimeSeconds;

public class ConverterCLI {
    public static void main(String[] args) {
        String inFile = args[0];
        String outFile = convert(inFile);
        System.out.println(inFile);
        print(inFile);
        System.out.println(outFile);
        print(outFile);
    }

    private static String convert(String inFile) {
        Path outDir = Paths.get(inFile).getParent();
        String outFile = buildTargetFile(outDir.toFile(), inFile).getAbsolutePath();
        Converter.convert(inFile, outFile, (current, total) -> {
            long percentage = 100 * current / total;
            System.out.println("[" + percentage + "%]");
        });
        return outFile;
    }

    private static void print(String file) {
        FileInfo fileInfo = getFileInfo(file);
        System.out.println(fileInfo.getWidth() + "x" + fileInfo.getHeight() + " " + formatTimeSeconds(fileInfo.getDuration()) + " " + formatFileSize(fileInfo.getSize()));
    }
}
