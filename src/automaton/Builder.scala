package automaton

import grammar.{Grammar, Combo}

object AutomatonBuilder {
  def fromGrammar(grammar: Grammar): Automaton = {
    // Estado inicial
    val initialState = State(0, Map(), isFinal = false, Set())
    
    // Para cada combo creamos un camino de estados
    val finalStates = grammar.combos.foldLeft(Map[Int, State](0 -> initialState)) {
      case (states, combo) =>
        buildPath(states, findNextId(states), combo)
    }

    // Debug: imprimir estados para verificar
    println("\n=== Estados del Autómata ===")
    finalStates.foreach { case (id, state) =>
      println(s"Estado $id:")
      println(s"  Es final: ${state.isFinal}")
      println(s"  Movimientos: ${state.possibleMoves}")
      println(s"  Transiciones: ${state.transitions.map { case (input, next) => s"$input -> ${next.id}" }.mkString(", ")}")
    }

    Automaton(
      states = finalStates,
      initialState = finalStates(0),
      currentState = finalStates(0),
      history = List()
    )
  }

  private def findNextId(states: Map[Int, State]): Int = {
    if (states.isEmpty) 0 else states.keys.max + 1
  }

  private def buildPath(states: Map[Int, State], startId: Int, combo: Combo): Map[Int, State] = {
    // Crear todos los estados del camino
    val newStates = (0 to combo.sequence.length).foldLeft(states) { 
      case (accStates, offset) =>
        val newId = startId + offset
        val isLastState = offset == combo.sequence.length
        accStates + (newId -> State(
          id = newId,
          transitions = Map(),
          isFinal = isLastState,
          possibleMoves = if (isLastState) Set(combo.moveName) else Set()
        ))
    }

    // Crear todas las transiciones
    combo.sequence.zipWithIndex.foldLeft(newStates) {
      case (accStates, (move, idx)) =>
        val currentId = startId + idx
        val nextId = startId + idx + 1
        val currentState = accStates(currentId)
        val nextState = accStates(nextId)
        accStates + (currentId -> currentState.copy(
          transitions = currentState.transitions + (move -> nextState)
        ))
    }
  }
}