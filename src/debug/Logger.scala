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
          println(s"¡TIMEOUT! Return to initial state")
      }
    }
  }
}

