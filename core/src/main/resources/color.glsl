#type vertex
#version 330 core

layout (location=0) in vec3 aPos;
layout (location=1) in vec4 aColor;
layout (location=2) in vec2 aTexCoords;
layout (location=3) in vec3 aNormal;
layout (location=4) in float aTexSlot;

uniform mat4 uMVP;

out vec4 fColor;
out vec2 fTexCoords;

void main() {
    fColor = aColor;
    gl_Position = uMVP * vec4(aPos, 1.0);
}

#type fragment
#version 330 core

in vec4 fColor;

out vec4 color;

void main() {
    color = fColor;
}