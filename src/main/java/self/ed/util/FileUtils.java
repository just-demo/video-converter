package self.ed.util;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;

public class FileUtils {
    public static List<String> listFiles(File dir) {
        Path dirPath = dir.toPath();
        return streamFiles(dir)
                .map(File::toPath)
                .map(dirPath::relativize)
                .map(Path::toString)
                .sorted()
                .collect(toList());
    }

    public static File buildTargetDir(File sourceDir) {
        return sourceDir.toPath().getParent().resolve(sourceDir.getName() + "_compressed").toFile();
    }

    public static File buildTargetFile(File targetDir, String sourcePath) {
        Path outPath = targetDir.toPath().resolve(sourcePath);
        return outPath.getParent().resolve(buildTargetFileName(outPath.getFileName().toString())).toFile();
    }

    private static String buildTargetFileName(String sourceFileName) {
        return getBaseName(sourceFileName) + "_compressed." + getExtension(sourceFileName);
    }

    private static Stream<File> streamFiles(File dir) {
        return dir.isDirectory() ? stream(dir.listFiles()).flatMap(FileUtils::streamFiles) : Stream.of(dir);
    }
}
