/*
 * GDXRoot.java
 *
 * This is the primary class file for running the game.  It is the "static main" of
 * LibGDX.  In the first lab, we extended ApplicationAdapter.  In previous lab
 * we extended Game.  This is because of a weird graphical artifact that we do not
 * understand.  Transparencies (in 3D only) is failing when we use ApplicationAdapter. 
 * There must be some undocumented OpenGL code in setScreen.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * Updated asset version, 2/6/2021
 */
 package edu.cornell.gdiac.discodale;

import com.badlogic.gdx.*;
import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.assets.*;

/**
 * Root class for a LibGDX.  
 * 
 * This class is technically not the ROOT CLASS. Each platform has another class above
 * this (e.g. PC games use DesktopLauncher) which serves as the true root.  However, 
 * those classes are unique to each platform, while this class is the same across all 
 * plaforms. In addition, this functions as the root class all intents and purposes, 
 * and you would draw it as a root class in an architecture specification.  
 */
public class GDXRoot extends Game implements ScreenListener {
	/** AssetManager to load game assets (textures, sounds, etc.) */
	AssetDirectory directory;
	/** Drawing context to display graphics (VIEW CLASS) */
	private GameCanvas canvas; 
	/** Player mode for the asset loading screen (CONTROLLER CLASS) */
	private LoadingMode loading;
	/** Player mode for the game proper (CONTROLLER CLASS) */
	private int current;

	private GameMode controller;

	private MenuMode menu;
	
	/**
	 * Creates a new game from the configuration settings.
	 *
	 * This method configures the asset manager, but does not load any assets
	 * or assign any screen.
	 */
	public GDXRoot() { }

	/** 
	 * Called when the Application is first created.
	 * 
	 * This is method immediately loads assets for the loading screen, and prepares
	 * the asynchronous loader for all other assets.
	 */
	public void create() {
		canvas  = new GameCanvas();
		loading = new LoadingMode("assets.json",canvas,1);
		menu = new MenuMode(canvas);
		// Initialize the three game worlds
		controller = new GameMode();
		current = 0;
		loading.setScreenListener(this);
		setScreen(loading);
	}

	/** 
	 * Called when the Application is destroyed. 
	 *
	 * This is preceded by a call to pause().
	 */
	public void dispose() {
		// Call dispose on our children
		setScreen(null);
		controller.dispose();

		canvas.dispose();
		canvas = null;
	
		// Unload all of the resources
		// Unload all of the resources
		if (directory != null) {
			directory.unloadAssets();
			directory.dispose();
			directory = null;
		}
		super.dispose();
	}
	
	/**
	 * Called when the Application is resized. 
	 *
	 * This can happen at any point during a non-paused state but will never happen 
	 * before a call to create().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		//canvas.resizeWindow(controller.bounds);
		canvas.resize();
		super.resize(width,height);
	}
	
	/**
	 * The given screen has made a request to exit its player mode.
	 *
	 * The value exitCode can be used to implement menu options.
	 *
	 * @param screen   The screen requesting to exit
	 * @param exitCode The state of the screen upon exit
	 */
	public void exitScreen(Screen screen, int exitCode) {
		if (screen == loading) {
			directory = loading.getAssets();
			menu.gatherAssets(directory);
			menu.setCanvas(canvas);
			menu.setScreenListener(this);
			setScreen(menu);
			loading.dispose();
			loading = null;
		} else if (exitCode == Constants.EXIT_LEVEL){
			menu.hide();
			controller.gatherAssets(directory);
			controller.setCanvas(canvas);
			controller.setVolumeBgm(menu.getVolumeBgm());
			controller.setVolumeSfx(menu.getVolumeSfx());
			controller.setScreenListener(this);
			if(menu.getLevel() != -1)
				controller.setLevel(menu.getLevel());
			controller.reset();
			setScreen(controller);
		} else if (exitCode == Constants.EXIT_MENU) {
			controller.hide();
			menu.setType(MenuMode.Type.START);
			setScreen(menu);
		} else if (exitCode == Constants.EXIT_COMPLETE) {
			controller.pause();
			menu.setType(MenuMode.Type.LEVEL_COMPLETE);
			menu.setCompletedLevelTime(controller.getLevelTime());
			menu.setShowNewBestTime(controller.wasNewBestTime());
			menu.setLevel(controller.getLevel());
			setScreen(menu);
		} else if (exitCode == Constants.EXIT_NEXT) {
			controller.setVolumeBgm(menu.getVolumeBgm());
			controller.setVolumeSfx(menu.getVolumeSfx());
			controller.nextLevel();
			menu.setLevel(controller.getLevel());
			controller.reset();
			setScreen(controller);
		} else if (exitCode == Constants.EXIT_PREV) {
//			current = (current+ controller.length-1) % controller.length;
			controller.setVolumeBgm(menu.getVolumeBgm());
			controller.setVolumeSfx(menu.getVolumeSfx());
			controller.reset();
			setScreen(controller);
		} else if (exitCode == Constants.EXIT_PAUSE) {
			controller.pause();
			menu.setType(MenuMode.Type.PAUSE);
			setScreen(menu);
		} else if (exitCode == Constants.EXIT_RESUME) {
			controller.setVolumeBgm(menu.getVolumeBgm());
			controller.setVolumeSfx(menu.getVolumeSfx());
			controller.resume();
			setScreen(controller);
		} else if (exitCode == Constants.EXIT_QUIT) {
			// We quit the main application
			Gdx.app.exit();
			System.exit(0);
		}
	}

}
