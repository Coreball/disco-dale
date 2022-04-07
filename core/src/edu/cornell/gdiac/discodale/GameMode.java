/*
 * WorldController.java
 *
 * This is the most important new class in this lab.  This class serves as a combination 
 * of the CollisionController and GameplayController from the previous lab.  There is not 
 * much to do for collisions; Box2d takes care of all of that for us.  This controller 
 * invokes Box2d and then performs any after the fact modifications to the data 
 * (e.g. gameplay).
 *
 * If you study this class, and the contents of the edu.cornell.cs3152.physics.obstacles
 * package, you should be able to understand how the Physics engine works.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.discodale;

import java.util.Iterator;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;

import edu.cornell.gdiac.discodale.controllers.DaleController;
import edu.cornell.gdiac.discodale.controllers.FlyController;

import edu.cornell.gdiac.discodale.models.*;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.discodale.obstacle.*;

/**
 * Base class for a world-specific controller.
 *
 *
 * A world has its own objects, assets, and input controller. Thus this is
 * really a mini-GameEngine in its own right. The only thing that it does
 * not do is create a GameCanvas; that is shared with the main application.
 *
 * You will notice that asset loading is not done with static methods this time.
 * Instance asset loading makes it easier to process our game modes in a loop,
 * which
 * is much more scalable. However, we still want the assets themselves to be
 * static.
 * This is the purpose of our AssetState variable; it ensures that multiple
 * instances
 * place nicely with the static assets.
 */
public class GameMode implements Screen {
	private static int WIN_CODE = 1;
	private static int LOSE_CODE = -1;
	private static int PLAY_CODE = 0;

	private static int CHANGE_COLOR_TIME = 300;

	/** The texture for walls and platforms */
	protected TextureRegion earthTile;
	/** The texture for the exit condition */
	protected TextureRegion goalTile;
	/** The font for giving messages to the player */
	protected BitmapFont displayFont;

	/** Reference to the game canvas */
	protected GameCanvas canvas;
	/** All the objects in the world. */
	protected PooledList<Obstacle> objects = new PooledList<Obstacle>();
	/** Queue for adding objects */
	protected PooledList<Obstacle> addQueue = new PooledList<Obstacle>();
	/** Listener that will update the player mode when we are done */
	private ScreenListener listener;

	/** The Box2D world */
	protected World world;
	/** The boundary of the world */
	protected Rectangle bounds;
	/** The world scale */
	protected Vector2 scale;

	/** Whether or not this is an active controller */
	protected boolean active;
	/** Whether we have completed this level */
	protected boolean complete;
	/** Whether we have failed at this world (and need a reset) */
	protected boolean failed;
	/** Whether or not debug mode is active */
	protected boolean debug;
	/** Countdown active for winning or losing */
	protected int countdown;

	/** Texture asset for character avatar */
	private TextureRegion blueTexture;
	private TextureRegion greenTexture;
	private TextureRegion pinkTexture;
	private TextureRegion flyTexture;

	/** Background music */
	private Sound theme;
	private long themeId = -1;

	/** The jump sound. We only want to play once. */
	private Sound jumpSound;
	private long jumpId = -1;
	/** The default sound volume */
	private float volume;

	// Physics objects for the game
	/** Physics constants for initialization */
	private JsonValue constants;
	/** Reference to the character avatar */
	private DaleModel dale;
	private FlyModel[] flies;
	private SceneModel scene;


	private DaleController daleController;
	private CollisionController collisionController;

	/** Which value to change with the increase/decrease buttons */
	private AdjustTarget adjustTarget = AdjustTarget.GRAPPLE_SPEED;
	/** Enum for which value to change with the increase/decrease buttons */
	private enum AdjustTarget {
		GRAPPLE_SPEED,
		GRAPPLE_FORCE,
	}

	private FlyController[] flyControllers;

	private int colorChangeCountdown;

	public GameMode() {
		this(Constants.DEFAULT_WIDTH, Constants.DEFAULT_HEIGHT, Constants.DEFAULT_GRAVITY);
		setDebug(false);
		setComplete(false);
		setFailure(false);
		this.scene = new SceneModel(bounds);
	}

	/**
	 * Returns true if debug mode is active.
	 *
	 * If true, all objects will display their physics bodies.
	 *
	 * @return true if debug mode is active.
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * Sets whether debug mode is active.
	 *
	 * If true, all objects will display their physics bodies.
	 *
	 * @param value whether debug mode is active.
	 */
	public void setDebug(boolean value) {
		debug = value;
	}

