package app.codcoll

// CodCollGUI.kt

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "CodColl - Code Collector",
        // Задаем минимальный размер окна
        state = androidx.compose.ui.window.rememberWindowState(
            width = 800.dp,
            height = 600.dp
        )
    ) {
        App()
    }
}

@Composable
@Preview
fun App() {
    // 1. Состояние для отображения логов и статуса
    var logText by remember { mutableStateOf("") }
    // 2. Состояние для управления активностью кнопки (блокировка во время работы)
    var isCollecting by remember { mutableStateOf(false) }

    // CoroutineScope позволяет запускать асинхронные задачи
    val coroutineScope = rememberCoroutineScope()

    // Функция для инициализации: создание папки и файла paths.txt
    fun initialize() {
        // Мы вызываем логику подготовки и сразу отображаем результат в логе
        logText = CodeCollector.prepareServiceFolderAndInputFile()
        logText += "\n\n---\n\nНажмите 'Собрать код' для запуска процесса сбора."
    }

    // LaunchedEffect запускает инициализацию только один раз при старте
    LaunchedEffect(Unit) {
        initialize()
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("CodColl - Сборщик кода проекта (Kotlin Compose)") })
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Информация о служебной папке и файле путей
                Text(
                    text = "Служебная папка: ${CodeCollector.serviceFolder.absolutePath}",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.subtitle2.copy(color = Color.Gray)
                )

                Spacer(Modifier.height(16.dp))

                // Кнопка запуска с индикатором
                Button(
                    onClick = {
                        // Запускаем сбор кода в корутине, чтобы не блокировать поток UI
                        coroutineScope.launch {
                            isCollecting = true
                            logText += "\n\n⏳ Идет сборка кода... Пожалуйста, подождите.\n"

                            // withContext(Dispatchers.IO) - ВАЖНО!
                            // Переключаемся на поток ввода/вывода, т.к. работа с файлами долгая.
                            val resultLog = withContext(Dispatchers.IO) {
                                CodeCollector.collectCode()
                            }
                            logText += resultLog
                            isCollecting = false
                        }
                    },
                    enabled = !isCollecting, // Кнопка неактивна во время сборки
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(if (isCollecting) "Сборка..." else "Собрать код")
                    if (isCollecting) {
                        Spacer(Modifier.width(8.dp))
                        // Индикатор прогресса
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Лог-окно
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
                    // Используем rememberScrollState и verticalScroll для прокрутки
                    val scrollState = rememberScrollState()
                    Text(
                        text = logText,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.9f))
                            .padding(8.dp)
                            .verticalScroll(scrollState),
                        color = Color(0xFF00FF00), // Зеленый цвет для стилизации
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.caption
                    )
                }
            }
        }
    }
}