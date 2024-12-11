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
    /* val shouldReset = history.headOption.exists(t => 
      currentTime - t.timestamp > timeoutMillis
    ) */
    val shouldReset = false

    if (debugState.isEnabled) {
      if (shouldReset) {
        Logger.log(debugState, TimeoutReset(input))
      } else {
        currentState.transitions.get(input) match {
          case Some(nextState) =>
            //Logger.log(debugState, StateTransition(currentState.id, input, nextState.id))
            println(s"SOME: ${currentState.id} --($input)--> ${nextState.id}")
          case None =>
            //Logger.log(debugState, StateTransition(currentState.id, input, initialState.id))
            println(s"NONE: ${currentState.id} --($input)--> ${initialState.id}")
        }
      }
    }

    val newAutomaton = if (shouldReset) {
      // Reset por timeout - volver al estado inicial y empezar un nuevo combo
      val nextState = initialState.transitions.get(input).getOrElse(initialState)
      this.copy(
        currentState = nextState,
        history = List(Transition(initialState, input, currentTime))
      )
    } else {
      // Transición normal
      currentState.transitions.get(input) match {
        case Some(nextState) =>
          // Transición válida - continuar el combo
          this.copy(
            currentState = nextState,
            history = Transition(currentState, input, currentTime) :: history
          )
        case None =>
          // Transición inválida - ver si podemos empezar un nuevo combo desde el estado inicial
          val nextState = initialState.transitions.get(input).getOrElse(initialState)
          this.copy(
            currentState = nextState,
            history = List(Transition(initialState, input, currentTime))
          )
      }
    }

    (newAutomaton, None)
  }

  def getCurrentMoves: Set[String] = currentState.possibleMoves
  def enableDebug: Automaton = this.copy(debugState = DebugState(true))
  def disableDebug: Automaton = this.copy(debugState = DebugState(false))
}