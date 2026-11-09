package com.backlogorder.ui.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.backlogorder.ui.theme.LocalHudColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@Composable
fun InteractiveCropDialog(
    imageUri: Uri,
    onDismiss: () -> Unit,
    onCropConfirmed: (String) -> Unit,
) {
    val context = LocalContext.current
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(imageUri) {
        withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
            } catch (e: Exception) {
                originalBitmap = null
            }
        }
    }

    if (originalBitmap == null) {
        Dialog(onDismissRequest = onDismiss) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
        return
    }

    val bitmap = originalBitmap!!

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    }
                    Text(
                        text = "Crop Cover (1:1)",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    TextButton(
                        onClick = {
                            val croppedPath = performBitmapCrop(
                                context = context,
                                bitmap = bitmap,
                                scale = scale,
                                offsetX = offsetX,
                                offsetY = offsetY,
                            )
                            if (croppedPath != null) {
                                onCropConfirmed(croppedPath)
                            }
                            onDismiss()
                        },
                    ) {
                        Text("Done", color = LocalHudColors.current.accent, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }

                // Interactive Viewport Area
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    val viewportWidthPx = constraints.maxWidth.toFloat()
                    val viewportHeightPx = constraints.maxHeight.toFloat()
                    val squareSizePx = minOf(viewportWidthPx, viewportHeightPx) * 0.85f

                    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
                        scale = (scale * zoomChange).coerceIn(1f, 5f)
                        val maxOffset = (squareSizePx * (scale - 1f)) / 2f
                        offsetX = (offsetX + offsetChange.x).coerceIn(-maxOffset, maxOffset)
                        offsetY = (offsetY + offsetChange.y).coerceIn(-maxOffset, maxOffset)
                    }

                    Box(
                        modifier = Modifier
                            .size(with(LocalDensity.current) { squareSizePx.toDp() })
                            .clip(RoundedCornerShape(12.dp))
                            .border(2.dp, Color.White, RoundedCornerShape(12.dp))
                            .transformable(state = transformableState),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Crop target",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offsetX,
                                    translationY = offsetY,
                                ),
                        )
                    }
                }

                Text(
                    text = "Pinch to zoom • Drag to position cover",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            }
        }
    }
}

private fun performBitmapCrop(
    context: Context,
    bitmap: Bitmap,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
): String? {
    return try {
        val minDim = minOf(bitmap.width, bitmap.height)
        val scaledDim = (minDim / scale).toInt().coerceIn(1, minDim)

        val normalizedOffsetX = (-offsetX / scale).toInt()
        val normalizedOffsetY = (-offsetY / scale).toInt()

        val baseX = (bitmap.width - scaledDim) / 2 + normalizedOffsetX
        val baseY = (bitmap.height - scaledDim) / 2 + normalizedOffsetY

        val cropX = baseX.coerceIn(0, (bitmap.width - scaledDim).coerceAtLeast(0))
        val cropY = baseY.coerceIn(0, (bitmap.height - scaledDim).coerceAtLeast(0))

        val croppedBitmap = Bitmap.createBitmap(bitmap, cropX, cropY, scaledDim, scaledDim)
        val finalBitmap = Bitmap.createScaledBitmap(croppedBitmap, 512, 512, true)

        val coverDir = File(context.filesDir, "covers").apply { if (!exists()) mkdirs() }
        val coverFile = File(coverDir, "cover_${System.currentTimeMillis()}.jpg")
        FileOutputStream(coverFile).use { out ->
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }

        Uri.fromFile(coverFile).toString()
    } catch (e: Exception) {
        null
    }
}
