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

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.discodale.*;
import edu.cornell.gdiac.discodale.obstacle.*;
import edu.cornell.gdiac.util.FilmStrip;

/**
 * Player avatar for the plaform game.
 *
 * Note that this class returns to static loading. That is because there are
 * no other subclasses that we might loop through.
 */
public class DaleModel extends WheelObstacle {
	private static int WIN_CODE = 1;
	private static int LOSE_CODE = -1;
	private static int PLAY_CODE = 0;

	/** The initializing data (to avoid magic numbers) */
	private final JsonValue data;

	/** The factor to multiply by the input */
	private final float walkForce;
	/** The maximum character speed */
	private final float maxSpeed;
	/** The maximum character speed in the AIR */
	private final float maxAirSpeed;
	/** Identifier to allow us to track the sensor in ContactListener */
	private final String sensorName;

	/** The current horizontal movement of the character */
	private float movement;
	/** Whether our feet are on the ground */
	private boolean isGrounded;
	/** Feet when facing right */
	private PolygonShape sensorShapeRight;
	/** Feet when facing left */
	private PolygonShape sensorShapeLeft;

	/** Dale's body */
	private final CapsuleObstacle bodyPart;
	/** Offset between Dale and body (used to initialize joint) */
	private float bodyOffset;

	// region Grapple Properties

	/** Tongue texture */
	private Texture tongueTexture;
	/** Speed the grapple sticky part moves at */
	private float stickyPartSpeed;
	/** Maximum tongue length before attachment */
	private float maxTongueLength;
	/** Force the grapple applies on Dale */
	private float grappleForce;

	/** Grapple state */
	private GrappleState grappleState;
	/** Angle grapple shoots out at (calculated based on target location) */
	private float grappleAngle;
	/** Tongue sticky part */
	private final WheelObstacle grappleStickyPart;
	/** Body that sticky part is attached to, if any */
	private Body grappleAttachedBody;
	/** Local anchor for attached body */
	private final Vector2 grappleAttachedBodyLocalAnchor = new Vector2();
	/** Joint for welding sticky part to Dale or a wall */
	private Joint grappleJoint;
	/** Hit reflective surface flag */
	private boolean hitReflectiveFlag;

	// endregion

	/** Whether Dale's color is matched with background */
	private boolean match;
	private int winLose;

	private int colorIndex;
	private DaleColor[] availableColors;
	private TextureRegion[] headIdleTextures;
	private TextureRegion[] bodyIdleTextures;
	private FilmStrip[] bodyWalkTextures;

	/** Seconds per frame */
	private static final float ANIMATION_SPEED = 0.10f;
	private float bodyWalkAnimationClock;

	/** Cache for internal force calculations */
	private final Vector2 forceCache = new Vector2();
	/** Caceh for general vector calculations */
	private final Vector2 vectorCache = new Vector2();

	public DaleColor getColor() {
		return availableColors[colorIndex];
	}

	public void rotateColor() {
		colorIndex = (colorIndex + 1) % availableColors.length;
	}

	/**
	 * Returns left/right movement of this character.
	 * 
	 * This is the result of input times dude force.
	 *
	 * @return left/right movement of this character.
	 */
	public float getMovement() {
		return movement;
	}

	/**
	 * Sets left/right movement of this character.
	 * 
	 * This is the result of input times dude force.
	 *
	 * @param value left/right movement of this character.
	 */
	public void setMovement(float value) {
		movement = value;
		boolean bodyFacingRight = Math.cos(bodyPart.getAngle()) >= 0;
		if ((movement < 0 && bodyFacingRight) || (movement > 0 && !bodyFacingRight)) {
			bodyPart.setAngle((float) (bodyPart.getAngle() + Math.PI));
		}
	}

	/**
	 * Returns true if the dude is on the ground.
	 *
	 * @return true if the dude is on the ground.
	 */
	public boolean isGrounded() {
		return isGrounded;
	}

	/**
	 * Sets whether the dude is on the ground.
	 *
	 * @param value whether the dude is on the ground.
	 */
	public void setGrounded(boolean value) {
		isGrounded = value;
	}

	/**
	 * Returns how much force to apply to get the dude moving
	 *
	 * Multiply this by the input to get the movement value.
	 *
	 * @return how much force to apply to get the dude moving
	 */
	public float getWalkForce() {
		return walkForce;
	}

