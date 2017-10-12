package com.github.deividasp.kchip8.interpreter

import com.github.deividasp.kchip8.emulator.VirtualMachine
import com.github.deividasp.kchip8.extension.load
import org.junit.Test
import kotlin.test.assertEquals

/**
 * @author Deividas Popelskis <deividas.popelskis@gmail.com>
 */
class InterpreterTests {

    private val vm = VirtualMachine()
    private val interpreter = Interpreter(vm)

    @Test
    fun `fetch and execute instruction 0x1`() {
        val instruction = 0x1234
        val position = instruction.and(0x0FFF)

        vm.load(instruction.toShort())
        interpreter.fetchAndExecuteInstruction()

        assertEquals(position, vm.memory.position(), "Incorrect memory position")
    }

    @Test
    fun `fetch and execute instruction 0x3 (condition is unsatisfied)`() {
        val instruction = 0x35FA

        vm.load(instruction.toShort())
        interpreter.fetchAndExecuteInstruction()

        assertEquals(VirtualMachine.PROGRAM_OFFSET + 2, vm.memory.position(), "Incorrect memory position")
    }

    @Test
    fun `fetch and execute instruction 0x3 (condition is satisfied)`() {
        val instruction = 0x35FA
        val registerIndex = instruction.and(0x0F00).shr(8)
        val value = instruction.and(0x00FF)

        vm.load(instruction.toShort())
        vm.dataRegisters[registerIndex] = value
        interpreter.fetchAndExecuteInstruction()

        assertEquals(VirtualMachine.PROGRAM_OFFSET + 4, vm.memory.position(), "Incorrect memory position")
    }

    @Test
    fun `fetch and execute instruction 0x4 (condition is unsatisfied)`() {
        val instruction = 0x45FA
        val registerIndex = instruction.and(0x0F00).shr(8)
        val value = instruction.and(0x00FF)

        vm.load(instruction.toShort())
        vm.dataRegisters[registerIndex] = value
        interpreter.fetchAndExecuteInstruction()

        assertEquals(VirtualMachine.PROGRAM_OFFSET + 2, vm.memory.position(), "Incorrect memory position")
    }

    @Test
    fun `fetch and execute instruction 0x4 (condition is satisfied)`() {
        val instruction = 0x45FA

        vm.load(instruction.toShort())
        interpreter.fetchAndExecuteInstruction()

        assertEquals(VirtualMachine.PROGRAM_OFFSET + 4, vm.memory.position(), "Incorrect memory position")
    }

    @Test
    fun `fetch and execute instruction 0x5 (condition is unsatisfied)`() {
        val instruction = 0x55FA
        val firstRegisterIndex = instruction.and(0x0F00).shr(8)
        val secondRegisterIndex = instruction.and(0x00F0).shr(4)

        vm.load(instruction.toShort())
        vm.dataRegisters[firstRegisterIndex] = 1
        vm.dataRegisters[secondRegisterIndex] = 2
        interpreter.fetchAndExecuteInstruction()

        assertEquals(VirtualMachine.PROGRAM_OFFSET + 2, vm.memory.position(), "Incorrect memory position")
    }

    @Test
    fun `fetch and execute instruction 0x5 (condition is satisfied)`() {
        val instruction = 0x55FA

        vm.load(instruction.toShort())
        interpreter.fetchAndExecuteInstruction()

        assertEquals(VirtualMachine.PROGRAM_OFFSET + 4, vm.memory.position(), "Incorrect memory position")
    }

    @Test
    fun `fetch and execute instruction 0x6`() {
        val instruction = 0x65A0
        val registerIndex = instruction.and(0x0F00).shr(8)
        val value = instruction.and(0x00FF)

        vm.load(instruction.toShort())
        interpreter.fetchAndExecuteInstruction()

        assertEquals(value, vm.dataRegisters[registerIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x7`() {
        val instruction = 0x75AA
        val registerIndex = instruction.and(0x0F00).shr(8)
        val initialValue = 64
        val expectedValue = instruction.and(0x00FF) + initialValue

        vm.load(instruction.toShort())
        vm.dataRegisters[registerIndex] = initialValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedValue, vm.dataRegisters[registerIndex], "Data register value mismatch")
    }

}