package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val NekoColorScheme = darkColorScheme(
  primary = NeonPurple,
  secondary = FlameOrange,
  tertiary = GoldStar,
  background = DeepCosmicBlack,
  surface = SurfaceGlassDark,
  onPrimary = TextLight,
  onSecondary = TextLight,
  onBackground = TextLight,
  onSurface = TextLight,
  outline = BorderPurple
)

@Composable
fun NekoStreamTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = NekoColorScheme,
    typography = Typography,
    content = content
  )
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Custom theme-centric Vibrant Palette overrides dynamic wallpaper-based coloring
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> NekoColorScheme
      else -> NekoColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
