package org.example;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.joml.Vector3f;
import java.util.List;

public class GuiManager {

    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    private SelectionManager selection;
    private ToolManager toolManager;

    public GuiManager(SelectionManager selection) {
        this.selection = selection;
    }

    public void init(long windowPtr) {
        ImGui.createContext();

        ImGuiIO io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);

        ImGuiStyle style = ImGui.getStyle();
        style.setWindowRounding(0.0f);
        style.setFrameRounding(3.0f);
        style.getColors()[ImGuiCol.WindowBg] = new float[]{0.1f, 0.1f, 0.1f, 1.0f};
        style.getColors()[ImGuiCol.Header] = new float[]{0.2f, 0.2f, 0.2f, 1.0f};

        imGuiGlfw.init(windowPtr, true);
        imGuiGl3.init("#version 330");
    }

    public void renderUI(Scene scene, int winWidth, int winHeight) {

        imGuiGlfw.newFrame();
        ImGui.newFrame();

        // 1. Сначала создаём dockspace (он занимает всё окно)
        int dockFlags = ImGuiDockNodeFlags.PassthruCentralNode;
        ImGui.dockSpaceOverViewport(ImGui.getMainViewport(), dockFlags);

        // 2. Потом рисуем панель инструментов (она будет поверх, но НЕ внутри dockspace)
        renderToolbar();

        // 3. Рисуем остальные окна (они будут прикреплены к dockspace)
        renderHierarchy(scene);
        renderInspector(scene);

        // 4. Оверлей поверх всего
        renderSelectionOverlay();

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    private void renderToolbar() {
        ImGui.begin("Tools",
                ImGuiWindowFlags.NoTitleBar |
                        ImGuiWindowFlags.AlwaysAutoResize |
                        ImGuiWindowFlags.NoMove |
                        ImGuiWindowFlags.NoDocking); // важно: не участвует в докинге

        ImGui.setWindowPos(10, 10);

        Tool current = toolManager != null ? toolManager.getCurrentTool() : Tool.SELECT;

        if (current == Tool.SELECT) {
            ImGui.pushStyleColor(ImGuiCol.Button, 0.3f, 0.5f, 0.8f, 1.0f);
        }
        if (ImGui.button("Select")) {
            if (toolManager != null) toolManager.setTool(Tool.SELECT);
        }
        if (current == Tool.SELECT) {
            ImGui.popStyleColor();
        }

        ImGui.sameLine();

        if (current == Tool.CREATE_TRIANGLE) {
            ImGui.pushStyleColor(ImGuiCol.Button, 0.3f, 0.5f, 0.8f, 1.0f);
        }
        if (ImGui.button("Triangle")) {
            if (toolManager != null) toolManager.setTool(Tool.CREATE_TRIANGLE);
        }
        if (current == Tool.CREATE_TRIANGLE) {
            ImGui.popStyleColor();
        }

        ImGui.sameLine();

        if (current == Tool.CREATE_RECTANGLE) {
            ImGui.pushStyleColor(ImGuiCol.Button, 0.3f, 0.5f, 0.8f, 1.0f);
        }
        if (ImGui.button("Rectangle")) {
            if (toolManager != null) toolManager.setTool(Tool.CREATE_RECTANGLE);
        }
        if (current == Tool.CREATE_RECTANGLE) {
            ImGui.popStyleColor();
        }

        ImGui.end();
    }

    private void renderHierarchy(Scene scene) {

        ImGui.begin("Scene Hierarchy");

        if (ImGui.treeNodeEx("Root Scene", ImGuiTreeNodeFlags.DefaultOpen)) {

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

        if (ImGui.button("Add Object", -1, 0)) {
            // логика создания объекта
        }

        ImGui.end();
    }

    private void renderInspector(Scene scene) {

        ImGui.begin("Inspector");

        List<SceneObject> selected = selection.getSelectedObjects();

        if (selected.isEmpty()) {
            ImGui.textDisabled("Select an object to see properties");
        } else if (selected.size() == 1) {
            SceneObject selectedObject = selected.get(0);
            if (selectedObject instanceof Shape) {
                Shape selectedShape = (Shape) selectedObject;

                if (ImGui.collapsingHeader("Transform", ImGuiTreeNodeFlags.DefaultOpen)) {
                    float[] pos = {
                            selectedShape.transform.position.x,
                            selectedShape.transform.position.y
                    };
                    if (drawVec2Control("Position", pos)) {
                        selectedShape.transform.position.set(pos[0], pos[1]);
                    }

                    // Добавляем управление размером (scale)
                    float[] scale = {
                            selectedShape.transform.scale.x,
                            selectedShape.transform.scale.y
                    };
                    if (drawVec2Control("Size", scale)) {
                        // Не даём размеру стать нулевым или отрицательным
                        scale[0] = Math.max(0.1f, scale[0]);
                        scale[1] = Math.max(0.1f, scale[1]);
                        selectedShape.transform.scale.set(scale[0], scale[1]);
                    }
                }

                ImGui.spacing();

                if (ImGui.collapsingHeader("Color", ImGuiTreeNodeFlags.DefaultOpen)) {
                    float[] color = {
                            selectedShape.color.x,
                            selectedShape.color.y,
                            selectedShape.color.z
                    };

                    if (ImGui.colorPicker3("Color Picker", color)) {
                        selectedShape.color.set(color[0], color[1], color[2]);
                    }

                    if (ImGui.inputFloat3("RGB", color)) {
                        color[0] = Math.max(0, Math.min(1, color[0]));
                        color[1] = Math.max(0, Math.min(1, color[1]));
                        color[2] = Math.max(0, Math.min(1, color[2]));
                        selectedShape.color.set(color[0], color[1], color[2]);
                    }

                    ImGui.colorButton("Preview", color);
                }

                ImGui.spacing();

                if (ImGui.button("Delete Object", -1, 0)) {
                    scene.remove(selectedObject);
                    selection.clearSelection();
                }
            } else {
                ImGui.text("Object properties not available");
            }
        } else {
            ImGui.textDisabled("Multiple objects selected (" + selected.size() + ")");
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

        if (ImGui.dragFloat2("##v", values, 0.1f))
            changed = true;

        ImGui.columns(1);

        ImGui.popID();

        return changed;
    }

    private void renderSelectionOverlay() {
        if (selection == null) return;

        float[] selectRect = selection.getAreaSelectRect();
        if (selectRect != null) {
            drawRect(selectRect, 1.0f, 1.0f, 1.0f, 0.5f);
        }

        // Весь этот блок удаляем
        // if (toolManager != null) {
        //     float[] createRect = toolManager.getCreationRect();
        //     if (createRect != null) {
        //         drawRect(createRect, 0.0f, 1.0f, 0.0f, 0.3f);
        //     }
        // }
    }

    private void drawRect(float[] rect, float r, float g, float b, float a) {
        ImGui.begin("##Overlay",
                ImGuiWindowFlags.NoTitleBar |
                        ImGuiWindowFlags.NoInputs |
                        ImGuiWindowFlags.NoMove |
                        ImGuiWindowFlags.NoScrollbar |
                        ImGuiWindowFlags.NoBackground |
                        ImGuiWindowFlags.NoDocking);

        ImGui.setWindowPos(0, 0);
        ImGui.setWindowSize(ImGui.getIO().getDisplaySizeX(), ImGui.getIO().getDisplaySizeY());

        ImGui.getWindowDrawList().addRect(
                rect[0], rect[1],
                rect[0] + rect[2], rect[1] + rect[3],
                ImGui.getColorU32(r, g, b, a),
                0, 0, 2.0f
        );

        ImGui.end();
    }

    public void dispose() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
    }

    public void setSelectionManager(SelectionManager selection) {
        this.selection = selection;
    }

    public void setToolManager(ToolManager toolManager) {
        this.toolManager = toolManager;
    }

    // Добавьте это внутрь класса GuiManager
    public ImGuiImplGlfw getImGuiGlfw() {
        return imGuiGlfw;
    }

}