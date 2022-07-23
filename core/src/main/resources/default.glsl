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
out vec3 fNormal;
out float fTexSlot;

void main() {
    fColor = aColor;
    fTexCoords = aTexCoords;
    fTexSlot = aTexSlot;
    gl_Position = uMVP * vec4(aPos, 1.0);
}

#type fragment
#version 330 core

uniform sampler2D texSampler;

in vec4 fColor;
in vec2 fTexCoords;
in vec3 fNormal;
in float fTexSlot;

out vec4 color;

void main() {
    vec4 tex = texture(texSampler, fTexCoords);
    if (tex.a < 0.1) {
        discard;
    }
    color = tex * fColor;
}