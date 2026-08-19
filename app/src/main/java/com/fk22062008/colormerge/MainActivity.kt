package com.fk22062008.colormerge

import android.content.Context
import android.os.Bundle
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ColorMergeApp(applicationContext) }
    }
}

data class Tile(val value: Int)

enum class Screen { MENU, GAME, HELP, OVER }

class GameState(private val context: Context) {
    var screen by mutableStateOf(Screen.MENU)
    var board by mutableStateOf(List(36) { Tile(0) })
    var score by mutableIntStateOf(0)
    var best by mutableIntStateOf(context.getSharedPreferences("color_merge", 0).getInt("best", 0))
    var muted by mutableStateOf(context.getSharedPreferences("color_merge", 0).getBoolean("muted", false))
    var selected by mutableIntStateOf(-1)
    private val prefs = context.getSharedPreferences("color_merge", 0)

    fun newGame() {
        board = List(36) { Tile(0) }.toMutableList().also {
            addRandom(it); addRandom(it)
        }
        score = 0; selected = -1; screen = Screen.GAME
    }

    private fun addRandom(list: MutableList<Tile>) {
        val empty = list.indices.filter { list[it].value == 0 }
        if (empty.isNotEmpty()) list[empty.random()] = Tile(if (Random.nextFloat() < .9f) 1 else 2)
    }

    fun tap(index: Int) {
        if (board[index].value == 0) return
        if (selected == -1) { selected = index; return }
        if (selected == index) { selected = -1; return }
        val a = selected; val b = index
        val ar = a / 6; val ac = a % 6; val br = b / 6; val bc = b % 6
        if (board[a].value == board[b].value && kotlin.math.abs(ar - br) + kotlin.math.abs(ac - bc) == 1) {
            val list = board.toMutableList()
            list[a] = Tile(board[a].value + 1); list[b] = Tile(0)
            val gained = 1 shl board[a].value
            score += gained; best = max(best, score); prefs.edit().putInt("best", best).apply()
            addRandom(list); board = list
            beep(); selected = -1
            if (!canMove(list)) screen = Screen.OVER
        } else selected = if (board[index].value > 0) index else -1
    }

    private fun canMove(b: List<Tile>): Boolean {
        if (b.any { it.value == 0 }) return true
        for (i in b.indices) for (d in intArrayOf(1, 6)) {
            if (i % 6 == 5 && d == 1) continue
            if (i + d < 36 && b[i].value == b[i + d].value) return true
        }
        return false
    }

    fun toggleMute() { muted = !muted; prefs.edit().putBoolean("muted", muted).apply() }
    private fun beep() { if (!muted) ToneGenerator(AudioManager.STREAM_MUSIC, 65).startTone(ToneGenerator.TONE_PROP_BEEP, 55) }
}

@Composable
fun ColorMergeApp(context: Context) {
    val state = remember { GameState(context) }
    MaterialTheme(colorScheme = lightColorScheme(background = Color(0xFFF7F8FC))) {
        Surface(Modifier.fillMaxSize(), color = Color(0xFFF7F8FC)) {
            AnimatedContent(state.screen, label = "screen") { screen ->
                when (screen) {
                    Screen.MENU -> Menu(state)
                    Screen.GAME -> Game(state)
                    Screen.HELP -> Help(state)
                    Screen.OVER -> GameOver(state)
                }
            }
        }
    }
}

@Composable
fun Menu(s: GameState) {
    Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("COLOR", fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color(0xFF6C63FF))
        Text("MERGE", fontSize = 48.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(10.dp)); Text("Merge colors. Build higher values.", color = Color.Gray)
        Spacer(Modifier.height(40.dp))
        Button(onClick = { s.newGame() }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(18.dp)) { Text("NEW GAME", fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(14.dp))
        OutlinedButton(onClick = { s.screen = Screen.HELP }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(18.dp)) { Text("HOW TO PLAY") }
        Spacer(Modifier.height(18.dp)); Text("Best Score  ${s.best}", fontWeight = FontWeight.SemiBold, color = Color.Gray)
    }
}

