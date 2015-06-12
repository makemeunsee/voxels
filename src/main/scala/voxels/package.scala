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
    TriangularCupola ::
    Icosidodecahedron ::
    RhombicDodecahedron ::
    // ElongatedDodecahedron // incompatible rhombi ::
    TrigonalTrapezohedron ::
    SquareCupola ::
    RhombicPrism ::
    Nil ++
    List( 3, 5, 6, 8 ).map( RegularPrism ) ++
    List( 4, 5, 6, 8 ).map( AntiPrism )
  ).zipWithIndex.map { case ( v, i ) => ( i, v ) }.toMap
}
