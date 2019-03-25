package self.ed;

public class VideoInfo {
    private long duration;
    private int width;
    private int height;
    private long size;

    public VideoInfo(long duration, int width, int height, long size) {
        this.duration = duration;
        this.width = width;
        this.height = height;
        this.size = size;
    }

    public long getDuration() {
        return duration;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public long getSize() {
        return size;
    }
}
