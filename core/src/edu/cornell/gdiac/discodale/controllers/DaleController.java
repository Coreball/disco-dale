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
		if (dale.getGrappleState() != GrappleState.ATTACHED) {
			dale.setMovement(InputController.getInstance().getHorizontal() * dale.getWalkForce());
		}
	}

	public void processColorRotation() {
		if (InputController.getInstance().didRotateColor()) {
			dale.rotateColor();
		}
	}

	public void processGrappleAction(World world) {
		switch (dale.getGrappleState()) {
			case RETRACTED:
				dale.lookPosition(InputController.getInstance().getCrossHair());
				if (InputController.getInstance().didClickHeld()) {
					dale.setGrappleState(GrappleState.EXTENDING);
					vectorCache.set(InputController.getInstance().getCrossHair()).sub(dale.getPosition());
					dale.setGrappleAngle(vectorCache.angleRad());
					dale.destroyGrappleJoint(world);
					System.out.println("Grapple angle: " + dale.getGrappleAngle());
				}
				break;
			case EXTENDING:
				dale.lookPosition(dale.getStickyPart().getPosition());
				if (!InputController.getInstance().didClickHeld() || dale.getTongueLength() > dale.getMaxTongueLength()) {
					dale.setGrappleState(GrappleState.RETURNING);
					dale.setStickyPartActive(false);
				} else if (dale.getGrappleAttachedBody() != null) {
					dale.setGrappleState(GrappleState.ATTACHED);
					dale.createGrappleJoint(dale.getGrappleAttachedBody(), dale.getGrappleAttachedBodyLocalAnchor(), world);
				}
				break;
			case ATTACHED:
				dale.lookPosition(dale.getStickyPart().getPosition());
				if (!InputController.getInstance().didClickHeld()) {
					dale.setGrappleState(GrappleState.RETURNING);
					dale.setGrappleAttachedBody(null);
					dale.setGrappleAttachedBodyLocalAnchor(Vector2.Zero);
					dale.setStickyPartActive(false);
					dale.destroyGrappleJoint(world);
				}
				break;
			case RETURNING:
				dale.lookPosition(dale.getStickyPart().getPosition());
				if (dale.getTongueLength() < 0.5) { // TODO don't use magic number here
					dale.setGrappleState(GrappleState.RETRACTED);
					dale.setStickyPartActive(true);
					dale.createGrappleJoint(world);
				}
				break;
		}
	}
}
