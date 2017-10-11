package com.github.deividasp.kchip8.interpreter

import com.github.deividasp.kchip8.emulator.VirtualMachine
import com.github.deividasp.kchip8.extension.load
import org.junit.Test
import kotlin.test.assertEquals

/**
 * @author Deividas Popelskis <deividas.popelskis@gmail.com>
 */
class InterpreterTests {

    @Test
    fun `fetch and execute instruction 0x1`() {
        val instruction = 0x1234.toShort()

        val vm = VirtualMachine()
        vm.load(instruction)

        val interpreter = Interpreter(vm)
        interpreter.fetchAndExecuteInstruction()

        assertEquals(0x234, vm.memory.position())
    }

}