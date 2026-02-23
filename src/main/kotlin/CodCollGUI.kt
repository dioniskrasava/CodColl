package app.codcoll

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.swing.JFileChooser

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "CodColl - Code Collector",
        state = androidx.compose.ui.window.rememberWindowState(
            width = 800.dp,
            height = 600.dp
        )
    ) {
        App()
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {
    var logText by remember { mutableStateOf("") }
    var isCollecting by remember { mutableStateOf(false) }
    var selectedFolder by remember { mutableStateOf<File?>(null) }
    var showHelpDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val savedPath = CodeCollector.loadSelectedPath()
        if (savedPath != null) {
            selectedFolder = File(savedPath)
            logText += "🔄 Восстановлен предыдущий путь: $savedPath\n"
        }
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("CodColl - Сборщик кода проекта (Kotlin)") },
                    actions = {
                        IconButton(
                            onClick = { showHelpDialog = true },
                            modifier = Modifier.onPointerEvent(PointerEventType.Enter) {
                                // Запустить задержку показа тултипа
                            }
                        ) {
                            Icon(Icons.Default.Help, contentDescription = "Справка")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Выбранная папка: " +
                            (selectedFolder?.absolutePath ?: "не выбрано"),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.subtitle2.copy(color = Color.Gray)
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        val chooser = JFileChooser().apply {
                            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                        }
                        val result = chooser.showOpenDialog(null)
                        if (result == JFileChooser.APPROVE_OPTION) {
                            selectedFolder = chooser.selectedFile
                            CodeCollector.saveSelectedPath(selectedFolder!!.absolutePath)
                            logText += "\n📁 Папка выбрана: ${selectedFolder!!.absolutePath}\n"
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Выбрать папку")
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (selectedFolder == null) {
                                logText += "\n⚠️ Сначала выберите папку!\n"
                                return@launch
                            }
                            isCollecting = true
                            logText += "\n⏳ Поиск файлов и сбор кода...\n"
                            val result = withContext(Dispatchers.IO) {
                                CodeCollector.collectCodeFromFolder(selectedFolder!!)
                            }
                            logText += result
                            isCollecting = false
                        }
                    },
                    enabled = !isCollecting,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(if (isCollecting) "Сборка..." else "Собрать код")
                    if (isCollecting) {
                        Spacer(Modifier.width(8.dp))
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Лог выполнения:",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.subtitle1
                )
                Spacer(Modifier.height(8.dp))
                Card(
                    elevation = 4.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val scrollState = rememberScrollState()
                    Text(
                        text = logText,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.9f))
                            .padding(8.dp)
                            .verticalScroll(scrollState),
                        color = Color(0xFF00FF00),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("О программе CodColl") },
            text = {
                Text(
                    buildString {
                        appendLine("CodColl — утилита для сбора исходного кода Kotlin-проектов в один текстовый файл.")
                        appendLine()
                        appendLine("• Сканирует указанную папку и находит все файлы с расширениями .kt и .kts")
                        appendLine("• Удаляет комментарии (//, /* */) и пустые строки")
                        appendLine("• Подсчитывает количество строк кода с учётом импортов и без них")
                        appendLine("• Сохраняет результат в файл:")
                        appendLine("  ${CodeCollector.getOutputFilePath()} (в папке CodColl рядом с программой)")
                        appendLine()
                        appendLine("Статистика строк выводится в лог после завершения сбора.")
                        appendLine()
                        appendLine("Вы можете выбрать папку, и программа запомнит её для следующего запуска.")
                    }
                )
            },
            confirmButton = {
                Button(onClick = { showHelpDialog = false }) {
                    Text("Закрыть")
                }
            }
        )
    }
}