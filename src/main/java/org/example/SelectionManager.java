package org.example;

import org.joml.Vector2f;
import java.util.ArrayList;
import java.util.List;
import static org.lwjgl.glfw.GLFW.*;

public class SelectionManager {
    private List<SceneObject> selectedObjects = new ArrayList<>();
    private Scene scene;
    private Camera camera;

    private boolean areaSelectActive = false;
    private float startX, startY;
    private float endX, endY;

    public SelectionManager(Scene scene, Camera camera) {
        this.scene = scene;
        this.camera = camera;
    }

    public void selectSingle(SceneObject obj) {
        for (SceneObject o : selectedObjects) {
            o.setSelected(false);
        }
        selectedObjects.clear();

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
        return selectedObjects.get(selectedObjects.size() - 1);
    }

    public boolean isSelected(SceneObject obj) {
        return selectedObjects.contains(obj);
    }

    public void startAreaSelection(double mouseX, double mouseY) {
        clearSelection();
        startX = (float) mouseX;
        startY = (float) mouseY;
        endX = (float) mouseX;
        endY = (float) mouseY;
        areaSelectActive = true;
    }

    public void updateAreaSelection(double mouseX, double mouseY) {
        if (areaSelectActive) {
            endX = (float) mouseX;
            endY = (float) mouseY;
        }
    }

    public void endAreaSelection() {
        if (areaSelectActive) {
            applyAreaSelection();
            areaSelectActive = false;
        }
    }

    private void applyAreaSelection() {
        Vector2f startWorld = camera.screenToWorld(startX, startY);
        Vector2f endWorld = camera.screenToWorld(endX, endY);

        float minX = Math.min(startWorld.x, endWorld.x);
        float maxX = Math.max(startWorld.x, endWorld.x);
        float minY = Math.min(startWorld.y, endWorld.y);
        float maxY = Math.max(startWorld.y, endWorld.y);

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
