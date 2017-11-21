package com.github.deividasp.kchip8.vm

import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

/**
 * @author Deividas Popelskis <deividas.popelskis@gmail.com>
 */
class VirtualMachine {

    val input = Input()
    val screen = Screen()

    val stack = Stack<Int>()
    val memory = ByteBuffer.allocate(MEMORY_SIZE)!!

    var delayTimer = 0
    var soundTimer = 0
    var addressRegister = 0

    val dataRegisters = IntArray(DATA_REGISTERS)

    fun loadProgram(data: ByteArray) {
        reset()

        Screen.FONT_DATA.forEach { value -> memory.put(value.toByte()) }

        memory.position(PROGRAM_OFFSET)
        memory.put(data)
        memory.position(PROGRAM_OFFSET)
    }

    fun loadProgram(path: String) {
        val data = Files.readAllBytes(Paths.get(path))

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
    }

}