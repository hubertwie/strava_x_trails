package com.example.stravaxszlaki.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint

fun createBlueDotBitmap(size: Int = 150): Bitmap {
    val center = size / 2f
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply { isAntiAlias = true; style = Paint.Style.FILL }

    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(center, center, 22f, paint)

    paint.color = android.graphics.Color.parseColor("#2196F3")
    canvas.drawCircle(center, center, 16f, paint)
    return bitmap
}

fun createDirectionalDotBitmap(size: Int = 150): Bitmap {
    val center = size / 2f
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply { isAntiAlias = true; style = Paint.Style.FILL }

    // 1. Zwykła kropka
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(center, center, 22f, paint)
    paint.color = android.graphics.Color.parseColor("#2196F3")
    canvas.drawCircle(center, center, 16f, paint)

    // 2. Odłączona strzałka
    val path = android.graphics.Path().apply {
        moveTo(center, 8f)
        lineTo(center - 18f, 38f)
        quadTo(center, 28f, center + 18f, 38f)
        close()
    }
    canvas.drawPath(path, paint)

    return bitmap
}