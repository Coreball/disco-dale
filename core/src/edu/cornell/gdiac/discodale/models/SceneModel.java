package edu.cornell.gdiac.discodale.models;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.discodale.GameCanvas;
import edu.cornell.gdiac.discodale.obstacle.BoxObstacle;
import edu.cornell.gdiac.discodale.obstacle.Obstacle;
import edu.cornell.gdiac.discodale.obstacle.PolygonObstacle;
import edu.cornell.gdiac.util.PooledList;
import java.util.LinkedList;

public class SceneModel {
    private static int GRID_WIDTH = 32;
    private static int GRID_HEIGHT = 18;

    /** Reference to the goalDoor (for collision detection) */
    public BoxObstacle goalDoor;
    /** Color regions */
    private ColorRegionModel[] colorRegions;

    /** The texture for walls and platforms */
    protected TextureRegion wallTile;
    /** The texture for the exit condition */
    protected TextureRegion goalTile;

    /** All the objects in the world. */
    protected PooledList<Obstacle> objects = new PooledList<>();

    /** The boundary of the world */
    protected Rectangle bounds;
    protected Vector2 scale;

    private int canvasWidth;
    private int canvasHeight;

    /** */
    private Vector2 pointCache;

    /** The grid: whether a tile has obstacle */
    private boolean[][] grid = new boolean[GRID_WIDTH][GRID_HEIGHT];

    public SceneModel(Rectangle bounds) {
        this.bounds = new Rectangle(bounds);
        this.scale = new Vector2();
    }

    public void setWallTexture(TextureRegion texture) {
        this.wallTile = texture;
    }

    public void setGoalTexture(TextureRegion texture) {
        this.goalTile = texture;
    }

    public void setCanvas(GameCanvas canvas) {
        this.canvasWidth = canvas.getWidth();
        ;
        this.canvasHeight = canvas.getHeight();
        this.scale.x = canvas.getWidth() / bounds.getWidth();
        this.scale.y = canvas.getHeight() / bounds.getHeight();
    }

    public boolean[][] getGrid() {
        return grid;
    }

    public ColorRegionModel[] getColorRegions() {
        return colorRegions;
    }

