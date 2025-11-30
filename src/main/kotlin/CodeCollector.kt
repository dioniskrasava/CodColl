// CodeCollector.kt

package app.codcoll

import java.io.File
import java.nio.file.Paths

object CodeCollector {

    private const val SERVICE_FOLDER_NAME = "CodColl"
    private const val OUTPUT_FILE_NAME = "project_code.txt"
    private const val SEPARATOR = "\n---\n"

    private val currentDir = Paths.get("").toAbsolutePath().toString()
    private val serviceFolder = File(currentDir, SERVICE_FOLDER_NAME)
    private val outputFile = File(serviceFolder, OUTPUT_FILE_NAME)

    init {
        if (!serviceFolder.exists()) serviceFolder.mkdirs()
    }

    /**
     * Рекурсивно ищет все Kotlin-файлы в папке
     */
    private fun findKotlinFiles(folder: File): List<File> =
        folder.walkTopDown()
            .filter { it.isFile && (it.extension == "kt" || it.extension == "kts") }
            .toList()

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
                output.append("Файл: ${file.absolutePath}\n\n")
                output.append(file.readText())
                output.append(SEPARATOR)

                count++
                log.append("✅ Добавлен: ${file.name}\n")
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
