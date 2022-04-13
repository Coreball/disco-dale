/*
 * InputController.java
 *
 * This class buffers in input from the devices and converts it into its
 * semantic meaning. If your game had an option that allows the player to
 * remap the control keys, you would store this information in this class.
 * That way, the main GameEngine does not have to keep track of the current
 * key mapping.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.discodale;

import com.badlogic.gdx.*;
import com.badlogic.gdx.math.*;

import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.util.*;

/**
 * Class for reading player input. 
 *
 * This supports both a keyboard and X-Box controller. In previous solutions, we only 
 * detected the X-Box controller on start-up.  This class allows us to hot-swap in
 * a controller via the new XBox360Controller class.
 */
public class InputController {
	// Sensitivity for moving crosshair with gameplay
	private static final float GP_ACCELERATE = 1.0f;
	private static final float GP_MAX_SPEED  = 10.0f;
	private static final float GP_THRESHOLD  = 0.01f;

	/** The singleton instance of the input controller */
	private static InputController theController = null;

	private static GameCanvas canvas = null;
	
	/** 
	 * Return the singleton instance of the input controller
	 *
	 * @return the singleton instance of the input controller
	 */
	public static InputController getInstance() {
		if (theController == null) {
			theController = new InputController();
		}
		return theController;
	}
	
	// Fields to manage buttons
	/** Whether the reset button was pressed. */
	private boolean resetPressed;
	private boolean resetPrevious;
	/** Whether the button to advanced worlds was pressed. */
	private boolean nextPressed;
	private boolean nextPrevious;
	/** Whether the button to step back worlds was pressed. */
	private boolean prevPressed;
	private boolean prevPrevious;
	/** Whether the rotating color action button was pressed. */
	private boolean rotateColorPressed;
	private boolean rotateColorPrevious;
	/** Whether the click action button was pressed. */
	private boolean clickPressed;
	private boolean clickPrevious;
	/** Whether the debug toggle was pressed. */
	private boolean debugPressed;
	private boolean debugPrevious;
	/** Whether the exit button was pressed. */
	private boolean exitPressed;
	private boolean exitPrevious;
	/** Whether the click action button is being held. */
	private boolean clickHeld;
	/** Whether the switch adjustment button was pressed. */
	private boolean switchAdjustmentPressed;
	private boolean switchAdjustmentPrevious;
	/** Whether the increase button was pressed. */
	private boolean increasePressed;
	private boolean increasePrevious;
	/** Whether the decrease button was pressed. */
	private boolean decreasePressed;
	private boolean decreasePrevious;
	/** Whether the menu button was pressed. */
	private boolean menuPressed;
	private boolean menuPrevious;
	/** Whether the color button was pressed (for changing color region displays */
	private boolean colorPressed;
	private boolean colorPrevious;

	/** How much did we move horizontally? */
	private float horizontal;
	/** How much did we move vertically? */
	private float vertical;
	/** The crosshair position (for raddoll) */
	private Vector2 crosshair;
	/** The crosshair cache (for using as a return value) */
	private Vector2 crosscache;
	/** For the gamepad crosshair control */
	private float momentum;
	
	/** An X-Box controller (if it is connected) */
	XBoxController xbox;

	public GameCanvas getCanvas() {return canvas;}

	public void setCanvas(GameCanvas canvas) {this.canvas = canvas;}
	
	/**
	 * Returns the amount of sideways movement. 
	 *
	 * -1 = left, 1 = right, 0 = still
	 *
	 * @return the amount of sideways movement. 
	 */
	public float getHorizontal() {
		return horizontal;
	}
	
	/**
	 * Returns the current position of the crosshairs on the screen.
	 *
	 * This value does not return the actual reference to the crosshairs position.
	 * That way this method can be called multiple times without any fair that 
	 * the position has been corrupted.  However, it does return the same object
	 * each time.  So if you modify the object, the object will be reset in a
	 * subsequent call to this getter.
	 *
	 * @return the current position of the crosshairs on the screen.
	 */
	public Vector2 getCrossHair() {
		return crosscache.set(crosshair);
	}

	/**
	 * Returns true if the rotating color action button was pressed.
	 *
	 * This is a one-press button. It only returns true at the moment it was
	 * pressed, and returns false at any frame afterwards.
	 *
	 * @return true if the primary action button was pressed.
	 */
	public boolean didRotateColor() {
		return rotateColorPressed && !rotateColorPrevious;
	}

