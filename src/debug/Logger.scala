package debug

object Logger {
  def formatMessage(message: DebugMessage): String = {
    message match {
      case StateTransition(from, input, to) =>
        s"DEBUG: Estado $from --($input)--> Estado $to"
      case AvailableMoves(moves) if moves.nonEmpty =>
        s"DEBUG: Movimientos posibles: ${moves.mkString(", ")}"
      case TimeoutReset(input) =>
        s"DEBUG: Reset por timeout después de: $input"
      case _ => ""
    }
  }
}