package nl.vibe.magister.ui.homework

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
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
import nl.vibe.magister.util.parseMagisterDate

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)
@Composable
fun HomeworkScreen(
    viewModel: HomeworkViewModel,
    onNavigate: (Screen) -> Unit,
    current: Screen
) {
    val haptics = rememberHaptics()
    val state = viewModel.uiState

    MagisterScaffold(
        title = stringResource(R.string.nav_huiswerk),
        current = current,
        onNavigate = onNavigate
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
                is UiState.Success -> HomeworkList(
                    data = s.data,
                    doneIds = viewModel.doneIds,
                    onToggle = { afspraak ->
                        haptics.confirm()
                        viewModel.toggle(afspraak)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeworkList(
    data: List<Afspraak>,
    doneIds: Set<Long>,
    onToggle: (Afspraak) -> Unit
) {
    val sorted = data.sortedBy { parseMagisterDate(it.start) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
    ) {
        items(
            items = sorted,
            key = { "hw-${it.id}" }
        ) { afspraak ->
            val done = afspraak.id != null && afspraak.id in doneIds
            val start = parseMagisterDate(afspraak.start)
            ListItem(
                headlineContent = {
                    Text(
                        text = afspraak.vak ?: afspraak.omschrijving ?: "-",
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (done) TextDecoration.LineThrough else null
                    )
                },
                supportingContent = {
                    val date = start?.dayLabel()
                    val time = start?.hhmm()
                    val text = listOfNotNull(
                        if (!afspraak.opdracht.isNullOrBlank()) afspraak.opdracht else null,
                        listOfNotNull(date, time).joinToString(" \u2022 ").ifBlank { null }
                    ).joinToString(" \u2014 ")
                    Text(
                        text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = if (done) TextDecoration.LineThrough else null
                    )
                },
                trailingContent = {
                    Checkbox(
                        checked = done,
                        onCheckedChange = { onToggle(afspraak) }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )
        }
        if (sorted.isEmpty()) {
            item(key = "empty") {
                EmptyView(stringResource(R.string.empty_homework))
            }
        }
    }
}
