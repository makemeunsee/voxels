package demo.webapp

/**
 * Created by markus on 16/02/2015.
 */

import geometry.{Matrix4, Vec3}
import geometry.Matrix4.{rotationMatrix, translationMatrix}
import io._
import voxels._

import scala.collection.immutable.TreeSet
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.{JSName, JSExport}
import scala.scalajs.js.typedarray.{Uint16Array, Float32Array}
import scala.scalajs.js.{Array, Dictionary}

import org.denigma.threejs._

import scala.util.Random

object TutoMain extends JSApp {
  def main(): Unit = {
    println( "start" )
  }

  private val dummyCam = new Camera
  private val scene = new Scene
  private val pickScene = new Scene

  private object ReadableWebGLRendererParameters extends WebGLRendererParameters {
    preserveDrawingBuffer = true
  }

  @JSExport
  val renderer = new WebGLRenderer( ReadableWebGLRendererParameters )

  private var meshes = Map.empty[Int, ( Mesh, Mesh )]

  private var showBorders = true

  @JSExport
  def toggleBorders(): Unit = {
    showBorders = !showBorders
  }

  private var rndColors = true

  @JSExport
  def toggleRndColors(): Unit = {
    rndColors = !rndColors
    if ( rndColors )
      meshes.keys foreach rndColor
    else
      meshes.keys foreach whiteColor
  }

  @JSExport
  def pickRender() = renderer.render( pickScene, dummyCam )

  private def updateMeshMaterialValue( mesh: Mesh ) ( field: String, value: js.Any ): Unit = {
    mesh.material.asInstanceOf[ShaderMaterial].uniforms.asInstanceOf[scala.scalajs.js.Dynamic]
      .selectDynamic( field )
      .updateDynamic( "value" )( value )
  }

  @JSExport
  def render() = {
    meshes.values.foreach { case ( m, pm ) =>
      val updateThis = updateMeshMaterialValue( m ) _
      updateThis( "u_time", 0f )
      updateThis( "u_borderWidth", if ( showBorders ) 1f else 0f )
      updateThis( "u_borderColor", new Vector3( 0.05,0.05,0.05 ) )
      val cCode = colorCode( selectedVoxel, selectedFace )
      updateThis( "u_highlightFlag", ( cCode % 131072 ).toFloat )
      updateThis( "u_faceHighlightFlag", cCode.toFloat )
      updateThis( "u_mvpMat", mvp )
    }
    renderer.render( scene, dummyCam )
  }

  private def naiveRotMat( theta: Double, phi: Double ): Matrix4 = {
    rotationMatrix( theta, 0, 1, 0 ) * rotationMatrix( phi, 1, 0, 0 )
  }

  private def zoomMatrix( zoom: Double ): Matrix4 = {
    assert( zoom != 0 )
    Matrix4.unit.copy( a33 = 1d / zoom )
  }

  private def orthoMatrix( left: Double, right: Double, bottom: Double, top: Double, near: Double, far: Double ): Matrix4 = {
    val x_orth = 2 / ( right - left )
    val y_orth = 2 / ( top - bottom )
    val z_orth = -2 / ( far - near )
    val tx = -( right + left ) / ( right - left )
    val ty = -( top + bottom ) / ( top - bottom )
    val tz = -( far + near ) / ( far - near )
    Matrix4( x_orth, 0, 0, tx
           , 0, y_orth, 0, ty
           , 0, 0, z_orth, tz
           , 0, 0, 0, 1 )
  }

  private def orthoMatrixFromScreen( w: Double, h: Double, k: Double ) = {
    val hh = if ( h < 0 ) 1 else h
    val aspect = w / hh
    val far = 100
    val near = -100
    val right = k * aspect
    val top = k
    val left = -right
    val bottom = -top
    orthoMatrix( left, right, bottom, top, near, far )
  }

  import scala.language.implicitConversions
  private implicit def toJsMatrix( m: Matrix4 ): org.denigma.threejs.Matrix4 = {
    val r = new org.denigma.threejs.Matrix4()
    r.set( m.a00, m.a01, m.a02, m.a03
         , m.a10, m.a11, m.a12, m.a13
         , m.a20, m.a21, m.a22, m.a23
         , m.a30, m.a31, m.a32, m.a33
         )
    r
  }