	/**
	 * Returns the upper limit on dude left-right movement.
	 *
	 * This does NOT apply to vertical movement.
	 *
	 * @return the upper limit on dude left-right movement.
	 */
	public float getMaxSpeed() {
		return maxSpeed;
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
	 * Returns the obstacle representing Dale's body part
	 * @return Dale's body part
	 */
	public Obstacle getBodyPart() {
		return bodyPart;
	}

	// region Grapple Methods

	/**
	 * Look at a position in the world
	 * @param position position to look at
	 */
	public void lookPosition(Vector2 position) {
		vectorCache.set(position).sub(getPosition());
		setAngle(vectorCache.angleRad());
	}

	/**
	 * Returns the tongue texture
	 * @return tongue texture
	 */
	public Texture getTongueTexture() {
		return tongueTexture;
	}

	/**
	 * Set the tongue texture
	 * @param tongueTexture tongue texture
	 */
	public void setTongueTexture(Texture tongueTexture) {
		this.tongueTexture = tongueTexture;
	}

	/**
	 * Set the tongue tip sticky part texture
	 * @param stickyPartTexture sticky part texture
	 */
	public void setStickyPartTexture(Texture stickyPartTexture) {
		grappleStickyPart.setTexture(new TextureRegion(stickyPartTexture));
	}

	/**
	 * Return grapple sticky part speed
	 * @return grapple sticky part speed
	 */
	public float getStickyPartSpeed() {
		return stickyPartSpeed;
	}

	/**
	 * Set grapple sticky part speed
	 * @param stickyPartSpeed grapple sticky part speed
	 */
	public void setStickyPartSpeed(float stickyPartSpeed) {
		this.stickyPartSpeed = stickyPartSpeed;
	}

	/**
	 * Get grapple force
	 * @return grapple force
	 */
	public float getGrappleForce() {
		return grappleForce;
	}

	/**
	 * Set grapple force
	 * @param grappleForce grapple force
	 */
	public void setGrappleForce(float grappleForce) {
		this.grappleForce = grappleForce;
	}

	/**
	 * Return grapple state
	 * @return grapple state
	 */
	public GrappleState getGrappleState() {
		return grappleState;
	}

	/**
	 * Set grapple state
	 * @param grappleState grapple state
	 */
	public void setGrappleState(GrappleState grappleState) {
		this.grappleState = grappleState;
	}

	/**
	 * Returns the grapple angle
	 * @return grapple angle
	 */
	public float getGrappleAngle() {
		return grappleAngle;
	}

	/**
	 * Set grapple angle
	 * @param grappleAngle grapple angle
	 */
	public void setGrappleAngle(float grappleAngle) {
		this.grappleAngle = grappleAngle;
	}

	/**
	 * Returns the maximum length of the tongue before attachment.
	 * @return max length of tongue
	 */
	public float getMaxTongueLength() {
		return maxTongueLength;
	}

	/**
	 * Calculate the current length of the tongue. This is the distance between Dale and the sticky part.
	 * @return length of tongue
	 */
	public float getTongueLength() {
		return vectorCache.set(grappleStickyPart.getPosition()).sub(getPosition()).len();
	}

	/**
	 * Calculate the current angle from Dale to the sticky part of the tongue.
	 * @return angle from Dale to tongue (radians)
	 */
	public float getTongueAngle() {
		return vectorCache.set(grappleStickyPart.getPosition()).sub(getPosition()).angleRad();
	}

	/**
	 * Return Dale's sticky part
	 * @return the sticky part
	 */
	public WheelObstacle getStickyPart() {
		return grappleStickyPart;
	}

	/**
	 * Return body that sticky part is attached to
	 * @return attached body
	 */
	public Body getGrappleAttachedBody() {
		return grappleAttachedBody;
	}

	/**
	 * Set attached body when sticky part hits something so can create the joint right after
	 * @param grappleAttachedBody body sticky part should attach to
	 */
	public void setGrappleAttachedBody(Body grappleAttachedBody) {
		this.grappleAttachedBody = grappleAttachedBody;
	}

	/**
	 * Returns local anchor vector for attached body
	 * @return attached body local anchor
	 */
	public Vector2 getGrappleAttachedBodyLocalAnchor() {
		return grappleAttachedBodyLocalAnchor;
	}

	/**
	 * Set attached body local anchor
	 * @param grappleAttachedBodyLocalAnchor attached body local anchor
	 */
	public void setGrappleAttachedBodyLocalAnchor(Vector2 grappleAttachedBodyLocalAnchor) {
		this.grappleAttachedBodyLocalAnchor.set(grappleAttachedBodyLocalAnchor);
	}

	/**
	 * Create weld joint at the center of Dale and the tongue sticky part.
	 * @param world physics world to make joint in
	 */
	public void createGrappleJoint(World world) {
		WeldJointDef jointDef = new WeldJointDef();
		jointDef.bodyA = this.getBody();
		jointDef.bodyB = grappleStickyPart.getBody();
		jointDef.collideConnected = false;
		grappleJoint = world.createJoint(jointDef);
	}

	/**
	 * Create weld joint at the center of another body and the tongue sticky part.
	 * @param bodyA other body for grapple joint
	 * @param world physics world to make joint in
	 */
	public void createGrappleJoint(Body bodyA, Vector2 localAnchorA, World world) {
		WeldJointDef jointDef = new WeldJointDef();
		jointDef.bodyA = bodyA;
		jointDef.localAnchorA.set(localAnchorA);
		jointDef.bodyB = grappleStickyPart.getBody();
		jointDef.collideConnected = false;
		grappleJoint = world.createJoint(jointDef);
	}

	/**
	 * Destroy weld joint at the center of Dale and tongue sticky part.
	 * @param world physics world to destroy joint in
	 */
	public void destroyGrappleJoint(World world) {
		world.destroyJoint(grappleJoint);
		grappleJoint = null;
	}

	/**
	 * Set grapple sticky part physics active
	 * @param active true if active
	 */
	public void setStickyPartActive(boolean active) {
		grappleStickyPart.setActive(active);
	}

	/**
	 * Get if did hit reflective surface flag is set
	 * @return true if hit reflective
	 */
	public boolean isHitReflectiveFlag() {
		return hitReflectiveFlag;
	}

	/**
	 * Set hit reflective surface flag
	 * @param value true if hit reflective
	 */
	public void setHitReflectiveFlag(boolean value) {
		hitReflectiveFlag = value;
	}

	// endregion

	/**
	 * Sets Dale's color match.
	 */
	public void setMatch(boolean match) {
		this.match = match;
	}

	/**
	 * Gets Dale's color match.
	 */
	public boolean getMatch() {
		return match;
	}

	/**
	 * Sets Dale's winLose.
	 */
	public void setWinLose(boolean win) {
		winLose = win ? WIN_CODE : LOSE_CODE;
	}

	/**
	 * Gets Dale's winLose.
	 */
	public int getWinLose() {
		return winLose;
	}

	/**
	 * Creates a new DALE avatar with the given physics data
	 *
	 * The size is expressed in physics units NOT pixels. In order for
	 * drawing to work properly, you MUST set the drawScale. The drawScale
	 * converts the physics units to pixels.
	 *
	 * @param data              The physics constants for Dale
	 * @param headRadius        The head radius in physics units
	 * @param bodyWidth         The body width in physics units
	 * @param bodyHeight        The body width in physics units
	 * @param bodyOffset        Distance between Dale head and body centers
	 * @param availableColors   Available colors for Dale, should be same length as headTextures and bodyTextures
	 * @param headIdleTextures  Head idle textures in order of colors
	 * @param bodyIdleTextures  Body idle textures in order of colors
	 * @param bodyWalkTextures  Body walk textures in order of colors
	 */
	public DaleModel(float x, float y, JsonValue data, float headRadius, float bodyWidth, float bodyHeight,
					 float bodyOffset, DaleColor[] availableColors, TextureRegion[] headIdleTextures,
					 TextureRegion[] bodyIdleTextures, FilmStrip[] bodyWalkTextures) {
		// The shrink factors fit the image to a tigher hitbox
		super(x, y, headRadius * data.getFloat("head_shrink", 1));
		setDensity(data.getFloat("density", 0));
		setFriction(data.getFloat("friction", 0)); /// HE WILL STICK TO WALLS IF YOU FORGET
		setFixedRotation(true);

		// Filter collisions
		Filter daleFilter = new Filter();
		daleFilter.categoryBits = 0b00001000;
		daleFilter.maskBits     = 0b00000111;
		setFilterData(daleFilter);

		maxSpeed = data.getFloat("max_speed", 0);
		maxAirSpeed = data.getFloat("max_air_speed", 0);
		walkForce = data.getFloat("walk_force", 0);
		sensorName = Constants.DALE_GROUND_SENSOR_NAME;
		this.data = data;

		// Gameplay attributes
		isGrounded = false;
		match = true;
		winLose = PLAY_CODE;

		// Dale's body
		bodyPart = new CapsuleObstacle(getX(), getY(), bodyWidth * data.get("body_shrink").getFloat(0),
				bodyHeight * data.get("body_shrink").getFloat(1));
		bodyPart.setName("dalebody");
		bodyPart.setDensity(data.getFloat("density", 0));
		bodyPart.setFriction(data.getFloat("friction", 0));
		bodyPart.setBodyType(BodyDef.BodyType.DynamicBody);
		bodyPart.setFilterData(daleFilter);
		this.bodyOffset = bodyOffset; // For use when making joints later

		// Grapple things
		stickyPartSpeed = data.getFloat("grapple_speed", 1);
		grappleForce = data.getFloat("grapple_force", 1);
		maxTongueLength = data.getFloat("max_tongue_length", 1);
		grappleAngle = 0;
		grappleState = GrappleState.RETRACTED;
		grappleStickyPart = new WheelObstacle(getX(), getY(), getRadius() / 10);
		grappleStickyPart.setName("stickypart");
		grappleStickyPart.setDensity(data.getFloat("density", 0));
		grappleStickyPart.setBodyType(BodyDef.BodyType.DynamicBody);
		Filter grappleFilter = new Filter();
		grappleFilter.categoryBits = 0b00010000;
		grappleFilter.maskBits     = 0b00000001;
		grappleStickyPart.setFilterData(grappleFilter);
		grappleAttachedBody = null;

		setName(Constants.DALE_NAME_TAG);

		colorIndex = 0;
		this.availableColors = availableColors;
		this.headIdleTextures = headIdleTextures;
		this.bodyIdleTextures = bodyIdleTextures;
		this.bodyWalkTextures = bodyWalkTextures;
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

		if (!bodyPart.activatePhysics(world)) {
			return false;
		}

		if (!grappleStickyPart.activatePhysics(world)) {
			return false;
		}

		RevoluteJointDef revoluteJointDef = new RevoluteJointDef();
		revoluteJointDef.bodyA = this.getBody();
		revoluteJointDef.bodyB = bodyPart.getBody();
		revoluteJointDef.localAnchorB.set(new Vector2(bodyOffset, 0));
		revoluteJointDef.collideConnected = false;
		world.createJoint(revoluteJointDef);

		createGrappleJoint(world);

		// Ground Sensor
		// We only allow Dale to walk when he's on the ground.
		JsonValue sensorjv = data.get("sensor");
		FixtureDef sensorDef = new FixtureDef();
		sensorDef.density = 0;
		sensorDef.isSensor = true;

		// Sensor for facing right is on the bottom
		Vector2 sensorCenterRight = new Vector2(0, 0 - bodyPart.getHeight() / 2);
		sensorShapeRight = new PolygonShape();
		sensorShapeRight.setAsBox(sensorjv.getFloat("shrink", 0) * bodyPart.getWidth() / 2.0f,
				sensorjv.getFloat("height", 0), sensorCenterRight, 0.0f);
		sensorDef.shape = sensorShapeRight;
		Fixture sensorFixtureRight = bodyPart.getBody().createFixture(sensorDef);
		sensorFixtureRight.setUserData(getSensorName());
		sensorFixtureRight.setFilterData(getFilterData()); // Required for walking

		// Sensor for facing left is on top
		Vector2 sensorCenterLeft = new Vector2(0, bodyPart.getHeight() / 2);
		sensorShapeLeft = new PolygonShape();
		sensorShapeLeft.setAsBox(sensorjv.getFloat("shrink", 0) * bodyPart.getWidth() / 2.0f,
				sensorjv.getFloat("height", 0), sensorCenterLeft, 0.0f);
		sensorDef.shape = sensorShapeLeft;
		Fixture sensorFixtureLeft = bodyPart.getBody().createFixture(sensorDef);
		sensorFixtureLeft.setUserData(getSensorName());
		sensorFixtureLeft.setFilterData(getFilterData()); // Required for walking

		return true;
	}

	/**
	 * Applies the force to the body of this dude
	 *
	 * This method should be called after the force attribute is set.
	 */
	public void applyForce() {
		if (!isActive()) {
			return;
		}


		if (grappleState == GrappleState.ATTACHED) {
			// Apply grapple force
			forceCache.set(grappleForce, 0).rotateRad(getTongueAngle());
			body.applyForce(forceCache, getPosition(), true);
		} else if (!isGrounded()) {
			// Limit max air speed
			if (getLinearVelocity().len2() >= maxAirSpeed) {
				setLinearVelocity(vectorCache.set(getLinearVelocity()).limit(maxAirSpeed));
			}
		} else {
			// Clamp walking speed
			if (Math.abs(getVX()) >= getMaxSpeed()) {
				setVX(Math.signum(getVX()) * getMaxSpeed());
			} else {
				forceCache.set(getMovement(),0);
				body.applyForce(forceCache,getPosition(),true);
			}
		}
	}


	/**
	 * Move the grapple sticky part according to the grapple state.
	 * If extending, it has constant linear velocity at the original angle between Dale and target
	 * If retracting, it ignores physics and moves towards Dale manually.
	 * @param dt seconds since last frame
	 */
	public void applyStickyPartMovement(float dt) {
		switch (grappleState) {
			case EXTENDING:
				vectorCache.set(1, 0).rotateRad(grappleAngle).scl(stickyPartSpeed);
				grappleStickyPart.setLinearVelocity(vectorCache);
				break;
			case RETURNING:
				vectorCache.set(getPosition()).sub(grappleStickyPart.getPosition()).nor().scl(dt * stickyPartSpeed);
				grappleStickyPart.setPosition(vectorCache.add(grappleStickyPart.getPosition()));
				// TODO is there a better way to do this
				break;
			default:
				break;
		}
	}

	/**
	 * Updates the object's physics state (NOT GAME LOGIC).
	 *
	 * We use this method to reset cooldowns.
	 *
	 * @param dt Number of seconds since last animation frame
	 */
	public void update(float dt) {
		super.update(dt);
		bodyPart.update(dt);
		grappleStickyPart.update(dt);

		bodyWalkAnimationClock = (bodyWalkAnimationClock + dt) % (ANIMATION_SPEED * bodyWalkTextures[0].getSize());
	}

	@Override
	public void setDrawScale(Vector2 value) {
		super.setDrawScale(value);
		bodyPart.setDrawScale(value);
		grappleStickyPart.setDrawScale(value); // So important!
	}

	/**
	 * Draws the physics object.
	 *
	 * @param canvas Drawing context
	 */
	public void draw(GameCanvas canvas) {
		boolean headFacingRight = Math.cos(getAngle()) >= 0;
		float headFlipY = headFacingRight ? 1.0f : -1.0f;
		boolean bodyFacingRight = Math.cos(bodyPart.getAngle()) >= 0;
		float bodyFlipY = bodyFacingRight ? 1.0f : -1.0f;

		setDaleTexture();

		// Reorder this to change if the tongue is on top of Dale or not
		bodyPart.draw(canvas, 1.0f, bodyFlipY);
		canvas.draw(tongueTexture, Color.WHITE, 0, tongueTexture.getHeight() / 2f, getX() * drawScale.x, getY() * drawScale.y,
				getTongueAngle(), getTongueLength() / tongueTexture.getWidth() * drawScale.x, 1);
		grappleStickyPart.draw(canvas);
		this.draw(canvas, 1.0f, headFlipY);
	}

	public void setDaleTexture() {
		this.setTexture(headIdleTextures[colorIndex]);
		if (isGrounded && Math.abs(getVX()) > 2) {
			bodyWalkTextures[colorIndex].setFrame((int) (bodyWalkAnimationClock / ANIMATION_SPEED));
			bodyPart.setTexture(bodyWalkTextures[colorIndex]);
		} else {
			bodyPart.setTexture(bodyIdleTextures[colorIndex]);
		}
	}

	/**
	 * Draws the outline of the physics body.
	 *
	 * This method can be helpful for understanding issues with collisions.
	 *
	 * @param canvas Drawing context
	 */
	public void drawDebug(GameCanvas canvas) {
		bodyPart.drawDebug(canvas);
		grappleStickyPart.drawDebug(canvas);
		super.drawDebug(canvas);
		canvas.drawPhysics(sensorShapeRight, Color.RED, bodyPart.getX(), bodyPart.getY(), bodyPart.getAngle(), drawScale.x, drawScale.y);
		canvas.drawPhysics(sensorShapeLeft, Color.RED, bodyPart.getX(), bodyPart.getY(), bodyPart.getAngle(), drawScale.x, drawScale.y);
	}

	/**
	 * The state of Dale's grapple tongue
	 */
	public enum GrappleState {
		RETRACTED,
		EXTENDING,
		ATTACHED,
		RETURNING,
	}
}
