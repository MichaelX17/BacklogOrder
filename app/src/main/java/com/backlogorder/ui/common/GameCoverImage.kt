package com.backlogorder.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.backlogorder.BacklogOrderApplication
import com.backlogorder.data.local.GameEntity
import com.backlogorder.domain.model.Game
import com.backlogorder.ui.theme.LocalHudColors
import com.backlogorder.util.CoverRefresher
import java.io.File

fun resolveCoverModel(
    localPath: String?,
    url: String?,
    gameId: String,
    refresher: CoverRefresher? = null,
): String? {
    val cleanLocal = localPath?.takeIf { it.isNotBlank() }
    val cleanUrl = url?.takeIf { it.isNotBlank() }

    if (cleanLocal == null) {
        return cleanUrl
    }

    return when {
        cleanLocal.startsWith("content://", ignoreCase = true) ||
            cleanLocal.startsWith("http://", ignoreCase = true) ||
            cleanLocal.startsWith("https://", ignoreCase = true) -> cleanLocal

        cleanLocal.startsWith("file://", ignoreCase = true) -> {
            val filePath = cleanLocal.substring(7)
            if (File(filePath).exists()) {
                cleanLocal
            } else {
                if (cleanUrl != null) refresher?.requestRefresh(gameId)
                cleanUrl
            }
        }

        cleanLocal.startsWith("/") -> {
            if (File(cleanLocal).exists()) {
                cleanLocal
            } else {
                if (cleanUrl != null) refresher?.requestRefresh(gameId)
                cleanUrl
            }
        }

        else -> cleanLocal
    }
}

@Composable
fun GameCoverImage(
    localPath: String?,
    url: String?,
    gameId: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderRes: Int? = null,
) {
    val context = LocalContext.current
    val refresher = remember(context) {
        (context.applicationContext as? BacklogOrderApplication)?.container?.coverRefresher
    }

    val resolvedModel = remember(localPath, url, gameId, refresher) {
        resolveCoverModel(localPath, url, gameId, refresher)
    }

    if (resolvedModel.isNullOrBlank()) {
        if (placeholderRes != null) {
            Image(
                painter = painterResource(placeholderRes),
                contentDescription = contentDescription,
                modifier = modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = contentScale,
            )
        } else {
            val colors = LocalHudColors.current
            val initial = contentDescription?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.accent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initial,
                    color = colors.text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    } else {
        AsyncImage(
            model = resolvedModel,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            placeholder = placeholderRes?.let { painterResource(it) },
            error = placeholderRes?.let { painterResource(it) },
            onError = {
                if (!localPath.isNullOrBlank() && !url.isNullOrBlank()) {
                    refresher?.requestRefresh(gameId)
                }
            },
        )
    }
}

@Composable
fun GameCoverImage(
    game: GameEntity,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderRes: Int? = null,
) {
    GameCoverImage(
        localPath = game.backgroundImage,
        url = game.backgroundImageUrl,
        gameId = game.id,
        contentDescription = game.name,
        modifier = modifier,
        contentScale = contentScale,
        placeholderRes = placeholderRes,
    )
}

@Composable
fun GameCoverImage(
    game: Game,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderRes: Int? = null,
) {
    GameCoverImage(
        localPath = game.backgroundImage,
        url = game.backgroundImageUrl,
        gameId = game.id,
        contentDescription = game.name,
        modifier = modifier,
        contentScale = contentScale,
        placeholderRes = placeholderRes,
    )
}
