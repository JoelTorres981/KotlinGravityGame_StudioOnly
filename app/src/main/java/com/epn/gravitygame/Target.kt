package com.epn.gravitygame

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.random.Random

class Target(
    var position: Vector2 = Vector2(650f, 650f),
    private val radius: Float = 36f
) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(34, 197, 94)
    }

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(22, 101, 52)
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    fun radius(): Float = radius

    fun relocate(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        val margin = 90
        position.x = Random.nextInt(margin, width - margin).toFloat()
        position.y = Random.nextInt(margin + 120, height - margin).toFloat()
    }

    fun draw(canvas: Canvas) {
        canvas.drawCircle(position.x, position.y, radius, paint)
        canvas.drawCircle(position.x, position.y, radius + 10f, ringPaint)
    }
}
