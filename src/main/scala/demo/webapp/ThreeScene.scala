package demo.webapp

import geometry.Matrix4
import geometry.voronoi.{Face, VoronoiModel}
import maze.Maze
import org.denigma.threejs._

import scala.scalajs.js
import scala.scalajs.js.Array
import scala.scalajs.js.annotation.{JSExport, JSName}
import scala.scalajs.js.typedarray.{Float32Array, Uint32Array, Uint8Array}

/**
 * Created by markus on 17/06/2015.
 */
object ThreeScene {

  private val textureW = 256
  private val textureH = 128

  @JSName( "THREE.PlaneBufferGeometry" )
  class PlaneBufferGeometry( width: Float, height: Float ) extends Geometry

  // BufferGeometry from org.denigma.threejs does not extends Geometry, has to be redefined
  @JSName( "THREE.BufferGeometry" )
  class MyBufferGeometry extends Geometry {
    override def clone(): MyBufferGeometry = js.native
    var attributes: js.Array[BufferAttribute] = js.native
    var drawcalls: js.Any = js.native
    var offsets: js.Any = js.native
    def addAttribute( name: String, attribute: BufferAttribute ): js.Dynamic = js.native
    def addAttribute( name: String, array: js.Any, itemSize: Double ): js.Dynamic = js.native
    def getAttribute( name: String ): js.Dynamic = js.native
    def addDrawCall( start: Double, count: Double, index: Double ): Unit = js.native
    def applyMatrix( matrix: Matrix4 ): Unit = js.native
    def fromGeometry( geometry: Geometry, settings: js.Any = js.native ): BufferGeometry = js.native
    def computeVertexNormals(): Unit = js.native
    def computeOffsets( indexBufferSize: Double ): Unit = js.native
    def merge(): Unit = js.native
    def normalizeNormals(): Unit = js.native
    def reorderBuffers( indexBuffer: Double, indexMap: js.Array[Double], vertexCount: Double ): Unit = js.native
  }

  object ReadableWebGLRendererParameters extends WebGLRendererParameters {
    preserveDrawingBuffer = false
  }

  // zip faces of a voxel with their offset and size as in the meshes data buffers
  def withOffsetsAndSizes( faces: Iterable[Face] ): Seq[( Face, Int, Int )] = {
    faces.foldLeft( ( 0, Seq.empty[( Face, Int, Int )] ) ) { case ( ( offset, acc ), f ) =>
      // counting the centers which is automatically added to the meshes
      val l = 2 * ( f.vertices.length + 1 )
      val newOffset = offset + l
      ( newOffset, ( f, offset, l ) +: acc )
    }._2.reverse
  }

  def colorCode( faceId: Int ) = faceId +1

  def revertColorCode( colorCode: Int ): Int = {
    colorCode -1
  }

  import scala.language.implicitConversions
  implicit def toJsMatrix( m: Matrix4 ): org.denigma.threejs.Matrix4 = {
    val r = new org.denigma.threejs.Matrix4()
    r.set( m.a00, m.a01, m.a02, m.a03
         , m.a10, m.a11, m.a12, m.a13
         , m.a20, m.a21, m.a22, m.a23
         , m.a30, m.a31, m.a32, m.a33
    )
    r
  }

  // ******************** mesh building functions ********************

