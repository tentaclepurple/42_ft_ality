package grammar

object Parser {
  def parseLine(line: String): Option[GrammarLine] = {
    def parseKeyMapping(parts: Array[String]): Option[KeyMapping] = {
      val pattern = """'(.+)'\s*->\s*(\[.+\])\s*\((.+)\)""".r
      line match {
        case pattern(key, symbol, description) => 
          Some(KeyMapping(key, symbol.trim, description.trim))
        case _ => None
      }
    }

    def parseCombo(parts: Array[String]): Option[Combo] = {
      try {
        val Array(sequence, moveName) = parts
        val moves = sequence.split(",").map(_.trim).toList
        val name = moveName.replaceAll("\"", "").trim
        Some(Combo(moves, name))
      } catch {
        case _: Exception => None
      }
    }

    line.trim match {
      case "" => None
      case l if l.startsWith("#") => None
      case l =>
        val parts = l.split("->").map(_.trim)
        if (l.contains("(")) parseKeyMapping(parts)
        else parseCombo(parts)
    }
  }

  def parseLines(lines: List[String]): Either[String, Grammar] = {
    val grammarLines = lines.flatMap(parseLine)
    
    val (mappings, combos) = grammarLines.foldLeft((List[KeyMapping](), List[Combo]())) {
      case ((maps, combs), line) => line match {
        case m: KeyMapping => (m :: maps, combs)
        case c: Combo => (maps, c :: combs)
      }
    }

    Right(Grammar(mappings.reverse, combos.reverse))
  }

  def readFile(path: String): Either[String, List[String]] = {
    try {
      val source = scala.io.Source.fromFile(path)
      val lines = source.getLines().toList
      source.close()
      Right(lines)
    } catch {
      case e: Exception => Left(s"Error reading file: ${e.getMessage}")
    }
  }

  def parseFile(path: String): Either[String, Grammar] = {
    for {
      lines <- readFile(path)
      grammar <- parseLines(lines)
    } yield grammar
  }
}