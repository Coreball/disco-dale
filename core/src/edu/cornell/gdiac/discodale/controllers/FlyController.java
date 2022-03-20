package edu.cornell.gdiac.discodale.controllers;

import edu.cornell.gdiac.discodale.models.DaleModel;
import edu.cornell.gdiac.discodale.models.FlyModel;

public class FlyController {
    /** The fly being controlled by this FlyController*/
    private FlyModel fly;
    /** The target dale (to chase when angry). */
    private DaleModel dale;
    /** The number of ticks since we started this controller */
    private long ticks;

    /**
     * Creates an FlyController for the fly with the given id.
     *
     * @param id The unique fly identifier
     * @param dale The target Dale
     */
    public FlyController(int id, DaleModel dale) {
//        this.fly = flies.get(id);
//        this.board = board;
//        this.fleet = ships;
//
//        state = FSMState.SPAWN;
//        move  = CONTROL_NO_ACTION;
//        ticks = 0;
//
//        // Select an initial target
//        target = null;
//        selectTarget();
    }
}
