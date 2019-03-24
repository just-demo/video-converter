package self.ed.util;

public class MathUtils {
    public static double divide(long x, long y) {
        return x == 0 || y == 0 ? 0 : (double) x / y;
    }
}
