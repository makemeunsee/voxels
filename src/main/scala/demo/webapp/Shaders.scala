package demo.webapp

/**
 * Created by markus on 11/06/15.
 */
object Shaders {

  val fragmentShader = """#ifdef GL_ES
#extension GL_OES_standard_derivatives : enable
  #endif

uniform vec4 u_color;
uniform vec4 u_borderColor;
uniform float u_borderWidth;
uniform float u_highlightFlag;

varying float v_centerFlag;
varying float v_highlightFlag;

float edgeFactor(const float thickness, const float centerFlag)
{
  return smoothstep(0.0, fwidth(centerFlag)*thickness, centerFlag);
}

void main()
{
  vec4 col = u_color;
  if (u_highlightFlag == v_highlightFlag) {
    col.r = 0.2+2.0*col.r;
    col.g = 0.5*col.g;
    col.b = 0.5*col.b;
  }
  float f = edgeFactor(u_borderWidth, v_centerFlag);
  gl_FragColor = vec4 (
    mix(u_borderColor,
      col,
      f).rgb,
    1.0
  );
}"""

  val vertexShader = """#ifdef GL_ES
#extension GL_OES_standard_derivatives : enable
#endif

attribute float a_centerFlag;
attribute vec3 a_normal;
attribute float a_pickColor;

uniform mat4 u_mvpMat;
uniform float u_time;

varying float v_centerFlag;
varying float v_highlightFlag;

void main()
{
  gl_Position = u_mvpMat * vec4(position, 1.0);
  v_centerFlag = a_centerFlag;
  v_highlightFlag = a_pickColor;
}"""

  val pickFragmentShader = """#ifdef GL_ES
#extension GL_OES_standard_derivatives : enable
#endif

varying vec3 v_color;

void main()
{
  gl_FragColor = vec4 ( v_color, 1.0 );
}"""

  val pickVertexShader = """#ifdef GL_ES
#extension GL_OES_standard_derivatives : enable
#endif

attribute vec3 a_normal;
attribute float a_pickColor;

uniform mat4 u_mvpMat;
uniform float u_time;

varying vec3 v_color;

vec3 unpackColor(float f) {
  vec3 color;
  color.r = floor(f / 256.0 / 256.0);
  color.g = floor((f - color.r * 256.0 * 256.0) / 256.0);
  color.b = floor(f - color.r * 256.0 * 256.0 - color.g * 256.0);
  // now we have a vec3 with the 3 components in range [0..255]. Let's normalize it!
  return color / 255.0;
}

void main()
{
  gl_Position = u_mvpMat * vec4(position, 1.0);
  v_color = unpackColor(a_pickColor);
  // v_color = a_pickColor;
}"""
}
