package com.devsoftware.kairos

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devsoftware.kairos.ui.theme.KairosTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devsoftware.kairos.data.CapsuleRepository
import com.devsoftware.kairos.data.CapsuleDraft
import com.devsoftware.kairos.notifications.ReminderScheduler
import com.devsoftware.kairos.security.BiometricGate
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class CapsuleType(val label: String, val icon: ImageVector) {
    LETTER("Mektup", Icons.Default.Mail),
    VOICE("Ses", Icons.Default.Mic),
    PHOTO("Fotoğraf", Icons.Default.Image)
}
private data class Capsule(
    val id: String,
    val title: String,
    val type: CapsuleType,
    val message: String,
    val unlockAt: Long,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isReady: Boolean get() = System.currentTimeMillis() >= unlockAt
}
private enum class Page { HOME, CREATE, DETAIL, SETTINGS }

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { KairosTheme { KairosApp(this) } }
    }
}

@Composable
private fun KairosApp(activity: FragmentActivity) {
    var dark by remember { mutableStateOf(false) }
    var page by remember { mutableStateOf(Page.HOME) }
    var selected by remember { mutableStateOf<Capsule?>(null) }
    var openedContent by remember { mutableStateOf<String?>(null) }
    var detailStatus by remember { mutableStateOf<String?>(null) }
    val repository = remember { CapsuleRepository(activity) }
    val rows by repository.observeCapsules().collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()
    val capsules = remember { mutableStateListOf<Capsule>() }
    LaunchedEffect(rows) {
        val summaries = runCatching { repository.summaries(rows) }.getOrDefault(emptyList())
        capsules.clear()
        capsules.addAll(summaries.map { row ->
            Capsule(row.id, row.title,
                when (row.type) { "VOICE" -> CapsuleType.VOICE; "PHOTO" -> CapsuleType.PHOTO; else -> CapsuleType.LETTER },
                "", row.unlockAt, row.createdAt)
        })
    }

    MaterialTheme(
        colorScheme = if (dark) darkColorScheme(
            primary = androidx.compose.ui.graphics.Color(0xFFF2B04A),
            onPrimary = androidx.compose.ui.graphics.Color(0xFF241704),
            background = androidx.compose.ui.graphics.Color(0xFF101923),
            surface = androidx.compose.ui.graphics.Color(0xFF1B2935),
            secondaryContainer = androidx.compose.ui.graphics.Color(0xFF46351F)
        ) else lightColorScheme(
            primary = androidx.compose.ui.graphics.Color(0xFF9A5B13),
            onPrimary = androidx.compose.ui.graphics.Color.White,
            background = androidx.compose.ui.graphics.Color(0xFFF3EFE8),
            surface = androidx.compose.ui.graphics.Color(0xFFFFFDF9),
            secondaryContainer = androidx.compose.ui.graphics.Color(0xFFF6E3C4),
            onSurface = androidx.compose.ui.graphics.Color(0xFF211D20)
        )
    ) {
        when (page) {
            Page.HOME -> HomePage(
                capsules = capsules,
                dark = dark,
                onTheme = { dark = !dark },
                onCreate = { page = Page.CREATE },
                onSettings = { page = Page.SETTINGS },
                onOpen = { selected = it; openedContent = null; detailStatus = null; page = Page.DETAIL }
            )
            Page.CREATE -> CreatePage(
                onBack = { page = Page.HOME },
                onSave = { title, type, message, unlock ->
                    scope.launch {
                        runCatching {
                            val id = repository.create(CapsuleDraft(title, type.name, unlock, message.toByteArray(Charsets.UTF_8)))
                            ReminderScheduler.schedule(activity, id, title, unlock)
                        }.onSuccess { page = Page.HOME }
                    }
                }
            )
            Page.DETAIL -> selected?.let { capsule ->
                DetailPage(capsule, openedContent, detailStatus,
                    onBack = { page = Page.HOME },
                    onOpen = {
                        if (!capsule.isReady) {
                            detailStatus = "Açılma tarihi henüz gelmedi. Bu kontrol cihaz saatine dayanır; kriptografik zaman kilidi değildir."
                        } else {
                            BiometricGate.authenticate(activity,
                                onSuccess = {
                                    scope.launch {
                                        runCatching { repository.readPayload(capsule.id) }
                                            .onSuccess { bytes ->
                                                if (capsule.type == CapsuleType.LETTER) openedContent = bytes.toString(Charsets.UTF_8)
                                                else detailStatus = "Bu kapsül türünün içerik oynatıcısı sonraki entegrasyonda tamamlanacak."
                                            }
                                            .onFailure { detailStatus = "Kapsül açılamadı: ${it.localizedMessage}" }
                                    }
                                },
                                onError = { detailStatus = it }
                            )
                        }
                    }
                )
            } ?: run { page = Page.HOME }
            Page.SETTINGS -> SettingsPage(dark, onTheme = { dark = !dark }, onBack = { page = Page.HOME })
        }
    }
}

