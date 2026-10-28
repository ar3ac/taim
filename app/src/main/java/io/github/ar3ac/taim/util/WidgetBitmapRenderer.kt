package io.github.ar3ac.taim.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import io.github.ar3ac.taim.R

/**
 * Renders text to high-DPI Bitmap using app-bundled Typefaces.
 * This completely bypasses third-party launcher (e.g. Nova Launcher) limitations,
 * ensuring the widget matches the in-app preview with 100% pixel perfection.
 */
object WidgetBitmapRenderer {

    fun createTextBitmap(
        context: Context,
        text: String,
        fontResId: Int,
        textSizeSp: Float,
        textColor: Int = android.graphics.Color.WHITE,
        letterSpacing: Float = 0f
    ): Bitmap {
        val density = context.resources.displayMetrics.scaledDensity
        val textSizePx = textSizeSp * density

        val typeface = ResourcesCompat.getFont(context, fontResId) ?: Typeface.DEFAULT
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            this.typeface = typeface
            this.textSize = textSizePx
            this.color = textColor
            this.letterSpacing = letterSpacing
            this.isFilterBitmap = true
            style = Paint.Style.FILL
        }

        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)

        val fontMetrics = paint.fontMetricsInt
        val textWidth = paint.measureText(text).toInt()
        val width = maxOf(textWidth, bounds.width(), 1) + 4
        val height = maxOf(fontMetrics.descent - fontMetrics.ascent, 1) + 4

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val x = 2f
        val y = -fontMetrics.ascent.toFloat() + 2f
        canvas.drawText(text, x, y, paint)

        return bitmap
    }
}
