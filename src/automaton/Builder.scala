//src/automaton/Builder.scala


package automaton

import grammar.{Grammar, Combo}

object AutomatonBuilder {
  def fromGrammar(grammar: Grammar): Automaton = {
    // Estado inicial
    val initialState = State(0, Map(), isFinal = false, Set())

    // Para cada combo, construimos un camino reutilizando estados
    val finalStates = grammar.combos.foldLeft(Map[Int, State](0 -> initialState)) {
      case (states, combo) =>
        //println(s"\nProcesando combo: ${combo.sequence.mkString(" -> ")} = ${combo.moveName}")

        // Construir el camino, reutilizando estados si es posible
        val statesWithUpdatedPath = buildPath(states, 0, combo)

        // Devolver el mapa actualizado de estados
        statesWithUpdatedPath
    }

    Automaton(
      states = finalStates,
      initialState = finalStates(0),
      currentState = finalStates(0),
      history = List()
    )
  }

  private def buildPath(states: Map[Int, State], startId: Int, combo: Combo): Map[Int, State] = {
    val sequence = combo.sequence

    // Iterar sobre la secuencia y construir estados y transiciones
    val (updatedStates, lastStateId) = sequence.foldLeft((states, startId)) {
      case ((currentStates, currentId), move) =>
        val currentState = currentStates(currentId)
        currentState.transitions.get(move) match {
          case Some(existingState) =>
            (currentStates, existingState.id)

          case None =>
            // Crear un nuevo estado
            val newStateId = findNextId(currentStates)
            val newState = State(newStateId, Map(), isFinal = false, Set())
            val updatedCurrentState = currentState.copy(
              transitions = currentState.transitions + (move -> newState)
            )
            (
              currentStates + (newStateId -> newState) + (currentId -> updatedCurrentState),
              newStateId
            )
        }
    }

    //println(s"Finalizando camino en estado $lastStateId")
    // Marcar el último estado como final
    val finalState = updatedStates(lastStateId).copy(isFinal = true, possibleMoves = Set(combo.moveName))
    updatedStates + (lastStateId -> finalState)
  }

  private def findNextId(states: Map[Int, State]): Int = {
    if (states.isEmpty) 0 else states.keys.max + 1
  }
}
