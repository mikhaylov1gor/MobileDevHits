package Cube

import kotlin.math.cos
import kotlin.math.sin

data class Point(val x: Float, val y: Float, val z: Float) {
    operator fun plus(p: Point) = Point(x + p.x, y + p.y, z + p.z)
    operator fun minus(p: Point) = Point(x - p.x, y - p.y, z - p.z)
    operator fun times(f: Float) = Point(x * f, y * f, z * f)
    operator fun div(d: Float) = Point(x / d, y / d, z / d)
}

data class Face(val vertices: List<Point>, val number: Int, val numberInPoints: List<Point>)

class Cube() {
    private val size: Float = 200f
    private val halfSize = size / 2
    private val vertices: List<Point> = listOf(
        Point(-halfSize, -halfSize, halfSize),
        Point(halfSize, -halfSize, halfSize),
        Point(halfSize, halfSize, halfSize),
        Point(-halfSize, halfSize, halfSize),
        Point(-halfSize, -halfSize, -halfSize),
        Point(halfSize, -halfSize, -halfSize),
        Point(halfSize, halfSize, -halfSize),
        Point(-halfSize, halfSize, -halfSize)
    )
    private val numbersInPoints: List<List<Point>> = listOf(
        listOf(
            Point(-3f / 10f * halfSize, -2f / 10f * halfSize, halfSize),
            Point(1f / 10f * halfSize, -7f / 10f * halfSize, halfSize),
            Point(1f / 10f * halfSize, 7f / 10f * halfSize, halfSize)
        ),
        listOf(
            Point(halfSize, -6f / 10f * halfSize, 3f / 10f * halfSize),
            Point(halfSize, -7f / 10f * halfSize, 2f / 10f * halfSize),
            Point(halfSize, -7f / 10f * halfSize, -2f / 10f * halfSize),
            Point(halfSize, -6f / 10f * halfSize, -3f / 10f * halfSize),
            Point(halfSize, -2f / 10f * halfSize, -3f / 10f * halfSize),
            Point(halfSize, 6f / 10f * halfSize, 3f / 10f * halfSize),
            Point(halfSize, 7f / 10f * halfSize, 3f / 10f * halfSize),
            Point(halfSize, 7f / 10f * halfSize, -3f / 10f * halfSize)
        ),
        listOf(
            Point(3f / 10f * halfSize, -6f / 10f * halfSize, -halfSize),
            Point(2f / 10f * halfSize, -7f / 10f * halfSize, -halfSize),
            Point(-2f / 10f * halfSize, -7f / 10f * halfSize, -halfSize),
            Point(-3f / 10f * halfSize, -6f / 10f * halfSize, -halfSize),
            Point(-3f / 10f * halfSize, -2f / 10f * halfSize, -halfSize),
            Point(-2f / 10f * halfSize, -1f / 10f * halfSize, -halfSize),
            Point(0f, 0f, -halfSize),
            Point(-2f / 10f * halfSize, 1f / 10f * halfSize, -halfSize),
            Point(-3f / 10f * halfSize, 2f / 10f * halfSize, -halfSize),
            Point(-3f / 10f * halfSize, 6f / 10f * halfSize, -halfSize),
            Point(-2f / 10f * halfSize, 7f / 10f * halfSize, -halfSize),
            Point(2f / 10f * halfSize, 7f / 10f * halfSize, -halfSize),
            Point(3f / 10f * halfSize, 6f / 10f * halfSize, -halfSize)
        ),
        listOf(
            Point(-halfSize, -7f / 10f * halfSize, -3f / 10f * halfSize),
            Point(-halfSize, -1f / 10f * halfSize, -3f / 10f * halfSize),
            Point(-halfSize, 0f, -2f / 10f * halfSize),
            Point(-halfSize, 0f, 3f / 10f * halfSize),
            Point(-halfSize, -7f / 10f * halfSize, 3f / 10f * halfSize),
            Point(-halfSize, 7f / 10f * halfSize, 3f / 10f * halfSize)
        ),
        listOf(
            Point(3f / 10f * halfSize, -halfSize, -7f / 10f * halfSize),
            Point(-3f / 10f * halfSize, -halfSize, -7f / 10f * halfSize),
            Point(-3f / 10f * halfSize, -halfSize, 0f),
            Point(-2f / 10f * halfSize, -halfSize, -1f / 10f * halfSize),
            Point(2f / 10f * halfSize, -halfSize, -1f / 10f * halfSize),
            Point(3f / 10f * halfSize, -halfSize, 0f),
            Point(3f / 10f * halfSize, -halfSize, 6f / 10f * halfSize),
            Point(2f / 10f * halfSize, -halfSize, 7f / 10f * halfSize),
            Point(-2f / 10f * halfSize, -halfSize, 7f / 10f * halfSize),
            Point(-3f / 10f * halfSize, -halfSize, 6f / 10f * halfSize)
        ),
        listOf(
            Point(3f / 10f * halfSize, halfSize, 6f / 10f * halfSize),
            Point(2f / 10f * halfSize, halfSize, 7f / 10f * halfSize),
            Point(-2f / 10f * halfSize, halfSize, 7f / 10f * halfSize),
            Point(-3f / 10f * halfSize, halfSize, 6f / 10f * halfSize),
            Point(-3f / 10f * halfSize, halfSize, -6f / 10f * halfSize),
            Point(-2f / 10f * halfSize, halfSize, -7f / 10f * halfSize),
            Point(2f / 10f * halfSize, halfSize, -7f / 10f * halfSize),
            Point(3f / 10f * halfSize, halfSize, -6f / 10f * halfSize),
            Point(3f / 10f * halfSize, halfSize, 0f),
            Point(2f / 10f * halfSize, halfSize, 1f / 10f * halfSize),
            Point(-2f / 10f * halfSize, halfSize, 1f / 10f * halfSize),
            Point(-3f / 10f * halfSize, halfSize, 0f)
        ),
    )
    private val faces: List<Face> = listOf(
        Face(listOf(vertices[0], vertices[1], vertices[2], vertices[3]), 1, numbersInPoints[0]),
        Face(listOf(vertices[1], vertices[5], vertices[6], vertices[2]), 2, numbersInPoints[1]),
        Face(listOf(vertices[5], vertices[4], vertices[7], vertices[6]), 3, numbersInPoints[2]),
        Face(listOf(vertices[4], vertices[0], vertices[3], vertices[7]), 4, numbersInPoints[3]),
        Face(listOf(vertices[4], vertices[5], vertices[1], vertices[0]), 5, numbersInPoints[4]),
        Face(listOf(vertices[3], vertices[2], vertices[6], vertices[7]), 6, numbersInPoints[5])
    )

