// src/MainApp.scala

import scala.swing._
import scala.swing.event._
import java.awt.{Color, Font, Graphics2D, RenderingHints, Image}
import javax.swing.{Timer, ImageIcon}
import grammar.Grammar
import automaton.Automaton
import javax.imageio.ImageIO

case class Message(text: String, isVisible: Boolean, timestamp: Long, duration: Long, image: Option[String] = None)
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
  background = Color.BLACK
  opaque = true

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

class MessageDisplay(fontSize: Int, isCombo: Boolean = false) extends Panel {
  background = Color.BLACK
  opaque = true
  
  private var message: Option[Message] = None
  private var comboImages: Map[String, Image] = Map()
  
  def loadImage(path: String): Unit = {
    try {
      val file = new java.io.File(path)
      if (!file.exists()) {
        println(s"File not found: $path")
        return
      }
      
      val icon = new ImageIcon(path)
      if (icon.getIconWidth <= 0 || icon.getIconHeight <= 0) {
        println(s"Image not load: $path")
        return
      }
      
      comboImages += (path -> icon.getImage)
    } catch {
      case e: Exception => 
        println(s"Image error: $path - ${e.getMessage}")
        e.printStackTrace()
    }
  }

  def update(newMessage: Option[Message]): Unit = {
    message = newMessage
    repaint()
  }

