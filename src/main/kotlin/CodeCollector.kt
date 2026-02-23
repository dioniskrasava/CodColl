package app.codcoll

import java.io.File
import java.nio.file.Paths
import kotlin.text.RegexOption

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

    private fun findKotlinFiles(folder: File): List<File> =
        folder.walkTopDown()
            .filter { it.isFile && (it.extension == "kt" || it.extension == "kts") }
            .toList()

    /**
     * Удаляет многострочные комментарии (/*...*/),
     * однострочные комментарии (//...) и пустые строки.
     *
     * @param code Исходный код файла.
     * @return Код, очищенный от комментариев и пустых строк.
     */
    private fun removeComments(code: String): String {
        val noBlockComments = code.replace(Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL), "")
        return noBlockComments.split('\n')
            .joinToString("\n") { line ->
                val commentIndex = line.indexOf("//")
                if (commentIndex == -1) {
                    line
                } else {
                    line.substring(0, commentIndex).trimEnd()
                }
            }
            .lines()
            .filter { it.isNotBlank() }
            .joinToString("\n")
    }

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
        var totalLinesWithImports = 0
        var totalLinesWithoutImports = 0

        files.forEach { file ->
            try {
                val fullText = file.readText()
                val codeWithoutComments = removeComments(fullText)

                if (codeWithoutComments.isNotBlank()) {
                    val linesWithImports = codeWithoutComments.lines()
                    val linesWithoutImports = linesWithImports.filter { line ->
                        !line.trimStart().startsWith("import ")
                    }

                    totalLinesWithImports += linesWithImports.size
                    totalLinesWithoutImports += linesWithoutImports.size

                    output.append("Файл: ${file.absolutePath}\n\n")
                    output.append(codeWithoutComments)
                    output.append(SEPARATOR)
                    count++
                    log.append("✅ Добавлен (очищен от комментариев): ${file.name}\n")
                } else {
                    log.append("➖ Пропущен (содержит только комментарии): ${file.name}\n")
                }
            } catch (e: Exception) {
                log.append("❌ Ошибка чтения: ${file.absolutePath}: ${e.message}\n")
            }
        }

        return try {
            outputFile.writeText(output.toString())
            log.append("\n🎉 Готово! Сохранено файлов: $count\n")
            log.append("💾 Результат: ${outputFile.absolutePath}\n")
            log.append("\n📊 Статистика строк кода (без комментариев и пустых строк):\n")
            log.append("   С импортами: $totalLinesWithImports\n")
            log.append("   Без импортов: $totalLinesWithoutImports\n")
            log.toString()
        } catch (e: Exception) {
            log.append("❌ Ошибка записи файла: ${e.message}\n")
            log.toString()
        }
    }

    fun getOutputFilePath(): String = outputFile.absolutePath
}

