package com.github.deividasp.kchip8.emulator

/**
 * @author Deividas Popelskis <deividas.popelskis@gmail.com>
 */
class Input {

    private var pressedKey: Char? = null

    fun reset() {
        pressedKey = null
    }

    fun setPressedKey(key: Char) {
        pressedKey = key
    }

    fun getPressedKeyHex() = KEY_HEX[pressedKey]

    companion object {
        val KEY_HEX = mapOf('1' to 0x1, '2' to 0x2, '3' to 0x3, '4' to 0xC,
                'Q' to 0x4, 'W' to 0x5, 'E' to 0x6, 'R' to 0xD,
                'A' to 0x7, 'S' to 0x8, 'D' to 0x9, 'F' to 0xE,
                'Z' to 0xA, 'X' to 0x0, 'C' to 0xB, 'V' to 0xF)
    }

}