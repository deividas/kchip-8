package com.github.deividasp.kchip8.vm

import com.github.deividasp.kchip8.extension.getResourcePath
import com.github.deividasp.kchip8.extension.load
import org.junit.Test
import kotlin.test.assertEquals

/**
 * @author Deividas Popelskis <deividas.popelskis@gmail.com>
 */
class VirtualMachineTests {

    private val vm = VirtualMachine()

    @Test
    fun `load program from a byte array`() {
        val data = byteArrayOf(0x10, 0x20, 0x30)

        vm.loadProgram(data)

        data.forEach { assertEquals(it, vm.memory.get(), "Memory value mismatch") }
    }

    @Test
    fun `load program from a file`() {
        val fileName = "321"
        val expectedValues = byteArrayOf(3, 2, 1)

        vm.loadProgram(this::class.getResourcePath(fileName))

        expectedValues.forEach { assertEquals(it, vm.memory.get(), "Memory value mismatch") }
    }

    @Test
    fun `load instructions`() {
        val instructions = shortArrayOf(0x1012, 0x2123, 0x3234)

        vm.load(*instructions)

        instructions.forEach { assertEquals(it, vm.memory.short, "Memory value mismatch") }
    }

}