    public void updateGrid() {
        // Test if center point of a grid is in any fixture
        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {
                boolean temp = false;
                for (Obstacle object : objects) {
                    if(object == goalDoor){
                        continue;
                    }
                    if (object != null && object.getBody() != null) {
                        for (Fixture fixture : object.getBody().getFixtureList()) {
                            if (fixture.testPoint(i + 0.5f, j + 0.5f)) {
                                temp = true;
                                break;
                            }
                        }
                        if (temp) {
                            break;
                        }
                    }

                }
                grid[i][j] = temp;
            }
        }
//        //Debugging message
//        for (int j = GRID_HEIGHT - 1; j >= 0; j--) {
//            for (int i = 0; i < GRID_WIDTH; i++) {
//                System.out.print(grid[i][j]?1:0);
//                System.out.print(" ");
//            }
//            System.out.println("");
//        }
//        System.out.println("");
    }

    /**
     * Lays out the game geography.
     */
    public void populateLevel(JsonValue walljv, JsonValue platjv, JsonValue defaults, JsonValue goal) {
        // Add level goal
        float dwidth = goalTile.getRegionWidth() / scale.x;
        float dheight = goalTile.getRegionHeight() / scale.y;
        JsonValue goalpos = goal.get("pos");
//        System.out.println(goalpos.getFloat(0));
        goalDoor = new BoxObstacle(goalpos.getFloat(0), goalpos.getFloat(1), dwidth, dheight);
        goalDoor.setBodyType(BodyDef.BodyType.StaticBody);
        goalDoor.setDensity(goal.getFloat("density", 0));
        goalDoor.setFriction(goal.getFloat("friction", 0));
        goalDoor.setRestitution(goal.getFloat("restitution", 0));
        goalDoor.setSensor(true);
        goalDoor.setDrawScale(scale);
        goalDoor.setTexture(goalTile);
        goalDoor.setName("goal");
        addObject(goalDoor);

        // Create color regions
        colorRegions = new ColorRegionModel[3];
        float[] vertices;
        vertices = new float[] { 0.0f, 0.0f, 0.0f, this.canvasHeight, this.canvasWidth / 2f, 0f };
        colorRegions[0] = new ColorRegionModel(DaleColor.RED, vertices);
        vertices = new float[] { this.canvasWidth / 2f, 0f, this.canvasWidth, this.canvasHeight, 0f,
                this.canvasHeight };
        colorRegions[1] = new ColorRegionModel(DaleColor.YELLOW, vertices);
        vertices = new float[] { this.canvasWidth, 0f, this.canvasWidth, this.canvasHeight, this.canvasWidth / 2f, 0f };
        colorRegions[2] = new ColorRegionModel(DaleColor.BLUE, vertices);

        String wname = "wall";
        for (int ii = 0; ii < walljv.size; ii++) {
            PolygonObstacle obj;
            obj = new PolygonObstacle(walljv.get(ii).asFloatArray(), 0, 0);
            obj.setBodyType(BodyDef.BodyType.StaticBody);
            obj.setDensity(defaults.getFloat("density", 0.0f));
            obj.setFriction(defaults.getFloat("friction", 0.0f));
            obj.setRestitution(defaults.getFloat("restitution", 0.0f));
            obj.setDrawScale(scale);
            obj.setTexture(wallTile);
            obj.setName(wname + ii);
            addObject(obj);
        }

        String pname = "platform";
        for (int ii = 0; ii < platjv.size; ii++) {
            PolygonObstacle obj;
            obj = new PolygonObstacle(platjv.get(ii).asFloatArray(), 0, 0);
            obj.setBodyType(BodyDef.BodyType.StaticBody);
            obj.setDensity(defaults.getFloat("density", 0.0f));
            obj.setFriction(defaults.getFloat("friction", 0.0f));
            obj.setRestitution(defaults.getFloat("restitution", 0.0f));
            obj.setDrawScale(scale);
            obj.setTexture(wallTile);
            obj.setName(pname + ii);
            addObject(obj);
        }

        updateGrid();
    }

    /**
     * Immediately adds the object to the physics world
     *
     * param obj The object to add
     */
    protected void addObject(Obstacle obj) {
        assert inBounds(obj) : "Object is not in bounds";
        objects.add(obj);
    }

    /**
     * Returns true if the object is in bounds.
     *
     * This assertion is useful for debugging the physics.
     *
     * @param obj The object to check.
     *
     * @return true if the object is in bounds.
     */
    public boolean inBounds(Obstacle obj) {
        boolean horiz = (bounds.x <= obj.getX() && obj.getX() <= bounds.x + bounds.width);
        boolean vert = (bounds.y <= obj.getY() && obj.getY() <= bounds.y + bounds.height);
        return horiz && vert;
    }

    public void draw(GameCanvas canvas) {
        for (ColorRegionModel crm : colorRegions) {
            crm.draw(canvas);
        }
        for (Obstacle obj : objects) {
            obj.draw(canvas);
        }
    }

    public void drawDebug(GameCanvas canvas) {
        for(Obstacle obj : objects) {
            obj.drawDebug(canvas);
        }
    }

    public boolean activatePhysics(World world) {
        for (Obstacle object : objects) {
            if (!object.activatePhysics(world))
                return false;
        }

        return true;
    }

    public void reset(World world) {
        for (Obstacle obj : objects) {
            obj.deactivatePhysics(world);
        }
        objects.clear();
    }

    public void updateColorRegions(){
        DaleColor c1 = colorRegions[colorRegions.length-1].getColor();
        for(int i=colorRegions.length-1;i>=1;i--){
            colorRegions[i].setColor(colorRegions[i-1].getColor());
        }
        colorRegions[0].setColor(c1);
    }

}
