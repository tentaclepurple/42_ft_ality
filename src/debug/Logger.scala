//src/debug/Logger.scala


package debug

object Logger {
  def log(state: DebugState, message: DebugMessage): Unit = {
    if (state.isEnabled) {
      message match {
        case StateTransition(from, input, to) =>
          println(s"State $from --($input)--> State $to")
        case AvailableMoves(moves) if moves.nonEmpty =>
          println(s"\nCOMBO! ${moves.mkString(", ")}!!!!\n")
        case AvailableMoves(_) => // Caso para conjunto vacío
        case TimeoutReset(_) =>
          println(s"¡TIMEOUT! Volviendo al estado inicial")
      }
    }
  }
}

/*
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
} */