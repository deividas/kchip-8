package com.github.deividasp.kchip8.interpreter

import com.github.deividasp.kchip8.emulator.VirtualMachine

/**
 * @author Deividas Popelskis <deividas.popelskis@gmail.com>
 */
class Interpreter(private val virtualMachine: VirtualMachine) {

    fun fetchAndExecuteInstruction() {
        val instruction = virtualMachine.memory.short.toInt().and(0xFFFF)

        val opcode = instruction.shr(12)
        val operand = instruction.and(0x0FFF)

        execute(opcode, operand)
    }

    private fun execute(opcode: Int, operand: Int) {
        when (opcode) {
            0x1 -> { // Memory position jump
                virtualMachine.memory.position(operand)
            }

            0x3 -> { // Instruction skip (if data register == value)
                val index = operand.and(0x0F00).shr(8)
                val value = operand.and(0x00FF)

                if (virtualMachine.dataRegisters[index] == value)
                    virtualMachine.memory.short // skip next instruction
            }

            0x4 -> { // Instruction skip (if data register != value)
                val index = operand.and(0x0F00).shr(8)
                val value = operand.and(0x00FF)

                if (virtualMachine.dataRegisters[index] != value)
                    virtualMachine.memory.short // skip next instruction
            }

            0x5 -> { // Instruction skip (if [data register x] == [data register y])
                val index1 = operand.and(0x0F00).shr(8)
                val index2 = operand.and(0x00F0).shr(4)

                if (virtualMachine.dataRegisters[index1] == virtualMachine.dataRegisters[index2])
                    virtualMachine.memory.short // skip next instruction
            }

            0x6 -> { // Data register value assign
                val index = operand.and(0x0F00).shr(8)
                val value = operand.and(0x00FF)

                virtualMachine.dataRegisters[index] = value
            }

            0x7 -> { // Data register value addition
                val index = operand.and(0x0F00).shr(8)
                val value = operand.and(0x00FF)

                virtualMachine.dataRegisters[index] += value
            }

            0x8 -> { // Operations with two data registers
                val type = operand.and(0x000F)
                val targetRegisterIndex = operand.and(0x0F00).shr(8)
                val sourceRegisterIndex = operand.and(0x00F0).shr(4)
                val targetRegister = virtualMachine.dataRegisters[targetRegisterIndex]
                val sourceRegister = virtualMachine.dataRegisters[sourceRegisterIndex]

                when (type) {
                    0x0 -> { // Value assign
                        virtualMachine.dataRegisters[targetRegisterIndex] = sourceRegister
                    }

                    0x1 -> { // Bitwise OR
                        virtualMachine.dataRegisters[targetRegisterIndex] = targetRegister.or(sourceRegister)
                    }

                    0x2 -> { // Bitwise AND
                        virtualMachine.dataRegisters[targetRegisterIndex] = targetRegister.and(sourceRegister)
                    }

                    0x3 -> { // Bitwise XOR
                        virtualMachine.dataRegisters[targetRegisterIndex] = targetRegister.xor(sourceRegister)
                    }

                    0x4 -> { // Value addition and carry flag assign
                        val value = targetRegister + sourceRegister

                        if (value > 0xFF) {
                            virtualMachine.dataRegisters[targetRegisterIndex] = value.and(0xFF)
                            virtualMachine.dataRegisters[0xF] = 1
                        } else {
                            virtualMachine.dataRegisters[targetRegisterIndex] = value
                            virtualMachine.dataRegisters[0xF] = 0
                        }
                    }

                    0x5 -> { // Value subtraction and borrow flag assign (target data register -= source data register)
                        val value = targetRegister - sourceRegister

                        if (targetRegister > sourceRegister) {
                            virtualMachine.dataRegisters[targetRegisterIndex] = value
                            virtualMachine.dataRegisters[0xF] = 1
                        } else {
                            virtualMachine.dataRegisters[targetRegisterIndex] = value.and(0xFF)
                            virtualMachine.dataRegisters[0xF] = 0
                        }
                    }

                    0x6 -> { // Right bit shift by 1 and carry flag assign
                        virtualMachine.dataRegisters[0xF] = sourceRegister.and(0x1)
                        virtualMachine.dataRegisters[targetRegisterIndex] = sourceRegister.shr(1)
                    }

                    0x7 -> { // Value and borrow flag assign (target data register = source data register - target data register)
                        val value = sourceRegister - targetRegister

                        if (sourceRegister > targetRegister) {
                            virtualMachine.dataRegisters[targetRegisterIndex] = value
                            virtualMachine.dataRegisters[0xF] = 1
                        } else {
                            virtualMachine.dataRegisters[targetRegisterIndex] = value.and(0xFF)
                            virtualMachine.dataRegisters[0xF] = 0
                        }
                    }

                    0xE -> { // Left bit shift by 1 and carry flag assign
                        virtualMachine.dataRegisters[0xF] = sourceRegister.and(0x80).shr(7)
                        virtualMachine.dataRegisters[targetRegisterIndex] = sourceRegister.shl(1)
                    }
                }
            }

            0x9 -> { // Instruction skip (if [data register x] != [data register y])
                val index1 = operand.and(0x0F00).shr(8)
                val index2 = operand.and(0x00F0).shr(4)

                if (virtualMachine.dataRegisters[index1] != virtualMachine.dataRegisters[index2])
                    virtualMachine.memory.short // skip next instruction
            }
        }
    }

}