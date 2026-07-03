# Kotlin Gravity Ball Game

Proyecto Android Studio en Kotlin para una clase taller de sensores.

## Qué hace

- Usa el acelerómetro del celular.
- Mueve una bolita con la inclinación del dispositivo.
- Tiene objetivos verdes, obstáculos rojos, puntaje, vidas y vibración.
- No usa dependencias externas para facilitar la compilación.

## Cómo abrirlo en Android Studio

1. Descomprime el ZIP.
2. Abre Android Studio.
3. Selecciona **File > Open**.
4. Elige la carpeta `KotlinGravityGame`.
5. Espera a que Android Studio sincronice Gradle.
6. Conecta un celular Android con depuración USB activada.
7. Presiona **Run**.

## Cómo generar APK

En Android Studio:

```bash
Build > Build Bundle(s) / APK(s) > Build APK(s)
```

El APK debug se genera normalmente en:

```bash
app/build/outputs/apk/debug/app-debug.apk
```

## Configuración técnica

- Kotlin
- Android Gradle Plugin 8.7.3
- Kotlin Plugin 2.0.21
- compileSdk 35
- targetSdk 35
- minSdk 24
- Java 17

## Archivos principales

```text
app/src/main/java/com/epn/gravitygame/
├── MainActivity.kt
├── GameView.kt
├── Ball.kt
├── Target.kt
├── Obstacle.kt
├── Collision.kt
└── Vector2.kt
```

## Ideas para extender en clase

- Agregar pantalla de inicio.
- Agregar niveles.
- Guardar récord con SharedPreferences.
- Agregar sonidos.
- Cambiar obstáculos dinámicamente.
- Agregar cuenta regresiva.
