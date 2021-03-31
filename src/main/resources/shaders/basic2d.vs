#version 130

in vec3 position;
in vec2 textureCoordinates;
in float alpha;

out vec2 textureCoordinatesVariation;
out float _alpha;

void main(void) {
  textureCoordinatesVariation = textureCoordinates;
  _alpha = alpha;
  gl_Position = vec4(position, 1.0);
}
