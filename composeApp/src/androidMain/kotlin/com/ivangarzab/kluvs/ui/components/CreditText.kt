package com.ivangarzab.kluvs.ui.components

import androidx.annotation.StringRes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.ivangarzab.kluvs.R
import com.ivangarzab.kluvs.designsystem.theme.KluvsTheme

/**
 * Third-party attribution line (Google Books / Wikipedia). The string resource is HTML, and its
 * `<a>` tags render as tappable links — the source and license links are a licensing
 * requirement, not decoration, so a plain `stringResource` + `Text` would flatten them.
 */
@Composable
fun CreditText(
    @StringRes resId: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = KluvsTheme.typography.finePrint,
    color: Color = KluvsTheme.colors.contentMuted,
) {
    val html = stringResource(resId)
    val linkColor = KluvsTheme.colors.accent
    val text = remember(html, linkColor) {
        AnnotatedString.fromHtml(
            htmlString = html,
            linkStyles = TextLinkStyles(
                style = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)
            ),
        )
    }
    Text(text = text, modifier = modifier, style = style, color = color)
}

@PreviewLightDark
@Composable
fun Preview_CreditText() = KluvsTheme {
    CreditText(resId = R.string.credits_author_data)
}
