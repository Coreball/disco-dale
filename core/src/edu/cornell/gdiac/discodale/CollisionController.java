package edu.cornell.gdiac.discodale;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.gdiac.discodale.models.FlyModel;
import edu.cornell.gdiac.discodale.models.SceneModel;
import edu.cornell.gdiac.discodale.obstacle.Obstacle;
import edu.cornell.gdiac.discodale.models.DaleModel;

public class CollisionController implements ContactListener {

    private DaleModel dale;
    private FlyModel[] flies;
    private SceneModel sceneModel;

    /** Vector math cache */
    private final Vector2 vectorCache = new Vector2();

    /** Mark set to handle more sophisticated collision callbacks */
    protected ObjectSet<Fixture> sensorFixtures;

    public CollisionController(DaleModel dale, FlyModel[] flies, SceneModel sceneModel) {
        this.sensorFixtures = new ObjectSet<>();
        this.dale = dale;
        this.flies = flies;
        this.sceneModel = sceneModel;
    }

    /**
     * Callback method for the start of a collision
     *
     * This method is called when we first get a collision between two objects.  We use
     * this method to test if it is the "right" kind of collision.  In particular, we
     * use it to test if we made it to the win door.
     *
     * @param contact The two bodies that collided
     */
    public void beginContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        try {
            Obstacle bd1 = (Obstacle)body1.getUserData();
            Obstacle bd2 = (Obstacle)body2.getUserData();

            // See if tongue sticky part has hit something
            if (dale.getGrappleState() == DaleModel.GrappleState.EXTENDING &&
                    ((bd1 == dale.getStickyPart() && bd2 != dale) || (bd2 == dale.getStickyPart() && bd1 != dale))) {
                if (bd1.getName().startsWith("reflective") || bd2.getName().startsWith("reflective")) {
                    dale.setHitReflectiveFlag(true);
                } else {
                    // Can't create the weld joint directly here because it violates locking assertions or something
                    // So set the attached body and local anchor location to create it when DaleController processes
                    Body bodyA = dale.getStickyPart() == bd1 ? body2 : body1;
                    Body stickyPartBody = dale.getStickyPart() == bd1 ? body1 : body2;
                    vectorCache.set(stickyPartBody.getPosition()).sub(bodyA.getPosition());
                    dale.setGrappleAttachedBody(bodyA);
                    dale.setGrappleAttachedBodyLocalAnchor(vectorCache);
                }
            }

            // See if we have landed on the ground.
            if ((dale.getSensorName().equals(fd2) && dale != bd1) ||
                    (dale.getSensorName().equals(fd1) && dale != bd2)) {
                dale.setGrounded(true);
                sensorFixtures.add(dale == bd1 ? fix2 : fix1); // Could have more than one ground
            }

            // Check for win condition
            if ((bd1 == dale && bd2 == sceneModel.goalDoor) ||
                    (bd1 == sceneModel.goalDoor && bd2 == dale)) {
                dale.setWinLose(true);
            }

            if ((bd1 == dale   && isFly(bd2)) ||
                    (isFly(bd2) && bd2 == dale)) {
                dale.setWinLose(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean isFly(Obstacle ob){
        try{
            FlyModel f = (FlyModel) ob;
            return true;
        } catch (Exception e){
            return false;
        }
    }

    /**
     * Callback method for the start of a collision
     *
     * This method is called when two objects cease to touch.  The main use of this method
     * is to determine when the characer is NOT on the ground.  This is how we prevent
     * double jumping.
     */
    public void endContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        Object bd1 = body1.getUserData();
        Object bd2 = body2.getUserData();

        if ((dale.getSensorName().equals(fd2) && dale != bd1) ||
                (dale.getSensorName().equals(fd1) && dale != bd2)) {
            sensorFixtures.remove(dale == bd1 ? fix2 : fix1);
            if (sensorFixtures.size == 0) {
                dale.setGrounded(false);
            }
        }
    }
    /** Unused ContactListener method */
    public void postSolve(Contact contact, ContactImpulse impulse) {}
    /** Unused ContactListener method */
    public void preSolve(Contact contact, Manifold oldManifold) {}
}
