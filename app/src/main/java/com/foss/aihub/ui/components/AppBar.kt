package com.foss.aihub.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.foss.aihub.R
import com.foss.aihub.models.AiService
import com.foss.aihub.ui.screens.dialogs.ActiveServicesDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiHubAppBar(
    selectedService: AiService,
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onHomeClick: () -> Unit = {},
    onClearSiteData: () -> Unit,
    onKillService: (AiService) -> Unit,
    loadedServiceNames: Set<String>,
    allServices: List<AiService>,
    onReload: (AiService) -> Unit,
    onServiceSelected: (AiService) -> Unit,
    isLoading: Boolean = false,
    loadingProgress: Int = 0,
    loadingColor: Color = colorScheme.primary
) {
    var showServicesDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var expanded: Boolean by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "icon_rotation"
    )

    // Wrap app bar and loading line in a Column so the line sits below
    Column(modifier = Modifier.fillMaxWidth()) {
        TopAppBar(
            title = {
                AnimatedContent(
                    targetState = selectedService, transitionSpec = {
                        fadeIn(animationSpec = tween(220, delayMillis = 90)) togetherWith fadeOut(
                            animationSpec = tween(90)
                        )
                    }, label = "service_title"
                ) { service ->
                    Text(
                        text = service.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.animateContentSize()
                    )
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = onMenuClick, modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        Icons.Rounded.Menu,
                        contentDescription = stringResource(R.string.label_menu),
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            actions = {
                BadgedBox(
                    badge = {
                        if (loadedServiceNames.isNotEmpty()) {
                            Badge(
                                containerColor = colorScheme.primary,
                                contentColor = colorScheme.onPrimary
                            ) {
                                Text(
                                    text = loadedServiceNames.size.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    },
                ) {
                    IconButton(
                        onClick = { showServicesDialog = true },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Apps,
                            contentDescription = stringResource(R.string.title_active_ai_services),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { expanded = true },
                        interactionSource = interactionSource,
                        modifier = Modifier.rotate(rotation)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = stringResource(R.string.action_more_options),
                            tint = colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.width(200.dp),
                        shape = RoundedCornerShape(16.dp),
                        containerColor = colorScheme.surfaceContainer,
                        tonalElevation = 2.dp
                    ) {
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Replay,
                                    contentDescription = null,
                                    tint = colorScheme.onSurfaceVariant
                                )
                            },
                            text = {
                                Text(
                                    text = stringResource(R.string.action_reload),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            onClick = {
                                expanded = false
                                onReload(selectedService)
                            },
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = colorScheme.onSurfaceVariant
                                )
                            },
                            text = {
                                Text(
                                    text = stringResource(R.string.title_settings),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            onClick = {
                                expanded = false
                                onSettingsClick()
                            },
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = colorScheme.onSurfaceVariant
                                )
                            },
                            text = {
                                Text(
                                    text = stringResource(R.string.action_clear_site_data),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            onClick = {
                                expanded = false
                                showClearDataDialog = true
                            },
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Home,
                                    contentDescription = null,
                                    tint = colorScheme.onSurfaceVariant
                                )
                            },
                            text = {
                                Text(
                                    text = "Dashboard",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            onClick = {
                                expanded = false
                                onHomeClick()
                            },
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Info,
                                    contentDescription = null,
                                    tint = colorScheme.onSurfaceVariant
                                )
                            },
                            text = {
                                Text(
                                    text = stringResource(R.string.section_about),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            onClick = {
                                expanded = false
                                onAboutClick()
                            },
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorScheme.surface,
                scrolledContainerColor = colorScheme.surfaceContainer,
                titleContentColor = colorScheme.onSurface,
                navigationIconContentColor = colorScheme.onSurfaceVariant,
                actionIconContentColor = colorScheme.onSurfaceVariant
            ),
            windowInsets = TopAppBarDefaults.windowInsets,
        )

        LoadingLine(
            isVisible = isLoading,
            accentColor = loadingColor,
            progress = loadingProgress,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showServicesDialog) {
        ActiveServicesDialog(
            activeServices = loadedServiceNames.mapNotNull { name -> allServices.find { it.name == name } },
            selectedServiceId = selectedService.name,
            onServiceSelected = { service ->
                onServiceSelected(service)
                showServicesDialog = false
            },
            onKillService = onKillService,
            onDismiss = { showServicesDialog = false },
        )
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.action_clear_site_data),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = stringResource(
                        R.string.msg_clear_site_data_warning, selectedService.name
                    ), style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDataDialog = false
                        onClearSiteData()
                    },
                ) {
                    Text(
                        text = stringResource(R.string.action_clear),
                        color = colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text(text = stringResource(R.string.action_close))
                }
            },
            containerColor = colorScheme.surfaceContainerHigh,
            titleContentColor = colorScheme.onSurface,
            textContentColor = colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LoadingLine(
    modifier: Modifier = Modifier, isVisible: Boolean, accentColor: Color, progress: Int = 0
) {
    val animatedFraction by animateFloatAsState(
        targetValue = progress / 100f,
        animationSpec = tween(durationMillis = 600),
        label = "progress"
    )

    androidx.compose.animation.AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(accentColor.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction)
                    .height(4.dp)
                    .background(accentColor)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Md3TopAppBar(
    title: String,
    onBack: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title, style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.action_back)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorScheme.surface,
            scrolledContainerColor = colorScheme.surfaceColorAtElevation(3.dp),
            titleContentColor = colorScheme.onSurface,
            navigationIconContentColor = colorScheme.onSurface,
            actionIconContentColor = colorScheme.onSurface,
        ),
        scrollBehavior = scrollBehavior,
        actions = actions,
    )
}