package org.example;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.joml.Vector3f;

public class GuiManager {

    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    private SelectionManager selection;

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

        int dockFlags = ImGuiDockNodeFlags.PassthruCentralNode;
        ImGui.dockSpaceOverViewport(ImGui.getMainViewport(), dockFlags);

        renderHierarchy(scene);
        renderInspector(scene);

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
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
                        selection.select(obj);
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

        SceneObject selectedObject = selection.getSelected();

        if (selectedObject != null && selectedObject instanceof Shape) {
            Shape selectedShape = (Shape) selectedObject;

            if (ImGui.collapsingHeader("Transform", ImGuiTreeNodeFlags.DefaultOpen)) {

                float[] pos = {
                        selectedShape.transform.position.x,
                        selectedShape.transform.position.y
                };

                if (drawVec2Control("Position", pos)) {
                    selectedShape.transform.position.set(pos[0], pos[1]);
                }
            }

            ImGui.spacing();

            if (ImGui.collapsingHeader("Color", ImGuiTreeNodeFlags.DefaultOpen)) {

                float[] color = {
                        selectedShape.color.x,
                        selectedShape.color.y,
                        selectedShape.color.z
                };

                // Color Picker
                if (ImGui.colorPicker3("Color Picker", color)) {
                    selectedShape.color.set(color[0], color[1], color[2]);
                }

                // Ручной ввод RGB
                if (ImGui.inputFloat3("RGB", color)) {
                    // Ограничиваем значения от 0 до 1
                    color[0] = Math.max(0, Math.min(1, color[0]));
                    color[1] = Math.max(0, Math.min(1, color[1]));
                    color[2] = Math.max(0, Math.min(1, color[2]));
                    selectedShape.color.set(color[0], color[1], color[2]);
                }

                // Предпросмотр цвета
                ImGui.colorButton("Preview", color);
            }

            ImGui.spacing();

            if (ImGui.button("Delete Object", -1, 0)) {
                scene.remove(selectedObject);
                selection.clear();
            }

        } else if (selectedObject != null) {
            // Если объект не Shape (маловероятно)
            ImGui.text("Object properties not available");
        } else {
            ImGui.textDisabled("Select an object to see properties");
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

    public void dispose() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
    }

    public void setSelectionManager(SelectionManager selection) {
        this.selection = selection;
    }
}