package org.example;
import org.joml.Vector2f;
import static org.lwjgl.glfw.GLFW.*;

public class SelectionManager {
    private SceneObject selectedObject;
    private Scene scene;
    private Camera camera;

    public SelectionManager(Scene scene, Camera camera) {
        this.scene = scene;
        this.camera = camera;
    }

    public void select(SceneObject obj) {
        if (selectedObject != null) {
            selectedObject.setSelected(false);
        }
        selectedObject = obj;
        if (selectedObject != null) {
            selectedObject.setSelected(true);
        }
    }

    public void clear() {
        if (selectedObject != null) {
            selectedObject.setSelected(false);
        }
        selectedObject = null;
    }

    public SceneObject getSelected() {
        return selectedObject;
    }

    public boolean isSelected(SceneObject obj) {
        return selectedObject == obj;
    }

    public void mouseButtonCallback(int button, int action, double mouseX, double mouseY) {
        if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
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
                select(picked);
            } else {
                clear();
            }
        }
    }
}