  private def makeWireFrameMesh( m: VoronoiModel ): ( Mesh, Mesh ) = {
    val customUniforms = js.Dynamic.literal(
      "u_time" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 ),
      "u_borderWidth" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 ),
      "u_mvpMat" -> js.Dynamic.literal( "type" -> "m4", "value" -> new org.denigma.threejs.Matrix4() ),
      "u_borderColor" -> js.Dynamic.literal( "type" -> "v3", "value" -> new Vector4( 0, 0, 0, 1 ) ),
      "u_faceHighlightFlag" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 ),
      "u_explosionFactor" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 ),
      "u_depthScale" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 )
    )

    val attrs = js.Dynamic.literal(
      "a_color" -> js.Dynamic.literal(
        "type" -> "v3",
        "value" -> new Array[Float]
      ),
      "a_normal" -> js.Dynamic.literal(
        "type" -> "v3",
        "value" -> new Array[Float]
      ),
      "a_pickColor" -> js.Dynamic.literal(
        "type" -> "f",
        "value" -> new Array[Float]
      ),
      "a_centerFlag" -> js.Dynamic.literal(
        "type" -> "f",
        "value" -> new Array[Float]
      )
    )

    val shaderMaterial = new ShaderMaterial
    shaderMaterial.attributes = attrs
    shaderMaterial.uniforms = customUniforms
    shaderMaterial.vertexShader = Shaders.vertexShader
    shaderMaterial.fragmentShader = Shaders.fragmentShader
    shaderMaterial.wireframe = true
    // useless until acceptance and release of https://github.com/mrdoob/three.js/pull/6778
    //    shaderMaterial.asInstanceOf[js.Dynamic].updateDynamic( "wireframeLinewidth" )( 3f )

    val pickShaderMaterial = new ShaderMaterial
    pickShaderMaterial.attributes = attrs
    pickShaderMaterial.uniforms = customUniforms
    pickShaderMaterial.vertexShader = Shaders.pickVertexShader
    pickShaderMaterial.fragmentShader = Shaders.pickFragmentShader

    val count = m.faces.map( _.vertices.length*3 ).sum
    val indicesCount = m.faces.map( _.vertices.length*2 ).sum
    val pickIndicesCount = m.faces.map( f => ( f.vertices.length - 2 )*3 ).sum

    val vertices = new Float32Array( count )
    val normals = new Float32Array( count )
    val colors = new Float32Array( count )
    val pickColors = new Float32Array( count / 3 )
    val centerFlags = new Float32Array( count / 3 )
    val indices = new Uint32Array( indicesCount )
    val pickIndices = new Uint32Array( pickIndicesCount )
    var offset = 0
    var indicesOffset = 0
    var pickIndicesOffset = 0

    for ( i <- m.faces.indices ; f = m.faces( i ) ) {
      val n = f.seed
      val pickColor = colorCode( i )
      val ( r, g, b ) = ( 1f, 1f, 1f )

      val triOffset = offset*3
      val vSize = f.vertices.length

      for ( j <- 0 until vSize ) {
        vertices.set( triOffset+3*j,   f.vertices( j ).x.toFloat )
        vertices.set( triOffset+3*j+1, f.vertices( j ).y.toFloat )
        vertices.set( triOffset+3*j+2, f.vertices( j ).z.toFloat )
        normals.set( triOffset+3*j,   n.x.toFloat )
        normals.set( triOffset+3*j+1, n.y.toFloat )
        normals.set( triOffset+3*j+2, n.z.toFloat )
        colors.set( triOffset+3*j,   r )
        colors.set( triOffset+3*j+1, g )
        colors.set( triOffset+3*j+2, b )
        centerFlags.set( offset+j, 0f )
        pickColors.set( offset+j, pickColor )
        indices.set( indicesOffset+2*j,   offset+j )
        indices.set( indicesOffset+2*j+1, offset+( j+1 )%vSize )
        if ( j < vSize-1 ) {
          pickIndices.set( pickIndicesOffset + 3 * j, offset )
          pickIndices.set( pickIndicesOffset + 3 * j + 1, offset + j + 1 )
          pickIndices.set( pickIndicesOffset + 3 * j + 2, offset + j + 2 )
        }
      }

      offset = offset + vSize
      indicesOffset = indicesOffset + vSize*2
      pickIndicesOffset = pickIndicesOffset + ( vSize - 2 )*3
    }

    val geom = new MyBufferGeometry()
    geom.addAttribute( "index", new BufferAttribute( indices, 1 ) )
    geom.addAttribute( "a_centerFlag", new BufferAttribute( centerFlags, 1 ) )
    geom.addAttribute( "a_normal", new BufferAttribute( normals, 3 ) )
    geom.addAttribute( "a_color", new BufferAttribute( colors, 3 ) )
    geom.addAttribute( "a_pickColor", new BufferAttribute( pickColors, 1 ) )
    geom.addAttribute( "position", new BufferAttribute( vertices, 3 ) )

    val pickGeom = geom.clone()
    pickGeom.addAttribute( "index", new BufferAttribute( pickIndices, 1 ) )

    ( assembleMesh( geom, shaderMaterial, "baseMesh" ), assembleMesh( pickGeom, pickShaderMaterial, "pickMesh" ) )
  }

  private def makeMesh( m: VoronoiModel, depthsMap: Map[Int, Int], depthMax: Int ): ( Mesh, Mesh ) = {
    val customUniforms = js.Dynamic.literal(
      "u_time" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 ),
      "u_borderWidth" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 ),
      "u_mvpMat" -> js.Dynamic.literal( "type" -> "m4", "value" -> new org.denigma.threejs.Matrix4() ),
      "u_borderColor" -> js.Dynamic.literal( "type" -> "v3", "value" -> new Vector4( 0, 0, 0, 1 ) ),
      "u_faceHighlightFlag" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 ),
      "u_explosionFactor" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 ),
      "u_depthScale" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 ),
      "u_thickness" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 )
    )

    val geom = new MyBufferGeometry()

    val attrs = js.Dynamic.literal(
      "a_color" -> js.Dynamic.literal(
        "type" -> "v3",
        "value" -> new Array[Float]
      ),
      "a_normal" -> js.Dynamic.literal(
        "type" -> "v3",
        "value" -> new Array[Float]
      ),
      "a_pickColor" -> js.Dynamic.literal(
        "type" -> "f",
        "value" -> new Array[Float]
      ),
      "a_centerFlag" -> js.Dynamic.literal(
        "type" -> "f",
        "value" -> new Array[Float]
      ),
      "a_depth" -> js.Dynamic.literal(
        "type" -> "f",
        "value" -> new Array[Float]
      ),
      "a_topFace" -> js.Dynamic.literal(
        "type" -> "f",
        "value" -> new Array[Float]
      )
    )

    val shaderMaterial = new ShaderMaterial
    shaderMaterial.attributes = attrs
    shaderMaterial.uniforms = customUniforms
    shaderMaterial.vertexShader = Shaders.vertexShader
    shaderMaterial.fragmentShader = Shaders.fragmentShader

    val pickShaderMaterial = new ShaderMaterial
    pickShaderMaterial.attributes = attrs
    pickShaderMaterial.uniforms = customUniforms
    pickShaderMaterial.vertexShader = Shaders.pickVertexShader
    pickShaderMaterial.fragmentShader = Shaders.pickFragmentShader

    val count = 2*m.faces.map( _.vertices.length*3+3 ).sum
    val indicesCount = m.faces.map( _.vertices.length*12 ).sum

    val vertices = new Float32Array( count )
    val normals = new Float32Array( count )
    val colors = new Float32Array( count )
    val pickColors = new Float32Array( count / 3 )
    val centerFlags = new Float32Array( count / 3 )
    val depths = new Float32Array( count / 3 )
    val topFlags = new Float32Array( count / 3 )
    val indices = new Uint32Array( indicesCount )
    var offset = 0
    var indicesOffset = 0

    for ( i <- m.faces.indices ; f = m.faces( i ) ) {
      val n = f.seed
      val uniformD = depthsMap( i ).toFloat / depthMax

      val pickColor = colorCode( i )
      val ( r, g, b ) = ( 1f, 1f, 1f )
      val ( cr, cg, cb ) = ( r, g, b )

      val triOffset = offset*3
      val vSize = f.vertices.length

      // edge vertices

      for ( j <- 0 until vSize ) {
        vertices.set( triOffset+3*j,   f.vertices( j ).x.toFloat )
        vertices.set( triOffset+3*j+1, f.vertices( j ).y.toFloat )
        vertices.set( triOffset+3*j+2, f.vertices( j ).z.toFloat )
        vertices.set( triOffset+3*(j+vSize),   f.vertices( j ).x.toFloat )
        vertices.set( triOffset+3*(j+vSize)+1, f.vertices( j ).y.toFloat )
        vertices.set( triOffset+3*(j+vSize)+2, f.vertices( j ).z.toFloat )
        normals.set( triOffset+3*j,   n.x.toFloat )
        normals.set( triOffset+3*j+1, n.y.toFloat )
        normals.set( triOffset+3*j+2, n.z.toFloat )
        normals.set( triOffset+3*(j+vSize),   n.x.toFloat )
        normals.set( triOffset+3*(j+vSize)+1, n.y.toFloat )
        normals.set( triOffset+3*(j+vSize)+2, n.z.toFloat )
        colors.set( triOffset+3*j,   r )
        colors.set( triOffset+3*j+1, g )
        colors.set( triOffset+3*j+2, b )
        colors.set( triOffset+3*(j+vSize),   r )
        colors.set( triOffset+3*(j+vSize)+1, g )
        colors.set( triOffset+3*(j+vSize)+2, b )
        centerFlags.set( offset+j,   0f )
        centerFlags.set( offset+j+vSize, 0f )
        depths.set( offset+j,       uniformD )
        depths.set( offset+j+vSize, uniformD )
        pickColors.set( offset+j,       pickColor )
        pickColors.set( offset+j+vSize, pickColor )
        topFlags.set( offset+j,       1f )
        topFlags.set( offset+j+vSize, 0f )

        // top triangle
        indices.set( indicesOffset+12*j,   offset+2*vSize )
        indices.set( indicesOffset+12*j+1, offset+j )
        indices.set( indicesOffset+12*j+2, offset+( j+1 )%vSize )

        // bottom triangle
        indices.set( indicesOffset+12*j+3, offset+2*vSize+1 )
        indices.set( indicesOffset+12*j+4, offset+vSize+( j+1 )%vSize )
        indices.set( indicesOffset+12*j+5, offset+vSize+j )

        // side triangle 1
        indices.set( indicesOffset+12*j+6, offset+j )
        indices.set( indicesOffset+12*j+7, offset+vSize+j )
        indices.set( indicesOffset+12*j+8, offset+( j+1 )%vSize )

        // side triangle 2
        indices.set( indicesOffset+12*j+9,  offset+( j+1 )%vSize )
        indices.set( indicesOffset+12*j+10, offset+vSize+j )
        indices.set( indicesOffset+12*j+11, offset+vSize+( j+1 )%vSize )
      }

      val c = f.barycenter

      // centers, one per face
      vertices.set( triOffset+6*vSize,   c.x.toFloat )
      vertices.set( triOffset+6*vSize+1, c.y.toFloat )
      vertices.set( triOffset+6*vSize+2, c.z.toFloat )
      vertices.set( triOffset+6*vSize+3, c.x.toFloat )
      vertices.set( triOffset+6*vSize+4, c.y.toFloat )
      vertices.set( triOffset+6*vSize+5, c.z.toFloat )
      normals.set( triOffset+6*vSize,   n.x.toFloat )
      normals.set( triOffset+6*vSize+1, n.y.toFloat )
      normals.set( triOffset+6*vSize+2, n.z.toFloat )
      normals.set( triOffset+6*vSize+3, n.x.toFloat )
      normals.set( triOffset+6*vSize+4, n.y.toFloat )
      normals.set( triOffset+6*vSize+5, n.z.toFloat )
      colors.set( triOffset+6*vSize,   cr )
      colors.set( triOffset+6*vSize+1, cg )
      colors.set( triOffset+6*vSize+2, cb )
      colors.set( triOffset+6*vSize+3, cr )
      colors.set( triOffset+6*vSize+4, cg )
      colors.set( triOffset+6*vSize+5, cb )
      centerFlags.set( offset+2*vSize,   1f )
      centerFlags.set( offset+2*vSize+1, 1f )
      depths.set( offset+2*vSize,   uniformD )
      depths.set( offset+2*vSize+1, uniformD )
      pickColors.set( offset+2*vSize,   pickColor )
      pickColors.set( offset+2*vSize+1, pickColor )
      topFlags.set( offset+2*vSize,   1f )
      topFlags.set( offset+2*vSize+1, 0f )

      offset = offset + 2 * ( vSize + 1 )
      indicesOffset = indicesOffset + vSize*12
    }

    geom.addAttribute( "index", new BufferAttribute( indices, 1 ) )
    geom.addAttribute( "a_centerFlag", new BufferAttribute( centerFlags, 1 ) )
    geom.addAttribute( "a_depth", new BufferAttribute( depths, 1 ) )
    geom.addAttribute( "a_normal", new BufferAttribute( normals, 3 ) )
    geom.addAttribute( "a_color", new BufferAttribute( colors, 3 ) )
    geom.addAttribute( "a_pickColor", new BufferAttribute( pickColors, 1 ) )
    geom.addAttribute( "a_topFace", new BufferAttribute( topFlags, 1 ) )
    geom.addAttribute( "position", new BufferAttribute( vertices, 3 ) )

    ( assembleMesh( geom, shaderMaterial, "baseMesh" ), assembleMesh( geom, pickShaderMaterial, "pickMesh" ) )
  }

  private def junction( face0: Face, face1: Face ): geometry.Vector3 = {
    face0.vertices.intersect( face1.vertices ) match {
      case v0 :: v1 :: Nil =>
        ( v0 + v1 ) * 0.5
      case _ =>
        throw new Error( "Illegal argument" )
    }
  }

  private def makeMazeMesh( m: VoronoiModel, flatMaze: Map[( Int, Int ), Set[( Int, Int )]], depthMax: Int ): Mesh = {
    val customUniforms = js.Dynamic.literal(
      "u_time" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 ),
      "u_mvpMat" -> js.Dynamic.literal( "type" -> "m4", "value" -> new org.denigma.threejs.Matrix4() ),
      "u_explosionFactor" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 ),
      "u_depthScale" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 ),
      "u_color" -> js.Dynamic.literal( "type" -> "v3", "value" -> new Vector3() )
    )

    val geom = new MyBufferGeometry()

    val attrs = js.Dynamic.literal(
      "a_normal" -> js.Dynamic.literal(
        "type" -> "v3",
        "value" -> new Array[Float]
      ),
      "a_depth0" -> js.Dynamic.literal(
        "type" -> "f",
        "value" -> new Array[Float]
      ),
      "a_depth1" -> js.Dynamic.literal(
        "type" -> "f",
        "value" -> new Array[Float]
      )
    )

    val shaderMaterial = new ShaderMaterial
    shaderMaterial.attributes = attrs
    shaderMaterial.uniforms = customUniforms
    shaderMaterial.vertexShader = Shaders.mazeVertexShader
    shaderMaterial.fragmentShader = Shaders.mazeFragmentShader
    shaderMaterial.wireframe = true
    // useless until acceptance and release of https://github.com/mrdoob/three.js/pull/6778
    //    shaderMaterial.asInstanceOf[js.Dynamic].updateDynamic( "wireframeLinewidth" )( 3f )

    val size = flatMaze.size
    // each maze node has 1 parent, except the root of the maze
    val relations = size - 1
    // there are as many vertices as node, plus 1 for each relation (the junction point)
    val count = 3*( relations + size )
    // there 2 segments = 4 indices for each relation
    val indicesCount = 4 * relations

    val id: ( () => Int ) = {
      var counter = 0
      () => {
        counter = counter+1
        counter-1
      }
    }

    // assign an indice to each node and each relation
    val mazeWithIndices = flatMaze.map { case ( p, children ) =>
      ( p._1, ( p._2, id(), children.map { case child =>
        ( child._1, child._2, id() )
      } ) )
    }

    val vertices = new Float32Array( count )
    val normals = new Float32Array( count )
    val depths0 = new Float32Array( count / 3 )
    val depths1 = new Float32Array( count / 3 )
    val indices = new Uint32Array( indicesCount )

    var indicesOffset = 0

    for ( ( node, ( d, id, children ) ) <- mazeWithIndices ) {
      val f = m.faces( node )
      val n = f.seed
      val uniformD = d.toFloat / depthMax

      // face center point
      val bary = f.barycenter * 1.001
      vertices.set( id*3,   bary.x.toFloat )
      vertices.set( id*3+1, bary.y.toFloat )
      vertices.set( id*3+2, bary.z.toFloat )

      normals.set( id*3,   n.x.toFloat )
      normals.set( id*3+1, n.y.toFloat )
      normals.set( id*3+2, n.z.toFloat )

      depths0.set( id, uniformD )
      depths1.set( id, uniformD )

      for( ( child, childD, relationId ) <- children ) {
        val ( _, childId, _ ) = mazeWithIndices( child )

        // junction point
        val j = junction( f, m.faces( child ) ) * 1.001
        vertices.set( relationId*3,   j.x.toFloat )
        vertices.set( relationId*3+1, j.y.toFloat )
        vertices.set( relationId*3+2, j.z.toFloat )

        normals.set( relationId*3,   n.x.toFloat )
        normals.set( relationId*3+1, n.y.toFloat )
        normals.set( relationId*3+2, n.z.toFloat )

        depths0.set( relationId, uniformD )
        depths1.set( relationId, childD.toFloat / depthMax )

        indices.set( indicesOffset, id )
        indices.set( indicesOffset+1, relationId )
        indices.set( indicesOffset+2, relationId )
        indices.set( indicesOffset+3, childId )

        indicesOffset += 4
      }
    }

    geom.addAttribute( "index", new BufferAttribute( indices, 1 ) )
    geom.addAttribute( "a_depth0", new BufferAttribute( depths0, 1 ) )
    geom.addAttribute( "a_depth1", new BufferAttribute( depths1, 1 ) )
    geom.addAttribute( "a_normal", new BufferAttribute( normals, 3 ) )
    geom.addAttribute( "position", new BufferAttribute( vertices, 3 ) )

    assembleMesh( geom, shaderMaterial, "mazeMesh" )
  }

  private def makeAxisMesh: Mesh = {
    val customUniforms = js.Dynamic.literal(
      "u_mvpMat" -> js.Dynamic.literal( "type" -> "m4", "value" -> new org.denigma.threejs.Matrix4() )
    )

    val geom = new MyBufferGeometry()

    val attrs = js.Dynamic.literal(
      "a_color" -> js.Dynamic.literal(
        "type" -> "v3",
        "value" -> new Array[Float]
      )
    )

    val shaderMaterial = new ShaderMaterial
    shaderMaterial.attributes = attrs
    shaderMaterial.uniforms = customUniforms
    shaderMaterial.vertexShader = Shaders.axisVertexShader
    shaderMaterial.fragmentShader = Shaders.axisFragmentShader
    shaderMaterial.wireframe = true

    val count = 18
    val indicesCount = 9

    val vertices = new Float32Array( count )
    val colors = new Float32Array( count )
    val indices = new Uint32Array( indicesCount )

    // 0
    vertices.set( 0, 0f )
    vertices.set( 1, 0f )
    vertices.set( 2, 0f )
    // 1
    vertices.set( 3, 3f )
    vertices.set( 4, 0f )
    vertices.set( 5, 0f )
    // 2
    vertices.set( 6, 0f )
    vertices.set( 7, 0f )
    vertices.set( 8, 0f )
    // 3
    vertices.set( 9, 0f )
    vertices.set( 10, 3f )
    vertices.set( 11, 0f )
    // 4
    vertices.set( 12, 0f )
    vertices.set( 13, 0f )
    vertices.set( 14, 3f )
    // 5
    vertices.set( 15, 0f )
    vertices.set( 16, 0f )
    vertices.set( 17, 0f )

    // 0
    colors.set( 0, 1f )
    colors.set( 1, 0f )
    colors.set( 2, 0f )
    // 1
    colors.set( 3, 1f )
    colors.set( 4, 0f )
    colors.set( 5, 0f )
    // 2
    colors.set( 6, 0f )
    colors.set( 7, 1f )
    colors.set( 8, 0f )
    // 3
    colors.set( 9, 0f )
    colors.set( 10, 1f )
    colors.set( 11, 0f )
    // 4
    colors.set( 12, 0f )
    colors.set( 13, 0f )
    colors.set( 14, 1f )
    // 5
    colors.set( 15, 0f )
    colors.set( 16, 0f )
    colors.set( 17, 1f )

    indices.set( 0, 0 )
    indices.set( 1, 1 )
    indices.set( 2, 0 )
    indices.set( 3, 2 )
    indices.set( 4, 3 )
    indices.set( 5, 2 )
    indices.set( 6, 4 )
    indices.set( 7, 5 )
    indices.set( 8, 4 )

    geom.addAttribute( "index", new BufferAttribute( indices, 1 ) )
    geom.addAttribute( "a_color", new BufferAttribute( colors, 3 ) )
    geom.addAttribute( "position", new BufferAttribute( vertices, 3 ) )

    assembleMesh( geom, shaderMaterial, "axisMesh" )
  }

  private def assembleMesh( geometry: Geometry, material: Material, name: String ): Mesh = {
    val mesh = new Mesh
    mesh.geometry = geometry
    mesh.material = material
    mesh.frustumCulled = false
    mesh.name = name
    mesh
  }
}

