package org.example;
import org.joml.*;

public class Shape extends SceneObject {
    public Mesh mesh;
    public Shader shader;
    public Vector3f color = new Vector3f(0.0f, 0.8f, 0.0f);
    public boolean isPreview = false; // для призраков
    public Shape(Mesh mesh, Shader shader) {
        this.mesh = mesh;
        this.shader = shader;
    }

    @Override
    public void render(Camera camera) {
        shader.use();

        Vector2f originalScale = new Vector2f(transform.scale);

        if (isSelected() && !isPreview) {
            transform.scale.set(originalScale.x * 1.02f, originalScale.y * 1.02f);

            shader.setUniformMat4("projection", camera.projection);
            shader.setUniformMat4("view", camera.getViewMatrix());
            shader.setUniformMat4("model", transform.getModelMatrix());
            shader.setUniformFloat("isOutline", 1.0f);
            shader.setUniformVec3("outlineColor", 1.0f, 0.8f, 0.0f);

            mesh.render();

            transform.scale.set(originalScale);
        }

        shader.setUniformMat4("projection", camera.projection);
        shader.setUniformMat4("view", camera.getViewMatrix());
        shader.setUniformMat4("model", transform.getModelMatrix());
        shader.setUniformFloat("isOutline", 0.0f);
        shader.setUniformVec3("fillColor", color.x, color.y, color.z);

        // Призраки полупрозрачные
        float alpha = isPreview ? 0.5f : 1.0f;
        shader.setUniformFloat("alpha", alpha);

        mesh.render();
    }

    @Override
    public boolean containsPoint(float worldX, float worldY) {
        float[] vertices = mesh.getVertices();
        if (vertices == null) return false;

        Matrix4f model = transform.getModelMatrix();
        Vector4f vec = new Vector4f();

        float minX = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;

        for (int i = 0; i < vertices.length; i += 3) {
            vec.set(vertices[i], vertices[i+1], vertices[i+2], 1.0f);
            model.transform(vec);
            if (vec.x < minX) minX = vec.x;
            if (vec.x > maxX) maxX = vec.x;
            if (vec.y < minY) minY = vec.y;
            if (vec.y > maxY) maxY = vec.y;
        }

        return worldX >= minX && worldX <= maxX && worldY >= minY && worldY <= maxY;
    }
    @Override
    public boolean intersectsRect(float minX, float maxX, float minY, float maxY) {
        float[] vertices = mesh.getVertices();
        if (vertices == null) return false;

        Matrix4f model = transform.getModelMatrix();
        Vector4f vec = new Vector4f();

        float objMinX = Float.MAX_VALUE;
        float objMaxX = -Float.MAX_VALUE;
        float objMinY = Float.MAX_VALUE;
        float objMaxY = -Float.MAX_VALUE;

        for (int i = 0; i < vertices.length; i += 3) {
            vec.set(vertices[i], vertices[i+1], vertices[i+2], 1.0f);
            model.transform(vec);
            if (vec.x < objMinX) objMinX = vec.x;
            if (vec.x > objMaxX) objMaxX = vec.x;
            if (vec.y < objMinY) objMinY = vec.y;
            if (vec.y > objMaxY) objMaxY = vec.y;
        }

        return !(objMaxX < minX || objMinX > maxX || objMaxY < minY || objMinY > maxY);
    }

    public void cleanup() {
        mesh.cleanup();
        shader.cleanup();
    }
    public void setColor(float r, float g, float b) {
        color.set(r, g, b);
    }

}
