package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.NoteEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    notes: List<NoteEntity>,
    onSaveNote: (NoteEntity) -> Unit,
    onDeleteNote: (NoteEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var activeNoteToEdit by remember { mutableStateOf<NoteEntity?>(null) }
    var isCreatingNewNote by remember { mutableStateOf(false) }

    val filteredNotes = remember(notes, searchQuery) {
        if (searchQuery.isBlank()) notes else {
            notes.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.content.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isCreatingNewNote = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .testTag("notes_fab_add_note")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            Text(
                text = "Notes",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search notes...") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .testTag("notes_search_input")
            )

            // Notes List or Empty State
            if (filteredNotes.isEmpty()) {
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
                            imageVector = Icons.Outlined.NoteAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Capture your first thought.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Keep quick notes and ideas while you work.",
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
                    items(filteredNotes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { activeNoteToEdit = note },
                            onTogglePin = {
                                onSaveNote(note.copy(isPinned = !note.isPinned))
                            },
                            onDelete = { onDeleteNote(note) }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Note Dialog
    if (isCreatingNewNote || activeNoteToEdit != null) {
        val noteToEdit = activeNoteToEdit
        var title by remember { mutableStateOf(noteToEdit?.title ?: "") }
        var content by remember { mutableStateOf(noteToEdit?.content ?: "") }
        var isPinned by remember { mutableStateOf(noteToEdit?.isPinned ?: false) }

        AlertDialog(
            onDismissRequest = {
                isCreatingNewNote = false
                activeNoteToEdit = null
            },
            title = {
                Text(
                    text = if (noteToEdit != null) "Edit Note" else "New Note",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        placeholder = { Text("e.g. Ideas for the future") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Content") },
                        placeholder = { Text("Write your thoughts...") },
                        minLines = 4,
                        maxLines = 8,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Pin to top",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = isPinned,
                            onCheckedChange = { isPinned = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() || content.isNotBlank()) {
                            val entity = noteToEdit?.copy(
                                title = title.ifBlank { "Untitled Note" },
                                content = content,
                                isPinned = isPinned,
                                updatedAt = System.currentTimeMillis()
                            ) ?: NoteEntity(
                                title = title.ifBlank { "Untitled Note" },
                                content = content,
                                isPinned = isPinned
                            )
                            onSaveNote(entity)
                        }
                        isCreatingNewNote = false
                        activeNoteToEdit = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        isCreatingNewNote = false
                        activeNoteToEdit = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun NoteCard(
    note: NoteEntity,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateStr = remember(note.updatedAt) {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        sdf.format(Date(note.updatedAt))
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (note.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (note.isPinned) "Unpin note" else "Pin note",
                            tint = if (note.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Delete note",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (note.content.isNotBlank()) {
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = dateStr,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
