// src/debug/Types.scala


package debug

case class DebugState(isEnabled: Boolean)

// DebugMessage is a sealed trait, which means that all its subclasses must be defined in the same file.
sealed trait DebugMessage
case class StateTransition(fromState: Int, input: String, toState: Int) extends DebugMessage
case class AvailableMoves(moves: Set[String]) extends DebugMessage
case class TimeoutReset(lastInput: String) extends DebugMessage