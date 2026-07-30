package com.cara.app.ui.screens.contextinput

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cara.app.ui.theme.Citrus
import com.cara.app.ui.theme.InkBase
import com.cara.app.ui.theme.InkRaised
import com.cara.app.ui.theme.InkSurface
import com.cara.app.ui.theme.WarmGrey
import com.cara.app.ui.theme.WarmWhite

private const val MIN_BUDGET = 100f
private const val MAX_BUDGET = 1500f
private const val DEFAULT_BUDGET = 500f

// Launched as a modal sheet from Home (see HomeScreen.kt) - not part of
// CaraNavHost. Feeds text_input (-> Gemini emotion detection) and budget
// (-> budget_fit) into the same /recommendations call HomeViewModel already
// makes on load, per CLAUDE_android.md's Context Input screen.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextInputSheet(
    onDismiss: () -> Unit,
    onSubmit: (textInput: String?, budget: Double?) -> Unit,
) {
    var moodText by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf(DEFAULT_BUDGET) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = InkSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "How are you feeling?",
                style = MaterialTheme.typography.headlineMedium,
                color = WarmWhite,
            )

            OutlinedTextField(
                value = moodText,
                onValueChange = { moodText = it },
                placeholder = { Text("e.g. \"a bit tired\", \"excited for the weekend\"") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Citrus,
                    unfocusedBorderColor = WarmGrey,
                    focusedTextColor = WarmWhite,
                    unfocusedTextColor = WarmWhite,
                    cursorColor = Citrus,
                    focusedPlaceholderColor = WarmGrey,
                    unfocusedPlaceholderColor = WarmGrey,
                ),
            )

            Text(
                "Budget: ₹${budget.toInt()}",
                style = MaterialTheme.typography.titleMedium,
                color = WarmWhite,
            )
            Slider(
                value = budget,
                onValueChange = { budget = it },
                valueRange = MIN_BUDGET..MAX_BUDGET,
                colors = SliderDefaults.colors(
                    thumbColor = Citrus,
                    activeTrackColor = Citrus,
                    inactiveTrackColor = InkRaised,
                ),
            )

            Button(
                onClick = { onSubmit(moodText.trim().ifBlank { null }, budget.toDouble()) },
                colors = ButtonDefaults.buttonColors(containerColor = Citrus, contentColor = InkBase),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Update recommendations")
            }
        }
    }
}
