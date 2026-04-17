package dj;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Gui {
    private final String title = "Vertex NodeExplorer";
    private long window;
    private int width = 1280;
    private int height = 720;

    public void run() {
        init();
        loop();

        // Cleanup after close
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // Fehler-Callback (druckt Fehler in die Konsole)
        GLFWErrorCallback.createPrint(System.err).set();

        // GLFW initiation
        if (!glfwInit()) {
            throw new IllegalStateException("GLFW could not get loadet");
        }

        // window config (Hints)
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        // create Window
        window = glfwCreateWindow(width, height, title, NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Window could not be created");
        }

        // currently closes on esc release
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }
        });

        glfwMakeContextCurrent(window);
        // activate V Sync
        glfwSwapInterval(1);

        glfwShowWindow(window);

        GL.createCapabilities();

        // Set background color
        glClearColor(0.1f, 0.1f, 0.12f, 1.0f);
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            // clear buffer
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // TODO: code for nodes

            glfwSwapBuffers(window);

            // call events (mice, keyboard)
            glfwPollEvents();
        }
    }
}
