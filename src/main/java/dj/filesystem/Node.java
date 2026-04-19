package dj.filesystem;

import org.joml.Vector4f;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NanoVG;

import static org.lwjgl.nanovg.NanoVG.*;

public class Node {
    private float fontSize = 20.0f;
    private static final NVGColor textColor = NVGColor.create();
    public String name;
    public float x, y;
    float radius = 25.0f;
    private Vector4f color;
    Node parent;

    public Node(String name, float x, float y, Vector4f color, Node parent) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.color = color;
        this.fontSize = Math.max(10.0f, radius / (name.length() * 0.5f));
        this.parent = parent;
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

    public void printSelf(long nvg, int width, int height) {
        printAtPos(nvg, x, y, radius);
        printSelfText(nvg);
    }

    public NVGColor getColor() {
        NVGColor textColor = NVGColor.create();
        nvgRGBAf(color.x, color.y, color.z, color.w, textColor);
        return textColor;
    }
}
