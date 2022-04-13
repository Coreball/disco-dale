package edu.cornell.gdiac.discodale;

public class Constants {
    public static final String DALE_GROUND_SENSOR_NAME = "DaleGroundSensor";
    public static String DALE_NAME_TAG = "dale";
    /** Exit code for quitting the game */
    public static final int EXIT_QUIT = 0;
    /** Exit code for advancing to next level */
    public static final int EXIT_NEXT = 1;
    /** Exit code for jumping back to previous level */
    public static final int EXIT_PREV = 2;
    /** Exit code for going to the levels */
    public static final int EXIT_LEVEL = 3;
    /** Exit code for going to the menu */
    public static final int EXIT_MENU = 4;
    /** How many frames after winning/losing do we continue? */
    public static final int EXIT_COUNT = 120;
    /** The amount of time for a physics engine step. */
    public static final float WORLD_STEP = 1/60.0f;
    /** Number of velocity iterations for the constrain solvers */
    public static final int WORLD_VELOC = 6;
    /** Number of position iterations for the constrain solvers */
    public static final int WORLD_POSIT = 2;
    /** Width of the game world in Box2d units */
    public static final float DEFAULT_WIDTH  = 32.0f;
    /** Height of the game world in Box2d units */
    public static final float DEFAULT_HEIGHT = 18.0f;
    /** The default value of gravity (going down) */
    public static final float DEFAULT_GRAVITY = -4.9f;
}
