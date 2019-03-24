package self.ed;

public class Rectangle {
    private int width;
    private int height;

    public Rectangle(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void scaleTo(int lessSize) {
        // Each side must be divisible by 2, otherwise ffmpeg throws an error
        lessSize = toEven(lessSize);
        if (width > height) {
            this.width = toEven(lessSize * width / height);
            this.height = lessSize;
        } else {
            this.height = toEven(lessSize * height / width);
            this.width = lessSize;
        }
    }

    private int toEven(int value) {
        return 2 * (value / 2);
    }

    @SuppressWarnings("all")
    public void rotate() {
        int width = this.width;
        int height = this.height;
        this.width = height;
        this.height = width;
    }
}
