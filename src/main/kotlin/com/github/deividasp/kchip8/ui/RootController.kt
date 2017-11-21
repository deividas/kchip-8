package com.github.deividasp.kchip8.ui

import com.github.deividasp.kchip8.vm.Input
import com.github.deividasp.kchip8.vm.Screen
import com.github.deividasp.kchip8.vm.VirtualMachine
import javafx.animation.AnimationTimer
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Alert
import javafx.scene.input.KeyEvent
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import javafx.stage.Stage

/**
 * @author Deividas Popelskis <deividas.popelskis@gmail.com>
 */
class RootController {

    private val virtualMachine = VirtualMachine()

    @FXML
    private lateinit var root: VBox

    @FXML
    private lateinit var canvas: Canvas

    @FXML
    private fun initialize() {
        object : AnimationTimer() {

            override fun handle(currentTime: Long) {
                repaintCanvas(getGraphicsContext())
            }

        }.start()
    }

    @FXML
    private fun loadRom(event: ActionEvent) {
        val fileChooser = FileChooser()
        fileChooser.title = "Select ROM"

        val selectedFile = fileChooser.showOpenDialog(getStage())

        selectedFile?.let {
            try {
                virtualMachine.loadProgram(it.absolutePath)
                virtualMachine.startExecution()
                getStage().title = "${Emulator.TITLE} - [${it.nameWithoutExtension}]"
            } catch (e: Exception) {
                showError(e.message!!)
            }
        }
    }

    @FXML
    private fun onKeyPressed(event: KeyEvent) = setKeyPressed(event, true)

    @FXML
    private fun onKeyReleased(event: KeyEvent) = setKeyPressed(event, false)

    private fun setKeyPressed(event: KeyEvent, pressed: Boolean) {
        if (!event.code.isLetterKey && !event.code.isKeypadKey)
            return

        val character = event.text.toCharArray()[0].toUpperCase()

        if (Input.KEY_HEX[character] != null)
            virtualMachine.input.setKeyPressed(character, pressed)
    }

    private fun repaintCanvas(context: GraphicsContext) {
        context.fill = Color.BLACK
        context.fillRect(0.0, 0.0, Emulator.WIDTH, Emulator.HEIGHT)
        context.fill = Color.WHITE

        (0 until Screen.WIDTH).forEach { x ->
            (0 until Screen.HEIGHT).forEach { y ->
                if (virtualMachine.screen.isPixelActive(x, y))
                    context.fillRect(x * Emulator.SCALE, y * Emulator.SCALE, Emulator.SCALE, Emulator.SCALE)
            }
        }
    }

    private fun showError(message: String) {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.headerText = "Error"
        alert.contentText = message
        alert.show()
    }

    private fun getStage() = root.scene.window as Stage
    private fun getGraphicsContext() = canvas.graphicsContext2D

}