// CodeCollector.kt

package app.codcoll

import java.io.File
import java.nio.file.Paths
import kotlin.text.RegexOption // Импорт, необходимый для использования DOT_MATCHES_ALL

object CodeCollector {

    private const val SERVICE_FOLDER_NAME = "CodColl"
    private const val OUTPUT_FILE_NAME = "project_code.txt"
    private const val SEPARATOR = "\n---\n"

    private val currentDir = Paths.get("").toAbsolutePath().toString()
    private val serviceFolder = File(currentDir, SERVICE_FOLDER_NAME)
    private val outputFile = File(serviceFolder, OUTPUT_FILE_NAME)

    private val pathFile = File(serviceFolder, "path.txt")

    init {
        if (!serviceFolder.exists()) serviceFolder.mkdirs()
    }


    fun saveSelectedPath(path: String) {
        try {
            pathFile.writeText(path)
        } catch (e: Exception) {
            // игнорируем ошибки записи
        }
    }

    fun loadSelectedPath(): String? {
        return try {
            if (pathFile.exists()) {
                pathFile.readText().takeIf { it.isNotBlank() && File(it).exists() }
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Рекурсивно ищет все Kotlin-файлы в папке
     */
    private fun findKotlinFiles(folder: File): List<File> =
        folder.walkTopDown()
            .filter { it.isFile && (it.extension == "kt" || it.extension == "kts") }
            .toList()

    /**
     * Удаляет из строки кода комментарии (однострочные // и многострочные /*...*/)
     * и KDoc/Javadoc (/**...*/).
     *
     * @param code Исходный код файла.
     * @return Код, очищенный от комментариев и пустых строк.
     */
    private fun removeComments(code: String): String {
        // Шаг 1: Удаляем все многострочные комментарии (включая Javadoc/KDoc)
        // Используем исправленную константу: RegexOption.DOT_MATCHES_ALL
        val noBlockComments = code.replace(Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL), "")

        // Шаг 2: Обрабатываем однострочные комментарии
        return noBlockComments.split('\n')
            .joinToString("\n") { line ->
                // Ищем индекс начала однострочного комментария
                val commentIndex = line.indexOf("//")

                if (commentIndex == -1) {
                    // Комментария нет, возвращаем строку как есть
                    line
                } else {
                    // Убираем комментарий и лишние пробелы в конце
                    line.substring(0, commentIndex).trimEnd()
                }
            }
            .lines()
            .filter { it.isNotBlank() } // Шаг 3: Убираем пустые строки, оставшиеся после удаления комментариев
            .joinToString("\n")
    }

    /**
     * Основная функция
     */
    fun collectCodeFromFolder(root: File): String {
        val log = StringBuilder()

        if (!root.exists() || !root.isDirectory) {
            return "❌ Указанная папка недоступна: ${root.absolutePath}"
        }

        log.append("🔍 Поиск Kotlin-файлов в: ${root.absolutePath}\n")

        val files = findKotlinFiles(root)

        if (files.isEmpty()) {
            log.append("⚠️ Kotlin-файлы (.kt/.kts) не найдены.\n")
            return log.toString()
        }

        log.append("📝 Найдено файлов: ${files.size}\n")

        val output = StringBuilder()
        var count = 0

        files.forEach { file ->
            try {
                // 1. Читаем весь текст
                val fullText = file.readText()

                // 2. !!! ПРИМЕНЯЕМ ФИЛЬТРАЦИЮ !!!
                val codeWithoutComments = removeComments(fullText)

                if (codeWithoutComments.isNotBlank()) {
                    // Добавляем файл, только если в нем остался код
                    output.append("Файл: ${file.absolutePath}\n\n")
                    output.append(codeWithoutComments) // Добавляем очищенный код
                    output.append(SEPARATOR)

                    count++
                    log.append("✅ Добавлен (очищен от комментариев): ${file.name}\n")
                } else {
                    // Если файл содержал только комментарии, мы его пропускаем
                    log.append("➖ Пропущен (содержит только комментарии): ${file.name}\n")
                }
            } catch (e: Exception) {
                log.append("❌ Ошибка чтения: ${file.absolutePath}: ${e.message}\n")
            }
        }

        // записываем результат
        return try {
            outputFile.writeText(output.toString())
            log.append("\n🎉 Готово! Сохранено файлов: $count\n")
            log.append("💾 Результат: ${outputFile.absolutePath}\n")
            log.toString()
        } catch (e: Exception) {
            log.append("❌ Ошибка записи файла: ${e.message}\n")
            log.toString()
        }
    }
}