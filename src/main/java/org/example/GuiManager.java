package org.example;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public class GuiManager {

    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    private SelectionManager selectionManager;
    private ToolManager toolManager;
    private Camera camera;
    private Scene scene;
    private CommandManager commandManager;
    private Grid grid;

    private Map<String, Integer> iconTextures = new HashMap<>();
    private static final int ICON_SIZE = 32;

    private boolean showAboutPopup = false;
    private Language currentLanguage = Language.ENGLISH;

    // Для цвета
    private boolean isDraggingColor = false;
    private List<Shape> dragShapes;
    private List<Vector3f> dragStartColors;

    public enum Language {
        ENGLISH,
        RUSSIAN
    }

    public GuiManager(SelectionManager selectionManager, Camera camera) {
        this.selectionManager = selectionManager;
        this.camera = camera;
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    public void init(long windowPtr, Scene scene, CommandManager commandManager) {
        this.scene = scene;
        this.commandManager = commandManager;
        this.selectionManager.setCommandManager(commandManager);

        ImGui.createContext();

        ImGuiIO io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.setFontGlobalScale(1.0f);

        try (InputStream is = getClass().getClassLoader().getResourceAsStream("fonts/consolas.ttf")) {
            if (is != null) {
                byte[] bytes = is.readAllBytes();
                imgui.ImFontConfig fontConfig = new imgui.ImFontConfig();
                io.getFonts().addFontFromMemoryTTF(bytes, 16.0f, fontConfig, io.getFonts().getGlyphRangesCyrillic());
                io.getFonts().build();
                fontConfig.destroy();
                System.out.println("Font loaded successfully");
            } else {
                System.out.println("Font file not found");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ImGuiStyle style = ImGui.getStyle();
        style.setWindowRounding(4.0f);
        style.setFrameRounding(3.0f);
        style.setGrabRounding(3.0f);
        style.setChildRounding(3.0f);
        style.setPopupRounding(3.0f);
        style.setScrollbarRounding(3.0f);
        style.setWindowBorderSize(1.0f);
        style.setFrameBorderSize(0.0f);
        style.setPopupBorderSize(1.0f);
        style.setWindowPadding(8.0f, 8.0f);
        style.setFramePadding(5.0f, 4.0f);
        style.setItemSpacing(6.0f, 4.0f);

        float bgDark = 0.12f;
        float bgMid = 0.18f;
        float bgLight = 0.25f;
        float textLight = 0.95f;
        float textDim = 0.65f;
        float border = 0.30f;

        style.setColor(ImGuiCol.Text, textLight, textLight, textLight, 1.00f);
        style.setColor(ImGuiCol.TextDisabled, textDim, textDim, textDim, 1.00f);
        style.setColor(ImGuiCol.WindowBg, bgDark, bgDark, bgDark, 1.00f);
        style.setColor(ImGuiCol.ChildBg, bgMid, bgMid, bgMid, 1.00f);
        style.setColor(ImGuiCol.PopupBg, bgDark, bgDark, bgDark, 1.00f);
        style.setColor(ImGuiCol.Border, border, border, border, 0.50f);
        style.setColor(ImGuiCol.BorderShadow, 0.00f, 0.00f, 0.00f, 0.00f);
        style.setColor(ImGuiCol.FrameBg, bgMid, bgMid, bgMid, 1.00f);
        style.setColor(ImGuiCol.FrameBgHovered, bgLight, bgLight, bgLight, 1.00f);
        style.setColor(ImGuiCol.FrameBgActive, 0.35f, 0.35f, 0.35f, 1.00f);
        style.setColor(ImGuiCol.TitleBg, 0.10f, 0.10f, 0.10f, 1.00f);
        style.setColor(ImGuiCol.TitleBgActive, bgMid, bgMid, bgMid, 1.00f);
        style.setColor(ImGuiCol.TitleBgCollapsed, bgDark, bgDark, bgDark, 1.00f);
        style.setColor(ImGuiCol.MenuBarBg, bgDark, bgDark, bgDark, 1.00f);
        style.setColor(ImGuiCol.ScrollbarBg, bgDark, bgDark, bgDark, 1.00f);
        style.setColor(ImGuiCol.ScrollbarGrab, bgMid, bgMid, bgMid, 1.00f);
        style.setColor(ImGuiCol.ScrollbarGrabHovered, bgLight, bgLight, bgLight, 1.00f);
        style.setColor(ImGuiCol.ScrollbarGrabActive, 0.40f, 0.40f, 0.40f, 1.00f);
        style.setColor(ImGuiCol.CheckMark, 0.80f, 0.80f, 0.80f, 1.00f);
        style.setColor(ImGuiCol.SliderGrab, bgMid, bgMid, bgMid, 1.00f);
        style.setColor(ImGuiCol.SliderGrabActive, bgLight, bgLight, bgLight, 1.00f);
        style.setColor(ImGuiCol.Button, bgMid, bgMid, bgMid, 1.00f);
        style.setColor(ImGuiCol.ButtonHovered, bgLight, bgLight, bgLight, 1.00f);
        style.setColor(ImGuiCol.ButtonActive, 0.35f, 0.35f, 0.35f, 1.00f);
        style.setColor(ImGuiCol.Header, bgMid, bgMid, bgMid, 1.00f);
        style.setColor(ImGuiCol.HeaderHovered, bgLight, bgLight, bgLight, 1.00f);
        style.setColor(ImGuiCol.HeaderActive, 0.35f, 0.35f, 0.35f, 1.00f);
        style.setColor(ImGuiCol.Separator, border, border, border, 1.00f);
        style.setColor(ImGuiCol.SeparatorHovered, bgLight, bgLight, bgLight, 1.00f);
        style.setColor(ImGuiCol.SeparatorActive, 0.40f, 0.40f, 0.40f, 1.00f);
        style.setColor(ImGuiCol.ResizeGrip, bgMid, bgMid, bgMid, 1.00f);
        style.setColor(ImGuiCol.ResizeGripHovered, bgLight, bgLight, bgLight, 1.00f);
        style.setColor(ImGuiCol.ResizeGripActive, 0.40f, 0.40f, 0.40f, 1.00f);
        style.setColor(ImGuiCol.Tab, bgMid, bgMid, bgMid, 1.00f);
        style.setColor(ImGuiCol.TabHovered, bgLight, bgLight, bgLight, 1.00f);
        style.setColor(ImGuiCol.TabActive, 0.35f, 0.35f, 0.35f, 1.00f);
        style.setColor(ImGuiCol.TabUnfocused, bgDark, bgDark, bgDark, 1.00f);
        style.setColor(ImGuiCol.TabUnfocusedActive, bgMid, bgMid, bgMid, 1.00f);
        style.setColor(ImGuiCol.DockingPreview, bgLight, bgLight, bgLight, 0.70f);
        style.setColor(ImGuiCol.DockingEmptyBg, bgDark, bgDark, bgDark, 1.00f);
        style.setColor(ImGuiCol.PlotLines, textLight, textLight, textLight, 1.00f);
        style.setColor(ImGuiCol.PlotLinesHovered, 1.00f, 1.00f, 1.00f, 1.00f);
        style.setColor(ImGuiCol.PlotHistogram, textLight, textLight, textLight, 1.00f);
        style.setColor(ImGuiCol.PlotHistogramHovered, 1.00f, 1.00f, 1.00f, 1.00f);
        style.setColor(ImGuiCol.TableHeaderBg, bgMid, bgMid, bgMid, 1.00f);
        style.setColor(ImGuiCol.TableBorderStrong, border, border, border, 1.00f);
        style.setColor(ImGuiCol.TableBorderLight, bgLight, bgLight, bgLight, 1.00f);
        style.setColor(ImGuiCol.TableRowBg, bgDark, bgDark, bgDark, 1.00f);
        style.setColor(ImGuiCol.TableRowBgAlt, bgMid, bgMid, bgMid, 1.00f);
        style.setColor(ImGuiCol.TextSelectedBg, bgLight, bgLight, bgLight, 0.50f);
        style.setColor(ImGuiCol.DragDropTarget, 0.60f, 0.60f, 0.60f, 1.00f);
        style.setColor(ImGuiCol.NavHighlight, bgLight, bgLight, bgLight, 1.00f);
        style.setColor(ImGuiCol.NavWindowingHighlight, 0.80f, 0.80f, 0.80f, 1.00f);
        style.setColor(ImGuiCol.NavWindowingDimBg, 0.20f, 0.20f, 0.20f, 0.50f);
        style.setColor(ImGuiCol.ModalWindowDimBg, 0.20f, 0.20f, 0.20f, 0.50f);

        imGuiGlfw.init(windowPtr, true);
        imGuiGl3.init("#version 330");

        loadIcons();
    }

    private void loadIcons() {
        loadIcon("select", "icons/select.jpg", 0.5f, 0.5f, 0.5f);
        loadIcon("move", "icons/move.jpg", 0.5f, 0.5f, 0.5f);
        loadIcon("rotate", "icons/rotate.jpg", 0.5f, 0.5f, 0.5f);
        loadIcon("scale", "icons/scale.jpg", 0.5f, 0.5f, 0.5f);
        loadIcon("triangle", "icons/triangle.jpg", 0.5f, 0.5f, 0.5f);
        loadIcon("rectangle", "icons/rectangle.jpg", 0.5f, 0.5f, 0.5f);
        loadIcon("undo", "icons/undo.jpg", 0.5f, 0.5f, 0.5f);
        loadIcon("redo", "icons/redo.jpg", 0.5f, 0.5f, 0.5f);
    }

    private void loadIcon(String name, String path, float r, float g, float b) {
        int texId = loadTexture(path, ICON_SIZE, ICON_SIZE, r, g, b);
        iconTextures.put(name, texId);
    }

    private int loadTexture(String path, int width, int height, float r, float g, float b) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                return createFallbackTexture(width, height, r, g, b);
            }
            byte[] bytes = is.readAllBytes();
            ByteBuffer data = BufferUtils.createByteBuffer(bytes.length);
            data.put(bytes).flip();

            IntBuffer wBuf = BufferUtils.createIntBuffer(1);
            IntBuffer hBuf = BufferUtils.createIntBuffer(1);
            IntBuffer compBuf = BufferUtils.createIntBuffer(1);

            ByteBuffer imageBuffer = STBImage.stbi_load_from_memory(data, wBuf, hBuf, compBuf, 4);
            if (imageBuffer == null) {
                return createFallbackTexture(width, height, r, g, b);
            }
            int w = wBuf.get(0);
            int h = hBuf.get(0);

            int texId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, texId);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w, h, 0, GL_RGBA, GL_UNSIGNED_BYTE, imageBuffer);
            glGenerateMipmap(GL_TEXTURE_2D);

            STBImage.stbi_image_free(imageBuffer);
            return texId;
        } catch (IOException e) {
            e.printStackTrace();
            return createFallbackTexture(width, height, r, g, b);
        }
    }

    private int createFallbackTexture(int width, int height, float r, float g, float b) {
        ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * 4);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels.put((byte) (r * 255));
                pixels.put((byte) (g * 255));
                pixels.put((byte) (b * 255));
                pixels.put((byte) 255);
            }
        }
        pixels.flip();

        int texId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texId);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
        return texId;
    }

    public void renderUI(Scene scene, int winWidth, int winHeight) {
        this.scene = scene;

        imGuiGlfw.newFrame();
        ImGui.newFrame();

        float menuBarHeight = renderMainMenuBar();
        int currentY = (int) menuBarHeight;

        ImGui.setNextWindowPos(0, currentY);
        ImGui.setNextWindowSize(winWidth, ICON_SIZE * 2 + 20);
        float toolbarActualHeight = renderToolbar(winWidth);
        currentY += toolbarActualHeight;

        int sidePanelWidth = 250;
        ImGui.setNextWindowPos(0, currentY);
        ImGui.setNextWindowSize(sidePanelWidth, winHeight - currentY);
        renderHierarchy();

        ImGui.setNextWindowPos(winWidth - sidePanelWidth, currentY);
        ImGui.setNextWindowSize(sidePanelWidth, winHeight - currentY);
        renderInspector();

        renderAboutPopup();
        renderSelectionOverlay();
        renderGizmos();

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    private float renderMainMenuBar() {
        float height = 0;
        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu(getString("File"))) {
                if (ImGui.menuItem(getString("Exit"))) {
                    System.exit(0);
                }
                ImGui.endMenu();
            }

            if (ImGui.beginMenu(getString("Edit"))) {
                if (ImGui.menuItem(getString("Undo"), "Ctrl+Z")) {
                    if (commandManager != null) commandManager.undo();
                }
                if (ImGui.menuItem(getString("Redo"), "Ctrl+Y")) {
                    if (commandManager != null) commandManager.redo();
                }
                ImGui.separator();
                if (ImGui.menuItem(getString("Copy"), "Ctrl+C")) {
                    if (selectionManager != null) selectionManager.copySelected();
                }
                if (ImGui.menuItem(getString("Paste"), "Ctrl+V")) {
                    if (selectionManager != null) selectionManager.paste();
                }
                ImGui.separator();
                if (ImGui.menuItem(getString("Delete Selected"), "Del")) {
                    deleteSelected();
                }
                if (ImGui.menuItem(getString("Select All"), "Ctrl+A")) {
                    if (selectionManager != null) selectionManager.selectAll();
                }
                ImGui.endMenu();
            }

            if (ImGui.beginMenu(getString("Settings"))) {
                if (ImGui.beginMenu(getString("Language"))) {
                    if (ImGui.menuItem(getString("English"), null, currentLanguage == Language.ENGLISH)) {
                        currentLanguage = Language.ENGLISH;
                    }
                    if (ImGui.menuItem(getString("Russian"), null, currentLanguage == Language.RUSSIAN)) {
                        currentLanguage = Language.RUSSIAN;
                    }
                    ImGui.endMenu();
                }

                ImGui.separator();

                if (grid != null && ImGui.beginMenu(getString("Grid Settings"))) {
                    boolean enabled = grid.isEnabled();
                    if (ImGui.menuItem(getString("Show Grid"), "G", enabled)) {
                        grid.setEnabled(!enabled);
                    }

                    ImGui.separator();

                    ImGui.text(getString("Grid Size"));
                    float[] size = { grid.getSize() };
                    ImGui.pushItemWidth(120);
                    if (ImGui.dragFloat("##gridSize", size, 0.1f, 1.0f, 20.0f)) {
                        grid.setSize(size[0]);
                    }

                    ImGui.text(getString("Grid Step"));
                    float[] step = { grid.getStep() };
                    if (ImGui.dragFloat("##gridStep", step, 0.05f, 0.25f, 5.0f)) {
                        grid.setStep(step[0]);
                    }

                    ImGui.text(getString("Grid Color"));
                    float[] color = grid.getColor();
                    if (ImGui.colorEdit3("##gridColor", color)) {
                        grid.setColor(color[0], color[1], color[2]);
                    }
                    ImGui.popItemWidth();

                    ImGui.endMenu();
                }

                ImGui.endMenu();
            }

            if (ImGui.beginMenu(getString("Help"))) {
                if (ImGui.menuItem(getString("About"))) {
                    showAboutPopup = true;
                }
                ImGui.endMenu();
            }

            height = ImGui.getWindowHeight();
            ImGui.endMainMenuBar();
        }
        return height;
    }

    private void deleteSelected() {
        if (selectionManager == null) return;
        List<Shape> selectedShapes = selectionManager.getSelectedShapes();
        if (commandManager != null && !selectedShapes.isEmpty()) {
            commandManager.execute(new DeleteCommand(scene, selectedShapes));
        }
    }

    private String getString(String key) {
        if (currentLanguage == Language.ENGLISH) {
            switch (key) {
                case "File": return "File";
                case "Exit": return "Exit";
                case "Edit": return "Edit";
                case "Undo": return "Undo";
                case "Redo": return "Redo";
                case "Copy": return "Copy";
                case "Paste": return "Paste";
                case "Delete Selected": return "Delete Selected";
                case "Select All": return "Select All";
                case "Settings": return "Settings";
                case "Language": return "Language";
                case "English": return "English";
                case "Russian": return "Russian";
                case "Help": return "Help";
                case "About": return "About";
                case "Grid Settings": return "Grid Settings";
                case "Show Grid": return "Show Grid";
                case "Grid Size": return "Grid Size";
                case "Grid Step": return "Grid Step";
                case "Grid Color": return "Grid Color";
                case "Tools": return "Tools";
                case "Scene Hierarchy": return "Scene Hierarchy";
                case "Inspector": return "Inspector";
                case "Add Object": return "Add Object";
                case "Select an object to see properties": return "Select an object to see properties";
                case "Multiple objects selected": return "Multiple objects selected";
                case "Transform": return "Transform";
                case "Position": return "Position";
                case "Size": return "Size";
                case "Rotation": return "Rotation";
                case "Color": return "Color";
                case "Color Picker": return "Color Picker";
                case "RGB": return "RGB";
                case "Delete Object": return "Delete Object";
                case "CAD Application": return "CAD Application";
                case "Version 0.1": return "Version 0.1";
                case "Built with LWJGL and ImGui": return "Built with LWJGL and ImGui";
                case "OK": return "OK";
                case "X": return "X";
                case "Y": return "Y";
                default: return key;
            }
        } else {
            switch (key) {
                case "File": return "Файл";
                case "Exit": return "Выход";
                case "Edit": return "Правка";
                case "Undo": return "Отменить";
                case "Redo": return "Повторить";
                case "Copy": return "Копировать";
                case "Paste": return "Вставить";
                case "Delete Selected": return "Удалить выбранное";
                case "Select All": return "Выбрать всё";
                case "Settings": return "Настройки";
                case "Language": return "Язык";
                case "English": return "Английский";
                case "Russian": return "Русский";
                case "Help": return "Справка";
                case "About": return "О программе";
                case "Grid Settings": return "Настройки сетки";
                case "Show Grid": return "Показать сетку";
                case "Grid Size": return "Размер сетки";
                case "Grid Step": return "Шаг сетки";
                case "Grid Color": return "Цвет сетки";
                case "Tools": return "Инструменты";
                case "Scene Hierarchy": return "Иерархия сцены";
                case "Inspector": return "Инспектор";
                case "Add Object": return "Добавить объект";
                case "Select an object to see properties": return "Выберите объект для просмотра свойств";
                case "Multiple objects selected": return "Выбрано несколько объектов";
                case "Transform": return "Трансформация";
                case "Position": return "Позиция";
                case "Size": return "Размер";
                case "Rotation": return "Поворот";
                case "Color": return "Цвет";
                case "Color Picker": return "Выбор цвета";
                case "RGB": return "RGB";
                case "Delete Object": return "Удалить объект";
                case "CAD Application": return "САПР Приложение";
                case "Version 0.1": return "Версия 0.1";
                case "Built with LWJGL and ImGui": return "Сделано на LWJGL и ImGui";
                case "OK": return "ОК";
                case "X": return "X";
                case "Y": return "Y";
                default: return key;
            }
        }
    }

    private float renderToolbar(int winWidth) {
        ImGui.begin(getString("Tools"), ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize);

        Tool current = toolManager != null ? toolManager.getCurrentTool() : Tool.SELECT;

        drawIcon("select", current == Tool.SELECT, () -> toolManager.setTool(Tool.SELECT));
        ImGui.sameLine();
        drawIcon("move", current == Tool.TRANSLATE, () -> toolManager.setTool(Tool.TRANSLATE));
        ImGui.sameLine();
        drawIcon("rotate", current == Tool.ROTATE, () -> toolManager.setTool(Tool.ROTATE));
        ImGui.sameLine();
        drawIcon("scale", current == Tool.SCALE, () -> toolManager.setTool(Tool.SCALE));
        ImGui.sameLine();

        ImGui.dummy(10, ICON_SIZE);
        ImGui.sameLine();

        drawIcon("triangle", current == Tool.CREATE_TRIANGLE, () -> toolManager.setTool(Tool.CREATE_TRIANGLE));
        ImGui.sameLine();
        drawIcon("rectangle", current == Tool.CREATE_RECTANGLE, () -> toolManager.setTool(Tool.CREATE_RECTANGLE));

        float windowWidth = ImGui.getWindowWidth();
        float buttonWidth = ICON_SIZE * 2 + 20;
        float rightMargin = 10;
        ImGui.sameLine(windowWidth - buttonWidth - rightMargin);

        drawIcon("undo", false, () -> { if (commandManager != null) commandManager.undo(); });
        ImGui.sameLine();
        drawIcon("redo", false, () -> { if (commandManager != null) commandManager.redo(); });

        float height = ImGui.getWindowHeight();
        ImGui.end();
        return height;
    }

    private void drawIcon(String name, boolean active, Runnable action) {
        Integer texId = iconTextures.get(name);
        if (texId == null) {
            if (ImGui.button(name)) action.run();
            return;
        }
        if (active) {
            ImGui.pushStyleColor(ImGuiCol.Button, 0.35f, 0.35f, 0.35f, 1.0f);
        }
        if (ImGui.imageButton(texId, ICON_SIZE, ICON_SIZE, 0, 0, 1, 1)) {
            action.run();
        }
        if (active) {
            ImGui.popStyleColor();
        }
    }

    private void renderHierarchy() {
        ImGui.begin(getString("Scene Hierarchy"), ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize);

        if (ImGui.treeNodeEx(getString("Root Scene"), ImGuiTreeNodeFlags.DefaultOpen)) {
            for (SceneObject obj : scene.getSceneObjectList()) {
                int flags = ImGuiTreeNodeFlags.Leaf | ImGuiTreeNodeFlags.NoTreePushOnOpen;
                if (selectionManager.isSelected(obj)) {
                    flags |= ImGuiTreeNodeFlags.Selected;
                }
                if (ImGui.treeNodeEx(obj.getName() + "##" + obj.hashCode(), flags)) {
                    if (ImGui.isItemClicked()) {
                        if (ImGui.getIO().getKeyCtrl()) {
                            selectionManager.toggleSelection(obj);
                        } else {
                            selectionManager.selectSingle(obj);
                        }
                    }
                }
            }
            ImGui.treePop();
        }

        ImGui.separator();

        if (ImGui.button(getString("Add Object"), -1, 0)) {
            ImGui.openPopup("add_object_popup");
        }

        if (ImGui.beginPopup("add_object_popup")) {
            if (ImGui.menuItem("Triangle")) {
                addTriangle();
            }
            if (ImGui.menuItem("Rectangle")) {
                addRectangle();
            }
            ImGui.endPopup();
        }

        ImGui.end();
    }

    private void addTriangle() {
        if (toolManager != null && commandManager != null) {
            Shape triangle = new Triangle(toolManager.getShader());
            triangle.transform.position.set(0, 0);
            triangle.transform.scale.set(1, 1);
            commandManager.execute(new CreateShapeCommand(scene, triangle));
            selectionManager.selectSingle(triangle);
        }
    }

    private void addRectangle() {
        if (toolManager != null && commandManager != null) {
            Shape rectangle = new Rectangle(toolManager.getShader());
            rectangle.transform.position.set(0, 0);
            rectangle.transform.scale.set(1, 1);
            commandManager.execute(new CreateShapeCommand(scene, rectangle));
            selectionManager.selectSingle(rectangle);
        }
    }

    private void renderInspector() {
        ImGui.begin(getString("Inspector"), ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize);

        List<Shape> selectedShapes = selectionManager.getSelectedShapes();

        if (selectedShapes.isEmpty()) {
            ImGui.textDisabled(getString("Select an object to see properties"));
        } else {
            if (selectedShapes.size() > 1) {
                ImGui.textColored(0.8f, 0.8f, 0.2f, 1.0f,
                    getString("Multiple objects selected") + ": " + selectedShapes.size());
                ImGui.separator();
            }

            if (ImGui.collapsingHeader(getString("Transform"), ImGuiTreeNodeFlags.DefaultOpen)) {
                renderTransformReadOnly(selectedShapes);
            }

            ImGui.spacing();

            if (ImGui.collapsingHeader(getString("Color"), ImGuiTreeNodeFlags.DefaultOpen)) {
                renderColor(selectedShapes);
            }

            ImGui.spacing();

            if (ImGui.button(getString("Delete Object"), -1, 0)) {
                if (commandManager != null) {
                    commandManager.execute(new DeleteCommand(scene, selectedShapes));
                }
            }
        }

        ImGui.end();
    }

    private void renderTransformReadOnly(List<Shape> shapes) {
        float avgX = 0, avgY = 0;
        float avgScaleX = 0, avgScaleY = 0;
        float avgRot = 0;

        for (Shape shape : shapes) {
            avgX += shape.transform.position.x;
            avgY += shape.transform.position.y;
            avgScaleX += shape.transform.scale.x;
            avgScaleY += shape.transform.scale.y;
            avgRot += shape.transform.rotation;
        }
        avgX /= shapes.size();
        avgY /= shapes.size();
        avgScaleX /= shapes.size();
        avgScaleY /= shapes.size();
        avgRot /= shapes.size();

        float[] pos = { avgX, avgY };
        float[] scale = { avgScaleX, avgScaleY };
        float[] rot = { avgRot };

        float w = (ImGui.getContentRegionAvailX() - 10) / 2;

        ImGui.pushID("pos");
        ImGui.text(getString("Position"));
        ImGui.sameLine();
        ImGui.pushItemWidth(w);
        ImGui.text("X");
        ImGui.sameLine();
        float[] xv = { pos[0] };
        ImGui.dragFloat("##x", xv, 0.01f);
        ImGui.sameLine();
        ImGui.text("Y");
        ImGui.sameLine();
        float[] yv = { pos[1] };
        ImGui.dragFloat("##y", yv, 0.01f);
        ImGui.popItemWidth();
        ImGui.popID();

        ImGui.pushID("scale");
        ImGui.text(getString("Size"));
        ImGui.sameLine();
        ImGui.pushItemWidth(w);
        ImGui.text("X");
        ImGui.sameLine();
        float[] sx = { scale[0] };
        ImGui.dragFloat("##sx", sx, 0.01f);
        ImGui.sameLine();
        ImGui.text("Y");
        ImGui.sameLine();
        float[] sy = { scale[1] };
        ImGui.dragFloat("##sy", sy, 0.01f);
        ImGui.popItemWidth();
        ImGui.popID();

        ImGui.pushID("rot");
        ImGui.text(getString("Rotation"));
        ImGui.sameLine();
        ImGui.pushItemWidth(w);
        float[] r = { rot[0] };
        ImGui.dragFloat("##rot", r, 0.01f);
        ImGui.popItemWidth();
        ImGui.popID();

        ImGui.textDisabled("(Use toolbar gizmos to edit)");
    }

    private void renderColor(List<Shape> shapes) {
        float[] editColor = { shapes.get(0).color.x, shapes.get(0).color.y, shapes.get(0).color.z };

        if (ImGui.isItemActivated()) {
            isDraggingColor = true;
            dragShapes = new ArrayList<>(shapes);
            dragStartColors = new ArrayList<>();
            for (Shape s : shapes) {
                dragStartColors.add(new Vector3f(s.color));
            }
        }

        ImGui.text("Color");
        ImGui.sameLine();
        for (int i = 0; i < Math.min(5, shapes.size()); i++) {
            ImGui.sameLine();
            ImGui.colorButton("##p" + i, new float[]{shapes.get(i).color.x, shapes.get(i).color.y, shapes.get(i).color.z, 1.0f}, ImGuiColorEditFlags.NoTooltip | ImGuiColorEditFlags.NoPicker);
        }
        ImGui.spacing();

        if (ImGui.colorPicker3(getString("Color Picker"), editColor)) {
            for (Shape s : shapes) {
                s.color.set(editColor[0], editColor[1], editColor[2]);
            }
        }

        if (ImGui.inputFloat3(getString("RGB"), editColor)) {
            editColor[0] = Math.max(0, Math.min(1, editColor[0]));
            editColor[1] = Math.max(0, Math.min(1, editColor[1]));
            editColor[2] = Math.max(0, Math.min(1, editColor[2]));
            for (Shape s : shapes) {
                s.color.set(editColor[0], editColor[1], editColor[2]);
            }
        }

        if (isDraggingColor && !ImGui.isItemActive()) {
            isDraggingColor = false;

            if (commandManager != null && dragShapes != null && dragStartColors != null) {
                boolean hasChanges = false;
                for (int i = 0; i < dragShapes.size(); i++) {
                    if (!dragStartColors.get(i).equals(dragShapes.get(i).color)) {
                        hasChanges = true;
                        break;
                    }
                }
                if (hasChanges) {
                    List<Vector3f> finalColors = new ArrayList<>();
                    for (Shape s : dragShapes) {
                        finalColors.add(new Vector3f(s.color));
                    }
                    commandManager.execute(new MultiColorCommand(dragShapes, dragStartColors, finalColors));
                }
            }

            dragShapes = null;
            dragStartColors = null;
        }

        ImGui.text("Selected: " + shapes.size());
    }

    private void renderAboutPopup() {
        if (showAboutPopup) {
            ImGui.openPopup(getString("About") + "##popup");
            showAboutPopup = false;
        }
        ImGui.setNextWindowSize(300, 150);
        if (ImGui.beginPopupModal(getString("About") + "##popup", ImGuiWindowFlags.AlwaysAutoResize)) {
            ImGui.text(getString("CAD Application"));
            ImGui.separator();
            ImGui.text(getString("Version 0.1"));
            ImGui.text(getString("Built with LWJGL and ImGui"));
            ImGui.spacing();
            if (ImGui.button(getString("OK"), 120, 0)) {
                ImGui.closeCurrentPopup();
            }
            ImGui.endPopup();
        }
    }

    private void renderSelectionOverlay() {
        if (selectionManager == null) return;
        float[] rect = selectionManager.getAreaSelectRect();
        if (rect != null) {
            ImGui.begin("##Overlay", ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoInputs |
                ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoBackground | ImGuiWindowFlags.NoDocking);
            ImGui.setWindowPos(0, 0);
            ImGui.setWindowSize(ImGui.getIO().getDisplaySizeX(), ImGui.getIO().getDisplaySizeY());
            ImGui.getWindowDrawList().addRect(rect[0], rect[1], rect[0] + rect[2], rect[1] + rect[3],
                ImGui.getColorU32(1, 1, 1, 0.5f), 0, 0, 2.0f);
            ImGui.end();
        }
    }

    private void renderGizmos() {
        if (toolManager == null || selectionManager == null) return;
        List<Shape> shapes = selectionManager.getSelectedShapes();
        if (shapes.isEmpty()) return;
        Tool tool = toolManager.getCurrentTool();
        if (tool == Tool.TRANSLATE || tool == Tool.ROTATE || tool == Tool.SCALE) {
            toolManager.getGizmo().draw(shapes, tool);
        }
    }

    public void dispose() {
        for (int id : iconTextures.values()) {
            glDeleteTextures(id);
        }
        iconTextures.clear();
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
    }

    public void setSelectionManager(SelectionManager sm) { this.selectionManager = sm; }
    public void setToolManager(ToolManager tm) { this.toolManager = tm; }
    public ImGuiImplGlfw getImGuiGlfw() { return imGuiGlfw; }
}
