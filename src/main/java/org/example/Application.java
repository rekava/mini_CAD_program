package org.example;

import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL33.*;
import java.util.List;

public class Application {
    Window window;
    Triangle tri;
    Rectangle rec;
    Renderer renderer;
    Camera camera;
    GuiManager guiManager;
    SelectionManager selectionManager;
    ToolManager toolManager;
    CommandManager commandManager;
    Grid grid; // Добавляем сетку
    Shader shader; // Сохраняем шейдер для сетки

    void init() {
        if (!glfwInit()) {
            throw new IllegalStateException();
        }

        window = new Window(1280, 720);
        camera = new Camera(window.getW(), window.getH());
        glfwMakeContextCurrent(window.getWin());
        GL.createCapabilities();
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

        shader = new Shader("src/main/resources/shaders/vertex.glsl", "src/main/resources/shaders/fragment.glsl");
        shader.create();

        tri = new Triangle(shader);
        rec = new Rectangle(shader);
        tri.transform.setPosition(-0.5f, 0);
        rec.transform.setPosition(0.5f, 0);

        Scene scene = new Scene();
        renderer = new Renderer(scene);
        scene.add(tri);
        scene.add(rec);

        selectionManager = new SelectionManager(scene, camera);
        commandManager = new CommandManager();
        toolManager = new ToolManager(scene, camera, shader, selectionManager, commandManager);

        // Создаем сетку
        grid = new Grid();

        guiManager = new GuiManager(selectionManager, camera);
        guiManager.setToolManager(toolManager);
        guiManager.setGrid(grid); // Передаем сетку в GuiManager
        guiManager.init(window.getWin(), scene, commandManager);

        setupCallbacks();
        glfwShowWindow(window.getWin());
    }

    private void setupCallbacks() {
        glfwSetFramebufferSizeCallback(window.getWin(), (w, width, height) -> {
            glViewport(0, 0, width, height);
            camera.setViewportSize(width, height);
            window.setWidth(width);
            window.setHeight(height);
        });

        glfwSetKeyCallback(window.getWin(), (w, key, scancode, action, mods) -> {
            guiManager.getImGuiGlfw().keyCallback(w, key, scancode, action, mods);

            if (imgui.ImGui.getIO().getWantCaptureKeyboard()) return;

            boolean ctrlPressed = (mods & GLFW_MOD_CONTROL) != 0;

            if (action == GLFW_PRESS) {
                if (ctrlPressed && key == GLFW_KEY_C) {
                    selectionManager.copySelected();
                    return;
                }
                if (ctrlPressed && key == GLFW_KEY_V) {
                    selectionManager.paste();
                    return;
                }
                if (ctrlPressed && key == GLFW_KEY_Z) {
                    if (commandManager != null) commandManager.undo();
                    return;
                }
                if (ctrlPressed && key == GLFW_KEY_Y) {
                    if (commandManager != null) commandManager.redo();
                    return;
                }
                if (key == GLFW_KEY_DELETE || key == GLFW_KEY_BACKSPACE) {
                    deleteSelectedObjects();
                    return;
                }
                // Добавляем горячую клавишу для сетки G
                if (key == GLFW_KEY_G) {
                    if (grid != null) {
                        grid.setEnabled(!grid.isEnabled());
                    }
                    return;
                }
            }

            float moveSpeed = 0.1f * camera.zoom;
            if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                if (key == GLFW_KEY_W || key == GLFW_KEY_UP) camera.position.y += moveSpeed;
                if (key == GLFW_KEY_S || key == GLFW_KEY_DOWN) camera.position.y -= moveSpeed;
                if (key == GLFW_KEY_A || key == GLFW_KEY_LEFT) camera.position.x -= moveSpeed;
                if (key == GLFW_KEY_D || key == GLFW_KEY_RIGHT) camera.position.x += moveSpeed;

                if (key == GLFW_KEY_KP_ADD || key == GLFW_KEY_EQUAL) camera.zoom -= 0.1f;
                if (key == GLFW_KEY_KP_SUBTRACT || key == GLFW_KEY_MINUS) camera.zoom += 0.1f;

                if (camera.zoom < 0.1f) camera.zoom = 0.1f;
                if (camera.zoom > 10f) camera.zoom = 10f;
                camera.updateProjection(window.getWidth(), window.getHeight());
            }
        });

        glfwSetCharCallback(window.getWin(), (w, codepoint) -> {
            guiManager.getImGuiGlfw().charCallback(w, codepoint);
        });

        glfwSetScrollCallback(window.getWin(), (w, xoffset, yoffset) -> {
            guiManager.getImGuiGlfw().scrollCallback(w, xoffset, yoffset);
            if (imgui.ImGui.getIO().getWantCaptureMouse()) return;

            camera.zoom -= yoffset * 0.1f;
            if (camera.zoom < 0.1f) camera.zoom = 0.1f;
            if (camera.zoom > 10f) camera.zoom = 10f;
            camera.updateProjection(window.getWidth(), window.getHeight());
        });

        glfwSetMouseButtonCallback(window.getWin(), (w, button, action, mods) -> {
            guiManager.getImGuiGlfw().mouseButtonCallback(w, button, action, mods);
            if (imgui.ImGui.getIO().getWantCaptureMouse()) return;

            double[] x = new double[1];
            double[] y = new double[1];
            glfwGetCursorPos(window.getWin(), x, y);

            toolManager.mouseButtonCallback(button, action, mods, x[0], y[0]);
        });

        glfwSetCursorPosCallback(window.getWin(), (w, x, y) -> {
            toolManager.cursorPosCallback(x, y);
        });
    }

    private void deleteSelectedObjects() {
        if (selectionManager == null) return;
        List<Shape> selectedShapes = selectionManager.getSelectedShapes();
        if (commandManager != null && !selectedShapes.isEmpty()) {
            commandManager.execute(new DeleteCommand(renderer.currentScene, selectedShapes));
        }
    }

    void loop() {
        while (!glfwWindowShouldClose(window.getWin())) {
            renderer.clear();

            // Рендерим сетку первой (в фоне)
            if (grid != null && grid.isEnabled()) {
                grid.render(camera, shader);
            }

            renderer.render(camera);
            guiManager.renderUI(renderer.currentScene, window.getWidth(), window.getHeight());
            glfwSwapBuffers(window.getWin());
            glfwPollEvents();
        }
        terminate();
    }

    void terminate() {
        if (grid != null) grid.cleanup();
        guiManager.dispose();
        glfwTerminate();
    }
}
