package com.epn.gravitygame

data class Vector2(
    var x: Float = 0f,
    var y: Float = 0f
) {
    fun set(newX: Float, newY: Float) {
        x = newX
        y = newY
    }
}
