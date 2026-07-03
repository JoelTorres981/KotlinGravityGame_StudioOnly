package com.epn.gravitygame

import android.graphics.RectF
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

object Collision {
    fun circleWithCircle(a: Vector2, ar: Float, b: Vector2, br: Float): Boolean {
        val dx = a.x - b.x
        val dy = a.y - b.y
        val distance = sqrt(dx.pow(2) + dy.pow(2))
        return distance <= ar + br
    }

    fun circleWithRect(center: Vector2, radius: Float, rect: RectF): Boolean {
        val nearestX = max(rect.left, min(center.x, rect.right))
        val nearestY = max(rect.top, min(center.y, rect.bottom))
        val dx = center.x - nearestX
        val dy = center.y - nearestY
        return dx * dx + dy * dy <= radius * radius
    }
}
