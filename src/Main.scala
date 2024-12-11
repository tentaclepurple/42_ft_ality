import grammar.{Parser, Printer}
import automaton.AutomatonBuilder

object Main {
  def main(args: Array[String]): Unit = {
    if (args.length < 1) {
      println("Usage: program <grammar-file>")
      sys.exit(1)
    }

    Parser.parseFile(args(0)) match {
      case Right(grammar) => 
        println(Printer.formatGrammar(grammar))
        val automaton = AutomatonBuilder.fromGrammar(grammar)
        new MainApp(grammar, automaton).main(args)
      case Left(error) => 
        println(s"Error: $error")
        sys.exit(1)
    }
  }
}