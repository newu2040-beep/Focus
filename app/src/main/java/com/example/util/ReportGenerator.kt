package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.DailyReviewEntity
import com.example.data.model.FocusSessionEntity
import com.example.data.model.TaskEntity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ReportFormat(val extension: String, val mimeType: String, val displayName: String) {
    PDF("pdf", "application/pdf", "PDF Document (.pdf)"),
    CSV("csv", "text/csv", "CSV Spreadsheet (.csv)"),
    TXT("txt", "text/plain", "Plain Text (.txt)")
}

object ReportGenerator {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val dateFileFormat = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())

    fun getDefaultFileName(format: ReportFormat): String {
        val stamp = dateFileFormat.format(Date())
        return "FocusGuide_Report_$stamp.${format.extension}"
    }

    /**
     * Generates a temporary report file in the app cache directory.
     */
    fun generateTempReport(
        context: Context,
        format: ReportFormat,
        tasks: List<TaskEntity>,
        sessions: List<FocusSessionEntity>,
        review: DailyReviewEntity?
    ): File {
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val fileName = getDefaultFileName(format)
        val file = File(reportsDir, fileName)

        FileOutputStream(file).use { outputStream ->
            writeReport(outputStream, format, tasks, sessions, review)
        }
        return file
    }

    /**
     * Writes the report directly to an output stream (e.g. from SAF URI).
     */
    fun writeReport(
        outputStream: OutputStream,
        format: ReportFormat,
        tasks: List<TaskEntity>,
        sessions: List<FocusSessionEntity>,
        review: DailyReviewEntity?
    ) {
        when (format) {
            ReportFormat.PDF -> generatePdf(outputStream, tasks, sessions, review)
            ReportFormat.CSV -> generateCsv(outputStream, tasks, sessions, review)
            ReportFormat.TXT -> generateTxt(outputStream, tasks, sessions, review)
        }
    }

    /**
     * Launches Android system share sheet with the generated report file.
     */
    fun shareReport(
        context: Context,
        file: File,
        format: ReportFormat,
        chooserTitle: String = "Share Productivity Report"
    ) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = format.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Focus Guide Productivity Report")
            putExtra(Intent.EXTRA_TEXT, "Here is my Focus Guide productivity and focus session report.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    // ==========================================
    // PDF GENERATION
    // ==========================================
    private fun generatePdf(
        outputStream: OutputStream,
        tasks: List<TaskEntity>,
        sessions: List<FocusSessionEntity>,
        review: DailyReviewEntity?
    ) {
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // Paints
        val primaryPaint = Paint().apply {
            color = Color.rgb(46, 105, 69) // Sage Forest Green
            isAntiAlias = true
        }

        val headerBgPaint = Paint().apply {
            color = Color.rgb(238, 245, 240) // Soft sage tint
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val cardBgPaint = Paint().apply {
            color = Color.rgb(248, 250, 248)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val cardBorderPaint = Paint().apply {
            color = Color.rgb(215, 230, 220)
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }

        val titlePaint = Paint().apply {
            color = Color.rgb(24, 45, 30)
            textSize = 20f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(90, 110, 95)
            textSize = 10f
            isAntiAlias = true
        }

        val sectionTitlePaint = Paint().apply {
            color = Color.rgb(35, 65, 45)
            textSize = 13f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.rgb(40, 50, 45)
            textSize = 9.5f
            isAntiAlias = true
        }

        val bodyMutedPaint = Paint().apply {
            color = Color.rgb(105, 120, 110)
            textSize = 8.5f
            isAntiAlias = true
        }

        val statValuePaint = Paint().apply {
            color = Color.rgb(30, 75, 45)
            textSize = 16f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val statLabelPaint = Paint().apply {
            color = Color.rgb(85, 105, 90)
            textSize = 8.5f
            isAntiAlias = true
        }

        val margin = 36f
        var currentY = margin

        // 1. Header Banner
        val headerRect = RectF(margin, currentY, pageWidth - margin, currentY + 70f)
        canvas.drawRoundRect(headerRect, 12f, 12f, headerBgPaint)
        canvas.drawRoundRect(headerRect, 12f, 12f, cardBorderPaint)

        // Draw Accent Bar on Left
        val accentBar = RectF(margin, currentY, margin + 6f, currentY + 70f)
        canvas.drawRoundRect(accentBar, 4f, 4f, primaryPaint)

        canvas.drawText("FOCUS GUIDE", margin + 18f, currentY + 28f, titlePaint)
        canvas.drawText("Productivity & Focus Sessions Report", margin + 18f, currentY + 44f, subtitlePaint)
        canvas.drawText("Generated: ${dateFormat.format(Date())}", margin + 18f, currentY + 58f, bodyMutedPaint)

        currentY += 86f

        // Stats calculations
        val completedTasksCount = tasks.count { it.isCompleted }
        val totalTasksCount = tasks.size
        val completionRate = if (totalTasksCount > 0) (completedTasksCount * 100) / totalTasksCount else 0
        val totalFocusSeconds = sessions.sumOf { it.completedSeconds }
        val totalFocusMins = totalFocusSeconds / 60
        val focusHours = totalFocusMins / 60
        val focusRemainingMins = totalFocusMins % 60

        // 2. Executive KPIs (4 Cards in a row)
        canvas.drawText("EXECUTIVE OVERVIEW", margin, currentY, sectionTitlePaint)
        currentY += 12f

        val statCardWidth = (pageWidth - (margin * 2) - (10f * 3)) / 4f
        val statCardHeight = 52f

        val stats = listOf(
            Pair("Focus Time", if (focusHours > 0) "${focusHours}h ${focusRemainingMins}m" else "${focusRemainingMins}m"),
            Pair("Completed Tasks", "$completedTasksCount / $totalTasksCount"),
            Pair("Completion Rate", "$completionRate%"),
            Pair("Focus Sessions", "${sessions.size}")
        )

        for (i in stats.indices) {
            val left = margin + i * (statCardWidth + 10f)
            val rect = RectF(left, currentY, left + statCardWidth, currentY + statCardHeight)
            canvas.drawRoundRect(rect, 8f, 8f, cardBgPaint)
            canvas.drawRoundRect(rect, 8f, 8f, cardBorderPaint)

            canvas.drawText(stats[i].second, left + 10f, currentY + 24f, statValuePaint)
            canvas.drawText(stats[i].first, left + 10f, currentY + 40f, statLabelPaint)
        }

        currentY += statCardHeight + 22f

        // 3. Category Breakdown
        canvas.drawText("TASKS BY CATEGORY", margin, currentY, sectionTitlePaint)
        currentY += 14f

        val categoriesWithCount = tasks.groupBy { it.category }
        val categoryText = if (categoriesWithCount.isEmpty()) {
            "No recorded tasks"
        } else {
            categoriesWithCount.entries.joinToString("   •   ") { (cat, list) ->
                "${cat.displayName}: ${list.size} (${list.count { it.isCompleted }} done)"
            }
        }
        canvas.drawText(categoryText, margin + 4f, currentY, bodyPaint)
        currentY += 24f

        // 4. Tasks Detail (Up to 10 recent items)
        canvas.drawText("RECENT TASKS LOG", margin, currentY, sectionTitlePaint)
        currentY += 14f

        // Table Header
        val colTaskX = margin + 6f
        val colCategoryX = margin + 280f
        val colPriorityX = margin + 370f
        val colStatusX = margin + 440f

        canvas.drawText("TASK TITLE", colTaskX, currentY, statLabelPaint)
        canvas.drawText("CATEGORY", colCategoryX, currentY, statLabelPaint)
        canvas.drawText("PRIORITY", colPriorityX, currentY, statLabelPaint)
        canvas.drawText("STATUS", colStatusX, currentY, statLabelPaint)
        currentY += 6f

        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, cardBorderPaint)
        currentY += 12f

        val displayedTasks = tasks.take(10)
        if (displayedTasks.isEmpty()) {
            canvas.drawText("No tasks recorded yet.", colTaskX, currentY, bodyMutedPaint)
            currentY += 16f
        } else {
            for (task in displayedTasks) {
                val titleTruncated = if (task.title.length > 42) task.title.take(39) + "..." else task.title
                val statusText = if (task.isCompleted) "[✓] Done" else "[ ] Pending"

                canvas.drawText(titleTruncated, colTaskX, currentY, bodyPaint)
                canvas.drawText(task.category.displayName, colCategoryX, currentY, bodyMutedPaint)
                canvas.drawText(task.priority.displayName, colPriorityX, currentY, bodyMutedPaint)
                canvas.drawText(statusText, colStatusX, currentY, bodyPaint)

                currentY += 15f
            }
        }

        currentY += 14f

        // 5. Recent Focus Sessions (Up to 6 items)
        canvas.drawText("RECENT FOCUS SESSIONS", margin, currentY, sectionTitlePaint)
        currentY += 14f

        val colSessionTaskX = margin + 6f
        val colSessionModeX = margin + 280f
        val colSessionDurX = margin + 380f
        val colSessionTimeX = margin + 450f

        canvas.drawText("ASSOCIATED TASK", colSessionTaskX, currentY, statLabelPaint)
        canvas.drawText("MODE", colSessionModeX, currentY, statLabelPaint)
        canvas.drawText("COMPLETED", colSessionDurX, currentY, statLabelPaint)
        canvas.drawText("TIME", colSessionTimeX, currentY, statLabelPaint)
        currentY += 6f

        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, cardBorderPaint)
        currentY += 12f

        val displayedSessions = sessions.take(6)
        if (displayedSessions.isEmpty()) {
            canvas.drawText("No focus sessions recorded yet.", colSessionTaskX, currentY, bodyMutedPaint)
            currentY += 16f
        } else {
            val sessionTimeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
            for (sess in displayedSessions) {
                val taskName = sess.taskTitle ?: "Unassigned Focus"
                val truncatedTask = if (taskName.length > 40) taskName.take(37) + "..." else taskName
                val mins = sess.completedSeconds / 60

                canvas.drawText(truncatedTask, colSessionTaskX, currentY, bodyPaint)
                canvas.drawText(sess.sessionType.displayName, colSessionModeX, currentY, bodyMutedPaint)
                canvas.drawText("${mins}m / ${sess.durationMinutes}m", colSessionDurX, currentY, bodyPaint)
                canvas.drawText(sessionTimeFmt.format(Date(sess.timestamp)), colSessionTimeX, currentY, bodyMutedPaint)

                currentY += 15f
            }
        }

        currentY += 14f

        // 6. Daily Reflection (if available)
        if (review != null) {
            canvas.drawText("DAILY REVIEW & REFLECTION", margin, currentY, sectionTitlePaint)
            currentY += 14f

            val reviewRect = RectF(margin, currentY, pageWidth - margin, currentY + 54f)
            canvas.drawRoundRect(reviewRect, 8f, 8f, cardBgPaint)
            canvas.drawRoundRect(reviewRect, 8f, 8f, cardBorderPaint)

            canvas.drawText("Rating: ${review.rating} / 5 stars   •   Tomorrow's Priority: ${review.tomorrowPriority.ifEmpty { "None specified" }}", margin + 10f, currentY + 18f, bodyPaint)
            if (review.whatWentWell.isNotEmpty()) {
                val winTruncated = if (review.whatWentWell.length > 70) review.whatWentWell.take(67) + "..." else review.whatWentWell
                canvas.drawText("Wins: $winTruncated", margin + 10f, currentY + 34f, bodyMutedPaint)
            }
            if (review.distractions.isNotEmpty()) {
                val distTruncated = if (review.distractions.length > 70) review.distractions.take(67) + "..." else review.distractions
                canvas.drawText("Distractions: $distTruncated", margin + 10f, currentY + 48f, bodyMutedPaint)
            }
            currentY += 66f
        }

        // 7. Footer
        val footerY = pageHeight - margin
        canvas.drawLine(margin, footerY - 14f, pageWidth - margin, footerY - 14f, cardBorderPaint)
        canvas.drawText("Generated by Focus Guide • Offline, Private & Mindful Productivity", margin, footerY, bodyMutedPaint)
        canvas.drawText("Page 1 of 1", pageWidth - margin - 50f, footerY, bodyMutedPaint)

        pdfDocument.finishPage(page)
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
    }

    // ==========================================
    // CSV GENERATION
    // ==========================================
    private fun generateCsv(
        outputStream: OutputStream,
        tasks: List<TaskEntity>,
        sessions: List<FocusSessionEntity>,
        review: DailyReviewEntity?
    ) {
        val writer = outputStream.bufferedWriter()

        // Section 1: Executive KPI Overview
        val completedTasksCount = tasks.count { it.isCompleted }
        val totalTasksCount = tasks.size
        val completionRate = if (totalTasksCount > 0) (completedTasksCount * 100) / totalTasksCount else 0
        val totalFocusMins = sessions.sumOf { it.completedSeconds } / 60

        writer.write("FOCUS GUIDE PRODUCTIVITY REPORT\n")
        writer.write("Generated at,${escapeCsv(dateFormat.format(Date()))}\n")
        writer.write("Total Focus Minutes,$totalFocusMins\n")
        writer.write("Completed Tasks,$completedTasksCount\n")
        writer.write("Total Tasks,$totalTasksCount\n")
        writer.write("Completion Rate,$completionRate%\n")
        writer.write("Focus Sessions Count,${sessions.size}\n\n")

        // Section 2: Tasks Table
        writer.write("TASKS LIST\n")
        writer.write("ID,Title,Category,Priority,Status,Estimated Minutes,Due Date,Due Time,Notes,Created At\n")
        val itemDateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        for (task in tasks) {
            val dueDateStr = task.dueDate?.let { itemDateFmt.format(Date(it)) } ?: ""
            val createdAtStr = itemDateFmt.format(Date(task.createdAt))
            val status = if (task.isCompleted) "COMPLETED" else "PENDING"

            writer.write(
                "${task.id}," +
                "${escapeCsv(task.title)}," +
                "${task.category.name}," +
                "${task.priority.name}," +
                "$status," +
                "${task.estimatedMinutes}," +
                "${escapeCsv(dueDateStr)}," +
                "${escapeCsv(task.dueTime ?: "")}," +
                "${escapeCsv(task.notes)}," +
                "$createdAtStr\n"
            )
        }
        writer.write("\n")

        // Section 3: Focus Sessions Table
        writer.write("FOCUS SESSIONS\n")
        writer.write("Session ID,Task ID,Task Title,Mode,Duration Minutes,Completed Seconds,Timestamp\n")
        for (session in sessions) {
            val sessionDateStr = dateFormat.format(Date(session.timestamp))
            writer.write(
                "${session.id}," +
                "${session.taskId ?: ""}," +
                "${escapeCsv(session.taskTitle ?: "Unassigned")}," +
                "${session.sessionType.name}," +
                "${session.durationMinutes}," +
                "${session.completedSeconds}," +
                "${escapeCsv(sessionDateStr)}\n"
            )
        }
        writer.write("\n")

        // Section 4: Daily Review
        if (review != null) {
            writer.write("DAILY REVIEW\n")
            writer.write("Date,Rating,What Went Well,Distractions,Tomorrow Priority\n")
            writer.write(
                "${review.dateKey}," +
                "${review.rating}," +
                "${escapeCsv(review.whatWentWell)}," +
                "${escapeCsv(review.distractions)}," +
                "${escapeCsv(review.tomorrowPriority)}\n"
            )
        }

        writer.flush()
    }

    // ==========================================
    // TXT GENERATION
    // ==========================================
    private fun generateTxt(
        outputStream: OutputStream,
        tasks: List<TaskEntity>,
        sessions: List<FocusSessionEntity>,
        review: DailyReviewEntity?
    ) {
        val writer = outputStream.bufferedWriter()
        val completedCount = tasks.count { it.isCompleted }
        val totalCount = tasks.size
        val rate = if (totalCount > 0) (completedCount * 100) / totalCount else 0
        val totalFocusMins = sessions.sumOf { it.completedSeconds } / 60
        val hours = totalFocusMins / 60
        val mins = totalFocusMins % 60

        writer.write("=================================================================\n")
        writer.write("               FOCUS GUIDE — PRODUCTIVITY REPORT\n")
        writer.write("=================================================================\n")
        writer.write("Generated on: ${dateFormat.format(Date())}\n\n")

        writer.write("------------------- [EXECUTIVE SUMMARY] -------------------------\n")
        writer.write("• Total Focus Time : ${if (hours > 0) "${hours}h ${mins}m" else "${mins}m"}\n")
        writer.write("• Tasks Completed  : $completedCount of $totalCount ($rate%)\n")
        writer.write("• Focus Sessions   : ${sessions.size} recorded sessions\n\n")

        writer.write("------------------- [CATEGORY BREAKDOWN] ------------------------\n")
        val catGroups = tasks.groupBy { it.category }
        if (catGroups.isEmpty()) {
            writer.write("• No tasks recorded.\n")
        } else {
            for ((cat, list) in catGroups) {
                val done = list.count { it.isCompleted }
                writer.write("• ${cat.displayName.padEnd(12)}: ${list.size} total ($done completed)\n")
            }
        }
        writer.write("\n")

        writer.write("------------------- [TASKS LOG] ---------------------------------\n")
        if (tasks.isEmpty()) {
            writer.write("No tasks recorded.\n")
        } else {
            for (task in tasks) {
                val check = if (task.isCompleted) "[✓]" else "[ ]"
                val prio = task.priority.displayName
                val cat = task.category.displayName
                writer.write("$check ${task.title} ($prio | $cat) - ${task.estimatedMinutes}m\n")
                if (task.notes.isNotBlank()) {
                    writer.write("    Notes: ${task.notes}\n")
                }
            }
        }
        writer.write("\n")

        writer.write("------------------- [RECENT FOCUS SESSIONS] ---------------------\n")
        if (sessions.isEmpty()) {
            writer.write("No sessions recorded.\n")
        } else {
            val sessionDateFmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            for (sess in sessions.take(15)) {
                val title = sess.taskTitle ?: "Unassigned Focus"
                val doneMins = sess.completedSeconds / 60
                writer.write("• ${sessionDateFmt.format(Date(sess.timestamp))} | ${sess.sessionType.displayName} | ${doneMins}m / ${sess.durationMinutes}m | $title\n")
            }
        }
        writer.write("\n")

        if (review != null) {
            writer.write("------------------- [DAILY REVIEW REFLECTION] -------------------\n")
            writer.write("• Date              : ${review.dateKey}\n")
            writer.write("• Day Rating        : ${review.rating} / 5 stars\n")
            writer.write("• Tomorrow Priority : ${review.tomorrowPriority.ifBlank { "None specified" }}\n")
            if (review.whatWentWell.isNotBlank()) {
                writer.write("• What Went Well    : ${review.whatWentWell}\n")
            }
            if (review.distractions.isNotBlank()) {
                writer.write("• Distractions      : ${review.distractions}\n")
            }
            writer.write("\n")
        }

        writer.write("=================================================================\n")
        writer.write("      Generated by Focus Guide • Offline, Private & Mindful\n")
        writer.write("=================================================================\n")

        writer.flush()
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
