package edu.cornell.gdiac.discodale.controllers;

import com.badlogic.gdx.physics.box2d.Fixture;
import edu.cornell.gdiac.discodale.models.DaleModel;
import edu.cornell.gdiac.discodale.models.FlyModel;
import edu.cornell.gdiac.discodale.models.SceneModel;

import java.util.*;

public class FlyController {

    /**
     * Enumeration to encode the finite state machine.
     */
    private static enum FSMState {
        /** The fly is in idle state */
        IDLE,
        /** The fly is chasing Dale */
        CHASE
    }

    private static int FLY_SPEED = 5;

    /** The fly being controlled by this FlyController */
    private FlyModel fly;
    /** The target dale (to chase when angry). */
    private DaleModel dale;
    /** The scene */
    private SceneModel scene;
    /** The number of ticks since we started this controller */
    private long ticks;
    /** The state of fly */
    private FSMState state;
    /** The fly's next direction. */
    private float dx;
    private float dy;

    private boolean seeDaleInRealWorld;

    public class Node {
        int x;
        int y;
        float g;
        float h;
        Node parent;

        public Node(int x, int y, float g, float h) {
            this.x = x;
            this.y = y;
            this.g = g;
            this.h = h;
            this.parent = null;
        }

    }

    /**
     * Creates an FlyController for the fly with the given id.
     *
     * @param fly   The fly of the controller
     * @param dale  The target Dale
     * @param scene The scene
     */
    public FlyController(FlyModel fly, DaleModel dale, SceneModel scene) {
        this.dale = dale;
        this.fly = fly;
        this.scene = scene;
        state = FSMState.IDLE;
        dx = 0;
        dy = 0;
        ticks = 0;
        seeDaleInRealWorld = false;
    }

    public void setSeeDaleInRealWorld(boolean seeDaleInRealWorld){
        this.seeDaleInRealWorld = seeDaleInRealWorld;
    }

    /** Gets if Dale is in fly's real world sight. Note that it does not know scene's mode. */
    public boolean getSeeDaleInRealWorld(){
        return seeDaleInRealWorld;
    }

    public FlyModel getFly(){
        return fly;
    }

    public void setVelocity() {
        if (dx == dy && dx == 0) {
            fly.setVelocity(0, 0);
        } else {
            fly.setVelocity(FLY_SPEED, (float) Math.toDegrees(Math.atan2(dy, dx)));
        }

    }

    /** Change fly's direction depending on state */
    public void changeDirection() {
        ticks++;
        changeState();
        switch (state) {
            case IDLE:
                setIdleDirection();
                break;
            case CHASE:
                findDirection();
                break;
            default:
                assert (false);
                break;
        }
    }
    /** Checks if Dale is in fly's area sight. Note that it does not know scene's mode. */
    private boolean daleInAreaSight(){
        boolean ans = true;
        float diffX = Math.abs(fly.getX() - dale.getX());
        float diffY = Math.abs(fly.getY() - dale.getY());
        if(Math.sqrt((double)(diffX*diffX) + (double)(diffY*diffY))>scene.getAreaSightRadius()){
            ans = false;
        }
        return ans;
    }

    /** Determine if fly should chase Dale */
    public boolean shouldChaseDale(){
        boolean seeInArea = true;
        boolean seeDaleInWorld = true;
        if(scene.isAreaSightMode()){
            seeInArea = daleInAreaSight();
        }
        if(scene.isRealSightMode()){
            seeDaleInWorld = getSeeDaleInRealWorld();
        }
        return seeInArea && seeDaleInWorld && !dale.getMatch();
    }

    /**
     * Change the state of the fly.
     */
    private void changeState() {

        // Next state depends on current state.
        switch (state) {
            case IDLE:
                if(shouldChaseDale()){
                    fly.setAngry(true);
                    state = FSMState.CHASE;
                }
                break;

            case CHASE:
                if (dale.getMatch()) {
                    fly.setAngry(false);
                    state = FSMState.IDLE;
                }
                break;

            default:
                // Unknown or unhandled state, should never get here
                assert (false);
                state = FSMState.IDLE; // If debugging is off
                break;
        }

    }

    // Convert a world coordinate into grid index
    private int cToG(float coordinate) {
        return (int) Math.floor(coordinate);
    }

    // Compute euclidean distance between two nodes
    private float distance(int x1, int y1, int x2, int y2) {
        return (float) (Math.sqrt(Math.pow((double) x1 - x2, 2) + Math.pow((double) y1 - y2, 2)));
    }

