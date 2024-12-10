import scala.swing._
import scala.swing.event._
import java.awt.Color

object Main extends SimpleSwingApplication {
  def top = new MainFrame {
    title = "Test Keyboard Events"
    
    // Label para mostrar la última tecla presionada
    val keyLabel = new Label("Presiona una tecla...") {
      preferredSize = new Dimension(480, 50)  // Aumentado el alto para mejor visibilidad
      background = Color.WHITE
      opaque = true
      border = Swing.EmptyBorder(10)  // Añadido padding interno
      font = new Font("Arial", 0, 16)  // Fuente más grande
    }
    
    // TextArea para mostrar el historial de teclas
    val historyArea = new TextArea {
      rows = 20
      columns = 40
      lineWrap = true
      wordWrap = true
      editable = false
      background = Color.WHITE
      foreground = Color.BLACK
      text = "El historial aparecerá aquí\n"
      font = new Font("Monospace", 0, 14)  // Fuente monoespaciada para mejor legibilidad
    }
    
    // Panel que capturará los eventos de teclado
    val mainPanel = new Panel {
      focusable = true
      
      val boxPanel = new BoxPanel(Orientation.Vertical) {
        contents += keyLabel
        contents += Swing.VStrut(20)  // Más espacio entre elementos
        contents += new Label("Historial de teclas:") {
          font = new Font("Arial", 0, 14)
        }
        contents += Swing.VStrut(10)
        contents += new ScrollPane(historyArea) {
          preferredSize = new Dimension(480, 400)  // Área de historial más grande
        }
        border = Swing.EmptyBorder(20)  // Más padding en los bordes
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
        historyArea.text = text + "\n" + historyArea.text
        historyArea.peer.setCaretPosition(0)
        println(s"Debug: $text")
    }
    
    // Configuración de la ventana
    size = new Dimension(720, 600)  // Ajustado el ancho para acomodar el padding
    centerOnScreen()
    resizable = false  // Ventana de tamaño fijo
    
    // Asegurar que el panel tiene el foco
    mainPanel.requestFocus()
  }
}