import demo.webapp.ThreeScene._

class ThreeScene( cfg: Config ) {

  // ******************** three scene basics ********************

  // dummy cam as projections are handled manually
  private val dummyCam = new Camera
  private val scene = new Scene
  private val pickScene = new Scene
  private val rtScene = new Scene

  @JSExport
  val renderer = new WebGLRenderer( ReadableWebGLRendererParameters )
  renderer.setClearColor( new org.denigma.threejs.Color( cfg.`Background color`( 0 ) /255f
                                                       , cfg.`Background color`( 1 ) /255f
                                                       , cfg.`Background color`( 2 ) /255f ) )

  // ******************** view management ********************

  private val zoomMax = 16
  private val zoomMin = 0.0625
  private val zoomSpeed = 1.05
  private var zoom = 0.7d

  @JSExport
  def zoom( delta: Double ): Unit = {
    zoom = Math.min( Math.max( zoom * Math.pow( zoomSpeed, delta ), zoomMin ), zoomMax )
    updateMVP()
  }

  private var projMat = Matrix4.unit
  private val viewMat = Matrix4.unit
  private var modelMat = Matrix4.naiveRotMat( 0.5, 0.3 )
  private var mvp = Matrix4.unit

  private var innerWidth: Int = 0
  private var innerHeight: Int = 0

