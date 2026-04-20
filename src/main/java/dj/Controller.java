package dj;

import dj.filesystem.DirReader;
import dj.filesystem.Directory;
import org.joml.Vector4f;

public class Controller {
    protected Directory currentDir;
    protected DirReader dr = new DirReader();
    private Directory root;
    private Gui gui;


    public void run() {
        gui = new Gui(this);
        reloadRoot();
        gui.run();
    }

    public void reloadRoot() {
        try {
            root = new Directory("c:\\", 0, 0, new Vector4f(1, 0, 0, 1), null, true);
            root = dr.getDirectories(root, root.getName(), 0);
            currentDir = root;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void reloadCurrentDir() {
        try {
            currentDir.setIfParent(false);
            currentDir = dr.getDirectories(currentDir, dr.getPath(currentDir), 0);
            currentDir.setIfParent(true);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void setCurrentDir(Directory dir) {
        currentDir = dir;
    }
}
