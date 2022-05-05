/*
 * LoadingMode.java
 *
 * Asset loading is a really tricky problem.  If you have a lot of sound or images,
 * it can take a long time to decompress them and load them into memory.  If you just
 * have code at the start to load all your assets, your game will look like it is hung
 * at the start.
 *
 * The alternative is asynchronous asset loading.  In asynchronous loading, you load a
 * little bit of the assets at a time, but still animate the game while you are loading.
 * This way the player knows the game is not hung, even though he or she cannot do 
 * anything until loading is complete. You know those loading screens with the inane tips 
 * that want to be helpful?  That is asynchronous loading.  
 *
 * This player mode provides a basic loading screen.  While you could adapt it for
 * between level loading, it is currently designed for loading all assets at the 
 * start of the game.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * Updated asset version, 2/6/2021
 */
package edu.cornell.gdiac.discodale;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.*;

import com.badlogic.gdx.math.Rectangle;
import edu.cornell.gdiac.assets.*;
import edu.cornell.gdiac.util.*;

import java.awt.*;

/**
 * Class that provides a loading screen for the state of the game.
 *
 * You still DO NOT need to understand this class for this lab.  We will talk about this
 * class much later in the course.  This class provides a basic template for a loading
 * screen to be used at the start of the game or between levels.  Feel free to adopt
 * this to your needs.
 *
 * You will note that this mode has some textures that are not loaded by the AssetManager.
 * You are never required to load through the AssetManager.  But doing this will block
 * the application.  That is why we try to have as few resources as possible for this
 * loading screen.
 */
public class LoadingMode implements Screen {
	// There are TWO asset managers.  One to load the loading screen.  The other to load the assets
	/**
	 * Internal assets for this loading screen
	 */
	private AssetDirectory internal;
	/**
	 * The actual assets to be loaded
	 */
	private AssetDirectory assets;

	/**
	 * Background texture for start-up
	 */
	private Texture background;

	/**
	 * Dale's texture for start-up
	 */
	private Texture dale;
	private Texture tongue;
	private Texture sticky;

	/**
	 * Default budget for asset loader (do nothing but load 60 fps)
	 */
	private static int DEFAULT_BUDGET = 15;
	/**
	 * Standard window size (for scaling)
	 */
	private static int STANDARD_WIDTH = 1920;
	/**
	 * Standard window height (for scaling)
	 */
	private static int STANDARD_HEIGHT = 1080;

	private static final int DALE_OFFSET_X = 0;
	private static final int DALE_OFFSET_Y = 175;

	/**
	 * Reference to GameCanvas created by the root
	 */
	private GameCanvas canvas;
	/**
	 * Listener that will update the player mode when we are done
	 */
	private ScreenListener listener;

	/**
	 * Scaling factor for when the student changes the resolution.
	 */
	private float scale;
	private float sx = 1f, sy = 1f;

	/**
	 * Current progress (0 to 1) of the asset manager
	 */
	private float progress;

	/**
	 * The amount of time to devote to loading assets (as opposed to on screen hints, etc.)
	 */
	private int budget;

	/**
	 * Whether or not this player mode is still active
	 */
	private boolean active;

	/**
	 * Returns the budget for the asset loader.
	 * <p>
	 * The budget is the number of milliseconds to spend loading assets each animation
	 * frame.  This allows you to do something other than load assets.  An animation
	 * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
	 * do something else.  This is how game companies animate their loading screens.
	 *
	 * @return the budget in milliseconds
	 */
	public int getBudget() {
		return budget;
	}

	/**
	 * Sets the budget for the asset loader.
	 * <p>
	 * The budget is the number of milliseconds to spend loading assets each animation
	 * frame.  This allows you to do something other than load assets.  An animation
	 * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
	 * do something else.  This is how game companies animate their loading screens.
	 *
	 * @param millis the budget in milliseconds
	 */
	public void setBudget(int millis) {
		budget = millis;
	}

	/**
	 * Returns true if all assets are loaded and the player is ready to go.
	 *
	 * @return true if the player is ready to go
	 */
	public boolean isReady() {
		return progress == 1f;
	}

	/**
	 * Returns the asset directory produced by this loading screen
	 * <p>
	 * This asset loader is NOT owned by this loading scene, so it persists even
	 * after the scene is disposed.  It is your responsbility to unload the
	 * assets in this directory.
	 *
	 * @return the asset directory produced by this loading screen
	 */
	public AssetDirectory getAssets() {
		return assets;
	}

	/**
	 * Creates a LoadingMode with the default budget, size and position.
	 *
	 * @param file   The asset directory to load in the background
	 * @param canvas The game canvas to draw to
	 */
	public LoadingMode(String file, GameCanvas canvas) {
		this(file, canvas, DEFAULT_BUDGET);
	}

