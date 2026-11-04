package com.kairos.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kairos.app.ui.theme.KairosTheme

data class CapsuleUi(
    val title: String,
    val kind: String,
    val unlockLabel: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { KairosTheme { KairosApp() } }
    }
}

@Composable
private fun KairosApp() {
    var showCreate by remember { mutableStateOf(false) }
    var dark by remember { mutableStateOf(false) }
    val capsules = remember {
        mutableStateListOf(
            CapsuleUi("A note to my future self", "Letter", "Opens in 4 years", Icons.Default.Mail),
            CapsuleUi("The sound of this summer", "Voice", "Opens in 1 year", Icons.Default.Mic),
            CapsuleUi("One family moment", "Photo", "Ready to open", Icons.Default.Image)
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreate = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) { Icon(Icons.Default.Add, contentDescription = "New capsule") }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("⌛  KAIROS", style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text("A letter to the future", fontSize = 28.sp,
                        fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold)
                }
                TextButton(onClick = { dark = !dark }) { Text(if (dark) "Light" else "Dark") }
            }
            Text("${capsules.size} capsules sealed", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            if (capsules.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Your future is waiting", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(8.dp))
                        Text("Seal a letter, voice note, or photo for a moment yet to come.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(capsules) { capsule ->
                        CapsuleCard(capsule)
                    }
                }
            }
        }
    }

    if (showCreate) {
        var title by remember { mutableStateOf("") }
        var kind by remember { mutableStateOf("Letter") }
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("New capsule") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it },
                        label = { Text("Title") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Letter", "Voice", "Photo").forEach {
                            FilterChip(selected = kind == it, onClick = { kind = it }, label = { Text(it) })
                        }
                    }
                    Text("This is an interface preview. Secure content capture is the next milestone.",
                        style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (title.isNotBlank()) {
                        val icon = when (kind) {
                            "Voice" -> Icons.Default.Mic
                            "Photo" -> Icons.Default.Image
                            else -> Icons.Default.Mail
                        }
                        capsules.add(CapsuleUi(title.trim(), kind, "Draft preview", icon))
                    }
                    showCreate = false
                }) { Text("Create preview") }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun CapsuleCard(capsule: CapsuleUi) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(52.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) { Icon(capsule.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(capsule.title, fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Text(capsule.kind, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium)
            }
            Column(horizontalAlignment = Alignment.End) {
                Icon(Icons.Default.Lock, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary)
                Text(capsule.unlockLabel, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