  @JSExport
  def updateViewport( width: Int, height: Int ): Unit = {
    println( "viewport", width, height )
    innerWidth = width
    innerHeight = height
    projMat = Matrix4.orthoMatrixFromScreen( width, height, 1 )
    updateMVP()

    adjustTexturing( innerWidth, innerHeight )
  }

  @JSExport
  def rotateView( deltaX: Int, deltaY: Int ): Unit = {
    val speed = 1f / 500f / zoom / ( 1f + cfg.safeExplosionFactor )
    modelMat = Matrix4.naiveRotMat( deltaX * speed, deltaY * speed ) * modelMat
    updateMVP()
  }

  private def updateMVP(): Unit = {
    mvp = projMat * Matrix4.zoomMatrix( zoom ) * viewMat * modelMat
  }

  // ******************** axis management ********************

  private lazy val axisMesh: Mesh = makeAxisMesh

  def toggleAxis(): Unit = {
    if ( cfg.`Show axis` )
      rtScene.add( axisMesh )
    else
      rtScene.remove( axisMesh )
  }

  // ******************** mesh management ********************

  private var meshes: Option[( Mesh, Mesh )] = None
  private var mazeMesh: Option[Mesh] = None

  def addModel( model: VoronoiModel, maze: Maze[Int], depthsMap: Map[Int, Int], depthMax: Int ): Unit = {
    val ( m, pm ) = makeMesh( model, depthsMap, depthMax )
    val mm = makeMazeMesh( model, maze.childrenMap, depthMax )
    mazeMesh = Some( mm )
    meshes = Some( m, pm )
    updateMeshCulling()
    if ( cfg.`Draw cells` ) {
      scene.add( m )
      pickScene.add( pm )
    }
    if ( cfg.`Draw path` )
      scene.add( mm )
  }

