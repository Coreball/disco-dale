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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.discodale.GameCanvas;
import edu.cornell.gdiac.discodale.obstacle.CapsuleObstacle;
import edu.cornell.gdiac.util.FilmStrip;

/**
 * Player avatar for the plaform game.
 *
 * Note that this class returns to static loading. That is because there are
 * no other subclasses that we might loop through.
 */
public class FlyModel extends CapsuleObstacle {

	/** How fast we change frames (one frame per 4 calls to update) */
	private static final float ANIMATION_SPEED = 0.25f;
	/** The number of animation frames in our filmstrip */
	private static final int   NUM_ANIM_FRAMES = 8;

	/** The scale to shrink the asset texture */
	private static final float TEXTURE_SCALE = 25f;

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

	/** CURRENT image for this object. May change over time. */
	protected FilmStrip animator;
	/** Current animation frame for this fly */
	private float animeFrame;

	private Texture idleTexture;
	private Texture chasingTexture;

	public enum IdleType {
		STATIONARY,
		HORIZONTAL
	}
	private IdleType idleType;

	public IdleType getIdleType(){
		return idleType;
	}

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

	public void initializeTexture (Texture idle, Texture chasing){
		idleTexture = idle;
		chasingTexture = chasing;
		animator = new FilmStrip(idle,1,NUM_ANIM_FRAMES,NUM_ANIM_FRAMES);
		origin = new Vector2(animator.getRegionWidth()/2.0f, animator.getRegionHeight()/2.0f);
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
	public FlyModel(JsonValue data, float x, float y, float width, float height, IdleType idleType) {
		// The shrink factors fit the image to a tigher hitbox
		super(x, y,
				width * data.get("shrink").getFloat(0),
				height * data.get("shrink").getFloat(1));
		setFixedRotation(true);
		setAngry(false);
		sensorName = "FlyGroundSensor";
		this.data = data;
		this.idleType = idleType;

		// Animation
		animeFrame = 0.0f;

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
		// Increase animation frame
		animeFrame += ANIMATION_SPEED;
		if (animeFrame >= NUM_ANIM_FRAMES) {
			animeFrame -= NUM_ANIM_FRAMES;
		}
		if (angry){
			System.out.println("angry!");
			animator.setTexture(chasingTexture);
		} else {
			animator.setTexture(idleTexture);
		}

		body.setLinearVelocity(this.velocity);
		super.update(dt);

	}

	/**
	 * Draws the physics object.
	 *
	 * @param canvas Drawing context
	 */
	public void draw(GameCanvas canvas) {
		animator.setFrame((int)animeFrame);
		float sx = 1f;
		if (velocity.x > 0){
			sx = -1f;
		}
		canvas.draw(animator, Color.WHITE, origin.x, origin.y, getX() * drawScale.x,
				getY() * drawScale.y, getAngle(),sx / TEXTURE_SCALE, 1.0f / TEXTURE_SCALE);
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
		// TODO remove feet sensor from FlyModel
//		canvas.drawPhysics(sensorShape, Color.RED, getX(), getY(), getAngle(), drawScale.x, drawScale.y);
	}
}