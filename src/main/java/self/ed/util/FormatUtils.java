package self.ed.util;

import java.text.DecimalFormat;
import java.util.Collection;

import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.time.DurationFormatUtils.formatDuration;

public class FormatUtils {
    public static String formatFileSize(long bytes) {
        return new DecimalFormat("0.0").format(bytes / 1024. / 1024.) + "MB";
    }

    public static String formatCompressionRatio(double ratio) {
        return new DecimalFormat("0.00").format(ratio);
    }

    public static String formatTimeSeconds(long seconds) {
        return formatTimeMillis(seconds * 1000);
    }

    public static String formatTimeMillis(long millis) {
        return formatDuration(millis, "HH:mm:ss");
    }

    public static String formatDimensions(Collection<Long> dimensions) {
        return join(dimensions, "x");
    }
}
