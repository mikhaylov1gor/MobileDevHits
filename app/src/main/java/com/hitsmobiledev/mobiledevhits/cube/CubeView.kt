package com.hitsmobiledev.mobiledevhits.cube

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import Cube.*
import android.util.Log

class CubeView (
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    private var cube: Cube = Cube()
    private val paint = Paint()
    private var cumulativeRotationX: Float = 0f
    private var cumulativeRotationY: Float = 0f
    private var scaleFactor: Float  = 1f
    private var previousX: Float  = 0f
    private var previousY: Float  = 0f
    private val colors = listOf(
        Color.rgb(0, 255, 255),
        Color.rgb(128, 0, 128),
        Color.rgb(255, 0, 0),
        Color.rgb(0, 255, 0),
        Color.rgb(255, 255, 0),
        Color.rgb(255, 0, 255)
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val rotationX: Matrix3x3 = rotationMatrixX(cumulativeRotationX)
        val rotationY: Matrix3x3 = rotationMatrixY(cumulativeRotationY)
        val rotationMatrix: Matrix3x3 = rotationY * rotationX

        val rotatedFaces: List<Face> = cube.rotate(rotationMatrix)
        for (face in rotatedFaces) {
            val centerZ = calculateCenterZ(face.vertices)
            if (centerZ > 0) {
                val path = Path()
                val firstVertex = face.vertices[0]
                path.moveTo(
                    firstVertex.x * scaleFactor + width / 2,
                    firstVertex.y * scaleFactor + height / 2
                )

                for (vertex in face.vertices) {
                    path.lineTo(
                        vertex.x * scaleFactor + width / 2,
                        vertex.y * scaleFactor + height / 2
                    )
                }
                path.close()

                paint.style = Paint.Style.FILL
                paint.color = colors[face.number - 1]
                canvas.drawPath(path, paint)

                paint.style = Paint.Style.STROKE
                paint.color = Color.BLACK
                paint.strokeWidth = 5f
                canvas.drawPath(path, paint)
            }
        }
    }

    private fun calculateCenterZ(vertices: List<Point>) : Float{
        var result = 0f
        for (vertex in vertices){
            result += vertex.z
        }
        result *= scaleFactor
        result /= 4
        return result
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.pointerCount == 2) {
            scaleGestureDetector.onTouchEvent(event)
        }

        if (event.pointerCount == 1 && !scaleGestureDetector.isInProgress) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    previousX = event.x / 500
                    previousY = event.y / 500
                }

                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.x / 500 - previousX
                    val deltaY = event.y / 500 - previousY
                    rotateCube(deltaX, deltaY)
                    previousX = event.x / 500
                    previousY = event.y / 500
                }
            }
        }

        return true
    }

    private fun rotateCube(deltaX: Float, deltaY: Float) {
        cumulativeRotationX -= deltaY
        cumulativeRotationY += deltaX
        invalidate()
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = scaleFactor.coerceIn(0.5f, 4.0f)
            invalidate()
            return true
        }
    }
}