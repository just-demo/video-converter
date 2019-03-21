package self.ed;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

public class VideoRecord {
    private SimpleStringProperty path;
    private SimpleLongProperty duration;
    private SimpleLongProperty size;
    private SimpleIntegerProperty width;
    private SimpleIntegerProperty height;

    public VideoRecord(String path, long duration, long size, int width, int height) {
        this.path = new SimpleStringProperty(path);
        this.duration = new SimpleLongProperty(duration);
        this.size = new SimpleLongProperty(size);
        this.width = new SimpleIntegerProperty(width);
        this.height = new SimpleIntegerProperty(height);
    }

    public String getPath() {
        return path.get();
    }

    public void setPath(String path) {
        this.path.set(path);
    }

    public long getDuration() {
        return duration.get();
    }

    public void setDuration(long duration) {
        this.duration.set(duration);
    }

    public long getSize() {
        return size.get();
    }

    public void setSize(long size) {
        this.size.set(size);
    }

    public int getWidth() {
        return width.get();
    }

    public void setWidth(int width) {
        this.width.set(width);
    }

    public int getHeight() {
        return height.get();
    }

    public void setHeight(int height) {
        this.height.set(height);
    }
}
