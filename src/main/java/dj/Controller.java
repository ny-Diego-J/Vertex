package dj;

import dj.filesystem.DirReader;
import dj.filesystem.Directory;
import org.joml.Vector4f;

public class Controller {
    Directory root;
    Directory currentDir;
    DirReader dr = new DirReader();
    Gui gui;


    public void run() {
        gui = new Gui(this);
        reloadRoot();
        gui.run();
    }

    public void reloadRoot() {
        try {
            root = new Directory("C:\\", 0, 0, new Vector4f(1, 0, 0, 1), null, true);
            root.parent = root;
            root = dr.getDirectories(root, "C:\\", 0);
            currentDir = root;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void reloadCurrentDir() {
        try {
            currentDir = dr.getDirectories(currentDir, dr.getPath(currentDir), 0);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void setCurrentDir(Directory dir) {
        currentDir = dir;
    }
}
