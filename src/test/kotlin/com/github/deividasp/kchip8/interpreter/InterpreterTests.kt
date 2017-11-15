package com.github.deividasp.kchip8.interpreter

import com.github.deividasp.kchip8.emulator.Screen
import com.github.deividasp.kchip8.emulator.VirtualMachine
import com.github.deividasp.kchip8.extension.load
import com.github.deividasp.kchip8.extension.setPressedKey
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import java.util.*
import kotlin.test.assertEquals

/**
 * @author Deividas Popelskis <deividas.popelskis@gmail.com>
 */
class InterpreterTests {

    private val vm = VirtualMachine()
    private val interpreter = Interpreter(vm)

    @Before
    fun `init`() {
        val random = mock(Random::class.java)
        `when`(random.nextInt(ArgumentMatchers.anyInt())).thenReturn(MOCKED_RANDOM_VALUE)

        interpreter.random = random
    }

    @Test
    fun `fetch and execute instruction 0x0 - type 0xE0`() {
        val instruction = 0x00E0
        val activePixelCoordinates = Pair(0, 0)
        val pixelExpectedActive = false

        vm.load(instruction.toShort())
        vm.screen.setPixelActive(activePixelCoordinates.first, activePixelCoordinates.second, true)
        interpreter.fetchAndExecuteInstruction()

        assertEquals(pixelExpectedActive, vm.screen.isPixelActive(activePixelCoordinates.first, activePixelCoordinates.second), "Pixel is active")
    }

    @Test
    fun `fetch and execute instruction 0x0 - type 0xEE`() {
        val instruction = 0x00EE
        val position = 0x0FEF

        vm.load(instruction.toShort())
        vm.stack.push(position)
        interpreter.fetchAndExecuteInstruction()

        assertEquals(position, vm.memory.position())
    }

