package demo.webapp

/**
 * Created by markus on 11/06/15.
 */
object Shaders {

  val fragmentShader = """#ifdef GL_ES
#extension GL_OES_standard_derivatives : enable
  #endif

// uniform vec4 u_color;
uniform vec3 u_borderColor;
uniform float u_borderWidth;
uniform float u_highlightFlag;
uniform float u_faceHighlightFlag;

varying vec3 v_color;
varying float v_centerFlag;
varying float v_highlightFlag;
varying float v_faceHighlightFlag;

float edgeFactor(const float thickness, const float centerFlag)
{
  return smoothstep(0.0, fwidth(centerFlag)*thickness, centerFlag);
}

void main()
{
  vec3 col = v_color;
  float c = 0.5+0.5*cos(3.0*6.28*v_centerFlag);
  if (u_faceHighlightFlag == v_faceHighlightFlag) {
    col.r = c * col.r + (1.0-c);
    col.g = c * col.g + 0.2*(1.0-c);
    col.b = c * col.b;
  }
  else if ( u_highlightFlag == v_highlightFlag ) {
    col.r = c*col.r;
    col.g = c*col.g;
    col.b = c*col.b;
  }
  float f = edgeFactor(u_borderWidth, v_centerFlag);
  gl_FragColor = vec4 (
    mix(u_borderColor,
      col,
      f),
    1.0
  );
}"""

  val vertexShader = """#ifdef GL_ES
#extension GL_OES_standard_derivatives : enable
#endif

attribute float a_centerFlag;
attribute vec3 a_normal;
attribute vec3 a_color;
attribute float a_pickColor;

uniform mat4 u_mvpMat;
uniform float u_time;

varying vec3 v_color;
varying float v_centerFlag;
varying float v_highlightFlag;
varying float v_faceHighlightFlag;

void main()
{
  gl_Position = u_mvpMat * vec4(position, 1.0);
  v_color = a_color;
  v_centerFlag = a_centerFlag;
  v_faceHighlightFlag = a_pickColor;
  v_highlightFlag = mod(v_faceHighlightFlag, 131072.0);
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
