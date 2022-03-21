package edu.cornell.gdiac.discodale.controllers;

import edu.cornell.gdiac.discodale.InputController;
import edu.cornell.gdiac.discodale.models.DaleModel;

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
}
