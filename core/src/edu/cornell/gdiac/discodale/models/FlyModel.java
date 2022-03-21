/*
 * DudeModel.java
 *
 * You SHOULD NOT need to modify this file.  However, you may learn valuable lessons
 * for the rest of the lab by looking at it.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * Updated asset version, 2/6/2021
 */
package edu.cornell.gdiac.discodale.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.discodale.GameCanvas;
import edu.cornell.gdiac.discodale.obstacle.CapsuleObstacle;

/**
 * Player avatar for the plaform game.
 *
 * Note that this class returns to static loading. That is because there are
 * no other subclasses that we might loop through.
 */
public class FlyModel extends CapsuleObstacle {

	/** The initializing data (to avoid magic numbers) */
	private final JsonValue data;

	/** Identifier to allow us to track the sensor in ContactListener */
	private final String sensorName;

	/** The physics shape of this object */
	private PolygonShape sensorShape;

	private Vector2 velocity = new Vector2();

	/** Cache for internal force calculations */
	private final Vector2 forceCache = new Vector2();

	/** Flag angry, whether the fly is chasing Dale or not */
	private boolean angry;

	public float getDirection() {
		return this.velocity.angleDeg();
	}

	public void setDirection(float direction) {
		this.velocity.setAngleDeg(direction);
	}

	public float getSpeed() {
		return this.velocity.len();
	}

	public void setSpeed(float speed) {
		this.velocity.setLength(speed);
	}

	public void setVelocity(float speed, float direction) {
		this.velocity.set(0, speed).setAngleDeg(direction);
	}

	public boolean getAngry() {
		return this.angry;
	}

	public void setAngry(boolean angry) {
		this.angry = angry;
	}

	/**
	 * Returns the name of the ground sensor
	 *
	 * This is used by ContactListener
	 *
	 * @return the name of the ground sensor
	 */
	public String getSensorName() {
		return sensorName;
	}

	/**
	 * Creates a new dude avatar with the given physics data
	 *
	 * The size is expressed in physics units NOT pixels. In order for
	 * drawing to work properly, you MUST set the drawScale. The drawScale
	 * converts the physics units to pixels.
	 *
	 * @param data   The physics constants for this dude
	 * @param width  The object width in physics units
	 * @param height The object width in physics units
	 */
	public FlyModel(JsonValue data, float x, float y, float width, float height) {
		// The shrink factors fit the image to a tigher hitbox
		super(x, y,
				width * data.get("shrink").getFloat(0),
				height * data.get("shrink").getFloat(1));
		setFixedRotation(true);
		setAngry(false);
		sensorName = "FlyGroundSensor";
		this.data = data;

		// Gameplay attributes

		setName("fly");
	}

	/**
	 * Creates the physics Body(s) for this object, adding them to the world.
	 *
	 * This method overrides the base method to keep your ship from spinning.
	 *
	 * @param world Box2D world to store body
	 *
	 * @return true if object allocation succeeded
	 */
	public boolean activatePhysics(World world) {
		// create the box from our superclass
		if (!super.activatePhysics(world)) {
			return false;
		}

		// Ground Sensor
		// -------------
		// We only allow the dude to jump when he's on the ground.
		// Double jumping is not allowed.
		//
		// To determine whether or not the dude is on the ground,
		// we create a thin sensor under his feet, which reports
		// collisions with the world but has no collision response.
		// Vector2 sensorCenter = new Vector2(0, -getHeight() / 2);
		// FixtureDef sensorDef = new FixtureDef();
		// sensorDef.density = data.getFloat("density",0);
		// sensorDef.isSensor = true;
		// sensorShape = new PolygonShape();
		// JsonValue sensorjv = data.get("sensor");
		// sensorShape.setAsBox(sensorjv.getFloat("shrink",0)*getWidth()/2.0f,
		// sensorjv.getFloat("height",0), sensorCenter, 0.0f);
		// sensorDef.shape = sensorShape;
		//
		// // Ground sensor to represent our feet
		// Fixture sensorFixture = body.createFixture( sensorDef );
		// sensorFixture.setUserData(getSensorName());
		// body.setMassData(new MassData());
		setGravityScale(0);
		return true;
	}

	/**
	 * Updates the object's physics state (NOT GAME LOGIC).
	 *
	 * We use this method to reset cooldowns.
	 *
	 * @param dt Number of seconds since last animation frame
	 */
	public void update(float dt) {
		body.setLinearVelocity(this.velocity);
		super.update(dt);
	}

	/**
	 * Draws the physics object.
	 *
	 * @param canvas Drawing context
	 */
	public void draw(GameCanvas canvas) {
		canvas.draw(texture, Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(),
				1.0f, 1.0f);
	}

	/**
	 * Draws the outline of the physics body.
	 *
	 * This method can be helpful for understanding issues with collisions.
	 *
	 * @param canvas Drawing context
	 */
	public void drawDebug(GameCanvas canvas) {
		super.drawDebug(canvas);
		canvas.drawPhysics(sensorShape, Color.RED, getX(), getY(), getAngle(), drawScale.x, drawScale.y);
	}
}