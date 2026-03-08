package org.example;

import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.Matrix4f;
import imgui.ImGui;
import imgui.ImDrawList;

public class Gizmo {
    public enum Operation {
        NONE,
        TRANSLATE_X, TRANSLATE_Y,
        ROTATE,
        SCALE_X, SCALE_Y, SCALE_ALL,
        SCALE_TOP_LEFT, SCALE_TOP_RIGHT, SCALE_BOTTOM_LEFT, SCALE_BOTTOM_RIGHT
    }

    private Operation currentOperation = Operation.NONE;
    private boolean isDragging = false;
    private Vector2f dragStartWorld;
    private Vector2f dragStartScreen;
    private Shape selectedShape;
    private Camera camera;

    private Vector2f initialPosition;
    private float initialRotation;
    private Vector2f initialScale;
    private Vector2f[] initialCorners; // Сохраняем начальные углы в мировых координатах

    private static final float HANDLE_SIZE = 10;
    private static final float ROTATE_RADIUS = 40;
    private static final float ARROW_LENGTH = 0.5f;

    public Gizmo(Camera camera) {
        this.camera = camera;
    }

    public boolean isDragging() {
        return isDragging;
    }

    public Operation getCurrentOperation() {
        return currentOperation;
    }

    public void startDrag(Operation op, Shape shape, Vector2f worldPos, Vector2f screenPos) {
        currentOperation = op;
        isDragging = true;
        selectedShape = shape;
        dragStartWorld = new Vector2f(worldPos);
        dragStartScreen = new Vector2f(screenPos);

        initialPosition = new Vector2f(shape.transform.position);
        initialRotation = shape.transform.rotation;
        initialScale = new Vector2f(shape.transform.scale);
        initialCorners = getWorldCorners(shape); // Сохраняем углы в момент начала драга
    }

    public void updateDrag(Vector2f currentWorldPos, Vector2f currentScreenPos) {
        if (!isDragging || selectedShape == null) return;

        Vector2f worldDelta = new Vector2f(currentWorldPos).sub(dragStartWorld);
        Vector2f screenDelta = new Vector2f(currentScreenPos).sub(dragStartScreen);

        switch (currentOperation) {
            case TRANSLATE_X:
                // Перемещение по глобальной X
                selectedShape.transform.position.x = initialPosition.x + worldDelta.x;
                selectedShape.transform.position.y = initialPosition.y;
                break;

            case TRANSLATE_Y:
                // Перемещение по глобальной Y
                selectedShape.transform.position.x = initialPosition.x;
                selectedShape.transform.position.y = initialPosition.y + worldDelta.y;
                break;

            case ROTATE:
                // Поворот
                float angleDelta = screenDelta.x * 0.01f;
                selectedShape.transform.rotation = initialRotation + angleDelta;
                break;

            case SCALE_X:
                // Масштабирование по локальной X
                scaleLocal(1.0f + worldDelta.x * 2.0f, 1.0f);
                break;

            case SCALE_Y:
                // Масштабирование по локальной Y
                scaleLocal(1.0f, 1.0f + worldDelta.y * 2.0f);
                break;

            case SCALE_ALL:
                // Равномерное масштабирование
                float scale = 1.0f + worldDelta.x * 2.0f;
                scale = Math.max(0.1f, Math.min(5.0f, scale));
                scaleLocal(scale, scale);
                break;

            case SCALE_TOP_LEFT:
                scaleFromCorner(currentWorldPos, 0);
                break;
            case SCALE_TOP_RIGHT:
                scaleFromCorner(currentWorldPos, 1);
                break;
            case SCALE_BOTTOM_LEFT:
                scaleFromCorner(currentWorldPos, 2);
                break;
            case SCALE_BOTTOM_RIGHT:
                scaleFromCorner(currentWorldPos, 3);
                break;
        }
    }

    private void scaleLocal(float scaleX, float scaleY) {
        if (selectedShape == null) return;

        // Применяем масштабирование с учетом текущего поворота
        selectedShape.transform.scale.x = initialScale.x * scaleX;
        selectedShape.transform.scale.y = initialScale.y * scaleY;
    }

