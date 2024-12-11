import scala.swing._
import scala.swing.event._
import java.awt.Color
import grammar.Parser


object Main extends SimpleSwingApplication {
  def top = new MainFrame {
    title = "Test Keyboard Events"
    
    // Label para mostrar la última tecla presionada
    val keyLabel = new Label("Presiona una tecla...") {
      preferredSize = new Dimension(280, 30)
      background = Color.WHITE
      opaque = true
    }
    
    // TextArea para mostrar el historial de teclas
    val historyArea = new TextArea {
      rows = 10
      columns = 30
      lineWrap = true
      wordWrap = true
      editable = false
      background = Color.WHITE
      foreground = Color.BLACK
      text = "El historial aparecerá aquí\n"
      preferredSize = new Dimension(280, 150)
    }
    
    // Panel que capturará los eventos de teclado
    val mainPanel = new Panel {
      focusable = true
      
      val boxPanel = new BoxPanel(Orientation.Vertical) {
        contents += keyLabel
        contents += Swing.VStrut(10)
        contents += new Label("Historial de teclas:")
        contents += new ScrollPane(historyArea) {
          preferredSize = new Dimension(280, 150)
        }
        border = Swing.EmptyBorder(10)
      }
      
      peer.add(boxPanel.peer)
      requestFocus()
    }
    
    // Establecer el panel principal
    contents = mainPanel
    
    // Escuchar eventos de teclado del panel principal
    listenTo(mainPanel.keys)
    
    reactions += {
      case KeyTyped(_, c, mod, _) =>
        val text = s"Tecla presionada: $c"
        keyLabel.text = text
        // Actualizar el historial añadiendo al principio
        historyArea.text = text + "\n" + historyArea.text
        // Asegurar que el scroll está al principio
        historyArea.peer.setCaretPosition(0)
        println(s"Debug: $text")  // Debug en consola
    }
    
    // Configuración de la ventana
    size = new Dimension(500, 400)
    centerOnScreen()
    
    // Asegurar que el panel tiene el foco
    mainPanel.requestFocus()
  }
}