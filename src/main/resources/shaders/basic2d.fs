#version 130

uniform sampler2D _texture;

in vec2 textureCoordinatesVariation;
in float _alpha;

out vec4 color;

void main(void) {
  color = texture(_texture, textureCoordinatesVariation);
  color.w *= _alpha;
}
