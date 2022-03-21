package edu.cornell.gdiac.discodale.controllers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.discodale.InputController;
import edu.cornell.gdiac.discodale.models.DaleModel;
import edu.cornell.gdiac.discodale.models.DaleModel.GrappleState;

public class DaleController {
	/** Dale Model that this Dale Controller handles */
	private final DaleModel dale;
	/** Vector math cache */
	private final Vector2 vectorCache;

	public DaleController(DaleModel dale) {
		this.dale = dale;
		this.vectorCache = new Vector2();
	}

	public void processMovement() {
		dale.setMovement(InputController.getInstance().getHorizontal() * dale.getForce());
	}

	public void processJumping() {
		dale.setJumping(InputController.getInstance().didJump());
	}

	public void processColorRotation() {
		if (InputController.getInstance().didRotateColor()) {
			dale.rotateColor();
		}
	}

	public void processGrappleAction(World world) {
		switch (dale.getGrappleState()) {
			case RETRACTED:
				if (InputController.getInstance().didClickHeld()) {
					dale.setGrappleState(GrappleState.EXTENDING);
					vectorCache.set(InputController.getInstance().getCrossHair()).sub(dale.getPosition());
					dale.setGrappleAngle(vectorCache.angleRad());
					dale.destroySelfGrappleJoint(world);
					System.out.println("Grapple angle: " + dale.getGrappleAngle());
				}
				break;
			case EXTENDING:
				if (!InputController.getInstance().didClickHeld()) {
					dale.setGrappleState(GrappleState.RETURNING);
					dale.setStickyPartActive(false);
//					dale.createSelfGrappleJoint(world);
				}
				break;
			case RETURNING:
				if (dale.getTongueLength() < 0.5) { // TODO don't use magic number here
					dale.setGrappleState(GrappleState.RETRACTED);
					dale.setStickyPartActive(true);
					dale.createSelfGrappleJoint(world);
				}
				break;
		}
	}
}
