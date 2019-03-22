package self.ed.util;

import java.util.Random;

public class ThreadUtils {
    public static void randomSleep() {
        try {
            Thread.sleep(new Random().nextInt(100));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