	/**
	 * Returns true if the level is completed.
	 *
	 * If true, the level will advance after a countdown
	 *
	 * @return true if the level is completed.
	 */
	public boolean isComplete() {
		return complete;
	}

	/**
	 * Sets whether the level is completed.
	 *
	 * If true, the level will advance after a countdown
	 *
	 * @param value whether the level is completed.
	 */
	public void setComplete(boolean value) {
		if(failed){
			return;
		}
		if (value && countdown<0) {
			countdown = Constants.EXIT_COUNT;
		}
		complete = value;
	}

	/**
	 * Returns true if the level is failed.
	 *
	 * If true, the level will reset after a countdown
	 *
	 * @return true if the level is failed.
	 */
	public boolean isFailure() {
		return failed;
	}

	/**
	 * Sets whether the level is failed.
	 *
	 * If true, the level will reset after a countdown
	 *
	 * @param value whether the level is failed.
	 */
	public void setFailure(boolean value) {
		if(complete){
			return;
		}
		if (value && countdown<0) {
			countdown = Constants.EXIT_COUNT;
		}
		failed = value;
	}

	/**
	 * Returns true if this is the active screen
	 *
	 * @return true if this is the active screen
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Returns the canvas associated with this controller
	 *
	 * The canvas is shared across all controllers
	 *
	 * @return the canvas associated with this controller
	 */
	public GameCanvas getCanvas() {
		return canvas;
	}

	/**
	 * Sets the canvas associated with this controller
	 *
	 * The canvas is shared across all controllers. Setting this value will compute
	 * the drawing scale from the canvas size.
	 *
	 * @param canvas the canvas associated with this controller
	 */
	public void setCanvas(GameCanvas canvas) {
		this.canvas = canvas;
		this.scale.x = canvas.getWidth() / bounds.getWidth();
		this.scale.y = canvas.getHeight() / bounds.getHeight();
		this.scene.setCanvas(canvas);
	}

	/**
	 * Creates a new game world with the default values.
	 *
	 * The game world is scaled so that the screen coordinates do not agree
	 * with the Box2d coordinates. The bounds are in terms of the Box2d
	 * world, not the screen.
	 */
	// protected GameMode() {
	// this(new Rectangle(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT),
	// new Vector2(0,DEFAULT_GRAVITY));
	// }

	/**
	 * Creates a new game world
	 *
	 * The game world is scaled so that the screen coordinates do not agree
	 * with the Box2d coordinates. The bounds are in terms of the Box2d
	 * world, not the screen.
	 *
	 * @param width   The width in Box2d coordinates
	 * @param height  The height in Box2d coordinates
	 * @param gravity The downward gravity
	 */
	protected GameMode(float width, float height, float gravity) {
		this(new Rectangle(0, 0, width, height), new Vector2(0, gravity));
	}

	/**
	 * Creates a new game world
	 *
	 * The game world is scaled so that the screen coordinates do not agree
	 * with the Box2d coordinates. The bounds are in terms of the Box2d
	 * world, not the screen.
	 *
	 * @param bounds  The game bounds in Box2d coordinates
	 * @param gravity The gravitational force on this Box2d world
	 */
	protected GameMode(Rectangle bounds, Vector2 gravity) {
		world = new World(gravity, false);
		this.bounds = new Rectangle(bounds);
		this.scale = new Vector2(1, 1);
		complete = false;
		failed = false;
		debug = false;
		active = false;
		countdown = -1;

		colorChangeCountdown = CHANGE_COLOR_TIME;
	}

	/**
	 * Dispose of all (non-static) resources allocated to this mode.
	 */
	public void dispose() {
		for (Obstacle obj : objects) {
			obj.deactivatePhysics(world);
		}
		objects.clear();
		addQueue.clear();
		world.dispose();
		objects = null;
		addQueue = null;
		bounds = null;
		scale = null;
		world = null;
		canvas = null;
	}

	/**
	 *
	 * Adds a physics object in to the insertion queue.
	 *
	 * Objects on the queue are added just before collision processing. We do this
	 * to
	 * control object creation.
	 *
	 * param obj The object to add
	 */
	public void addQueuedObject(Obstacle obj) {
		assert inBounds(obj) : "Object is not in bounds";
		addQueue.add(obj);
	}

	/**
	 * Immediately adds the object to the physics world
	 *
	 * param obj The object to add
	 */
	protected void addObject(Obstacle obj) {
		assert inBounds(obj) : "Object is not in bounds";
		objects.add(obj);
		obj.activatePhysics(world);
	}

