#version 330 core
out vec4 FragColor;

uniform float isOutline;
uniform vec3 fillColor;
uniform vec3 outlineColor;

void main() {
    if (isOutline > 0.5) {
        FragColor = vec4(outlineColor, 1.0);
    } else {
        FragColor = vec4(fillColor, 1.0);
    }
}