    private boolean inBounds(int x, int y, boolean [][] grid){
        return x >= 0 && x < grid.length && y >= 0 && y < grid[0].length;
    }

    private void setIdleDirection(){
        switch (fly.getIdleType()){
            case STATIONARY:
                dx = 0;
                dy = 0;
                break;
            case HORIZONTAL:
                if (Math.abs(dx) != 1.0 || dy != 0f){   // if a fly changes from CHASE to IDLE
                    dx = 1f;
                    dy = 0f;
                }
                boolean[][] grid = scene.getGrid();
                int nextx = cToG(fly.getX() + dx);
                int nexty = cToG(fly.getY() + dy);
                if(!inBounds(nextx, nexty, grid) || grid[nextx][nexty]){
                    dx = -dx;
                    dy = -dy;
                }
                break;
        }
    }

    private void findDirection() {
        boolean[][] grid = scene.getGrid();
        boolean[][] visited = new boolean[grid.length][grid[0].length];
        // Goal's x and y grids
        int gx = cToG(dale.getX());
        int gy = cToG(dale.getY());
        // Fly's x and y grids
        int fx = cToG(fly.getX());
        int fy = cToG(fly.getY());
        Node current = new Node(fx, fy, 0, distance(gx, gy, fx, fy));
        LinkedList<Node> explored = new LinkedList<Node>();
        LinkedList<Node> frontier = new LinkedList<Node>();
        frontier.add(current);
        visited[current.x][current.y] = true;
        Comparator<Node> comparator = new Comparator<Node>() {
            public int compare(Node n1, Node n2) {
                float cost1 = n1.g + n1.h;
                float cost2 = n2.g + n2.h;
                return (int) Math.signum(cost1 - cost2);
            }
        };
        Node goal = null;
        while (!frontier.isEmpty()) {
            Node n = Collections.min(frontier, comparator);
            if (n.x == gx && n.y == gy) {
                goal = n;
                break;
            }
            frontier.remove(n);
            explored.add(n);
            for (int i =-1;i<= 1;i++) {
                for (int j=-1;j<=1;j++) {
                    int nextx = n.x + i;
                    int nexty = n.y + j;
                    if (!inBounds(nextx, nexty, grid)) {
                        continue;
                    }
                    if(grid[nextx][nexty]){
                        continue;
                    }
                    if (!visited[nextx][nexty]) {
                        if (Math.abs(i * j) == 1) {
                            if (grid[n.x + i][n.y] || grid[n.x][n.y + j]) {
                                continue;
                            }
                        }
                        visited[nextx][nexty] = true;
                        float dist1 = distance(n.x, n.y, nextx, nexty);
                        float dist2 = distance(nextx, nexty, gx, gy);
                        Node next = new Node(nextx, nexty, n.g + dist1, dist2);
                        next.parent = n;
                        frontier.add(next);
                    }else{
                        Node next = null;
                        for(Node node : frontier){
                            if(node.x == nextx && node.y == nexty){
                                next = node;
                                break;
                            }
                        }
                        if(next == null){
                            continue;
                        }
                        float dist1 = distance(n.x, n.y, nextx, nexty);
                        if(dist1 < next.g){
                            next.g = dist1;
                            next.parent = n;
                        }

                    }
                }
            }

        }

        // When there is no path to dale, just stay
        // TODO: May be different if idle behavior is more complex
        if(goal == null){
            dx = 0;
            dy = 0;
            return;
        }

        // In corner cases (won or losed, but haven't reset), dale and fly are in one grid, so goal has no parent
        if(goal.parent == null){
            return;
        }

        // backtrack
        while (goal.parent != current) {
            goal = goal.parent;
        }

        // set dx, dy
        Node next = goal;
        dx = next.x - fly.getX()+0.5f;
        dy = next.y - fly.getY()+0.5f;
        dx = dx / (float) Math.sqrt(dx * dx + dy * dy);
        dy = dy / (float) Math.sqrt(dx * dx + dy * dy);

        // debugging message
//        System.out.println("hi");
//        System.out.println(gx);
//        System.out.println(gy);
//        System.out.println(fly.getX());
//        System.out.println(fly.getY());
//        System.out.println(next.x);
//        System.out.println(next.y);
//        System.out.println(dx);
//        System.out.println(dy);
    }
}