	/**
	 * Returns true if the object is in bounds.
	 *
	 * This assertion is useful for debugging the physics.
	 *
	 * @param obj The object to check.
	 *
	 * @return true if the object is in bounds.
	 */
	public boolean inBounds(Obstacle obj) {
		boolean horiz = (bounds.x <= obj.getX() && obj.getX() <= bounds.x + bounds.width);
		boolean vert = (bounds.y <= obj.getY() && obj.getY() <= bounds.y + bounds.height);
		return horiz && vert;
	}

	/**
	 * Resets the status of the game so that we can play again.
	 *
	 * This method disposes of the world and creates a new one.
	 */
	public void reset() {
		Vector2 gravity = new Vector2(world.getGravity());

		for (Obstacle obj : objects) {
			obj.deactivatePhysics(world);
		}
		scene.reset(world);
		objects.clear();
		addQueue.clear();
		world.dispose();

		world = new World(gravity, false);
		world.setContactListener(this.collisionController);
		setComplete(false);
		setFailure(false);
		countdown = -1;
		colorChangeCountdown = CHANGE_COLOR_TIME;
		populateLevel();
	}

	/**
	 * Lays out the game geography.
	 */
	private void populateLevel() {
		float dwidth = blueTexture.getRegionWidth() / scale.x;
		float dheight = blueTexture.getRegionHeight() / scale.y;
		TextureRegion[] textures = {pinkTexture, blueTexture, greenTexture};
		dale = new DaleModel(constants.get("dale"), dwidth, dheight, textures);
		dale.setDrawScale(scale);
		dale.setDaleTexture();

		Pixmap tonguePixmap = new Pixmap(5, 5, Pixmap.Format.RGBA8888);
		tonguePixmap.setColor(Color.PINK);
		tonguePixmap.fill();
		Texture tongueTexture = new Texture(tonguePixmap);
		dale.setTongueTexture(tongueTexture);
		Pixmap stickyPartPixmap = new Pixmap(11, 11, Pixmap.Format.RGBA8888);
		stickyPartPixmap.setColor(Color.PINK);
		stickyPartPixmap.fillCircle(5, 5, 5);
		Texture stickyPartTexture = new Texture(stickyPartPixmap);
		dale.setStickyPartTexture(stickyPartTexture);

		addObject(dale);


		daleController = new DaleController(this.dale);

		dwidth = flyTexture.getRegionWidth() / scale.x;
		dheight = flyTexture.getRegionHeight() / scale.y;
		flies = new FlyModel[2];
		flies[0] = new FlyModel(constants.get("fly"), 5f, 5f, dwidth, dheight, FlyModel.IdleType.STATIONARY);
		flies[0].setDrawScale(scale);
		flies[0].setTexture(flyTexture);
		addObject(flies[0]);
		flies[1] = new FlyModel(constants.get("fly"), 5f, 15f, dwidth, dheight, FlyModel.IdleType.HORIZONTAL);
		flies[1].setDrawScale(scale);
		flies[1].setTexture(flyTexture);
		addObject(flies[1]);
		flyControllers = new FlyController[2];
		for (int i = 0; i < flies.length; i++) {
			flyControllers[i] = new FlyController(flies[i], dale, scene);
		}

		collisionController = new CollisionController(this.dale, this.flies, this.scene);

		world.setContactListener(this.collisionController);

		scene.setGoalTexture(goalTile);
		scene.setWallTexture(earthTile);
		scene.populateLevel(constants.get("walls"), constants.get("platforms"), constants.get("defaults"),
				constants.get("goal"));
		scene.activatePhysics(this.world);

		JsonValue defaults = constants.get("defaults");

		// This world is heavier
		world.setGravity(new Vector2(0, defaults.getFloat("gravity", 0)));

		volume = constants.getFloat("volume", 1.0f);
	}

