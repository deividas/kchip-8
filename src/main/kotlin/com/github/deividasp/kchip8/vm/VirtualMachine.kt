package com.github.deividasp.kchip8.vm

import com.github.deividasp.kchip8.interpreter.Interpreter
import java.awt.Toolkit
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * @author Deividas Popelskis <deividas.popelskis@gmail.com>
 */
class VirtualMachine {

    private var executionTask: ScheduledFuture<*>? = null

    private val interpreter = Interpreter(this)

    val input = Input()
    val screen = Screen()

    val stack = Stack<Int>()
    val memory = ByteBuffer.allocate(MEMORY_SIZE)!!

    var delayTimer = 0
    var soundTimer = 0
    var addressRegister = 0

    val dataRegisters = IntArray(DATA_REGISTERS)

    fun startExecution() {
        executionTask?.cancel(true)

        executionTask = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            interpreter.fetchAndExecuteInstruction()

            if (delayTimer > 0)
                delayTimer--

            if (soundTimer > 0) {
                if (soundTimer-- == 1)
                    Toolkit.getDefaultToolkit().beep()
            }
        }, EXECUTION_RATE, EXECUTION_RATE, TimeUnit.MILLISECONDS)
    }

    fun loadProgram(data: ByteArray) {
        reset()

        Screen.FONT_DATA.forEach { memory.put(it.toByte()) }

        memory.position(PROGRAM_OFFSET)
        memory.put(data)
        memory.position(PROGRAM_OFFSET)
    }

    fun loadProgram(path: String) {
        val data = Files.readAllBytes(Paths.get(path))

        if (data.size > PROGRAM_SIZE_LIMIT)
            throw Exception("Program size limit exceeded")

        loadProgram(data)
    }

    private fun reset() {
        input.reset()
        screen.reset()

        stack.clear()
        memory.clear()

        delayTimer = 0
        soundTimer = 0
        addressRegister = 0

        dataRegisters.forEachIndexed { i, _ -> dataRegisters[i] = 0 }
    }

    companion object {
        const val DATA_REGISTERS = 16
        const val MEMORY_SIZE = 0x1000
        const val PROGRAM_OFFSET = 0x200
        const val EXECUTION_RATE = 1000 / 500L
        const val PROGRAM_SIZE_LIMIT = MEMORY_SIZE - PROGRAM_OFFSET
    }

}