  private def updateMeshCulling(): Unit = {
    for( ( m, pm ) <- meshes ) {
      val culling = if ( cfg.`Cull back` ) 0 else 2 // 2 = org.denigma.threejs.THREE.DoubleSide
      m.material.asInstanceOf[js.Dynamic].updateDynamic( "side" )( culling )
      pm.material.asInstanceOf[js.Dynamic].updateDynamic( "side" )( culling )
    }
  }

  private def makeScreenMesh: Mesh = {
    val plane = new PlaneBufferGeometry( 2, 2 )

    val customUniforms = js.Dynamic.literal(
      "texture" -> js.Dynamic.literal("type" -> "t", "value" -> renderingTexture )
    )
    val shaderMaterial = new ShaderMaterial
    shaderMaterial.uniforms = customUniforms
    shaderMaterial.vertexShader =
      """varying vec2 vUv;
        |void main() {
        |    vUv = position.xy;
        |    gl_Position = vec4( 2.0*position-1.0, 1.0 );
        |}
      """.stripMargin
    shaderMaterial.fragmentShader =
      """uniform sampler2D texture;
        |varying vec2 vUv;
        |void main(){
        |    gl_FragColor = texture2D( texture, vUv );
        |}
      """.stripMargin
    shaderMaterial.depthWrite = false

    assembleMesh( plane, shaderMaterial, "screenMesh" )
  }

