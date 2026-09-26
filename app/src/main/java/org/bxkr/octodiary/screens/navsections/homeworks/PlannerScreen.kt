package org.bxkr.octodiary.screens.navsections.homeworks

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.bxkr.octodiary.DataService
import org.bxkr.octodiary.R
import org.bxkr.octodiary.showFilterLive
import org.bxkr.octodiary.ui.theme.LocalCompactLayout
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

data class PlannerTask(
    val id: String,
    val title: String,
    val subject: String,
    val dueDate: String,
    val completed: Boolean
)

val plannerTasksLive = MutableLiveData<List<PlannerTask>>(emptyList())

object PlannerTaskStore {
    private const val PREFS = "octoplan_planner"
    private const val TASKS = "personal_tasks"
    private val listType = object : TypeToken<List<PlannerTask>>() {}.type

    fun read(context: Context): List<PlannerTask> {
        val json = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(TASKS, null) ?: return emptyList()
        return runCatching {
            Gson().fromJson<List<PlannerTask>>(json, listType) ?: emptyList()
        }.getOrDefault(emptyList())
    }

    fun write(context: Context, tasks: List<PlannerTask>) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(TASKS, Gson().toJson(tasks))
            .apply()
        plannerTasksLive.postValue(tasks)
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen() {
    val context = LocalContext.current
    val compactLayout = LocalCompactLayout.current
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var tasks by remember {
        mutableStateOf(PlannerTaskStore.read(context).also { plannerTasksLive.value = it })
    }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(selectedTab) {
        // The old subject-filter action belongs to the diary list, not the local planner.
        showFilterLive.postValue(false)
    }

    fun saveTasks(updated: List<PlannerTask>) {
        tasks = updated
        PlannerTaskStore.write(context, updated)
    }

    Column(Modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = selectedTab, divider = {}) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(stringResource(R.string.planner_tab_personal)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(stringResource(R.string.planner_tab_diary)) }
            )
        }

        if (selectedTab == 0) {
            PersonalTasks(
                tasks = tasks,
                onAdd = { showAddDialog = true },
                onToggle = { task, completed ->
                    saveTasks(tasks.map { if (it.id == task.id) it.copy(completed = completed) else it })
                },
                onDelete = { task -> saveTasks(tasks.filterNot { it.id == task.id }) }
            )
        } else {
            DiaryAssignments()
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, subject, dueDate ->
                saveTasks(tasks + PlannerTask(UUID.randomUUID().toString(), title, subject, dueDate, false))
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun PersonalTasks(
    tasks: List<PlannerTask>,
    onAdd: () -> Unit,
    onToggle: (PlannerTask, Boolean) -> Unit,
    onDelete: (PlannerTask) -> Unit
) {
    val openCount = tasks.count { !it.completed }
    Column(Modifier.fillMaxSize()) {
        Card(
            Modifier
                .fillMaxWidth()
                .padding(if (compactLayout) 10.dp else 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(if (compactLayout) 14.dp else 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.planner_remaining_count, openCount), style = MaterialTheme.typography.headlineSmall)
                    Text(stringResource(R.string.planner_empty_message), style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.size(12.dp))
                FilledTonalButton(onClick = onAdd) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(Modifier.size(6.dp))
                    Text(stringResource(R.string.planner_add_task))
                }
            }
        }

        if (tasks.isEmpty()) {
            OutlinedCard(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.planner_empty_title), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.planner_empty_message), style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks.sortedWith(compareBy<PlannerTask> { it.completed }.thenBy { it.dueDate }), key = { it.id }) { task ->
                    PersonalTaskCard(task, onToggle = { onToggle(task, it) }, onDelete = { onDelete(task) })
                }
            }
        }
    }
}

@Composable
private fun PersonalTaskCard(task: PlannerTask, onToggle: (Boolean) -> Unit, onDelete: () -> Unit) {
    OutlinedCard(shape = MaterialTheme.shapes.large) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = task.completed, onCheckedChange = onToggle)
            Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else null
                )
                val details = listOfNotNull(
                    task.subject.takeIf { it.isNotBlank() },
                    task.dueDate.takeIf { it.isNotBlank() }?.let(::formatDueDate)
                ).joinToString(" · ")
                if (details.isNotBlank()) {
                    Text(details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.DeleteOutline, contentDescription = stringResource(R.string.delete))
            }
        }
    }
}

@Composable
private fun DiaryAssignments() {
    if (!DataService.hasHomeworks) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Rounded.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.size(12.dp))
            Text(stringResource(R.string.planner_no_homeworks), style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val homeworks = DataService.homeworks.sortedWith(compareBy({ it.date }, { it.subjectName }))
    if (homeworks.isEmpty()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.planner_no_assignments), style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(homeworks, key = { "${it.homeworkEntryId}-${it.homeworkEntryStudentId}" }) { homework ->
                var completed by rememberSaveable(homework.homeworkEntryStudentId) { mutableStateOf(homework.isDone) }
                OutlinedCard(shape = MaterialTheme.shapes.large) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(end = 12.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = completed, onCheckedChange = { value ->
                            completed = value
                            DataService.setHomeworkDoneState(homework.homeworkEntryStudentId, value) {}
                        })
                        Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
                            Text(homework.subjectName, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            Text(
                                homework.description.ifBlank { homework.homework },
                                style = MaterialTheme.typography.bodyLarge,
                                textDecoration = if (completed) TextDecoration.LineThrough else null
                            )
                            Text(
                                "${homework.date} · ${homework.datePreparedFor}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddTaskDialog(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var subject by rememberSaveable { mutableStateOf("") }
    var dueDate by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    val validDate = runCatching { LocalDate.parse(dueDate) }.isSuccess

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.planner_add_task)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.planner_task_title_hint)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text(stringResource(R.string.planner_subject_hint)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text(stringResource(R.string.planner_due_date_hint)) },
                    singleLine = true,
                    isError = !validDate
                )
                if (!validDate) Text(stringResource(R.string.planner_invalid_due_date), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(title.trim(), subject.trim(), dueDate) },
                enabled = title.isNotBlank() && validDate
            ) { Text(stringResource(R.string.planner_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.planner_cancel)) }
        }
    )
}

private fun formatDueDate(value: String): String = runCatching {
    LocalDate.parse(value).format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))
}.getOrDefault(value)
