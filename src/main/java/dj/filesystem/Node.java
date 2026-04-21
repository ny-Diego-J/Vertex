package dj.filesystem;

import dj.Camera;
import org.joml.Vector4f;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NanoVG;

import static org.lwjgl.nanovg.NanoVG.*;

public class Node {
    protected static final NVGColor sharedColor = NVGColor.create();
    private static final NVGColor textColor = NVGColor.create();
    protected static double moveSpeed = 1;
    private final Directory parent;
    private final float fontSize;
    private final Vector4f color;
    public double moveAngle;
    protected float x, y;
    protected double targetX, targetY;
    protected float vx = 0.0f;
    protected float vy = 0.0f;
    protected boolean isParent;
    protected float radius = 25.0f;
    private String name;

    public Node(String name, float x, float y, Vector4f color, Directory parent, boolean isParent) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
        this.color = color;
        this.fontSize = Math.max(10.0f, radius / (name.length() * 0.5f));
        this.parent = parent;
        this.isParent = isParent;
        this.moveAngle = Math.random() * 360;
    }

    /**
     * default movement of the center dot
     * 
     * @param width window width
     * @param height window height
     * @param camera current camera
     */
    protected void moveSelf(int width, int height, Camera camera) {
        double radians = Math.toRadians(moveAngle);
        double dx = moveSpeed * Math.cos(radians);
        double dy = moveSpeed * Math.sin(radians);

        double nextX = targetX + dx;
        double nextY = targetY + dy;

        float centerX = width / 2.0f;
        float centerY = height / 2.0f;

        double visibleLeft = (0 - centerX) / camera.getZoom() + camera.getX();
        double visibleRight = (width - centerX) / camera.getZoom() + camera.getX();

        double visibleTop = (0 - centerY) / camera.getZoom() + camera.getY();
        double visibleBottom = (height - centerY) / camera.getZoom() + camera.getY();

        double minX = visibleLeft + radius;
        double maxX = visibleRight - radius;

        double minY = visibleTop + radius;
        double maxY = visibleBottom - radius;

        if (nextX <= minX || nextX >= maxX) {
            dx = -dx;
            moveAngle = moveAngle > 180 ? -moveAngle + 540 : -moveAngle + 180;
        }

        if (nextY <= minY || nextY >= maxY) {
            dy = -dy;
            moveAngle = 360 - moveAngle;
        }

        moveAngle = (moveAngle % 360 + 360) % 360;

        targetX += dx;
        targetY += dy;
    }

    /**
     * prints the name of the node
     */
    public void printSelfText(long nvg) {
        NanoVG.nvgFontSize(nvg, fontSize);
        NanoVG.nvgFontFace(nvg, "jbm");
        nvgFillColor(nvg, nvgRGBAf(1, 1, 1, 1, textColor));
        NanoVG.nvgTextAlign(nvg, NVG_ALIGN_CENTER | NVG_ALIGN_MIDDLE);
        NanoVG.nvgText(nvg, x, y, name);
    }

    /**
     * prints a circle at the given position with the radius
     * 
     * @param x x position to print at
     * @param y y position to print at
     * @param radius radius of the circle
     */
    public void printAtPos(long nvg, float x, float y, float radius) {
        nvgBeginPath(nvg);
        nvgCircle(nvg, x, y, radius);
        nvgFillColor(nvg, getColor());
        nvgFill(nvg);
    }

    /**
     * moves the node the calculated distance to its target position
     */
    public void moveTargetPos() {
        float tension = 0.045f;
        float dampening = 0.85f;

        double dx = targetX - x;
        double dy = targetY - y;

        vx += (float) (dx * tension);
        vy += (float) (dy * tension);

        vx *= dampening;
        vy *= dampening;

        x += vx;
        y += vy;

        if (Math.abs(dx) < 0.5f && Math.abs(dy) < 0.5f && Math.abs(vx) < 0.1f && Math.abs(vy) < 0.1f) {
            x = (float) targetX;
            y = (float) targetY;
            vx = 0f;
            vy = 0f;
        }
    }

    /**
     * gets the color of the directory so it doesn't have to create a new one
     * 
     * @return color
     */
    public NVGColor getColor() {
        if (isParent) nvgRGBAf(1, 0, 0, 1, sharedColor);
        else nvgRGBAf(color.x, color.y, color.z, color.w, sharedColor);
        return sharedColor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getVx() {
        return vx;
    }

    public void setVx(float vx) {
        this.vx = vx;
    }

    public float getVy() {
        return vy;
    }

    public void setVy(float vy) {
        this.vy = vy;
    }

    public Directory getParent() {
        return parent;
    }

    public void setIfParent(boolean parent) {
        isParent = parent;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public double getTargetX() {
        return targetX;
    }

    public void setTargetX(double targetX) {
        this.targetX = targetX;
    }

    public double getTargetY() {
        return targetY;
    }

    public void setTargetY(double targetY) {
        this.targetY = targetY;
    }

}