  // ******************** special effects ********************

  def toggleMazePath(): Unit = {
    if ( cfg.`Draw path` )
      mazeMesh.foreach( scene.add )
    else
      mazeMesh.foreach( scene.remove )
  }

  def toggleCells(): Unit = {
    if ( cfg.`Draw cells` )
      meshes.foreach { case ( m, pm ) => scene.add( m ) ; pickScene.add( pm ) }
    else
      meshes.foreach { case ( m, pm ) => scene.remove( m ) ; pickScene.remove( pm ) }
  }

  def setBackground( color: Int ): Unit = {
    import demo.Colors.intColorToFloatsColors
    val ( r, g, b ): ( Float, Float, Float ) = color
    renderer.setClearColor( new org.denigma.threejs.Color( r, g, b ) )
  }

  def udpateDownsampling(): Unit = {
    adjustTexturing( innerWidth, innerHeight )
  }

  private val screenMesh: Mesh = makeScreenMesh
  rtScene.add( screenMesh )

  private var renderingTexture = makeTexture( textureW, textureH )

  private def makeTexture( w: Int, h: Int ): WebGLRenderTarget = {
    val t = new WebGLRenderTarget( w, h )
    //    t.asInstanceOf[js.Dynamic].updateDynamic( "format" )( 1020d ) // RGB
    t.asInstanceOf[js.Dynamic].updateDynamic( "minFilter" )( 1006d ) // Linear, needed for non power of 2 sizes
    t.asInstanceOf[js.Dynamic].updateDynamic( "magFilter" )( 1003d ) // Nearest. Comment for smoother rendering
    t
  }

