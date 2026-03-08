package org.example;

import org.joml.Vector2f;
import java.util.List;
import static org.lwjgl.glfw.GLFW.*;

public class ToolManager {
    private Tool currentTool = Tool.SELECT;
    private SelectionManager selectionManager;
    private Scene scene;
    private Camera camera;
    private Shader shader;
    private Gizmo gizmo;

    // Для создания фигур
    private boolean isCreating = false;
    private boolean previewCreated = false;
    private Vector2f startWorld;
    private Vector2f endWorld;
    private Shape previewShape;

    public ToolManager(Scene scene, Camera camera, Shader shader, SelectionManager selectionManager) {
        this.scene = scene;
        this.camera = camera;
        this.shader = shader;
        this.selectionManager = selectionManager;
        this.gizmo = new Gizmo(camera);
    }

    public void setTool(Tool tool) {
        if (isCreating && previewShape != null) {
            scene.remove(previewShape);
            previewShape = null;
        }

        this.currentTool = tool;
        previewCreated = false;
        isCreating = false;
        gizmo.endDrag();
    }

    public Tool getCurrentTool() {
        return currentTool;
    }

    public Gizmo getGizmo() {
        return gizmo;
    }

    public void mouseButtonCallback(int button, int action, int mods, double mouseX, double mouseY) {
        if (button != GLFW_MOUSE_BUTTON_LEFT) return;

        Vector2f worldPos = camera.screenToWorld((float) mouseX, (float) mouseY);
        Vector2f screenPos = new Vector2f((float) mouseX, (float) mouseY);
        boolean ctrlPressed = (mods & GLFW_MOD_CONTROL) != 0;

        if (action == GLFW_PRESS) {
            // Инструменты создания фигур
            if (currentTool == Tool.CREATE_TRIANGLE || currentTool == Tool.CREATE_RECTANGLE) {
                SceneObject picked = pickObject(worldPos);
                if (picked == null) {
                    isCreating = true;
                    previewCreated = false;
                    startWorld = new Vector2f(worldPos);
                    endWorld = new Vector2f(worldPos);
                } else {
                    selectionManager.selectSingle(picked);
                }
                return;
            }

            // Трансформационные инструменты
            if (currentTool == Tool.TRANSLATE || currentTool == Tool.ROTATE || currentTool == Tool.SCALE) {
                List<SceneObject> selected = selectionManager.getSelectedObjects();

                // Если есть выделенный объект, проверяем гизмо
                if (selected.size() == 1 && selected.get(0) instanceof Shape) {
                    Shape shape = (Shape) selected.get(0);
                    Gizmo.Operation op = gizmo.pickOperation(screenPos, shape, currentTool);

                    if (op != Gizmo.Operation.NONE) {
                        gizmo.startDrag(op, shape, worldPos, screenPos);
                        return;
                    }
                }

                // Если не попали в гизмо, проверяем выделение
                SceneObject picked = pickObject(worldPos);

                if (picked != null) {
                    if (ctrlPressed) {
                        if (selectionManager.isSelected(picked)) {
                            selectionManager.removeFromSelection(picked);
                        } else {
                            selectionManager.addToSelection(picked);
                        }
                    } else {
                        selectionManager.selectSingle(picked);
                    }
                } else {
                    selectionManager.clearSelection();
                }
                return;
            }

            // Инструмент SELECT
            if (currentTool == Tool.SELECT) {
                SceneObject picked = pickObject(worldPos);

                if (picked != null) {
                    if (ctrlPressed) {
                        if (selectionManager.isSelected(picked)) {
                            selectionManager.removeFromSelection(picked);
                        } else {
                            selectionManager.addToSelection(picked);
                        }
                    } else {
                        selectionManager.selectSingle(picked);
                    }
                } else {
                    selectionManager.startAreaSelection(mouseX, mouseY);
                }
            }
        }
        else if (action == GLFW_RELEASE) {
            if (isCreating) {
                if (previewCreated) {
                    finishCreation();
                }
                if (previewShape != null) {
                    scene.remove(previewShape);
                    previewShape = null;
                }
                isCreating = false;
                previewCreated = false;
            }

            if (gizmo.isDragging()) {
                gizmo.endDrag();
            }

            if (currentTool == Tool.SELECT) {
                selectionManager.endAreaSelection();
            }
        }
    }

