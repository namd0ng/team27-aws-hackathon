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
    imageWidth: Int,
    imageHeight: Int,
    displayWidth: Float,
    displayHeight: Float,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    
    Canvas(modifier = modifier.fillMaxSize()) {
        faces.forEach { face ->
            // 더 정확한 좌표 변환 - aspect ratio 유지하면서 중앙 정렬
            val imageAspectRatio = imageWidth.toFloat() / imageHeight.toFloat()
            val displayAspectRatio = displayWidth / displayHeight
            
            val (actualDisplayWidth, actualDisplayHeight, offsetX, offsetY) = if (imageAspectRatio > displayAspectRatio) {
                // 이미지가 더 넓음 - 가로를 맞추고 세로 중앙 정렬
                val scaledHeight = displayWidth / imageAspectRatio
                val yOffset = (displayHeight - scaledHeight) / 2
                arrayOf(displayWidth, scaledHeight, 0f, yOffset)
            } else {
                // 이미지가 더 높음 - 세로를 맞추고 가로 중앙 정렬
                val scaledWidth = displayHeight * imageAspectRatio
                val xOffset = (displayWidth - scaledWidth) / 2
                arrayOf(scaledWidth, displayHeight, xOffset, 0f)
            }
            
            // 얼굴 박스 좌표 계산 (정확한 비율 적용)
            val boxLeft = offsetX + (face.left * actualDisplayWidth)
            val boxTop = offsetY + (face.top * actualDisplayHeight)
            val boxWidth = face.width * actualDisplayWidth
            val boxHeight = face.height * actualDisplayHeight
            
            // 얼굴 박스 색상 결정
            val boxColor = when {
                face.drunkPercentage < 30 -> Color.Green
                face.drunkPercentage < 60 -> Color.Yellow
                else -> Color.Red
            }
            
            // 얼굴 박스 그리기 (2pt 얇은 선)
            drawRect(
                color = boxColor,
                topLeft = Offset(boxLeft, boxTop),
                size = Size(boxWidth, boxHeight),
                style = Stroke(width = 2.dp.toPx())
            )
            
            // 라벨 텍스트 (Person ID + 퍼센트)
            val labelText = "${face.personId}: ${face.drunkPercentage}%"
            val textStyle = TextStyle(
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            
            val textLayoutResult = textMeasurer.measure(labelText, textStyle)
            val textWidth = textLayoutResult.size.width
            val textHeight = textLayoutResult.size.height
            
            // 텍스트 배경 박스 (박스 위쪽에 배치)
            val textBgLeft = boxLeft
            val textBgTop = boxTop - textHeight - 8.dp.toPx()
            val textBgWidth = textWidth + 12.dp.toPx()
            val textBgHeight = textHeight + 6.dp.toPx()
            
            // 화면 경계 체크
            val finalTextBgTop = if (textBgTop < 0) boxTop + boxHeight + 4.dp.toPx() else textBgTop
            
            drawRect(
                color = boxColor,
                topLeft = Offset(textBgLeft, finalTextBgTop),
                size = Size(textBgWidth, textBgHeight)
            )
            
            // 텍스트 그리기
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    textBgLeft + 6.dp.toPx(),
                    finalTextBgTop + 3.dp.toPx()
                ),
                color = Color.White
            )
        }
    }
}
