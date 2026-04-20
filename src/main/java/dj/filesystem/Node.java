package dj.filesystem;

import org.joml.Vector4f;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NanoVG;

import static org.lwjgl.nanovg.NanoVG.*;

public class Node {
    private float fontSize;
    private static final NVGColor textColor = NVGColor.create();
    public String name;
    public float x, y;
    public double targetX, targetY;
    float radius = 25.0f;
    private Vector4f color;
    public Node parent;
    boolean isParent;

    public Node(String name, float x, float y, Vector4f color, Node parent, boolean isParent) {
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
        float leapSpeed = 0.05f;

        double dx = targetX - x;
        double dy = targetY - y;

        if (Math.abs(dx) < 0.5f && Math.abs(dy) < 0.5f) {
            x = (float) targetX;
            y = (float) targetY;
            return;
        }
        x += (float) (dx * leapSpeed);
        y += (float) (dy * leapSpeed);
    }

    public void printSelf(long nvg, int width, int height) {
        moveTargetPos();
        printAtPos(nvg, x, y, radius);
        printSelfText(nvg);
    }

    public NVGColor getColor() {
        NVGColor textColor = NVGColor.create();
        nvgRGBAf(color.x, color.y, color.z, color.w, textColor);
        return textColor;
    }
}
