import scala.swing._

object Main extends SimpleSwingApplication {
  def top = new MainFrame {
    title = "Test Swing"
    contents = new FlowPanel {
      contents += new Label("Hello Swing!")
      contents += new Button("Click me!")
    }
    size = new Dimension(200, 100)
    centerOnScreen()
  }
}