  private val zoomMax = 16
  private val zoomMin = 0.0625
  private val zoomSpeed = 1.05
  private var zoom = 1d

  @JSExport
  def zoom( delta: Double ): Unit = {
    zoom = Math.min( Math.max( zoom * Math.pow( zoomSpeed, delta ), zoomMin ), zoomMax )
    updateMVP()
  }

  private var projMat = Matrix4.unit
  private val viewMat = Matrix4.unit
  private var modelMat = naiveRotMat( 0.5, 0.3 )
  private var transMat = Matrix4.unit
  private var mvp = Matrix4.unit

  @JSExport
  def updateViewport( width: Int, height: Int ): Unit = {
    projMat = orthoMatrixFromScreen( width, height, 1.75 )
    updateMVP()
  }

  @JSExport
  def rotateView( deltaX: Int, deltaY: Int ): Unit = {
    modelMat = naiveRotMat( deltaX * 0.002, deltaY * 0.002 ) * modelMat
    updateMVP()
  }

  @JSExport
  def centerViewOn( voxelId: Int ): Unit = {
    transMat =
      voxels
        .get( voxelId )
        .map { v => Matrix4.translationMatrix( v.faces.foldLeft( Vec3( 0,0,0 ) )( _ - _.center ) / v.faces.length ) }
        .getOrElse( Matrix4.unit )
    updateMVP()
    voxelActionsStack = CenterOn( voxelId ) :: voxelActionsStack
  }

  private def updateMVP(): Unit = {
    mvp = projMat * zoomMatrix( zoom ) * viewMat * modelMat * transMat
  }

  // BufferGeometry from org.denigma.threejs does not extends Geometry, has to be redefined
  @JSName( "THREE.BufferGeometry" )
  class MyBufferGeometry extends Geometry {
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

  @JSExport
  val voxelTypeCount = standards.size

  private var freeVoxelIds: Set[Int] = TreeSet.empty
  private var lastDockedId: Int = -1

  private var voxels: Map[Int, Voxel] = Map.empty
  private var selectedVoxel: Int = -1
  private var selectedFace: Int = -1
  // ( voxelStd id, face id, rotation step )
  private var dockingOptions = Seq.empty[( Int,Int,Int )]

  private var voxelActionsStack: List[VoxelAction] = Nil

  private def makeMesh( v: Voxel, vId: Int ): ( Mesh, Mesh ) = {
    val customUniforms = js.Dynamic.literal(
      "u_time" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 ),
      "u_borderWidth" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 ),
      "u_mvpMat" -> js.Dynamic.literal( "type" -> "m4", "value" -> new org.denigma.threejs.Matrix4() ),
      "u_borderColor" -> js.Dynamic.literal( "type" -> "v3", "value" -> new Vector4( 0, 0, 0, 1 ) ),
      "u_highlightFlag" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 ),
      "u_faceHighlightFlag" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 )
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
      )
    )

    val shaderMaterial = new ShaderMaterial
    shaderMaterial.attributes = attrs
    shaderMaterial.uniforms = customUniforms
    shaderMaterial.vertexShader = Shaders.vertexShader
    shaderMaterial.fragmentShader = Shaders.fragmentShader
//    shaderMaterial.side = org.denigma.threejs.THREE.DoubleSide

    val pickShaderMaterial = new ShaderMaterial
    pickShaderMaterial.attributes = attrs
    pickShaderMaterial.uniforms = customUniforms
    pickShaderMaterial.vertexShader = Shaders.pickVertexShader
    pickShaderMaterial.fragmentShader = Shaders.pickFragmentShader
