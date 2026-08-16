package com.example.data.models

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class GameType(val key: String) {
    ZIP("zip"),
    SUDOKU("sudoku"),
    TANGO("tango"),
    QUEENS("queens"),
    CROSSCLIMB("crossclimb"),
    PINPOINT("pinpoint"),
    WEND("wend"),
    PATCHES("patches"),
    BUBBLE_SORT("bubble_sort"),
    BUBBLE_SHOOTER("bubble_shooter"),
    TILE_MATCH("tile_match")
}

enum class GameCategory(val title: String) {
    ALL("All"),
    LOGIC("Logic"),
    WORDS("Words"),
    MATCHING("Matching"),
    ACTION("Action"),
    DAILY("Daily"),
    FAVORITES("Favorites")
}

enum class GameDifficulty(
    val displayName: String,
    val shortName: String,
    val tagLine: String,
    val scoreMultiplier: Float,
    val timeLimitSeconds: Int,
    val badgeEmoji: String,
    val gridSummary: String,
    val levelOffset: Int
) {
    EASY(
        displayName = "Easy / Relaxed",
        shortName = "EASY",
        tagLine = "Smaller grids, extra hints & relaxed timing",
        scoreMultiplier = 1.0f,
        timeLimitSeconds = 180,
        badgeEmoji = "🟢",
        gridSummary = "4×4 Grids • 180s Time Limit",
        levelOffset = 1
    ),
    MEDIUM(
        displayName = "Medium / Standard",
        shortName = "MED",
        tagLine = "Balanced dimensions & moderate challenge",
        scoreMultiplier = 1.5f,
        timeLimitSeconds = 120,
        badgeEmoji = "🟡",
        gridSummary = "5×5 / 6×6 Grids • 120s Time Limit",
        levelOffset = 21
    ),
    HARD(
        displayName = "Hard / Master",
        shortName = "HARD",
        tagLine = "Large boards, tricky constraints & faster tempo",
        scoreMultiplier = 2.0f,
        timeLimitSeconds = 75,
        badgeEmoji = "🔴",
        gridSummary = "6×6 / 8×8 Grids • 75s Time Limit",
        levelOffset = 51
    ),
    EXPERT(
        displayName = "Expert / Grandmaster",
        shortName = "EXPERT",
        tagLine = "Maximum grid size, minimal clues & blitz timer",
        scoreMultiplier = 3.0f,
        timeLimitSeconds = 45,
        badgeEmoji = "🟣",
        gridSummary = "8×8 / 9×9 Grids • 45s Blitz",
        levelOffset = 101
    );

    fun getGridDimension(gameType: GameType): String {
        return when (this) {
            EASY -> when (gameType) {
                GameType.SUDOKU -> "4×4"
                GameType.TANGO -> "4×4"
                GameType.QUEENS -> "5×5"
                GameType.ZIP -> "4×4"
                GameType.PATCHES -> "4×4"
                GameType.WEND -> "4×4"
                GameType.BUBBLE_SORT -> "3 Tubes"
                GameType.BUBBLE_SHOOTER -> "3 Rows"
                GameType.CROSSCLIMB -> "3 Rungs"
                GameType.PINPOINT -> "5 Clues"
                GameType.TILE_MATCH -> "4 Sets"
            }
            MEDIUM -> when (gameType) {
                GameType.SUDOKU -> "6×6"
                GameType.TANGO -> "6×6"
                GameType.QUEENS -> "6×6"
                GameType.ZIP -> "5×5"
                GameType.PATCHES -> "5×5"
                GameType.WEND -> "5×5"
                GameType.BUBBLE_SORT -> "4 Tubes"
                GameType.BUBBLE_SHOOTER -> "4 Rows"
                GameType.CROSSCLIMB -> "4 Rungs"
                GameType.PINPOINT -> "4 Clues"
                GameType.TILE_MATCH -> "6 Sets"
            }
            HARD -> when (gameType) {
                GameType.SUDOKU -> "6×6 Hard"
                GameType.TANGO -> "8×8"
                GameType.QUEENS -> "7×7"
                GameType.ZIP -> "6×6"
                GameType.PATCHES -> "6×6"
                GameType.WEND -> "6×6"
                GameType.BUBBLE_SORT -> "5 Tubes"
                GameType.BUBBLE_SHOOTER -> "5 Rows"
                GameType.CROSSCLIMB -> "5 Rungs"
                GameType.PINPOINT -> "3 Clues"
                GameType.TILE_MATCH -> "8 Sets"
            }
            EXPERT -> when (gameType) {
                GameType.SUDOKU -> "9×9"
                GameType.TANGO -> "8×8 Strict"
                GameType.QUEENS -> "8×8"
                GameType.ZIP -> "7×7"
                GameType.PATCHES -> "6×6 Complex"
                GameType.WEND -> "6×6 Dense"
                GameType.BUBBLE_SORT -> "6 Tubes"
                GameType.BUBBLE_SHOOTER -> "6 Rows"
                GameType.CROSSCLIMB -> "6 Rungs"
                GameType.PINPOINT -> "2 Clues"
                GameType.TILE_MATCH -> "10 Sets"
            }
        }
    }
}

