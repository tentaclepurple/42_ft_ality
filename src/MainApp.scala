//src/MainApp.scala

import scala.swing._
import scala.swing.event._
import java.awt.Color
import grammar.Grammar
import automaton.Automaton

class MainApp(grammar: Grammar, initialAutomaton: Automaton) extends SimpleSwingApplication {
  private var automaton = initialAutomaton

  // Crear un mapa de teclas a acciones
  private val keyToAction = grammar.mappings.map(m => m.key -> m.action).toMap

  def top = new MainFrame {
    title = "Test Keyboard Events"
    
    private val keyLabel = new Label("Presiona una tecla...") {
      preferredSize = new Dimension(280, 30)
      background = Color.WHITE
      opaque = true
    }
    
    private val historyArea = new TextArea {
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

    private val comboArea = new TextArea {
      rows = 5
      columns = 30
      lineWrap = true
      wordWrap = true
      editable = false
      background = Color.BLACK
      foreground = Color.YELLOW
      text = "Los combos aparecerán aquí\n"
      preferredSize = new Dimension(280, 100)
    }
    
    private val mainPanel = new Panel {
      preferredSize = new Dimension(900, 750)
      focusable = true
      
      val boxPanel = new BoxPanel(Orientation.Vertical) {
        contents += keyLabel
        contents += Swing.VStrut(10)
        contents += new Label("Historial de teclas:")
        contents += new ScrollPane(historyArea)
        contents += Swing.VStrut(10)
        contents += new Label("Combos reconocidos:")
        contents += new ScrollPane(comboArea)
        border = Swing.EmptyBorder(10)
      }
      
      peer.add(boxPanel.peer)
      requestFocus()
    }
    
    contents = mainPanel
    listenTo(mainPanel.keys)
    
    reactions += {
  case KeyTyped(_, c, mod, _) =>
    if (c == '?') {
      automaton = if (automaton.debugState.isEnabled) {
        println("\nDebug mode OFF")
        automaton.disableDebug
      } else {
        println("\nDebug mode ON")
        automaton.enableDebug
      }
    } else {
      val keyStr = c.toString
      keyToAction.get(keyStr) match {
        case Some(action) =>
          val text = s"Tecla: $keyStr -> Acción: $action"
          keyLabel.text = text
          historyArea.text = text + "\n" + historyArea.text
          historyArea.peer.setCaretPosition(0)
          
          automaton = automaton.transition(action, System.currentTimeMillis())._1
          
          val moves = automaton.getCurrentMoves
          if (moves.nonEmpty) {
            comboArea.text = s"¡COMBO! ${moves.mkString(", ")}\n" + comboArea.text
            comboArea.peer.setCaretPosition(0)
          }

        case None =>
          keyLabel.text = s"Tecla no mapeada: $keyStr"
      }
    }
    }
  }
}