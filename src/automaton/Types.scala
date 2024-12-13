//src/automaton/Types.scala

package automaton

import debug.{DebugState, Logger, DebugMessage, StateTransition, AvailableMoves, TimeoutReset}

// First the types that define the automaton
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

// Then the types that define the automaton's state
case class Automaton(
  states: Map[Int, State],
  initialState: State,
  currentState: State,
  history: List[Transition],
  timeoutMillis: Long = 1000,
  debugState: DebugState = DebugState(false)
) {
  def transition(input: String, currentTime: Long): (Automaton, Option[String]) = {

    // If the input is a timeout, reset the automaton we try to start a new combo
    if (currentState.isFinal || !currentState.transitions.contains(input)) {
      // Check if the last move was a timeout
      states(0).transitions.get(input) match {
        case Some(nextState) =>
          Logger.log(debugState, StateTransition(states(0).id, input, nextState.id))
          (this.copy(
            currentState = states(nextState.id),
            history = List(Transition(states(0), input, currentTime))
          ), None)
        case None =>
          (this.copy(
            currentState = states(0),
            history = List()
          ), None)
      }
    } else {
      // Continue the combo
      currentState.transitions.get(input) match {
        case Some(nextState) =>
          Logger.log(debugState, StateTransition(currentState.id, input, nextState.id))
          val newState = states(nextState.id)
          val message = if (newState.isFinal) {
            Logger.log(debugState, AvailableMoves(newState.possibleMoves))
            Some(s"COMBO! ${newState.possibleMoves.mkString(", ")}")
          } else None
          
          (this.copy(
            currentState = newState,
            history = Transition(currentState, input, currentTime) :: history
          ), message)
        case None =>
          (this.copy(
            currentState = states(0),
            history = List()
          ), None)
      }
    }
}

  def getCurrentMoves: Set[String] = currentState.possibleMoves
  def enableDebug: Automaton = this.copy(debugState = DebugState(true))
  def disableDebug: Automaton = this.copy(debugState = DebugState(false))
}
