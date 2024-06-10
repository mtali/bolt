/*
 * Designed and developed by 2024 mtali (Emmanuel Mtali)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mtali.core.designsystem.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import kotlinx.coroutines.delay

@Composable
fun TypewriterText(
  texts: List<String>,
  fontSize: TextUnit,
  fontWeight: FontWeight? = null,
  loop: Boolean = true,
  typingDelay: Long = 90,
  delayBetweenText: Long = 5000,
) {
  var textIndex by remember { mutableIntStateOf(0) }
  var textToDisplay by remember { mutableStateOf("") }

  LaunchedEffect(key1 = texts) {
    while (textIndex < texts.size) {
      texts[textIndex].forEachIndexed { charIndex, _ ->
        textToDisplay = texts[textIndex].substring(startIndex = 0, endIndex = charIndex + 1)
        delay(typingDelay)
      }
      if ((textIndex + 1 == texts.size) && !loop) {
        break
      } else {
        textIndex = (textIndex + 1) % texts.size
        delay(delayBetweenText)
      }
    }
  }

  Text(
    text = textToDisplay,
    fontSize = fontSize,
    fontWeight = fontWeight,
  )
}
