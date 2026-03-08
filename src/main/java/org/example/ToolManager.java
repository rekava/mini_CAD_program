package org.example;

import org.joml.Vector2f;
import static org.lwjgl.glfw.GLFW.*;

public class ToolManager {
    private Tool currentTool = Tool.SELECT;

    private boolean isCreating = false;
    private boolean previewCreated = false; // новый флаг
    private Vector2f startWorld;
    private Vector2f endWorld;

    private Scene scene;
    private Camera camera;
    private Shader shader;

    private Shape previewShape;

    public ToolManager(Scene scene, Camera camera, Shader shader) {
        this.scene = scene;
        this.camera = camera;
        this.shader = shader;
    }

    public void setTool(Tool tool) {
        this.currentTool = tool;
        if (previewShape != null) {
            scene.remove(previewShape);
            previewShape = null;
        }
        previewCreated = false;
    }

    public Tool getCurrentTool() {
        return currentTool;
    }

    public void mouseButtonCallback(int button, int action, double mouseX, double mouseY) {
        if (button != GLFW_MOUSE_BUTTON_LEFT) return;

        Vector2f worldPos = camera.screenToWorld((float) mouseX, (float) mouseY);

        if (action == GLFW_PRESS) {
            if (currentTool != Tool.SELECT) {
                isCreating = true;
                previewCreated = false; // сбрасываем флаг
                startWorld = new Vector2f(worldPos);
                endWorld = new Vector2f(worldPos);

                // НЕ создаём призрак сразу
            }
        } else if (action == GLFW_RELEASE) {
            if (isCreating) {
                if (previewCreated) {
                    // Завершаем создание только если был предпросмотр
                    finishCreation();
                }

                // Удаляем призрак
                if (previewShape != null) {
                    scene.remove(previewShape);
                    previewShape = null;
                }

                isCreating = false;
                previewCreated = false;
            }
        }
    }

    public void cursorPosCallback(double mouseX, double mouseY) {
        if (isCreating) {
            Vector2f worldPos = camera.screenToWorld((float) mouseX, (float) mouseY);
            endWorld = worldPos;

            // Проверяем, достаточно ли далеко ушла мышь
            float dx = endWorld.x - startWorld.x;
            float dy = endWorld.y - startWorld.y;
            float distance = (float) Math.sqrt(dx*dx + dy*dy);

            if (!previewCreated && distance > 0.1f) {
                // Создаём призрак только при первом значительном движении
                createPreviewShape();
                previewCreated = true;
            }

            // Обновляем позицию и размер призрака, если он существует
            if (previewShape != null) {
                updatePreviewShape();
            }
        }
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