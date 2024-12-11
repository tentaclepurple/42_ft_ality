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
    println(s"\n=== TRANSICIÓN CON INPUT: $input ===")
    println(s"Estado actual: ${currentState.id}")
    println(s"Transiciones disponibles en mapa de estados: ${states(currentState.id).transitions.map { case (k,v) => s"$k->Estado${v.id}" }.mkString(", ")}")
    println(s"Transiciones disponibles en estado actual: ${currentState.transitions.map { case (k,v) => s"$k->Estado${v.id}" }.mkString(", ")}")

    // Si estamos en un estado final o la transición actual no es válida,
    // intentamos empezar un nuevo combo desde el estado inicial
    if (currentState.isFinal || !currentState.transitions.contains(input)) {
      // Verificar si podemos empezar un nuevo combo con esta entrada
      states(0).transitions.get(input) match {
        case Some(nextState) =>
          println(s"**starting new combo: Estado 0 --($input)--> Estado ${nextState.id}")
          (this.copy(
            currentState = states(nextState.id),
            history = List(Transition(states(0), input, currentTime))
          ), None)
        case None =>
          println(s"**invalid input: permaneciendo en estado inicial")
          (this.copy(
            currentState = states(0),
            history = List()
          ), None)
      }
    } else {
      // Continuamos el combo actual
      currentState.transitions.get(input) match {
        case Some(nextState) =>
          println(s"**continuing combo: Estado ${currentState.id} --($input)--> Estado ${nextState.id}")
          val newState = states(nextState.id)
          val message = if (newState.isFinal) {
            println(s"**combo completed: ${newState.possibleMoves.mkString(", ")}")
            Some(s"¡COMBO! ${newState.possibleMoves.mkString(", ")}")
          } else None
          
          (this.copy(
            currentState = newState,
            history = Transition(currentState, input, currentTime) :: history
          ), message)
        case None =>
          println(s"**invalid transition: volviendo a estado inicial")
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