@Composable
private fun HomePage(
    capsules: List<Capsule>,
    dark: Boolean,
    onTheme: () -> Unit,
    onCreate: () -> Unit,
    onSettings: () -> Unit,
    onOpen: (Capsule) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreate,
                shape = RoundedCornerShape(22.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) { Icon(Icons.Default.Add, contentDescription = "Yeni kapsül", modifier = Modifier.size(30.dp)) }
        }
    ) { insets ->
        Column(Modifier.fillMaxSize().padding(insets).padding(horizontal = 20.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("⌛  KAIROS", color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Geleceğe bir mektup", fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold, fontSize = 29.sp, lineHeight = 34.sp,
                        color = MaterialTheme.colorScheme.onBackground)
                }
                IconButton(onClick = onTheme) {
                    Icon(if (dark) Icons.Default.WbSunny else Icons.Default.DarkMode,
                        contentDescription = "Tema değiştir", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Ayarlar",
                        tint = MaterialTheme.colorScheme.primary)
                }
            }
            Text("${capsules.size} kapsül mühürlendi",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(20.dp))
            if (capsules.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⌛", fontSize = 54.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("Gelecek seni bekliyor", fontFamily = FontFamily.Serif,
                            fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text("Bir mektubu, ses kaydını veya fotoğrafı gelecekteki bir an için mühürle.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(18.dp))
                        Button(onClick = onCreate) { Text("İlk kapsülünü oluştur") }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(13.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(capsules, key = { it.id }) { capsule ->
                        CapsuleCard(capsule, onClick = { onOpen(capsule) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CapsuleCard(capsule: Capsule, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(23.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 17.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(66.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(capsule.type.icon, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(29.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(capsule.title, fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold, fontSize = 19.sp, lineHeight = 24.sp,
                    maxLines = 2)
                Spacer(Modifier.height(3.dp))
                Text(capsule.type.label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(6.dp))
            Column(horizontalAlignment = Alignment.End) {
                Icon(Icons.Default.Lock, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(27.dp))
                Spacer(Modifier.height(5.dp))
                Text(if (capsule.isReady) "Açılmaya hazır" else remainingLabel(capsule.unlockAt),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium, maxLines = 2)
            }
        }
    }
}

@Composable
private fun CreatePage(
    onBack: () -> Unit,
    onSave: (String, CapsuleType, String, Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(CapsuleType.LETTER) }
    var date by remember {
        mutableStateOf(SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR"))
            .format(Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000)))
    }
    var error by remember { mutableStateOf(false) }
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            TopBar("Yeni kapsül", onBack)
            Text("Neyi geleceğe bırakmak istiyorsun?", fontFamily = FontFamily.Serif,
                fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))
            Text("KAPSÜL TÜRÜ", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CapsuleType.values().forEach { item ->
                    FilterChip(
                        selected = type == item,
                        onClick = { type = item },
                        label = { Text(item.label) },
                        leadingIcon = { Icon(item.icon, contentDescription = null) }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = title, onValueChange = { title = it; error = false },
                modifier = Modifier.fillMaxWidth(), label = { Text("Kapsül başlığı") },
                placeholder = { Text("Örn. Gelecekteki bana") }, singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = message, onValueChange = { message = it },
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false).heightIn(min = 130.dp),
                label = { Text(if (type == CapsuleType.LETTER) "Mektubun" else "Not (isteğe bağlı)") },
                placeholder = { Text("İçinden geçenleri buraya yaz...") },
                minLines = 4, shape = RoundedCornerShape(16.dp)
            )
            Spacer(Modifier.height(18.dp))
            Text("NE ZAMAN AÇILSIN?", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = date, onValueChange = { date = it; error = false },
                modifier = Modifier.fillMaxWidth(), label = { Text("Açılma tarihi (GG.AA.YYYY)") },
                leadingIcon = { Icon(Icons.Default.Event, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true, shape = RoundedCornerShape(16.dp)
            )
            AnimatedVisibility(error) {
                Text("Başlık ve geçerli, gelecekte bir tarih gir.", color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val parsed = runCatching {
                        SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR")).apply { isLenient = false }
                            .parse(date)?.time
                    }.getOrNull()
                    if (title.isBlank() || parsed == null || parsed <= System.currentTimeMillis()) {
                        error = true
                    } else onSave(title.trim(), type, message.trim(), parsed)
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) { Icon(Icons.Default.Lock, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Kapsülü mühürle") }
            Spacer(Modifier.height(14.dp))
            Text("Not: Bu sürüm arayüz prototipidir. Gerçek şifreleme ve zaman kilidi henüz etkin değildir.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun DetailPage(
    capsule: Capsule,
    openedContent: String?,
    status: String?,
    onBack: () -> Unit,
    onOpen: () -> Unit
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            TopBar("Kapsülün", onBack)
            Spacer(Modifier.height(28.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(Modifier.size(112.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                    contentAlignment = Alignment.Center) {
                    Icon(if (capsule.isReady) Icons.Default.Fingerprint else Icons.Default.Lock,
                        contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp))
                }
            }
            Spacer(Modifier.height(22.dp))
            Text(capsule.title, modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontFamily = FontFamily.Serif, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(capsule.type.label, modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    DetailLine("Oluşturulma", dateLabel(capsule.createdAt))
                    HorizontalDivider(Modifier.padding(vertical = 12.dp))
                    DetailLine("Açılma tarihi", dateLabel(capsule.unlockAt))
                    HorizontalDivider(Modifier.padding(vertical = 12.dp))
                    DetailLine("Durum", if (capsule.isReady) "Tarih geldi" else "Mühürlü")
                }
            }
            Spacer(Modifier.height(22.dp))
            Button(onClick = onOpen, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Fingerprint, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Biyometri ile doğrula ve aç")
            }
            status?.let { Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 12.dp)) }
            openedContent?.let {
                Spacer(Modifier.height(18.dp))
                Text("MEKTUBUN", color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(it, modifier = Modifier.padding(top = 8.dp))
            }
            Text("Zaman kontrolü cihaz saatini kullanır. Gerçek kriptografik zaman kilidi henüz etkin değildir.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp))
        }
    }
}

@Composable
private fun SettingsPage(dark: Boolean, onTheme: () -> Unit, onBack: () -> Unit) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            TopBar("Ayarlar", onBack)
            Text("Görünüm", style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
            ListItem(
                headlineContent = { Text("Koyu tema") },
                supportingContent = { Text("Kairos görünümünü değiştir") },
                leadingContent = { Icon(if (dark) Icons.Default.DarkMode else Icons.Default.WbSunny, null) },
                trailingContent = { Switch(checked = dark, onCheckedChange = { onTheme() }) }
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Uygulama dili") },
                supportingContent = { Text("Türkçe") },
                leadingContent = { Text("🇹🇷", fontSize = 22.sp) }
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Güvenlik ve gizlilik") },
                supportingContent = { Text("Şifreleme ve kurtarma seçenekleri sonraki aşamada eklenecek.") },
                leadingContent = { Icon(Icons.Default.Lock, null) }
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Kairos") },
                supportingContent = { Text("Geleceğe bırakılan küçük bir an.") }
            )
        }
    }
}

@Composable
private fun TopBar(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Geri") }
        Text(title, fontFamily = FontFamily.Serif, fontSize = 23.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

private fun dateLabel(time: Long): String =
    SimpleDateFormat("d MMMM yyyy", Locale("tr", "TR")).format(Date(time))

private fun remainingLabel(time: Long): String {
    val diff = (time - System.currentTimeMillis()).coerceAtLeast(0L)
    val days = diff / 86_400_000L
    val years = days / 365
    val months = (days % 365) / 30
    val rest = days % 30
    return when {
        years > 0 -> "$years yıl sonra"
        months > 0 -> "$months ay sonra"
        days > 0 -> "$days gün sonra"
        else -> "Bugün açılıyor"
    }
}
