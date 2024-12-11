package automaton

import debug.{DebugState, Logger, DebugMessage, StateTransition, AvailableMoves, TimeoutReset}

// Definimos los tipos básicos primero
case class State(
  id: Int,
  transitions: Map[String, State],
  isFinal: Boolean,
  possibleMoves: Set[String]
)

case class Transition(
  from: State,
  input: String,
  timestamp: Long
)

// Luego el Autómata que usa estos tipos
case class Automaton(
  states: Map[Int, State],
  initialState: State,
  currentState: State,
  history: List[Transition],
  timeoutMillis: Long = 1000,
  debugState: DebugState = DebugState(false)
) {
  def transition(input: String, currentTime: Long): (Automaton, Option[String]) = {
    val shouldReset = history.headOption.exists(t => 
      currentTime - t.timestamp > timeoutMillis
    )

    val debugMessage = if (debugState.isEnabled) {
      if (shouldReset) {
        Some(Logger.formatMessage(TimeoutReset(input)))
      } else {
        currentState.transitions.get(input) match {
          case Some(nextState) =>
            Some(Logger.formatMessage(StateTransition(currentState.id, input, nextState.id)))
          case None =>
            Some(Logger.formatMessage(StateTransition(currentState.id, input, initialState.id)))
        }
      }
    } else None

    val newAutomaton = if (shouldReset) {
      this.copy(
        currentState = initialState,
        history = List(Transition(initialState, input, currentTime))
      )
    } else {
      currentState.transitions.get(input) match {
        case Some(nextState) =>
          this.copy(
            currentState = nextState,
            history = Transition(currentState, input, currentTime) :: history
          )
        case None => 
          this.copy(
            currentState = initialState,
            history = List(Transition(initialState, input, currentTime))
          )
      }
    }

    (newAutomaton, debugMessage)
  }

  def getCurrentMoves: Set[String] = currentState.possibleMoves
  def enableDebug: Automaton = this.copy(debugState = DebugState(true))
  def disableDebug: Automaton = this.copy(debugState = DebugState(false))
}