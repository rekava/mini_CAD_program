package org.example;

import org.joml.Vector2f;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SelectionManager {

    private List<SceneObject> selectedObjects;
    private List<Shape> clipboard;
    private Scene scene;
    private Camera camera;
    private CommandManager commandManager;

    private boolean areaSelectActive;
    private float startX, startY;
    private float endX, endY;
    private boolean ctrlPressed;

    public SelectionManager(Scene scene, Camera camera) {
        this.scene = scene;
        this.camera = camera;
        this.selectedObjects = new ArrayList<>();
        this.clipboard = new ArrayList<>();
        this.areaSelectActive = false;
        this.startX = 0;
        this.startY = 0;
        this.endX = 0;
        this.endY = 0;
        this.ctrlPressed = false;
    }

    public void setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    // ==================== ОСНОВНЫЕ ОПЕРАЦИИ ВЫДЕЛЕНИЯ ====================

    public void selectSingle(SceneObject obj) {
        clearSelection();
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

    public void toggleSelection(SceneObject obj) {
        if (obj == null) return;
        if (selectedObjects.contains(obj)) {
            removeFromSelection(obj);
        } else {
            addToSelection(obj);
        }
    }

    public void clearSelection() {
        for (SceneObject obj : selectedObjects) {
            obj.setSelected(false);
        }
        selectedObjects.clear();
    }

    public void selectAll() {
        clearSelection();
        for (SceneObject obj : scene.getSceneObjectList()) {
            obj.setSelected(true);
            selectedObjects.add(obj);
        }
    }

    public void deleteSelected() {
        List<Shape> selectedShapes = getSelectedShapes();
        if (commandManager != null && !selectedShapes.isEmpty()) {
            commandManager.execute(new DeleteCommand(scene, selectedShapes));
        } else {
            for (Shape shape : selectedShapes) {
                scene.remove(shape);
            }
            clearSelection();
        }
    }

    // ==================== ПОЛУЧЕНИЕ ВЫДЕЛЕННЫХ ОБЪЕКТОВ ====================

    public List<SceneObject> getSelectedObjects() {
        return selectedObjects;
    }

    public List<Shape> getSelectedShapes() {
        return selectedObjects.stream()
            .filter(obj -> obj instanceof Shape)
            .map(obj -> (Shape) obj)
            .collect(Collectors.toList());
    }

    public SceneObject getPrimarySelected() {
        if (selectedObjects.isEmpty()) return null;
        return selectedObjects.get(selectedObjects.size() - 1);
    }

    public boolean isSelected(SceneObject obj) {
        return selectedObjects.contains(obj);
    }

    public int getSelectionCount() {
        return selectedObjects.size();
    }

    public int getSelectedShapeCount() {
        return (int) selectedObjects.stream().filter(obj -> obj instanceof Shape).count();
    }

    public boolean hasSelection() {
        return !selectedObjects.isEmpty();
    }

    // ==================== COPY / PASTE ====================

    public void copySelected() {
        clipboard.clear();
        List<Shape> selectedShapes = getSelectedShapes();
        for (Shape original : selectedShapes) {
            Shape copy = copyShape(original);
            if (copy != null) {
                clipboard.add(copy);
            }
        }
        System.out.println("Copied " + clipboard.size() + " objects");
    }

    public void paste() {
        if (clipboard.isEmpty()) return;

        List<Shape> pastedShapes = new ArrayList<>();
        for (Shape shape : clipboard) {
            Shape copy = copyShape(shape);
            if (copy != null) {
                copy.transform.position.x += 0.5f;
                copy.transform.position.y += 0.5f;
                pastedShapes.add(copy);
            }
        }

        if (commandManager != null && !pastedShapes.isEmpty()) {
            commandManager.execute(new PasteCommand(scene, pastedShapes));
        } else {
            for (Shape shape : pastedShapes) {
                scene.add(shape);
            }
        }

        clearSelection();
        for (Shape shape : pastedShapes) {
            shape.setSelected(true);
            selectedObjects.add(shape);
        }

        System.out.println("Pasted " + pastedShapes.size() + " objects");
    }

    private Shape copyShape(Shape original) {
        Shape copy;
        if (original instanceof Triangle) {
            copy = new Triangle(original.shader);
        } else if (original instanceof Rectangle) {
            copy = new Rectangle(original.shader);
        } else {
            return null;
        }

        copy.transform.position.set(original.transform.position);
        copy.transform.scale.set(original.transform.scale);
        copy.transform.rotation = original.transform.rotation;
        copy.color.set(original.color);
        copy.setName(original.getName() + " (Copy)");

        return copy;
    }

    // ==================== ВЫДЕЛЕНИЕ ОБЛАСТЬЮ ====================

    public void startAreaSelection(double mouseX, double mouseY, boolean ctrl) {
        startX = (float) mouseX;
        startY = (float) mouseY;
        endX = (float) mouseX;
        endY = (float) mouseY;
        areaSelectActive = true;
        ctrlPressed = ctrl;

        if (!ctrlPressed) {
            clearSelection();
        }
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

        List<SceneObject> objectsInRect = new ArrayList<>();
        for (SceneObject obj : scene.getSceneObjectList()) {
            if (obj.intersectsRect(minX, maxX, minY, maxY)) {
                objectsInRect.add(obj);
            }
        }

        if (ctrlPressed) {
            for (SceneObject obj : objectsInRect) {
                if (!selectedObjects.contains(obj)) {
                    obj.setSelected(true);
                    selectedObjects.add(obj);
                }
            }
        } else {
            clearSelection();
            for (SceneObject obj : objectsInRect) {
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

    // ==================== ВЫДЕЛЕНИЕ ПО КЛИКУ ====================

    public void selectAtWorldPosition(Vector2f worldPos, boolean ctrl, boolean shift) {
        SceneObject picked = pickObjectAtWorldPosition(worldPos);

        if (picked != null) {
            if (ctrl) {
                toggleSelection(picked);
            } else if (shift) {
                addToSelection(picked);
            } else {
                selectSingle(picked);
            }
        } else if (!ctrl && !shift) {
            clearSelection();
        }
    }

    private SceneObject pickObjectAtWorldPosition(Vector2f worldPos) {
        List<SceneObject> objects = scene.getSceneObjectList();
        for (int i = objects.size() - 1; i >= 0; i--) {
            SceneObject obj = objects.get(i);
            if (obj.containsPoint(worldPos.x, worldPos.y)) {
                return obj;
            }
        }
        return null;
    }

    // ==================== ГРУППОВЫЕ ТРАНСФОРМАЦИИ ====================

    public Vector2f getSelectionCenter() {
        if (selectedObjects.isEmpty()) return new Vector2f(0, 0);

        Vector2f center = new Vector2f(0, 0);
        for (SceneObject obj : selectedObjects) {
            center.x += obj.transform.position.x;
            center.y += obj.transform.position.y;
        }
        center.x /= selectedObjects.size();
        center.y /= selectedObjects.size();
        return center;
    }

    public float[] getSelectionBounds() {
        if (selectedObjects.isEmpty()) return null;

        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;

        for (SceneObject obj : selectedObjects) {
            if (obj instanceof Shape) {
                Shape shape = (Shape) obj;
                float[] vertices = shape.mesh.getVertices();
                org.joml.Matrix4f model = shape.transform.getModelMatrix();
                org.joml.Vector4f vec = new org.joml.Vector4f();

                for (int i = 0; i < vertices.length; i += 3) {
                    vec.set(vertices[i], vertices[i+1], vertices[i+2], 1.0f);
                    model.transform(vec);
                    minX = Math.min(minX, vec.x);
                    maxX = Math.max(maxX, vec.x);
                    minY = Math.min(minY, vec.y);
                    maxY = Math.max(maxY, vec.y);
                }
            }
        }

        if (minX == Float.MAX_VALUE) return null;
        return new float[]{minX, minY, maxX - minX, maxY - minY};
    }

    public void translateSelected(float dx, float dy) {
        for (SceneObject obj : selectedObjects) {
            obj.transform.position.x += dx;
            obj.transform.position.y += dy;
        }
    }

    public void scaleSelected(float scaleX, float scaleY) {
        Vector2f center = getSelectionCenter();
        for (SceneObject obj : selectedObjects) {
            Vector2f relativePos = new Vector2f(obj.transform.position).sub(center);
            relativePos.mul(scaleX, scaleY);
            obj.transform.position.set(center.x + relativePos.x, center.y + relativePos.y);
            if (obj instanceof Shape) {
                ((Shape) obj).transform.scale.x *= scaleX;
                ((Shape) obj).transform.scale.y *= scaleY;
            }
        }
    }

    public void rotateSelected(float angle) {
        Vector2f center = getSelectionCenter();
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        for (SceneObject obj : selectedObjects) {
            Vector2f relativePos = new Vector2f(obj.transform.position).sub(center);
            float newX = relativePos.x * cos - relativePos.y * sin;
            float newY = relativePos.x * sin + relativePos.y * cos;
            obj.transform.position.set(center.x + newX, center.y + newY);

            if (obj instanceof Shape) {
                ((Shape) obj).transform.rotation += angle;
            }
        }
    }

    // ==================== ЦВЕТ ====================

    public void setColorSelected(float r, float g, float b) {
        for (Shape shape : getSelectedShapes()) {
            shape.color.set(r, g, b);
        }
    }

    public float[] getAverageColor() {
        List<Shape> shapes = getSelectedShapes();
        if (shapes.isEmpty()) return new float[]{1, 1, 1};

        float r = 0, g = 0, b = 0;
        for (Shape shape : shapes) {
            r += shape.color.x;
            g += shape.color.y;
            b += shape.color.z;
        }
        return new float[]{r / shapes.size(), g / shapes.size(), b / shapes.size()};
    }

    public boolean hasSameColor() {
        List<Shape> shapes = getSelectedShapes();
        if (shapes.size() <= 1) return true;

        Vector3f firstColor = shapes.get(0).color;
        for (int i = 1; i < shapes.size(); i++) {
            if (Math.abs(firstColor.x - shapes.get(i).color.x) > 0.01f ||
                Math.abs(firstColor.y - shapes.get(i).color.y) > 0.01f ||
                Math.abs(firstColor.z - shapes.get(i).color.z) > 0.01f) {
                return false;
            }
        }
        return true;
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    public List<String> getSelectedNames() {
        return selectedObjects.stream()
            .map(SceneObject::getName)
            .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public <T extends SceneObject> List<T> getSelectedObjectsOfType(Class<T> type) {
        return selectedObjects.stream()
            .filter(type::isInstance)
            .map(obj -> (T) obj)
            .collect(Collectors.toList());
    }

    public boolean hasSelectedOfType(Class<? extends SceneObject> type) {
        return selectedObjects.stream().anyMatch(type::isInstance);
    }
}
