package com.xinto.mauth.ui.screen.home.component

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Row
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
    BottomAppBar(
        actions = {
            AnimatedContent(
                targetState = isSelectionActive,
                transitionSpec = {
                    slideIntoContainer(AnimatedContentScope.SlideDirection.Up) + fadeIn() with
                            slideOutOfContainer(AnimatedContentScope.SlideDirection.Up) + fadeOut()
                },
                label = "Actions"
            ) { isSelectionActive ->
                if (isSelectionActive) {
                    IconButton(onClick = onDeleteSelected) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteForever,
                            contentDescription = null
                        )
                    }
                } else {
                    Row {
                        var isMoreActionsVisible by remember { mutableStateOf(false) }
                        IconButton(onClick = {
                            isMoreActionsVisible = true
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = null
                            )
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
                        }

                        var isSortVisible by remember { mutableStateOf(false) }
                        IconButton(onClick = {
                            isSortVisible = true
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Sort,
                                contentDescription = null
                            )
                            DropdownMenu(
                                expanded = isSortVisible,
                                onDismissRequest = {
                                    isSortVisible = false
                                }
                            ) {
                                SortSetting.values().forEach {
                                    DropdownMenuItem(
                                        onClick = { onActiveSortChange(it) },
                                        text = {
                                            val resource = remember(it) {
                                                when (it) {
                                                    SortSetting.DateAsc, SortSetting.DateDesc -> R.string.home_sort_date
                                                    SortSetting.LabelAsc, SortSetting.LabelDesc -> R.string.home_sort_label
                                                    SortSetting.IssuerAsc, SortSetting.IssuerDesc -> R.string.home_sort_issuer
                                                }
                                            }
                                            Text(stringResource(resource))
                                        },
                                        leadingIcon = {
                                            val drawable = remember(it) {
                                                when (it) {
                                                    SortSetting.DateAsc, SortSetting.LabelAsc, SortSetting.IssuerAsc -> Icons.Rounded.ArrowUpward
                                                    SortSetting.DateDesc, SortSetting.LabelDesc, SortSetting.IssuerDesc -> Icons.Rounded.ArrowDownward
                                                }
                                            }
                                            Icon(
                                                imageVector = drawable,
                                                contentDescription = null
                                            )
                                        },
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
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            AnimatedContent(
                targetState = isSelectionActive,
                transitionSpec = {
                    scaleIn() + fadeIn() with scaleOut() + fadeOut()
                },
                label = "FAB"
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