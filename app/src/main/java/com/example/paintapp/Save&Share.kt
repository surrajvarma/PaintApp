package com.example.paintapp.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.paintapp.MainViewModel
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color
import java.io.File



fun saveImage(
    context: Context,
    viewModel: MainViewModel,
    width: Int,
    height: Int
) {

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.White.toArgb())

    viewModel.strokes.forEach { stroke ->
        val paint = Paint().apply {
            color = stroke.color.copy(alpha = stroke.strokeOpacity).toArgb()
            strokeWidth = stroke.strokeWidth
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        canvas.drawPath(stroke.path.asAndroidPath(), paint)
    }

    val filename = "Paint_${System.currentTimeMillis()}.png"
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PaintApp")
    }

    val uri = context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        values
    )

    uri?.let {
        context.contentResolver.openOutputStream(it)?.use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
        Toast.makeText(context, "Saved to Gallery ✓", Toast.LENGTH_SHORT).show()
    }
}





fun shareImage(
    context: Context,
    viewModel: MainViewModel,
    width: Int,
    height: Int
) {

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.White.toArgb())

    viewModel.strokes.forEach { stroke ->
        val paint = Paint().apply {
            color = stroke.color.copy(alpha = stroke.strokeOpacity).toArgb()
            strokeWidth = stroke.strokeWidth
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        canvas.drawPath(stroke.path.asAndroidPath(), paint)
    }


    val file = File(context.cacheDir, "PaintShare_${System.currentTimeMillis()}.png")
    file.outputStream().use {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
    }

    val uri = FileProvider.getUriForFile(
        context,
        context.packageName + ".fileprovider",
        file
    )

    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(android.content.Intent.EXTRA_STREAM, uri)
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(
        android.content.Intent.createChooser(shareIntent, "Share Image")
    )
}