data class GameInfo(
    val type: GameType,
    val name: String,
    val tagLine: String,
    val description: String,
    val category: GameCategory,
    val accentColor: Color,
    val pastelColor: Color,
    val symbolEmoji: String,
    val totalLevels: Int = 200,
    val rules: List<String>
)

object GameCatalog {
    val games: List<GameInfo> = listOf(
        GameInfo(
            type = GameType.ZIP,
            name = "ZIP",
            tagLine = "Pathfinding Grid",
            description = "Use your pathfinding skills to move through the grid without crossing your path.",
            category = GameCategory.LOGIC,
            accentColor = PrimaryIndigo,
            pastelColor = PastelIndigo,
            symbolEmoji = "⚡",
            rules = listOf(
                "Start at the green starting tile.",
                "Draw a single continuous line visiting every required node.",
                "Avoid obstacles and do not cross over existing paths."
            )
        ),
        GameInfo(
            type = GameType.SUDOKU,
            name = "Mini Sudoku",
            tagLine = "Compact Sudoku",
            description = "The classic number puzzle made mini and fast with 4×4 and 6×6 compact boards.",
            category = GameCategory.LOGIC,
            accentColor = AccentEmerald,
            pastelColor = PastelEmerald,
            symbolEmoji = "🔢",
            rules = listOf(
                "Fill the empty cells with digits.",
                "Each row, column, and block must contain unique numbers.",
                "Use pencil notes to track candidates."
            )
        ),
        GameInfo(
            type = GameType.TANGO,
            name = "Tango",
            tagLine = "Sun & Moon Logic",
            description = "Use pure reasoning to balance Sun and Moon symbols across the grid.",
            category = GameCategory.LOGIC,
            accentColor = AccentAmber,
            pastelColor = PastelAmber,
            symbolEmoji = "☀️",
            rules = listOf(
                "No three identical symbols may be consecutive in any row or column.",
                "Each row and column must contain equal Sun and Moon symbols.",
                "Follow clue marks between adjacent cells (= same, × opposite)."
            )
        ),
        GameInfo(
            type = GameType.QUEENS,
            name = "Queens",
            tagLine = "Crown Placement",
            description = "Use your spatial logic to place queens safely inside colored territory zones.",
            category = GameCategory.LOGIC,
            accentColor = AccentRose,
            pastelColor = PastelRose,
            symbolEmoji = "👑",
            rules = listOf(
                "Place exactly one Queen in each colored region.",
                "Each row and column can only contain one Queen.",
                "Queens cannot touch each other diagonally or orthogonally."
            )
        ),
        GameInfo(
            type = GameType.CROSSCLIMB,
            name = "Crossclimb",
            tagLine = "Word Ladder Trivia",
            description = "Solve word clues by changing one letter at a time to climb the trivia ladder.",
            category = GameCategory.WORDS,
            accentColor = AccentViolet,
            pastelColor = PastelViolet,
            symbolEmoji = "🪜",
            rules = listOf(
                "Read the clue for each rung of the ladder.",
                "Change exactly one letter from the previous word to solve.",
                "Reach the top word to complete the ladder."
            )
        ),
        GameInfo(
            type = GameType.PINPOINT,
            name = "Pinpoint",
            tagLine = "Pattern & Association",
            description = "Discover the hidden category or common link connecting the revealed clues.",
            category = GameCategory.WORDS,
            accentColor = AccentSky,
            pastelColor = PastelSky,
            symbolEmoji = "🎯",
            rules = listOf(
                "Read the revealed item clues one by one.",
                "Guess the common theme connecting all clues.",
                "Guess earlier with fewer clues to earn higher bonus points."
            )
        ),
        GameInfo(
            type = GameType.WEND,
            name = "Wend",
            tagLine = "Word Wander Grid",
            description = "Wander through the grid to discover all themed words hidden in the letters.",
            category = GameCategory.WORDS,
            accentColor = AccentTeal,
            pastelColor = PastelTeal,
            symbolEmoji = "🧭",
            rules = listOf(
                "Swipe across adjacent letters in any direction.",
                "Uncover all target category words.",
                "Find every word on the list to solve the board."
            )
        ),
        GameInfo(
            type = GameType.PATCHES,
            name = "Patches",
            tagLine = "Geometric Shape Fit",
            description = "Fit every colorful geometric patch onto the board without any overlap.",
            category = GameCategory.LOGIC,
            accentColor = AccentOrange,
            pastelColor = PastelOrange,
            symbolEmoji = "🧩",
            rules = listOf(
                "Drag and position puzzle pieces onto the board grid.",
                "All board cells must be completely covered.",
                "Pieces cannot overlap or stick out of bounds."
            )
        ),
        GameInfo(
            type = GameType.BUBBLE_SORT,
            name = "Bubble Sort",
            tagLine = "Color Water/Ball Sort",
            description = "Sort colorful bubbles into matching tubes until each tube holds one single color.",
            category = GameCategory.MATCHING,
            accentColor = AccentFuchsia,
            pastelColor = PastelFuchsia,
            symbolEmoji = "🧪",
            rules = listOf(
                "Tap a tube to lift the top bubble, tap another tube to place it.",
                "Bubbles can only be placed on the same color or into an empty tube.",
                "Each tube has a maximum capacity of 4 bubbles."
            )
        ),
        GameInfo(
            type = GameType.BUBBLE_SHOOTER,
            name = "Bubble Shooting",
            tagLine = "Aim & Pop Arcade",
            description = "Aim, launch, and match 3 or more bubbles of the same color to clear the ceiling.",
            category = GameCategory.ACTION,
            accentColor = AccentLime,
            pastelColor = PastelLime,
            symbolEmoji = "🫧",
            rules = listOf(
                "Aim your launcher towards the cluster above.",
                "Hit 3 or more bubbles of matching color to pop them.",
                "Drop hanging bubbles and clear all targets to win."
            )
        ),
        GameInfo(
            type = GameType.TILE_MATCH,
            name = "Tile Match",
            tagLine = "Triple Tray Match",
            description = "Pick tiles from the multilayer stack into your tray. Match triples to clear the board.",
            category = GameCategory.MATCHING,
            accentColor = AccentTeal,
            pastelColor = PastelCyan,
            symbolEmoji = "🀄",
            rules = listOf(
                "Tap visible tiles to move them into your tray.",
                "When 3 identical tiles enter the tray, they clear out.",
                "Keep the tray from overflowing (max 7 slots)."
            )
        )
    )

    fun getGame(type: GameType): GameInfo {
        return games.firstOrNull { it.type == type } ?: games[0]
    }
}
