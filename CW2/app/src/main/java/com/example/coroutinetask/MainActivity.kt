
package com.example.coroutinetask

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var countInput: EditText
    private lateinit var sequentialButton: Button
    private lateinit var parallelButton: Button
    private lateinit var cancelButton: Button
    private lateinit var overlayCancelButton: Button

    private lateinit var overlay: View
    private lateinit var statusText: TextView
    private lateinit var progressLabel: TextView
    private lateinit var seekBar: SeekBar

    private val repository = Repository()

    private val lock = Any()
    private val threads = mutableListOf<Thread>()
    private val progressValues = mutableListOf<Int>()

    private var running = false
    private var currentRunId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        countInput = findViewById(R.id.countInput)

        sequentialButton = findViewById(R.id.sequentialButton)
        parallelButton = findViewById(R.id.parallelButton)
        cancelButton = findViewById(R.id.cancelButton)
        overlayCancelButton = findViewById(R.id.overlayCancelButton)

        overlay = findViewById(R.id.overlay)
        statusText = findViewById(R.id.statusText)
        progressLabel = findViewById(R.id.progressLabel)
        seekBar = findViewById(R.id.seekBar)

        sequentialButton.setOnClickListener {
            startSequential()
        }

        parallelButton.setOnClickListener {
            startParallel()
        }

        cancelButton.setOnClickListener {
            cancelWork()
        }

        overlayCancelButton.setOnClickListener {
            cancelWork()
        }
    }

    // Получение количества задач

    private fun getCount(): Int? {

        val count = countInput.text.toString().toIntOrNull()

        if (count == null || count !in 1..100) {

            Toast.makeText(
                this,
                R.string.invalid_count,
                Toast.LENGTH_SHORT
            ).show()

            return null
        }

        return count
    }

    // Подготовка к запуску

    private fun startWork(count: Int): Int {

        val runId: Int

        synchronized(lock) {

            currentRunId++
            runId = currentRunId

            running = true
            threads.clear()
            progressValues.clear()

            repeat(count) {
                progressValues.add(0)
            }
        }

        overlay.visibility = View.VISIBLE

        sequentialButton.isEnabled = false
        parallelButton.isEnabled = false
        cancelButton.isEnabled = false
        countInput.isEnabled = false

        seekBar.progress = 0
        progressLabel.text = getString(R.string.progress, 0)
        statusText.text = getString(R.string.running)

        return runId
    }

    // Последовательный запуск

    private fun startSequential() {

        val count = getCount() ?: return
        val runId = startWork(count)

        val worker = Thread {

            try {

                for (i in 0 until count) {

                    checkInterrupted()

                    runOnUiThread {

                        if (isCurrentRun(runId)) {
                            statusText.text = getString(
                                R.string.task_status,
                                i + 1,
                                count
                            )
                        }
                    }

                    repository.doHardWork { value ->

                        checkInterrupted()

                        updateProgress(runId, i, value)
                    }
                }

                finishWork(runId, R.string.completed)

            } catch (_: InterruptedException) {

                finishWork(runId, R.string.cancelled)
            }
        }

        addAndStart(runId, worker)
    }

    // Параллельный запуск

    private fun startParallel() {

        val count = getCount() ?: return
        val runId = startWork(count)

        val completedTasks = java.util.concurrent.atomic.AtomicInteger(0)

        val workerThreads = (0 until count).map { index ->

            Thread {

                try {

                    repository.doHardWork { value ->

                        checkInterrupted()

                        updateProgress(runId, index, value)
                    }

                } catch (_: InterruptedException) {
                    // Поток отменён
                } finally {

                    val completed = completedTasks.incrementAndGet()

                    if (completed == count) {
                        finishWork(runId, R.string.completed)
                    }
                }
            }
        }

        synchronized(lock) {

            if (currentRunId != runId || !running) {
                workerThreads.forEach { it.interrupt() }
                return
            }

            threads.addAll(workerThreads)
        }

        workerThreads.forEach { thread ->
            thread.start()
        }
    }

    // Регистрация и запуск потока

    private fun addAndStart(runId: Int, thread: Thread) {

        synchronized(lock) {

            if (currentRunId != runId || !running) {
                return
            }

            threads.add(thread)
        }

        thread.start()
    }

    // Проверка отмены

    private fun checkInterrupted() {

        if (Thread.currentThread().isInterrupted) {
            throw InterruptedException()
        }
    }

    // Проверка актуальности операции

    private fun isCurrentRun(runId: Int): Boolean {

        synchronized(lock) {
            return currentRunId == runId && running
        }
    }

    // Обновление SeekBar

    private fun updateProgress(
        runId: Int,
        index: Int,
        value: Int
    ) {

        val average: Int

        synchronized(lock) {

            if (currentRunId != runId || !running) return
            if (index !in progressValues.indices) return

            progressValues[index] = value

            average = progressValues.sum() / progressValues.size
        }

        runOnUiThread {

            if (isCurrentRun(runId)) {

                seekBar.progress = average

                progressLabel.text = getString(
                    R.string.progress,
                    average
                )
            }
        }
    }

    // Завершение операции

    private fun finishWork(runId: Int, messageId: Int) {

        runOnUiThread {

            val shouldFinish = synchronized(lock) {

                if (currentRunId != runId || !running) {
                    false
                } else {
                    running = false
                    threads.clear()
                    true
                }
            }

            if (!shouldFinish) return@runOnUiThread

            overlay.visibility = View.GONE

            sequentialButton.isEnabled = true
            parallelButton.isEnabled = true
            cancelButton.isEnabled = false
            countInput.isEnabled = true

            statusText.text = getString(messageId)
        }
    }

    // Отмена всех потоков

    private fun cancelWork() {

        val oldThreads: List<Thread>

        synchronized(lock) {

            currentRunId++
            running = false

            oldThreads = threads.toList()
            threads.clear()
        }

        oldThreads.forEach { thread ->
            thread.interrupt()
        }

        overlay.visibility = View.GONE

        sequentialButton.isEnabled = true
        parallelButton.isEnabled = true
        cancelButton.isEnabled = false
        countInput.isEnabled = true

        statusText.text = getString(R.string.cancelled)
    }

    override fun onDestroy() {

        cancelWork()

        super.onDestroy()
    }
}
