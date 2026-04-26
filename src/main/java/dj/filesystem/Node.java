package dj.filesystem;

import dj.Camera;
import org.joml.Vector4f;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.system.MemoryStack;
import java.nio.IntBuffer;

import static org.lwjgl.nanovg.NanoVG.*;

public class Node {
    public static final NVGColor sharedColor = NVGColor.create();
    public static final NVGColor textColorObj = NVGColor.create();
    private static long lastLoadTime = 0;
    protected static double moveSpeed = 1;
    private Directory parent;
    private final float fontSize;
    private final Vector4f color;
    public double moveAngle;
    protected float x, y;
    protected double targetX, targetY;
    protected float vx = 0.0f;
    protected float vy = 0.0f;
    protected boolean isParent;
    protected float radius = 25.0f;
    private String path;
    private int imageHandle = -1;

    public Node(String path, float x, float y, Vector4f color, Directory parent, boolean isParent) {
        this.path = path;
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
        this.color = color;
        this.fontSize = Math.max(10.0f, radius / (getName().length() * 0.5f));
        this.parent = parent;
        this.isParent = isParent;
        this.moveAngle = Math.random() * 360;
    }

    public Node(float x, float y, Vector4f color, Directory parent, boolean isParent, String path) {
        this.path = path;
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
        this.color = color;
        this.fontSize = Math.max(10.0f, radius / (getName().length() * 0.5f));
        this.parent = parent;
        this.isParent = isParent;
        this.moveAngle = Math.random() * 360;
    }

    public Node(float x, float y, Vector4f color, boolean isParent, String path) {
        this.path = path;
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
        this.color = color;
        this.parent = null;
        this.fontSize = Math.max(10.0f, radius / (getName().length() * 0.5f));
        this.isParent = isParent;
        this.moveAngle = Math.random() * 360;
    }

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

    public void printSelfText(long nvg) {
        NanoVG.nvgFontSize(nvg, fontSize);
        NanoVG.nvgFontFace(nvg, "jbm");
        nvgFillColor(nvg, nvgRGBAf(1, 1, 1, 1, textColorObj));
        NanoVG.nvgTextAlign(nvg, NVG_ALIGN_CENTER | NVG_ALIGN_MIDDLE);
        NanoVG.nvgText(nvg, x, y, getName());
    }

    public void printAtPos(long nvg, float x, float y, float radius) {
        String lowerPath = path.toLowerCase();

        if (lowerPath.endsWith(".png") || lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) {

            if (imageHandle == -1) {
                if (System.currentTimeMillis() - lastLoadTime > 30) {
                    imageHandle = nvgCreateImage(nvg, path, 0);
                    lastLoadTime = System.currentTimeMillis();
                }
            }

            if (imageHandle > 0) {
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    IntBuffer w = stack.mallocInt(1);
                    IntBuffer h = stack.mallocInt(1);
                    nvgImageSize(nvg, imageHandle, w, h);

                    float imgW = w.get(0);
                    float imgH = h.get(0);

                    float maxBoxSize = radius * 2.0f;
                    float scale = Math.min(maxBoxSize / imgW, maxBoxSize / imgH);

                    float finalW = imgW * scale;
                    float finalH = imgH * scale;

                    float drawX = x - (finalW / 2.0f);
                    float drawY = y - (finalH / 2.0f);

                    NVGPaint imgPaint = NVGPaint.malloc(stack);
                    nvgImagePattern(nvg, drawX, drawY, finalW, finalH, 0.0f, imageHandle, 1.0f, imgPaint);
                    nvgBeginPath(nvg);
                    nvgRect(nvg, drawX, drawY, finalW, finalH);
                    nvgFillPaint(nvg, imgPaint);
                    nvgFill(nvg);
                }
            } else {
                drawDefaultCircle(nvg, x, y, radius);
            }
        } else {
            drawDefaultCircle(nvg, x, y, radius);
        }
    }

    private void drawDefaultCircle(long nvg, float x, float y, float radius) {
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
        String[] parts = path.split("\\\\");
        return parts[parts.length - 1];
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
        String[] paths = path.split("\\\\");
        if (parent != null) return parent;
        if (paths.length > 1) {
            String newPath = path.replace("\\" + getName(), "");
            if (newPath.equals("C:")) newPath = "C:\\";
            return new Directory(newPath, 0, 0, (Directory) this, color, true);
        } else {
            return null;
        }
    }

    public void setParent(Directory parent) {
        this.parent = parent;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
