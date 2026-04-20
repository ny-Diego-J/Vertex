package dj.filesystem;

import dj.Camera;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NanoVG;

import java.util.ArrayList;

import static dj.Gui.*;
import static org.lwjgl.nanovg.NanoVG.*;

public class Directory extends Node {
    protected ArrayList<Node> children = new ArrayList<>();

    public Directory(String name, float x, float y, Vector4f color, Directory parent, boolean isParent) {
        super(name, x, y, color, parent, isParent);
    }

    public void printChildren(long nvg) {
        if (children.isEmpty()) return;
        applyRepulsion();
        float startAngle = (float) (-Math.PI / 2.0);
        float angleStep = (float) (2 * Math.PI / children.size());
        float orbitRadius = 200.0f;
        if (children.size() > 20) orbitRadius = 250;
        for (int i = 0; i < children.size(); i++) {
            Node child = children.get(i);
            float angle = startAngle + (i * angleStep);
            child.targetX = (float) (this.x + orbitRadius * Math.cos(angle));
            child.targetY = (float) (this.y + orbitRadius * Math.sin(angle));
            child.moveTargetPos();
            drawLine(nvg, this.x, this.y, child.x, child.y);

        }
        for (Node n : children) {
            if (n instanceof Directory) {
                Directory n1 = (Directory) n;
                n1.printChildren(nvg);
            }
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
    public void printSelf(long nvg, int width, int height, Camera camera) {
        moveTargetPos();
        printChildren(nvg);
        printAtPos(nvg, x, y, radius);
        if (isParent) moveRoot(width, height, camera);
        super.printSelfText(nvg);
    }

    private void moveRoot(int width, int height, Camera camera) {
        double radians = Math.toRadians(moveAngle);
        double dx = moveSpeed * Math.cos(radians);
        double dy = moveSpeed * Math.sin(radians);

        double nextX = targetX + dx;
        double nextY = targetY + dy;

        float centerX = width / 2.0f;
        float centerY = height / 2.0f;

        double visibleLeft = (0 - centerX) / camera.zoom + camera.x;
        double visibleRight = (width - centerX) / camera.zoom + camera.x;

        double visibleTop = (0 - centerY) / camera.zoom + camera.y;
        double visibleBottom = (height - centerY) / camera.zoom + camera.y;

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

    public Node getClickedNode(float mouseWorldX, float mouseWorldY) {
        float dxSelf = mouseWorldX - this.x;
        float dySelf = mouseWorldY - this.y;
        if ((dxSelf * dxSelf) + (dySelf * dySelf) <= (this.radius * this.radius)) {
            GLFW.glfwSetCursor(window, handCursor);
            return this;

        }

        for (Node n : children) {
            float dx = mouseWorldX - n.x;
            float dy = mouseWorldY - n.y;

            float distanceSquared = (dx * dx) + (dy * dy);
            float radiusSquared = n.radius * n.radius;

            if (distanceSquared <= radiusSquared) {
                GLFW.glfwSetCursor(window, handCursor);
                return n;
            }
        }
        GLFW.glfwSetCursor(window, arrowCursor);
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