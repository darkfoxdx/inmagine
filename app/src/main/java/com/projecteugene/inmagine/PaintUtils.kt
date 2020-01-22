package com.projecteugene.inmagine

import android.graphics.Paint

/**
 * Created by Eugene Low
 */
object PaintUtils {
    fun fillPaint(colorValue: Int) = Paint().apply {
        style = Paint.Style.FILL
        color = colorValue
    }

    fun strokePaint(colorValue: Int) = Paint().apply {
        style = Paint.Style.STROKE
        color = colorValue
    }

    fun imagePaint() = Paint().apply {
        isAntiAlias = true
    }
}