@Composable
fun Game(s: GameState) {
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column { Text("Color Merge", fontSize = 28.sp, fontWeight = FontWeight.Black); Text("Best ${s.best}", color = Color.Gray) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallButton(if (s.muted) "🔇" else "🔊") { s.toggleMute() }
                SmallButton("↻") { s.newGame() }
            }
        }
        Spacer(Modifier.height(16.dp))
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(16.dp)) { Text("SCORE", fontSize = 12.sp, color = Color.Gray); Text("${s.score}", fontSize = 28.sp, fontWeight = FontWeight.Bold) } }
        Spacer(Modifier.height(16.dp))
        Board(s)
        Spacer(Modifier.height(14.dp)); Text("Tap a tile, then tap an adjacent tile of the same value to merge.", textAlign = TextAlign.Center, color = Color.Gray, modifier = Modifier.fillMaxWidth())
    }
}

@Composable fun SmallButton(text: String, action: () -> Unit) { Surface(shape = RoundedCornerShape(14.dp), tonalElevation = 2.dp, modifier = Modifier.size(48.dp).clickable { action() }) { Box(contentAlignment = Alignment.Center) { Text(text, fontSize = 20.sp) } } }

@Composable
fun Board(s: GameState) {
    Column(Modifier.fillMaxWidth().aspectRatio(1f).background(Color(0xFFE7E8F0), RoundedCornerShape(18.dp)).padding(6.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        for (r in 0 until 6) Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            for (c in 0 until 6) { val i = r * 6 + c; TileView(s.board[i].value, s.selected == i) { s.tap(i) } }
        }
    }
}

@Composable
fun TileView(value: Int, selected: Boolean, onClick: () -> Unit) {
    val colors = listOf(Color(0xFFE7E8F0), Color(0xFF6C63FF), Color(0xFFFF8A65), Color(0xFF26A69A), Color(0xFFFFCA28), Color(0xFF42A5F5), Color(0xFFAB47BC), Color(0xFFEF5350), Color(0xFF66BB6A), Color(0xFFFF7043), Color(0xFF5C6BC0))
    val bg by animateColorAsState(if (value == 0) colors[0] else colors[(value - 1).coerceAtMost(colors.lastIndex)], tween(180), label = "tile")
    val scale by animateDpAsState(if (selected) 0.92.dp else 1.dp, tween(100), label = "scale")
    Box(Modifier.weight(1f).fillMaxHeight().scale(if (selected) 0.92f else 1f).background(bg, RoundedCornerShape(10.dp)).clickable(enabled = value > 0) { onClick() }, contentAlignment = Alignment.Center) {
        if (value > 0) Text("${1 shl (value - 1)}", fontSize = if (value > 7) 14.sp else 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
    }
}

@Composable
fun Help(s: GameState) {
    Column(Modifier.fillMaxSize().padding(28.dp)) {
        Text("How to Play", fontSize = 32.sp, fontWeight = FontWeight.Black); Spacer(Modifier.height(22.dp))
        listOf("1. Tap any colored tile to select it.", "2. Tap an adjacent tile with the same value.", "3. The two tiles merge into a higher-value tile.", "4. A new tile appears after every merge.", "5. Keep merging to beat your best score.", "6. When no valid merge or empty space remains, the game ends.").forEach { Text(it, fontSize = 17.sp, modifier = Modifier.padding(vertical = 8.dp)) }
        Spacer(Modifier.weight(1f)); Button(onClick = { s.screen = Screen.MENU }, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text("BACK TO MENU") }
    }
}

@Composable
fun GameOver(s: GameState) {
    Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("GAME OVER", fontSize = 40.sp, fontWeight = FontWeight.Black); Spacer(Modifier.height(12.dp)); Text("Final Score", color = Color.Gray); Text("${s.score}", fontSize = 52.sp, fontWeight = FontWeight.Black, color = Color(0xFF6C63FF)); Text("Best ${s.best}", color = Color.Gray)
        Spacer(Modifier.height(36.dp)); Button(onClick = { s.newGame() }, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("PLAY AGAIN") }; Spacer(Modifier.height(12.dp)); OutlinedButton(onClick = { s.screen = Screen.MENU }, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("MAIN MENU") }
    }
}
