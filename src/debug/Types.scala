// src/debug/Types.scala


package debug

case class DebugState(isEnabled: Boolean)

// Para los diferentes tipos de mensajes de debug
sealed trait DebugMessage
case class StateTransition(fromState: Int, input: String, toState: Int) extends DebugMessage
case class AvailableMoves(moves: Set[String]) extends DebugMessage
case class TimeoutReset(lastInput: String) extends DebugMessage