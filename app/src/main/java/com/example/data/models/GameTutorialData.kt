package com.example.data.models

data class GameTutorial(
    val objective: String,
    val steps: List<TutorialStep>,
    val proTip: String
)

data class TutorialStep(
    val icon: String,
    val title: String,
    val description: String
)

object GameTutorialData {
    val tutorials: Map<GameType, GameTutorial> = mapOf(
        GameType.ZIP to GameTutorial(
            objective = "Connect all nodes on the grid into one continuous non-intersecting line.",
            steps = listOf(
                TutorialStep("🟢", "Start at Green", "Begin your path from the bright green starting node."),
                TutorialStep("✏️", "Draw Continuous Path", "Swipe or tap adjacent grid tiles to trace your route without crossing your own trail."),
                TutorialStep("🏁", "Visit Every Node", "Every required tile must be filled to complete the circuit and clear the level.")
            ),
            proTip = "Plan from dead-ends first; corners usually have only one valid path in and out!"
        ),
        GameType.SUDOKU to GameTutorial(
            objective = "Fill the compact grid with numbers so every row, column, and block has unique digits.",
            steps = listOf(
                TutorialStep("🔢", "Select & Place Digits", "Tap an empty cell, then select a number from the keypad (1 to 4, 6, or 9)."),
                TutorialStep("🚫", "No Duplicate Numbers", "Each number can only appear once per row, column, and outlined region."),
                TutorialStep("✏️", "Pencil Mode", "Toggle Pencil mode to jot down possible candidates when multiple choices exist.")
            ),
            proTip = "Look for rows or blocks that already have most numbers filled in first!"
        ),
        GameType.TANGO to GameTutorial(
            objective = "Balance Sun and Moon symbols across the grid according to logic rules.",
            steps = listOf(
                TutorialStep("☀️", "Sun & Moon Balance", "Each row and column must contain an equal number of Suns and Moons."),
                TutorialStep("⛔", "No Three in a Row", "You can never place three identical symbols adjacent horizontally or vertically."),
                TutorialStep("🔍", "Follow Clues", "An '=' clue means both cells match; an '×' clue means they must be opposite.")
            ),
            proTip = "When two identical symbols sit next to each other, place the opposite symbol on both ends!"
        ),
        GameType.QUEENS to GameTutorial(
            objective = "Place Queens safely so no two Queens can attack each other.",
            steps = listOf(
                TutorialStep("👑", "One Queen Per Zone", "Place exactly one Queen inside each distinct colored region."),
                TutorialStep("↔️", "Row & Column Limits", "No two Queens can share the same row or column."),
                TutorialStep("🛡️", "No Adjacent Queens", "Queens cannot touch each other orthogonally or diagonally (even 1 step away).")
            ),
            proTip = "Place Queens in the smallest color zones first to eliminate large areas quickly!"
        ),
        GameType.CROSSCLIMB to GameTutorial(
            objective = "Solve trivia clues by changing one letter per rung to climb the word ladder.",
            steps = listOf(
                TutorialStep("💡", "Read the Clue", "Check the prompt for each ladder rung to deduce the word."),
                TutorialStep("⌨️", "Single Letter Swap", "Each rung must differ by exactly ONE letter from the word below it."),
                TutorialStep("🪜", "Climb to the Top", "Type words using your device keyboard or on-screen keys to reach the top rung.")
            ),
            proTip = "If stuck on a clue, inspect the adjacent rung's letters to narrow down the target word!"
        ),
        GameType.PINPOINT to GameTutorial(
            objective = "Deduce the secret mystery topic connecting all revealed item clues.",
            steps = listOf(
                TutorialStep("🔎", "Reveal Clues", "Tap to unlock hints one by one (e.g. Lion, Tiger, Leopard)."),
                TutorialStep("🎯", "Find the Common Link", "Pick the correct category or theme that connects all clues."),
                TutorialStep("⚡", "High Score Bonus", "Guessing with fewer clues revealed awards maximum points and stars.")
            ),
            proTip = "Think broadly about shared origins, definitions, idioms, or taxonomies!"
        ),
        GameType.WEND to GameTutorial(
            objective = "Wander through the letter grid to uncover all target category words.",
            steps = listOf(
                TutorialStep("🧭", "Trace Letters", "Swipe across adjacent letters horizontally, vertically, or diagonally."),
                TutorialStep("📋", "Target Word List", "Find all the words listed at the top belonging to today's theme."),
                TutorialStep("🔀", "Shuffle & Helpers", "Use the Shuffle button or Hints if you need a fresh perspective on the board.")
            ),
            proTip = "Look for uncommon letters (Z, X, Q, J, K) first to locate anchor words on the grid!"
        ),
        GameType.PATCHES to GameTutorial(
            objective = "Fit all colorful geometric puzzle patches into the board with zero overlap.",
            steps = listOf(
                TutorialStep("🧩", "Select a Patch", "Tap or drag a geometric polyomino piece from your tray."),
                TutorialStep("📐", "Place on Board", "Fit the piece onto the highlighted board grid."),
                TutorialStep("✨", "Complete the Silhouette", "Every square on the board must be covered with no pieces sticking out.")
            ),
            proTip = "Place large and awkward L- or T-shaped pieces into corners before filling smaller gaps!"
        ),
        GameType.BUBBLE_SORT to GameTutorial(
            objective = "Sort the colorful bubbles into tubes until each tube holds a single solid color.",
            steps = listOf(
                TutorialStep("🧪", "Tap to Lift", "Tap any tube to lift the top bubble into the ready position."),
                TutorialStep("🎯", "Tap to Place", "Tap a destination tube with matching top color or an empty tube."),
                TutorialStep("📦", "Tube Capacity", "Tubes can hold at most 4 bubbles. Group 4 matching bubbles to seal a tube.")
            ),
            proTip = "Always keep one tube empty as a workspace to shuffle and free up trapped colors!"
        ),
        GameType.BUBBLE_SHOOTER to GameTutorial(
            objective = "Aim and shoot bubbles to match 3+ of the same color and clear the ceiling.",
            steps = listOf(
                TutorialStep("🎯", "Aim Your Shot", "Drag or tap on the board to line up your launcher trajectory with wall bounces."),
                TutorialStep("💥", "Pop Clusters", "Connect 3 or more bubbles of matching color to pop them instantly."),
                TutorialStep("🌊", "Cascade Drops", "Popping upper anchors will drop all unsupported hanging bubbles below for combos!")
            ),
            proTip = "Bounce shots off side walls to reach high-value clusters tucked behind blocker bubbles!"
        ),
        GameType.TILE_MATCH to GameTutorial(
            objective = "Pick tiles from the multilayer stack into your tray and clear them by matching triples.",
            steps = listOf(
                TutorialStep("🀄", "Pick Uncovered Tiles", "Tap any bright, accessible tile on the board to move it into your tray."),
                TutorialStep("✨", "Match 3 Identical", "When 3 identical tiles enter the tray, they clear immediately."),
                TutorialStep("⚠️", "Mind Tray Capacity", "Your tray has strictly 4 slots. If all slots fill without a match, it's game over!")
            ),
            proTip = "Only pick tiles that help complete a triplet or unblock essential layers underneath!"
        )
    )

    fun getTutorial(type: GameType): GameTutorial {
        return tutorials[type] ?: tutorials.values.first()
    }
}
