# **ft_ality - Fighting Game Automaton**

## **Description**

`ft_ality` is a functional project that emulates a training mode for fighting games, like those found in franchises such as *Street Fighter*. Its main functionality is to recognize key combinations (combos) using a finite-state automaton, formally representing state transitions and their association with movement combinations.

The project also includes a graphical interface to visually display messages triggered by key presses and executed combos. This system allows players to experience how combos are built and recognized in a fighting game.

---

## **Features**

### **Mandatory Features**
1. **Formal definition of the automaton**:
   - The automaton is defined as a set of states, transitions, and acceptance states. It follows this structure:
     ```
     A = <Q, Σ, Q0, F, δ>
     ```
   - Where:
     - `Σ` is the input alphabet.
     - `Q` is the set of states.
     - `Q0` is the initial state.
     - `F` is the set of acceptance states.
     - `δ` is the transition function.

2. **Automaton training**:
   - The automaton is trained by reading grammar files that define key combinations associated with moves.
   - Grammar files are processed at runtime to generate the transitions.

3. **Execution and language recognition**:
   - The program recognizes key combinations as they are entered via the keyboard.
   - It displays the name of the combo when a valid combination is executed.

### **Bonus Features**
- **Graphical interface**:
  - Displays system-generated messages, including executed combos.
  - Supports animated images (GIFs) to visually represent executed combos.

- **Debug mode**:
  - Includes a debug mode to display the automaton's state transitions step-by-step.

---

## **Objectives**

This project aims to introduce fundamental concepts of regular languages and finite-state automata, including:

- Definition and usage of formal grammars.
- Implementation of finite-state automata for recognizing regular languages.
- Practical application in an interactive and visual environment.

---

## **Requirements**

- **Language**: Scala (version 2.13+)
- **Dependencies**:
  - Java standard library (`Swing`, `AWT`, `ImageIO`).

---

## **How to Use**

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your_username/ft_ality.git
   cd ft_ality
   make
   make load
   in browser: localhost:port
   "?" for debug mode