    fun rotate(rotationMatrix: Matrix3x3): List<Face> {
        val result = ArrayList<Face>()
        for (face in faces) {
            val newVertices: ArrayList<Point> = ArrayList()
            for (vertex in face.vertices) {
                newVertices.add(rotationMatrix * vertex)
            }
            val newNumberPoints: ArrayList<Point> = ArrayList()
            for (point in face.numberInPoints) {
                newNumberPoints.add(rotationMatrix * point)
            }
            result.add(Face(newVertices, face.number, newNumberPoints))
        }
        return result
    }
}

class Matrix3x3(private val matrix: Array<FloatArray>) {
    operator fun times(point: Point): Point {
        val x = matrix[0][0] * point.x + matrix[0][1] * point.y + matrix[0][2] * point.z
        val y = matrix[1][0] * point.x + matrix[1][1] * point.y + matrix[1][2] * point.z
        val z = matrix[2][0] * point.x + matrix[2][1] * point.y + matrix[2][2] * point.z
        return Point(x, y, z)
    }

    operator fun times(m: Matrix3x3): Matrix3x3 {
        val result = Array(3) { FloatArray(3) }
        for (i in 0..2) {
            for (j in 0..2) {
                for (k in 0..2) {
                    result[i][j] += this.matrix[i][k] * m.matrix[k][j]
                }
            }
        }
        return Matrix3x3(result)
    }
}

fun rotationMatrixX(angle: Float): Matrix3x3 {
    val cos = cos(angle)
    val sin = sin(angle)
    return Matrix3x3(
        arrayOf(
            floatArrayOf(1f, 0f, 0f),
            floatArrayOf(0f, cos, -sin),
            floatArrayOf(0f, sin, cos)
        )
    )
}

fun rotationMatrixY(angle: Float): Matrix3x3 {
    val cos = cos(angle)
    val sin = sin(angle)
    return Matrix3x3(
        arrayOf(
            floatArrayOf(cos, 0f, sin),
            floatArrayOf(0f, 1f, 0f),
            floatArrayOf(-sin, 0f, cos)
        )
    )
}