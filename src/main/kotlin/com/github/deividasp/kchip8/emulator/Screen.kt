package com.github.deividasp.kchip8.emulator

/**
 * @author Deividas Popelskis <deividas.popelskis@gmail.com>
 */
class Screen {

    private val pixels = BooleanArray(SCREEN_WIDTH * SCREEN_HEIGHT)

    fun clear() {
        pixels.forEachIndexed { i, _ -> pixels[i] = false }
    }

    fun setPixelActive(x: Int, y: Int, active: Boolean) {
        pixels[x + y * SCREEN_WIDTH] = active
    }

    fun isPixelActive(x: Int, y: Int) = pixels[x + y * SCREEN_WIDTH]

    companion object {
        const val SPRITE_WIDTH = 8
        const val SCREEN_WIDTH = 64
        const val SCREEN_HEIGHT = 32
    }

}