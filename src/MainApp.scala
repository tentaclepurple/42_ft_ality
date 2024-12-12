//src/MainApp.scala

import scala.swing._
import scala.swing.event._
import java.awt.{Color, Font, Graphics2D, RenderingHints}
import javax.swing.Timer
import grammar.Grammar
import automaton.Automaton

// Modelo inmutable
case class Message(text: String, isVisible: Boolean, timestamp: Long)
case class AppState(
  automaton: Automaton,
  keyMessage: Option[Message] = None,
  comboMessage: Option[Message] = None
)

// Acciones que pueden modificar el estado
sealed trait Action
case class KeyPressed(key: String, action: String, time: Long) extends Action
case class ShowCombo(combo: String, time: Long) extends Action
case class HideMessage(messageType: String, time: Long) extends Action
case object ToggleDebug extends Action

// Vista para mensajes
class MessageDisplay(fontSize: Int) extends Panel {
  preferredSize = new Dimension(800, 100)
  opaque = false

  private var message: Option[Message] = None
  def update(newMessage: Option[Message]): Unit = {
    message = newMessage
    repaint()
  }

  override def paintComponent(g: Graphics2D): Unit = {
    super.paintComponent(g)
    message.filter(_.isVisible).foreach { msg =>
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      g.setFont(new Font("Arial", Font.BOLD, fontSize))
      
      val metrics = g.getFontMetrics
      val x = (size.width - metrics.stringWidth(msg.text)) / 2
      val y = size.height / 2

      // Sombra
      g.setColor(new Color(0, 0, 0, 128))
      g.drawString(msg.text, x + 2, y + 2)
      
      // Texto principal
      g.setColor(Color.YELLOW)
      g.drawString(msg.text, x, y)
    }
  }
}

class MainApp(grammar: Grammar, initialAutomaton: Automaton) extends SimpleSwingApplication {
  // Función pura que actualiza el estado
  def update(state: AppState, action: Action): AppState = action match {
    case KeyPressed(key, action, time) =>
      val keyMessage = Some(Message(s"$key -> $action", true, time))
      val (newAutomaton, comboMsg) = state.automaton.transition(action, time)
      val comboMessage = comboMsg.map(msg => Message(msg, true, time))
      state.copy(
        automaton = newAutomaton,
        keyMessage = keyMessage,
        comboMessage = comboMessage.orElse(state.comboMessage)
      )
      
    case ShowCombo(combo, time) =>
      state.copy(comboMessage = Some(Message(combo, true, time)))
      
    case HideMessage(msgType, _) =>
      msgType match {
        case "key" => state.copy(keyMessage = None)
        case "combo" => state.copy(comboMessage = None)
        case _ => state
      }
      
    case ToggleDebug =>
      val newAutomaton = if (state.automaton.debugState.isEnabled) {
        println("\nDebug mode OFF")
        state.automaton.disableDebug
      } else {
        println("\nDebug mode ON")
        state.automaton.enableDebug
      }
      state.copy(automaton = newAutomaton)
  }

  // Estado inicial
  private var currentState = AppState(initialAutomaton)
  private val keyToAction = grammar.mappings.map(m => m.key -> m.action).toMap
  
  def top = new MainFrame {
    title = "Test Keyboard Events"

    private val keyDisplay = new MessageDisplay(24)
    private val comboDisplay = new MessageDisplay(32)
    
    // Dispatcher para las acciones
    private def dispatch(action: Action): Unit = {
      currentState = update(currentState, action)
      keyDisplay.update(currentState.keyMessage)
      comboDisplay.update(currentState.comboMessage)
    }
    
    private val mainPanel = new BoxPanel(Orientation.Vertical) {
      preferredSize = new Dimension(800, 600)
      background = Color.BLACK
      contents += new Panel { preferredSize = new Dimension(800, 200) }
      contents += keyDisplay
      contents += Swing.VStrut(200)
      contents += comboDisplay
      contents += new Panel { preferredSize = new Dimension(800, 200) }
      focusable = true
      requestFocus()
    }
    
    contents = mainPanel
    listenTo(mainPanel.keys)
    
    reactions += {
  case KeyTyped(_, c, mod, _) =>
    if (c == '?') {
      dispatch(ToggleDebug)
    } else {
      val keyStr = c.toString
      keyToAction.get(keyStr).foreach { action =>
        val time = System.currentTimeMillis()
        dispatch(KeyPressed(keyStr, action, time))
        
        // Timer para la desaparición del mensaje de tecla
        val keyTimer = new Timer(1000, _ => {
          dispatch(HideMessage("key", time + 1000))
        })
        keyTimer.setRepeats(false)
        keyTimer.start()

        // Timer para la desaparición del mensaje de combo
        val comboTimer = new Timer(1500, _ => {  // Un poco más largo para el combo
          dispatch(HideMessage("combo", time + 1500))
        })
        comboTimer.setRepeats(false)
        comboTimer.start()
      }
    }
  }
}
}