//    pickShaderMaterial.side = org.denigma.threejs.THREE.DoubleSide

    val count = v.faces.map( _.rawVertices.length+3 ).sum
    val indicesCount = v.faces.map( _.vertices.length*3 ).sum

    val vertices = new Float32Array( count )
    val normals = new Float32Array( count )
    val colors = new Float32Array( count )
    val pickColors = new Float32Array( count / 3 )
    val centerFlags = new Float32Array( count / 3 )
    val indices = new Uint16Array( indicesCount )
    var offset = 0
    var indicesOffset = 0
    v.faces
      .zipWithIndex
      .foreach { case ( f, fId ) =>
        val Vec3( nx, ny, nz ) = f.normal
        val Vec3( cx, cy, cz ) = f.center
        val pickColor = colorCode( vId, fId )
        val ( r, g, b ) = if ( rndColors ) rndColor() else ( 1f, 1f, 1f )
        val ( cr, cg, cb ) = if ( rndColors ) rndColor() else ( 1f, 1f, 1f )

        val triOffset = offset*3
        val vSize = f.vertices.length

        ( 0 until vSize ).foreach { i =>
          vertices.set( triOffset+3*i,   f.rawVertices( 3*i ).toFloat )
          vertices.set( triOffset+3*i+1, f.rawVertices( 3*i+1 ).toFloat )
          vertices.set( triOffset+3*i+2, f.rawVertices( 3*i+2 ).toFloat )
          normals.set( triOffset+3*i,   nx.toFloat )
          normals.set( triOffset+3*i+1, ny.toFloat )
          normals.set( triOffset+3*i+2, nz.toFloat )
          colors.set( triOffset+3*i,   r )
          colors.set( triOffset+3*i+1, g )
          colors.set( triOffset+3*i+2, b )
          centerFlags.set( offset+i, 0f )
          pickColors.set( offset+i, pickColor )
          indices.set( indicesOffset+3*i,   offset+vSize )
          indices.set( indicesOffset+3*i+1, offset+i )
          indices.set( indicesOffset+3*i+2, offset+( i+1 )%vSize )
        }
        vertices.set( triOffset+3*vSize,   cx.toFloat )
        vertices.set( triOffset+3*vSize+1, cy.toFloat )
        vertices.set( triOffset+3*vSize+2, cz.toFloat )
        normals.set( triOffset+3*vSize,   nx.toFloat )
        normals.set( triOffset+3*vSize+1, ny.toFloat )
        normals.set( triOffset+3*vSize+2, nz.toFloat )
        colors.set( triOffset+3*vSize,   cr )
        colors.set( triOffset+3*vSize+1, cg )
        colors.set( triOffset+3*vSize+2, cb )
        centerFlags.set( offset+vSize, 1f )
        pickColors.set( offset+vSize, pickColor )

        offset = offset + vSize + 1
        indicesOffset = indicesOffset + vSize*3
      }

    geom.addAttribute( "index", new BufferAttribute( indices, 1 ) )
    geom.addAttribute( "a_centerFlag", new BufferAttribute( centerFlags, 1 ) )
    geom.addAttribute( "a_normal", new BufferAttribute( normals, 3 ) )
    geom.addAttribute( "a_color", new BufferAttribute( colors, 3 ) )
    geom.addAttribute( "a_pickColor", new BufferAttribute( pickColors, 1 ) )
    geom.addAttribute( "position", new BufferAttribute( vertices, 3 ) )

    ( assembleMesh( geom, shaderMaterial ), assembleMesh( geom, pickShaderMaterial ) )
  }

  private def assembleMesh( geometry: MyBufferGeometry, material: ShaderMaterial ): Mesh = {
    val mesh = new Mesh
    mesh.geometry = geometry
    mesh.material = material
    mesh.frustumCulled = false
    mesh
  }


  @JSExport
  def unloadVoxel(): Unit = {
    freeVoxelIds = TreeSet.empty
    voxels = Map.empty
    meshes.foreach { case ( _, ( m, pm ) ) =>
      scene.remove( m )
      pickScene.remove( pm )
      // TODO check if m.geometry.dispose() and such are useful
    }
    meshes = Map.empty
    voxelActionsStack = Nil
    clearSelection()
  }

  @JSExport
  def loadVoxel( i: Int ): Unit = {
    unloadVoxel()
    voxels = Map( 0 -> Voxel( standards.getOrElse( i, Cube ), Matrix4.unit ) )
    meshes = voxels.map { case ( k, v ) => ( k, makeMesh( v, k ) ) }
    scene.add( meshes( 0 )._1 )
    pickScene.add( meshes( 0 )._2 )
    voxelActionsStack = Insert( i ) :: voxelActionsStack
    centerViewOn( 0 )
  }

  private def colorCode( voxelId: Int, faceId: Int ) = ( faceId << 17 ) + ( voxelId+1 )
  private def revertColorCode( colorCode: Int ): ( Int, Int ) = {
    val faceId = colorCode >>> 17
    val voxelId = colorCode - ( faceId << 17 ) - 1
    ( voxelId, faceId )
  }

  @JSExport
  def selectFace( colorCode: Int ): Dictionary[String] = {
    val ( vId, fId ) = revertColorCode( colorCode )
    dockingOptions = listDockingOptions( vId, fId )
    selectedVoxel = vId
    selectedFace = fId
    val selection = voxels.lift( vId ).flatMap( _.faces.lift( fId ) ).fold( -1, -1, "" )( f => ( vId, fId, f.faceType.toString ) )
    Map( ( "voxelId", selection._1.toString )
       , ( "faceId", selection._2.toString )
       , ( "faceInfo", selection._3 )
       )
      .toJSDictionary
  }

  @JSExport
  def showDockingOptions(): Dictionary[String] = dockingOptions
    .zipWithIndex
    .map { case ( ( stdId, _, _ ), i ) =>
      val std = standards( stdId )
      ( i.toString
      , s"${std.name}"
      )
    }
    .toMap
    .toJSDictionary

  private def listDockingOptions( voxelId: Int, faceId: Int ): Seq[( Int,Int,Int )] = {
    voxels
      .lift( voxelId )
      .flatMap( _.faces.lift( faceId ) )
      .fold( Seq.empty[( Int,Int,Int )] ) { f =>
        val faceType = f.faceType
        standards
          .flatMap { case ( stdId, std ) =>
            std
              .uniquePositionings
              .collect { case ( fId, fType, rot ) if fType == faceType => ( stdId, fId, rot ) }
        }.toSeq
    }
  }

  private val rnd = new Random

  private def rndColor(): ( Float, Float, Float ) = {
    val rndInt = rnd.nextInt()
    val r = ( ( rndInt >> 16 ) & 0xff ).toFloat / 255f
    val g = ( ( rndInt >> 8 ) & 0xff ).toFloat / 255f
    val b = ( rndInt & 0xff ).toFloat / 255f
    ( r, g, b )
  }

  private def rndColor( voxelId: Int ): Unit = {
    voxels.lift( voxelId ).foreach { voxel =>
      ( 0 until voxel.faces.length ).foreach( i => colorFace( voxelId, i, rndColor(), rndColor() ) )
    }
  }

  private def whiteColor( voxelId: Int ): Unit = {
    voxels.lift( voxelId ).foreach { voxel =>
      ( 0 until voxel.faces.length ).foreach( i => colorFace( voxelId, i, ( 1,1,1 ), ( 1,1,1 ) ) )
    }
  }

  private def colorFace( voxelId: Int
                         , faceId: Int
                         , color: ( Float, Float, Float )
                         , centerColor: ( Float, Float, Float ) ): Unit = {
    voxels.lift( voxelId ).foreach { voxel =>
      voxel.faces.lift( faceId ).foreach { face =>
        val mesh = meshes( voxelId )._1
        val attrs = mesh
          .geometry.asInstanceOf[MyBufferGeometry]
          .attributes
        val colorAttr = attrs
          .asInstanceOf[scala.scalajs.js.Dynamic]
          .selectDynamic( "a_color" )
        val colorData = colorAttr.selectDynamic( "array" ).asInstanceOf[Array[Float]]
        val offset = voxel.faces.zipWithIndex.foldLeft( 0 ) { case ( acc, ( f, i ) ) =>
          if ( i < faceId ) acc + f.vertices.length + 1
          else if ( i > faceId ) acc
          else acc
        } * 3
        val l = face.vertices.length
        ( 0 until l ).foreach { i =>
          colorData.update( offset+3*i,   color._1 )
          colorData.update( offset+3*i+1, color._2 )
          colorData.update( offset+3*i+2, color._3 )
        }
        colorData.update( offset+3*l,   centerColor._1 )
        colorData.update( offset+3*l+1, centerColor._2 )
        colorData.update( offset+3*l+2, centerColor._3 )
        colorAttr.updateDynamic( "needsUpdate" )( true )
      }
    }
  }

  @JSExport
  def dockVoxel( dockingID: Int ): Unit = {
    assert( dockingID >= 0 && dockingID < dockingOptions.length )

    val newVId = dockingOptions( dockingID )._1
    val fId = dockingOptions( dockingID )._2
    val rot = dockingOptions( dockingID )._3

    dockVoxel( newVId, fId, rot, selectedVoxel, selectedFace )
  }

  private def dockVoxel( stdId: Int, faceId: Int, rot: Int, onVoxelId: Int, onFaceId: Int ): Unit = {

    val newVoxelStd = standards.getOrElse( stdId, Cube )
    val newVoxelUntransformed = Voxel( newVoxelStd, Matrix4.unit )
    val sourceFace = newVoxelUntransformed.faces( faceId )
    val targetFace = voxels( onVoxelId ).faces( onFaceId )

    // rotation so docking faces actually face each other
    val targetN@Vec3( nx, ny, nz ) = targetFace.normal
    val sourceN = sourceFace.normal
    val rM = rotationMatrix( sourceN, targetN.negate )

    // rotated voxel
    val newVoxelRotated = Voxel( newVoxelStd, rM )
    val sourceFace1 = newVoxelRotated.faces( faceId )

    val to = targetFace.center

    // spin so docking faces actually fit each other
    val from = sourceFace1.center
    val preSpinTranslation = translationMatrix( from.negate )
    val postSpinTranslation = translationMatrix( from )
    val spinRotation = rotationMatrix( ( sourceFace1.vertices.head - from ).normalize
                                     , ( targetFace.vertices.head - to ).normalize
                                     , Some( targetN ) )
    // spin shift (rotation variations)
    val bonusSpinRotation = rotationMatrix( rot*2*math.Pi/sourceFace.vertices.length, nx, ny, nz )

    // translation so docking faces actually touch each other
    val tM = translationMatrix( to - from )

    val newVoxel = Voxel( newVoxelStd, tM * postSpinTranslation * spinRotation * bonusSpinRotation * preSpinTranslation * rM  )

    lastDockedId = if ( freeVoxelIds.isEmpty ) voxels.size
                   else { val r = freeVoxelIds.head
                          freeVoxelIds = freeVoxelIds - r
                          r }
    println( s"new voxel with id $lastDockedId, free ids: $freeVoxelIds" )

    voxels = voxels + ( ( lastDockedId, newVoxel ) )

    val newMeshes = makeMesh( newVoxel, lastDockedId )
    meshes = meshes + ( ( lastDockedId, newMeshes ) )
    scene.add( newMeshes._1 )
    pickScene.add( newMeshes._2 )

    voxelActionsStack = Dock( stdId, faceId, rot, onVoxelId, onFaceId ) :: voxelActionsStack
  }

  @JSExport
  def undockLastVoxel(): Unit = {
    voxels = voxels - lastDockedId
    val removedMeshes = meshes( lastDockedId )
    meshes = meshes - lastDockedId
    scene.remove( removedMeshes._1 )
    pickScene.remove( removedMeshes._2 )
    freeVoxelIds = freeVoxelIds + lastDockedId
    voxelActionsStack = voxelActionsStack.tail
  }

  @JSExport
  def deleteSelected(): Unit = delete( selectedVoxel )

  private def delete( id: Int ): Unit = {
    if ( voxels.size > 1 ) {
      voxels.get( id ).foreach { v =>
        voxels = voxels - id
        val removedMeshes = meshes( id )
        meshes = meshes - id
        scene.remove( removedMeshes._1 )
        pickScene.remove( removedMeshes._2 )
        freeVoxelIds = freeVoxelIds + id
        voxelActionsStack = Delete( id ) :: voxelActionsStack
        println( s"deleted voxel $id, free ids: $freeVoxelIds" )
        clearSelection()
      }
    }
  }

  @JSExport
  def clearSelection(): Unit = {
    selectedVoxel = -1
    selectedFace = -1
    dockingOptions = Seq.empty
    lastDockedId = -1
  }

  @JSExport
  def getVoxelName( id: Int ): String = standards.lift( id ).fold( "" )( _.name )

  @JSExport
  def buildCode(): String = voxelActionsStack.mkString( "," )

  @JSExport
  def loadCode( code: String ): Int = {
    var id = -1
    code.split( "," ).reverse.foreach {
      case VoxelAction.deletePattern( c ) =>
        delete( c.toInt )
      case VoxelAction.insertPattern( c ) =>
        id = c.toInt
        loadVoxel( id )
      case VoxelAction.centerOnPattern( c ) =>
        centerViewOn( c.toInt )
      case VoxelAction.dockPattern( c0, c1, c2, c3, c4 ) =>
        dockVoxel( c0.toInt, c1.toInt, c2.toInt, c3.toInt, c4.toInt )
      case _ => ()
    }
    id
  }
}