  override def paintComponent(g: Graphics2D): Unit = {
    super.paintComponent(g)
    
    g.setColor(Color.BLACK)
    g.fillRect(0, 0, size.width, size.height)
    
    message.filter(_.isVisible).foreach { msg =>
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      
      if (isCombo) {
        // Combo panels have an image and text 
        msg.image.flatMap(comboImages.get).foreach { img =>
          val icon = new ImageIcon(img)
          val x = (size.width - icon.getIconWidth) / 2
          val y = (size.height - icon.getIconHeight) / 3 
          icon.paintIcon(peer, g, x, y)
        }
        
        // Text centered in the lower third
        g.setFont(new Font("Arial", Font.BOLD, fontSize))
        val metrics = g.getFontMetrics
        val x = (size.width - metrics.stringWidth(msg.text)) / 2
        val y = (size.height * 3) / 4 

        g.setColor(new Color(0, 0, 0, 128))
        g.drawString(msg.text, x + 2, y + 2)
        
        g.setColor(Color.YELLOW)
        g.drawString(msg.text, x, y)
      } else {
        // For key messages, text is centered
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
}

class MainApp(grammar: Grammar, initialAutomaton: Automaton) extends SimpleSwingApplication {
  def update(state: AppState, action: Action): AppState = action match {
    case KeyPressed(key, action, time) =>
      val currentTime = System.currentTimeMillis()
      val keyMessage = Some(Message(s"$action", true, currentTime, 1500))
      val (newAutomaton, comboMsg) = state.automaton.transition(action, time)
      val comboMessage = comboMsg.map { msg =>
        val comboName = msg.split("!")(1).trim
        val imagePath = comboImages.get(comboName)
        // Keep the combo message if it's still active
        state.comboMessage match {
          case Some(existing) if currentTime - existing.timestamp < existing.duration =>
            existing
          case _ =>
            Message(msg, true, currentTime, getGifDuration(imagePath.getOrElse("")), imagePath)
        }
      }
      state.copy(
        automaton = newAutomaton,
        keyMessage = keyMessage,
        comboMessage = if (comboMessage.isDefined) comboMessage else state.comboMessage
      )
      
    case ShowCombo(combo, time, image) =>
      val currentTime = System.currentTimeMillis()
      // Only show the combo message if it's not already displayed
      state.comboMessage match {
        case Some(existing) if currentTime - existing.timestamp < existing.duration =>
          state
        case _ =>
          state.copy(comboMessage = Some(Message(combo, true, time, getGifDuration(image.getOrElse("")), image)))
      }
      
    case HideMessage(msgType, time) =>
      msgType match {
        case "key" => state.copy(keyMessage = None)
        case "combo" =>
          // Only hide the combo message if it's still active
          state.comboMessage match {
            case Some(msg) if System.currentTimeMillis() - msg.timestamp >= msg.duration =>
              state.copy(comboMessage = None)
            case _ => state
          }
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
  
  private val comboImages = Map(
    "RUNNING PUNCH" -> "images/Runningpunch.png",
    "HADOUUUUKEN" -> "images/Hadouken.gif",
    "OSOTO MAWASHIGERI" -> "images/Osoto.gif",
    "TATSUMAKI" -> "images/Tatsumaki.gif",
    "SHORYU" -> "images/Shoryu.gif",
    "SHORYUREPPA" -> "images/Shoryureppa.gif",
  )

  def getGifDuration(path: String): Long = {
    try {
        val inputStream = ImageIO.createImageInputStream(new java.io.File(path))
        val readers = ImageIO.getImageReadersByFormatName("gif")
        if (readers.hasNext) {
            val reader = readers.next()
            reader.setInput(inputStream)
            val numFrames = reader.getNumImages(true)
            var duration = 0L

            for (i <- 0 until numFrames) {
                val imgMeta = reader.getImageMetadata(i)
                val node = imgMeta.getAsTree("javax_imageio_gif_image_1.0")
                val children = node.getChildNodes
                for (j <- 0 until children.getLength) {
                    val child = children.item(j)
                    if (child.getNodeName == "GraphicControlExtension") {
                        val delay = child.getAttributes.getNamedItem("delayTime").getNodeValue.toInt
                        duration += delay * 5
                    }
                }
            }
            duration
        } else {
            3000L 
        }
    } catch {
        case _: Exception => 3000L
    }
  }

  def top = new MainFrame {
    title = "Test Keyboard Events"
    preferredSize = new Dimension(800, 700)
    background = Color.BLACK

    private val debugPanel = new DebugPanel
    private val keyDisplay = new MessageDisplay(24)
    private val comboDisplay = new MessageDisplay(32, true)
    
    // Set sizes
    keyDisplay.preferredSize = new Dimension(800, 100)
    comboDisplay.preferredSize = new Dimension(800, 500)
    
    // Load images
    comboImages.values.foreach(comboDisplay.loadImage)
    
    private def dispatch(action: Action): Unit = {
      currentState = update(currentState, action)
      keyDisplay.update(currentState.keyMessage)
      comboDisplay.update(currentState.comboMessage)
      debugPanel.updateDebugState(currentState.debugEnabled)
    }
    
    private val mainPanel = new BoxPanel(Orientation.Vertical) {
      background = Color.BLACK
      preferredSize = new Dimension(800, 700)
      
      contents += new BorderPanel {
        background = Color.BLACK
        layout(debugPanel) = BorderPanel.Position.East
      }
      
      contents += keyDisplay
      contents += Swing.VStrut(20)
      contents += comboDisplay
      
      focusable = true
      requestFocus()
    }
    
    contents = mainPanel
    listenTo(mainPanel.keys)
    peer.setBackground(Color.BLACK)
    
    reactions += {
      case KeyTyped(_, c, mod, _) =>
        if (c == '?') {
          dispatch(ToggleDebug)
        } else {
          val keyStr = c.toString
          keyToAction.get(keyStr).foreach { action =>
            val currentTime = System.currentTimeMillis()
            dispatch(KeyPressed(keyStr, action, currentTime))
            
            // Timer for key message
            val keyTimer = new Timer(1500, _ => {
              dispatch(HideMessage("key", currentTime + 1500))
            })
            keyTimer.setRepeats(false)
            keyTimer.start()

            // Timer for combo message
            val comboTimer = new Timer(3000, _ => {
              // Verify if the combo message is still active
              currentState.comboMessage.foreach { msg =>
                if (System.currentTimeMillis() - msg.timestamp >= msg.duration) {
                  dispatch(HideMessage("combo", currentTime + 3000))
                }
              }
            })
            comboTimer.setRepeats(false)
            comboTimer.start()
          }
        }
    }
  }
}