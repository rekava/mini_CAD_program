package org.example;
import org.joml.*;

public class Shape extends SceneObject {
    public Mesh mesh;
    public Shader shader;

    public Shape(Mesh mesh, Shader shader) {
        this.mesh = mesh;
        this.shader = shader;
    }

    @Override
    public void render(Camera camera) {
        shader.use();
        shader.setUniformMat4("projection", camera.projection);
        shader.setUniformMat4("view", camera.getViewMatrix());
        shader.setUniformMat4("model", transform.getModelMatrix());
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

    public void cleanup() {
        mesh.cleanup();
        shader.cleanup();
    }
}