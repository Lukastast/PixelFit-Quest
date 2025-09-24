import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun AutoSizeText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    maxFontSize: TextUnit = 24.sp,
    minFontSize: TextUnit = 12.sp,
    stepSize: TextUnit = 1.sp
) {
    var fontSize by remember { mutableStateOf(maxFontSize) }
    var readyToDraw by remember { mutableStateOf(false) }

    Text(
        text = text,
        style = style.copy(fontSize = fontSize),
        maxLines = 1,
        overflow = TextOverflow.Clip,
        modifier = modifier,
        onTextLayout = { layoutResult: TextLayoutResult ->
            if (layoutResult.hasVisualOverflow || layoutResult.lineCount > 1) {
                if (fontSize > minFontSize) {
                    fontSize = (fontSize.value - stepSize.value).sp
                } else {
                    readyToDraw = true
                }
            } else {
                readyToDraw = true
            }
        }
    )
}