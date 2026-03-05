package org.example;
import org.joml.*;

public class Shape extends SceneObject{

    public Mesh mesh;
    public Shader shader;

    Shape(Mesh mesh, Shader shader){
        this.mesh = mesh;
        this.shader = shader;
    }

    @Override
    void render(Camera camera){
        shader.use();
        // Внутри цикла отрисовки
        shader.setUniformMat4("projection", camera.projection);
        shader.setUniformMat4("view", camera.getViewMatrix());
        shader.setUniformMat4("model", transform.getModelMatrix());

        mesh.render();
    }

    public void clenap(){
        mesh.cleanup();
        shader.cleanup();
    }
}
