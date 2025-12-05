package com.example.paintapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val _strokes = mutableStateListOf<Stroke>()
    val strokes: List<Stroke> get() = _strokes

    var currentColor by mutableStateOf(Color.Black)
    var currentStrokeWidth by mutableStateOf(10f)
    var currentOpacity by mutableStateOf(1f)
    var isErasing by mutableStateOf(false)
    private val undoStack = mutableListOf<Stroke>()


    // Start stroke
    fun startStroke(offset: Offset) {
        val path = Path().apply {
            moveTo(offset.x, offset.y)
            // a tiny movement makes first dot visible
            lineTo(offset.x + 0.1f, offset.y + 0.1f)
        }

        _strokes.add(
            Stroke(
                path = path,
                color = if (isErasing) Color.White else currentColor,
                strokeWidth = if (isErasing) 50f else currentStrokeWidth,
                strokeOpacity = currentOpacity
            )
        )
    }


    fun continueStroke(offset: Offset) {
        if (_strokes.isNotEmpty()) {
            val lastIndex = _strokes.lastIndex
            val old = _strokes[lastIndex]

            val newPath = Path().apply {
                addPath(old.path)
                lineTo(offset.x, offset.y)
            }

            _strokes[lastIndex] = old.copy(path = newPath)
        }
    }


    fun addDot(offset: Offset) {
        val path = Path().apply {
            moveTo(offset.x, offset.y)
            lineTo(offset.x + 0.1f, offset.y + 0.1f)
        }

        _strokes.add(
            Stroke(
                path = path,
                color = if (isErasing) Color.White else currentColor,
                strokeWidth = if (isErasing) 50f else currentStrokeWidth,
                strokeOpacity = currentOpacity
            )
        )
    }

    fun clearCanvas() {
        _strokes.clear()
    }

    fun undo(){
        if(_strokes.isNotEmpty()){
            val removed = _strokes.removeLast()
            undoStack.add(removed)
        }
    }
    fun redo(){
        if (undoStack.isNotEmpty()) {
            val restored = undoStack.removeLast()
            _strokes.add(restored)
        }

    }
}
