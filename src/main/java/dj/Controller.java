package dj;

import dj.filesystem.DirReader;
import dj.filesystem.Directory;
import dj.filesystem.Node;
import org.joml.Vector4f;

import java.util.ArrayList;

public class Controller {
    public ArrayList<Node> nodes = new ArrayList<>();
    Directory root;
    DirReader dr = new DirReader();

    public void run() {
        try {
            nodes = dr.getDirectories("C:");
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        new Gui(this).run();

    }
}
