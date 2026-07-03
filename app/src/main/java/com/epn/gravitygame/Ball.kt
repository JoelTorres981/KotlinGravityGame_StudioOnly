package com.epn.gravitygame

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.max
import kotlin.math.min

class Ball(
    var position: Vector2 = Vector2(250f, 350f),
    private val radius: Float = 50f
) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(37, 99, 235)
    }

    private val shinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(190, 255, 255, 255)
    }

    fun radius(): Float = radius

    fun update(sensorX: Float, sensorY: Float, width: Int, height: Int) {
        position.x += sensorX * SPEED
        position.y += sensorY * SPEED

        position.x = max(radius, min(width - radius, position.x))
        position.y = max(radius, min(height - radius, position.y))
    }

    fun draw(canvas: Canvas) {
        canvas.drawCircle(position.x, position.y, radius, paint)
        canvas.drawCircle(position.x - radius / 3, position.y - radius / 3, radius / 4, shinePaint)
    }

    companion object {
        private const val SPEED = 7.5f
    }
}
