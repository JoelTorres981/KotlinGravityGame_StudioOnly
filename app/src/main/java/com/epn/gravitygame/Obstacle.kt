package com.epn.gravitygame

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

class Obstacle(
    val rect: RectF
) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(239, 68, 68)
    }

    fun draw(canvas: Canvas) {
        canvas.drawRoundRect(rect, 24f, 24f, paint)
    }
}
