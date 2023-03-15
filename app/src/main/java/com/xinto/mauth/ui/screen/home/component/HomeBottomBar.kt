package com.xinto.mauth.ui.screen.home.component

import androidx.compose.animation.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.xinto.mauth.R

@Composable
fun HomeBottomBar(
    isSelectionActive: Boolean = false,
    onAddClick: () -> Unit,
    onCancelSelection: () -> Unit,
    onRemoveSelected: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var isMoreActionsVisible by remember { mutableStateOf(false) }
    BottomAppBar(
        actions = {
            AnimatedContent(targetState = isSelectionActive) {
                if (isSelectionActive) {
                    DropdownMenu(
                        expanded = isMoreActionsVisible,
                        onDismissRequest = {
                            isMoreActionsVisible = false
                        }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(stringResource(R.string.home_more_settings))
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = null
                                )
                            },
                            onClick = onSettingsClick
                        )
                    }
                    IconButton(onClick = {
                        isMoreActionsVisible = true
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = null
                        )
                    }
                } else {
                    IconButton(onClick = onRemoveSelected) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteForever,
                            contentDescription = null
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            AnimatedContent(
                targetState = isSelectionActive,
                transitionSpec = {
                    scaleIn() + fadeIn() with scaleOut() + fadeOut()
                }
            ) { isSelectionActive ->
                if (isSelectionActive) {
                    FloatingActionButton(onClick = onCancelSelection) {
                        Icon(
                            imageVector = Icons.Rounded.Undo,
                            contentDescription = null
                        )
                    }
                } else {
                    FloatingActionButton(onClick = onAddClick) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    )
}