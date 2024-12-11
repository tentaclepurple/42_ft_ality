package grammar

object Printer {
  def formatGrammar(grammar: Grammar): String = {
    val mappingsStr = formatMappings(grammar.mappings)
    val combosStr = formatCombos(grammar.combos)
    s"$mappingsStr\n$combosStr"
  }

  private def formatMappings(mappings: List[KeyMapping]): String = {
    val header = "=== KEY MAPPINGS ===\n"
    val content = mappings.map { mapping =>
      s"${mapping.key}  -> ${mapping.action}"
    }.mkString("\n")
    s"$header$content"
  }

  private def formatCombos(combos: List[Combo]): String = {
    val header = "\n=== COMBOS ===\n"
    val content = combos.map { combo =>
      s"${combo.sequence.mkString("  ")}  -> ${combo.moveName}"
    }.mkString("\n")
    s"$header$content"
  }
}