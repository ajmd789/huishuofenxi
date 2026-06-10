/**
 * author:zhangsiyuan
 *
 * 关键解释：
 *
 * fillMaxWidth(0.1f) 使每个按钮占据父级 Row 宽度的 10%
 *
 * fillMaxHeight() 让按钮高度填满横条的 32dp
 *
 * 按钮数目可动态传入，不超过 10 个时刚好占满 100%（多出会溢出，可按需调整）
 */
package org.example.project

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class TopBarButton(
    val text: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true
)

@Composable
fun TopButtonBar(
    buttons: List<TopBarButton>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
    ) {
        buttons.forEach { button ->
            Button(
                onClick = button.onClick,
                enabled = button.enabled,
                modifier = Modifier
                    .fillMaxWidth(0.1f)   // 每个按钮占10%宽度
                    .fillMaxHeight(),
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding
            ) {
                Text(
                    text = button.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}