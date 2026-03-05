package org.example;

import org.joml.Vector2f;
import java.util.ArrayList;
import java.util.List;
import static org.lwjgl.glfw.GLFW.*;

public class SelectionManager {
    private List<SceneObject> selectedObjects = new ArrayList<>();
    private Scene scene;
    private Camera camera;

    private boolean isDragging = false;
    private float startX, startY;
    private float endX, endY;
    private boolean areaSelectActive = false;

    public SelectionManager(Scene scene, Camera camera) {
        this.scene = scene;
        this.camera = camera;
    }

    public void selectSingle(SceneObject obj) {
        // Снимаем выделение со всех
        for (SceneObject o : selectedObjects) {
            o.setSelected(false);
        }
        selectedObjects.clear();

        // Выделяем новый
        if (obj != null) {
            obj.setSelected(true);
            selectedObjects.add(obj);
        }
    }

    public void addToSelection(SceneObject obj) {
        if (obj != null && !selectedObjects.contains(obj)) {
            obj.setSelected(true);
            selectedObjects.add(obj);
        }
    }

    public void removeFromSelection(SceneObject obj) {
        if (obj != null && selectedObjects.contains(obj)) {
            obj.setSelected(false);
            selectedObjects.remove(obj);
        }
    }

    public void clearSelection() {
        for (SceneObject obj : selectedObjects) {
            obj.setSelected(false);
        }
        selectedObjects.clear();
    }

    public List<SceneObject> getSelectedObjects() {
        return selectedObjects;
    }

    public SceneObject getPrimarySelected() {
        if (selectedObjects.isEmpty()) return null;
        return selectedObjects.get(selectedObjects.size() - 1); // последний выделенный
    }

    public boolean isSelected(SceneObject obj) {
        return selectedObjects.contains(obj);
    }

    public void mouseButtonCallback(int button, int action, double mouseX, double mouseY) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) {
            if (action == GLFW_PRESS) {
                Vector2f worldPos = camera.screenToWorld((float) mouseX, (float) mouseY);

                SceneObject picked = null;
                var objects = scene.getSceneObjectList();
                for (int i = objects.size() - 1; i >= 0; i--) {
                    SceneObject obj = objects.get(i);
                    if (obj.containsPoint(worldPos.x, worldPos.y)) {
                        picked = obj;
                        break;
                    }
                }

                if (picked != null) {
                    // Если зажат Ctrl — добавляем к выделению, иначе только этот
                    boolean ctrlPressed = false; // TODO: получить состояние Ctrl
                    if (ctrlPressed) {
                        if (isSelected(picked)) {
                            removeFromSelection(picked);
                        } else {
                            addToSelection(picked);
                        }
                    } else {
                        selectSingle(picked);
                    }
                    isDragging = false;
                    areaSelectActive = false;
                } else {
                    clearSelection(); // теперь точно сбрасываем всё
                    startX = (float) mouseX;
                    startY = (float) mouseY;
                    endX = (float) mouseX;
                    endY = (float) mouseY;
                    isDragging = true;
                    areaSelectActive = true;
                }
            } else if (action == GLFW_RELEASE) {
                if (areaSelectActive) {
                    applyAreaSelection();
                    areaSelectActive = false;
                }
                isDragging = false;
            }
        }
    }

    public void cursorPosCallback(double mouseX, double mouseY) {
        if (isDragging && areaSelectActive) {
            endX = (float) mouseX;
            endY = (float) mouseY;
        }
    }

    private void applyAreaSelection() {
        Vector2f startWorld = camera.screenToWorld(startX, startY);
        Vector2f endWorld = camera.screenToWorld(endX, endY);

        float minX = Math.min(startWorld.x, endWorld.x);
        float maxX = Math.max(startWorld.x, endWorld.x);
        float minY = Math.min(startWorld.y, endWorld.y);
        float maxY = Math.max(startWorld.y, endWorld.y);

        // Снимаем выделение со всех
        clearSelection();

        // Выделяем объекты внутри рамки
        for (SceneObject obj : scene.getSceneObjectList()) {
            if (obj.intersectsRect(minX, maxX, minY, maxY)) {
                obj.setSelected(true);
                selectedObjects.add(obj);
            }
        }
    }

    public boolean isAreaSelectActive() {
        return areaSelectActive;
    }

    public float[] getAreaSelectRect() {
        if (!areaSelectActive) return null;

        float left = Math.min(startX, endX);
        float right = Math.max(startX, endX);
        float top = Math.min(startY, endY);
        float bottom = Math.max(startY, endY);

        return new float[]{left, top, right - left, bottom - top};
    }
}