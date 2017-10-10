package com.github.deividasp.kchip8.emulator

import com.github.deividasp.kchip8.extension.getResourcePath
import kotlin.test.assertEquals
import org.junit.Test

/**
 * @author Deividas Popelskis <deividas.popelskis@gmail.com>
 */
class VirtualMachineTests {

    @Test
    fun `load program from a byte array`() {
        val data = byteArrayOf(0x10, 0x20, 0x30)

        val vm = VirtualMachine()
        vm.loadProgram(data)

        data.forEach { assertEquals(it, vm.memory.get(), "Memory value mismatch") }
    }

    @Test
    fun `load program from a file`() {
        val fileName = "321"
        val expectedValues = byteArrayOf(3, 2, 1)

        val vm = VirtualMachine()
        vm.loadProgram(this::class.getResourcePath(fileName))

        expectedValues.forEach { assertEquals(it, vm.memory.get(), "Memory value mismatch") }
    }

}