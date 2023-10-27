package com.xinto.mauth.ui.screen.home.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.xinto.mauth.R
import com.xinto.mauth.core.settings.model.SortSetting

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
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up) + fadeIn() togetherWith
                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Up) + fadeOut()
                },
                label = "Actions"
            ) { isSelectionActive ->
                if (isSelectionActive) {
                    IconButton(onClick = onDeleteSelected) {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete_forever),
                            contentDescription = null
                        )
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        var isMoreActionsVisible by remember { mutableStateOf(false) }
                        IconButton(onClick = {
                            isMoreActionsVisible = true
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_more_vert),
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
                                            painter = painterResource(R.drawable.ic_settings),
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        isMoreActionsVisible = false
                                        onSettingsClick()
                                    }
                                )
                            }
                        }

                        var isSortVisible by remember { mutableStateOf(false) }
                        IconButton(onClick = {
                            isSortVisible = true
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_sort),
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
                                        onClick = {
                                            isSortVisible = false
                                            onActiveSortChange(it)
                                        },
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
                                                    SortSetting.DateAsc, SortSetting.LabelAsc, SortSetting.IssuerAsc -> R.drawable.ic_arrow_upward
                                                    SortSetting.DateDesc, SortSetting.LabelDesc, SortSetting.IssuerDesc -> R.drawable.ic_arrow_downward
                                                }
                                            }
                                            Icon(
                                                painter = painterResource(drawable),
                                                contentDescription = null
                                            )
                                        },
                                        trailingIcon = {
                                            if (activeSortSetting == it) {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_check),
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
                    scaleIn() + fadeIn() togetherWith
                            scaleOut() + fadeOut()
                },
                label = "FAB"
            ) { isSelectionActive ->
                if (isSelectionActive) {
                    FloatingActionButton(onClick = onCancelSelection) {
                        Icon(
                            painter = painterResource(R.drawable.ic_undo),
                            contentDescription = null
                        )
                    }
                } else {
                    FloatingActionButton(onClick = onAdd) {
                        Icon(
                            painter = painterResource(R.drawable.ic_add),
                            contentDescription = null
                        )
                    }
                }
            }
        }
    )
}