	/**
	 * Returns whether to process the update loop
	 *
	 * At the start of the update loop, we check if it is time
	 * to switch to a new game mode. If not, the update proceeds
	 * normally.
	 *
	 * @param dt Number of seconds since last animation frame
	 * 
	 * @return whether to process the update loop
	 */
	public boolean preUpdate(float dt) {
		InputController input = InputController.getInstance();
		input.readInput(bounds, scale);
		if (listener == null) {
			return true;
		}

		// Toggle debug
		if (input.didDebug()) {
			debug = !debug;
		}


		// Adjust values for technical prototype if buttons pressed
		if (input.didSwitchAdjust()) {
			adjustTarget = AdjustTarget.values()[(adjustTarget.ordinal() + 1) % AdjustTarget.values().length];
		}
		if (input.didIncrease() || input.didDecrease()) {
			float adjustment = 1.0f * (input.didIncrease() ? 1f : -1f);
			switch (adjustTarget) {
				case GRAPPLE_SPEED:
					dale.setStickyPartSpeed(Math.max(0, dale.getStickyPartSpeed() + adjustment));
					break;
				case GRAPPLE_FORCE:
					dale.setGrappleForce(Math.max(0, dale.getGrappleForce() + adjustment));
					break;
			}
		}
		// Show latest values if just updated
		if (input.didSwitchAdjust() || input.didIncrease() || input.didDecrease()) {
			System.out.println("Adjust target: " + adjustTarget.name());
			System.out.println("Grapple sticky part speed: " + dale.getStickyPartSpeed());
			System.out.println("Grapple force: " + dale.getGrappleForce());
			System.out.println();
		}
		
		// Handle resets
		if (input.didReset()) {
			reset();
		}

		// Now it is time to maybe switch screens.
		if (input.didExit()) {
			pause();
			listener.exitScreen(this, Constants.EXIT_QUIT);
			return false;
		} else if (input.didAdvance()) {
			pause();
			listener.exitScreen(this, Constants.EXIT_NEXT);
			return false;
		} else if (input.didRetreat()) {
			pause();
			listener.exitScreen(this, Constants.EXIT_PREV);
			return false;
		} else if (countdown > 0) {
			countdown--;
//			System.out.println(countdown);
		} else if (countdown == 0) {
			if (failed) {
				reset();
			} else if (complete) {
				pause();
				listener.exitScreen(this, Constants.EXIT_NEXT);
				return false;
			}
		}
		return true;
	}

	private DaleColor daleBackground() {
		for (ColorRegionModel c : scene.getColorRegions()) {
			if (c.shape.contains(dale.getX() * scale.x, dale.getY() * scale.y)) {
				return c.getColor();
			}
		}
		return null;
	}

	private boolean daleMatches() {
		return dale.getColor() == daleBackground();
	}

	/**
	 * The core gameplay loop of this world.
	 *
	 * This method contains the specific update code for this mini-game. It does
	 * not handle collisions, as those are managed by the parent class
	 * WorldController.
	 * This method is called after input is read, but before collisions are
	 * resolved.
	 * The very last thing that it should do is apply forces to the appropriate
	 * objects.
	 *
	 * @param dt Number of seconds since last animation frame
	 */
	public void update(float dt) {
		daleController.processMovement();
		daleController.processJumping();
		daleController.processColorRotation();
		daleController.processGrappleAction(world);
		dale.applyForce();
		dale.applyStickyPartMovement(dt);

		themeId = playBGM(theme, themeId, volume);

		if (dale.isJumping()) {
			jumpId = playSound(jumpSound, jumpId, volume);
		}

		dale.setMatch(daleMatches());

		for (int i = 0; i < flyControllers.length; i++){
			flyControllers[i].changeDirection();
			flyControllers[i].setVelocity();
		}


		int winLose = dale.getWinLose();
		if(winLose == WIN_CODE){
			setComplete(true);
//			//debugging message
//			System.out.println("win");
		}
		if(winLose == LOSE_CODE){
			setFailure(true);
//			//debugging message
//			System.out.println("lose");
		}

		if(colorChangeCountdown>0){
			colorChangeCountdown--;
		}else {
			scene.updateColorRegions();
			colorChangeCountdown = CHANGE_COLOR_TIME;
		}

		 scene.updateGrid();
	}

	/**
	 * Processes physics
	 *
	 * Once the update phase is over, but before we draw, we are ready to handle
	 * physics. The primary method is the step() method in world. This
	 * implementation
	 * works for all applications and should not need to be overwritten.
	 *
	 * @param dt Number of seconds since last animation frame
	 */
	public void postUpdate(float dt) {
		// Add any objects created by actions
		while (!addQueue.isEmpty()) {
			addObject(addQueue.poll());
		}

		// Turn the physics engine crank.
		world.step(Constants.WORLD_STEP, Constants.WORLD_VELOC, Constants.WORLD_POSIT);

		// Garbage collect the deleted objects.
		// Note how we use the linked list nodes to delete O(1) in place.
		// This is O(n) without copying.
		Iterator<PooledList<Obstacle>.Entry> iterator = objects.entryIterator();
		while (iterator.hasNext()) {
			PooledList<Obstacle>.Entry entry = iterator.next();
			Obstacle obj = entry.getValue();
			if (obj.isRemoved()) {
				obj.deactivatePhysics(world);
				entry.remove();
			} else {
				// Note that update is called last!
				obj.update(dt);
			}
		}
	}

