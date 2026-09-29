package com.ekoehler.expressivecutout.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ekoehler.expressivecutout.R
import com.ekoehler.expressivecutout.core.DynamicTile
import com.ekoehler.expressivecutout.ui.AppViewModel
import com.ekoehler.expressivecutout.ui.components.ColorPickerCard

/**
 * Settings for the assistant dynamic tile: whether to display the text answer in the cutout,
 * the max cutout height as a percentage of the screen height, and the tile's icon container colour.
 */
@Composable
internal fun AssistantScreen(
    viewModel: AppViewModel,
    contentPadding: PaddingValues,
) {
    val settings by viewModel.assistantTile.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    var chooseApp by remember { androidx.compose.runtime.mutableStateOf(false) }
    if (chooseApp) AppPickerSheet(onPick = {
        viewModel.setAssistantShortcutPackage(it)
        chooseApp = false
    }, onDismiss = { chooseApp = false })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        SettingsToggleCard(shape = RoundedCornerShape(16.dp), title = "Automatically expand responses",
            description = "Off keeps replies in the small island until you tap. Expanded replies scroll within the chosen height.",
            checked = settings.autoExpand, onCheckedChange = viewModel::setAssistantAutoExpand)
        Text("Galaxy AI Preview", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
        SettingsToggleCard(
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
            title = "Enable Galaxy AI",
            description = "Experimental. Off by default. Enabling this permits configured AI providers to process notification text only when you request an AI action.",
            checked = ai.enabled,
            onCheckedChange = viewModel::setAiEnabled,
        )
        AnimatedVisibility(visible = ai.enabled) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SettingsToggleCard(
                    shape = RoundedCornerShape(4.dp),
                    title = "Summarize notifications",
                    description = "Adds an AI summary action to supported expanded notifications.",
                    checked = ai.notificationSummaries,
                    onCheckedChange = viewModel::setAiNotificationSummaries,
                )
                SettingsToggleCard(
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp, topStart = 4.dp, topEnd = 4.dp),
                    title = "Suggested replies",
                    description = "Allows AI to draft a reply. Galaxy Island will never send it without your action.",
                    checked = ai.suggestedReplies,
                    onCheckedChange = viewModel::setAiSuggestedReplies,
                )
            }
        }
        Text(
            "No model or API credential is bundled in this preview. AI actions remain unavailable until a provider is configured.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        Text("Assistant shortcuts", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
        androidx.compose.material3.Button(onClick = { chooseApp = true }, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Choose assistant app")
        }
        Text(settings.shortcutPackage ?: "Using your phone's default assistant", modifier = Modifier.padding(horizontal = 16.dp))
        androidx.compose.material3.TextButton(onClick = { viewModel.setAssistantShortcutPackage(null) }) { Text("Use phone default") }
        androidx.compose.material3.TextButton(onClick = {
            com.ekoehler.expressivecutout.core.AssistantLauncher.open(context, settings.shortcutPackage)
        }) { Text("Open assistant") }
        SettingsToggleCard(shape = RoundedCornerShape(16.dp), title = "Long-press island for assistant",
            description = "Hold the collapsed island to open the chosen app. Enable Show when empty for access between notifications.",
            checked = settings.longPressShortcut, onCheckedChange = viewModel::setAssistantLongPress)
        Text("Galaxy Island displays supported assistant responses; choosing an app does not add response capture support. Responses are not saved to history. The D2 connection hides the island while locked.",
            style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(16.dp))
        var textSize by remember(settings.textSizeSp) { mutableFloatStateOf(settings.textSizeSp.toFloat()) }
        SettingsSliderCard(shape = RoundedCornerShape(16.dp), title = "Response text size",
            description = "Adjust readability inside the expanded assistant tile.", valueText = "${textSize.toInt()} sp",
            value = textSize, valueRange = 12f..24f, step = 1f,
            onValueChange = { textSize = it }, onCommit = { viewModel.setAssistantTextSize(textSize.toInt()) })
        SettingsToggleCard(shape = RoundedCornerShape(16.dp), title = "Always show Dismiss",
            description = "Keep a dismiss button even when other action buttons are hidden.",
            checked = settings.showCloseButton, onCheckedChange = viewModel::setAssistantCloseButton)
        SettingsToggleCard(
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
            title = stringResource(R.string.assistant_display_answer_title),
            description = stringResource(R.string.assistant_display_answer_desc),
            checked = settings.displayAnswerInCutout,
            onCheckedChange = viewModel::setAssistantDisplayAnswerInCutout,
        )

        // The max height only matters while the answer is actually rendered in the cutout.
        AnimatedVisibility(visible = settings.displayAnswerInCutout) {
            var sliderValue by remember(settings.maxCutoutHeightPercent) {
                mutableFloatStateOf(settings.maxCutoutHeightPercent.toFloat())
            }
            SettingsSliderCard(
                shape = RoundedCornerShape(4.dp),
                title = stringResource(R.string.assistant_max_height_title),
                description = stringResource(R.string.assistant_max_height_desc),
                valueText = stringResource(R.string.assistant_max_height_value, sliderValue.toInt()),
                value = sliderValue,
                valueRange = 10f..30f,
                step = 5f,
                onValueChange = { sliderValue = it },
                onCommit = { viewModel.setAssistantMaxCutoutHeightPercent(sliderValue.toInt()) },
            )
        }

        SettingsToggleCard(
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 32.dp, bottomEnd = 32.dp),
            title = stringResource(R.string.assistant_animated_icon_title),
            description = stringResource(R.string.assistant_animated_icon_desc),
            checked = settings.useAnimatedIcon,
            onCheckedChange = viewModel::setAssistantUseAnimatedIcon,
        )

        Text(
            text = stringResource(R.string.tile_icon_container_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp, top = 12.dp, bottom = 4.dp),
        )
        ColorPickerCard(
            label = stringResource(R.string.tile_icon_container_label),
            selected = settings.iconContainerColor,
            onSelect = viewModel::setAssistantIconContainerColor,
            defaultLabel = stringResource(R.string.music_default_accent),
            defaultColor = Color(DynamicTile.ASSISTANT.accent),
        )

        Text(
            text = stringResource(R.string.assistant_tile_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
        )
    }
}
