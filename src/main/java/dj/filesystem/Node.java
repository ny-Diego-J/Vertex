package dj.filesystem;

import org.joml.Vector4f;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NanoVG;

import static org.lwjgl.nanovg.NanoVG.nvgFillColor;
import static org.lwjgl.nanovg.NanoVG.nvgRGBAf;
import static org.lwjgl.opengl.GL11.*;

public class Node {
    private static final NVGColor textColor = NVGColor.create();
    String name;
    float x, y;
    float width = 100.0f;
    float height = 100.0f;
    private Vector4f color;

    public Node(String name, float x, float y, Vector4f color) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public void printSelfText(long nvg) {
        NanoVG.nvgFontSize(nvg, 32.0f);
        NanoVG.nvgFontFace(nvg, "jbm");
        nvgFillColor(nvg, nvgRGBAf(1, 1, 1, 1, textColor));
        NanoVG.nvgTextAlign(nvg, NanoVG.NVG_ALIGN_CENTER);
        NanoVG.nvgText(nvg, x + width / 2, y + height / 2, name);
    }

    public void printSelf() {
        glColor4f(color.x, color.y, color.z, color.w);
        glBegin(GL_QUADS);
        glVertex2f(x, y);
        glVertex2f(x + width, y);
        glVertex2f(x + width, y + height);
        glVertex2f(x, y + height);
        glEnd();

        glColor4f(color.x + 0.2f, color.y + 0.2f, color.z + 0.2f, 1.0f); // Etwas heller
        glLineWidth(2f);
        glBegin(GL_LINE_LOOP);
        glVertex2f(x, y);
        glVertex2f(x + width, y);
        glVertex2f(x + width, y + height);
        glVertex2f(x, y + height);
        glEnd();


        // 3. TODO: Hier kommt dein drawText() aus dem Editor rein!
        // drawText(name, x + 5, y + 5);
    }
}
