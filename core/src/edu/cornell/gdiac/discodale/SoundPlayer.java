package edu.cornell.gdiac.discodale;

import com.badlogic.gdx.audio.Sound;

/** Helper methods for playing sounds */
public class SoundPlayer {


    public static long loopSound(Sound sound, long soundId, float volume) {
        if (soundId != -1) {
            sound.setVolume(soundId, volume);
            return soundId;
        }
        return sound.loop(volume);
    }

    /**
     * Method to ensure that a sound asset is only played once.
     *
     * Every time you play a sound asset, it makes a new instance of that sound.
     * If you play the sounds to close together, you will have overlapping copies.
     * To prevent that, you must stop the sound before you play it again. That
     * is the purpose of this method. It stops the current instance playing (if
     * any) and then returns the id of the new instance for tracking.
     *
     * @param sound   The sound asset to play
     * @param soundId The previously playing sound instance
     *
     * @return the new sound instance for this asset.
     */
    public static long playSound(Sound sound, long soundId) {
        return playSound(sound, soundId, 1.0f);
    }

    /**
     * Method to ensure that a sound asset is only played once.
     *
     * Every time you play a sound asset, it makes a new instance of that sound.
     * If you play the sounds to close together, you will have overlapping copies.
     * To prevent that, you must stop the sound before you play it again. That
     * is the purpose of this method. It stops the current instance playing (if
     * any) and then returns the id of the new instance for tracking.
     *
     * @param sound   The sound asset to play
     * @param soundId The previously playing sound instance
     * @param volume  The sound volume
     *
     * @return the new sound instance for this asset.
     */
    public static long playSound(Sound sound, long soundId, float volume) {
        if (soundId != -1) {
            sound.stop(soundId);
        }
        return sound.play(volume);
    }
}
