package dj.filesystem;

import dj.Camera;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.nanovg.NanoVG;

import java.util.*;

import static dj.Gui.*;
import static org.lwjgl.nanovg.NanoVG.*;

public class Directory extends Node {
    protected static boolean isIdleState = false;
    protected ArrayList<Node> children = new ArrayList<>();
    public float startAngle = (float) (-Math.PI / 2.0f);

    public float angleStep = 0;

    public Directory(String name, float x, float y, Vector4f color, Directory parent, boolean isParent) {
        super(name, x, y, color, parent, isParent);
    }

    /**
     * sets the target position for all children and also draws them
     */
    public void printChildren(long nvg) {
        if (children.isEmpty()) return;
        float orbitRadius = 200.0f;
        nvgBeginPath(nvg);
        if (children.size() > 20) orbitRadius = 250;
        for (int i = 0; i < children.size(); i++) {
            Node child = children.get(i);
            float angle = startAngle + (i * angleStep);
            child.targetX = (float) (this.x + orbitRadius * Math.cos(angle));
            child.targetY = (float) (this.y + orbitRadius * Math.sin(angle));
            child.moveTargetPos();
            NanoVG.nvgMoveTo(nvg, this.x, this.y);
            NanoVG.nvgLineTo(nvg, child.x, child.y);
        }
        nvgRGBAf(1, 1, 1, 1, Node.sharedColor);
        NanoVG.nvgStrokeColor(nvg, Node.sharedColor);
        NanoVG.nvgStrokeWidth(nvg, 2.0f);
        NanoVG.nvgStroke(nvg);
        for (Node n : children) {
            if (n instanceof Directory n1) {
                n1.printChildren(nvg);
            }
            n.printAtPos(nvg, n.x, n.y, n.radius);
            n.printSelfText(nvg);
        }
        applyRepulsion(nvg);

    }

    private void printIdleChildren(long nvg, int width, int height, Camera camera) {
        if (children.isEmpty()) return;

        for (int i = 0; i < children.size(); i++) {
            Node child = children.get(i);
            child.moveSelf(width, height, camera);

            if (child instanceof Directory childDir) {
                childDir.printIdleChildren(nvg, width, height, camera);
            }
            child.moveTargetPos();
            child.printAtPos(nvg, child.x, child.y, child.radius);
            child.printSelfText(nvg);
        }

        applyRepulsion(nvg);
    }

    /**
     * prints the directory itself and everything that comes with it
     *
     * @param nvg nvg
     * @param width current window width
     * @param height current window height
     * @param camera the camera
     */
    public void printSelf(long nvg, int width, int height, Camera camera) {
        moveSpeed = isIdleState ? 1.0f : 0.0f;
        moveTargetPos();
        if (isIdleState) {
            printIdleChildren(nvg, width, height, camera);
        } else {
            printChildren(nvg);
        }
        if (isParent) moveSelf(width, height, camera);
        printAtPos(nvg, x, y, radius);
        printSelfText(nvg);
    }

    /**
     * returns the node that is currently hovered over
     *
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
     * NORMAL STATE: Wendet eine weiche Abstoßungskraft an, damit Nodes nicht
     * ineinander hängen.
     */
    private void checkNormalCollision(Node n1, Node n2) {
        float minDistance = 65.0f;
        float dx = n2.x - n1.x;
        float dy = n2.y - n1.y;
        float distSq = (dx * dx) + (dy * dy);

        if (distSq < (minDistance * minDistance) && distSq > 0.001f) {
            float distance = (float) Math.sqrt(distSq);
            float overlap = minDistance - distance;

            float nx = dx / distance;
            float ny = dy / distance;

            float repulsionStrength = 0.1f;
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
     * IDLE STATE: Harter Abprall wie bei Billardkugeln
     */
    private void checkIdleCollision(Node n1, Node n2) {
        float minDistance = n1.getRadius() + n2.getRadius();

        float dx = n2.x - n1.x;
        float dy = n2.y - n1.y;
        float distSq = (dx * dx) + (dy * dy);

        if (distSq < (minDistance * minDistance) && distSq > 0.001f) {
            float distance = (float) Math.sqrt(distSq);
            float overlap = minDistance - distance;

            float nx = dx / distance;
            float ny = dy / distance;

            float sepX = nx * (overlap / 2.0f);
            float sepY = ny * (overlap / 2.0f);

            double v1x = Math.cos(Math.toRadians(n1.moveAngle));
            double v1y = Math.sin(Math.toRadians(n1.moveAngle));
            double v2x = Math.cos(Math.toRadians(n2.moveAngle));
            double v2y = Math.sin(Math.toRadians(n2.moveAngle));

            double relVelX = v2x - v1x;
            double relVelY = v2y - v1y;

            double velAlongNormal = (relVelX * nx) + (relVelY * ny);

            if (velAlongNormal < 0) {
                reflectNodeAngle(n1, nx, ny);
                reflectNodeAngle(n2, nx, ny);
            }
            n1.x -= sepX;
            n1.targetX -= sepX;
            n1.y -= sepY;
            n1.targetY -= sepY;

            n2.x += sepX;
            n2.targetX += sepX;
            n2.y += sepY;
            n2.targetY += sepY;
        }
    }

    /**
     * makes a hashmap out of all children and the Directory itself The primary
     * key is the cell and the other value is the node
     *
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
     * Reflektiert den moveAngle einer Node an der Kollisionsnormalen
     */
    private void reflectNodeAngle(Node node, float nx, float ny) {
        double radians = Math.toRadians(node.moveAngle);
        double dirX = Math.cos(radians);
        double dirY = Math.sin(radians);

        double dotProduct = (dirX * nx) + (dirY * ny);

        double rx = dirX - 2 * dotProduct * nx;
        double ry = dirY - 2 * dotProduct * ny;

        node.moveAngle = Math.toDegrees(Math.atan2(ry, rx));

        node.moveAngle = (node.moveAngle % 360 + 360) % 360;
    }

    /**
     * checks for all nodes if the collision has been checkt and if not check it
     *
     * @param node node to check the collisions with
     * @param neighbors all possible neighbors
     */
    private void checkPotentialColliders(Node node, List<Node> neighbors, long nvg) {
        nvgBeginPath(nvg);
        for (Node potentialCollider : neighbors) {
            if (potentialCollider != node
                    && System.identityHashCode(node) < System.identityHashCode(potentialCollider)) {
                if (isIdleState) {

                    if (node == this || potentialCollider == this) {
                        NanoVG.nvgMoveTo(nvg, node.x, node.y);
                        NanoVG.nvgLineTo(nvg, potentialCollider.x, potentialCollider.y);
                    }
                    checkIdleCollision(node, potentialCollider);
                } else {

                    checkNormalCollision(node, potentialCollider);
                }
            }
        }
        nvgRGBAf(1, 1, 1, 1, Node.sharedColor);
        NanoVG.nvgStrokeColor(nvg, Node.sharedColor);
        NanoVG.nvgStrokeWidth(nvg, 2.0f);
        NanoVG.nvgStroke(nvg);
    }

    /**
     * checks all collisions for every Node in their directory
     */
    private void applyRepulsion(long nvg) {
        int cellSize = 100;
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
                    checkPotentialColliders(node, neighbors, nvg);
                }
            }
        }
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    public void setIdleState(boolean idleState) {
        isIdleState = idleState;
    }
}
