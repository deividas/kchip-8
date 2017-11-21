package com.github.deividasp.kchip8.interpreter

import com.github.deividasp.kchip8.vm.Screen
import com.github.deividasp.kchip8.vm.VirtualMachine
import java.util.*

/**
 * @author Deividas Popelskis <deividas.popelskis@gmail.com>
 */
class Interpreter(private val virtualMachine: VirtualMachine) {

    var random = Random()

    fun fetchAndExecuteInstruction() {
        val instruction = virtualMachine.memory.short.toInt().and(0xFFFF)

        val opcode = instruction.shr(12)
        val operand = instruction.and(0x0FFF)

        executeInstruction(opcode, operand)
    }

    private fun executeInstruction(opcode: Int, operand: Int) {
        when (opcode) {
            0x0 -> {
                val type = operand.and(0x00FF)

                when (type) {
                    0xE0 -> {
                        virtualMachine.screen.reset()
                    }

                    0xEE -> { // Return from a subroutine
                        virtualMachine.memory.position(virtualMachine.stack.pop())
                    }
                }
            }

            0x1 -> {
                virtualMachine.memory.position(operand)
            }

            0x2 -> { // Call a subroutine
                virtualMachine.stack.push(virtualMachine.memory.position())
                virtualMachine.memory.position(operand)
            }

            0x3 -> {
                val index = operand.and(0x0F00).shr(8)
                val value = operand.and(0x00FF)

                if (virtualMachine.dataRegisters[index] == value)
                    virtualMachine.memory.short // skip next instruction
            }

            0x4 -> {
                val index = operand.and(0x0F00).shr(8)
                val value = operand.and(0x00FF)

                if (virtualMachine.dataRegisters[index] != value)
                    virtualMachine.memory.short // skip next instruction
            }

            0x5 -> {
                val index1 = operand.and(0x0F00).shr(8)
                val index2 = operand.and(0x00F0).shr(4)

                if (virtualMachine.dataRegisters[index1] == virtualMachine.dataRegisters[index2])
                    virtualMachine.memory.short // skip next instruction
            }

            0x6 -> {
                val index = operand.and(0x0F00).shr(8)
                val value = operand.and(0x00FF)

                virtualMachine.dataRegisters[index] = value
            }

            0x7 -> {
                val index = operand.and(0x0F00).shr(8)
                val value = operand.and(0x00FF)

                virtualMachine.dataRegisters[index] = (virtualMachine.dataRegisters[index] + value).and(0xFF)
            }

            0x8 -> {
                val type = operand.and(0x000F)
                val targetRegisterIndex = operand.and(0x0F00).shr(8)
                val sourceRegisterIndex = operand.and(0x00F0).shr(4)
                val targetRegister = virtualMachine.dataRegisters[targetRegisterIndex]
                val sourceRegister = virtualMachine.dataRegisters[sourceRegisterIndex]

                when (type) {
                    0x0 -> {
                        virtualMachine.dataRegisters[targetRegisterIndex] = sourceRegister
                    }

                    0x1 -> {
                        virtualMachine.dataRegisters[targetRegisterIndex] = targetRegister.or(sourceRegister)
                    }

                    0x2 -> {
                        virtualMachine.dataRegisters[targetRegisterIndex] = targetRegister.and(sourceRegister)
                    }

                    0x3 -> {
                        virtualMachine.dataRegisters[targetRegisterIndex] = targetRegister.xor(sourceRegister)
                    }

                    0x4 -> {
                        val value = targetRegister + sourceRegister

                        if (value > 0xFF) {
                            virtualMachine.dataRegisters[targetRegisterIndex] = value.and(0xFF)
                            virtualMachine.dataRegisters[0xF] = 1 // carry flag
                        } else {
                            virtualMachine.dataRegisters[targetRegisterIndex] = value
                            virtualMachine.dataRegisters[0xF] = 0 // carry flag
                        }
                    }

                    0x5 -> {
                        val value = targetRegister - sourceRegister

                        if (targetRegister > sourceRegister) {
                            virtualMachine.dataRegisters[targetRegisterIndex] = value
                            virtualMachine.dataRegisters[0xF] = 1 // borrow flag
                        } else {
                            virtualMachine.dataRegisters[targetRegisterIndex] = value.and(0xFF)
                            virtualMachine.dataRegisters[0xF] = 0 // borrow flag
                        }
                    }

                    0x6 -> {
                        virtualMachine.dataRegisters[0xF] = targetRegister.and(0x1)
                        virtualMachine.dataRegisters[targetRegisterIndex] = targetRegister.shr(1)
                    }

                    0x7 -> {
                        val value = sourceRegister - targetRegister

                        if (sourceRegister > targetRegister) {
                            virtualMachine.dataRegisters[targetRegisterIndex] = value
                            virtualMachine.dataRegisters[0xF] = 1 // borrow flag
                        } else {
                            virtualMachine.dataRegisters[targetRegisterIndex] = value.and(0xFF)
                            virtualMachine.dataRegisters[0xF] = 0 // borrow flag
                        }
                    }

                    0xE -> {
                        virtualMachine.dataRegisters[0xF] = targetRegister.and(0x80).shr(7)
                        virtualMachine.dataRegisters[targetRegisterIndex] = targetRegister.shl(1)
                    }
                }
            }

            0x9 -> {
                val index1 = operand.and(0x0F00).shr(8)
                val index2 = operand.and(0x00F0).shr(4)

                if (virtualMachine.dataRegisters[index1] != virtualMachine.dataRegisters[index2])
                    virtualMachine.memory.short // skip next instruction
            }

            0xA -> {
                virtualMachine.addressRegister = operand
            }

            0xB -> {
                virtualMachine.memory.position(operand + virtualMachine.dataRegisters[0])
            }

            0xC -> {
                val index = operand.and(0x0F00).shr(8)
                val value = operand.and(0x00FF)

                virtualMachine.dataRegisters[index] = value.and(random.nextInt(0xFF))
            }

            0xD -> { // Draw a sprite
                val index1 = operand.and(0x0F00).shr(8)
                val index2 = operand.and(0x00F0).shr(4)
                val height = operand.and(0x000F)

                val baseX = virtualMachine.dataRegisters[index1]
                val baseY = virtualMachine.dataRegisters[index2]

                virtualMachine.dataRegisters[0xF] = 0 // collision flag

                (0 until height).forEach { yOffset ->
                    val y = (baseY + yOffset) % Screen.HEIGHT
                    val pixels = virtualMachine.memory[virtualMachine.addressRegister + yOffset].toInt()

                    (0 until Screen.SPRITE_WIDTH).forEach innerLoop@ { xOffset ->
                        val x = (baseX + xOffset) % Screen.WIDTH
                        val pixelActive = pixels.and(0x80.shr(xOffset)) > 0

                        if (!pixelActive)
                            return@innerLoop

                        if (virtualMachine.screen.isPixelActive(x, y))
                            virtualMachine.dataRegisters[0xF] = 1 // collision flag

                        virtualMachine.screen.setPixelActive(x, y, !virtualMachine.screen.isPixelActive(x, y))
                    }
                }
            }

            0xE -> {
                val type = operand.and(0x00FF)
                val registerIndex = operand.and(0x0F00).shr(8)
                val key = virtualMachine.dataRegisters[registerIndex]

                when (type) {
                    0x9E -> {
                        if (virtualMachine.input.isKeyPressed(key))
                            virtualMachine.memory.short // skip next instruction
                    }

                    0xA1 -> {
                        if (!virtualMachine.input.isKeyPressed(key))
                            virtualMachine.memory.short // skip next instruction
                    }
                }
            }

            0xF -> {
                val type = operand.and(0x00FF)
                val registerIndex = operand.and(0x0F00).shr(8)

                when (type) {
                    0x07 -> {
                        virtualMachine.dataRegisters[registerIndex] = virtualMachine.delayTimer
                    }

                    0x0A -> {
                        virtualMachine.input.lastPressedKey.set(0)

                        while (virtualMachine.input.lastPressedKey.get() == 0)
                            Thread.sleep(VirtualMachine.EXECUTION_RATE)

                        virtualMachine.dataRegisters[registerIndex] = virtualMachine.input.lastPressedKey.get()
                    }

                    0x15 -> {
                        virtualMachine.delayTimer = virtualMachine.dataRegisters[registerIndex]
                    }

                    0x18 -> {
                        virtualMachine.soundTimer = virtualMachine.dataRegisters[registerIndex]
                    }

                    0x1E -> {
                        virtualMachine.addressRegister += virtualMachine.dataRegisters[registerIndex]
                    }

                    0x29 -> {
                        virtualMachine.addressRegister = virtualMachine.dataRegisters[registerIndex] * Screen.FONT_CHARACTER_LENGTH
                    }

                    0x33 -> { // Store the binary-coded decimal representation of a data register into the memory
                        val baseValue = virtualMachine.dataRegisters[registerIndex]

                        virtualMachine.memory.put(virtualMachine.addressRegister, (baseValue / 100).toByte())
                        virtualMachine.memory.put(virtualMachine.addressRegister + 1, ((baseValue % 100) / 10).toByte())
                        virtualMachine.memory.put(virtualMachine.addressRegister + 2, ((baseValue % 100) % 10).toByte())
                    }

                    0x55 -> { // Store data registers into the memory
                        (0..registerIndex).forEach { i ->
                            virtualMachine.memory.put(virtualMachine.addressRegister + i, virtualMachine.dataRegisters[i].toByte())
                        }
                    }

                    0x65 -> { // Load data registers from the the memory
                        (0..registerIndex).forEach { i ->
                            virtualMachine.dataRegisters[i] = virtualMachine.memory.get(virtualMachine.addressRegister + i).toInt()
                        }
                    }
                }
            }
        }
    }

}