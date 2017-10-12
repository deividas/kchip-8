package com.github.deividasp.kchip8.interpreter

import com.github.deividasp.kchip8.emulator.VirtualMachine

/**
 * @author Deividas Popelskis <deividas.popelskis@gmail.com>
 */
class Interpreter(private val virtualMachine: VirtualMachine) {

    fun fetchAndExecuteInstruction() {
        val instruction = virtualMachine.memory.short.toInt()

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
        }
    }

}