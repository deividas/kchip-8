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

    @Test
    fun `fetch and execute instruction 0x8 - type 0x0`() {
        val instruction = 0x8010
        val targetRegisterIndex = instruction.and(0x0F00).shr(8)
        val sourceRegisterIndex = instruction.and(0x00F0).shr(4)
        val value = 123

        vm.load(instruction.toShort())
        vm.dataRegisters[sourceRegisterIndex] = value
        interpreter.fetchAndExecuteInstruction()

        assertEquals(value, vm.dataRegisters[targetRegisterIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x8 - type 0x1`() {
        val instruction = 0x8011
        val targetRegisterIndex = instruction.and(0x0F00).shr(8)
        val sourceRegisterIndex = instruction.and(0x00F0).shr(4)
        val targetRegisterValue = 123
        val sourceRegisterValue = 321

        vm.load(instruction.toShort())
        vm.dataRegisters[targetRegisterIndex] = targetRegisterValue
        vm.dataRegisters[sourceRegisterIndex] = sourceRegisterValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(targetRegisterValue.or(sourceRegisterValue), vm.dataRegisters[targetRegisterIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x8 - type 0x2`() {
        val instruction = 0x8012
        val targetRegisterIndex = instruction.and(0x0F00).shr(8)
        val sourceRegisterIndex = instruction.and(0x00F0).shr(4)
        val targetRegisterValue = 123
        val sourceRegisterValue = 321

        vm.load(instruction.toShort())
        vm.dataRegisters[targetRegisterIndex] = targetRegisterValue
        vm.dataRegisters[sourceRegisterIndex] = sourceRegisterValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(targetRegisterValue.and(sourceRegisterValue), vm.dataRegisters[targetRegisterIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x8 - type 0x3`() {
        val instruction = 0x8013
        val targetRegisterIndex = instruction.and(0x0F00).shr(8)
        val sourceRegisterIndex = instruction.and(0x00F0).shr(4)
        val targetRegisterValue = 123
        val sourceRegisterValue = 321

        vm.load(instruction.toShort())
        vm.dataRegisters[targetRegisterIndex] = targetRegisterValue
        vm.dataRegisters[sourceRegisterIndex] = sourceRegisterValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(targetRegisterValue.xor(sourceRegisterValue), vm.dataRegisters[targetRegisterIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x8 - type 0x4 (carry flag set)`() {
        val instruction = 0x8014
        val targetRegisterIndex = instruction.and(0x0F00).shr(8)
        val sourceRegisterIndex = instruction.and(0x00F0).shr(4)
        val targetRegisterValue = 123
        val sourceRegisterValue = 321

        vm.load(instruction.toShort())
        vm.dataRegisters[targetRegisterIndex] = targetRegisterValue
        vm.dataRegisters[sourceRegisterIndex] = sourceRegisterValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(1, vm.dataRegisters[0xF], "Carry flag not set")
        assertEquals((targetRegisterValue + sourceRegisterValue).and(0xFF), vm.dataRegisters[targetRegisterIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x8 - type 0x4 (carry flag not set)`() {
        val instruction = 0x8014
        val targetRegisterIndex = instruction.and(0x0F00).shr(8)
        val sourceRegisterIndex = instruction.and(0x00F0).shr(4)
        val targetRegisterValue = 50
        val sourceRegisterValue = 100

        vm.load(instruction.toShort())
        vm.dataRegisters[targetRegisterIndex] = targetRegisterValue
        vm.dataRegisters[sourceRegisterIndex] = sourceRegisterValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(0, vm.dataRegisters[0xF], "Carry flag is set")
        assertEquals(targetRegisterValue + sourceRegisterValue, vm.dataRegisters[targetRegisterIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x8 - type 0x5 (carry flag set)`() {
        val instruction = 0x8015
        val targetRegisterIndex = instruction.and(0x0F00).shr(8)
        val sourceRegisterIndex = instruction.and(0x00F0).shr(4)
        val targetRegisterValue = 321
        val sourceRegisterValue = 123

        vm.load(instruction.toShort())
        vm.dataRegisters[targetRegisterIndex] = targetRegisterValue
        vm.dataRegisters[sourceRegisterIndex] = sourceRegisterValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(1, vm.dataRegisters[0xF], "Carry flag not set")
        assertEquals(targetRegisterValue - sourceRegisterValue, vm.dataRegisters[targetRegisterIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x8 - type 0x5 (carry flag not set)`() {
        val instruction = 0x8015
        val targetRegisterIndex = instruction.and(0x0F00).shr(8)
        val sourceRegisterIndex = instruction.and(0x00F0).shr(4)
        val targetRegisterValue = 123
        val sourceRegisterValue = 321

        vm.load(instruction.toShort())
        vm.dataRegisters[targetRegisterIndex] = targetRegisterValue
        vm.dataRegisters[sourceRegisterIndex] = sourceRegisterValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(0, vm.dataRegisters[0xF], "Carry flag is set")
        assertEquals((targetRegisterValue - sourceRegisterValue).and(0xFF), vm.dataRegisters[targetRegisterIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x8 - type 0x6 (carry flag set)`() {
        val instruction = 0x8016
        val targetRegisterIndex = instruction.and(0x0F00).shr(8)
        val sourceRegisterIndex = instruction.and(0x00F0).shr(4)
        val sourceRegisterValue = 273

        vm.load(instruction.toShort())
        vm.dataRegisters[sourceRegisterIndex] = sourceRegisterValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(1, vm.dataRegisters[0xF], "Carry flag not set")
        assertEquals(sourceRegisterValue.shr(1), vm.dataRegisters[targetRegisterIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x8 - type 0x6 (carry flag not set)`() {
        val instruction = 0x8016
        val targetRegisterIndex = instruction.and(0x0F00).shr(8)
        val sourceRegisterIndex = instruction.and(0x00F0).shr(4)
        val sourceRegisterValue = 272

        vm.load(instruction.toShort())
        vm.dataRegisters[sourceRegisterIndex] = sourceRegisterValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(0, vm.dataRegisters[0xF], "Carry flag is set")
        assertEquals(sourceRegisterValue.shr(1), vm.dataRegisters[targetRegisterIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x8 - type 0x7 (carry flag set)`() {
        val instruction = 0x8017
        val targetRegisterIndex = instruction.and(0x0F00).shr(8)
        val sourceRegisterIndex = instruction.and(0x00F0).shr(4)
        val targetRegisterValue = 200
        val sourceRegisterValue = 300

        vm.load(instruction.toShort())
        vm.dataRegisters[targetRegisterIndex] = targetRegisterValue
        vm.dataRegisters[sourceRegisterIndex] = sourceRegisterValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(1, vm.dataRegisters[0xF], "Carry flag not set")
        assertEquals(sourceRegisterValue - targetRegisterValue, vm.dataRegisters[targetRegisterIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x8 - type 0x7 (carry flag not set)`() {
        val instruction = 0x8017
        val targetRegisterIndex = instruction.and(0x0F00).shr(8)
        val sourceRegisterIndex = instruction.and(0x00F0).shr(4)
        val targetRegisterValue = 321
        val sourceRegisterValue = 123

        vm.load(instruction.toShort())
        vm.dataRegisters[targetRegisterIndex] = targetRegisterValue
        vm.dataRegisters[sourceRegisterIndex] = sourceRegisterValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(0, vm.dataRegisters[0xF], "Carry flag is set")
        assertEquals((sourceRegisterValue - targetRegisterValue).and(0xFF), vm.dataRegisters[targetRegisterIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x8 - type 0xE (carry flag set)`() {
        val instruction = 0x801E
        val targetRegisterIndex = instruction.and(0x0F00).shr(8)
        val sourceRegisterIndex = instruction.and(0x00F0).shr(4)
        val sourceRegisterValue = 128

        vm.load(instruction.toShort())
        vm.dataRegisters[sourceRegisterIndex] = sourceRegisterValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(1, vm.dataRegisters[0xF], "Carry flag not set")
        assertEquals(sourceRegisterValue.shl(1), vm.dataRegisters[targetRegisterIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x8 - type 0xE (carry flag not set)`() {
        val instruction = 0x801E
        val targetRegisterIndex = instruction.and(0x0F00).shr(8)
        val sourceRegisterIndex = instruction.and(0x00F0).shr(4)
        val sourceRegisterValue = 10

        vm.load(instruction.toShort())
        vm.dataRegisters[sourceRegisterIndex] = sourceRegisterValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(0, vm.dataRegisters[0xF], "Carry flag is set")
        assertEquals(sourceRegisterValue.shl(1), vm.dataRegisters[targetRegisterIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x9 (condition is unsatisfied)`() {
        val instruction = 0x95FA

        vm.load(instruction.toShort())
        interpreter.fetchAndExecuteInstruction()

        assertEquals(VirtualMachine.PROGRAM_OFFSET + 2, vm.memory.position(), "Incorrect memory position")
    }

    @Test
    fun `fetch and execute instruction 0x9 (condition is satisfied)`() {
        val instruction = 0x95FA
        val firstRegisterIndex = instruction.and(0x0F00).shr(8)
        val secondRegisterIndex = instruction.and(0x00F0).shr(4)

        vm.load(instruction.toShort())
        vm.dataRegisters[firstRegisterIndex] = 1
        vm.dataRegisters[secondRegisterIndex] = 2
        interpreter.fetchAndExecuteInstruction()

        assertEquals(VirtualMachine.PROGRAM_OFFSET + 4, vm.memory.position(), "Incorrect memory position")
    }

}