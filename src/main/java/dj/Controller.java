package dj;

import dj.filesystem.DirReader;
import dj.filesystem.Directory;
import dj.filesystem.Node;
import org.joml.Vector4f;

import java.util.ArrayList;

public class Controller {
    Directory root;
    DirReader dr = new DirReader();

    public void run() {
        reloadFiles();
        new Gui(this).run();
    }

    public void reloadFiles(){
        try {
            root = new Directory("C:", 0, 0, new Vector4f(1, 0, 0, 1), null);
            root = dr.getDirectories(root, "C:\\", 0);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
