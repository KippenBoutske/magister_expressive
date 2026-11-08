package nl.vibe.magister.ui.home

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import nl.vibe.magister.R
import nl.vibe.magister.data.model.Afspraak
import nl.vibe.magister.ui.UiState
import nl.vibe.magister.ui.components.EmptyView
import nl.vibe.magister.ui.components.ErrorView
import nl.vibe.magister.ui.components.LoadingView
import nl.vibe.magister.ui.haptics.confirm
import nl.vibe.magister.ui.haptics.rememberHaptics
import nl.vibe.magister.ui.navigation.Screen
import nl.vibe.magister.ui.shell.MagisterScaffold
import nl.vibe.magister.util.dayLabel
import nl.vibe.magister.util.hhmm
import nl.vibe.magister.util.now
import nl.vibe.magister.util.parseMagisterDate
import java.time.Duration
import java.time.LocalDate
import java.time.OffsetDateTime

@OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalSharedTransitionApi::class
)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    sharedScope: SharedTransitionScope,
    animatedScope: AnimatedVisibilityScope,
    onOpenDetail: (Long) -> Unit,
    onNavigate: (Screen) -> Unit,
    current: Screen
) {
    val haptics = rememberHaptics()
    val state = viewModel.uiState
    var now by remember { mutableStateOf(now()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            now = now()
        }
    }

    MagisterScaffold(
        title = if (viewModel.userName.isNotBlank()) {
            "${stringResource(R.string.nav_agenda)} \u2022 ${viewModel.userName}"
        } else {
            stringResource(R.string.nav_agenda)
        },
        current = current,
        onNavigate = onNavigate,
        actions = {
            IconButton(onClick = {
                haptics.click()
                viewModel.logout()
            }) {
                Icon(Icons.Default.Logout, contentDescription = stringResource(R.string.logout))
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = viewModel.refreshing,
            onRefresh = {
                haptics.confirm()
                viewModel.load()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val s = state) {
                is UiState.Loading -> LoadingView()
                is UiState.Error -> ErrorView(s.message) { viewModel.load() }
                is UiState.Success -> AgendaList(
                    data = s.data,
                    now = now,
                    sharedScope = sharedScope,
                    animatedScope = animatedScope,
                    onOpenDetail = onOpenDetail
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun AgendaList(
    data: List<Afspraak>,
    now: OffsetDateTime,
    sharedScope: SharedTransitionScope,
    animatedScope: AnimatedVisibilityScope,
    onOpenDetail: (Long) -> Unit
) {
    val current = data.firstOrNull { afspraak ->
        val start = parseMagisterDate(afspraak.start)
        val end = parseMagisterDate(afspraak.einde)
        start != null && end != null && !now.isBefore(start) && now.isBefore(end)
    }

    val grouped = data
        .mapNotNull { a -> parseMagisterDate(a.start)?.let { a to it } }
        .sortedBy { it.second }
        .groupBy { it.second.toLocalDate() }
        .toSortedMap()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
    ) {
        if (current != null) {
            item(key = "current-lesson") {
                CurrentLessonCard(afspraak = current, now = now)
            }
        }
        grouped.forEach { (day, items) ->
            stickyHeader(key = "day-$day") {
                DayHeader(day = day, isToday = day == now.toLocalDate())
            }
            items(
                items = items,
                key = { (afspraak, _) -> "lesson-${afspraak.id}" }
            ) { (afspraak, _) ->
                LessonCard(
                    afspraak = afspraak,
                    sharedScope = sharedScope,
                    animatedScope = animatedScope,
                    onClick = { onOpenDetail(afspraak.id ?: 0L) }
                )
            }
        }
        if (grouped.isEmpty() && current == null) {
            item(key = "empty") {
                EmptyView(stringResource(R.string.empty_agenda))
            }
        }
    }
}

@Composable
private fun DayHeader(day: LocalDate, isToday: Boolean) {
    val label = if (isToday) {
        stringResource(R.string.today)
    } else {
        day.dayLabel()
    }
    Text(
        label,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CurrentLessonCard(afspraak: Afspraak, now: OffsetDateTime) {
    val start = parseMagisterDate(afspraak.start)
    val end = parseMagisterDate(afspraak.einde)
    val fraction = if (start != null && end != null) {
        val total = Duration.between(start, end).toMillis().coerceAtLeast(1L)
        (Duration.between(start, now).toMillis().toFloat() / total).coerceIn(0f, 1f)
    } else {
        0f
    }
    val animated by animateFloatAsState(
        targetValue = fraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "lessonProgress"
    )

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularWavyProgressIndicator(
                progress = { animated },
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.now_playing),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    afspraak.vak ?: afspraak.omschrijving ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${start?.hhmm() ?: "--:--"} - ${end?.hhmm() ?: "--:--"}" +
                        (if (!afspraak.locatie.isNullOrBlank()) " \u2022 ${afspraak.locatie}" else ""),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
