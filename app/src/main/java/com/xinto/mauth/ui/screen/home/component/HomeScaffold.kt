package com.xinto.mauth.ui.screen.home.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.xinto.mauth.R
import com.xinto.mauth.core.settings.model.SortSetting
import com.xinto.mauth.ui.component.ResponsiveAppBarScaffold

@Composable
fun HomeScaffold(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior,
    isSelectionActive: Boolean = false,
    activeSortSetting: SortSetting,
    onActiveSortChange: (SortSetting) -> Unit,
    onAdd: () -> Unit,
    onCancelSelection: () -> Unit,
    onDeleteSelected: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    ResponsiveAppBarScaffold(
        modifier = modifier,
        appBarTitle = { Text(stringResource(id = R.string.app_name)) },
        scrollBehavior = scrollBehavior,
        actions = { arrangement ->
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
                        horizontalArrangement = arrangement,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
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
                                SortSetting.entries.forEach {
                                    DropdownMenuItem(
                                        onClick = {
                                            isSortVisible = false
                                            onActiveSortChange(it)
                                        },
                                        text = {
                                            val resource = remember(it) {
                                                when (it) {
                                                    SortSetting.DateAsc -> R.string.home_sort_date_ascending
                                                    SortSetting.DateDesc -> R.string.home_sort_date_descending
                                                    SortSetting.LabelAsc -> R.string.home_sort_label_ascending
                                                    SortSetting.LabelDesc -> R.string.home_sort_label_descending
                                                    SortSetting.IssuerAsc -> R.string.home_sort_issuer_ascending
                                                    SortSetting.IssuerDesc -> R.string.home_sort_issuer_descending
                                                }
                                            }
                                            Text(stringResource(resource))
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
                                        Text(stringResource(R.string.settings_title))
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
                                DropdownMenuItem(
                                    text = {
                                        Text(stringResource(R.string.about_title))
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_info),
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        isMoreActionsVisible = false
                                        onAboutClick()
                                    }
                                )
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
        },
        content = content
    )
}