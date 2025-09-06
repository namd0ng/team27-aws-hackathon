package com.hackathon.alcolook

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FaceDetectionOverlay(
    faces: List<FaceBox>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        faces.forEach { face ->
            // 직접 비율 좌표를 캔버스 크기에 맞춤
            val boxLeft = face.left * canvasWidth
            val boxTop = face.top * canvasHeight
            val boxWidth = face.width * canvasWidth
            val boxHeight = face.height * canvasHeight
            
            // 얼굴 박스 색상 결정
            val boxColor = when {
                face.drunkPercentage < 30 -> Color(0xFF4CAF50) // Green
                face.drunkPercentage < 60 -> Color(0xFFFF9800) // Orange
                else -> Color(0xFFF44336) // Red
            }
            
            // 얼굴 박스 그리기 (3dp 두꺼운 선)
            drawRect(
                color = boxColor,
                topLeft = Offset(boxLeft, boxTop),
                size = Size(boxWidth, boxHeight),
                style = Stroke(width = 3.dp.toPx())
            )
            
            // 라벨 텍스트 (퍼센트만 표시)
            val labelText = "${face.drunkPercentage}%"
            val textStyle = TextStyle(
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            
            val textLayoutResult = textMeasurer.measure(labelText, textStyle)
            val textWidth = textLayoutResult.size.width.toFloat()
            val textHeight = textLayoutResult.size.height.toFloat()
            
            // 텍스트 배경 박스 (박스 위쪽에 배치)
            val padding = 8.dp.toPx()
            val textBgLeft = boxLeft
            val textBgTop = maxOf(0f, boxTop - textHeight - padding * 2)
            val textBgWidth = textWidth + padding * 2
            val textBgHeight = textHeight + padding
            
            // 배경 박스 그리기
            drawRect(
                color = boxColor,
                topLeft = Offset(textBgLeft, textBgTop),
                size = Size(textBgWidth, textBgHeight)
            )
            
            // 텍스트 그리기
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    textBgLeft + padding,
                    textBgTop + padding / 2
                )
            )
        }
    }
}
