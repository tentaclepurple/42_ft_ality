//src/automaton/Builder.scala


package automaton

import grammar.{Grammar, Combo}

object AutomatonBuilder {
  def fromGrammar(grammar: Grammar): Automaton = {
    println("\nCreando autómata...")
    // Estado inicial
    val initialState = State(0, Map(), isFinal = false, Set())

    // Para cada combo, construimos un camino reutilizando estados
    val finalStates = grammar.combos.foldLeft(Map[Int, State](0 -> initialState)) {
      case (states, combo) =>
        println(s"\nProcesando combo: ${combo.sequence.mkString(" -> ")} = ${combo.moveName}")

        // Construir el camino, reutilizando estados si es posible
        val statesWithUpdatedPath = buildPath(states, 0, combo)

        // Devolver el mapa actualizado de estados
        statesWithUpdatedPath
    }
    println("\n=== DUMP COMPLETO DEL AUTÓMATA ===")
    finalStates.toList.sortBy(_._1).foreach { case (id, state) =>
      println(s"\nESTADO ${id}:")
      println(s"  Es final: ${state.isFinal}")
      println(s"  Movimientos posibles: ${state.possibleMoves}")
      println("  Transiciones:")
      state.transitions.foreach { case (symbol, nextState) =>
        println(f"    $symbol%-10s -> Estado ${nextState.id}")
        println(f"      nextState.transitions: ${nextState.transitions.map { case (k,v) => s"$k->Estado${v.id}" }.mkString(", ")}")
        println(f"      nextState.isFinal: ${nextState.isFinal}")
        println(f"      nextState.possibleMoves: ${nextState.possibleMoves}")
      }
    }
    /* println("\nEstados finales:")
    finalStates.toList.sortBy(_._1).foreach { case (id, state) =>
      println(s"Estado $id:")
      println(s"  Transiciones: ${state.transitions.map { case (input, next) => s"$input -> ${next.id}" }.mkString(", ")}")
      if (state.isFinal) println(s"  Movimientos: ${state.possibleMoves.mkString(", ")}")
    } */

    val automaton = Automaton(
      states = finalStates,
      initialState = finalStates(0),
      currentState = finalStates(0),
      history = List()
    )
  

  // También imprimimos el estado inicial del autómata
  println("\n=== ESTADO INICIAL DEL AUTÓMATA ===")
  println(s"Estado inicial: ${automaton.initialState.id}")
  println(s"Estado actual: ${automaton.currentState.id}")
  println(s"Transiciones desde estado inicial: ${automaton.initialState.transitions.map { case (k,v) => s"$k->Estado${v.id}" }.mkString(", ")}")

  automaton
}

  private def buildPath(states: Map[Int, State], startId: Int, combo: Combo): Map[Int, State] = {
    println(s"\nConstruyendo camino para: ${combo.sequence.mkString(" -> ")}")
    val sequence = combo.sequence

    // Iterar sobre la secuencia y construir estados y transiciones
    val (updatedStates, lastStateId) = sequence.foldLeft((states, startId)) {
      case ((currentStates, currentId), move) =>
        println(s"Procesando movimiento: $move desde estado $currentId")
        val currentState = currentStates(currentId)
        println(s"Estado actual tiene transiciones: ${currentState.transitions.keys.mkString(", ")}")

        // Si ya existe una transición para este movimiento, reutilizar el estado
        currentState.transitions.get(move) match {
          case Some(existingState) =>
            println(s"Reutilizando estado ${existingState.id} para movimiento $move")
            (currentStates, existingState.id)

          case None =>
            // Crear un nuevo estado
            val newStateId = findNextId(currentStates)
            println(s"Creando nuevo estado $newStateId para movimiento $move")
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

    println(s"Finalizando camino en estado $lastStateId")
    // Marcar el último estado como final
    val finalState = updatedStates(lastStateId).copy(isFinal = true, possibleMoves = Set(combo.moveName))
    updatedStates + (lastStateId -> finalState)
  }

  private def findNextId(states: Map[Int, State]): Int = {
    if (states.isEmpty) 0 else states.keys.max + 1
  }
}
