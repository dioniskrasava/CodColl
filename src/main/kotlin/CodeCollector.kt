package app.codcoll

// CodeCollector.kt

import java.io.File
import java.nio.file.Paths

/**
 * Объект для выполнения основной логики сбора кода.
 * Отвечает за работу с файловой системой.
 */
object CodeCollector {
    // --- КОНСТАНТЫ И НАСТРОЙКИ ---
    private const val SERVICE_FOLDER_NAME = "CodColl"
    private const val INPUT_FILE_NAME = "paths.txt"
    private const val OUTPUT_FILE_NAME = "project_code.txt"
    private const val SEPARATOR = "\n---\n"

    // Определяем пути, относительно папки, откуда запускается программа
    private val currentDir = Paths.get("").toAbsolutePath().toString()
    val serviceFolder = File(currentDir, SERVICE_FOLDER_NAME)
    val inputFile = File(serviceFolder, INPUT_FILE_NAME)
    val outputFile = File(serviceFolder, OUTPUT_FILE_NAME)

    /**
     * Проверяет наличие служебной папки и входного файла, при необходимости создает их.
     * @return Лог действий в виде строки.
     */
    fun prepareServiceFolderAndInputFile(): String {
        val log = StringBuilder()

        // 1. Подготовка служебной папки
        if (!serviceFolder.exists()) {
            serviceFolder.mkdirs()
            log.append("✅ Создана служебная папка '$SERVICE_FOLDER_NAME' по пути:\n${serviceFolder.absolutePath}\n")
        } else {
            log.append("📂 Служебная папка '$SERVICE_FOLDER_NAME' уже существует.\n")
        }

        // 2. Проверка и создание входного файла с путями
        if (!inputFile.exists()) {
            // Начальное содержимое с инструкциями, как вы и просили
            val initialContent = """
// --------------------------------------------------------------------------------------
// CodColl: Файл для указания абсолютных путей к вашим Kotlin-файлам проекта.
// Каждый путь должен быть на отдельной строке.
// Строки, начинающиеся с '//', игнорируются (используйте для комментариев).
// --------------------------------------------------------------------------------------

// ПРИМЕР: Замените этот путь на абсолютный путь к вашему первому файлу
// /home/user/my_compose_project/src/main/kotlin/Main.kt
// C:\Users\YourUser\Documents\MyComposeProject\app\src\main\kotlin\data\Model.kt
""".trimIndent()
            try {
                inputFile.writeText(initialContent)
                log.append("✅ Создан входной файл '$INPUT_FILE_NAME' с примером заполнения.\n")
                log.append("❗ Пожалуйста, отредактируйте его и добавьте пути к файлам вашего проекта.\n")
            } catch (e: Exception) {
                log.append("❌ Ошибка при создании входного файла: ${e.message}\n")
            }
        } else {
            log.append("📝 Входной файл '$INPUT_FILE_NAME' найден. Готов к работе.\n")
        }
        return log.toString()
    }

    /**
     * Основная функция для сбора кода из всех путей, указанных в paths.txt.
     * @return Подробный лог выполнения.
     */
    fun collectCode(): String {
        val log = StringBuilder()
        log.append("\n--- Запуск сбора кода ---\n")

        // ... [Остальная логика чтения, сборки и записи остается прежней] ...

        // 1. Чтение списка путей
        val filePaths = try {
            inputFile.readLines().filter { it.isNotBlank() && !it.startsWith("//") }
        } catch (e: Exception) {
            return log.append("❌ Критическая ошибка при чтении файла путей: ${e.message}").toString()
        }

        if (filePaths.isEmpty()) {
            return log.append("⚠️ Предупреждение: Файл путей пуст или содержит только комментарии. Сборка отменена.").toString()
        }

        log.append("📝 Найдено ${filePaths.size} путей к файлам. Начинаю сборку...\n")

        // 2. Сборка содержимого
        val outputContent = StringBuilder()
        var successfulFiles = 0

        filePaths.forEach { path ->
            val codeFile = File(path.trim())

            if (codeFile.exists() && codeFile.isFile) {
                try {
                    // Форматирование заголовка
                    outputContent.append("Файл \"${codeFile.absolutePath}\"\n\n")

                    // Содержимое файла
                    outputContent.append(codeFile.readText())

                    // Разделитель
                    outputContent.append(SEPARATOR)

                    successfulFiles++
                    log.append("✅ Добавлен: ${codeFile.name}\n")
                } catch (e: Exception) {
                    log.append("⚠️ Ошибка чтения файла $path: ${e.message}\n")
                }
            } else {
                log.append("❌ Пропущен: Файл не найден или это не файл: $path\n")
            }
        }

        // 3. Сохранение результата
        return try {
            outputFile.writeText(outputContent.toString())
            log.append("\n🎉 УСПЕХ! Собрано $successfulFiles файлов.\n")
            log.append("💾 Код сохранен в: ${outputFile.absolutePath}\n").toString()
        } catch (e: Exception) {
            log.append("\n❌ Критическая ошибка при записи файла: ${e.message}\n").toString()
        }
    }
}