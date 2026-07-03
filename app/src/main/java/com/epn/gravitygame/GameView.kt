package com.epn.gravitygame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MotionEvent
import android.view.View
import kotlin.math.roundToInt

class GameView(context: Context) : View(context) {

    private val ball = Ball()
    private val target = Target()
    private val obstacles = mutableListOf<Obstacle>()

    private var sensorX = 0f
    private var sensorY = 0f
    private var score = 0
    private var lives = 3
    private var started = false
    private var gameOver = false
    private var wasTouchingBorder = false

    private val sharedPrefs = context.getSharedPreferences("gravity_ball_prefs", Context.MODE_PRIVATE)
    private var highScore = 0

    init {
        highScore = sharedPrefs.getInt("high_score", 0)
    }

    private val instructionsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(71, 85, 105)
        textSize = 30f
        isFakeBoldText = true
    }

    private fun getTopBoundary(): Float = 200f
    private fun getBottomBoundary(): Float = height - 200f

    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(15, 23, 42)
        textSize = 48f
        isFakeBoldText = true
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(51, 65, 85)
        textSize = 32f
    }

    private val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(100, 116, 139)
        textSize = 24f
    }

    private val panelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(235, 255, 255, 255)
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(226, 232, 240)
        strokeWidth = 3f
    }

    fun updateSensorValues(x: Float, y: Float) {
        if (!started || gameOver) return
        sensorX = x
        sensorY = y
        updateGame()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        adjustPaints()
        resetGame()
    }

    private fun adjustPaints() {
        if (width == 0) return
        val scale = width / 1080f
        titlePaint.textSize = 48f * scale
        textPaint.textSize = 32f * scale
        smallPaint.textSize = 24f * scale
        instructionsPaint.textSize = kotlin.math.max(26f, kotlin.math.min(34f, 32f * scale))
    }

    private fun getSafeSpawnPosition(): Vector2 {
        val defaultSpawn = Vector2(width / 2f, height / 2f)
        if (obstacles.isEmpty()) return defaultSpawn

        val top = getTopBoundary()
        val playH = getBottomBoundary() - top
        val candidates = listOf(
            Vector2(width / 2f, top + playH * 0.5f),
            Vector2(width * 0.25f, top + playH * 0.15f),
            Vector2(width * 0.75f, top + playH * 0.15f),
            Vector2(width * 0.25f, top + playH * 0.5f),
            Vector2(width * 0.75f, top + playH * 0.5f),
            Vector2(width * 0.2f, top + playH * 0.8f),
            Vector2(width * 0.8f, top + playH * 0.8f)
        )

        var bestCandidate = defaultSpawn
        var maxDistance = -1f

        val ballRadius = ball.radius()
        val safeMargin = ballRadius + 60f

        for (candidate in candidates) {
            var minDistanceToAnyObstacle = Float.MAX_VALUE
            var collides = false

            for (obstacle in obstacles) {
                val closestX = kotlin.math.max(obstacle.rect.left, kotlin.math.min(candidate.x, obstacle.rect.right))
                val closestY = kotlin.math.max(obstacle.rect.top, kotlin.math.min(candidate.y, obstacle.rect.bottom))
                
                val dx = candidate.x - closestX
                val dy = candidate.y - closestY
                val dist = kotlin.math.sqrt(dx * dx + dy * dy)

                if (dist < safeMargin) {
                    collides = true
                }
                if (dist < minDistanceToAnyObstacle) {
                    minDistanceToAnyObstacle = dist
                }
            }

            if (!collides) {
                if (minDistanceToAnyObstacle > maxDistance) {
                    maxDistance = minDistanceToAnyObstacle
                    bestCandidate = candidate
                }
            }
        }

        return bestCandidate
    }

    private fun resetGame() {
        score = 0
        lives = 3
        gameOver = false
        started = false
        createObstacles()
        val spawnPos = getSafeSpawnPosition()
        ball.position.set(spawnPos.x, spawnPos.y)
        target.relocate(width, height, getTopBoundary(), getBottomBoundary())
        invalidate()
    }

    private fun createObstacles() {
        obstacles.clear()
        if (width == 0 || height == 0) return
        val top = getTopBoundary()
        val playH = getBottomBoundary() - top
        
        obstacles.add(Obstacle(RectF(width * 0.18f, top + playH * 0.22f, width * 0.48f, top + playH * 0.28f)))
        obstacles.add(Obstacle(RectF(width * 0.55f, top + playH * 0.45f, width * 0.86f, top + playH * 0.51f)))
        obstacles.add(Obstacle(RectF(width * 0.25f, top + playH * 0.68f, width * 0.62f, top + playH * 0.74f)))
        // Obstáculos adicionales adaptados
        obstacles.add(Obstacle(RectF(width * 0.10f, top + playH * 0.05f, width * 0.40f, top + playH * 0.11f)))
        obstacles.add(Obstacle(RectF(width * 0.60f, top + playH * 0.82f, width * 0.90f, top + playH * 0.88f)))
    }

    private fun updateGame() {
        val topBoundary = getTopBoundary()
        val bottomBoundary = getBottomBoundary()
        ball.update(sensorX, sensorY, width, height, topBoundary, bottomBoundary)

        // Detectar choque con bordes usando las nuevas fronteras de juego
        val isTouchingBorder = ball.position.x <= ball.radius() || 
                             ball.position.x >= width - ball.radius() ||
                             ball.position.y <= topBoundary + ball.radius() || 
                             ball.position.y >= bottomBoundary - ball.radius()
        
        if (isTouchingBorder) {
            if (!wasTouchingBorder) {
                vibrate(40)
                wasTouchingBorder = true
            }
        } else {
            wasTouchingBorder = false
        }

        if (Collision.circleWithCircle(ball.position, ball.radius(), target.position, target.radius())) {
            score += 10
            if (score > highScore) {
                highScore = score
                sharedPrefs.edit().putInt("high_score", highScore).apply()
            }
            target.relocate(width, height, topBoundary, bottomBoundary)
            vibrate(35)
        }

        obstacles.forEach { obstacle ->
            if (Collision.circleWithRect(ball.position, ball.radius(), obstacle.rect)) {
                lives--
                val spawnPos = getSafeSpawnPosition()
                ball.position.set(spawnPos.x, spawnPos.y)
                vibrate(120)
                if (lives <= 0) {
                    gameOver = true
                }
                return@forEach
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBackground(canvas)
        
        if (!started) {
            drawStartOverlay(canvas)
        } else {
            drawHeader(canvas)
            target.draw(canvas)
            obstacles.forEach { it.draw(canvas) }
            ball.draw(canvas)

            // Instrucciones de juego en la parte inferior adaptables
            drawBottomInstructions(canvas)

            if (gameOver) drawGameOver(canvas)
        }
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawColor(Color.rgb(248, 250, 252))
        val step = 80
        var x = 0
        while (x < width) {
            canvas.drawLine(x.toFloat(), 0f, x.toFloat(), height.toFloat(), linePaint)
            x += step
        }
        var y = 0
        while (y < height) {
            canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), linePaint)
            y += step
        }
    }

    private fun drawHeader(canvas: Canvas) {
        val top = 60f
        canvas.drawRoundRect(24f, top, width - 24f, top + 120f, 28f, 28f, panelPaint)
        canvas.drawText("Gravity Ball Kotlin", 48f, top + 48f, titlePaint)
        canvas.drawText("Puntaje: $score   Record: $highScore   Vidas: $lives", 48f, top + 92f, textPaint)
        canvas.drawText("Sensor X: ${sensorX.roundToInt()}  Y: ${sensorY.roundToInt()}", width - 320f, top + 48f, smallPaint)
        canvas.drawText("Bola X: ${ball.position.x.roundToInt()}  Y: ${ball.position.y.roundToInt()}", width - 320f, top + 92f, smallPaint)
    }

    private fun drawBottomInstructions(canvas: Canvas) {
        val top = height - 180f
        canvas.drawRoundRect(24f, top, width - 24f, height - 30f, 28f, 28f, panelPaint)
        
        // Alinear al centro para centrar en el panel
        instructionsPaint.textAlign = Paint.Align.CENTER
        
        val centerX = width / 2f
        val textY = top + 60f
        
        drawWrappedText(
            canvas,
            "Instrucciones: Mueve la bola azul al objetivo verde esquivando los obstáculos rojos.",
            centerX,
            textY,
            width - 80f,
            instructionsPaint
        )
        
        instructionsPaint.textAlign = Paint.Align.LEFT
    }

    private fun drawWrappedText(canvas: Canvas, text: String, x: Float, y: Float, maxWidth: Float, paint: Paint) {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "${currentLine} ${word}"
            val testWidth = paint.measureText(testLine)
            if (testWidth <= maxWidth) {
                currentLine.append(if (currentLine.isEmpty()) word else " ${word}")
            } else {
                lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }

        var currentY = y
        val spacing = paint.textSize * 1.3f
        for (line in lines) {
            canvas.drawText(line, x, currentY, paint)
            currentY += spacing
        }
    }

    private fun drawStartOverlay(canvas: Canvas) {
        // Pantalla de inicio dedicada y estética
        canvas.drawColor(Color.rgb(241, 245, 249)) // Fondo gris suave premium
        
        val centerX = width / 2f
        val startY = height * 0.3f
        
        // Alinear al centro para la presentación
        titlePaint.textAlign = Paint.Align.CENTER
        textPaint.textAlign = Paint.Align.CENTER
        smallPaint.textAlign = Paint.Align.CENTER
        
        canvas.drawText("GRAVITY BALL KOTLIN", centerX, startY, titlePaint)
        
        canvas.drawText("¡Bienvenido al juego!", centerX, startY + 80f, textPaint)
        canvas.drawText("Inclina tu teléfono para esquivar obstáculos", centerX, startY + 165f, textPaint)
        canvas.drawText("rojos y atrapar el objetivo verde.", centerX, startY + 215f, textPaint)
        canvas.drawText("Record actual: $highScore puntos", centerX, startY + 280f, smallPaint)
        
        // Botón Jugar
        val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(37, 99, 235) // Azul vibrante
        }
        val buttonTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 36f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        
        val btnLeft = centerX - 200f
        val btnTop = startY + 350f
        val btnRight = centerX + 200f
        val btnBottom = startY + 450f
        canvas.drawRoundRect(btnLeft, btnTop, btnRight, btnBottom, 20f, 20f, buttonPaint)
        canvas.drawText("JUGAR", centerX, btnTop + 62f, buttonTextPaint)
        
        // Restaurar alineación original del texto para el resto del juego
        titlePaint.textAlign = Paint.Align.LEFT
        textPaint.textAlign = Paint.Align.LEFT
        smallPaint.textAlign = Paint.Align.LEFT
    }

    private fun drawGameOver(canvas: Canvas) {
        val box = RectF(60f, height * 0.30f, width - 60f, height * 0.58f)
        canvas.drawRoundRect(box, 36f, 36f, panelPaint)
        canvas.drawText("Juego terminado", box.left + 50f, box.top + 90f, titlePaint)
        canvas.drawText("Puntaje: $score    Record Máximo: $highScore", box.left + 50f, box.top + 150f, textPaint)
        canvas.drawText("Toca para reiniciar", box.left + 50f, box.top + 205f, textPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (gameOver) {
                resetGame()
            } else {
                started = true
            }
            invalidate()
            return true
        }
        return true
    }

    private fun vibrate(milliseconds: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(milliseconds)
        }
    }
}
