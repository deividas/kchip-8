package com.github.deividasp.kchip8.emulator

import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Paths

/**
 * @author Deividas Popelskis <deividas.popelskis@gmail.com>
 */
class VirtualMachine {

    val memory = ByteBuffer.allocate(MEMORY_SIZE)!!
    val dataRegisters = IntArray(DATA_REGISTERS)

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
        memory.clear()
    }

    companion object {
        const val DATA_REGISTERS = 16
        const val MEMORY_SIZE = 0x1000
        const val PROGRAM_OFFSET = 0x200
    }

}