	/**
	 * Draw the physics objects to the canvas
	 *
	 * For simple worlds, this method is enough by itself. It will need
	 * to be overriden if the world needs fancy backgrounds or the like.
	 *
	 * The method draws all objects in the order that they were added.
	 *
	 * @param dt Number of seconds since last animation frame
	 */
	public void draw(float dt) {
		canvas.clear();

		canvas.begin();
		scene.draw(canvas);
		for (Obstacle obj : objects) {
			obj.draw(canvas);
		}
		canvas.end();

		if (debug) {
			canvas.beginDebug();

			scene.drawDebug(canvas);
			for(Obstacle obj : objects) {
				obj.drawDebug(canvas);
			}
			canvas.endDebug();
		}

		// Final message

		if (complete) {
			displayFont.setColor(Color.BLACK);
			canvas.begin(); // DO NOT SCALE
			canvas.drawTextCentered("VICTORY!", displayFont, 0.0f);
			canvas.end();
		} else if (failed) {
			displayFont.setColor(Color.BROWN);
			canvas.begin(); // DO NOT SCALE
			canvas.drawTextCentered("FAILURE!", displayFont, 0.0f);
			canvas.end();
		}
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
	public long playSound(Sound sound, long soundId) {
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
	public long playSound(Sound sound, long soundId, float volume) {
		if (soundId != -1) {
			sound.stop(soundId);
		}
		return sound.play(volume);
	}

	public long playBGM(Sound sound, long soundId, float volume) {
		if (soundId != -1) {
			return soundId;
		}
		return sound.loop(volume);
	}

	/**
	 * Called when the Screen is resized.
	 *
	 * This can happen at any point during a non-paused state but will never happen
	 * before a call to show().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		// IGNORE FOR NOW
	}

	/**
	 * Called when the Screen should render itself.
	 *
	 * We defer to the other methods update() and draw(). However, it is VERY
	 * important
	 * that we only quit AFTER a draw.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	public void render(float delta) {
		if (active) {
			if (preUpdate(delta)) {
				update(delta); // This is the one that must be defined.
				postUpdate(delta);
			}
			draw(delta);
		}
	}

	/**
	 * Called when the Screen is paused.
	 * 
	 * This is usually when it's not active or visible on screen. An Application is
	 * also paused before it is destroyed.
	 */
	public void pause() {
		// TODO Auto-generated method stub
	}

	/**
	 * Called when the Screen is resumed from a paused state.
	 *
	 * This is usually when it regains focus.
	 */
	public void resume() {
		// TODO Auto-generated method stub
	}

	/**
	 * Called when this screen becomes the current screen for a Game.
	 */
	public void show() {
		// Useless if called in outside animation loop
		active = true;
	}

	/**
	 * Called when this screen is no longer the current screen for a Game.
	 */
	public void hide() {
		// Useless if called in outside animation loop
		active = false;
	}

	/**
	 * Sets the ScreenListener for this mode
	 *
	 * The ScreenListener will respond to requests to quit.
	 */
	public void setScreenListener(ScreenListener listener) {
		this.listener = listener;
	}

	/**
	 * Gather the assets for this controller.
	 *
	 * This method extracts the asset variables from the given asset directory. It
	 * should only be called after the asset directory is completed.
	 *
	 * @param directory Reference to global asset manager.
	 */
	public void gatherAssets(AssetDirectory directory) {
		blueTexture = new TextureRegion(directory.getEntry("platform:blue", Texture.class));
		greenTexture = new TextureRegion(directory.getEntry("platform:green", Texture.class));
		pinkTexture = new TextureRegion(directory.getEntry("platform:pink", Texture.class));

		flyTexture = new TextureRegion(directory.getEntry("platform:fly", Texture.class));

		jumpSound = directory.getEntry("platform:jump", Sound.class);
		theme = directory.getEntry("theme", Sound.class);

		constants = directory.getEntry("platform:constants", JsonValue.class);
		// Allocate the tiles
		earthTile = new TextureRegion(directory.getEntry("shared:earth", Texture.class));
		goalTile = new TextureRegion(directory.getEntry("shared:goal", Texture.class));
		displayFont = directory.getEntry("shared:retro", BitmapFont.class);
	}

}