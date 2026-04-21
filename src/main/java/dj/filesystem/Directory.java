package dj.filesystem;

import dj.Camera;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.nanovg.NanoVG;

import java.util.*;

import static dj.Gui.*;
import static org.lwjgl.nanovg.NanoVG.*;

public class Directory extends Node {
    protected ArrayList<Node> children = new ArrayList<>();

    public Directory(String name, float x, float y, Vector4f color, Directory parent, boolean isParent) {
        super(name, x, y, color, parent, isParent);
    }

    /**
     * sets the target position for all children and also draws them
     */
    public void printChildren(long nvg) {
        if (children.isEmpty()) return;
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
        applyRepulsion();

    }

    /**
     * Draws a line from a start point to the end point
     * @param startX starting x position
     * @param startY starting y position
     * @param endX ending x position
     * @param endY ending y position
     */
    private void drawLine(long nvg, float startX, float startY, float endX, float endY) {
        nvgBeginPath(nvg);
        NanoVG.nvgMoveTo(nvg, startX, startY);
        NanoVG.nvgLineTo(nvg, endX, endY);
        nvgRGBAf(1, 1, 1, 1, Node.sharedColor);
        NanoVG.nvgStrokeColor(nvg, Node.sharedColor);
        NanoVG.nvgStrokeWidth(nvg, 2.0f);
        NanoVG.nvgStroke(nvg);
    }

    /**
     * prints the directory itself and everything that comes with it
     * @param nvg nvg
     * @param width current window width
     * @param height current window height
     * @param camera the camera
     */
    public void printSelf(long nvg, int width, int height, Camera camera) {
        moveTargetPos();
        printChildren(nvg);
        printAtPos(nvg, x, y, radius);
        if (isParent) moveRoot(width, height, camera);
        super.printSelfText(nvg);
    }

    /**
     * default movement of the center dot
     * @param width window width
     * @param height window height
     * @param camera current camera
     */
    private void moveRoot(int width, int height, Camera camera) {
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
     * returns the node that is currently hovered over
     * @param mouseWorldX mouse x position
     * @param mouseWorldY mouse y position
     * @return hovered node
     */
    public Node getHoverdNode(float mouseWorldX, float mouseWorldY) {
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


    /**
     * checks the collisions between two nodes and applies repulsion to move them away from each other
     * @param n1 first node
     * @param n2 second node
     */
    private void checkCollisions(Node n1, Node n2) {
        float repulsionStrength = 0.1f;
        float minDistance = 65.0f;
        float dx = n2.x - n1.x;
        float dy = n2.y - n1.y;

        float distSq = (dx * dx) + (dy * dy);

        if (distSq < (minDistance * minDistance) && distSq > 0.001f) {
            float distance = (float) Math.sqrt(distSq);

            float overlap = minDistance - distance;

            float nx = dx / distance;
            float ny = dy / distance;

            float forceX = nx * overlap * repulsionStrength;
            float forceY = ny * overlap * repulsionStrength;

            if (n1 == this) {
                n2.vx += forceX * 20;
                n2.vy += forceY * 20;

            } else if (n2 == this) {
                n1.vx -= forceX * 20;
                n1.vy -= forceY * 20;
            } else {
                n1.vx -= forceX;
                n1.vy -= forceY;

                n2.vx += forceX;
                n2.vy += forceY;
            }
        }
    }


    /**
     * makes a hashmap out of all children and the Directory itself
     * The primary key is the cell and the other value is the node
     * @param cellSize grid size
     * @return hashmap with all children
     */
    private HashMap<String, List<Node>> getCellList(int cellSize) {
        HashMap<String, List<Node>> grid = new HashMap<>();
        for (Node node : children) {
            int gridX = Math.floorDiv((int) node.x, cellSize);
            int gridY = Math.floorDiv((int) node.y, cellSize);
            grid.computeIfAbsent(gridX + "," + gridY, k -> new ArrayList<>()).add(node);
        }
        int parentX = Math.floorDiv((int) this.x, cellSize);
        int parentY = Math.floorDiv((int) this.y, cellSize);
        grid.computeIfAbsent(parentX + "," + parentY, k -> new ArrayList<>()).add(this);
        return grid;
    }

    /**
     * checks for all nodes if the collision has been checkt and if not check it
     * @param node node to check the collisions with
     * @param neighbors all possible neighbors
     */
    private void checkPotentialColliders(Node node, List<Node> neighbors) {
        for (Node potentialCollider : neighbors) {
            if (potentialCollider != node && System.identityHashCode(node) < System.identityHashCode(potentialCollider)) {
                checkCollisions(node, potentialCollider);
            }
        }
    }

    /**
     * checks all collisions for every Node in their directory
     */
    private void applyRepulsion() {
        int cellSize = 200;
        List<Node> allNodesToUpdate = new ArrayList<>(children);
        allNodesToUpdate.add(this);

        HashMap<String, List<Node>> grid = getCellList(cellSize);
        for (Node node : allNodesToUpdate) {
            int myGridX = Math.floorDiv((int) node.x, cellSize);
            int myGridY = Math.floorDiv((int) node.y, cellSize);
            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                for (int offsetY = -1; offsetY <= 1; offsetY++) {
                    String neighborKey = (myGridX + offsetX) + "," + (myGridY + offsetY);
                    List<Node> neighbors = grid.getOrDefault(neighborKey, Collections.emptyList());
                    checkPotentialColliders(node, neighbors);
                }
            }
        }
    }
}