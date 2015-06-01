/**
 * Created by markus on 26/05/15.
 */
package object voxels {
  val standards = (
    Tetrahedron ::
    Cube ::
    Octahedron ::
    Dodecahedron ::
    Icosahedron ::
    TruncatedTetrahedron ::
    TruncatedCube ::
    TruncatedOctahedron ::
    TruncatedIcosahedron ::
    Cuboctahedron ::
    RhombicDodecahedron ::
    // ElongatedDodecahedron // incompatible rhombi ::
    TrigonalTrapezohedron ::
    Rhombicuboctahedron ::
    RhombicPrism ::
    Nil ++
    List( 3, 5, 6, 8 ).map( RegularPrism )
  ).zipWithIndex.map { case ( v, i ) => ( i, v ) }.toMap
}