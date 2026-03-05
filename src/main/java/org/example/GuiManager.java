package org.example;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

public class GuiManager {
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    private SceneObject selectedObject = null;

    public void init(long windowPtr) {
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable); // Разрешаем перетаскивание окон друг в друга

        // Настройка стиля "Modern Dark"
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

        // 1. Создаем Dockspace с флагом Passthru
        int dockFlags = ImGuiDockNodeFlags.PassthruCentralNode;
        ImGui.dockSpaceOverViewport(ImGui.getMainViewport(), dockFlags);

        // 2. Рисуем твои панели
        renderHierarchy(scene);
        renderInspector(scene);

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }


    private void renderHierarchy(Scene scene) {
        ImGui.begin("Scene Hierarchy");

        // Используем Tree Nodes для иерархии (позволяет делать вложенность)
        if (ImGui.treeNodeEx("Root Scene", ImGuiTreeNodeFlags.DefaultOpen)) {
            for (SceneObject obj : scene.getSceneObjectList()) {
                int flags = ImGuiTreeNodeFlags.Leaf | ImGuiTreeNodeFlags.NoTreePushOnOpen;
                if (selectedObject == obj) flags |= ImGuiTreeNodeFlags.Selected;

                if (ImGui.treeNodeEx(obj.getName() + "##" + obj.hashCode(), flags)) {
                    if (ImGui.isItemClicked()) selectedObject = obj;
                }
            }
            ImGui.treePop();
        }

        ImGui.separator();
        if (ImGui.button("Add Object", -1, 0)) { /* logic */ }
        ImGui.end();
    }

    private void renderInspector(Scene scene) {
        ImGui.begin("Inspector");
        if (selectedObject != null) {
            // Группировка свойств через CollapsingHeader
            if (ImGui.collapsingHeader("Transform", ImGuiTreeNodeFlags.DefaultOpen)) {
                float[] pos = {selectedObject.transform.position.x, selectedObject.transform.position.y};
                if (drawVec2Control("Position", pos)) {
                    selectedObject.transform.position.set(pos[0], pos[1]);
                }
            }

            ImGui.spacing();
            if (ImGui.button("Delete Object", -1, 0)) {
                scene.remove(selectedObject);
                selectedObject = null;
            }
        } else {
            ImGui.textDisabled("Select an object to see properties");
        }
        ImGui.end();
    }

    // Вспомогательный метод для красивого ввода координат
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

    public void dispose() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
    }
}
