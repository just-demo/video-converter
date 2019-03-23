package self.ed.util;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public class FileUtils {
    public static List<String> listFiles(File dir) {
        Path dirPath = dir.toPath();
        return streamFiles(dir)
                .map(File::toPath)
                .map(dirPath::relativize)
                .map(Path::toString)
                .collect(toList());
    }

    private static Stream<File> streamFiles(File dir) {
        return dir.isDirectory() ? stream(dir.listFiles()).flatMap(FileUtils::streamFiles) : Stream.of(dir);
    }
}
