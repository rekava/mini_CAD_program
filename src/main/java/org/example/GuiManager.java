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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public class GuiManager {

    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    private SelectionManager selection;
    private ToolManager toolManager;
    private Camera camera;

    private Map<String, Integer> iconTextures = new HashMap<>();
    private static final int ICON_SIZE = 32;

    private boolean showAboutPopup = false;
    private Language currentLanguage = Language.ENGLISH;

    public enum Language {
        ENGLISH,
        RUSSIAN
    }

    public GuiManager(SelectionManager selection, Camera camera) {
        this.selection = selection;
        this.camera = camera;
    }

    public void init(long windowPtr) {
        ImGui.createContext();

        ImGuiIO io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.setFontGlobalScale(1.0f);


        // Переменная io уже создана выше в коде: ImGuiIO io = ImGui.getIO();

        try (InputStream is = getClass().getClassLoader().getResourceAsStream("fonts/consolas.ttf")) {
            if (is != null) {
                byte[] bytes = is.readAllBytes();

                // 1. Используем ImFontConfig
                imgui.ImFontConfig fontConfig = new imgui.ImFontConfig();

                // 2. Передаем уже существующую переменную io
                io.getFonts().addFontFromMemoryTTF(bytes, 16.0f, fontConfig, io.getFonts().getGlyphRangesCyrillic());

                io.getFonts().build();
                fontConfig.destroy(); // Обязательно освобождаем память

                System.out.println("Шрифт загружен успешно");
            } else {
                System.out.println("Файл шрифта не найден");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }




        // Современная строгая цветовая схема
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

        // Цвета (серо-чёрная гамма)
        float bgDark = 0.12f;
        float bgMid = 0.18f;
        float bgLight = 0.25f;
        float textLight = 0.95f;
        float textDim = 0.65f;
        float border = 0.30f;

        style.setColor(ImGuiCol.Text,                textLight, textLight, textLight, 1.00f);
        style.setColor(ImGuiCol.TextDisabled,        textDim, textDim, textDim, 1.00f);
        style.setColor(ImGuiCol.WindowBg,            bgDark, bgDark, bgDark, 1.00f);
        style.setColor(ImGuiCol.ChildBg,             bgMid, bgMid, bgMid, 1.00f);
        style.setColor(ImGuiCol.PopupBg,             bgDark, bgDark, bgDark, 1.00f);
        style.setColor(ImGuiCol.Border,               border, border, border, 0.50f);
        style.setColor(ImGuiCol.BorderShadow,         0.00f, 0.00f, 0.00f, 0.00f);
        style.setColor(ImGuiCol.FrameBg,              bgMid, bgMid, bgMid, 1.00f);
        style.setColor(ImGuiCol.FrameBgHovered,       bgLight, bgLight, bgLight, 1.00f);
        style.setColor(ImGuiCol.FrameBgActive,        0.35f, 0.35f, 0.35f, 1.00f);
        style.setColor(ImGuiCol.TitleBg,              0.10f, 0.10f, 0.10f, 1.00f);
        style.setColor(ImGuiCol.TitleBgActive,        bgMid, bgMid, bgMid, 1.00f);
        style.setColor(ImGuiCol.TitleBgCollapsed,     bgDark, bgDark, bgDark, 1.00f);
        style.setColor(ImGuiCol.MenuBarBg,            bgDark, bgDark, bgDark, 1.00f);
        style.setColor(ImGuiCol.ScrollbarBg,          bgDark, bgDark, bgDark, 1.00f);
        style.setColor(ImGuiCol.ScrollbarGrab,        bgMid, bgMid, bgMid, 1.00f);
        style.setColor(ImGuiCol.ScrollbarGrabHovered, bgLight, bgLight, bgLight, 1.00f);
        style.setColor(ImGuiCol.ScrollbarGrabActive,  0.40f, 0.40f, 0.40f, 1.00f);
        style.setColor(ImGuiCol.CheckMark,            0.80f, 0.80f, 0.80f, 1.00f);
        style.setColor(ImGuiCol.SliderGrab,           bgMid, bgMid, bgMid, 1.00f);
        style.setColor(ImGuiCol.SliderGrabActive,     bgLight, bgLight, bgLight, 1.00f);
        style.setColor(ImGuiCol.Button,               bgMid, bgMid, bgMid, 1.00f);
        style.setColor(ImGuiCol.ButtonHovered,        bgLight, bgLight, bgLight, 1.00f);
        style.setColor(ImGuiCol.ButtonActive,         0.35f, 0.35f, 0.35f, 1.00f);
        style.setColor(ImGuiCol.Header,                bgMid, bgMid, bgMid, 1.00f);
        style.setColor(ImGuiCol.HeaderHovered,         bgLight, bgLight, bgLight, 1.00f);
        style.setColor(ImGuiCol.HeaderActive,          0.35f, 0.35f, 0.35f, 1.00f);
        style.setColor(ImGuiCol.Separator,             border, border, border, 1.00f);
        style.setColor(ImGuiCol.SeparatorHovered,      bgLight, bgLight, bgLight, 1.00f);
        style.setColor(ImGuiCol.SeparatorActive,       0.40f, 0.40f, 0.40f, 1.00f);
        style.setColor(ImGuiCol.ResizeGrip,            bgMid, bgMid, bgMid, 1.00f);
        style.setColor(ImGuiCol.ResizeGripHovered,     bgLight, bgLight, bgLight, 1.00f);
        style.setColor(ImGuiCol.ResizeGripActive,      0.40f, 0.40f, 0.40f, 1.00f);
        style.setColor(ImGuiCol.Tab,                   bgMid, bgMid, bgMid, 1.00f);
        style.setColor(ImGuiCol.TabHovered,            bgLight, bgLight, bgLight, 1.00f);
        style.setColor(ImGuiCol.TabActive,             0.35f, 0.35f, 0.35f, 1.00f);
        style.setColor(ImGuiCol.TabUnfocused,          bgDark, bgDark, bgDark, 1.00f);
        style.setColor(ImGuiCol.TabUnfocusedActive,    bgMid, bgMid, bgMid, 1.00f);
        style.setColor(ImGuiCol.DockingPreview,        bgLight, bgLight, bgLight, 0.70f);
        style.setColor(ImGuiCol.DockingEmptyBg,        bgDark, bgDark, bgDark, 1.00f);
        style.setColor(ImGuiCol.PlotLines,             textLight, textLight, textLight, 1.00f);
        style.setColor(ImGuiCol.PlotLinesHovered,      1.00f, 1.00f, 1.00f, 1.00f);
        style.setColor(ImGuiCol.PlotHistogram,         textLight, textLight, textLight, 1.00f);
        style.setColor(ImGuiCol.PlotHistogramHovered,  1.00f, 1.00f, 1.00f, 1.00f);
        style.setColor(ImGuiCol.TableHeaderBg,         bgMid, bgMid, bgMid, 1.00f);
        style.setColor(ImGuiCol.TableBorderStrong,     border, border, border, 1.00f);
        style.setColor(ImGuiCol.TableBorderLight,      bgLight, bgLight, bgLight, 1.00f);
        style.setColor(ImGuiCol.TableRowBg,            bgDark, bgDark, bgDark, 1.00f);
        style.setColor(ImGuiCol.TableRowBgAlt,         bgMid, bgMid, bgMid, 1.00f);
        style.setColor(ImGuiCol.TextSelectedBg,        bgLight, bgLight, bgLight, 0.50f);
        style.setColor(ImGuiCol.DragDropTarget,        0.60f, 0.60f, 0.60f, 1.00f);
        style.setColor(ImGuiCol.NavHighlight,          bgLight, bgLight, bgLight, 1.00f);
        style.setColor(ImGuiCol.NavWindowingHighlight, 0.80f, 0.80f, 0.80f, 1.00f);
        style.setColor(ImGuiCol.NavWindowingDimBg,     0.20f, 0.20f, 0.20f, 0.50f);
        style.setColor(ImGuiCol.ModalWindowDimBg,      0.20f, 0.20f, 0.20f, 0.50f);

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
    }

    private void loadIcon(String name, String path, float r, float g, float b) {
        int texId = loadTexture(path, ICON_SIZE, ICON_SIZE, r, g, b);
        iconTextures.put(name, texId);
    }

    private int loadTexture(String path, int width, int height, float r, float g, float b) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                System.out.println("Icon not found: " + path + " (using fallback)");
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
                System.err.println("Failed to decode image: " + path);
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
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        float menuBarHeight = renderMainMenuBar();

        int currentY = (int) menuBarHeight;

        ImGui.setNextWindowPos(0, currentY);
        ImGui.setNextWindowSize(winWidth, ICON_SIZE * 2 + 20);
        float toolbarActualHeight = renderToolbar();
        currentY += toolbarActualHeight;

        int sidePanelWidth = 250;
        ImGui.setNextWindowPos(0, currentY);
        ImGui.setNextWindowSize(sidePanelWidth, winHeight - currentY);
        renderHierarchy(scene);

        ImGui.setNextWindowPos(winWidth - sidePanelWidth, currentY);
        ImGui.setNextWindowSize(sidePanelWidth, winHeight - currentY);
        renderInspector(scene);

        renderAboutPopup();
        renderSelectionOverlay();
        renderGizmos();

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    private float renderMainMenuBar() {
        float height = 0;
        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu(getLocalizedString("File"))) {
                if (ImGui.menuItem(getLocalizedString("Exit"))) {
                    System.exit(0);
                }
                ImGui.endMenu();
            }
            if (ImGui.beginMenu(getLocalizedString("Settings"))) {
                if (ImGui.beginMenu(getLocalizedString("Language"))) {
                    if (ImGui.menuItem(getLocalizedString("English"), null, currentLanguage == Language.ENGLISH)) {
                        currentLanguage = Language.ENGLISH;
                    }
                    if (ImGui.menuItem(getLocalizedString("Russian"), null, currentLanguage == Language.RUSSIAN)) {
                        currentLanguage = Language.RUSSIAN;
                    }
                    ImGui.endMenu();
                }
                ImGui.endMenu();
            }
            if (ImGui.beginMenu(getLocalizedString("Help"))) {
                if (ImGui.menuItem(getLocalizedString("About"))) {
                    showAboutPopup = true;
                }
                ImGui.endMenu();
            }
            height = ImGui.getWindowHeight();
            ImGui.endMainMenuBar();
        }
        return height;
    }

    private String getLocalizedString(String key) {
        if (currentLanguage == Language.ENGLISH) {
            switch (key) {
                case "File": return "File";
                case "Exit": return "Exit";
                case "Settings": return "Settings";
                case "Language": return "Language";
                case "English": return "English";
                case "Russian": return "Russian";
                case "Help": return "Help";
                case "About": return "About";
                case "Tools": return "Tools";
                case "Scene Hierarchy": return "Scene Hierarchy";
                case "Inspector": return "Inspector";
                case "Add Object": return "Add Object";
                case "Select an object to see properties": return "Select an object to see properties";
                case "Object properties not available": return "Object properties not available";
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
                default: return key;
            }
        } else { // Russian
            switch (key) {
                case "File": return "Файл";
                case "Exit": return "Выход";
                case "Settings": return "Настройки";
                case "Language": return "Язык";
                case "English": return "Английский";
                case "Russian": return "Русский";
                case "Help": return "Справка";
                case "About": return "О программе";
                case "Tools": return "Инструменты";
                case "Scene Hierarchy": return "Иерархия сцены";
                case "Inspector": return "Инспектор";
                case "Add Object": return "Добавить объект";
                case "Select an object to see properties": return "Выберите объект для просмотра свойств";
                case "Object properties not available": return "Свойства объекта недоступны";
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
                default: return key;
            }
        }
    }

    private float renderToolbar() {
        ImGui.begin(getLocalizedString("Tools"), ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize);

        Tool current = toolManager != null ? toolManager.getCurrentTool() : Tool.SELECT;

        drawIconButton("select", current == Tool.SELECT, () -> toolManager.setTool(Tool.SELECT));
        ImGui.sameLine();

        drawIconButton("move", current == Tool.TRANSLATE, () -> toolManager.setTool(Tool.TRANSLATE));
        ImGui.sameLine();

        drawIconButton("rotate", current == Tool.ROTATE, () -> toolManager.setTool(Tool.ROTATE));
        ImGui.sameLine();

        drawIconButton("scale", current == Tool.SCALE, () -> toolManager.setTool(Tool.SCALE));
        ImGui.sameLine();

        ImGui.dummy(10, ICON_SIZE);
        ImGui.sameLine();

        drawIconButton("triangle", current == Tool.CREATE_TRIANGLE, () -> toolManager.setTool(Tool.CREATE_TRIANGLE));
        ImGui.sameLine();

        drawIconButton("rectangle", current == Tool.CREATE_RECTANGLE, () -> toolManager.setTool(Tool.CREATE_RECTANGLE));

        float height = ImGui.getWindowHeight();
        ImGui.end();
        return height;
    }

    private void drawIconButton(String iconName, boolean active, Runnable action) {
        Integer texId = iconTextures.get(iconName);
        if (texId == null) {
            if (ImGui.button(iconName)) action.run();
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

    private void renderHierarchy(Scene scene) {
        ImGui.begin(getLocalizedString("Scene Hierarchy"), ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize);

        if (ImGui.treeNodeEx(getLocalizedString("Root Scene"), ImGuiTreeNodeFlags.DefaultOpen)) {
            for (SceneObject obj : scene.getSceneObjectList()) {
                int flags = ImGuiTreeNodeFlags.Leaf | ImGuiTreeNodeFlags.NoTreePushOnOpen;
                if (selection.isSelected(obj))
                    flags |= ImGuiTreeNodeFlags.Selected;

                if (ImGui.treeNodeEx(obj.getName() + "##" + obj.hashCode(), flags)) {
                    if (ImGui.isItemClicked())
                        selection.selectSingle(obj);
                }
            }
            ImGui.treePop();
        }

        ImGui.separator();
        if (ImGui.button(getLocalizedString("Add Object"), -1, 0)) {
            // логика создания объекта
        }

        ImGui.end();
    }

    private void renderInspector(Scene scene) {
        ImGui.begin(getLocalizedString("Inspector"), ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize);

        List<SceneObject> selected = selection.getSelectedObjects();

        if (selected.isEmpty()) {
            ImGui.textDisabled(getLocalizedString("Select an object to see properties"));
        } else if (selected.size() == 1) {
            SceneObject selectedObject = selected.get(0);
            if (selectedObject instanceof Shape) {
                Shape selectedShape = (Shape) selectedObject;

                if (ImGui.collapsingHeader(getLocalizedString("Transform"), ImGuiTreeNodeFlags.DefaultOpen)) {
                    float[] pos = { selectedShape.transform.position.x, selectedShape.transform.position.y };
                    if (drawVec2Control(getLocalizedString("Position"), pos)) {
                        selectedShape.transform.position.set(pos[0], pos[1]);
                    }

                    float[] scale = { selectedShape.transform.scale.x, selectedShape.transform.scale.y };
                    if (drawVec2Control(getLocalizedString("Size"), scale)) {
                        scale[0] = Math.max(0.1f, scale[0]);
                        scale[1] = Math.max(0.1f, scale[1]);
                        selectedShape.transform.scale.set(scale[0], scale[1]);
                    }

                    float[] rot = { selectedShape.transform.rotation };
                    if (ImGui.dragFloat(getLocalizedString("Rotation"), rot, 0.01f)) {
                        selectedShape.transform.rotation = rot[0];
                    }
                }

                ImGui.spacing();

                if (ImGui.collapsingHeader(getLocalizedString("Color"), ImGuiTreeNodeFlags.DefaultOpen)) {
                    float[] color = { selectedShape.color.x, selectedShape.color.y, selectedShape.color.z };
                    if (ImGui.colorPicker3(getLocalizedString("Color Picker"), color)) {
                        selectedShape.color.set(color[0], color[1], color[2]);
                    }
                    if (ImGui.inputFloat3(getLocalizedString("RGB"), color)) {
                        color[0] = Math.max(0, Math.min(1, color[0]));
                        color[1] = Math.max(0, Math.min(1, color[1]));
                        color[2] = Math.max(0, Math.min(1, color[2]));
                        selectedShape.color.set(color[0], color[1], color[2]);
                    }
                    ImGui.colorButton(getLocalizedString("Preview"), color);
                }

                ImGui.spacing();
                if (ImGui.button(getLocalizedString("Delete Object"), -1, 0)) {
                    scene.remove(selectedObject);
                    selection.clearSelection();
                }
            } else {
                ImGui.textDisabled(getLocalizedString("Object properties not available"));
            }
        } else {
            ImGui.textDisabled(getLocalizedString("Multiple objects selected") + " (" + selected.size() + ")");
        }

        ImGui.end();
    }

    private boolean drawVec2Control(String label, float[] values) {
        boolean changed = false;
        ImGui.pushID(label);
        ImGui.columns(2);
        ImGui.setColumnWidth(0, 80);
        ImGui.text(label);
        ImGui.nextColumn();
        if (ImGui.dragFloat2("##v", values, 0.1f)) changed = true;
        ImGui.columns(1);
        ImGui.popID();
        return changed;
    }

    private void renderAboutPopup() {
        if (showAboutPopup) {
            ImGui.openPopup(getLocalizedString("About") + "##popup");
            showAboutPopup = false;
        }

        ImGui.setNextWindowSize(300, 150);
        if (ImGui.beginPopupModal(getLocalizedString("About") + "##popup", ImGuiWindowFlags.AlwaysAutoResize)) {
            ImGui.text(getLocalizedString("CAD Application"));
            ImGui.separator();
            ImGui.text(getLocalizedString("Version 0.1"));
            ImGui.text(getLocalizedString("Built with LWJGL and ImGui"));
            ImGui.spacing();
            if (ImGui.button(getLocalizedString("OK"), 120, 0)) {
                ImGui.closeCurrentPopup();
            }
            ImGui.endPopup();
        }
    }

    private void renderSelectionOverlay() {
        if (selection == null) return;
        float[] selectRect = selection.getAreaSelectRect();
        if (selectRect != null) {
            drawRect(selectRect, 1.0f, 1.0f, 1.0f, 0.5f);
        }
    }

    private void drawRect(float[] rect, float r, float g, float b, float a) {
        ImGui.begin("##Overlay",
            ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoInputs | ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoBackground | ImGuiWindowFlags.NoDocking);
        ImGui.setWindowPos(0, 0);
        ImGui.setWindowSize(ImGui.getIO().getDisplaySizeX(), ImGui.getIO().getDisplaySizeY());
        ImGui.getWindowDrawList().addRect(
            rect[0], rect[1], rect[0] + rect[2], rect[1] + rect[3],
            ImGui.getColorU32(r, g, b, a), 0, 0, 2.0f);
        ImGui.end();
    }

    private void renderGizmos() {
        if (toolManager == null || selection == null) return;
        List<SceneObject> selected = selection.getSelectedObjects();
        if (selected.size() != 1) return;
        SceneObject obj = selected.get(0);
        if (!(obj instanceof Shape)) return;
        Shape shape = (Shape) obj;
        Tool currentTool = toolManager.getCurrentTool();
        if (currentTool == Tool.TRANSLATE || currentTool == Tool.ROTATE || currentTool == Tool.SCALE) {
            toolManager.getGizmo().draw(shape, currentTool);
        }
    }

    public void dispose() {
        for (int texId : iconTextures.values()) {
            glDeleteTextures(texId);
        }
        iconTextures.clear();
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
    }

    public void setSelectionManager(SelectionManager selection) { this.selection = selection; }
    public void setToolManager(ToolManager toolManager) { this.toolManager = toolManager; }
    public ImGuiImplGlfw getImGuiGlfw() { return imGuiGlfw; }
}
