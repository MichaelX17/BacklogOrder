package com.backlogorder.ui.hud

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.backlogorder.domain.model.GameStatus
import com.backlogorder.domain.score.ScoreCalculator
import com.backlogorder.ui.theme.LocalHudColors
import java.util.Locale

private val tagBubbleWidth = 108.dp
private val tagBubbleHeight = 32.dp

class BevelCutShape(private val cornerCutSize: Float = 16f) : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): androidx.compose.ui.graphics.Outline {
        val path = Path().apply {
            moveTo(cornerCutSize, 0f)
            lineTo(size.width - cornerCutSize, 0f)
            lineTo(size.width, cornerCutSize)
            lineTo(size.width, size.height - cornerCutSize)
            lineTo(size.width - cornerCutSize, size.height)
            lineTo(cornerCutSize, size.height)
            lineTo(0f, size.height - cornerCutSize)
            lineTo(0f, cornerCutSize)
            close()
        }
        return androidx.compose.ui.graphics.Outline.Generic(path)
    }
}

class HexagonShape : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): androidx.compose.ui.graphics.Outline {
        val width = size.width
        val height = size.height
        val path = Path().apply {
            moveTo(width * 0.5f, 0f)
            lineTo(width, height * 0.25f)
            lineTo(width, height * 0.75f)
            lineTo(width * 0.5f, height)
            lineTo(0f, height * 0.75f)
            lineTo(0f, height * 0.25f)
            close()
        }
        return androidx.compose.ui.graphics.Outline.Generic(path)
    }
}

@Composable
fun BackIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHudColors.current
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp)),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = colors.navButtonBg,
            contentColor = colors.text,
        ),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back navigation",
            modifier = Modifier.size(24.dp),
            tint = colors.text,
        )
    }
}

@Composable
fun BevelFrame(
    modifier: Modifier = Modifier,
    backgroundColor: Color = LocalHudColors.current.panel,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        shadowElevation = 2.dp,
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun HexShape(
    modifier: Modifier = Modifier,
    color: Color = LocalHudColors.current.accent.copy(alpha = 0.3f),
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = HexagonShape(),
        color = color,
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

@Composable
fun BevelImage(
    model: Any,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val context = LocalContext.current
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(model)
            .build(),
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHudColors.current
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = colors.text,
        modifier = modifier.padding(vertical = 4.dp),
    )
}

@Composable
fun StatCard(
    value: Any,
    label: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHudColors.current
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp),
        shape = RoundedCornerShape(16.dp),
        color = colors.panel,
        shadowElevation = 2.dp,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 8.dp),
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = LocalHudColors.current
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.accent,
            contentColor = colors.text,
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun ScoreTag(score: Double?, modifier: Modifier = Modifier) {
    val colors = LocalHudColors.current
    val label = ScoreCalculator.scoreDisplay(score)
    Surface(
        modifier = modifier
            .height(tagBubbleHeight)
            .then(if (modifier == Modifier) Modifier.width(tagBubbleWidth) else Modifier),
        color = colors.accent.copy(alpha = 0.18f),
        contentColor = colors.text,
        shape = RoundedCornerShape(999.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Score $label",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp,
                ),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun ScoreTag(score: Int?, modifier: Modifier = Modifier) {
    ScoreTag(score = score?.toDouble(), modifier = modifier)
}

@Composable
fun TimeTag(playtimeHours: Double?, modifier: Modifier = Modifier) {
    val colors = LocalHudColors.current
    val label = if (playtimeHours != null && playtimeHours > 0.0) {
        String.format(Locale.US, "%.1fh", playtimeHours)
    } else {
        "—"
    }
    Surface(
        modifier = modifier
            .height(tagBubbleHeight)
            .then(if (modifier == Modifier) Modifier.width(tagBubbleWidth) else Modifier),
        color = colors.accent.copy(alpha = 0.14f),
        contentColor = colors.text,
        shape = RoundedCornerShape(999.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp,
                ),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun StatusBadge(status: GameStatus, modifier: Modifier = Modifier) {
    val colors = LocalHudColors.current
    val tint = when (status) {
        GameStatus.BACKLOG -> colors.warning
        GameStatus.PLAYING -> colors.success
        GameStatus.COMPLETED -> colors.accent
        GameStatus.DROPPED -> colors.danger
    }
    Surface(
        modifier = modifier
            .height(tagBubbleHeight)
            .then(if (modifier == Modifier) Modifier.width(tagBubbleWidth) else Modifier),
        color = tint.copy(alpha = 0.16f),
        contentColor = colors.text,
        shape = RoundedCornerShape(999.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = status.name,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp,
                ),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    BevelFrame(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineSmall)
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
            if (action != null) {
                action()
            }
        }
    }
}

@Composable
fun GameCard(
    title: String,
    status: GameStatus,
    score: Double? = null,
    meta: String = "",
    imageUrl: String? = null,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color? = null,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHudColors.current
    val cardBg = backgroundColor ?: colors.surface
    BevelFrame(
        backgroundColor = cardBg,
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!imageUrl.isNullOrBlank()) {
                BevelImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp)),
                )
            } else {
                HexShape(modifier = Modifier.size(56.dp), color = colors.accent.copy(alpha = 0.2f)) {
                    Text(text = title.firstOrNull()?.uppercaseChar()?.toString() ?: "?", color = colors.text)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (meta.isNotBlank()) {
                    Text(
                        text = meta,
                        modifier = Modifier.padding(top = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.text.copy(alpha = 0.7f),
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(status = status)
                ScoreTag(score = score)
            }
        }
    }
}

@Composable
fun GameCard(
    title: String,
    status: GameStatus,
    score: Int?,
    meta: String = "",
    imageUrl: String? = null,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color? = null,
    modifier: Modifier = Modifier,
) {
    GameCard(
        title = title,
        status = status,
        score = score?.toDouble(),
        meta = meta,
        imageUrl = imageUrl,
        onClick = onClick,
        backgroundColor = backgroundColor,
        modifier = modifier,
    )
}
