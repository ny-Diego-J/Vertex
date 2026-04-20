package dj;

import dj.filesystem.Directory;
import dj.filesystem.Node;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nanovg.NanoVG.nvgCreateFont;
import static org.lwjgl.nanovg.NanoVGGL2.NVG_ANTIALIAS;
import static org.lwjgl.nanovg.NanoVGGL2.NVG_STENCIL_STROKES;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.nanovg.NanoVGGL3.*;
import static org.lwjgl.nanovg.NanoVG.*;

public class Gui {
    private static long window;
    private boolean leftMouseButtonPressed = false;
    private boolean rightMouseButtonPressed = false;
    private final String title = "Vertex NodeExplorer";
    private int[] winWidth = new int[1];
    private int[] winHeight = new int[1];
    //TODO: animate cursor
    long handCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR);
    long arrowCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_ARROW_CURSOR);
    private Controller ct;
    private Camera camera;
    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private int width = 1280;
    private int height = 720;
    private boolean wasLeftMouseButtonPressed = false;
    private boolean wasRightMouseButtonPressed = false;
    private boolean isControllPressed = false;
    private boolean isDragging = false;

    public Gui(Controller ct) {
        this.ct = ct;
        this.camera = new Camera(0, 0);
    }

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
        glfwWindowHint(GLFW_STENCIL_BITS, 8);

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

        glfwSetFramebufferSizeCallback(window, (windowHandle, newWidth, newHeight) -> {
            this.width = newWidth;
            this.height = newHeight;
            // OpenGL direkt informieren
            glViewport(0, 0, newWidth, newHeight);
        });

        glfwSetCursorPosCallback(window, (windowHandle, xPos, yPos) -> {
            if (rightMouseButtonPressed) {
                double diffX = xPos - lastMouseX;
                double diffY = yPos - lastMouseY;

                camera.x -= (float) (diffX / camera.zoom);
                camera.y -= (float) (diffY / camera.zoom);
            }
            lastMouseX = xPos;
            lastMouseY = yPos;
        });

        glfwSetMouseButtonCallback(window, (windowHandle, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                if (action == GLFW_PRESS) {
                    leftMouseButtonPressed = true;
                } else if (action == GLFW_RELEASE) {
                    leftMouseButtonPressed = false;
                }
            }
            if (button == GLFW_MOUSE_BUTTON_RIGHT) {
                if (action == GLFW_PRESS) {
                    rightMouseButtonPressed = true;
                } else if (action == GLFW_RELEASE) {
                    rightMouseButtonPressed = false;
                }
            }
        });

        glfwSetScrollCallback(window, (w, xOffset, yOffset) -> {
                    if (isControllPressed) {
                        camera.zoom *= yOffset > 0 ? 1.02f : 0.98f;
                    } else {
                        camera.y -= (float) yOffset * 20;
                    }
                }
        );

        glfwMakeContextCurrent(window);
        // activate V Sync
        glfwSwapInterval(1);

        glfwShowWindow(window);

        GL.createCapabilities();

        glViewport(0, 0, width, height);
        // Set background color
        glClearColor(0.1f, 0.1f, 0.12f, 1.0f);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, width, height, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
    }

    private void loop() {
        long nvg = nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES);
        if (nvg == 0) {
            throw new RuntimeException("NanoVG konnte nicht initialisiert werden!");
        }

        int font = nvgCreateFont(nvg, "jbm", "src/main/resources/fonts/jbm.ttf");
        if (font == -1) {
            System.err.println("Warnung: Font konnte nicht geladen werden. Text wird nicht angezeigt.");
        }

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

            int[] fbWidth = new int[1];
            int[] fbHeight = new int[1];


            glfwGetFramebufferSize(window, fbWidth, fbHeight);

            glfwGetWindowSize(window, winWidth, winHeight);

            float pxRatio = (float) fbWidth[0] / (float) winWidth[0];

            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glOrtho(0, width, height, 0, -1, 1);

            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();

            float centerX = width / 2.0f;
            float centerY = height / 2.0f;

            glPushMatrix();
            glTranslatef(centerX, centerY, 0);
            glScalef(camera.zoom, camera.zoom, 1.0f);
            glTranslatef(-camera.x, -camera.y, 0);
            glPopMatrix();

            nvgBeginFrame(nvg, width, height, 1.0f);

            nvgSave(nvg);
            nvgTranslate(nvg, centerX, centerY);
            nvgScale(nvg, camera.zoom, camera.zoom);
            nvgTranslate(nvg, -camera.x, -camera.y);

            ct.currentDir.printSelf(nvg, winWidth[0], winHeight[0]);
            double[] rawMouseX = {0};
            double[] rawMouseY = {0};
            glfwGetCursorPos(window, rawMouseX, rawMouseY);

            float mouseWorldX = (float) ((rawMouseX[0] - centerX) / camera.zoom + camera.x);
            float mouseWorldY = (float) ((rawMouseY[0] - centerY) / camera.zoom + camera.y);

            if (isDragging) {
                if (leftMouseButtonPressed) {
                    ct.currentDir.setX(mouseWorldX);
                    ct.currentDir.setTargetX(mouseWorldX);
                    ct.currentDir.setY(mouseWorldY);
                    ct.currentDir.setTargetY(mouseWorldY);
                } else isDragging = false;
            } else {

                Node clicked = ct.currentDir.getClickedNode(mouseWorldX, mouseWorldY);
                if (clicked != null) {
                    if (leftMouseButtonPressed && !wasLeftMouseButtonPressed) {
                        if (clicked != ct.currentDir) {
                            System.out.println("sub");
                            if (clicked instanceof Directory) {
                                Directory nextDir = (Directory) clicked;

                                nextDir.setX(0);
                                nextDir.setY(0);

                                ct.setCurrentDir(nextDir);
                                ct.reloadCurrentDir();
                                System.out.println("navigate to: " + nextDir.getName());
                            } else {
                                ct.dr.openFile(clicked);
                            }
                        }
                    } else if (leftMouseButtonPressed && wasLeftMouseButtonPressed) {
                        if (clicked == ct.currentDir) {
                            ct.currentDir.setX(mouseWorldX);
                            ct.currentDir.setTargetX(mouseWorldX);
                            ct.currentDir.setY(mouseWorldY);
                            ct.currentDir.setTargetY(mouseWorldY);
                            isDragging = true;
                        }
                    } else if (rightMouseButtonPressed && !wasRightMouseButtonPressed) {
                        if (clicked == ct.currentDir && clicked.getParent() != null) {
                            Directory nextDir = clicked.getParent();

                            nextDir.setX(0);
                            nextDir.setY(0);

                            ct.setCurrentDir(nextDir);
                            ct.reloadCurrentDir();
                            System.out.println("navigate to parent directory: " + nextDir.getName());
                        }
                    }
                }
            }
            wasLeftMouseButtonPressed = leftMouseButtonPressed;
            wasRightMouseButtonPressed = rightMouseButtonPressed;
            nvgRestore(nvg);
            nvgEndFrame(nvg);

            handleInput();
            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        nvgDelete(nvg);
    }

    private void handleInput() {
        float speed = 5.0f / camera.zoom;
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) camera.y -= speed;
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) camera.y += speed;
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) camera.x -= speed;
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) camera.x += speed;
        if (glfwGetKey(window, GLFW_KEY_R) == GLFW_PRESS && glfwGetKey(window, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS)
            ct.reloadRoot();

        // Zoom with Q and E
        if (glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) camera.zoom *= 1.02f;
        if (glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS) camera.zoom /= 1.02f;
        if (glfwGetKey(window, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS) isControllPressed = true;
        if (glfwGetKey(window, GLFW_KEY_LEFT_CONTROL) == GLFW_RELEASE) isControllPressed = false;

    }
}
