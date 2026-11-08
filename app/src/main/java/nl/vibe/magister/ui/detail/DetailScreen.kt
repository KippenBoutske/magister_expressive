package nl.vibe.magister.ui.detail

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedContentState
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.rememberSharedContentState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import nl.vibe.magister.R
import nl.vibe.magister.ui.UiState
import nl.vibe.magister.ui.haptics.click
import nl.vibe.magister.ui.haptics.confirm
import nl.vibe.magister.ui.home.HomeViewModel
import nl.vibe.magister.util.hhmm
import nl.vibe.magister.util.parseMagisterDate

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun DetailScreen(
    id: Long,
    homeViewModel: HomeViewModel,
    sharedScope: SharedTransitionScope,
    animatedScope: AnimatedVisibilityScope,
    onBack: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val state = homeViewModel.uiState
    val afspraak = (state as? UiState.Success)?.data?.firstOrNull { it.id == id }

    Scaffold(
        topBar = {
            MediumFlexibleTopAppBar(
                title = {
                    if (afspraak != null) {
                        with(sharedScope) {
                            Text(
                                text = afspraak.vak ?: afspraak.omschrijving ?: "",
                                modifier = Modifier.sharedBounds(
                                    sharedContentState = rememberSharedContentState(
                                        key = SharedContentState.Key("lesson-$id")
                                    ),
                                    animatedVisibilityScope = animatedScope
                                )
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        haptics.click()
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
            )
        }
    ) { padding ->
        if (afspraak == null) {
            LoadingIndicator(modifier = Modifier.padding(padding))
        } else {
            val start = parseMagisterDate(afspraak.start)
            val end = parseMagisterDate(afspraak.einde)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "${start?.hhmm() ?: "--:--"} - ${end?.hhmm() ?: "--:--"}",
                    style = MaterialTheme.typography.displaySmall
                )

                InfoRow(icon = Icons.Default.Person, text = afspraak.docent ?: "-")
                InfoRow(icon = Icons.Default.LocationOn, text = afspraak.locatie ?: "-")

                if (!afspraak.opdracht.isNullOrBlank() || !afspraak.instructie.isNullOrBlank()) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                stringResource(R.string.homework_label),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                            if (!afspraak.opdracht.isNullOrBlank()) {
                                Text(
                                    afspraak.opdracht,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            if (!afspraak.instructie.isNullOrBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    afspraak.instructie,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        haptics.confirm()
                        homeViewModel.startLiveUpdate(afspraak)
                    },
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(stringResource(R.string.start_live_update))
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
