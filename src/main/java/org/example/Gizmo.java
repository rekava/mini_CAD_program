package org.example;

import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.Matrix4f;
import imgui.ImGui;
import imgui.ImDrawList;

import java.util.List;
import java.util.ArrayList;

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
    private List<Shape> selectedShapes;
    private Camera camera;

    private Vector2f initialCenterPosition;
    private float initialRotation;
    private Vector2f initialScale;
    private Vector2f[] initialCorners;

    private List<Vector2f> initialPositions;
    private List<Float> initialRotations;
    private List<Vector2f> initialScales;

    private static final float HANDLE_SIZE = 10;
    private static final float ROTATE_RADIUS = 40;
    private static final float ARROW_LENGTH = 0.5f;

    public Gizmo(Camera camera) {
        this.camera = camera;
        this.selectedShapes = new ArrayList<>();
    }

    public boolean isDragging() {
        return isDragging;
    }

    public Operation getCurrentOperation() {
        return currentOperation;
    }

    public void startDrag(Operation op, List<Shape> shapes, Vector2f worldPos, Vector2f screenPos) {
        if (shapes == null || shapes.isEmpty()) return;

        currentOperation = op;
        isDragging = true;
        selectedShapes = new ArrayList<>(shapes);
        dragStartWorld = new Vector2f(worldPos);
        dragStartScreen = new Vector2f(screenPos);

        // Сохраняем начальные значения для всех объектов
        initialPositions = new ArrayList<>();
        initialRotations = new ArrayList<>();
        initialScales = new ArrayList<>();

        for (Shape shape : selectedShapes) {
            initialPositions.add(new Vector2f(shape.transform.position));
            initialRotations.add(shape.transform.rotation);
            initialScales.add(new Vector2f(shape.transform.scale));
        }

        // Вычисляем центр группы
        initialCenterPosition = getGroupCenter();
        initialRotation = getAverageRotation();
        initialScale = new Vector2f(1, 1);

        // Сохраняем углы группы
        initialCorners = getGroupWorldCorners();
    }

    private Vector2f getGroupCenter() {
        if (selectedShapes.isEmpty()) return new Vector2f(0, 0);
        Vector2f center = new Vector2f(0, 0);
        for (Shape shape : selectedShapes) {
            center.add(shape.transform.position);
        }
        center.div(selectedShapes.size());
        return center;
    }

    private float getAverageRotation() {
        if (selectedShapes.isEmpty()) return 0;
        float sum = 0;
        for (Shape shape : selectedShapes) {
            sum += shape.transform.rotation;
        }
        return sum / selectedShapes.size();
    }

    private Vector2f[] getGroupWorldCorners() {
        if (selectedShapes.isEmpty()) return new Vector2f[4];

        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;

        for (Shape shape : selectedShapes) {
            Vector2f[] corners = getWorldCorners(shape);
            if (corners != null) {
                for (Vector2f corner : corners) {
                    minX = Math.min(minX, corner.x);
                    maxX = Math.max(maxX, corner.x);
                    minY = Math.min(minY, corner.y);
                    maxY = Math.max(maxY, corner.y);
                }
            }
        }

        return new Vector2f[]{
            new Vector2f(minX, maxY),
            new Vector2f(maxX, maxY),
            new Vector2f(minX, minY),
            new Vector2f(maxX, minY)
        };
    }

    public void updateDrag(Vector2f currentWorldPos, Vector2f currentScreenPos) {
        if (!isDragging || selectedShapes == null || selectedShapes.isEmpty()) return;

        Vector2f worldDelta = new Vector2f(currentWorldPos).sub(dragStartWorld);
        Vector2f screenDelta = new Vector2f(currentScreenPos).sub(dragStartScreen);

        switch (currentOperation) {
            case TRANSLATE_X:
                for (int i = 0; i < selectedShapes.size(); i++) {
                    Shape shape = selectedShapes.get(i);
                    shape.transform.position.x = initialPositions.get(i).x + worldDelta.x;
                    shape.transform.position.y = initialPositions.get(i).y;
                }
                break;

            case TRANSLATE_Y:
                for (int i = 0; i < selectedShapes.size(); i++) {
                    Shape shape = selectedShapes.get(i);
                    shape.transform.position.x = initialPositions.get(i).x;
                    shape.transform.position.y = initialPositions.get(i).y + worldDelta.y;
                }
                break;

            case ROTATE:
                float angleDelta = screenDelta.x * 0.01f;
                for (int i = 0; i < selectedShapes.size(); i++) {
                    Shape shape = selectedShapes.get(i);
                    shape.transform.rotation = initialRotations.get(i) + angleDelta;
                }
                break;

            case SCALE_X:
                scaleGroup(1.0f + worldDelta.x * 2.0f, 1.0f);
                break;

            case SCALE_Y:
                scaleGroup(1.0f, 1.0f + worldDelta.y * 2.0f);
                break;

            case SCALE_ALL:
                float scale = 1.0f + worldDelta.x * 2.0f;
                scale = Math.max(0.1f, Math.min(5.0f, scale));
                scaleGroup(scale, scale);
                break;

            case SCALE_TOP_LEFT:
                scaleGroupFromCorner(currentWorldPos, 0);
                break;
            case SCALE_TOP_RIGHT:
                scaleGroupFromCorner(currentWorldPos, 1);
                break;
            case SCALE_BOTTOM_LEFT:
                scaleGroupFromCorner(currentWorldPos, 2);
                break;
            case SCALE_BOTTOM_RIGHT:
                scaleGroupFromCorner(currentWorldPos, 3);
                break;
        }
    }

    private void scaleGroup(float scaleX, float scaleY) {
        if (selectedShapes.isEmpty()) return;

        Vector2f groupCenter = getGroupCenter();

        for (int i = 0; i < selectedShapes.size(); i++) {
            Shape shape = selectedShapes.get(i);
            Vector2f initialPos = initialPositions.get(i);
            Vector2f initialScaleVec = initialScales.get(i);

            Vector2f relativePos = new Vector2f(initialPos).sub(groupCenter);
            relativePos.mul(scaleX, scaleY);

            shape.transform.position.set(groupCenter.x + relativePos.x, groupCenter.y + relativePos.y);
            shape.transform.scale.set(initialScaleVec.x * scaleX, initialScaleVec.y * scaleY);
        }
    }

    private void scaleGroupFromCorner(Vector2f currentWorldPos, int cornerIndex) {
        if (initialCorners == null) return;

        int oppositeCorner;
        switch (cornerIndex) {
            case 0: oppositeCorner = 3; break;
            case 1: oppositeCorner = 2; break;
            case 2: oppositeCorner = 1; break;
            case 3: oppositeCorner = 0; break;
            default: return;
        }

        Vector2f fixedCorner = initialCorners[oppositeCorner];
        Vector2f movingCorner = currentWorldPos;

        float minX = Math.min(fixedCorner.x, movingCorner.x);
        float maxX = Math.max(fixedCorner.x, movingCorner.x);
        float minY = Math.min(fixedCorner.y, movingCorner.y);
        float maxY = Math.max(fixedCorner.y, movingCorner.y);

        float newWidth = Math.max(0.1f, maxX - minX);
        float newHeight = Math.max(0.1f, maxY - minY);
        float newCenterX = (minX + maxX) / 2;
        float newCenterY = (minY + maxY) / 2;

        float initialMinX = Float.MAX_VALUE, initialMaxX = -Float.MAX_VALUE;
        float initialMinY = Float.MAX_VALUE, initialMaxY = -Float.MAX_VALUE;

        for (Vector2f corner : initialCorners) {
            if (corner != null) {
                initialMinX = Math.min(initialMinX, corner.x);
                initialMaxX = Math.max(initialMaxX, corner.x);
                initialMinY = Math.min(initialMinY, corner.y);
                initialMaxY = Math.max(initialMaxY, corner.y);
            }
        }

        float initialWidth = initialMaxX - initialMinX;
        float initialHeight = initialMaxY - initialMinY;

        if (initialWidth == 0 || initialHeight == 0) return;

        float scaleX = newWidth / initialWidth;
        float scaleY = newHeight / initialHeight;

        for (int i = 0; i < selectedShapes.size(); i++) {
            Shape shape = selectedShapes.get(i);
            Vector2f initialPos = initialPositions.get(i);
            Vector2f initialScaleVec = initialScales.get(i);

            Vector2f relativePos = new Vector2f(initialPos).sub(initialCenterPosition);
            Vector2f newRelativePos = new Vector2f(relativePos);
            newRelativePos.mul(scaleX, scaleY);

            shape.transform.position.set(newCenterX + newRelativePos.x, newCenterY + newRelativePos.y);
            shape.transform.scale.set(initialScaleVec.x * scaleX, initialScaleVec.y * scaleY);
        }
    }

    public void endDrag() {
        isDragging = false;
        currentOperation = Operation.NONE;
        selectedShapes = null;
        initialCorners = null;
        initialPositions = null;
        initialRotations = null;
        initialScales = null;
    }

    public Operation pickOperation(Vector2f screenPos, List<Shape> shapes, Tool currentTool) {
        if (shapes == null || shapes.isEmpty()) return Operation.NONE;

        Vector2f centerScreen = camera.worldToScreen(getGroupCenter().x, getGroupCenter().y);
        float distThreshold = HANDLE_SIZE * 2;

        switch (currentTool) {
            case TRANSLATE:
                Vector2f xArrowEnd = camera.worldToScreen(
                    getGroupCenter().x + ARROW_LENGTH,
                    getGroupCenter().y
                );
                if (distanceToLineSegment(screenPos, centerScreen, xArrowEnd) < distThreshold) {
                    return Operation.TRANSLATE_X;
                }

                Vector2f yArrowEnd = camera.worldToScreen(
                    getGroupCenter().x,
                    getGroupCenter().y + ARROW_LENGTH
                );
                if (distanceToLineSegment(screenPos, centerScreen, yArrowEnd) < distThreshold) {
                    return Operation.TRANSLATE_Y;
                }
                break;

            case ROTATE:
                float distToCenter = screenPos.distance(centerScreen);
                if (Math.abs(distToCenter - ROTATE_RADIUS) < HANDLE_SIZE * 2) {
                    return Operation.ROTATE;
                }
                break;

            case SCALE:
                Vector2f[] corners = getGroupScreenCorners();
                if (corners != null) {
                    Operation[] cornerOps = {
                        Operation.SCALE_TOP_LEFT, Operation.SCALE_TOP_RIGHT,
                        Operation.SCALE_BOTTOM_LEFT, Operation.SCALE_BOTTOM_RIGHT
                    };

                    for (int i = 0; i < corners.length; i++) {
                        if (corners[i] != null && screenPos.distance(corners[i]) < HANDLE_SIZE * 2) {
                            return cornerOps[i];
                        }
                    }

                    Vector2f[] edgeCenters = getGroupScreenEdgeCenters();
                    Operation[] edgeOps = {Operation.SCALE_Y, Operation.SCALE_X, Operation.SCALE_Y, Operation.SCALE_X};

                    for (int i = 0; i < edgeCenters.length; i++) {
                        if (edgeCenters[i] != null && screenPos.distance(edgeCenters[i]) < HANDLE_SIZE * 2) {
                            return edgeOps[i];
                        }
                    }
                }

                if (screenPos.distance(centerScreen) < HANDLE_SIZE * 2) {
                    return Operation.SCALE_ALL;
                }
                break;
        }

        return Operation.NONE;
    }

    public void draw(List<Shape> shapes, Tool currentTool) {
        if (shapes == null || shapes.isEmpty()) return;

        this.selectedShapes = shapes;

        ImDrawList drawList = ImGui.getForegroundDrawList();
        if (drawList == null) return;

        Vector2f centerScreen = camera.worldToScreen(getGroupCenter().x, getGroupCenter().y);

        switch (currentTool) {
            case TRANSLATE:
                drawTranslateGizmo(drawList, centerScreen);
                break;
            case ROTATE:
                drawRotateGizmo(drawList, centerScreen);
                break;
            case SCALE:
                drawScaleGizmo(drawList, centerScreen);
                break;
        }
    }

    private void drawTranslateGizmo(ImDrawList drawList, Vector2f centerScreen) {
        if (drawList == null) return;

        Vector2f xEndWorld = new Vector2f(getGroupCenter().x + ARROW_LENGTH, getGroupCenter().y);
        Vector2f xEndScreen = camera.worldToScreen(xEndWorld.x, xEndWorld.y);

        drawList.addLine(centerScreen.x, centerScreen.y, xEndScreen.x, xEndScreen.y,
            ImGui.getColorU32(1, 0, 0, 1), 3.0f);
        drawList.addCircleFilled(xEndScreen.x, xEndScreen.y, HANDLE_SIZE, ImGui.getColorU32(1, 0, 0, 1));

        Vector2f yEndWorld = new Vector2f(getGroupCenter().x, getGroupCenter().y + ARROW_LENGTH);
        Vector2f yEndScreen = camera.worldToScreen(yEndWorld.x, yEndWorld.y);

        drawList.addLine(centerScreen.x, centerScreen.y, yEndScreen.x, yEndScreen.y,
            ImGui.getColorU32(0, 1, 0, 1), 3.0f);
        drawList.addCircleFilled(yEndScreen.x, yEndScreen.y, HANDLE_SIZE, ImGui.getColorU32(0, 1, 0, 1));
    }

    private void drawRotateGizmo(ImDrawList drawList, Vector2f centerScreen) {
        if (drawList == null) return;

        drawList.addCircle(centerScreen.x, centerScreen.y, ROTATE_RADIUS,
            ImGui.getColorU32(0, 0, 1, 1), 0, 2.0f);

        float angle = getAverageRotation();
        float handleX = centerScreen.x + (float) Math.cos(angle) * ROTATE_RADIUS;
        float handleY = centerScreen.y + (float) Math.sin(angle) * ROTATE_RADIUS;

        drawList.addCircleFilled(handleX, handleY, HANDLE_SIZE, ImGui.getColorU32(0, 0, 1, 1));
        drawList.addLine(centerScreen.x, centerScreen.y, handleX, handleY,
            ImGui.getColorU32(0, 0, 1, 0.5f), 1.0f);
    }

    private void drawScaleGizmo(ImDrawList drawList, Vector2f centerScreen) {
        if (drawList == null) return;

        Vector2f[] corners = getGroupScreenCorners();
        if (corners == null) return;

        int white = ImGui.getColorU32(1, 1, 1, 1);
        drawList.addLine(corners[0].x, corners[0].y, corners[1].x, corners[1].y, white, 1.0f);
        drawList.addLine(corners[1].x, corners[1].y, corners[3].x, corners[3].y, white, 1.0f);
        drawList.addLine(corners[3].x, corners[3].y, corners[2].x, corners[2].y, white, 1.0f);
        drawList.addLine(corners[2].x, corners[2].y, corners[0].x, corners[0].y, white, 1.0f);

        int yellow = ImGui.getColorU32(1, 1, 0, 1);
        for (Vector2f corner : corners) {
            if (corner != null) {
                drawList.addCircleFilled(corner.x, corner.y, HANDLE_SIZE, yellow);
            }
        }

        int cyan = ImGui.getColorU32(0, 1, 1, 1);
        Vector2f[] edgeCenters = getGroupScreenEdgeCenters();
        for (Vector2f edge : edgeCenters) {
            if (edge != null) {
                drawList.addCircleFilled(edge.x, edge.y, HANDLE_SIZE, cyan);
            }
        }

        drawList.addCircleFilled(centerScreen.x, centerScreen.y, HANDLE_SIZE, ImGui.getColorU32(1, 1, 1, 1));
    }

    private Vector2f[] getWorldCorners(Shape shape) {
        if (shape == null || shape.mesh == null) return null;

        float[] vertices = shape.mesh.getVertices();
        if (vertices == null) return null;

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
            {minX, maxY},
            {maxX, maxY},
            {minX, minY},
            {maxX, minY}
        };

        for (int i = 0; i < 4; i++) {
            vec.set(localCorners[i][0], localCorners[i][1], 0, 1);
            model.transform(vec);
            corners[i] = new Vector2f(vec.x, vec.y);
        }

        return corners;
    }

    private Vector2f[] getGroupScreenCorners() {
        if (selectedShapes == null || selectedShapes.isEmpty()) {
            return null;
        }

        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;

        for (Shape shape : selectedShapes) {
            Vector2f[] corners = getWorldCorners(shape);
            if (corners != null) {
                for (Vector2f corner : corners) {
                    minX = Math.min(minX, corner.x);
                    maxX = Math.max(maxX, corner.x);
                    minY = Math.min(minY, corner.y);
                    maxY = Math.max(maxY, corner.y);
                }
            }
        }

        Vector2f[] worldCorners = new Vector2f[]{
            new Vector2f(minX, maxY),
            new Vector2f(maxX, maxY),
            new Vector2f(minX, minY),
            new Vector2f(maxX, minY)
        };

        Vector2f[] screenCorners = new Vector2f[4];
        for (int i = 0; i < 4; i++) {
            screenCorners[i] = camera.worldToScreen(worldCorners[i].x, worldCorners[i].y);
        }
        return screenCorners;
    }

    private Vector2f[] getGroupScreenEdgeCenters() {
        Vector2f[] corners = getGroupScreenCorners();
        if (corners == null) return new Vector2f[4];

        Vector2f[] edges = new Vector2f[4];

        edges[0] = new Vector2f((corners[0].x + corners[1].x) / 2, (corners[0].y + corners[1].y) / 2);
        edges[1] = new Vector2f((corners[1].x + corners[3].x) / 2, (corners[1].y + corners[3].y) / 2);
        edges[2] = new Vector2f((corners[3].x + corners[2].x) / 2, (corners[3].y + corners[2].y) / 2);
        edges[3] = new Vector2f((corners[2].x + corners[0].x) / 2, (corners[2].y + corners[0].y) / 2);

        return edges;
    }

    private float distanceToLineSegment(Vector2f point, Vector2f a, Vector2f b) {
        Vector2f ab = new Vector2f(b).sub(a);
        Vector2f ap = new Vector2f(point).sub(a);

        float t = ap.dot(ab) / ab.dot(ab);
        t = Math.max(0, Math.min(1, t));

        Vector2f projection = new Vector2f(a).add(new Vector2f(ab).mul(t));
        return point.distance(projection);
    }
}
