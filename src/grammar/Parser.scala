package grammar

object Parser {
  def parseFile(path: String): Either[String, Grammar] = {
    try {
      val source = scala.io.Source.fromFile(path)
      val lines = source.getLines().toList
      source.close()

      val parsedLines = lines
        .map(_.trim)
        .filter(line => line.nonEmpty && !line.startsWith("#"))
        .flatMap(parseLine)

      val (mappings, combos) = parsedLines.foldLeft((List[KeyMapping](), List[Combo]())) {
        case ((maps, combs), line) => line match {
          case m: KeyMapping => (m :: maps, combs)
          case c: Combo => (maps, c :: combs)
        }
      }

      Right(Grammar(mappings.reverse, combos.reverse))
    } catch {
      case e: Exception => Left(s"Error parsing file: ${e.getMessage}")
    }
  }

  private def parseLine(line: String): Option[GrammarLine] = {
    line match {
      case l if l.contains("->") && l.startsWith("'") =>
        parseKeyMapping(l)
      case l if l.contains("->") =>
        parseCombo(l)
      case _ => None
    }
  }

  private def parseKeyMapping(line: String): Option[KeyMapping] = {
    // '6' -> Right
    val pattern = "'(.+)'\\s*->\\s*(.+)".r
    line match {
      case pattern(key, action) => 
        Some(KeyMapping(key, action.trim))
      case _ => None
    }
  }

  private def parseCombo(line: String): Option[Combo] = {
    // Right, Right, Punch -> "RUNNING PUNCH"
    val pattern = "(.+)\\s*->\\s*\"(.+)\"".r
    line match {
      case pattern(sequence, name) =>
        val moves = sequence.split(",").map(_.trim).toList
        Some(Combo(moves, name.trim))
      case _ => None
    }
  }
}