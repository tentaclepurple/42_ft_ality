//src/automaton/Types.scala

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

    // Si estamos en un estado final o la transición actual no es válida,
    // intentamos empezar un nuevo combo desde el estado inicial
    if (currentState.isFinal || !currentState.transitions.contains(input)) {
      // Verificar si podemos empezar un nuevo combo con esta entrada
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
      // Continuamos el combo actual
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