  private def adjustTexturing( w: Int, h: Int ): Unit = {
    val downsampling = cfg.safeDownsamplingFactor
    renderingTexture = makeTexture( w / downsampling, h / downsampling )
  }

  def colorFace( faceOffset: Int
               , faceSize: Int
               , color: ( Float, Float, Float )
               , centerColor: ( Float, Float, Float ) ): Unit = {

    for ( ( mesh, _ ) <- meshes ) {
      val attrs = mesh
        .geometry.asInstanceOf[MyBufferGeometry]
        .attributes
      val colorAttr = attrs
        .asInstanceOf[scala.scalajs.js.Dynamic]
        .selectDynamic( "a_color" )
      val colorData = colorAttr.selectDynamic( "array" ).asInstanceOf[Array[Float]]
      // apply color until face center offset
      ( 0 until faceSize-1 ).foreach { i =>
        colorData.update( 3*faceOffset+3*i,   color._1 )
        colorData.update( 3*faceOffset+3*i+1, color._2 )
        colorData.update( 3*faceOffset+3*i+2, color._3 )
        colorData.update( 3*faceOffset+3*(i+faceSize-1),   color._1 )
        colorData.update( 3*faceOffset+3*(i+faceSize-1)+1, color._2 )
        colorData.update( 3*faceOffset+3*(i+faceSize-1)+2, color._3 )
      }
      // apply specific color at face center offset
      colorData.update( 3*faceOffset+3*faceSize-6, centerColor._1 )
      colorData.update( 3*faceOffset+3*faceSize-5, centerColor._2 )
      colorData.update( 3*faceOffset+3*faceSize-4, centerColor._3 )
      colorData.update( 3*faceOffset+3*faceSize-3, centerColor._1 )
      colorData.update( 3*faceOffset+3*faceSize-2, centerColor._2 )
      colorData.update( 3*faceOffset+3*faceSize-1, centerColor._3 )
      colorAttr.updateDynamic( "needsUpdate" )( true )
    }
  }

