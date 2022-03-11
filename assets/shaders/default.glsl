#type vertex
#version 330 core

layout (location=0) in vec3 aPos;
layout (location=1) in vec4 aColor;
layout (location=2) in vec2 aTexCoords;
layout (location=3) in vec3 aNormal;

const int MAX_LIGHTS = 8;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;
uniform vec3 uLightPos[MAX_LIGHTS];
uniform vec3 uLightColor[MAX_LIGHTS];

out vec4 fColor;
out vec2 fTexCoords;
out vec3 fPos;
out vec3 fNormal;

void main() {
    fColor = aColor;
    fTexCoords = aTexCoords;
    fPos = vec3(uModel * vec4(aPos, 1.0));
    gl_Position = uProjection * uView * uModel * vec4(aPos, 1.0);
}

#type fragment
#version 330 core

uniform sampler2D texSampler;

in int MAX_LIGHTS;
in vec4 fColor;
in vec2 fTexCoords;
in vec3 fPos;
in vec3 fNormal;
in vec3 fLightPos[MAX_LIGHTS];
in vec3 fLightColor[MAX_LIGHTS];

out vec4 color;

void main() {
    vec3 normal = normalize(fNormal);
    color = texture(texSampler, fTexCoords);
}