package com.example.studenttaskmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 1. DATA MODEL
data class StudentTask(
    val id: Long,
    val title: String,
    val subject: String,
    val isComplete: Boolean = false
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // Surface provides the background color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StudentTaskManagerApp()
                }
            }
        }
    }
}

// 2. MAIN APP SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentTaskManagerApp() {
    var tasks by remember { mutableStateOf(listOf<StudentTask>()) }
    var taskTitle by remember { mutableStateOf("") }
    var taskSubject by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Student Planner", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Input Fields
            OutlinedTextField(
                value = taskTitle,
                onValueChange = { taskTitle = it },
                label = { Text("Task Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = taskSubject,
                onValueChange = { taskSubject = it },
                label = { Text("Subject (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Add Button
            Button(
                onClick = {
                    if (taskTitle.isNotBlank()) {
                        val newTask = StudentTask(
                            id = System.currentTimeMillis(),
                            title = taskTitle,
                            subject = taskSubject
                        )
                        tasks = tasks + newTask
                        taskTitle = ""
                        taskSubject = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Add Task", modifier = Modifier.padding(start = 8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Active Tasks", fontSize = 18.sp, fontWeight = FontWeight.Bold)

            // Task List
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(tasks) { task ->
                    TaskItem(
                        task = task,
                        onDelete = { tasks = tasks.filter { it.id != task.id } },
                        onToggle = {
                            tasks = tasks.map {
                                if (it.id == task.id) it.copy(isComplete = !it.isComplete) else it
                            }
                        }
                    )
                }
            }
        }
    }
}

// 3. INDIVIDUAL TASK ITEM
@Composable
fun TaskItem(task: StudentTask, onDelete: () -> Unit, onToggle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = task.isComplete, onCheckedChange = { onToggle() })
            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (task.isComplete) TextDecoration.LineThrough else null
                )
                if (task.subject.isNotEmpty()) {
                    Text(text = task.subject, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFD32F2F))
            }
        }
    }
}
