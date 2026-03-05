package org.example;
import org.joml.*;

public class Camera {
    public Vector2f position = new Vector2f(0, 0);
    public Matrix4f projection = new Matrix4f();
    public float zoom = 1.0f;

    private int viewportWidth;
    private int viewportHeight;

    public Camera(int width, int height) {
        this.viewportWidth = width;
        this.viewportHeight = height;
        updateProjection(width, height);
    }

    public void setViewportSize(int width, int height) {
        this.viewportWidth = width;
        this.viewportHeight = height;
        updateProjection(width, height);
    }

    public void updateProjection(int width, int height) {
        float aspect = (float) width / height;
        projection.identity();
        projection.ortho(
                -aspect * zoom,
                aspect * zoom,
                -1.0f * zoom,
                1.0f * zoom,
                -1.0f,
                1.0f
        );
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f().translate(-position.x, -position.y, 0);
    }

    public Vector2f screenToWorld(float screenX, float screenY) {
        float ndcX = (2.0f * screenX) / viewportWidth - 1.0f;
        float ndcY = 1.0f - (2.0f * screenY) / viewportHeight;
        float aspect = (float) viewportWidth / viewportHeight;
        float worldX = position.x + ndcX * zoom * aspect;
        float worldY = position.y + ndcY * zoom;
        return new Vector2f(worldX, worldY);
    }
}