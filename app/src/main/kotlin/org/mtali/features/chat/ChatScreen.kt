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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.ui.components.composer.MessageInput
import io.getstream.chat.android.compose.ui.messages.composer.MessageComposer
import io.getstream.chat.android.compose.ui.messages.list.MessageList
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.rememberMessageListState
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.ui.common.state.messages.list.DeletedMessageVisibility
import org.mtali.R

@Composable
fun ChatRoute(viewModel: ChatViewModel = hiltViewModel(), onBackPressed: () -> Unit) {
  ChatScreen(viewModel.args.channelId, onBackPressed)
}

@Composable
private fun ChatScreen(channelId: String, onBackPressed: () -> Unit) {
  Scaffold { padding ->
    Box(modifier = Modifier.padding(padding)) {
      ChatTheme {
        BoltMessageScreen(channelId = channelId, onBackCLick = onBackPressed)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoltMessageScreen(channelId: String, onBackCLick: () -> Unit) {
  val context = LocalContext.current
  val factory = remember(context) {
    MessagesViewModelFactory(
      context = context,
      channelId = channelId,
      autoTranslationEnabled = false,
      isComposerLinkPreviewEnabled = false,
      deletedMessageVisibility = DeletedMessageVisibility.ALWAYS_HIDDEN,
      messageId = null,
      parentMessageId = null,
    )
  }

  val listViewModel = viewModel<MessageListViewModel>(factory = factory)
  val composerViewModel = viewModel<MessageComposerViewModel>(factory = factory)
  val lazyListState = rememberMessageListState()

  Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
      modifier = Modifier.fillMaxSize(),
      bottomBar = {
        BoltComposer(composerViewModel)
      },
      topBar = {
        TopAppBar(
          title = { Text(text = stringResource(id = R.string.chat)) },
          windowInsets = WindowInsets(top = 0.dp),
          navigationIcon = {
            IconButton(onClick = onBackCLick) {
              Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
            }
          },
        )
      },
    ) {
      MessageList(
        modifier = Modifier
          .padding(it)
          .background(ChatTheme.colors.appBackground)
          .fillMaxSize(),
        viewModel = listViewModel,
        messagesLazyListState = if (listViewModel.isInThread) rememberMessageListState() else lazyListState,
      )
    }
  }
}

@Composable
fun BoltComposer(composerViewModel: MessageComposerViewModel) {
  MessageComposer(
    modifier = Modifier
      .fillMaxWidth()
      .wrapContentHeight(),
    viewModel = composerViewModel,
    statefulStreamMediaRecorder = null,
    integrations = {},
    input = { inputState ->
      MessageInput(
        modifier = Modifier
          .fillMaxWidth()
          .imePadding()
          .weight(7f)
          .padding(start = 8.dp),
        messageComposerState = inputState,
        onValueChange = { composerViewModel.setMessageInput(it) },
        onAttachmentRemoved = { composerViewModel.removeSelectedAttachment(it) },
        label = {
          Row(
            Modifier.wrapContentWidth(),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              modifier = Modifier.padding(start = 4.dp),
              text = stringResource(id = R.string.message),
              color = ChatTheme.colors.textLowEmphasis,
            )
          }
        },
        innerTrailingContent = {
          Icon(
            modifier = Modifier
              .size(24.dp)
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(),
              ) {
                val state = composerViewModel.messageComposerState.value
                composerViewModel.sendMessage(
                  composerViewModel.buildNewMessage(
                    state.inputValue,
                    state.attachments,
                  ),
                )
              },
            painter = painterResource(id = R.drawable.ic_send),
            tint = ChatTheme.colors.primaryAccent,
            contentDescription = null,
          )
        },
      )
    },
    trailingContent = { Spacer(modifier = Modifier.size(8.dp)) },
  )
}
