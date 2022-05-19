package edu.cornell.gdiac.discodale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Class for managing save games through libGDX Preferences
 * In Disco Dale, these are "best times" on each level.
 */
public class SaveManager {
    /** Singleton save manager */
    private static SaveManager saveManager;
    /** Preferences for best times */
    private final Preferences bestTimes;

    /**
     * Return the singleton instance of the save manager
     * @return the singleton save manager
     */
    public static SaveManager getInstance() {
        if (saveManager == null) {
            saveManager = new SaveManager();
        }
        return saveManager;
    }

    private SaveManager() {
        this.bestTimes = Gdx.app.getPreferences("edu.cornell.gdiac.discodale.besttimes");
    }

    /**
     * Get best time for a level. Returns -1 if the level has never been completed.
     * @param levelName level name
     * @return the best time, or -1 if there is none
     */
    public float getBestTime(String levelName) {
        return bestTimes.getFloat(levelName, -1);
    }

    /**
     * Put a new best time for a level, saving immediately
     * Does not check if the new time is lower than previous value
     * @param levelName level name
     * @param bestTime the best time
     */
    public void putBestTime(String levelName, float bestTime) {
        bestTimes.putFloat(levelName, bestTime);
        bestTimes.flush();
    }

    /**
     * Clear all best times, erasing the save
     */
    public void clearBestTimes() {
        bestTimes.clear();
        bestTimes.flush(); // Maybe not necessary
    }
}
