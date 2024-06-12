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
package org.mtali.features.chat.navigation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.mtali.features.chat.ChatRoute

const val CHANNEL_ID = "channelId"
const val CHAT_ROUTE = "chat_route/{$CHANNEL_ID}"

class ChatArgs(val channelId: String) {
  constructor(savedStateHandle: SavedStateHandle) :
    this(checkNotNull(savedStateHandle.get<String>(CHANNEL_ID)))
}

fun NavController.navigateToChat(channelId: String, navOptions: NavOptions? = null) {
  navigate("chat_route/${Uri.encode(channelId)}", navOptions)
}

fun NavGraphBuilder.chatScreen(onBackPressed: () -> Unit) {
  composable(
    route = CHAT_ROUTE,
    arguments = listOf(navArgument(CHANNEL_ID) { type = NavType.StringType }),
  ) {
    ChatRoute(onBackPressed = onBackPressed)
  }
}
