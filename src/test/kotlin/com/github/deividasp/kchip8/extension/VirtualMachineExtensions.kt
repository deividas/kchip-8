package com.github.deividasp.kchip8.extension

import com.github.deividasp.kchip8.vm.VirtualMachine

fun VirtualMachine.load(vararg instructions: Short) {
    val data = arrayListOf<Byte>()

    instructions.forEach {
        val firstByte = it.toInt().shr(8).toByte()
        val secondByte = it.toInt().and(0x00FF).toByte()

        data.add(firstByte)
        data.add(secondByte)
    }

    loadProgram(data.toByteArray())
}