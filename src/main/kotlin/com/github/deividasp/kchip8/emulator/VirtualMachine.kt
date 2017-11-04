package com.github.deividasp.kchip8.emulator

import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Paths

/**
 * @author Deividas Popelskis <deividas.popelskis@gmail.com>
 */
class VirtualMachine {

    val screen = Screen()
    val dataRegisters = IntArray(DATA_REGISTERS)
    val memory = ByteBuffer.allocate(MEMORY_SIZE)!!

    var addressRegister = 0

    fun loadProgram(data: ByteArray) {
        reset()

        memory.position(PROGRAM_OFFSET)
        memory.put(data)
        memory.position(PROGRAM_OFFSET)
    }

    fun loadProgram(path: String) {
        val data = Files.readAllBytes(Paths.get(path))

        loadProgram(data)
    }

    private fun reset() {
        screen.clear()
        memory.clear()
        addressRegister = 0
        dataRegisters.forEachIndexed { i, _ -> dataRegisters[i] = 0 }
    }

    companion object {
        const val DATA_REGISTERS = 16
        const val MEMORY_SIZE = 0x1000
        const val PROGRAM_OFFSET = 0x200
    }

}