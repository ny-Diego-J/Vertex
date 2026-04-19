package dj.filesystem;

import org.joml.Vector4f;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NanoVG;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static dj.Gui.window;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nanovg.NanoVG.*;

public class Directory extends Node {
    public ArrayList<Node> children = new ArrayList<>();

    public Directory(String name, float x, float y, Vector4f color, Node parent) {
        super(name, x, y, color, parent);
    }

    public void printChildren(long nvg, int screenWidth, int screenHeight) {
        if (children.isEmpty()) return;

        float cx = screenWidth / 2.0f;
        float cy = screenHeight / 2.0f;
        float startAngle = (float) (-Math.PI / 2.0);
        float angleStep = (float) (2 * Math.PI / children.size());
        float orbitRadius = 200.0f;
        for (int i = 0; i < children.size(); i++) {
            Node child = children.get(i);
            float angle = startAngle + (i * angleStep);
            child.x = (float) (this.x + orbitRadius * Math.cos(angle));
            child.y = (float) (this.y + orbitRadius * Math.sin(angle));
            drawLine(nvg, this.x, this.y, child.x, child.y);
            child.printSelf(nvg, screenWidth, screenHeight);
        }
    }

    private void drawLine(long nvg, float startX, float startY, float endX, float endY) {
        nvgBeginPath(nvg);
        NanoVG.nvgMoveTo(nvg, startX, startY);
        NanoVG.nvgLineTo(nvg, endX, endY);
        NVGColor lineColor = NVGColor.create();
        nvgRGBAf(1, 1, 1, 1, lineColor);
        NanoVG.nvgStrokeColor(nvg, lineColor);
        NanoVG.nvgStrokeWidth(nvg, 2.0f);
        NanoVG.nvgStroke(nvg);
    }

    @Override
    public void printSelf(long nvg, int width, int height) {
        printChildren(nvg, width, height);
        printAtPos(nvg, x, y, radius);
        super.printSelfText(nvg);
    }

    public Node getClickedNode(float mouseWorldX, float mouseWorldY) {
        float dxSelf = mouseWorldX - this.x;
        float dySelf = mouseWorldY - this.y;
        if ((dxSelf * dxSelf) + (dySelf * dySelf) <= (this.radius * this.radius)) {
            return parent;
        }

        for (Node n : children) {
            float dx = mouseWorldX - n.x;
            float dy = mouseWorldY - n.y;

            float distanceSquared = (dx * dx) + (dy * dy);
            float radiusSquared = n.radius * n.radius;

            if (distanceSquared <= radiusSquared) {
                return n;
            }
        }


        return null;
    }
}