	/**
	 * Returns true if the click action button was pressed.
	 *
	 * This is a one-press button. It only returns true at the moment it was
	 * pressed, and returns false at any frame afterwards.
	 *
	 * @return true if the secondary action button was pressed.
	 */
	public boolean didClick() {
		return clickPressed && !clickPrevious;
	}

	/**
	 * Returns true if the reset button was pressed.
	 *
	 * @return true if the reset button was pressed.
	 */
	public boolean didReset() {
		return resetPressed && !resetPrevious;
	}

	/**
	 * Returns true if the player wants to go to the next level.
	 *
	 * @return true if the player wants to go to the next level.
	 */
	public boolean didAdvance() {
		return nextPressed && !nextPrevious;
	}
	
	/**
	 * Returns true if the player wants to go to the previous level.
	 *
	 * @return true if the player wants to go to the previous level.
	 */
	public boolean didRetreat() {
		return prevPressed && !prevPrevious;
	}
	
	/**
	 * Returns true if the player wants to go toggle the debug mode.
	 *
	 * @return true if the player wants to go toggle the debug mode.
	 */
	public boolean didDebug() {
		return debugPressed && !debugPrevious;
	}
	
	/**
	 * Returns true if the exit button was pressed.
	 *
	 * @return true if the exit button was pressed.
	 */
	public boolean didExit() {
		return exitPressed && !exitPrevious;
	}

	/**
	 * Returns true if the click button is held.
	 *
	 * @return true if the click button is held.
	 */
	public boolean didClickHeld() {
		return clickHeld;
	}

	/**
	 * Returns true if the switch adjust button was pressed. (For technical prototype)
	 *
	 * @return true if the switch adjust button was pressed.
	 */
	public boolean didSwitchAdjust() {
		return switchAdjustmentPressed && !switchAdjustmentPrevious;
	}

	/**
	 * Returns true if the increase button was pressed. (For technical prototype)
	 *
	 * @return true if the increase button was pressed.
	 */
	public boolean didIncrease() {
		return increasePressed && !increasePrevious;
	}

	/**
	 * Returns true if the decrease button was pressed. (For technical prototype)
	 *
	 * @return true if the decrease button was pressed.
	 */
	public boolean didDecrease() {
		return decreasePressed && !decreasePrevious;
	}

	/**
	 * Returns true if the decrease button was pressed. (For technical prototype)
	 *
	 * @return true if the decrease button was pressed.
	 */
	public boolean didMenu() {
		return menuPressed && !menuPrevious;
	}

	public boolean didColor() { return colorPressed && !colorPrevious; }
	
	/**
	 * Creates a new input controller
	 * 
	 * The input controller attempts to connect to the X-Box controller at device 0,
	 * if it exists.  Otherwise, it falls back to the keyboard control.
	 */
	public InputController() {
		// If we have a game-pad for id, then use it.
		Array<XBoxController> controllers = Controllers.get().getXBoxControllers();
		if (controllers.size > 0) {
			xbox = controllers.get( 0 );
		} else {
			xbox = null;
		}
		crosshair = new Vector2();
		crosscache = new Vector2();
	}

	/**
	 * Reads the input for the player and converts the result into game logic.
	 *
	 * The method provides both the input bounds and the drawing scale.  It needs
	 * the drawing scale to convert screen coordinates to world coordinates.  The
	 * bounds are for the crosshair.  They cannot go outside of this zone.
	 *
	 * @param bounds The input bounds for the crosshair.  
	 * @param scale  The drawing scale
	 */
	public void readInput(Rectangle bounds, Vector2 scale) {
		// Copy state from last animation frame
		// Helps us ignore buttons that are held down
		rotateColorPrevious  = rotateColorPressed;
		clickPrevious = clickPressed;
		resetPrevious  = resetPressed;
		debugPrevious  = debugPressed;
		exitPrevious = exitPressed;
		nextPrevious = nextPressed;
		prevPrevious = prevPressed;
		switchAdjustmentPrevious = switchAdjustmentPressed;
		increasePrevious = increasePressed;
		decreasePrevious = decreasePressed;
		menuPrevious = menuPressed;
		colorPrevious = colorPressed;
		
		// Check to see if a GamePad is connected
		if (xbox != null && xbox.isConnected()) {
			readGamepad(bounds, scale);
			readKeyboard(bounds, scale, true); // Read as a back-up
		} else {
			readKeyboard(bounds, scale, false);
		}
	}

