package dj.filesystem;

import dj.Camera;
import org.joml.Vector4f;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NanoVG;

import static org.lwjgl.nanovg.NanoVG.*;

public class Node {
    protected static final NVGColor sharedColor = NVGColor.create();
    private static final NVGColor textColor = NVGColor.create();
    private final Directory parent;
    private final float fontSize;
    private final Vector4f color;
    public double moveAngle = Math.random() * 360;
    protected float x, y;
    protected double targetX, targetY;
    protected float vx = 0.0f;
    protected float vy = 0.0f;
    protected boolean isParent;
    protected float radius = 25.0f;
    protected double moveSpeed = 0.05;
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
    }

    public void printSelfText(long nvg) {
        NanoVG.nvgFontSize(nvg, fontSize);
        NanoVG.nvgFontFace(nvg, "jbm");
        nvgFillColor(nvg, nvgRGBAf(1, 1, 1, 1, textColor));
        NanoVG.nvgTextAlign(nvg, NVG_ALIGN_CENTER | NVG_ALIGN_MIDDLE);
        NanoVG.nvgText(nvg, x, y, name);
    }

    public void printAtPos(long nvg, float x, float y, float radius) {
        nvgBeginPath(nvg);
        nvgCircle(nvg, x, y, radius);
        nvgFillColor(nvg, getColor());
        nvgFill(nvg);
    }

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
