package demo.webapp

import scala.scalajs.js.annotation.JSExport

/**
 * Created by markus on 11/06/15.
 */
@JSExport
object Shaders {

  // ***************** Common *****************

  private val header_common: String =
    """#ifdef GL_ES
      |#extension GL_OES_standard_derivatives : enable
      |#endif
    """.stripMargin

  // ***************** projections common *****************

  private val header_vertex_projs: String =
    """uniform mat4 u_mMat;
      |
      |varying float v_hemi0;
      |varying float v_hemi1;
    """.stripMargin

  private val body_vertex_projs: String =
    """vec4 rotPos = u_mMat * vec4(position, 1.0);
      |float longi = atan( rotPos.x, rotPos.z );
      |if (longi > 0.0) {
      |  v_hemi0 = 1.0; // east quadrant
      |} else {
      |  v_hemi0 = 0.0; // west quadrant
      |}
      |if (abs(longi) > 1.57079632679) {
      |  v_hemi1 = 1.0; // back quadrant
      |} else {
      |  v_hemi1 = 0.0; // front quadrant
      |}
    """.stripMargin

  private val header_fragment_projs: String =
    """varying float v_hemi0;
      |varying float v_hemi1;
    """.stripMargin

  private val body_fragment_projs: String => String =
    """if (v_hemi1 == 1.0 && ( v_hemi0 != 0.0 && v_hemi0 != 1.0 ) ) {
      |  // back hemisphere AND crossing east and west hemispheres
      |  gl_FragColor = vec4(0.0,0.0,0.0,0.0);
      |} else {
      |  gl_FragColor = vec4 (@@@color@@@, 1.0);
      |}
    """.stripMargin.replace( "@@@color@@@", _ )

  // ***************** Cassini projection *****************

  private val body_vertex_cassini: String =
    """float x = asin( cos( lati ) * sin( longi ) ) / 1.57079632679;
      |float y = atan( tan( lati ), cos( longi ) ) / 3.14159265359;
      |gl_Position = vec4( x, y, 0.0, 1.0);
    """.stripMargin

  private val hemispheres_vertex_cassini: String =
    """vec4 rotPos = u_mMat * vec4(position, 1.0);
      |float lati = 1.57079632679 - acos( rotPos.y );
      |float longi = atan( rotPos.x, rotPos.z );
      |if (lati > 0.0) {
      |  v_hemi0 = 1.0; // north hemisphere
      |} else {
      |  v_hemi0 = 0.0; // south hemisphere
      |}
      |if (abs(longi) > 1.57079632679) {
      |  v_hemi1 = 1.0; // back quadrant
      |} else {
      |  v_hemi1 = 0.0; // front quadrant
      |}
    """.stripMargin

  // ***************** Winkel-Tripel projection *****************

  private val body_vertex_winkeltripel: String =
    """float lati = 1.57079632679 - acos( rotPos.y );
      |float cPhi1 = 0.96592582623; // cos( 15° )
      |float alpha = acos( cos( lati ) * cos( longi / 2.0 ) );
      |gl_Position = vec4( 0.5 * ( longi * cPhi1 + 2.0 * cos( lati ) * sin( longi/2.0 ) * alpha / sin( alpha ) ) / 3.14159265359, 0.5 * ( lati + sin( lati ) * alpha / sin( alpha ) ) / 1.57079632679, 0.0, 1.0);
    """.stripMargin

  // ***************** Equirectangular  projection *****************

  private val body_vertex_equirectangular: String =
    """float lati = acos( rotPos.y );
      |gl_Position = vec4( longi / 3.14159265359, 2.0 * ( -lati / 3.14159265359 + 0.5 ), 0.0, 1.0);
    """.stripMargin

  // ***************** Hammer-Aitoff projection *****************

  private val body_vertex_hammer_aitoff: String =
    """float lati = acos( rotPos.y );
      |float z = sqrt( 1.0 + sin( lati ) * cos( longi / 2.0 ) );
      |gl_Position = vec4( ( sin( lati ) * sin( longi / 2.0 ) ) / z, cos( lati ) / z, 0.0, 1.0);
    """.stripMargin

  // ***************** Spherical projection *****************

  private val body_vertex_spherical: String =
    "gl_Position = vec4( longi / 3.14159265359, log( ( 1.0+rotPos.y ) / ( 1.0-rotPos.y ) ) / 12.5663706144, 0.0, 1.0);"

  // ***************** programs *****************

  private def assembleShaderProgram( headers: List[String], bodyParts: List[String] ): String = {
    s"${ ( header_common :: headers ).mkString( "\n" ) }\nvoid main(){\n${ bodyParts.mkString( "\n" ) }\n}"
  }

  val cells_fragment_projs = assembleShaderProgram(
    List(
      """uniform vec3 u_borderColor;
        |uniform float u_borderWidth;
        |
        |varying vec3 v_color;
        |varying float v_centerFlag;
        |
        |float edgeFactor(const float thickness, const float centerFlag)
        |{
        |  return smoothstep(0.0, fwidth(centerFlag)*thickness, centerFlag);
        |}
      """.stripMargin,
      header_fragment_projs ),
    List( body_fragment_projs( "mix(u_borderColor, v_color, edgeFactor(u_borderWidth, v_centerFlag))" ) )
  )

  private def cells_vertex_projs( str: String, defaultHemisphere: String = body_vertex_projs ) = assembleShaderProgram(
    List(
      """attribute float a_centerFlag;
        |attribute vec3 a_color;
        |
        |varying vec3 v_color;
        |varying float v_centerFlag;
      """.stripMargin,
      header_vertex_projs
    ),
    List(
      defaultHemisphere,
      str,
      """v_color = a_color;
        |v_centerFlag = a_centerFlag;
      """.stripMargin
    )
  )

  val cells_vertex_spherical: String = cells_vertex_projs( body_vertex_spherical )

  val cells_vertex_hammer_aitoff: String = cells_vertex_projs( body_vertex_hammer_aitoff )

  val cells_vertex_equirectangular: String = cells_vertex_projs( body_vertex_equirectangular )

  val cells_vertex_winkeltripel: String = cells_vertex_projs( body_vertex_winkeltripel )

  val cells_vertex_cassini: String = cells_vertex_projs( body_vertex_cassini, hemispheres_vertex_cassini )

  val maze_fragment_projs = assembleShaderProgram(
    List(
      "uniform vec3 u_color;",
      header_fragment_projs ),
    List( body_fragment_projs( "u_color" ) )
  )

  private def maze_vertex_projs( str: String, defaultHemisphere: String = body_vertex_projs ): String = assembleShaderProgram(
    List(
      """attribute float a_centerFlag;
        |attribute vec3 a_color;
        |
        |varying vec3 v_color;
        |varying float v_centerFlag;
      """.stripMargin,
      header_vertex_projs
    ),
    List(
      defaultHemisphere,
      str
    )
  )

  val maze_vertex_spherical: String = maze_vertex_projs( body_vertex_spherical )

  val maze_vertex_hammer_aitoff: String = maze_vertex_projs( body_vertex_hammer_aitoff )

  val maze_vertex_equirectangular: String = maze_vertex_projs( body_vertex_equirectangular )

  val maze_vertex_winkeltripel: String = maze_vertex_projs( body_vertex_winkeltripel )

  val maze_vertex_cassini: String = maze_vertex_projs( body_vertex_cassini, hemispheres_vertex_cassini )
}
