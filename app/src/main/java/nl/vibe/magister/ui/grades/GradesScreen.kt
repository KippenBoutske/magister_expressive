package nl.vibe.magister.ui.grades

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import nl.vibe.magister.R
import nl.vibe.magister.data.model.Cijfer
import nl.vibe.magister.ui.UiState
import nl.vibe.magister.ui.components.EmptyView
import nl.vibe.magister.ui.components.ErrorView
import nl.vibe.magister.ui.components.LoadingView
import nl.vibe.magister.ui.haptics.click
import nl.vibe.magister.ui.haptics.rememberHaptics
import nl.vibe.magister.ui.haptics.confirm
import nl.vibe.magister.ui.navigation.Screen
import nl.vibe.magister.ui.shell.MagisterScaffold
import nl.vibe.magister.util.dayLabel
import nl.vibe.magister.util.parseMagisterDate
import java.util.Locale

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)
@Composable
fun GradesScreen(
    viewModel: GradesViewModel,
    onNavigate: (Screen) -> Unit,
    current: Screen
) {
    val haptics = rememberHaptics()
    val state = viewModel.uiState

    MagisterScaffold(
        title = stringResource(R.string.nav_cijfers),
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
                is UiState.Success -> GradesList(s.data)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
private fun GradesList(data: List<Cijfer>) {
    val average = data.mapNotNull { it.value }.average().takeIf { data.any { c -> c.value != null } }
    val grouped = data
        .sortedByDescending { parseMagisterDate(it.datumInvoer) }
        .groupBy { it.vak ?: it.vakAfkorting ?: "-" }
        .toSortedMap()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
    ) {
        if (average != null) {
            item(key = "average") {
                AverageCard(average = average)
            }
        }
        grouped.forEach { (vak, cijfers) ->
            stickyHeader(key = "vak-$vak") {
                Text(
                    vak,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
            items(
                items = cijfers,
                key = { "grade-${it.id ?: it.hashCode()}" }
            ) { cijfer ->
                GradeRow(cijfer = cijfer)
            }
        }
        if (grouped.isEmpty()) {
            item(key = "empty") {
                EmptyView(stringResource(R.string.empty_grades))
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AverageCard(average: Double) {
    val animated by animateFloatAsState(
        targetValue = average.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "average"
    )

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.average),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                AnimatedContent(
                    targetState = String.format(Locale("nl", "NL"), "%.1f", animated),
                    transitionSpec = {
                        fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) togetherWith
                            fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow))
                    },
                    label = "averageValue"
                ) { value ->
                    Text(
                        value,
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun GradeRow(cijfer: Cijfer) {
    val haptics = rememberHaptics()
    val (container, onContainer) = gradeColors(cijfer.value)
    val date = parseMagisterDate(cijfer.datumInvoer)?.dayLabel()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = container,
            modifier = Modifier.size(width = 56.dp, height = 44.dp)
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    cijfer.display,
                    style = MaterialTheme.typography.titleMedium,
                    color = onContainer,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                cijfer.omschrijving ?: cijfer.kolomKop ?: "-",
                style = MaterialTheme.typography.bodyLarge
            )
            val meta = listOfNotNull(
                cijfer.kolomKop,
                date,
                cijfer.weging?.let { "weging ${it.toString().replace('.', ',')}" }
            ).joinToString(" \u2022 ")
            if (meta.isNotBlank()) {
                Text(
                    meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = container,
            onClick = { haptics.click() }
        ) {
            Text(
                cijfer.display,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge,
                color = onContainer
            )
        }
    }
}

@Composable
private fun gradeColors(value: Double?): Pair<Color, Color> {
    val scheme = MaterialTheme.colorScheme
    return when {
        value == null -> scheme.surfaceVariant to scheme.onSurfaceVariant
        value >= 7.0 -> scheme.primaryContainer to scheme.onPrimaryContainer
        value >= 5.5 -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        else -> scheme.errorContainer to scheme.onErrorContainer
    }
}
