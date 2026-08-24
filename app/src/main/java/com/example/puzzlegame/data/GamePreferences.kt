package com.example.puzzlegame.data

import android.content.Context
import android.content.SharedPreferences

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("PuzzleGamePrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_VOLUME = "setting_volume"
        private const val KEY_DARK_MODE = "setting_dark_mode"

        private const val KEY_HAS_CONTINUE = "continue_has_game"
        private const val KEY_CONTINUE_TYPE = "continue_game_type"
        private const val KEY_CONTINUE_DATA = "continue_game_data"
    }

    // --- Settings ---
    var volume: Float
        get() = prefs.getFloat(KEY_VOLUME, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_VOLUME, value).apply()

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, true) // Mặc định là Dark Mode như yêu cầu cao cấp
        set(value) = prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()

    // --- Progression levels ---
    var sokobanUnlockedLevel: Int
        get() = prefs.getInt("sokoban_unlocked_level", 1)
        set(value) = prefs.edit().putInt("sokoban_unlocked_level", value).apply()

    var sokobanAllUnlocked: Boolean
        get() = prefs.getBoolean("sokoban_all_unlocked", false)
        set(value) = prefs.edit().putBoolean("sokoban_all_unlocked", value).apply()

    var nonogramUnlockedLevel: Int
        get() = prefs.getInt("nonogram_unlocked_level", 1)
        set(value) = prefs.edit().putInt("nonogram_unlocked_level", value).apply()

    // --- Shared Profile System ---
    var profileLevel: Int
        get() = prefs.getInt("profile_level", 1)
        set(value) = prefs.edit().putInt("profile_level", value).apply()

    var profileXp: Int
        get() = prefs.getInt("profile_xp", 0)
        set(value) = prefs.edit().putInt("profile_xp", value).apply()

    var profileCoins: Int
        get() = prefs.getInt("profile_coins", 0)
        set(value) = prefs.edit().putInt("profile_coins", value).apply()

    var profileStreak: Int
        get() = prefs.getInt("profile_streak", 0)
        set(value) = prefs.edit().putInt("profile_streak", value).apply()

    var unlockedCollectionIds: Set<String>
        get() = prefs.getStringSet("unlocked_collection_ids", emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet("unlocked_collection_ids", value).apply()

    var unlockedAchievements: Set<String>
        get() = prefs.getStringSet("unlocked_achievements", emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet("unlocked_achievements", value).apply()

    fun addXpAndCoins(xpGained: Int, coinsGained: Int): Boolean {
        var currentLevel = profileLevel
        var currentXp = profileXp + xpGained
        profileCoins = profileCoins + coinsGained
        
        var leveledUp = false
        while (currentXp >= currentLevel * 1000) {
            currentXp -= currentLevel * 1000
            currentLevel++
            leveledUp = true
        }
        
        profileLevel = currentLevel
        profileXp = currentXp
        return leveledUp
    }

    // --- Scores ---
    fun getHighScore(gameKey: String): Int {
        return prefs.getInt("highscore_$gameKey", 0)
    }

    fun saveScore(gameKey: String, score: Int) {
        val currentHigh = getHighScore(gameKey)
        if (score > currentHigh) {
            prefs.edit().putInt("highscore_$gameKey", score).apply()
        }
        prefs.edit().putInt("lastscore_$gameKey", score).apply()
    }

    fun getLastScore(gameKey: String): Int {
        return prefs.getInt("lastscore_$gameKey", 0)
    }

    // --- Continue Game ---
    fun hasContinueGame(): Boolean {
        return prefs.getBoolean(KEY_HAS_CONTINUE, false)
    }

    fun getContinueGameType(): String? {
        return prefs.getString(KEY_CONTINUE_TYPE, null)
    }

    fun getContinueGameData(): String? {
        return prefs.getString(KEY_CONTINUE_DATA, null)
    }

    fun saveContinueGame(type: String, data: String) {
        prefs.edit()
            .putBoolean(KEY_HAS_CONTINUE, true)
            .putString(KEY_CONTINUE_TYPE, type)
            .putString(KEY_CONTINUE_DATA, data)
            .apply()
    }

    fun clearContinueGame() {
        prefs.edit()
            .putBoolean(KEY_HAS_CONTINUE, false)
            .remove(KEY_CONTINUE_TYPE)
            .remove(KEY_CONTINUE_DATA)
            .apply()
    }

    // --- Helpers to serialize/deserialize grids ---
    fun serializeGrid(grid: Array<IntArray>): String {
        return grid.joinToString(";") { row -> row.joinToString(",") }
    }

    fun deserializeGrid(str: String): Array<IntArray> {
        val rows = str.split(";")
        return Array(rows.size) { r ->
            val cols = rows[r].split(",")
            IntArray(cols.size) { c -> cols[c].toInt() }
        }
    }
}