  def toggleCullback(): Unit = {
    updateMeshCulling()
  }

  // ******************** rendering ********************

  private def updateMeshMaterialValue( mesh: Mesh ) ( field: String, value: js.Any ): Unit = {
    mesh.material.asInstanceOf[ShaderMaterial].uniforms.asInstanceOf[scala.scalajs.js.Dynamic]
      .selectDynamic( field )
      .updateDynamic( "value" )( value )
  }

  private val pixels = new Uint8Array( 4 )

  @JSExport
  def pickRender( mouseX: Int, mouseY: Int ): Int = {
    if ( cfg.`Draw cells` ) {
      for ( ( _, m ) <- meshes ) {
        val updateThis = updateMeshMaterialValue( m ) _
        updateThis( "u_time", 0f )
        updateThis( "u_explosionFactor", cfg.safeExplosionFactor )
        updateThis( "u_depthScale", cfg.mazeDepthFactor )
        updateThis( "u_thickness", cfg.safeCellThickness )
        updateThis( "u_mvpMat", mvp )
      }
      renderer.render( pickScene, dummyCam )
      import js.DynamicImplicits._
      val gl = renderer.getContext().asInstanceOf[js.Dynamic]
      gl.readPixels( mouseX, innerHeight - mouseY, 1, 1, gl.RGBA, gl.UNSIGNED_BYTE, pixels )
      256 * 256 * pixels( 0 ) + 256 * pixels( 1 ) + pixels( 2 )
    } else
      0
  }

  @JSExport
  def render( highlighted: Int ): Unit = {
    if ( cfg.`Draw cells` )
      for ( ( m, _ ) <- meshes ) {
        val updateThis = updateMeshMaterialValue( m ) _
        updateThis( "u_time", 0f )
        updateThis( "u_explosionFactor", cfg.safeExplosionFactor )
        updateThis( "u_depthScale", cfg.mazeDepthFactor )
        updateThis( "u_thickness", cfg.safeCellThickness )
        updateThis( "u_mvpMat", mvp )
        updateThis( "u_borderWidth", cfg.safeBordersWidth )
        updateThis( "u_borderColor", new org.denigma.threejs.Vector3( cfg.`Borders color`( 0 ) / 255f
                                                                    , cfg.`Borders color`( 1 ) / 255f
                                                                    , cfg.`Borders color`( 2 ) / 255f ) )
        updateThis( "u_faceHighlightFlag", highlighted.toFloat )
      }

    if ( cfg.`Show axis` )
      updateMeshMaterialValue( axisMesh )( "u_mvpMat", mvp )

    if ( cfg.`Draw path` )
      for( mm <- mazeMesh ) {
        val updateThis = updateMeshMaterialValue( mm ) _
        updateThis( "u_time", 0f )
        updateThis( "u_explosionFactor", cfg.safeExplosionFactor )
        updateThis( "u_depthScale", cfg.mazeDepthFactor )
        updateThis( "u_mvpMat", mvp )
        updateThis( "u_color", new org.denigma.threejs.Vector3( cfg.`Path color`( 0 ) / 255f
                                                              , cfg.`Path color`( 1 ) / 255f
                                                              , cfg.`Path color`( 2 ) / 255f ) )
      }

    renderer.clearColor()
    renderer.render( scene, dummyCam, renderingTexture )
    updateMeshMaterialValue( screenMesh )( "texture", renderingTexture )
    renderer.render( rtScene, dummyCam )
  }
}
