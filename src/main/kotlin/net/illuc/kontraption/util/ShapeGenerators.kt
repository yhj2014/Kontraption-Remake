package net.illuc.kontraption.util

object ShapeGenerators {
    fun largeIonRing(
        width: Int,
        height: Int,
        depth: Int,
    ): Shape3D {
        val types = ByteArray(width * height * depth)
        var i = 0
        for (y in 0 until height) {
            for (z in 0 until depth) {
                for (x in 0 until width) {
                    val isCorner = (x == 0 || x == width - 1) && (z == 0 || z == depth - 1)
                    val isEdge = (x == 0 || x == width - 1 || z == 0 || z == depth - 1)
                    val isInner = (x > 0 && x < width - 1) && (z > 0 && z < depth - 1)
                    val isnInner = (x > 1 && x < width - 2) && (z > 1 && z < depth - 2)
                    val isSpecialCell =
                        (x <= 1 || x >= width - 2) && (z == 0 || z == 1 || z == depth - 1 || z == depth - 2) && !isCorner

                    types[i++] =
                        when {
                            y == height - 1 && isCorner -> 4
                            isSpecialCell && !isInner -> 1
                            y == height - 1 && isEdge -> 3
                            isCorner && y == 0 -> 1
                            isCorner -> 2
                            y > 0 && isEdge -> 1
                            !isnInner && isInner && y == 0 -> 1
                            !isnInner && isInner && y > 0 -> 5
                            y > 0 -> 0
                            else -> if (isEdge) 2 else 0
                        }
                }
            }
        }
        return Shape3D(width, height, depth, types)
    }
}
