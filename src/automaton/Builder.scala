package automaton

import grammar.{Grammar, Combo}

object AutomatonBuilder {
  def fromGrammar(grammar: Grammar): Automaton = {
    println("\nCreando autómata...")
    // Estado inicial
    val initialState = State(0, Map(), isFinal = false, Set())
    
    // Para cada combo creamos un camino de estados
    val finalStates = grammar.combos.foldLeft(Map[Int, State](0 -> initialState)) {
      case (states, combo) =>
        println(s"\nProcesando combo: ${combo.sequence.mkString(" -> ")} = ${combo.moveName}")
        val nextId = findNextId(states)
        println(s"Siguiente ID disponible: $nextId")
        
        // Obtener el estado inicial actual con sus transiciones existentes
        val currentInitialTransitions = states(0).transitions
        
        // Construir el nuevo camino
        val statesWithNewPath = buildPath(states, nextId, combo)
        
        // Actualizar el estado inicial combinando las transiciones antiguas y nuevas
        val firstMove = combo.sequence.head
        val firstStateId = nextId
        val updatedInitialTransitions = currentInitialTransitions + (firstMove -> statesWithNewPath(firstStateId))
        
        // Actualizar el mapa final con el estado inicial actualizado
        statesWithNewPath + (0 -> State(0, updatedInitialTransitions, isFinal = false, Set()))
    }

    println("\nEstados finales:")
    finalStates.toList.sortBy(_._1).foreach { case (id, state) =>
      println(s"Estado $id:")
      println(s"  Transiciones: ${state.transitions.map { case (input, next) => s"$input -> ${next.id}" }.mkString(", ")}")
      if (state.isFinal) println(s"  Movimientos: ${state.possibleMoves.mkString(", ")}")
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
    // Crear un estado por cada elemento en la secuencia, más uno para el estado final
    val sequence = combo.sequence
    val numStates = sequence.length + 1
    
    // Primero creamos todos los estados necesarios
    println(s"Creando estados para el camino desde $startId")
    val newStates = (0 until numStates).foldLeft(states) { case (accStates, offset) =>
      val stateId = startId + offset
      val isLastState = offset == sequence.length
      println(s"Creando estado $stateId${if (isLastState) " (final)" else ""}")
      
      accStates + (stateId -> State(
        id = stateId,
        transitions = Map(),
        isFinal = isLastState,
        possibleMoves = if (isLastState) Set(combo.moveName) else Set()
      ))
    }

    // Luego añadimos las transiciones entre los estados
    println("Creando transiciones")
    sequence.zipWithIndex.foldLeft(newStates) { case (accStates, (move, idx)) =>
      val fromId = startId + idx
      val toId = startId + idx + 1
      println(s"Añadiendo transición: Estado $fromId --($move)--> Estado $toId")
      
      accStates + (fromId -> accStates(fromId).copy(
        transitions = accStates(fromId).transitions + (move -> accStates(toId))
      ))
    }
  }
}