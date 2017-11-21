package com.github.deividasp.kchip8.extension

import com.github.deividasp.kchip8.vm.Input
import com.github.deividasp.kchip8.vm.Input.Companion.KEY_HEX

fun Input.setKeyPressed(hex: Int, pressed: Boolean) {

    KEY_HEX.forEach { k, v ->
        if (v == hex) {
            setKeyPressed(k, pressed)
            return@forEach
        }
    }

}