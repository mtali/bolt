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
package org.mtali.features.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import io.getstream.chat.android.compose.ui.messages.MessagesScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory

@Composable
fun ChatRoute(viewModel: ChatViewModel = hiltViewModel(), onBackPressed: () -> Unit) {
  ChatScreen(viewModel.args.channelId, onBackPressed)
}

@Composable
private fun ChatScreen(channelId: String, onBackPressed: () -> Unit) {
  val context = LocalContext.current
  Scaffold { padding ->
    Box(modifier = Modifier.padding(padding)) {
      ChatTheme {
        MessagesScreen(
          viewModelFactory = MessagesViewModelFactory(
            context = context,
            channelId = channelId,
            messageLimit = 30,
          ),
          onBackPressed = onBackPressed,
        )
      }
    }
  }
}
