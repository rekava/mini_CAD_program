package org.example;

public class Rectangle extends Shape {

    public Rectangle(Shader shader) {
        super(null, shader);

        float[] vertices = {

                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
                0.5f,  0.5f, 0.0f,


                0.5f,  0.5f, 0.0f,
                -0.5f,  0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f
        };

        this.mesh = new Mesh(vertices);
        this.transform = new Transform();
        setColor(0.0f, 0.0f, 1.0f); // синий
    }
}