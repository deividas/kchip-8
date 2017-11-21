package com.github.deividasp.kchip8.vm

import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Deividas Popelskis <deividas.popelskis@gmail.com>
 */
class Input {

    private var keyPressed = BooleanArray(KEY_HEX.size)

    var lastPressedKey: AtomicInteger = AtomicInteger()

    @Synchronized
    fun reset() {
        keyPressed.forEachIndexed { i, _ -> keyPressed[i] = false }
    }

    @Synchronized
    fun setKeyPressed(key: Char, pressed: Boolean) {
        val hex = KEY_HEX[key] ?: return
        keyPressed[hex] = pressed

        if (pressed)
            lastPressedKey.set(hex)
    }

    @Synchronized
    fun isKeyPressed(hex: Int): Boolean {
        return keyPressed[hex]
    }

    companion object {
        val KEY_HEX = mapOf('1' to 0x1, '2' to 0x2, '3' to 0x3, '4' to 0xC,
                'Q' to 0x4, 'W' to 0x5, 'E' to 0x6, 'R' to 0xD,
                'A' to 0x7, 'S' to 0x8, 'D' to 0x9, 'F' to 0xE,
                'Z' to 0xA, 'X' to 0x0, 'C' to 0xB, 'V' to 0xF)
    }

}