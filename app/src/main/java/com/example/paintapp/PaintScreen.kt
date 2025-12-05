package com.example.paintapp


import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.paintapp.utils.saveImage
import com.example.paintapp.utils.shareImage
import com.github.skydoves.colorpicker.compose.ColorPickerController
import com.github.skydoves.colorpicker.compose.HsvColorPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaintScreen(viewModel: MainViewModel = viewModel()) {

    val gradient = Brush.linearGradient(
        colors = listOf(Color.Magenta, Color.Blue)
    )

    val showBrushSheet = remember { mutableStateOf(false) }
    val showColorSheet = remember { mutableStateOf(false) }
    val showOpacitySheet = remember { mutableStateOf(false) }
    val controller = remember { ColorPickerController() }
    val context = LocalContext.current
    val config = LocalConfiguration.current
    val density = LocalDensity.current

    val screenWidthPx = with(density) { config.screenWidthDp.dp.toPx().toInt() }
    val screenHeightPx = with(density) { config.screenHeightDp.dp.toPx().toInt() }
    val showExtraBottomSheet = remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),

        topBar = {
            Box(Modifier.background(gradient)) {
                TopAppBar(
                    title = {
                        Row () {
                            Icon(
                                painter = painterResource(id = R.drawable.icons8_color_40),
                                contentDescription = "",
                                modifier = Modifier.size(30.dp),
                                tint = Color.Yellow

                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Paint",
                                color = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    actions = {
                        Row (
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Row(
                                modifier = Modifier
                                    .background(
                                        Color(0xFF939393).copy(alpha = 0.7f),
                                        RoundedCornerShape(20.dp)
                                    )

                            ){
                                IconButton(
                                    onClick = {
                                        viewModel.undo()
                                    }
                                ){
                                    Icon(
                                        painter = painterResource(R.drawable.icons8_undo_60) ,
                                        contentDescription = "",
                                        tint= Color.White,
                                        )
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.redo()
                                    }
                                ){
                                    Icon(
                                        painter = painterResource(R.drawable.icons8_redo_60) ,
                                        contentDescription = "",
                                        tint= Color.White
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    viewModel.clearCanvas()
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.icons8_delete_48),
                                    contentDescription = "",
                                    modifier= Modifier.size(20.dp),
                                    tint = Color.White

                                )
                            }
                            IconButton(
                                onClick = {
                                    showExtraBottomSheet.value = true

                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Save&Share",
                                    modifier= Modifier.size(20.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                )
            }
        },

        bottomBar = {
            Box(Modifier.background(gradient)) {
                NavigationBar {

                    NavigationBarItem(
                        selected = false,
                        onClick = { viewModel.isErasing = false },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.icons8_brush_50),
                                contentDescription = "",
                                modifier= Modifier.background(gradient).size(20.dp),
                                tint = Color.White

                            )
                        },
                        label = { Text("Brush", color = Color.White) }
                    )

                    NavigationBarItem(
                        selected = false,
                        onClick = { viewModel.isErasing = true },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.icons8_eraser_48),
                                contentDescription = "",
                                modifier= Modifier.background(gradient).size(20.dp),
                                tint = Color.White

                            )
                        },
                        label = { Text("Eraser", color = Color.White) }
                    )

                    Column (
                        modifier = Modifier.align(Alignment.CenterVertically).padding(horizontal = 20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(gradient, CircleShape)
                                .clickable { showColorSheet.value = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(viewModel.currentColor, CircleShape)
                            )
                        }
                        Text(
                            text = "Color",
                            color = Color.White
                        )
                    }

                    NavigationBarItem(
                        selected = false,
                        onClick = { showOpacitySheet.value = true },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.icons8_opacity_64),
                                contentDescription = "",
                                modifier= Modifier.background(gradient).size(20.dp),
                                tint = Color.White

                            )
                        },
                        label = { Text("Opacity", color = Color.White) }
                    )

                    NavigationBarItem(
                        selected = false,
                        onClick = { showBrushSheet.value = true },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.icons8_line_width_50),
                                contentDescription = "",
                                modifier= Modifier.background(gradient).size(20.dp),
                                tint = Color.White

                            )
                        },
                        label = { Text("Width", color = Color.White) }
                    )
                }
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()


                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            viewModel.addDot(offset)
                        }
                    }


                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                viewModel.startStroke(offset)
                            },
                            onDrag = { change, _ ->
                                viewModel.continueStroke(change.position)
                            }
                        )
                    }
            ) {
                viewModel.strokes.forEach { stroke ->
                    drawPath(
                        path = stroke.path,
                        color = stroke.color.copy(alpha=stroke.strokeOpacity),
                        style = Stroke(
                            stroke.strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )

                    )
                }
            }
        }

        if(showExtraBottomSheet.value){
            ModalBottomSheet(
                onDismissRequest = { showExtraBottomSheet.value = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(
                    Modifier.wrapContentSize(),
                ){
                    Row(Modifier.clickable(onClick = {
                        saveImage(context, viewModel, screenWidthPx, screenHeightPx)
                    }),
                        verticalAlignment = Alignment.CenterVertically){
                        Icon(
                            painter = painterResource(R.drawable.icons8_download_50),
                            contentDescription = "Save",
                            Modifier.padding(8.dp).size(20.dp)
                        )
                        Text("Save Image", fontSize = 20.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.clickable(onClick = {
                        shareImage(context, viewModel, screenWidthPx, screenHeightPx)
                    }),
                    verticalAlignment = Alignment.CenterVertically){
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            Modifier.padding(8.dp).size(20.dp)
                        )
                        Text("Share", fontSize = 20.sp)
                    }
                }
            }
        }

        if(showOpacitySheet.value){
            ModalBottomSheet(
                onDismissRequest = {showOpacitySheet.value=false}
            ) {
                Slider(
                    value = viewModel.currentOpacity,
                    onValueChange = {viewModel.currentOpacity=it},
                    valueRange = 0f..1f,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        if (showBrushSheet.value) {
            ModalBottomSheet(
                onDismissRequest = { showBrushSheet.value = false }
            ) {
                Slider(
                    value = viewModel.currentStrokeWidth,
                    onValueChange = { viewModel.currentStrokeWidth = it },
                    valueRange = 5f..50f,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        if(showColorSheet.value){
            ModalBottomSheet(
                onDismissRequest = { showColorSheet.value = false }
            ) {
                HsvColorPicker(
                    modifier = Modifier
                        .height(200.dp)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    onColorChanged = { colorEnvelope ->
                        viewModel.currentColor = colorEnvelope.color
                    },
                    controller = controller
                )
                Box(
                    modifier = Modifier
                        .size(55.dp)
                        .background(controller.selectedColor.value, CircleShape)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(15.dp))
                Button(
                    onClick = {showColorSheet.value=false},
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text("Select Color")
                }
            }
        }
    }
}
