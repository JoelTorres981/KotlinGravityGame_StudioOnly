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
        resetGame()
    }

    private fun resetGame() {
        score = 0
        lives = 3
        gameOver = false
        started = false
        ball.position.set(width / 2f, height / 2f)
        target.relocate(width, height)
        createObstacles()
        invalidate()
    }

    private fun createObstacles() {
        obstacles.clear()
        if (width == 0 || height == 0) return
        obstacles.add(Obstacle(RectF(width * 0.18f, height * 0.34f, width * 0.48f, height * 0.39f)))
        obstacles.add(Obstacle(RectF(width * 0.55f, height * 0.55f, width * 0.86f, height * 0.60f)))
        obstacles.add(Obstacle(RectF(width * 0.25f, height * 0.73f, width * 0.62f, height * 0.78f)))
        // Nuevos obstáculos para el nivel intermedio
        obstacles.add(Obstacle(RectF(width * 0.10f, height * 0.20f, width * 0.40f, height * 0.24f)))
        obstacles.add(Obstacle(RectF(width * 0.60f, height * 0.85f, width * 0.90f, height * 0.89f)))
    }

    private fun updateGame() {
        ball.update(sensorX, sensorY, width, height)

        // Detectar choque con bordes
        val isTouchingBorder = ball.position.x <= ball.radius() || 
                             ball.position.x >= width - ball.radius() ||
                             ball.position.y <= ball.radius() || 
                             ball.position.y >= height - ball.radius()
        
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
            target.relocate(width, height)
            vibrate(35)
        }

        obstacles.forEach { obstacle ->
            if (Collision.circleWithRect(ball.position, ball.radius(), obstacle.rect)) {
                lives--
                ball.position.set(width / 2f, height / 2f)
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

            // Instrucciones de juego en la parte inferior
            canvas.drawText("Instrucciones: Mueve la bola azul al objetivo verde esquivando los obstáculos rojos.", 24f, height - 40f, smallPaint)

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
        canvas.drawRoundRect(24f, 24f, width - 24f, 138f, 28f, 28f, panelPaint)
        canvas.drawText("Gravity Ball Kotlin", 48f, 72f, titlePaint)
        canvas.drawText("Puntaje: $score   Record: $highScore   Vidas: $lives", 48f, 116f, textPaint)
        canvas.drawText("Sensor X: ${sensorX.roundToInt()}  Y: ${sensorY.roundToInt()}", width - 320f, 72f, smallPaint)
        canvas.drawText("Bola X: ${ball.position.x.roundToInt()}  Y: ${ball.position.y.roundToInt()}", width - 320f, 116f, smallPaint)
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
