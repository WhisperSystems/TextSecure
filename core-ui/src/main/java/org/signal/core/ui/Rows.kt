package org.signal.core.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import org.signal.core.ui.Rows.TextAndLabel

object Rows {

  /**
   * A row consisting of a radio button and [text] and optional [label] in a [TextAndLabel].
   */
  @Composable
  fun RadioRow(
    selected: Boolean,
    text: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true
  ) {
    RadioRow(
      content = {
        TextAndLabel(
          text = text,
          label = label,
          enabled = enabled
        )
      },
      selected = selected,
      modifier = modifier,
      enabled = enabled
    )
  }

  /**
   * Customizable radio row that allows [content] to be provided as composable functions instead of primitives.
   */
  @Composable
  fun RadioRow(
    content: @Composable RowScope.() -> Unit,
    selected: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
  ) {
    Row(
      modifier = modifier
        .fillMaxWidth()
        .padding(defaultPadding()),
      verticalAlignment = CenterVertically
    ) {
      RadioButton(
        enabled = enabled,
        selected = selected,
        onClick = null,
        modifier = Modifier.padding(end = 24.dp)
      )

      content()
    }
  }

  /**
   * Row that positions [text] and optional [label] in a [TextAndLabel] to the side of a [Switch].
   *
   * Can display a circular loading indicator by setting isLoaded to true. Setting isLoading to true
   * will disable the control by default.
   */
  @Composable
  fun ToggleRow(
    checked: Boolean,
    text: String,
    onCheckChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    isLoading: Boolean = false,
    enabled: Boolean = true
  ) {
    val enabledAndNotLoading by rememberSaveable(isLoading,enabled) {
      mutableStateOf(!isLoading && enabled)
    }

    Row(
      modifier = modifier
        .fillMaxWidth()
        .clickable(enabled = enabledAndNotLoading) { onCheckChanged(!checked) }
        .padding(defaultPadding()),
      verticalAlignment = CenterVertically
    ) {
      TextAndLabel(
        text = text,
        label = label,
        textColor = textColor,
        enabled = enabledAndNotLoading,
        modifier = Modifier.padding(end = 16.dp)
      )

      val loadingContent by rememberDelayedState(isLoading)
      val toggleState = remember(checked, loadingContent, enabledAndNotLoading, onCheckChanged) {
        ToggleState(checked, loadingContent, enabledAndNotLoading, onCheckChanged)
      }

      AnimatedContent(
        toggleState,
        label = "toggle-loading-state",
        contentKey = { it.isLoading },
        transitionSpec = {
          fadeIn(animationSpec = tween(220, delayMillis = 90))
            .togetherWith(fadeOut(animationSpec = tween(90)))
        }
      ) { state ->
        if (state.isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.minimumInteractiveComponentSize()
          )
        } else {
          Switch(
            checked = state.checked,
            enabled = state.enabled,
            onCheckedChange = state.onCheckChanged
          )
        }
      }
    }
  }

  /**
   * Text row that positions [text] and optional [label] in a [TextAndLabel] to the side of an optional [icon].
   */
  @Composable
  fun TextRow(
    text: String,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    label: String? = null,
    icon: Painter? = null,
    foregroundTint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true
  ) {
    TextRow(
      text = {
        TextAndLabel(
          text = text,
          label = label,
          textColor = foregroundTint,
          enabled = enabled
        )
      },
      icon = if (icon != null) {
        {
          Icon(
            painter = icon,
            contentDescription = null,
            tint = foregroundTint,
            modifier = iconModifier
          )
        }
      } else {
        null
      },
      modifier = modifier,
      onClick = onClick,
      onLongClick = onLongClick,
      enabled = enabled
    )
  }

  /**
   * Customizable text row that allows [text] and [icon] to be provided as composable functions instead of primitives.
   */
  @OptIn(ExperimentalFoundationApi::class)
  @Composable
  fun TextRow(
    text: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable RowScope.() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true
  ) {
    Row(
      modifier = modifier
        .fillMaxWidth()
        .combinedClickable(
          enabled = enabled && (onClick != null || onLongClick != null),
          onClick = onClick ?: {},
          onLongClick = onLongClick ?: {}
        )
        .padding(defaultPadding()),
      verticalAlignment = CenterVertically
    ) {
      if (icon != null) {
        icon()
        Spacer(modifier = Modifier.width(24.dp))
      }
      text()
    }
  }

  @Composable
  fun defaultPadding(): PaddingValues {
    return PaddingValues(
      horizontal = dimensionResource(id = R.dimen.gutter),
      vertical = 16.dp
    )
  }

  /**
   * Row component to position text above an optional label.
   */
  @Composable
  fun RowScope.TextAndLabel(
    text: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge
  ) {
    Column(
      modifier = modifier
        .alpha(if (enabled) 1f else 0.4f)
        .weight(1f)
    ) {
      Text(
        text = text,
        style = textStyle,
        color = textColor
      )

      if (label != null) {
        Text(
          text = label,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

private data class ToggleState(
  val checked: Boolean,
  val isLoading: Boolean,
  val enabled: Boolean,
  val onCheckChanged: (Boolean) -> Unit
)

@SignalPreview
@Composable
private fun RadioRowPreview() {
  Previews.Preview {
    var selected by remember { mutableStateOf(true) }

    Rows.RadioRow(
      selected,
      "RadioRow",
      label = "RadioRow Label",
      modifier = Modifier.clickable {
        selected = !selected
      }
    )
  }
}

@SignalPreview
@Composable
private fun ToggleRowPreview() {
  Previews.Preview {
    var checked by remember { mutableStateOf(false) }

    Rows.ToggleRow(
      checked = checked,
      text = "ToggleRow",
      label = "ToggleRow label",
      onCheckChanged = {
        checked = it
      }
    )
  }
}

@SignalPreview
@Composable
private fun ToggleLoadingRowPreview() {
  Previews.Preview {
    var checked by remember { mutableStateOf(false) }

    Rows.ToggleRow(
      checked = checked,
      text = "ToggleRow",
      label = "ToggleRow label",
      isLoading = true,
      onCheckChanged = {
        checked = it
      }
    )
  }
}

@SignalPreview
@Composable
private fun TextRowPreview() {
  Previews.Preview {
    Rows.TextRow(
      text = "TextRow",
      icon = painterResource(id = android.R.drawable.ic_menu_camera),
      onClick = {}
    )
  }
}

@SignalPreview
@Composable
private fun TextAndLabelPreview() {
  Previews.Preview {
    Row {
      TextAndLabel(
        text = "TextAndLabel Text",
        label = "TextAndLabel Label"
      )
      TextAndLabel(
        text = "TextAndLabel Text",
        label = "TextAndLabel Label",
        enabled = false
      )
    }
  }
}
