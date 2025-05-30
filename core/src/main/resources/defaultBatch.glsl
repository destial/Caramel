#type vertex
#version 330 core

layout (location=0) in vec3 aPos;
layout (location=1) in vec4 aColor;
layout (location=2) in vec2 aTexCoords;
layout (location=3) in vec3 aNormal;
layout (location=4) in float aTexSlot;

out vec4 fColor;
out vec2 fTexCoords;
out vec3 fNormal;
out float fTexSlot;

void main() {
    fColor = aColor;
    fTexCoords = aTexCoords;
    fTexSlot = aTexSlot;
    gl_Position = vec4(aPos, 1.0);
}

#type fragment
#version 330 core

uniform sampler2D[8] texSampler;

in vec4 fColor;
in vec2 fTexCoords;
in vec3 fNormal;
in float fTexSlot;

out vec4 FragColor;

void main() {
    int id = int(fTexSlot);
    if (id < 0) {
        FragColor = fColor;
    } else {
        vec4 tex = texture(texSampler[id], fTexCoords);
        if (tex.a < 0.1) {
            discard;
        }
        FragColor = tex * fColor;
    }
}