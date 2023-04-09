package com.xinto.mauth.ui.screen.home.component

import androidx.compose.animation.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.xinto.mauth.R
import com.xinto.mauth.domain.settings.model.SortSetting

@Composable
fun HomeBottomBar(
    isSelectionActive: Boolean = false,
    activeSortSetting: SortSetting,
    onActiveSortChange: (SortSetting) -> Unit,
    onAdd: () -> Unit,
    onCancelSelection: () -> Unit,
    onDeleteSelected: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var isMoreActionsVisible by remember { mutableStateOf(false) }
    BottomAppBar(
        actions = {
            AnimatedContent(
                targetState = isSelectionActive,
                transitionSpec = {
                    slideIntoContainer(AnimatedContentScope.SlideDirection.Up) + fadeIn() with
                            slideOutOfContainer(AnimatedContentScope.SlideDirection.Up) + fadeOut()
                }
            ) { isSelectionActive ->
                if (isSelectionActive) {
                    IconButton(onClick = onDeleteSelected) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteForever,
                            contentDescription = null
                        )
                    }
                    var isSortVisible by remember { mutableStateOf(false) }
                    DropdownMenu(
                        expanded = isSortVisible,
                        onDismissRequest = {
                            isSortVisible = false
                        }
                    ) {
                        SortSetting.values().forEach {
                            DropdownMenuItem(
                                text = { /*TODO*/ },
                                onClick = { onActiveSortChange(it) },
                                trailingIcon = {
                                    if (activeSortSetting == it) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null
                                        )
                                    }
                                }
                            )
                        }
                    }
                } else {
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
                    FloatingActionButton(onClick = onAdd) {
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