package org.example;
import org.joml.*;

public class Camera {
    public Vector2f position = new Vector2f(0, 0);
    public Matrix4f projection = new Matrix4f();
    public float zoom = 1.0f;

    public Camera(int width, int height) {

        float aspect = (float)width / height;
        projection.ortho(-aspect, aspect, -1.0f, 1.0f, -1.0f, 1.0f);
    }

    public Matrix4f getViewMatrix() {

        return new Matrix4f().translate(-position.x, -position.y, 0);
    }

    public void setAspect(int width, int height) {
        float aspect = (float) width / height;
        projection.identity();
        projection.ortho(-aspect, aspect, -1.0f, 1.0f, -1.0f, 1.0f);
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
}

