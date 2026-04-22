package dj;

public class Camera {
    private float x, y;
    private float zoom = 1.5f;

    public Camera(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZoom() {
        return zoom;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void addToX(float add) {
        x += add;
    }

    public void addToY(float add) {
        y += add;
    }

    public void multiplyToZoom(float add) {
        zoom *= add;
    }
}
