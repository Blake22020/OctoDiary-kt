package org.bxkr.octodiary.screens.navsections.dashboard

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.NavSection
import org.bxkr.octodiary.components.settings.CommonPrefs
import org.bxkr.octodiary.demoScheduleDate
import org.bxkr.octodiary.formatToDay
import org.bxkr.octodiary.formatToHumanDay
import org.bxkr.octodiary.get
import org.bxkr.octodiary.isDemo
import org.bxkr.octodiary.mainPrefs
import org.bxkr.octodiary.navControllerLive
import org.bxkr.octodiary.modalBottomSheetContentLive
import org.bxkr.octodiary.modalBottomSheetStateLive
import org.bxkr.octodiary.parseFromDay
import org.bxkr.octodiary.parseLongDate
import org.bxkr.octodiary.screens.navsections.daybook.DayItem
import org.bxkr.octodiary.screens.navsections.homeworks.PlannerTaskStore
import org.bxkr.octodiary.screens.navsections.homeworks.plannerTasksLive
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val greeting = if (DataService.hasProfile && DataService.profile.children.isNotEmpty() && DataService.currentProfile in DataService.profile.children.indices) {
        stringResource(R.string.home_greeting, DataService.profile.children[DataService.currentProfile].firstName)
    } else {
        stringResource(R.string.home_greeting_generic)
    }
    val now = Date()
    val nextEvent = if (DataService.hasEventCalendar) {
        DataService.eventCalendar.asSequence()
            .filterNot { it.cancelled == true }
            .mapNotNull { event ->
                val start = runCatching { event.startAt.parseLongDate() }.getOrNull() ?: return@mapNotNull null
                val finish = runCatching { event.finishAt.parseLongDate() }.getOrNull() ?: start
                if (finish >= now) event to start else null
            }
            .minByOrNull { it.second.time }
    } else null
    val openHomeworks = if (DataService.hasHomeworks) {
        DataService.homeworks.filterNot { it.isDone }.sortedBy { it.date }.take(3)
    } else emptyList()
    val averages = if (DataService.hasMarksSubject) {
        DataService.marksSubject.mapNotNull { subject ->
            val value = subject.average.replace(',', '.').toDoubleOrNull()
            if (value != null && value in 1.0..5.0) subject to value else null
        }
    } else emptyList()
    val average = averages.map { it.second }.takeIf { it.isNotEmpty() }?.average()
    val focusSubject = averages.minByOrNull { it.second }
    val personalTasks by plannerTasksLive.observeAsState(PlannerTaskStore.read(context))
    val openTaskCount = personalTasks.count { !it.completed } +
        if (DataService.hasHomeworks) DataService.homeworks.count { !it.isDone } else 0

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxHeight(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(greeting, style = MaterialTheme.typography.headlineMedium)
                    Text(now.formatToHumanDay(), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.home_school_day),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .82f)
                    )
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickStat(
                    Modifier.weight(1f),
                    openTaskCount.toString(),
                    stringResource(R.string.home_open_tasks)
                )
                QuickStat(
                    Modifier.weight(1f),
                    average?.let { String.format(Locale.getDefault(), "%.1f", it) } ?: "—",
                    stringResource(R.string.home_grade_average)
                )
            }
        }
        item {
            SectionHeader(stringResource(R.string.home_next_lesson))
            Card(
                Modifier
                    .fillMaxWidth()
                    .clickable { navControllerLive.value?.navigate(NavSection.Daybook.route) },
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (nextEvent == null) {
                        Text(stringResource(R.string.home_no_next_lesson), style = MaterialTheme.typography.titleMedium)
                    } else {
                        val event = nextEvent.first
                        Text(
                            event.subjectName ?: event.lessonName ?: event.title ?: stringResource(R.string.home_next_lesson),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            "${nextEvent.second.formatTime()} · ${nextEvent.second.formatToHumanDay()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val room = listOfNotNull(event.roomName, event.roomNumber).joinToString(" · ")
                        if (room.isNotBlank()) Text(room, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                SectionHeader(stringResource(R.string.home_upcoming_homework))
                TextButton(onClick = { navControllerLive.value?.navigate(NavSection.Homeworks.route) }) {
                    Text(stringResource(R.string.home_view_plan))
                }
            }
        }
        if (openHomeworks.isEmpty()) {
            item {
                Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                    Text(
                        stringResource(R.string.home_no_upcoming_homework),
                        Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(openHomeworks.size) { index ->
                val homework = openHomeworks[index]
                Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(homework.subjectName, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text(homework.description.ifBlank { homework.homework }, style = MaterialTheme.typography.bodyLarge, maxLines = 2)
                        Text(homework.date.parseFromDay().formatToHumanDay(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        if (focusSubject != null) {
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Text(
                        stringResource(
                            R.string.home_focus_subject,
                            focusSubject.first.subjectName,
                            String.format(Locale.getDefault(), "%.1f", focusSubject.second)
                        ),
                        Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        dashboardRatingVisits()
        item { ChangelogCard(context) }
    }
}

@Composable
private fun QuickStat(modifier: Modifier, value: String, label: String) {
    Card(modifier, shape = MaterialTheme.shapes.large) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 4.dp, top = 8.dp))
}

private fun Date.formatTime(): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(this)

fun LazyListScope.dashboardSchedule(context: Context, showNumbers: Boolean) {
    val date = if (context.isDemo) {
        demoScheduleDate
    } else Date()
    item {
        Spacer(Modifier.size(8.dp))
    }
    DayItem(
        day = DataService.eventCalendar.filter { it.startAt.parseLongDate().time > date.time }
            .minByOrNull {
                it.startAt.parseLongDate().time - date.time
            }?.startAt?.parseLongDate()?.formatToDay()?.let { day ->
                DataService.eventCalendar.filter {
                    it.startAt.parseLongDate().formatToDay() == day
                }
            } ?: listOf(), showNumbers, showBreaks = false, reversed = true)
    item {
        val currentDay = remember { date.formatToDay() }
        Column(
            verticalArrangement = Arrangement.Bottom
        ) {
            val todayCalendar = DataService.eventCalendar.filter {
                it.startAt.parseLongDate().formatToDay() == currentDay
            }
            val nearestEvent =
                DataService.eventCalendar.filter { it.startAt.parseLongDate().time > date.time }
                    .minByOrNull {
                        it.startAt.parseLongDate().time - date.time
                    }
            if (todayCalendar.isNotEmpty() && date < todayCalendar.maxBy { it.finishAt.parseLongDate() }.finishAt.parseLongDate()) {
                Text(
                    stringResource(id = R.string.schedule_today),
                    style = MaterialTheme.typography.labelLarge
                )
            } else if (nearestEvent != null) {
                Text(
                    stringResource(
                        id = R.string.schedule_for,
                        nearestEvent.startAt.parseLongDate().formatToHumanDay()
                    ),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

fun LazyListScope.dashboardRatingVisits() {
    item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom)) {
            Column {
                if (LocalContext.current.mainPrefs.get<Boolean>(CommonPrefs.mainRating.prefKey) != false) {
                    Text(
                        stringResource(id = R.string.rating),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                modalBottomSheetContentLive.value = { RankingList() }
                                modalBottomSheetStateLive.postValue(true)
                            }
                    ) {
                        Column(
                            Modifier
                                .padding(16.dp)
                        ) {
                            Text(
                                stringResource(
                                    id = R.string.rating_place,
                                    DataService
                                        .run { ranking.firstOrNull { it.personId == profile.children[currentProfile].contingentGuid } }
                                        ?.rank?.rankPlace ?: "?"
                                )
                            )
                        }
                    }
                }
            }
            Column {
                if (DataService.hasVisits && DataService.visits.payload.isNotEmpty()) {
                    val lastVisit =
                        DataService.visits.payload.filter { day -> !day.visits.any { it.inX == "-" && it.out == "-" } }
                            .maxByOrNull {
                                it.date.parseFromDay().toInstant().toEpochMilli()
                            }
                    if (lastVisit != null) {
                        Text(
                            text = stringResource(
                                R.string.visits_t,
                                lastVisit.date.parseFromDay().formatToHumanDay()
                            ),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Card(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    modalBottomSheetContentLive.value = { VisitsList() }
                                    modalBottomSheetStateLive.postValue(true)
                                }
                        ) {
                            Row(
                                Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(lastVisit.visits[0].inX)
                                Icon(
                                    Icons.AutoMirrored.Rounded.ArrowForward,
                                    stringResource(id = R.string.to)
                                )
                                Text(lastVisit.visits[0].out)
                            }
                        }
                    }
                }
            }
        }
    }
}