	/**
	 * Creates a LoadingMode with the default size and position.
	 * <p>
	 * The budget is the number of milliseconds to spend loading assets each animation
	 * frame.  This allows you to do something other than load assets.  An animation
	 * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
	 * do something else.  This is how game companies animate their loading screens.
	 *
	 * @param file   The asset directory to load in the background
	 * @param canvas The game canvas to draw to
	 * @param millis The loading budget in milliseconds
	 */
	public LoadingMode(String file, GameCanvas canvas, int millis) {
		this.canvas = canvas;
		budget = millis;

		// Compute the dimensions from the canvas
		resize(canvas.getWidth(), canvas.getHeight());
		canvas.updateCam(canvas.getWidth()/2, canvas.getHeight()/2, 1/sx,
				new Rectangle(0, 0, 32, 18), 32);

		// We need these files loaded immediately
		internal = new AssetDirectory("loading.json");
		internal.loadAssets();
		internal.finishLoading();

		// Load the next two images immediately.
		background = internal.getEntry("background", Texture.class);
		background.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		dale = internal.getEntry("dale", Texture.class);

		// No progress so far.
		progress = 0;

		Pixmap tonguePixmap = new Pixmap(10, 10, Pixmap.Format.RGBA8888);
		tonguePixmap.setColor(Color.PINK);
		tonguePixmap.fill();
		tongue = new Texture(tonguePixmap);

		Pixmap stickyPartPixmap = new Pixmap(24, 24, Pixmap.Format.RGBA8888);
		stickyPartPixmap.setColor(Color.PINK);
		stickyPartPixmap.fillCircle(12, 12, 10);
		sticky = new Texture(stickyPartPixmap);

		// Start loading the real assets
		assets = new AssetDirectory(file);
		assets.loadAssets();
		active = true;
	}

	/**
	 * Called when this screen should release all resources.
	 */
	public void dispose() {
		internal.unloadAssets();
		internal.dispose();
	}

	/**
	 * Update the status of this player mode.
	 * <p>
	 * We prefer to separate update and draw from one another as separate methods, instead
	 * of using the single render() method that LibGDX does.  We will talk about why we
	 * prefer this in lecture.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	private void update(float delta) {
		assets.update(budget);
		this.progress = assets.getProgress();
		if (progress >= 1.0f) {
			this.progress = 1.0f;
		}
	}

	/**
	 * Draw the status of this player mode.
	 * <p>
	 * We prefer to separate update and draw from one another as separate methods, instead
	 * of using the single render() method that LibGDX does.  We will talk about why we
	 * prefer this in lecture.
	 */
	private void draw() {
		canvas.begin();
		canvas.draw(background, Color.WHITE,0, 0, canvas.getWidth(), canvas.getHeight());
		drawProgress(canvas);
		canvas.draw(dale, Color.WHITE, 0, 0, DALE_OFFSET_X * sx, DALE_OFFSET_Y * sy, 0, sx, sy);
		canvas.end();
	}

	/**
	 * Updates the progress bar according to loading progress
	 * <p>
	 * The progress bar is composed of parts: two rounded caps on the end,
	 * and a rectangle in a middle.  We adjust the size of the rectangle in
	 * the middle to represent the amount of progress.
	 *
	 * @param canvas The drawing context
	 */
	private void drawProgress(GameCanvas canvas) {
		canvas.draw(tongue, Color.WHITE, (DALE_OFFSET_X + dale.getWidth() / 2f) * sx,
				(DALE_OFFSET_Y + dale.getHeight() / 2f) * sy,
				1300 * progress * sx, tongue.getHeight() * sy);
		canvas.draw(sticky, Color.WHITE, 0, 0,
				(DALE_OFFSET_X + dale.getWidth() / 2f + 1300 * progress - sticky.getWidth()/2f) * sx,
				(DALE_OFFSET_Y + dale.getHeight() / 2f + tongue.getHeight()/2f - sticky.getHeight()/2f) * sy,
				0, sx, sy);

	}

	// ADDITIONAL SCREEN METHODS

	/**
	 * Called when the Screen should render itself.
	 * <p>
	 * We defer to the other methods update() and draw().  However, it is VERY important
	 * that we only quit AFTER a draw.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	public void render(float delta) {
		if (active) {
			update(delta);
			draw();

			// We are are ready, notify our listener
			if (isReady() && listener != null) {
				listener.exitScreen(this, 0);
			}
		}
	}

	/**
	 * Called when the Screen is resized.
	 * <p>
	 * This can happen at any point during a non-paused state but will never happen
	 * before a call to show().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		// Compute the drawing scale
		sx = ((float) width) / STANDARD_WIDTH;
		sy = ((float) height) / STANDARD_HEIGHT;
		scale = (sx < sy ? sx : sy);

	}

	/**
	 * Called when the Screen is paused.
	 * <p>
	 * This is usually when it's not active or visible on screen. An Application is
	 * also paused before it is destroyed.
	 */
	public void pause() {
		// TODO Auto-generated method stub

	}

	/**
	 * Called when the Screen is resumed from a paused state.
	 * <p>
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
	 * <p>
	 * The ScreenListener will respond to requests to quit.
	 */
	public void setScreenListener(ScreenListener listener) {
		this.listener = listener;
	}

}