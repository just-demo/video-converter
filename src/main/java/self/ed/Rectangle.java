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

    public void scaleToHeight(int height) {
        this.width = height * this.width / this.height;
        this.height = height;
    }

    @SuppressWarnings("all")
    public void rotate() {
        int width = this.width;
        int height = this.height;
        this.width = height;
        this.height = width;
    }
}
