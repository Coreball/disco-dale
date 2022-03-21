package edu.cornell.gdiac.discodale.controllers;

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
    private static int BIG_NUM = 20000; // Used in A* search

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

    public float getDx() {
        return dx;
    }

    public float getDy() {
        return dy;
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
                // TODO: temporary implementation. May be more complex
                dx = 0;
                dy = 0;
                break;
            case CHASE:
                findDirection();
                break;
            default:
                assert (false);
                break;
        }
    }

    /**
     * Change the state of the fly.
     */
    private void changeState() {

        // Next state depends on current state.
        switch (state) {
            case IDLE:
                if (!dale.getMatch()) {
                    state = FSMState.CHASE;
                }
                break;

            case CHASE:
                if (dale.getMatch()) {
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
        // goal does not need g cost, so 0
        Node goal = new Node(gx, gy, 0, 0);
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
        boolean find = false;
        while (!frontier.isEmpty()) {
            Node n = Collections.min(frontier, comparator);
            frontier.remove(n);
            explored.add(n);
            int[] xs = new int[]{-1,1,0,0};
            int[] ys = new int[]{0,0,-1,1};
            for (int i : xs) {
                for (int j :ys) {
                    int nextx = n.x + i;
                    int nexty = n.y + j;
                    if (nextx < 0 || nextx > grid.length - 1 || nexty < 0 || nexty > grid[0].length - 1) {
                        continue;
                    }
                    if(i!=0&&j!=0){
                        continue;
                    }
                    if (nextx == gx && nexty == gy) {
                        goal.parent = n;
                        find = true;
                        break;
                    }
                    if (!visited[nextx][nexty]) {
                        visited[nextx][nexty] = true;
                        float dist1 = distance(n.x, n.y, nextx, nexty);
                        if (grid[nextx][nexty]) {
                            dist1 = BIG_NUM;
                        }
                        if (Math.abs(i * j) == 1) {
                            if (nextx+i>0&&nextx+i<grid.length&&grid[nextx + i][nexty] || nexty+j>0&&nexty+j<grid[0].length&&grid[nextx][nexty + j]) {
                                dist1 = BIG_NUM;
                            }
                        }
                        float dist2 = distance(nextx, nexty, gx, gy);
                        Node next = new Node(nextx, nexty, n.g + dist1, dist2);
                        frontier.add(next);
                        next.parent = n;
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
                        if (grid[nextx][nexty]) {
                            dist1 = BIG_NUM;
                        }
//                        if (Math.abs(i * j) == 1) {
//                            if (nextx+i>0&&nextx+i<grid.length&&grid[nextx + i][nexty] || nexty+j>0&&nexty+j<grid[0].length&&grid[nextx][nexty + j]) {
//                                dist2 = BIG_NUM;
//                            }
//                        }
                        if(dist1 < next.g){
                            next.g = dist1;
                            next.parent = n;
                        }

                    }
                }
                if (find) {
                    break;
                }
            }

        }

        // Debugging message
        if (goal.parent == null) {
            System.out.println("NOOOOOOOOO");
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
    }
}
