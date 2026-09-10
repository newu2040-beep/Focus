package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.TaskCategory
import com.example.data.model.TaskEntity
import com.example.ui.components.TaskItemRow

enum class TaskFilterTab(val label: String) {
    TODAY("Today"),
    UPCOMING("Upcoming"),
    ALL("All"),
    COMPLETED("Completed")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    tasks: List<TaskEntity>,
    onToggleTaskComplete: (TaskEntity) -> Unit,
    onEditTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onStartFocusWithTask: (TaskEntity) -> Unit,
    onOpenAddTask: () -> Unit,
    onClearCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(TaskFilterTab.TODAY) }
    var selectedCategory by remember { mutableStateOf<TaskCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    val filteredTasks = remember(tasks, selectedTab, selectedCategory, searchQuery) {
        tasks.filter { task ->
            // Tab filter
            val matchesTab = when (selectedTab) {
                TaskFilterTab.TODAY -> !task.isCompleted
                TaskFilterTab.UPCOMING -> !task.isCompleted && task.dueDate != null
                TaskFilterTab.ALL -> true
                TaskFilterTab.COMPLETED -> task.isCompleted
            }

            // Category filter
            val matchesCategory = selectedCategory == null || task.category == selectedCategory

            // Search query
            val matchesSearch = if (searchQuery.isBlank()) true else {
                task.title.contains(searchQuery, ignoreCase = true) ||
                        task.notes.contains(searchQuery, ignoreCase = true)
            }

            matchesTab && matchesCategory && matchesSearch
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddTask,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .testTag("tasks_fab_add_task")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header & Search
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tasks",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { isSearchExpanded = !isSearchExpanded }) {
                        Icon(
                            imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Outlined.Search,
                            contentDescription = "Search tasks",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (selectedTab == TaskFilterTab.COMPLETED && tasks.any { it.isCompleted }) {
                        IconButton(onClick = onClearCompleted) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteSweep,
                                contentDescription = "Clear completed",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Search input field if expanded
            AnimatedVisibility(visible = isSearchExpanded) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by title or notes...") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    }
                )
            }

            // Tab Filter Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TaskFilterTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { selectedTab = tab }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Category Chips Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All Categories") },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
                items(TaskCategory.values()) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                        label = { Text(cat.displayName) },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            // Task List or Empty State
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DoneAll,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = when (selectedTab) {
                                TaskFilterTab.COMPLETED -> "No completed tasks yet."
                                TaskFilterTab.UPCOMING -> "No upcoming scheduled tasks."
                                else -> "Your day is clear."
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap + to add your next focus item.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskItemRow(
                            task = task,
                            onToggleComplete = { onToggleTaskComplete(task) },
                            onEdit = { onEditTask(task) },
                            onDelete = { onDeleteTask(task) },
                            onStartFocus = { onStartFocusWithTask(task) }
                        )
                    }
                }
            }
        }
    }
}
