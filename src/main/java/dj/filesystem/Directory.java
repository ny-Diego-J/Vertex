package dj.filesystem;

import org.joml.Vector4f;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NanoVG;

import java.util.ArrayList;

import static org.lwjgl.nanovg.NanoVG.*;

public class Directory extends Node {
    protected ArrayList<Node> children = new ArrayList<>();

    public Directory(String name, float x, float y, Vector4f color, Directory parent, boolean isParent) {
        super(name, x, y, color, parent, isParent);
    }

    public void printChildren(long nvg, int screenWidth, int screenHeight) {
        if (children.isEmpty()) return;
        applyRepulsion();
        float startAngle = (float) (-Math.PI / 2.0);
        float angleStep = (float) (2 * Math.PI / children.size());
        float orbitRadius = 200.0f;
        for (int i = 0; i < children.size(); i++) {
            Node child = children.get(i);
            float angle = startAngle + (i * angleStep);
            child.targetX = (float) (this.x + orbitRadius * Math.cos(angle));
            child.targetY = (float) (this.y + orbitRadius * Math.sin(angle));
            child.moveTargetPos();
            drawLine(nvg, this.x, this.y, child.x, child.y);

        }
        for (Node n : children) {
            n.printAtPos(nvg, n.x, n.y, n.radius);
            n.printSelfText(nvg);
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
        moveTargetPos();
        if (isParent) printChildren(nvg, width, height);
        printAtPos(nvg, x, y, radius);
        super.printSelfText(nvg);
    }

    public Node getClickedNode(float mouseWorldX, float mouseWorldY) {
        float dxSelf = mouseWorldX - this.x;
        float dySelf = mouseWorldY - this.y;
        if ((dxSelf * dxSelf) + (dySelf * dySelf) <= (this.radius * this.radius)) {
            return this;

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

    private void applyRepulsion() {
        float repulsionStrength = 0.08f;
        float minDistance = 65.0f;


        for (int i = 0; i < children.size(); i++) {
            Node n1 = children.get(i);

            for (int j = i + 1; j < children.size(); j++) {
                Node n2 = children.get(j);

                float dx = n2.x - n1.x;
                float dy = n2.y - n1.y;
                float rootDx = this.x - n1.x;
                float rootDy = this.y - n1.y;

                float distSq = (dx * dx) + (dy * dy);
                float rootDistSq = (rootDx * rootDx) + (rootDy * rootDy);

                if (distSq < (minDistance * minDistance) && distSq > 0.001f) {
                    float distance = (float) Math.sqrt(distSq);

                    float overlap = minDistance - distance;

                    float nx = dx / distance;
                    float ny = dy / distance;

                    float forceX = nx * overlap * repulsionStrength;
                    float forceY = ny * overlap * repulsionStrength;

                    n1.vx -= forceX;
                    n1.vy -= forceY;

                    n2.vx += forceX;
                    n2.vy += forceY;
                }
                if (rootDistSq < (minDistance * minDistance) && rootDistSq > 0.001f) {
                    float distance = (float) Math.sqrt(rootDistSq);

                    float overlap = minDistance - distance;

                    float nx = rootDx / distance;
                    float ny = rootDy / distance;

                    float forceX = nx * overlap * repulsionStrength;
                    float forceY = ny * overlap * repulsionStrength;

                    n1.vx -= forceX;
                    n1.vy -= forceY;
                }
            }
        }
    }
}