    @Test
    fun `fetch and execute instruction 0x1`() {
        val instruction = 0x1234
        val expectedPosition = instruction.and(0x0FFF)

        vm.load(instruction.toShort())
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedPosition, vm.memory.position(), "Incorrect memory position")
    }

    @Test
    fun `fetch and execute instruction 0x2`() {
        val instruction = 0x2345
        val expectedPosition = instruction.and(0x0FFF)

        vm.load(instruction.toShort())
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedPosition, vm.memory.position(), "Incorrect memory position")
        assertEquals(VirtualMachine.PROGRAM_OFFSET + 2, vm.stack.peek(), "Incorrect stack value")
    }

    @Test
    fun `fetch and execute instruction 0x3 (condition is unsatisfied)`() {
        val instruction = 0x35FA
        val expectedPosition = VirtualMachine.PROGRAM_OFFSET + 2

        vm.load(instruction.toShort())
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedPosition, vm.memory.position(), "Incorrect memory position")
    }

    @Test
    fun `fetch and execute instruction 0x3 (condition is satisfied)`() {
        val instruction = 0x35FA
        val registerIndex = instruction.and(0x0F00).shr(8)
        val registerValue = instruction.and(0x00FF)
        val expectedPosition = VirtualMachine.PROGRAM_OFFSET + 4

        vm.load(instruction.toShort())
        vm.dataRegisters[registerIndex] = registerValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedPosition, vm.memory.position(), "Incorrect memory position")
    }

    @Test
    fun `fetch and execute instruction 0x4 (condition is unsatisfied)`() {
        val instruction = 0x45FA
        val registerIndex = instruction.and(0x0F00).shr(8)
        val registerValue = instruction.and(0x00FF)
        val expectedPosition = VirtualMachine.PROGRAM_OFFSET + 2

        vm.load(instruction.toShort())
        vm.dataRegisters[registerIndex] = registerValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedPosition, vm.memory.position(), "Incorrect memory position")
    }

    @Test
    fun `fetch and execute instruction 0x4 (condition is satisfied)`() {
        val instruction = 0x45FA
        val expectedPosition = VirtualMachine.PROGRAM_OFFSET + 4

        vm.load(instruction.toShort())
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedPosition, vm.memory.position(), "Incorrect memory position")
    }

    @Test
    fun `fetch and execute instruction 0x5 (condition is unsatisfied)`() {
        val instruction = 0x55FA
        val firstRegisterIndex = instruction.and(0x0F00).shr(8)
        val secondRegisterIndex = instruction.and(0x00F0).shr(4)
        val expectedPosition = VirtualMachine.PROGRAM_OFFSET + 2

        vm.load(instruction.toShort())
        vm.dataRegisters[firstRegisterIndex] = 1
        vm.dataRegisters[secondRegisterIndex] = 2
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedPosition, vm.memory.position(), "Incorrect memory position")
    }

    @Test
    fun `fetch and execute instruction 0x5 (condition is satisfied)`() {
        val instruction = 0x55FA
        val expectedPosition = VirtualMachine.PROGRAM_OFFSET + 4

        vm.load(instruction.toShort())
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedPosition, vm.memory.position(), "Incorrect memory position")
    }

    @Test
    fun `fetch and execute instruction 0x6`() {
        val instruction = 0x65A0
        val registerIndex = instruction.and(0x0F00).shr(8)
        val expectedValue = instruction.and(0x00FF)

        vm.load(instruction.toShort())
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedValue, vm.dataRegisters[registerIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x7`() {
        val instruction = 0x75AA
        val registerIndex = instruction.and(0x0F00).shr(8)
        val registerValue = 64
        val expectedValue = instruction.and(0x00FF) + registerValue

        vm.load(instruction.toShort())
        vm.dataRegisters[registerIndex] = registerValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedValue, vm.dataRegisters[registerIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x8 - type 0x0`() {
        val instruction = 0x8010
        val targetRegisterIndex = instruction.and(0x0F00).shr(8)
        val sourceRegisterIndex = instruction.and(0x00F0).shr(4)
        val sourceRegisterValue = 123

        vm.load(instruction.toShort())
        vm.dataRegisters[sourceRegisterIndex] = sourceRegisterValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(sourceRegisterValue, vm.dataRegisters[targetRegisterIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x8 - type 0x1`() {
        val instruction = 0x8011
        val targetRegisterIndex = instruction.and(0x0F00).shr(8)
        val sourceRegisterIndex = instruction.and(0x00F0).shr(4)
        val targetRegisterValue = 123
        val sourceRegisterValue = 321
        val expectedValue = targetRegisterValue.or(sourceRegisterValue)

        vm.load(instruction.toShort())
        vm.dataRegisters[targetRegisterIndex] = targetRegisterValue
        vm.dataRegisters[sourceRegisterIndex] = sourceRegisterValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedValue, vm.dataRegisters[targetRegisterIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x8 - type 0x2`() {
        val instruction = 0x8012
        val targetRegisterIndex = instruction.and(0x0F00).shr(8)
        val sourceRegisterIndex = instruction.and(0x00F0).shr(4)
        val targetRegisterValue = 123
        val sourceRegisterValue = 321
        val expectedValue = targetRegisterValue.and(sourceRegisterValue)

        vm.load(instruction.toShort())
        vm.dataRegisters[targetRegisterIndex] = targetRegisterValue
        vm.dataRegisters[sourceRegisterIndex] = sourceRegisterValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedValue, vm.dataRegisters[targetRegisterIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x8 - type 0x3`() {
        val instruction = 0x8013
        val targetRegisterIndex = instruction.and(0x0F00).shr(8)
        val sourceRegisterIndex = instruction.and(0x00F0).shr(4)
        val targetRegisterValue = 123
        val sourceRegisterValue = 321
        val expectedValue = targetRegisterValue.xor(sourceRegisterValue)

        vm.load(instruction.toShort())
        vm.dataRegisters[targetRegisterIndex] = targetRegisterValue
        vm.dataRegisters[sourceRegisterIndex] = sourceRegisterValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedValue, vm.dataRegisters[targetRegisterIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x8 - type 0x4 (carry flag set)`() {
        val instruction = 0x8014
        val targetRegisterIndex = instruction.and(0x0F00).shr(8)
        val sourceRegisterIndex = instruction.and(0x00F0).shr(4)
        val targetRegisterValue = 123
        val sourceRegisterValue = 321
        val expectedFlagValue = 1
        val expectedRegisterValue = (targetRegisterValue + sourceRegisterValue).and(0xFF)

        vm.load(instruction.toShort())
        vm.dataRegisters[targetRegisterIndex] = targetRegisterValue
        vm.dataRegisters[sourceRegisterIndex] = sourceRegisterValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedFlagValue, vm.dataRegisters[0xF], "Carry flag not set")
        assertEquals(expectedRegisterValue, vm.dataRegisters[targetRegisterIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x8 - type 0x4 (carry flag not set)`() {
        val instruction = 0x8014
        val targetRegisterIndex = instruction.and(0x0F00).shr(8)
        val sourceRegisterIndex = instruction.and(0x00F0).shr(4)
        val targetRegisterValue = 50
        val sourceRegisterValue = 100
        val expectedFlagValue = 0
        val expectedRegisterValue = targetRegisterValue + sourceRegisterValue

        vm.load(instruction.toShort())
        vm.dataRegisters[targetRegisterIndex] = targetRegisterValue
        vm.dataRegisters[sourceRegisterIndex] = sourceRegisterValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedFlagValue, vm.dataRegisters[0xF], "Carry flag is set")
        assertEquals(expectedRegisterValue, vm.dataRegisters[targetRegisterIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x8 - type 0x5 (borrow flag set)`() {
        val instruction = 0x8015
        val targetRegisterIndex = instruction.and(0x0F00).shr(8)
        val sourceRegisterIndex = instruction.and(0x00F0).shr(4)
        val targetRegisterValue = 321
        val sourceRegisterValue = 123
        val expectedFlagValue = 1
        val expectedRegisterValue = targetRegisterValue - sourceRegisterValue

        vm.load(instruction.toShort())
        vm.dataRegisters[targetRegisterIndex] = targetRegisterValue
        vm.dataRegisters[sourceRegisterIndex] = sourceRegisterValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedFlagValue, vm.dataRegisters[0xF], "Borrow flag not set")
        assertEquals(expectedRegisterValue, vm.dataRegisters[targetRegisterIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x8 - type 0x5 (borrow flag not set)`() {
        val instruction = 0x8015
        val targetRegisterIndex = instruction.and(0x0F00).shr(8)
        val sourceRegisterIndex = instruction.and(0x00F0).shr(4)
        val targetRegisterValue = 123
        val sourceRegisterValue = 321
        val expectedFlagValue = 0
        val expectedRegisterValue = (targetRegisterValue - sourceRegisterValue).and(0xFF)

        vm.load(instruction.toShort())
        vm.dataRegisters[targetRegisterIndex] = targetRegisterValue
        vm.dataRegisters[sourceRegisterIndex] = sourceRegisterValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedFlagValue, vm.dataRegisters[0xF], "Borrow flag is set")
        assertEquals(expectedRegisterValue, vm.dataRegisters[targetRegisterIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x8 - type 0x6 (carry flag set)`() {
        val instruction = 0x8016
        val targetRegisterIndex = instruction.and(0x0F00).shr(8)
        val sourceRegisterIndex = instruction.and(0x00F0).shr(4)
        val sourceRegisterValue = 273
        val expectedFlagValue = 1
        val expectedRegisterValue = sourceRegisterValue.shr(1)

        vm.load(instruction.toShort())
        vm.dataRegisters[sourceRegisterIndex] = sourceRegisterValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedFlagValue, vm.dataRegisters[0xF], "Carry flag not set")
        assertEquals(expectedRegisterValue, vm.dataRegisters[targetRegisterIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x8 - type 0x6 (carry flag not set)`() {
        val instruction = 0x8016
        val targetRegisterIndex = instruction.and(0x0F00).shr(8)
        val sourceRegisterIndex = instruction.and(0x00F0).shr(4)
        val sourceRegisterValue = 272
        val expectedFlagValue = 0
        val expectedRegisterValue = sourceRegisterValue.shr(1)

        vm.load(instruction.toShort())
        vm.dataRegisters[sourceRegisterIndex] = sourceRegisterValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedFlagValue, vm.dataRegisters[0xF], "Carry flag is set")
        assertEquals(expectedRegisterValue, vm.dataRegisters[targetRegisterIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x8 - type 0x7 (borrow flag set)`() {
        val instruction = 0x8017
        val targetRegisterIndex = instruction.and(0x0F00).shr(8)
        val sourceRegisterIndex = instruction.and(0x00F0).shr(4)
        val targetRegisterValue = 200
        val sourceRegisterValue = 300
        val expectedFlagValue = 1
        val expectedRegisterValue = sourceRegisterValue - targetRegisterValue

        vm.load(instruction.toShort())
        vm.dataRegisters[targetRegisterIndex] = targetRegisterValue
        vm.dataRegisters[sourceRegisterIndex] = sourceRegisterValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedFlagValue, vm.dataRegisters[0xF], "Borrow flag not set")
        assertEquals(expectedRegisterValue, vm.dataRegisters[targetRegisterIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x8 - type 0x7 (borrow flag not set)`() {
        val instruction = 0x8017
        val targetRegisterIndex = instruction.and(0x0F00).shr(8)
        val sourceRegisterIndex = instruction.and(0x00F0).shr(4)
        val targetRegisterValue = 321
        val sourceRegisterValue = 123
        val expectedFlagValue = 0
        val expectedRegisterValue = (sourceRegisterValue - targetRegisterValue).and(0xFF)

        vm.load(instruction.toShort())
        vm.dataRegisters[targetRegisterIndex] = targetRegisterValue
        vm.dataRegisters[sourceRegisterIndex] = sourceRegisterValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedFlagValue, vm.dataRegisters[0xF], "Borrow flag is set")
        assertEquals(expectedRegisterValue, vm.dataRegisters[targetRegisterIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x8 - type 0xE (carry flag set)`() {
        val instruction = 0x801E
        val targetRegisterIndex = instruction.and(0x0F00).shr(8)
        val sourceRegisterIndex = instruction.and(0x00F0).shr(4)
        val sourceRegisterValue = 128
        val expectedFlagValue = 1
        val expectedRegisterValue = sourceRegisterValue.shl(1)

        vm.load(instruction.toShort())
        vm.dataRegisters[sourceRegisterIndex] = sourceRegisterValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedFlagValue, vm.dataRegisters[0xF], "Carry flag not set")
        assertEquals(expectedRegisterValue, vm.dataRegisters[targetRegisterIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x8 - type 0xE (carry flag not set)`() {
        val instruction = 0x801E
        val targetRegisterIndex = instruction.and(0x0F00).shr(8)
        val sourceRegisterIndex = instruction.and(0x00F0).shr(4)
        val sourceRegisterValue = 10
        val expectedFlagValue = 0
        val expectedRegisterValue = sourceRegisterValue.shl(1)

        vm.load(instruction.toShort())
        vm.dataRegisters[sourceRegisterIndex] = sourceRegisterValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedFlagValue, vm.dataRegisters[0xF], "Carry flag is set")
        assertEquals(expectedRegisterValue, vm.dataRegisters[targetRegisterIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0x9 (condition is unsatisfied)`() {
        val instruction = 0x95FA
        val expectedPosition = VirtualMachine.PROGRAM_OFFSET + 2

        vm.load(instruction.toShort())
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedPosition, vm.memory.position(), "Incorrect memory position")
    }

    @Test
    fun `fetch and execute instruction 0x9 (condition is satisfied)`() {
        val instruction = 0x95FA
        val firstRegisterIndex = instruction.and(0x0F00).shr(8)
        val secondRegisterIndex = instruction.and(0x00F0).shr(4)
        val firstRegisterValue = 1
        val secondRegisterValue = 2
        val expectedPosition = VirtualMachine.PROGRAM_OFFSET + 4

        vm.load(instruction.toShort())
        vm.dataRegisters[firstRegisterIndex] = firstRegisterValue
        vm.dataRegisters[secondRegisterIndex] = secondRegisterValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedPosition, vm.memory.position(), "Incorrect memory position")
    }

    @Test
    fun `fetch and execute instruction 0xA`() {
        val instruction = 0xA5AF
        val expectedValue = instruction.and(0x0FFF)

        vm.load(instruction.toShort())
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedValue, vm.addressRegister, "Address register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0xB`() {
        val instruction = 0xBBAE
        val registerValue = 50
        val expectedPosition = instruction.and(0x0FFF) + registerValue

        vm.load(instruction.toShort())
        vm.dataRegisters[0] = registerValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedPosition, vm.memory.position(), "Incorrect memory position")
    }

    @Test
    fun `fetch and execute instruction 0xC`() {
        val instruction = 0xCDBA
        val registerIndex = instruction.and(0x0F00).shr(8)
        val baseValue = instruction.and(0x00FF)
        val expectedValue = baseValue.and(MOCKED_RANDOM_VALUE)

        vm.load(instruction.toShort())
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedValue, vm.dataRegisters[registerIndex], "Incorrect memory position")
    }

    @Test
    fun `fetch and execute instruction 0xD (collision flag not set)`() {
        val instruction = 0xD012
        val firstRegisterIndex = instruction.and(0x0F00).shr(8)
        val secondRegisterIndex = instruction.and(0x00F0).shr(4)
        val height = instruction.and(0x000F)
        val baseX = 10
        val baseY = 5
        val pixels = intArrayOf(0xEF, 0xAA)
        val expectedFlagValue = 0

        vm.load(instruction.toShort())
        pixels.forEachIndexed { i, pixel -> vm.memory.put(i, pixel.toByte()) }
        vm.dataRegisters[firstRegisterIndex] = baseX
        vm.dataRegisters[secondRegisterIndex] = baseY
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedFlagValue, vm.dataRegisters[0xF], "Collision flag is set")

        (0 until height).forEach { yOffset ->
            (0 until Screen.SPRITE_WIDTH).forEach { xOffset ->
                val expectedActive = pixels[yOffset].and(0x80.shr(xOffset)) > 0
                val x = baseX + xOffset
                val y = baseY + yOffset
                assertEquals(expectedActive, vm.screen.isPixelActive(x, y))
            }
        }
    }

    @Test
    fun `fetch and execute instruction 0xD (collision flag is set)`() {
        val instruction = 0xD012
        val firstRegisterIndex = instruction.and(0x0F00).shr(8)
        val secondRegisterIndex = instruction.and(0x00F0).shr(4)
        val height = instruction.and(0x000F)
        val baseX = 10
        val baseY = 5
        val pixels = intArrayOf(0xEF, 0xAA)
        val expectedFlagValue = 1

        vm.load(instruction.toShort())
        pixels.forEachIndexed { i, pixel -> vm.memory.put(i, pixel.toByte()) }
        vm.dataRegisters[firstRegisterIndex] = baseX
        vm.dataRegisters[secondRegisterIndex] = baseY
        vm.screen.setPixelActive(baseX, baseY, true)
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedFlagValue, vm.dataRegisters[0xF], "Collision flag is not set")

        (0 until height).forEach { yOffset ->
            (0 until Screen.SPRITE_WIDTH).forEach { xOffset ->
                val x = baseX + xOffset
                val y = baseY + yOffset
                val expectedActive = pixels[yOffset].and(0x80.shr(xOffset)) > 0 && x != baseX && y != baseY

                assertEquals(expectedActive, vm.screen.isPixelActive(x, y) && x != baseX && y != baseY, "Incorrect pixel state at [x: $x, y: $y]")
            }
        }
    }

    @Test
    fun `fetch and execute instruction 0xE - type 0x9E (key is not pressed)`() {
        val instruction = 0xE19E
        val expectedPosition = VirtualMachine.PROGRAM_OFFSET + 2

        vm.load(instruction.toShort())
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedPosition, vm.memory.position(), "Incorrect memory position")
    }

    @Test
    fun `fetch and execute instruction 0xE - type 0x9E (key is pressed)`() {
        val instruction = 0xE19E
        val registerIndex = instruction.and(0x0F00).shr(8)
        val key = 0x5
        val expectedPosition = VirtualMachine.PROGRAM_OFFSET + 4

        vm.load(instruction.toShort())
        vm.dataRegisters[registerIndex] = key
        vm.input.setPressedKey(key)
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedPosition, vm.memory.position(), "Incorrect memory position")
    }

    @Test
    fun `fetch and execute instruction 0xE - type 0xA1 (key is pressed)`() {
        val instruction = 0xE1A1
        val registerIndex = instruction.and(0x0F00).shr(8)
        val key = 0x5
        val expectedPosition = VirtualMachine.PROGRAM_OFFSET + 2

        vm.load(instruction.toShort())
        vm.dataRegisters[registerIndex] = key
        vm.input.setPressedKey(key)
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedPosition, vm.memory.position(), "Incorrect memory position")
    }

    @Test
    fun `fetch and execute instruction 0xE - type 0xA1 (key is not pressed)`() {
        val instruction = 0xE1A1
        val expectedPosition = VirtualMachine.PROGRAM_OFFSET + 4

        vm.load(instruction.toShort())
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedPosition, vm.memory.position(), "Incorrect memory position")
    }

    @Test
    fun `fetch and execute instruction 0xF - type 0x07`() {
        val instruction = 0xF107
        val registerIndex = instruction.and(0x0F00).shr(8)
        val delayTimerValue = 10

        vm.load(instruction.toShort())
        vm.delayTimer = delayTimerValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(delayTimerValue, vm.dataRegisters[registerIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0xF - type 0x0A`() {
        val instruction = 0xF20A
        val registerIndex = instruction.and(0x0F00).shr(8)
        val key = 0x1

        vm.load(instruction.toShort())
        vm.input.setPressedKey(key)
        interpreter.fetchAndExecuteInstruction()

        assertEquals(key, vm.dataRegisters[registerIndex], "Data register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0xF - type 0x15`() {
        val instruction = 0xF315
        val registerIndex = instruction.and(0x0F00).shr(8)
        val delayTimerValue = 120

        vm.load(instruction.toShort())
        vm.dataRegisters[registerIndex] = delayTimerValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(vm.delayTimer, delayTimerValue, "Delay timer value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0xF - type 0x18`() {
        val instruction = 0xF418
        val registerIndex = instruction.and(0x0F00).shr(8)
        val soundTimerValue = 210

        vm.load(instruction.toShort())
        vm.dataRegisters[registerIndex] = soundTimerValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(vm.soundTimer, soundTimerValue, "Sound timer value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0xF - type 0x1E`() {
        val instruction = 0xF51E
        val registerIndex = instruction.and(0x0F00).shr(8)
        val dataRegisterValue = 20
        val addressRegisterValue = 40
        val expectedValue = dataRegisterValue + addressRegisterValue

        vm.load(instruction.toShort())
        vm.dataRegisters[registerIndex] = dataRegisterValue
        vm.addressRegister = addressRegisterValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals(expectedValue, vm.addressRegister, "Address register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0xF - type 0x29`() {
        val instruction = 0xF129
        val registerIndex = instruction.and(0x0F00).shr(8)
        val character = 0xA

        vm.load(instruction.toShort())
        vm.dataRegisters[registerIndex] = character
        interpreter.fetchAndExecuteInstruction()

        assertEquals(character * Screen.FONT_CHARACTER_LENGTH, vm.addressRegister, "Address register value mismatch")
    }

    @Test
    fun `fetch and execute instruction 0xF - type 0x33`() {
        val instruction = 0xF133
        val registerIndex = instruction.and(0x0F00).shr(8)
        val dataRegisterValue = 30
        val addressRegisterValue = 20

        vm.load(instruction.toShort())
        vm.dataRegisters[registerIndex] = dataRegisterValue
        vm.addressRegister = addressRegisterValue
        interpreter.fetchAndExecuteInstruction()

        assertEquals((dataRegisterValue / 100).toByte(), vm.memory[vm.addressRegister], "Memory value at position [${vm.addressRegister}] mismatch")
        assertEquals(((dataRegisterValue % 100) / 10).toByte(), vm.memory[vm.addressRegister + 1], "Memory value at position [${vm.addressRegister + 1}] mismatch")
        assertEquals(((dataRegisterValue % 100) % 10).toByte(), vm.memory[vm.addressRegister + 2], "Memory value at position [${vm.addressRegister + 2}] mismatch")
    }

    @Test
    fun `fetch and execute instruction 0xF - type 0x55`() {
        val instruction = 0xF655
        val dataRegisterIndex = instruction.and(0x0F00).shr(8)
        val baseDataRegisterValue = 10
        val addressRegisterValue = 50

        vm.load(instruction.toShort())
        vm.addressRegister = addressRegisterValue
        vm.dataRegisters.forEachIndexed { i, _ -> vm.dataRegisters[i] = baseDataRegisterValue + i }
        interpreter.fetchAndExecuteInstruction()

        (0..dataRegisterIndex).forEach { i ->
            assertEquals(vm.dataRegisters[i].toByte(), vm.memory[vm.addressRegister + i], "Incorrect memory value")
        }
    }

    @Test
    fun `fetch and execute instruction 0xF - type 0x65`() {
        val instruction = 0xF765
        val dataRegisterIndex = instruction.and(0x0F00).shr(8)
        val baseDataRegisterValue = 20
        val addressRegisterValue = 60

        vm.load(instruction.toShort())
        vm.addressRegister = addressRegisterValue
        (0..dataRegisterIndex).forEach { i -> vm.memory.put(vm.addressRegister + i, (baseDataRegisterValue + i).toByte()) }
        interpreter.fetchAndExecuteInstruction()

        (0..dataRegisterIndex).forEachIndexed { i, _ ->
            assertEquals(i + baseDataRegisterValue, vm.dataRegisters[i], "Data register [$i] value mismatch")
        }
    }

    private companion object {
        const val MOCKED_RANDOM_VALUE = 50
    }

}