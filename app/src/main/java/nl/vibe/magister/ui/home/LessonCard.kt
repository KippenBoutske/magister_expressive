package nl.vibe.magister.ui.home

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.animation.AnimatedVisibilityScope
import nl.vibe.magister.R
import nl.vibe.magister.data.model.Afspraak
import nl.vibe.magister.ui.haptics.click
import nl.vibe.magister.ui.haptics.rememberHaptics
import nl.vibe.magister.util.hhmm
import nl.vibe.magister.util.parseMagisterDate

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun LessonCard(
    afspraak: Afspraak,
    sharedScope: SharedTransitionScope,
    animatedScope: AnimatedVisibilityScope,
    onClick: () -> Unit
) {
    val haptics = rememberHaptics()
    val start = parseMagisterDate(afspraak.start)
    val end = parseMagisterDate(afspraak.einde)
    val title = afspraak.vak ?: afspraak.omschrijving ?: stringResource(R.string.lesson_label)

    Card(
        onClick = {
            haptics.click()
            onClick()
        },
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    start?.hhmm() ?: "--:--",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    end?.hhmm() ?: "--:--",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                with(sharedScope) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.sharedBounds(
                            sharedContentState = rememberSharedContentState(
                                key = SharedTransitionScope.SharedContentState.Key("lesson-${afspraak.id}")
                            ),
                            animatedVisibilityScope = animatedScope
                        )
                    )
                }
                Spacer(Modifier.height(2.dp))
                val subtitle = listOfNotNull(
                    afspraak.docentAfkorting ?: afspraak.docent,
                    afspraak.locatie
                ).joinToString(" \u2022 ")
                if (subtitle.isNotBlank()) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (afspraak.isHomework) {
                    Spacer(Modifier.height(6.dp))
                    AssistChip(
                        onClick = {},
                        label = { Text(stringResource(R.string.homework_label)) }
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
