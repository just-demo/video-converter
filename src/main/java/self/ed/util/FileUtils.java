package self.ed.util;

import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static self.ed.util.Constants.TARGET_HEIGHT;
import static self.ed.util.Constants.TARGET_WIDTH;

public class FileUtils {
    public static List<String> listFiles(File dir) {
        Path dirPath = dir.toPath();
        return streamFiles(dir)
                .map(File::toPath)
                .map(dirPath::relativize)
                .map(Path::toString)
                .collect(toList());
    }

    public static File buildOutDir(File inDir) {
        String time = new SimpleDateFormat("YYYYMMDD-HHmmss").format(new Date());
        return inDir.toPath().getParent().resolve(inDir.getName() + "_compressed" + "_" + time).toFile();
    }

    public static File buildOutFile(File outDir, String inPath) {
        Path outPath = outDir.toPath().resolve(inPath);
        return outPath.getParent().resolve(buildOutFileName(outPath.getFileName().toString())).toFile();
    }

    private static String buildOutFileName(String inFileName) {
        String outSuffix = "_" + TARGET_WIDTH + "x" + TARGET_HEIGHT;
        return getBaseName(inFileName) + outSuffix + "." + getExtension(inFileName);
    }

    private static Stream<File> streamFiles(File dir) {
        return dir.isDirectory() ? stream(dir.listFiles()).flatMap(FileUtils::streamFiles) : Stream.of(dir);
    }
}
