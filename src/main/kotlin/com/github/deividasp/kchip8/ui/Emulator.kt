package com.github.deividasp.kchip8.ui

import com.github.deividasp.kchip8.vm.Screen
import javafx.application.Application
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

/**
 * @author Deividas Popelskis <deividas.popelskis@gmail.com>
 */
class Emulator : Application() {

    override fun start(stage: Stage) {
        val root = FXMLLoader.load<Parent>(this::class.java.javaClass.getResource(FXML_PATH))

        stage.title = TITLE
        stage.scene = Scene(root)
        stage.onCloseRequest = EventHandler { System.exit(0) }

        stage.show()
    }

    companion object {
        const val TITLE = "KChip-8"
        const val FXML_PATH = "/fxml/Emulator.fxml"

        const val SCALE = 10.0
        const val WIDTH = Screen.WIDTH * SCALE
        const val HEIGHT = Screen.HEIGHT * SCALE
    }

}