    private void scaleFromCorner(Vector2f currentWorldPos, int cornerIndex) {
        if (initialCorners == null) return;

        // Определяем противоположный угол (который остается на месте)
        int oppositeCorner;
        switch (cornerIndex) {
            case 0: oppositeCorner = 3; break; // верхний левый -> нижний правый
            case 1: oppositeCorner = 2; break; // верхний правый -> нижний левый
            case 2: oppositeCorner = 1; break; // нижний левый -> верхний правый
            case 3: oppositeCorner = 0; break; // нижний правый -> верхний левый
            default: return;
        }

        Vector2f fixedCorner = initialCorners[oppositeCorner];
        Vector2f movingCorner = currentWorldPos;

        // Вычисляем новые границы в мировых координатах
        float minX = Math.min(fixedCorner.x, movingCorner.x);
        float maxX = Math.max(fixedCorner.x, movingCorner.x);
        float minY = Math.min(fixedCorner.y, movingCorner.y);
        float maxY = Math.max(fixedCorner.y, movingCorner.y);

        // Проверяем минимальный размер
        float newWidth = Math.max(0.1f, maxX - minX);
        float newHeight = Math.max(0.1f, maxY - minY);

        // Новый центр
        float newCenterX = (minX + maxX) / 2;
        float newCenterY = (minY + maxY) / 2;

        // Вычисляем новый масштаб относительно начального
        // Для этого нужно получить размеры объекта в локальных координатах
        float[] vertices = selectedShape.mesh.getVertices();
        float localMinX = Float.MAX_VALUE, localMaxX = -Float.MAX_VALUE;
        float localMinY = Float.MAX_VALUE, localMaxY = -Float.MAX_VALUE;

        for (int i = 0; i < vertices.length; i += 3) {
            localMinX = Math.min(localMinX, vertices[i]);
            localMaxX = Math.max(localMaxX, vertices[i]);
            localMinY = Math.min(localMinY, vertices[i+1]);
            localMaxY = Math.max(localMaxY, vertices[i+1]);
        }

        float localWidth = localMaxX - localMinX;
        float localHeight = localMaxY - localMinY;

        if (localWidth == 0 || localHeight == 0) return;

        // Новый масштаб
        float newScaleX = newWidth / localWidth;
        float newScaleY = newHeight / localHeight;

        selectedShape.transform.position.set(newCenterX, newCenterY);
        selectedShape.transform.scale.set(newScaleX, newScaleY);
    }

    public void endDrag() {
        isDragging = false;
        currentOperation = Operation.NONE;
        selectedShape = null;
        initialCorners = null;
    }

    public Operation pickOperation(Vector2f screenPos, Shape shape, Tool currentTool) {
        if (shape == null) return Operation.NONE;

        Vector2f centerScreen = camera.worldToScreen(shape.transform.position.x, shape.transform.position.y);
        float distThreshold = HANDLE_SIZE * 2;

        switch (currentTool) {
            case TRANSLATE:
                // Стрелка X
                Vector2f xArrowEnd = camera.worldToScreen(
                    shape.transform.position.x + ARROW_LENGTH,
                    shape.transform.position.y
                );
                if (distanceToLineSegment(screenPos, centerScreen, xArrowEnd) < distThreshold) {
                    return Operation.TRANSLATE_X;
                }

                // Стрелка Y
                Vector2f yArrowEnd = camera.worldToScreen(
                    shape.transform.position.x,
                    shape.transform.position.y + ARROW_LENGTH
                );
                if (distanceToLineSegment(screenPos, centerScreen, yArrowEnd) < distThreshold) {
                    return Operation.TRANSLATE_Y;
                }
                break;

            case ROTATE:
                // Круг поворота
                float distToCenter = screenPos.distance(centerScreen);
                if (Math.abs(distToCenter - ROTATE_RADIUS) < HANDLE_SIZE * 2) {
                    return Operation.ROTATE;
                }
                break;

            case SCALE:
                Vector2f[] corners = getScreenCorners(shape);
                Operation[] cornerOps = {
                    Operation.SCALE_TOP_LEFT, Operation.SCALE_TOP_RIGHT,
                    Operation.SCALE_BOTTOM_LEFT, Operation.SCALE_BOTTOM_RIGHT
                };

                // Проверяем углы
                for (int i = 0; i < corners.length; i++) {
                    if (screenPos.distance(corners[i]) < HANDLE_SIZE * 2) {
                        return cornerOps[i];
                    }
                }

                // Проверяем середины сторон
                Vector2f[] edgeCenters = getScreenEdgeCenters(shape);
                Operation[] edgeOps = {Operation.SCALE_Y, Operation.SCALE_X, Operation.SCALE_Y, Operation.SCALE_X};

                for (int i = 0; i < edgeCenters.length; i++) {
                    if (edgeCenters[i] != null && screenPos.distance(edgeCenters[i]) < HANDLE_SIZE * 2) {
                        return edgeOps[i];
                    }
                }

                // Центр для равномерного масштабирования
                if (screenPos.distance(centerScreen) < HANDLE_SIZE * 2) {
                    return Operation.SCALE_ALL;
                }
                break;
        }

        return Operation.NONE;
    }

