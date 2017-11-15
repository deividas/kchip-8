package com.github.deividasp.kchip8.extension

import com.github.deividasp.kchip8.emulator.Input
import com.github.deividasp.kchip8.emulator.Input.Companion.KEY_HEX

fun Input.setPressedKey(hex: Int) {

    KEY_HEX.forEach { k, v ->
        if (v == hex) {
            setPressedKey(k)
            return@forEach
        }
    }

}