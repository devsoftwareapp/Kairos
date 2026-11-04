package com.devsoftware.kairos.ui.theme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
private val L=lightColorScheme(primary=Color(0xFF9A5B13),onPrimary=Color.White,secondary=Color(0xFF735A3B),background=Color(0xFFF3EFE8),surface=Color(0xFFFFFDF9),secondaryContainer=Color(0xFFF6E3C4))
private val D=darkColorScheme(primary=Color(0xFFF2B04A),onPrimary=Color(0xFF241704),secondary=Color(0xFFD9BD96),background=Color(0xFF101923),surface=Color(0xFF1B2935),secondaryContainer=Color(0xFF46351F))
@Composable fun KairosTheme(content:@Composable ()->Unit){MaterialTheme(colorScheme=L,content=content)}