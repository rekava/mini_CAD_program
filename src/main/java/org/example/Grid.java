package org.example;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import static org.lwjgl.opengl.GL33.*;

public class Grid {
    private boolean enabled = true;
    private float size = 10.0f;
    private float step = 1.0f;
    private float[] color = {0.3f, 0.3f, 0.3f};
    private int vao;
    private int vbo;
    private int vertexCount;

    public Grid() {
        generateGrid();
    }

    private void generateGrid() {
        // Количество линий
        int linesPerSide = (int)(size / step);
        // Каждая линия - 2 вершины, всего линий: (linesPerSide * 2 + 2) по X и по Y
        vertexCount = (linesPerSide * 2 + 2) * 4; // *2 для X и Y, *2 для двух вершин на линию

        float[] vertices = new float[vertexCount * 3];
        int idx = 0;

        // Вертикальные линии (X = const)
        for (float x = -size; x <= size + 0.001f; x += step) {
            vertices[idx++] = x;
            vertices[idx++] = -size;
            vertices[idx++] = 0;

            vertices[idx++] = x;
            vertices[idx++] = size;
            vertices[idx++] = 0;
        }

        // Горизонтальные линии (Y = const)
        for (float y = -size; y <= size + 0.001f; y += step) {
            vertices[idx++] = -size;
            vertices[idx++] = y;
            vertices[idx++] = 0;

            vertices[idx++] = size;
            vertices[idx++] = y;
            vertices[idx++] = 0;
        }

        // Создаем VAO и VBO
        vao = glGenVertexArrays();
        vbo = glGenBuffers();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void render(Camera camera, Shader shader) {
        if (!enabled) return;

        shader.use();

        // Для сетки нужен специальный шейдер или используем существующий
        shader.setUniformMat4("projection", camera.projection);
        shader.setUniformMat4("view", camera.getViewMatrix());
        shader.setUniformMat4("model", new Matrix4f());
        shader.setUniformFloat("isOutline", 0.0f);
        shader.setUniformVec3("fillColor", color[0], color[1], color[2]);
        shader.setUniformFloat("alpha", 0.5f);

        glBindVertexArray(vao);
        glDrawArrays(GL_LINES, 0, vertexCount);
        glBindVertexArray(0);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
        generateGrid();
    }

    public float getStep() {
        return step;
    }

    public void setStep(float step) {
        this.step = step;
        generateGrid();
    }

    public float[] getColor() {
        return color;
    }

    public void setColor(float r, float g, float b) {
        this.color[0] = r;
        this.color[1] = g;
        this.color[2] = b;
    }

    public void cleanup() {
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
    }
}
