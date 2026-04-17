package dj.filesystem;

import org.joml.Vector4f;

import java.util.HashMap;
import java.util.Map;

public class Directory extends Node {
    private Map<String, Node> children = new HashMap<>();

    public Directory(String name, float x, float y, Vector4f color, Map<String, Node> children) {
        super(name, x, y, color);
        this.children = children;
    }
}
