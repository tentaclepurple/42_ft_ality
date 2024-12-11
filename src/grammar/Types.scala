package grammar

sealed trait GrammarLine
case class KeyMapping(key: String, action: String) extends GrammarLine
case class Combo(sequence: List[String], moveName: String) extends GrammarLine
case class Grammar(mappings: List[KeyMapping], combos: List[Combo])