    public void draw(Shape shape, Tool currentTool) {
        if (shape == null) return;

        ImDrawList drawList = ImGui.getForegroundDrawList();
        Vector2f centerScreen = camera.worldToScreen(shape.transform.position.x, shape.transform.position.y);

        switch (currentTool) {
            case TRANSLATE:
                drawTranslateGizmo(drawList, shape, centerScreen);
                break;
            case ROTATE:
                drawRotateGizmo(drawList, centerScreen);
                break;
            case SCALE:
                drawScaleGizmo(drawList, shape, centerScreen);
                break;
        }
    }

    private void drawTranslateGizmo(ImDrawList drawList, Shape shape, Vector2f centerScreen) {
        // Стрелка X (красная) - всегда по глобальной X
        Vector2f xEndWorld = new Vector2f(shape.transform.position.x + ARROW_LENGTH, shape.transform.position.y);
        Vector2f xEndScreen = camera.worldToScreen(xEndWorld.x, xEndWorld.y);

        drawList.addLine(centerScreen.x, centerScreen.y, xEndScreen.x, xEndScreen.y,
            ImGui.getColorU32(1, 0, 0, 1), 3.0f);
        drawList.addCircleFilled(xEndScreen.x, xEndScreen.y, HANDLE_SIZE, ImGui.getColorU32(1, 0, 0, 1));

        // Стрелка Y (зеленая) - всегда по глобальной Y
        Vector2f yEndWorld = new Vector2f(shape.transform.position.x, shape.transform.position.y + ARROW_LENGTH);
        Vector2f yEndScreen = camera.worldToScreen(yEndWorld.x, yEndWorld.y);

        drawList.addLine(centerScreen.x, centerScreen.y, yEndScreen.x, yEndScreen.y,
            ImGui.getColorU32(0, 1, 0, 1), 3.0f);
        drawList.addCircleFilled(yEndScreen.x, yEndScreen.y, HANDLE_SIZE, ImGui.getColorU32(0, 1, 0, 1));
    }

    private void drawRotateGizmo(ImDrawList drawList, Vector2f centerScreen) {
        // Круг для поворота
        drawList.addCircle(centerScreen.x, centerScreen.y, ROTATE_RADIUS,
            ImGui.getColorU32(0, 0, 1, 1), 0, 2.0f);

        // Ручка на круге
        float angle = selectedShape != null ? selectedShape.transform.rotation : 0;
        float handleX = centerScreen.x + (float) Math.cos(angle) * ROTATE_RADIUS;
        float handleY = centerScreen.y + (float) Math.sin(angle) * ROTATE_RADIUS;

        drawList.addCircleFilled(handleX, handleY, HANDLE_SIZE, ImGui.getColorU32(0, 0, 1, 1));
        drawList.addLine(centerScreen.x, centerScreen.y, handleX, handleY,
            ImGui.getColorU32(0, 0, 1, 0.5f), 1.0f);
    }

