package self.ed.util;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public class FileUtils {
    public static List<File> listFiles(File dir) {
        return streamFiles(dir).collect(toList());
    }

    public static Stream<File> streamFiles(File dir) {
        return dir.isDirectory() ? stream(dir.listFiles()).flatMap(FileUtils::streamFiles) : Stream.of(dir);
    }
}
