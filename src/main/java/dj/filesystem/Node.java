package dj.filesystem;

import org.joml.Vector4f;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NanoVG;

import static org.lwjgl.nanovg.NanoVG.*;

public class Node {
    private static final NVGColor textColor = NVGColor.create();
    public String name;
    public float x, y;
    public double targetX, targetY;
    public float vx = 0.0f;
    public float vy = 0.0f;
    public Directory parent;
    public boolean isParent;
    float radius = 25.0f;
    private float fontSize;
    private Vector4f color;

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
