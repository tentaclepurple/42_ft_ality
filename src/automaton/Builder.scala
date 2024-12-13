//src/automaton/Builder.scala


package automaton

import grammar.{Grammar, Combo}

object AutomatonBuilder {
  def fromGrammar(grammar: Grammar): Automaton = {
    // initial state is always 0
    val initialState = State(0, Map(), isFinal = false, Set())

    // For each combo, build the path
    val finalStates = grammar.combos.foldLeft(Map[Int, State](0 -> initialState)) {
      case (states, combo) =>

        // Build the path for the current combo
        val statesWithUpdatedPath = buildPath(states, 0, combo)

        // Return the updated states
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

    // Iterate over the sequence of moves to build the path
    val (updatedStates, lastStateId) = sequence.foldLeft((states, startId)) {
      case ((currentStates, currentId), move) =>
        val currentState = currentStates(currentId)
        currentState.transitions.get(move) match {
          case Some(existingState) =>
            (currentStates, existingState.id)

          case None =>
            // Create a new state and add it to the current state transitions
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

    // Mark the last state as final
    val finalState = updatedStates(lastStateId).copy(isFinal = true, possibleMoves = Set(combo.moveName))
    updatedStates + (lastStateId -> finalState)
  }

  private def findNextId(states: Map[Int, State]): Int = {
    if (states.isEmpty) 0 else states.keys.max + 1
  }
}
