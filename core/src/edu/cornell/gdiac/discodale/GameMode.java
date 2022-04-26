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

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

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

	private static int CHANGE_COLOR_TIME = 440;

	private static int FLY_SIZE = 32;

	private static int NUM_LEVELS = 10;

	private static float ZOOM_AMOUNT = 0.75f;

	/** The texture for neutral walls */
	protected TextureRegion brickTile;
	/** The texture for non-grappleable walls */
	protected TextureRegion reflectiveTile;
	/** The texture for the exit condition */
	protected TextureRegion goalTile;
	/** The font for giving messages to the player */
	protected BitmapFont displayFont;
	protected Texture background;

	/** Reference to the game canvas */
	protected GameCanvas canvas;
	/** All the objects in the world. */
	protected PooledList<Obstacle> objects = new PooledList<Obstacle>();
	/** Queue for adding objects */
	protected PooledList<Obstacle> addQueue = new PooledList<Obstacle>();
	/** Listener that will update the player mode when we are done */
	private ScreenListener listener;

	private int levelIndex;
	private boolean isNewLevel;

	private float zoomFactor;
	private float zoomValue;
	private int ticks;

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

	/** All head textures for Dale, in order of colors */
	private FilmStrip[] headTextures;
	/** All body idle textures for Dale, in order of colors */
	private TextureRegion[] bodyIdleTextures;
	/** All body walk textures for Dale, in order of colors */
	private FilmStrip[] bodyWalkTextures;

	private Texture flyIdleTexture;
	private Texture flyChaseTexture;

	private Texture[] colors = new Texture[5];

	private TextureRegion light;
	private TextureRegion darkness;

	/** Sound effects */
	private Sound died;
	private Sound extend;
	private Sound stick;

	private long diedId = -1;
	private long extendId = -1;
	private long stickId = -1;


	// TODO support colors with the split-body model
	/** Dale body texture */
	private TextureRegion pinkGrapple2Texture;
	private TextureRegion pinkIdleBody1Texture;

	/** The default sound volume */
	private float volumeBgm;
	private float volumeSfx;

	// Physics objects for the game
	/** Physics constants for initialization */
	private JsonValue constants;
	private JsonValue testlevel;

	private JsonValue[] levels = new JsonValue[NUM_LEVELS];
	/** Reference to the character avatar */
	private DaleModel dale;
	private PooledList<FlyModel> flies;
	private SceneModel scene;

	private LevelLoader levelLoader;

	private DaleController daleController;
	private CollisionController collisionController;

	private CameraState camState;

	/** Which value to change with the increase/decrease buttons */
	private AdjustTarget adjustTarget = AdjustTarget.GRAPPLE_SPEED;
	/** Enum for which value to change with the increase/decrease buttons */
	private enum AdjustTarget {
		GRAPPLE_SPEED,
		GRAPPLE_FORCE,
	}

	private LinkedList<FlyController> flyControllers;

	private int colorChangeCountdown;

	public GameMode() {
		this(Constants.DEFAULT_WIDTH, Constants.DEFAULT_HEIGHT, Constants.DEFAULT_GRAVITY);
		setDebug(false);
		setComplete(false);
		setFailure(false);
//		this.scene = new SceneModel(bounds);
	}

	public void setVolumeBgm(int volumeBgm) { this.volumeBgm = volumeBgm / 100f; }
	public void setVolumeSfx(int volumeSfx) { this.volumeSfx = volumeSfx / 100f; }

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
		this.scene.setCanvas(canvas);
		updateScale();
	}

	public void updateScale() {
//		this.scale.x = canvas.getWidth() / bounds.getWidth();
//		this.scale.y = canvas.getHeight() / bounds.getHeight();
		this.scale.x = (float) this.scene.getTileSize();
		this.scale.y = (float) this.scene.getTileSize();
//		System.out.println("gamemode scale " + this.scale);
	}

	public void setLevel(int index){
		levelIndex = index;
		isNewLevel = true;
	}

	public void nextLevel(){
		setLevel((levelIndex + 1) % NUM_LEVELS);
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
		loadLevel(levelIndex);
		isNewLevel = false;
		// this.scene = levelLoader.load(this.testlevel, constants.get("defaults"), new Rectangle(0, 0, canvas.width, canvas.height));
		this.scene.setCanvas(canvas);
		populateLevel();
	}

	/**
	 * Lays out the game geography.
	 */
	private void populateLevel() {
		float dradius = headTextures[0].getRegionHeight() / scale.x / 2f;
		float dwidth = bodyIdleTextures[0].getRegionWidth() / scale.x;
		float dheight = bodyIdleTextures[0].getRegionHeight() / scale.y;
		float bodyOffset = 10 / scale.x; // Magic number that produces offset between head and body

		DaleColor[] availableColors = scene.getPossibleColors();

		FilmStrip[] availableHeadTextures = new FilmStrip[availableColors.length];
		TextureRegion[] availableBodyIdleTextures = new TextureRegion[availableColors.length];
		FilmStrip[] availableBodyWalkTextures = new FilmStrip[availableColors.length];
		for (int i = 0; i < availableColors.length; i++) {
			int colorIndex = availableColors[i].ordinal();
			availableHeadTextures[i] = headTextures[colorIndex];
			availableBodyIdleTextures[i] = bodyIdleTextures[colorIndex];
			availableBodyWalkTextures[i] = bodyWalkTextures[colorIndex];
		}

		dale = new DaleModel(scene.getDaleStart().x, scene.getDaleStart().y, constants.get("dale"),
				dradius, dwidth, dheight, bodyOffset, availableColors, availableHeadTextures,
				availableBodyIdleTextures, availableBodyWalkTextures);
		dale.setDrawScale(scale);

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

		dwidth = FLY_SIZE / scale.x;
		dheight = FLY_SIZE / scale.y;
		flies = new PooledList<>();
		flyControllers = new LinkedList<>();
		for (Vector2 flyLocation : scene.getFlyLocations()) {
			FlyModel fly = new FlyModel(constants.get("fly"), flyLocation.x, flyLocation.y, dwidth, dheight, FlyModel.IdleType.STATIONARY);
			fly.setDrawScale(scale);
			fly.initializeTexture(flyIdleTexture, flyChaseTexture);
			flies.add(fly);
			addObject(fly);
			flyControllers.add(new FlyController(fly, dale, scene));
		}

		collisionController = new CollisionController(this.dale, this.flies, this.scene);

		world.setContactListener(this.collisionController);

		scene.activatePhysics(this.world);

		JsonValue defaults = constants.get("defaults");

		// This world is heavier
		world.setGravity(new Vector2(0, defaults.getFloat("gravity", 0)));

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
		input.setCanvas(canvas);
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

		if(input.didColor()){
			ColorRegionModel.switchDisplay();
		}

		// Handle resets
		if (input.didReset()) {
			reset();
		}

		// Now it is time to maybe switch screens.
		if (input.didPause()) {
			pause();
			listener.exitScreen(this, Constants.EXIT_PAUSE);
			return false;
		} else if (input.didMenu()){
			pause();
			listener.exitScreen(this, Constants.EXIT_MENU);
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
				listener.exitScreen(this, Constants.EXIT_COMPLETE);
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

	public CameraState getCameraState() {return camState;}

	public void setCameraState(CameraState state) {camState = state;}

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
//		daleController.processMovement();
//		daleController.processColorRotation();
//		daleController.processGrappleAction(world);
		dale.applyForce();
		dale.applyStickyPartMovement(dt);

		dale.setMatch(daleMatches());
		switch (daleController.sfx){
			case TONGUE_EXTEND:
				extendId = SoundPlayer.playSound(extend, extendId, volumeSfx);
				break;
			case TONGUE_STICK:
				stickId = SoundPlayer.playSound(stick, stickId, volumeSfx);
				break;
		}

		switch (getCameraState()) {
			case START:
				zoomValue = Math.max(
						this.bounds.getWidth() * this.scene.getTileSize() / this.canvas.getWidth(),
						this.bounds.getHeight() * this.scene.getTileSize() / this.canvas.getHeight()
				);
				canvas.updateCam(
						(float) canvas.getWidth() / 2,
						(float) canvas.getHeight() / 2,
						zoomValue,
						this.bounds,
						this.scene.getTileSize()
				);
				if (ticks >= 55) {
					zoomFactor = (zoomValue - ZOOM_AMOUNT) / 60;
					setCameraState(CameraState.ZOOM);
				} else {
					ticks++;
				}
				break;
			case ZOOM:
				if (canvas.getCameraZoom() <= ZOOM_AMOUNT) {
					setCameraState(CameraState.PLAY);
				} else {
					float zoom = canvas.getCameraZoom();
					canvas.updateCam(
							dale.getX() * scale.x,
							dale.getY() * scale.y,
							zoom - zoomFactor,
							this.bounds,
							this.scene.getTileSize()
					);
					scene.updateGrid();
				}
				break;
			case PLAY:
				canvas.updateCam(
						dale.getX() * scale.x,
						dale.getY() * scale.y,
						ZOOM_AMOUNT,
						this.bounds,
						this.scene.getTileSize()
				);

				daleController.processMovement();
				daleController.processColorRotation();
				daleController.processGrappleAction(world);
				dale.applyForce();
				dale.applyStickyPartMovement(dt);

				for (FlyController flyController : flyControllers) {
					flyController.changeDirection();
					flyController.setVelocity();
				}

				if (colorChangeCountdown > 0) {
					colorChangeCountdown--;
				} else {
					colorChangeCountdown = CHANGE_COLOR_TIME;
					scene.updateColorRegions();
				}

				if (dale.getY() * scale.y < -150) {
					reset();
				}

				scene.updateGrid();
				scene.updateColorRegionMovement();
				break;
		}

		int winLose = dale.getWinLose();

		if(winLose == WIN_CODE){
			setComplete(true);
		}

		if(winLose == LOSE_CODE){
			setFailure(true);
		}

		class FixtureAndDistance{
			public Fixture fixture;
			public float distance;
			public FixtureAndDistance(Fixture fixture,float distance){
				this.fixture = fixture;
				this.distance = distance;
			}
		}

		class CustomizedRayCastCallBack implements RayCastCallback{
			public LinkedList<FixtureAndDistance> fixtureAndDistances = new LinkedList<>();
			public void reset(){
				fixtureAndDistances = new LinkedList<>();
			}
			@Override
			public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
				float diffX = Math.abs(point.x - dale.getX());
				float diffY = Math.abs(point.y - dale.getY());
				float distance = (float) Math.sqrt((double)(diffX*diffX) + (double)(diffY*diffY));
				fixtureAndDistances.add(new FixtureAndDistance(fixture,distance));

				return 1;
			}
		}

		CustomizedRayCastCallBack callback = new CustomizedRayCastCallBack();

		if(scene.isRealSightMode()){
			for(FlyController f:flyControllers){
				f.setSeeDaleInRealWorld(false);
			}
			for(FlyController f:flyControllers){
				FlyModel fly = f.getFly();
//				System.out.println("startRay");
//				System.out.println(dale.getX() +"  "+ dale.getY() +"  "+fly.getX() +"  "+fly.getY());
				callback.reset();
				world.rayCast(callback,dale.getX(),dale.getY(),fly.getX(),fly.getY());
				LinkedList<FixtureAndDistance> fixtureAndDistances = callback.fixtureAndDistances;
				Collections.sort(fixtureAndDistances,new Comparator<FixtureAndDistance>(){
					public int compare(FixtureAndDistance f1,FixtureAndDistance f2){
						if(f1.distance>f2.distance){
							return 1;
						}
						else if(f1.distance==f2.distance){
							return 0;
						}else{
							return -1;
						}
					}
				});
				for(FixtureAndDistance fd: fixtureAndDistances){
					Fixture fixture = fd.fixture;
					boolean isFly = false;
					for(FlyController ff:flyControllers){
						FlyModel flyModel = ff.getFly();
						for(Fixture flyModelFixture: flyModel.getBody().getFixtureList()){
							if (fixture==flyModelFixture){
//								System.out.println(flyModel.getX() +"  "+flyModel.getY() + fd.distance);
								isFly = true;
								ff.setSeeDaleInRealWorld(true);
							}
						}
					}
					if(!isFly){
						if(!dale.checkFixtureInDale(fixture)){
//							System.out.println("Distance to obstacle: "+fd.distance);
							break;
						}
					}
				}
//				System.out.println("endRay");
			}
//			for(FlyController f:flyControllers){
//				if(f.getSeeDaleInRealWorld()){
//					System.out.println("Can see");
//				}
//			}

		}

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
		canvas.draw(background, Color.WHITE,0, 0, scene.getBounds().getWidth() * scene.getTileSize(),
				scene.getBounds().getHeight() * scene.getTileSize());
		scene.draw(canvas);

		for (Obstacle obj : objects) {
			obj.draw(canvas);
		}
		canvas.end();

		if(scene.isDarkMode()){
			// Draw light
			canvas.beginLight();
			float ch = canvas.getHeight();
			float cw = canvas.getWidth();
			float h = light.getRegionHeight();
			float w = light.getRegionWidth();
			// Some magic number to determine the size of the light. Original size of light: 64 x 64.
			float lightScale = 2f;
			canvas.draw(light,new Color(256,256,256,0f),w*lightScale/2f,h*lightScale/2f,dale.getX()*scale.x,dale.getY()*scale.y,w*lightScale,h*lightScale);
			canvas.endLight();

			// Draw darkness around light
			canvas.beginLight2();
			canvas.draw(darkness,new Color(256,256,256,0.5f),cw/2f,ch/2f,canvas.getCameraX(),canvas.getCameraY(),cw,ch);
			canvas.endLight();
		}

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
			canvas.drawText("VICTORY!", displayFont, (dale.getX() * scale.x) - 130, (dale.getY() * scale.y) + 50);
			canvas.end();
		} else if (failed) {
			diedId = SoundPlayer.playSound(died, diedId, volumeSfx);
			displayFont.setColor(Color.BLACK);
			canvas.begin(); // DO NOT SCALE
			canvas.drawText("FAILURE!", displayFont, (dale.getX() * scale.x) - 130, (dale.getY() * scale.y) + 50);
			canvas.end();
		}
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
		canvas.updateCam(canvas.getWidth() /2,canvas.getHeight()/2, 1.0f, this.bounds, this.scene.getTileSize());
	}

	/**
	 * Called when the Screen is resumed from a paused state.
	 *
	 * This is usually when it regains focus.
	 */
	public void resume() {
		// TODO Auto-generated method stub
		canvas.updateCam(dale.getX() * scale.x, dale.getY() * scale.y, 0.75f, this.bounds, this.scene.getTileSize());
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
		bodyIdleTextures = new TextureRegion[]{
				new TextureRegion(directory.getEntry("platform:body:idle:pink", Texture.class)),
				new TextureRegion(directory.getEntry("platform:body:idle:blue", Texture.class)),
				new TextureRegion(directory.getEntry("platform:body:idle:green", Texture.class))
		};

		headTextures = new FilmStrip[]{
				new FilmStrip(directory.getEntry("platform:head:pink", Texture.class), 1, 3),
				new FilmStrip(directory.getEntry("platform:head:blue", Texture.class), 1, 3),
				new FilmStrip(directory.getEntry("platform:head:green", Texture.class), 1, 3)
		};

		bodyWalkTextures = new FilmStrip[]{
				new FilmStrip(directory.getEntry("platform:body:walk:pink", Texture.class), 1, 10),
				new FilmStrip(directory.getEntry("platform:body:walk:blue", Texture.class), 1, 10),
				new FilmStrip(directory.getEntry("platform:body:walk:green", Texture.class), 1, 10),
		};

		flyIdleTexture = directory.getEntry("platform:flyidle", Texture.class);
		flyChaseTexture = directory.getEntry("platform:flychasing", Texture.class);

		light = new TextureRegion(directory.getEntry("platform:light",Texture.class));
		darkness = new TextureRegion(directory.getEntry("platform:darkness",Texture.class));

		constants = directory.getEntry("platform:constants", JsonValue.class);
		// Allocate the tiles
		brickTile = new TextureRegion(directory.getEntry("shared:brick", Texture.class));
		reflectiveTile = new TextureRegion(directory.getEntry("shared:reflective", Texture.class));
		goalTile = new TextureRegion(directory.getEntry("shared:goal", Texture.class));
		displayFont = directory.getEntry("shared:alienitalic", BitmapFont.class);
		background = directory.getEntry("menu:bg", Texture.class);

		died = directory.getEntry("died", Sound.class);
		extend = directory.getEntry("extend", Sound.class);
		stick = directory.getEntry("stick", Sound.class);

		colors[0] = directory.getEntry("platform:pinkcolor", Texture.class);
		colors[1] = directory.getEntry("platform:bluecolor", Texture.class);
		colors[2] = directory.getEntry("platform:greencolor", Texture.class);
		colors[3] = directory.getEntry("platform:purplecolor", Texture.class);
		colors[4] = directory.getEntry("platform:orangecolor", Texture.class);
		ColorRegionModel.setColorTexture(colors);



		this.testlevel = directory.getEntry("testlevel", JsonValue.class);

		for (int i = 0; i < NUM_LEVELS; i++){
			levels[i] = directory.getEntry("level" + Integer.toString(i + 1), JsonValue.class);
		}

		this.levelLoader = new LevelLoader(brickTile, reflectiveTile, goalTile, this.bounds.getWidth(), this.bounds.getHeight());
		// loadLevel(levelIndex);
		this.scene = levelLoader.load(this.testlevel, constants.get("defaults"));
	}

	private void loadLevel(int index) {
		if (levels[index] != null) {
			this.scene = levelLoader.load(levels[index], constants.get("defaults"));
		} else {
			this.scene = levelLoader.load(testlevel, constants.get("defaults"));
		}
		this.bounds = new Rectangle(scene.getBounds());
		updateScale();
		ticks = 0;
		if (isNewLevel)
			setCameraState(CameraState.START);
		else
			setCameraState(CameraState.PLAY);
	}

}