	/**
	 * Reads input from an X-Box controller connected to this computer.
	 *
	 * The method provides both the input bounds and the drawing scale.  It needs
	 * the drawing scale to convert screen coordinates to world coordinates.  The
	 * bounds are for the crosshair.  They cannot go outside of this zone.
	 *
	 * @param bounds The input bounds for the crosshair.  
	 * @param scale  The drawing scale
	 */
	private void readGamepad(Rectangle bounds, Vector2 scale) {
		resetPressed = xbox.getStart();
		exitPressed  = xbox.getBack();
		nextPressed  = xbox.getRBumper();
		prevPressed  = xbox.getLBumper();
		rotateColorPressed = xbox.getA();
		debugPressed  = xbox.getY();

		// Increase animation frame, but only if trying to move
		horizontal = xbox.getLeftX();
		
		// Move the crosshairs with the right stick.r
		// TODO implement clickPressed and clickHeld
		clickPressed = false;
		crosscache.set(xbox.getLeftX(), xbox.getLeftY());
		if (crosscache.len2() > GP_THRESHOLD) {
			momentum += GP_ACCELERATE;
			momentum = Math.min(momentum, GP_MAX_SPEED);
			crosscache.scl(momentum);
			crosscache.scl(1/scale.x,1/scale.y);
			crosshair.add(crosscache);
		} else {
			momentum = 0;
		}
		clampPosition(bounds);
	}

	/**
	 * Reads input from the keyboard.
	 *
	 * This controller reads from the keyboard regardless of whether or not an X-Box
	 * controller is connected.  However, if a controller is connected, this method
	 * gives priority to the X-Box controller.
	 *
	 * @param secondary true if the keyboard should give priority to a gamepad
	 */
	private void readKeyboard(Rectangle bounds, Vector2 scale, boolean secondary) {
		// Give priority to gamepad results
		resetPressed = (secondary && resetPressed) || (Gdx.input.isKeyPressed(Input.Keys.R));
		debugPressed = (secondary && debugPressed) || (Gdx.input.isKeyPressed(Input.Keys.G));
		rotateColorPressed = (secondary && rotateColorPressed) || (Gdx.input.isKeyPressed(Input.Keys.SPACE));
		prevPressed = (secondary && prevPressed) || (Gdx.input.isKeyPressed(Input.Keys.P));
		nextPressed = (secondary && nextPressed) || (Gdx.input.isKeyPressed(Input.Keys.N));
		exitPressed  = (secondary && exitPressed) || (Gdx.input.isKeyPressed(Input.Keys.ESCAPE));
		
		// Directional controls
		horizontal = (secondary ? horizontal : 0.0f);
		if (Gdx.input.isKeyPressed(Input.Keys.D)) {
			horizontal += 0.2f;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.A)) {
			horizontal -= 0.2f;
		}

		// Technical prototype value adjustment controls
		switchAdjustmentPressed = (secondary && switchAdjustmentPressed) || (Gdx.input.isKeyPressed(Input.Keys.NUM_0));
		increasePressed = (secondary && increasePressed) || (Gdx.input.isKeyPressed(Input.Keys.EQUALS));
		decreasePressed = (secondary && decreasePressed) || (Gdx.input.isKeyPressed(Input.Keys.MINUS));
		menuPressed = (secondary && menuPressed) || (Gdx.input.isKeyPressed(Input.Keys.M));
		colorPressed = (secondary && colorPressed) || (Gdx.input.isKeyPressed(Input.Keys.C));

		// Mouse results
        clickPressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
		clickHeld = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
		Vector3 vec = canvas.cameraConvert(Gdx.input.getX(), Gdx.input.getY());
		//crosshair.set(Gdx.input.getX(), Gdx.input.getY());
		crosshair.set(vec.x, vec.y);
		crosshair.scl(1/scale.x,-1/scale.y);
		crosshair.y += bounds.height;
		clampPosition(bounds);
	}
	
	/**
	 * Clamp the cursor position so that it does not go outside the window
	 *
	 * While this is not usually a problem with mouse control, this is critical 
	 * for the gamepad controls.
	 */
	private void clampPosition(Rectangle bounds) {
		crosshair.x = Math.max(bounds.x, Math.min(bounds.x+bounds.width, crosshair.x));
		crosshair.y = Math.max(bounds.y, Math.min(bounds.y+bounds.height, crosshair.y));
	}
}