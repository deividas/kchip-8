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
        }
    }

}