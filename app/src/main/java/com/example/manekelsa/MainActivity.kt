package com.example.manekelsa

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.firestore.FirebaseFirestore

// ─── Data Model ───────────────────────────────────────────────
data class Worker(
    var id: String = "",
    var name: String = "",
    var skill: String = "",
    var dailyRate: String = "",
    var phone: String = "",
    var area: String = "",
    var isAvailable: Boolean = false,
    var thumbsUp: Int = 0
)

// ─── Main Entry Point ─────────────────────────────────────────
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppNavigation()
            }
        }
    }
}

// ─── Navigation ───────────────────────────────────────────────
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("add") { AddWorkerScreen(navController) }
    }
}

// ─── Home Screen ──────────────────────────────────────────────
@Composable
fun HomeScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var workers by remember { mutableStateOf(listOf<Worker>()) }

    // Real-time Firebase listener
    LaunchedEffect(Unit) {
        db.collection("workers").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val list = snapshot.documents.map { doc ->
                    val w = doc.toObject(Worker::class.java) ?: Worker()
                    w.id = doc.id
                    w
                }.sortedByDescending { it.isAvailable }
                workers = list
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // Top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "ಮನೆ ಕೆಲಸ — Mane Kelsa",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
        }

        // Add Worker Button
        Button(
            onClick = { navController.navigate("add") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("+ ಕೆಲಸಗಾರರನ್ನು ಸೇರಿಸಿ  (Add Worker)", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (workers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("ಯಾರೂ ಇಲ್ಲ — No workers yet", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(workers) { worker ->
                    WorkerCard(worker)
                }
            }
        }
    }
}

// ─── Worker Card ──────────────────────────────────────────────
@Composable
fun WorkerCard(worker: Worker) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    var thumbs by remember { mutableIntStateOf(worker.thumbsUp) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Name + availability
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = worker.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (worker.isAvailable) "✅ ಲಭ್ಯ" else "❌ ಇಲ್ಲ",
                    fontSize = 14.sp,
                    color = if (worker.isAvailable) Color(0xFF2E7D32) else Color.Red
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("🛠 ${worker.skill}", fontSize = 15.sp, color = Color(0xFF555555))
            Text("💰 ${worker.dailyRate}", fontSize = 15.sp, color = Color(0xFF1565C0))
            Text("📍 ${worker.area}", fontSize = 13.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {

                // Call button
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${worker.phone}"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("📞 ಕರೆ ಮಾಡಿ", fontSize = 14.sp)
                }

                // Thumbs Up button
                OutlinedButton(
                    onClick = {
                        val newCount = thumbs + 1
                        thumbs = newCount
                        db.collection("workers").document(worker.id)
                            .update("thumbsUp", newCount)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("👍 $thumbs", fontSize = 14.sp)
                }
            }
        }
    }
}

// ─── Add Worker Screen ────────────────────────────────────────
@Composable
fun AddWorkerScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var skill by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var isAvailable by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "ಕೆಲಸಗಾರರ ನೋಂದಣಿ",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1565C0)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("ಹೆಸರು (Name)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("ಫೋನ್ ನಂಬರ್ (Phone)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = skill,
            onValueChange = { skill = it },
            label = { Text("ಕೌಶಲ್ಯ: Cleaning / Gardening / Cooking") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = rate,
            onValueChange = { rate = it },
            label = { Text("ದೈನಿಕ ದರ e.g. ₹400/day") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = area,
            onValueChange = { area = it },
            label = { Text("ಪ್ರದೇಶ (Area/Street)") },
            modifier = Modifier.fillMaxWidth()
        )

        // Availability toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("ಇಂದು ಲಭ್ಯ? (Available today?)", fontSize = 16.sp)
            Switch(checked = isAvailable, onCheckedChange = { isAvailable = it })
        }

        // Error message
        if (errorMsg.isNotEmpty()) {
            Text(errorMsg, color = Color.Red, fontSize = 14.sp)
        }

        // Save button
        Button(
            onClick = {
                if (name.isEmpty() || phone.isEmpty() || skill.isEmpty()) {
                    errorMsg = "❌ ದಯವಿಟ್ಟು ಎಲ್ಲಾ ಮಾಹಿತಿ ತುಂಬಿ (Fill all fields)"
                    return@Button
                }
                isSaving = true
                val worker = Worker(
                    name = name,
                    phone = phone,
                    skill = skill,
                    dailyRate = rate,
                    area = area,
                    isAvailable = isAvailable,
                    thumbsUp = 0
                )
                db.collection("workers").add(worker)
                    .addOnSuccessListener { navController.popBackStack() }
                    .addOnFailureListener {
                        errorMsg = "❌ Error: ${it.message}"
                        isSaving = false
                    }
            },
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(if (isSaving) "ಉಳಿಸುತ್ತಿದೆ..." else "💾 ಉಳಿಸಿ (Save)", fontSize = 16.sp)
        }
    }
}