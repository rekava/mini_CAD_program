package org.example;

public class Triangle extends Shape {

    public Triangle(Shader shader) {
        super(null, shader); // временно null для mesh

        float[] vertices = {
                -0.5f, -0.5f, 0.0f,  // левая нижняя
                0.5f, -0.5f, 0.0f,   // правая нижняя
                0.0f, 0.5f, 0.0f     // верхняя
        };

        this.mesh = new Mesh(vertices);
        this.transform = new Transform();

        // Для теста можно временно изменить цвет через uniform, если шейдер поддерживает
    }
}