package edu.cornell.gdiac.discodale.controllers;

import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.discodale.InputController;
import edu.cornell.gdiac.discodale.models.DaleModel;
import edu.cornell.gdiac.discodale.models.DaleModel.GrappleState;

public class DaleController {
	/** Dale Model that this Dale Controller handles */
	private final DaleModel dale;

	public DaleController(DaleModel dale) {
		this.dale = dale;
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
				if (InputController.getInstance().didClick()) {
					dale.getGrappleTarget().set(InputController.getInstance().getCrossHair());
					dale.setGrappleState(GrappleState.EXTENDING);
					dale.destroySelfGrappleJoint(world);
					System.out.println("Grapple target: " + dale.getGrappleTarget());
				}
				break;
			case EXTENDING:
				// Not final code
				if (InputController.getInstance().didClick()) {
					dale.setGrappleState(GrappleState.RETRACTED);
					dale.createSelfGrappleJoint(world);
				}
				break;
		}
	}
}
