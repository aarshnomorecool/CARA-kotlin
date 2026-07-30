package com.cara.app.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cara.app.data.remote.RecommendationContextDto
import com.cara.app.ui.theme.Citrus
import com.cara.app.ui.theme.InkBase
import com.cara.app.ui.theme.InkRaised
import com.cara.app.ui.theme.InkSurface
import com.cara.app.ui.theme.WarmGrey
import kotlin.math.roundToInt

// The one place in the app that makes "context-awareness" visible rather
// than just claimed - see CLAUDE_android.md's "Signature element: the
// Context Strip". Pinned below the top app bar on Home.

enum class ContextFactor { WEATHER, TIME, LOCATION, EMOTION }

@Composable
fun ContextStrip(
    context: RecommendationContextDto,
    highlightedFactor: ContextFactor? = null,
    modifier: Modifier = Modifier,
) {
    var explainedFactor by remember { mutableStateOf<ContextFactor?>(null) }

    Crossfade(
        targetState = context,
        label = "context-strip",
        modifier = modifier
            .fillMaxWidth()
            .background(InkSurface)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) { c ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ContextPill(
                icon = weatherIcon(c.weatherCondition),
                label = "${c.tempCelsius.roundToInt()}°C",
                highlighted = highlightedFactor == ContextFactor.WEATHER,
                onClick = { explainedFactor = ContextFactor.WEATHER },
            )
            ContextPill(
                icon = "🕒",
                label = c.timeSlot.replaceFirstChar { it.uppercase() },
                highlighted = highlightedFactor == ContextFactor.TIME,
                onClick = { explainedFactor = ContextFactor.TIME },
            )
            ContextPill(
                icon = "📍",
                label = locationLabel(c.locationContext),
                highlighted = highlightedFactor == ContextFactor.LOCATION,
                onClick = { explainedFactor = ContextFactor.LOCATION },
            )
            ContextPill(
                icon = emotionIcon(c.emotion),
                label = c.emotion.replace("_", " ").replaceFirstChar { it.uppercase() },
                highlighted = highlightedFactor == ContextFactor.EMOTION,
                onClick = { explainedFactor = ContextFactor.EMOTION },
            )
        }
    }

    explainedFactor?.let { factor ->
        ContextExplanationDialog(factor = factor, context = context, onDismiss = { explainedFactor = null })
    }
}

@Composable
private fun ContextPill(icon: String, label: String, highlighted: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (highlighted) Citrus else InkRaised,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text(icon, fontSize = 13.sp)
            Spacer(Modifier.width(4.dp))
            Text(
                label,
                color = if (highlighted) InkBase else WarmGrey,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun ContextExplanationDialog(factor: ContextFactor, context: RecommendationContextDto, onDismiss: () -> Unit) {
    val (title, explanation) = when (factor) {
        ContextFactor.WEATHER -> "Weather" to
            "Currently ${context.weatherCondition.replace('_', ' ')} at ${context.tempCelsius.roundToInt()}°C nearby. " +
            "This affects whether indoor or outdoor places are favored."
        ContextFactor.TIME -> "Time of day" to
            "It's ${context.timeSlot} right now, from your device's clock. Some places (gyms, cafés, restaurants) " +
            "get busier or quieter depending on the time of day."
        ContextFactor.LOCATION -> "Location" to when (context.locationContext) {
            "HOME" -> "You're within about 100m of your saved home location."
            "COLLEGE" -> "You're within about 100m of your saved college location."
            else -> "You're outside your saved home and college locations."
        }
        ContextFactor.EMOTION -> "Mood" to
            "Detected as \"${context.emotion}\" - either from what you typed, or \"neutral\" by default. " +
            "This nudges recommendations toward places that fit how you're feeling."
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Got it") } },
        title = { Text(title) },
        text = { Text(explanation) },
    )
}

private fun weatherIcon(condition: String): String = when (condition) {
    "clear" -> "☀️"
    "cloudy" -> "☁️"
    "rain" -> "🌧️"
    "extreme_heat" -> "🥵"
    else -> "☁️"
}

private fun emotionIcon(emotion: String): String = when (emotion) {
    "neutral" -> "😐"
    "tired" -> "😴"
    "stressed" -> "😣"
    "happy" -> "😊"
    "excited" -> "🤩"
    "hungry" -> "🍽️"
    "bored" -> "🥱"
    "want_to_relax" -> "😌"
    "want_to_exercise" -> "💪"
    else -> "😐"
}

private fun locationLabel(locationContext: String): String = when (locationContext) {
    "HOME" -> "Near Home"
    "COLLEGE" -> "Near College"
    else -> "Out and about"
}