    public void cursorPosCallback(double mouseX, double mouseY) {
        Vector2f worldPos = camera.screenToWorld((float) mouseX, (float) mouseY);
        Vector2f screenPos = new Vector2f((float) mouseX, (float) mouseY);

        if (gizmo.isDragging()) {
            gizmo.updateDrag(worldPos, screenPos);
        } else if (isCreating) {
            updateCreation(worldPos);
        } else if (currentTool == Tool.SELECT) {
            selectionManager.updateAreaSelection(mouseX, mouseY);
        }
    }

    private void updateCreation(Vector2f worldPos) {
        endWorld = worldPos;
        float dx = endWorld.x - startWorld.x;
        float dy = endWorld.y - startWorld.y;
        float distance = (float) Math.sqrt(dx*dx + dy*dy);

        if (!previewCreated && distance > 0.1f) {
            createPreviewShape();
            previewCreated = true;
        }
        if (previewShape != null) {
            updatePreviewShape();
        }
    }

    private SceneObject pickObject(Vector2f worldPos) {
        var objects = scene.getSceneObjectList();
        for (int i = objects.size() - 1; i >= 0; i--) {
            SceneObject obj = objects.get(i);
            if (obj.containsPoint(worldPos.x, worldPos.y)) {
                return obj;
            }
        }
        return null;
    }

    private void createPreviewShape() {
        switch (currentTool) {
            case CREATE_TRIANGLE:
                previewShape = new Triangle(shader);
                break;
            case CREATE_RECTANGLE:
                previewShape = new Rectangle(shader);
                break;
            default:
                return;
        }
        if (previewShape != null) {
            previewShape.isPreview = true;
            previewShape.color.set(1.0f, 1.0f, 1.0f);
            scene.add(previewShape);
        }
    }

    private void updatePreviewShape() {
        if (previewShape == null || startWorld == null || endWorld == null) return;

        float minX = Math.min(startWorld.x, endWorld.x);
        float maxX = Math.max(startWorld.x, endWorld.x);
        float minY = Math.min(startWorld.y, endWorld.y);
        float maxY = Math.max(startWorld.y, endWorld.y);

        float width = maxX - minX;
        float height = maxY - minY;
        float centerX = (minX + maxX) / 2;
        float centerY = (minY + maxY) / 2;

        previewShape.transform.scale.set(width, height);
        previewShape.transform.position.set(centerX, centerY);
    }

    private void finishCreation() {
        if (startWorld == null || endWorld == null) return;

        float minX = Math.min(startWorld.x, endWorld.x);
        float maxX = Math.max(startWorld.x, endWorld.x);
        float minY = Math.min(startWorld.y, endWorld.y);
        float maxY = Math.max(startWorld.y, endWorld.y);

        float width = maxX - minX;
        float height = maxY - minY;
        float centerX = (minX + maxX) / 2;
        float centerY = (minY + maxY) / 2;

        if (width < 0.1f || height < 0.1f) return;

        Shape newShape = null;
        switch (currentTool) {
            case CREATE_TRIANGLE:
                newShape = new Triangle(shader);
                break;
            case CREATE_RECTANGLE:
                newShape = new Rectangle(shader);
                break;
        }

        if (newShape != null) {
            newShape.transform.scale.set(width, height);
            newShape.transform.position.set(centerX, centerY);
            scene.add(newShape);
        }
    }

    public boolean isCreating() {
        return isCreating;
    }
}