    private void drawScaleGizmo(ImDrawList drawList, Shape shape, Vector2f centerScreen) {
        Vector2f[] corners = getScreenCorners(shape);

        // Рамка
        int white = ImGui.getColorU32(1, 1, 1, 1);
        drawList.addLine(corners[0].x, corners[0].y, corners[1].x, corners[1].y, white, 1.0f);
        drawList.addLine(corners[1].x, corners[1].y, corners[3].x, corners[3].y, white, 1.0f);
        drawList.addLine(corners[3].x, corners[3].y, corners[2].x, corners[2].y, white, 1.0f);
        drawList.addLine(corners[2].x, corners[2].y, corners[0].x, corners[0].y, white, 1.0f);

        // Угловые ручки (желтые)
        int yellow = ImGui.getColorU32(1, 1, 0, 1);
        for (Vector2f corner : corners) {
            drawList.addCircleFilled(corner.x, corner.y, HANDLE_SIZE, yellow);
        }

        // Боковые ручки (голубые)
        int cyan = ImGui.getColorU32(0, 1, 1, 1);
        Vector2f[] edgeCenters = getScreenEdgeCenters(shape);
        for (Vector2f edge : edgeCenters) {
            if (edge != null) {
                drawList.addCircleFilled(edge.x, edge.y, HANDLE_SIZE, cyan);
            }
        }

        // Центральная ручка (белая)
        drawList.addCircleFilled(centerScreen.x, centerScreen.y, HANDLE_SIZE, ImGui.getColorU32(1, 1, 1, 1));
    }

    private float distanceToLineSegment(Vector2f point, Vector2f a, Vector2f b) {
        Vector2f ab = new Vector2f(b).sub(a);
        Vector2f ap = new Vector2f(point).sub(a);

        float t = ap.dot(ab) / ab.dot(ab);
        t = Math.max(0, Math.min(1, t));

        Vector2f projection = new Vector2f(a).add(new Vector2f(ab).mul(t));
        return point.distance(projection);
    }

    private Vector2f[] getWorldCorners(Shape shape) {
        float[] vertices = shape.mesh.getVertices();
        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;

        for (int i = 0; i < vertices.length; i += 3) {
            minX = Math.min(minX, vertices[i]);
            maxX = Math.max(maxX, vertices[i]);
            minY = Math.min(minY, vertices[i+1]);
            maxY = Math.max(maxY, vertices[i+1]);
        }

        Matrix4f model = shape.transform.getModelMatrix();
        Vector4f vec = new Vector4f();

        Vector2f[] corners = new Vector2f[4];
        float[][] localCorners = {
            {minX, maxY}, // верхний левый
            {maxX, maxY}, // верхний правый
            {minX, minY}, // нижний левый
            {maxX, minY}  // нижний правый
        };

        for (int i = 0; i < 4; i++) {
            vec.set(localCorners[i][0], localCorners[i][1], 0, 1);
            model.transform(vec);
            corners[i] = new Vector2f(vec.x, vec.y);
        }

        return corners;
    }

    private Vector2f[] getScreenCorners(Shape shape) {
        Vector2f[] worldCorners = getWorldCorners(shape);
        Vector2f[] screenCorners = new Vector2f[4];
        for (int i = 0; i < 4; i++) {
            screenCorners[i] = camera.worldToScreen(worldCorners[i].x, worldCorners[i].y);
        }
        return screenCorners;
    }

    private Vector2f[] getScreenEdgeCenters(Shape shape) {
        Vector2f[] corners = getScreenCorners(shape);
        Vector2f[] edges = new Vector2f[4];

        edges[0] = new Vector2f((corners[0].x + corners[1].x) / 2, (corners[0].y + corners[1].y) / 2); // верх
        edges[1] = new Vector2f((corners[1].x + corners[3].x) / 2, (corners[1].y + corners[3].y) / 2); // право
        edges[2] = new Vector2f((corners[3].x + corners[2].x) / 2, (corners[3].y + corners[2].y) / 2); // низ
        edges[3] = new Vector2f((corners[2].x + corners[0].x) / 2, (corners[2].y + corners[0].y) / 2); // лево

        return edges;
    }
}
