//src/MainApp.scala

import scala.swing._
import scala.swing.event._
import java.awt.{Color, Font, Graphics2D, RenderingHints, Image}
import javax.swing.{Timer, ImageIcon}
import grammar.Grammar
import automaton.Automaton

case class Message(text: String, isVisible: Boolean, timestamp: Long, image: Option[String] = None)
case class AppState(
  automaton: Automaton,
  keyMessage: Option[Message] = None,
  comboMessage: Option[Message] = None,
  debugEnabled: Boolean = false
)

sealed trait Action
case class KeyPressed(key: String, action: String, time: Long) extends Action
case class ShowCombo(combo: String, time: Long, image: Option[String] = None) extends Action
case class HideMessage(messageType: String, time: Long) extends Action
case object ToggleDebug extends Action

class DebugPanel extends Panel {
  preferredSize = new Dimension(200, 30)
  var isDebugEnabled = false

  override def paintComponent(g: Graphics2D): Unit = {
    super.paintComponent(g)
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.setFont(new Font("Arial", Font.BOLD, 14))
    g.setColor(Color.WHITE)
    g.drawString(s"Debug Mode (?): ${if (isDebugEnabled) "ON" else "OFF"}", 10, 20)
  }

  def updateDebugState(enabled: Boolean): Unit = {
    isDebugEnabled = enabled
    repaint()
  }
}

class MessageDisplay(fontSize: Int) extends Panel {
  preferredSize = new Dimension(1024, 100)
  opaque = false
  
  private var message: Option[Message] = None
  private var comboImages: Map[String, Image] = Map()
  
  def loadImage(path: String): Unit = {
  println(s"Intentando cargar imagen: $path")
  try {
    val file = new java.io.File(path)
    println(s"¿Existe el archivo?: ${file.exists()}")
    println(s"Ruta absoluta: ${file.getAbsolutePath}")
    
    val icon = new ImageIcon(path)
    println(s"Tamaño de la imagen: ${icon.getIconWidth}x${icon.getIconHeight}")
    comboImages += (path -> icon.getImage)
    println(s"Imagen cargada exitosamente: $path")
  } catch {
    case e: Exception => 
      println(s"Error detallado al cargar imagen: $path")
      println(s"Tipo de error: ${e.getClass.getName}")
      println(s"Mensaje de error: ${e.getMessage}")
      e.printStackTrace()
  }
}

  def update(newMessage: Option[Message]): Unit = {
    message = newMessage
    repaint()
  }

  override def paintComponent(g: Graphics2D): Unit = {
    super.paintComponent(g)
    message.filter(_.isVisible).foreach { msg =>
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      
      // Draw image if available
      msg.image.flatMap(comboImages.get).foreach { img =>
        val x = (size.width - img.getWidth(null)) / 2
        val y = (size.height - img.getHeight(null)) / 2
        g.drawImage(img, x, y, null)
      }
      
      // Draw text
      g.setFont(new Font("Arial", Font.BOLD, fontSize))
      val metrics = g.getFontMetrics
      val x = (size.width - metrics.stringWidth(msg.text)) / 2
      val y = size.height / 2

      g.setColor(new Color(0, 0, 0, 128))
      g.drawString(msg.text, x + 2, y + 2)
      
      g.setColor(Color.YELLOW)
      g.drawString(msg.text, x, y)
    }
  }
}

class MainApp(grammar: Grammar, initialAutomaton: Automaton) extends SimpleSwingApplication {
  println(s"Directorio de trabajo actual: ${System.getProperty("user.dir")}")

  def update(state: AppState, action: Action): AppState = action match {
    case KeyPressed(key, action, time) =>
      val keyMessage = Some(Message(s"$key -> $action", true, time))
      val (newAutomaton, comboMsg) = state.automaton.transition(action, time)
      val comboMessage = comboMsg.map { msg =>
        // Extraer el nombre del combo de msg (que probablemente es algo como "COMBO! HADOKEN")
        val comboName = msg.split("!")(1).trim
        println(s"Detectado combo: $comboName")
        // Buscar la imagen correspondiente
        val imagePath = comboImages.get(comboName)
        println(s"Ruta de imagen encontrada: $imagePath")
        Message(msg, true, time, imagePath)
      }
      state.copy(
        automaton = newAutomaton,
        keyMessage = keyMessage,
        comboMessage = comboMessage.orElse(state.comboMessage)
      )
      
    case ShowCombo(combo, time, image) =>
      state.copy(comboMessage = Some(Message(combo, true, time, image)))
      
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
      state.copy(
        automaton = newAutomaton,
        debugEnabled = !state.debugEnabled
      )
  }

  private var currentState = AppState(initialAutomaton)
  private val keyToAction = grammar.mappings.map(m => m.key -> m.action).toMap
  
  // Mapa de combos a imágenes
  private val comboImages = Map(
    "HADOUUUUKEN" -> "images/Hadouken.png",
    "SHORYU" -> "images/Shoryu.png",
    "TATSUMAKI" -> "images/Tatsumaki.png",
    "SHORYUREPPA" -> "images/Shoryureppa.png",
    "OSOTO MAWASHIGERI" -> "images/Osotomawashigeri.png",
    "RUNNING PUNCH" -> "images/Runningpunch.png"
    // Añade más combos e imágenes según necesites
  )
  
  def top = new MainFrame {
    title = "Test Keyboard Events"
    preferredSize = new Dimension(1024, 768)

    private val debugPanel = new DebugPanel
    private val keyDisplay = new MessageDisplay(24)
    private val comboDisplay = new MessageDisplay(32)
    
    // Cargar imágenes
    comboImages.values.foreach(keyDisplay.loadImage)
    comboImages.values.foreach(comboDisplay.loadImage)
    
    private def dispatch(action: Action): Unit = {
      currentState = update(currentState, action)
      keyDisplay.update(currentState.keyMessage)
      comboDisplay.update(currentState.comboMessage)
      debugPanel.updateDebugState(currentState.debugEnabled)
    }
    
    private val mainPanel = new BoxPanel(Orientation.Vertical) {
      preferredSize = new Dimension(1024, 768)
      background = Color.BLACK
      
      contents += new BorderPanel {
        layout(debugPanel) = BorderPanel.Position.East
      }
      contents += new Panel { preferredSize = new Dimension(1024, 200) }
      contents += keyDisplay
      contents += Swing.VStrut(200)
      contents += comboDisplay
      contents += new Panel { preferredSize = new Dimension(1024, 200) }
      
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
            val comboTimer = new Timer(1500, _ => {
              dispatch(HideMessage("combo", time + 1500))
            })
            comboTimer.setRepeats(false)
            comboTimer.start()
          }
        }
    }
  }
}