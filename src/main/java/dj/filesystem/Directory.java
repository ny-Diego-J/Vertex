package dj.filesystem;

import org.joml.Vector4f;
import org.lwjgl.nanovg.NanoVG;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.nanovg.NanoVG.nvgFillColor;
import static org.lwjgl.nanovg.NanoVG.nvgRGBAf;

public class Directory extends Node {
    public ArrayList<Node> children;

    public Directory(String name, float x, float y, Vector4f color, Node parent) {
        super(name, x, y, color, parent);
    }

    @Override
    public void printSelfText(long nvg) {
        super.printSelfText(nvg);
        if (this.children != null) {
            for (Node n : children) {
                n.printSelf();
                n.printSelfText(nvg);
            }
        }
    }


}
