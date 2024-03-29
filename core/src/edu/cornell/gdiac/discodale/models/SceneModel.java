package edu.cornell.gdiac.discodale.models;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.discodale.GameCanvas;
import edu.cornell.gdiac.discodale.obstacle.BoxObstacle;
import edu.cornell.gdiac.discodale.obstacle.Obstacle;
import edu.cornell.gdiac.discodale.obstacle.PolygonObstacle;
import edu.cornell.gdiac.util.PooledList;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class SceneModel {

    private TextureRegion brickTile;
    private TextureRegion reflectiveTile;

    public enum ColorMovement {
        NO_MOVEMENT,
        SCROLL_HORIZONTAL,
        SCROLL_VERTICAL,
        ROTATE;
    }

    /** Window size */
    private float window_width;
    private float window_height;

    /** Reference to the goalDoor (for collision detection) */
    public BoxObstacle goalDoor;
    /** Color regions */
    private PooledList<ColorRegionModel> colorRegions;
    private ColorMovement colorMovement;
    // TODO: as inputs of scenemodel?
    private float colorRotationAmount = 0.5f;
    private float colorMovementAmount = 1f;
    private float colorMovementX = 512;
    private float colorMovementY = 288;

    private Vector2 centerOfRotation = null;

    /** Whether flies can start to chase Dale only if Dale is within an area of a radius or not */
    private boolean areaSightMode = true;

    /** Whether flies can start to chase Dale only if there is no obstacle between them or not */
    private boolean realSightMode = true;

    /** The radius of area of fly's sight */
    private float areaSightRadius = 10f;

    /** Whether Dale can only see an area around him or not. */
    private boolean darkMode = false;

    /** Whether there are moving spotlights in the level or not. */
    private boolean spotlightMode = false;
    private float spotlightRadius = 100f;
    private float[] spotlightPath = new float[]{1.0f,1.0f,200.0f,200.0f};

    private boolean hasColorChange;

    /** The texture for walls and platforms */
    protected Map<ScaffoldType, TextureRegion> brickScaffolds;
    protected Map<ScaffoldType, TextureRegion> reflectiveScaffolds;
    protected Map<WallType, TextureRegion> walls;
    /** The texture for the exit condition */
    protected TextureRegion goalTile;

    /** All the objects in the world. */
    protected PooledList<Obstacle> objects = new PooledList<>();

    private PooledList<Obstacle> seeThroughObstacles = new PooledList<>();

    private PooledList<TutorialElement> tutorialElements = new PooledList<>();

    /** The boundary of the world */
    protected Rectangle bounds;
    protected Vector2 scale;

    private Vector2 daleStart = new Vector2();
    private PooledList<Vector2> flyLocations = new PooledList<>();

    private int tileSize;

    /** */
    private Vector2 pointCache;

    /** The grid: whether a tile has obstacle */
    private boolean[][] grid; // = new boolean[GRID_WIDTH][GRID_HEIGHT];

    public SceneModel(Rectangle bounds, ColorMovement movement, int tileSize) {
        this.tileSize = tileSize;
        this.bounds = new Rectangle(bounds);
        this.grid = new boolean[(int) bounds.getWidth()][(int) bounds.getHeight()];
        System.out.println(bounds);
//        this.scale = new Vector2(1024 / bounds.getWidth(), 576 / bounds.getHeight()); //todo
        this.scale = new Vector2(64f, 64f);
        System.out.println("scene scale: " + this.scale);
        this.colorMovement = movement;
        this.colorRegions = new PooledList<>();
        this.window_width = bounds.getWidth()* tileSize;
        this.window_height = bounds.getHeight()* tileSize;
    }

    public int getTileSize() {
        return tileSize;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public Vector2 getCenterOfRotation() {
        return centerOfRotation;
    }

    public void setCenterOfRotation(Vector2 centerOfRotation) {
        this.centerOfRotation = centerOfRotation;
    }

    public void setBrickTexture(TextureRegion texture) {
        this.brickTile = texture;
    }

    public void setGoalTexture(TextureRegion texture) {
        this.goalTile = texture;
    }

    public void setReflectiveTexture(TextureRegion texture) {
        this.reflectiveTile = texture;
    }

    public void setBrickScaffolds(Map<ScaffoldType, TextureRegion> brickScaffolds) {
        this.brickScaffolds = brickScaffolds;
    }

    public void setReflectiveScaffolds(Map<ScaffoldType, TextureRegion> reflectiveScaffolds) {
        this.reflectiveScaffolds = reflectiveScaffolds;
    }

    public void setWalls(Map<WallType, TextureRegion> walls) {
        this.walls = walls;
    }

    public Vector2 getDaleStart() {
        return daleStart;
    }

    public void setDaleStart(float x, float y) {
        this.daleStart.set(x, y);
    }

    public PooledList<Vector2> getFlyLocations() {
        return this.flyLocations;
    }

    public void addFly(float x, float y) {
        this.flyLocations.add(new Vector2(x, y));
    }

    public void addTutorialElement(TutorialElement te) {
        this.tutorialElements.add(te);
    }

    public boolean isAreaSightMode() {
        return areaSightMode;
    }

    public boolean isRealSightMode() {
        return realSightMode;
    }

    public float getAreaSightRadius() {
        return areaSightRadius;
    }
    public boolean isDarkMode() {
        return darkMode;
    }

    public PooledList<Obstacle> getSeeThroughObstacles() {
        return seeThroughObstacles;
    }

    public void setColorChange(){
        hasColorChange = false;
        for (ColorRegionModel cr: colorRegions){
            if (cr.getSeq() != null)
                hasColorChange = true;
        }
    }

    public boolean getColorChange() {
        return hasColorChange;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }

    public boolean isSpotlightMode() {
        return spotlightMode;
    }

    public void setSpotlightMode(boolean spotlightMode) {
        this.spotlightMode = spotlightMode;
    }

    public float[] getSpotlightPath() {
        return spotlightPath;
    }

    public void setSpotlightPath(float[] spotlightPath) {
        this.spotlightPath = spotlightPath;
    }

    public float getSpotlightRadius() {
        return spotlightRadius;
    }

    public void setSpotlightRadius(float spotlightRadius) {
        this.spotlightRadius = spotlightRadius;
    }

    public void setCanvas(GameCanvas canvas) {
//        this.scale.x = canvas.getWidth() / bounds.getWidth();
//        this.scale.y = canvas.getHeight() / bounds.getHeight();
//        canvas.setWidth((int) (this.bounds.getWidth() * 32));
//        canvas.setHeight((int) (this.bounds.getHeight() * 32));
        for (Obstacle object : this.objects) {
            object.setDrawScale(this.scale);
        }
    }

    public boolean[][] getGrid() {
        return grid;
    }

    public List<ColorRegionModel> getColorRegions() {
        return colorRegions;
    }

    public void addColorRegion(ColorRegionModel crm) {
        if(colorMovement==ColorMovement.SCROLL_HORIZONTAL || colorMovement == ColorMovement.SCROLL_VERTICAL){
            float[] vertices = crm.getVertices().clone();
            DaleColor[] seq = crm.getSeq() == null ? null : crm.getSeq().clone();
            DaleColor color = crm.getColor();
            for(int i=-1;i<=1;i++){
                for(int j=-1;j<=1;j++){
                    ColorRegionModel newCrm = new ColorRegionModel(color,vertices,seq);
                    newCrm.move(i*window_width,j*window_height);
                    this.colorRegions.add(newCrm);
                }
            }
        }else{
            this.colorRegions.add(crm);
        }



    }

    /**
     * Get all the colors present in a level.
     * Includes colors that are part of color sequences and not visible at the start.
     *
     * @return ordered array of Dale colors present in the level
     */
    public DaleColor[] getPossibleColors() {
        return getColorRegions()
                .stream()
                .flatMap(colorRegion -> colorRegion.getSeq() == null
                        ? Stream.of(colorRegion.getColor())
                        : Stream.of(colorRegion.getSeq()))
                .distinct()
                .sorted()
                .toArray(DaleColor[]::new);
    }

    public void updateGrid() {
        // Test if center point of a grid is in any fixture
        for (int i = 0; i < bounds.getWidth(); i++) {
            for (int j = 0; j < bounds.getHeight(); j++) {
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

    public void updateColorRegionMovement(){
        switch (colorMovement){
            case NO_MOVEMENT:
                break;
            case SCROLL_HORIZONTAL:
                for(ColorRegionModel cr:colorRegions){
                    cr.move(colorMovementAmount,0);
                    cr.move(cr.testBound(window_width,0)*window_width*3,0);
                }
                break;
            case SCROLL_VERTICAL:
                for(ColorRegionModel cr:colorRegions){
                    cr.move(0,-colorMovementAmount);
                    cr.move(0,cr.testBound(window_height,1)*window_height*3);
                }
                break;
            case ROTATE:
                for(ColorRegionModel cr:colorRegions){
                    cr.rotateAround(centerOfRotation.x,centerOfRotation.y,colorRotationAmount);
                }
                break;
            default:
                break;
        }
    }

    /**
     * Lays out the game geography.
     */
    public void populateLevel(JsonValue walljv, JsonValue platjv, JsonValue defaults, JsonValue goal) {
        // Add level goal
//        float dwidth = goalTile.getRegionWidth() / (scale.x);
//        float dheight = goalTile.getRegionHeight() / (scale.y);
//        JsonValue goalpos = goal.get("pos");
////        System.out.println(goalpos.getFloat(0));
//        goalDoor = new BoxObstacle(goalpos.getFloat(0), goalpos.getFloat(1), dwidth, dheight);
//        goalDoor.setBodyType(BodyDef.BodyType.StaticBody);
//        goalDoor.setDensity(goal.getFloat("density", 0));
//        goalDoor.setFriction(goal.getFloat("friction", 0));
//        goalDoor.setRestitution(goal.getFloat("restitution", 0));
//        goalDoor.setSensor(true);
//        goalDoor.setDrawScale(scale);
//        goalDoor.setTexture(goalTile);
//        goalDoor.setName("goal");
//        addObject(goalDoor);

        // Create color regions
//        colorRegions = new ColorRegionModel[3];
//        float[] vertices;
//        vertices = new float[] { 0.0f, 0.0f, 0.0f, this.canvasHeight, this.canvasWidth / 2f, 0f };
//        colorRegions[0] = new ColorRegionModel(DaleColor.RED, vertices);
//        vertices = new float[] { this.canvasWidth / 2f, 0f, this.canvasWidth, this.canvasHeight, 0f,
//                this.canvasHeight };
//        colorRegions[1] = new ColorRegionModel(DaleColor.YELLOW, vertices);
//        vertices = new float[] { this.canvasWidth, 0f, this.canvasWidth, this.canvasHeight, this.canvasWidth / 2f, 0f };
//        colorRegions[2] = new ColorRegionModel(DaleColor.BLUE, vertices);

//        String wname = "wall";
//        for (int ii = 0; ii < walljv.size; ii++) {
//            PolygonObstacle obj;
//            obj = new PolygonObstacle(walljv.get(ii).asFloatArray(), 0, 0);
//            obj.setBodyType(BodyDef.BodyType.StaticBody);
//            obj.setDensity(defaults.getFloat("density", 0.0f));
//            obj.setFriction(defaults.getFloat("friction", 0.0f));
//            obj.setRestitution(defaults.getFloat("restitution", 0.0f));
//            obj.setDrawScale(scale);
//            obj.setTexture(wallTile);
//            obj.setName(wname + ii);
//            addObject(obj);
//        }
//
//        String pname = "platform";
//        for (int ii = 0; ii < platjv.size; ii++) {
//            PolygonObstacle obj;
//            obj = new PolygonObstacle(platjv.get(ii).asFloatArray(), 0, 0);
//            obj.setBodyType(BodyDef.BodyType.StaticBody);
//            obj.setDensity(defaults.getFloat("density", 0.0f));
//            obj.setFriction(defaults.getFloat("friction", 0.0f));
//            obj.setRestitution(defaults.getFloat("restitution", 0.0f));
//            obj.setDrawScale(scale);
//            obj.setTexture(wallTile);
//            obj.setName(pname + ii);
//            addObject(obj);
//        }
        System.out.println(objects.size());

        updateGrid();
    }

    public void addBrickWall(float[] vertices, String name, JsonValue defaults, WallType type) {
        PolygonObstacle obj;
        obj = new PolygonObstacle(vertices, 0, 0);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(defaults.getFloat("density", 0.0f));
        obj.setFriction(defaults.getFloat("friction", 0.0f));
        obj.setRestitution(defaults.getFloat("restitution", 0.0f));
        obj.setDrawScale(scale);
        obj.setTexture(walls.get(type));
        obj.setName(name);
        Filter objFilter = new Filter();
        objFilter.categoryBits = 0b00000001;
        objFilter.maskBits     = 0b00011100;
        obj.setFilterData(objFilter);
        addObject(obj);
    }

    public void addReflectiveWall(float[] vertices, String name, JsonValue defaults) {
        PolygonObstacle obj;
        obj = new PolygonObstacle(vertices, 0, 0);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(defaults.getFloat("density", 0.0f));
        obj.setFriction(defaults.getFloat("friction", 0.0f));
        obj.setRestitution(defaults.getFloat("restitution", 0.0f));
        obj.setDrawScale(scale);
        obj.setTexture(reflectiveTile);
        obj.setName(name);
        Filter objFilter = new Filter();
        objFilter.categoryBits = 0b00000001;
        objFilter.maskBits     = 0b00011100;
        obj.setFilterData(objFilter);
        addObject(obj);
    }

    public void addBrickScaffold(float[] vertices, String name, JsonValue defaults, ScaffoldType type) {
        PolygonObstacle obj;
        obj = new PolygonObstacle(vertices, 0, 0);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(defaults.getFloat("density", 0.0f));
        obj.setFriction(defaults.getFloat("friction", 0.0f));
        obj.setRestitution(defaults.getFloat("restitution", 0.0f));
        obj.setDrawScale(scale);
        obj.setTexture(brickScaffolds.get(type));
        obj.setName(name);
        Filter objFilter = new Filter();
        objFilter.categoryBits = 0b00000001;
        objFilter.maskBits     = 0b00011100;
        obj.setFilterData(objFilter);
        addObject(obj);
        seeThroughObstacles.add(obj);
    }

    public void addReflectiveScaffold(float[] vertices, String name, JsonValue defaults, ScaffoldType type) {
        PolygonObstacle obj;
        obj = new PolygonObstacle(vertices, 0, 0);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(defaults.getFloat("density", 0.0f));
        obj.setFriction(defaults.getFloat("friction", 0.0f));
        obj.setRestitution(defaults.getFloat("restitution", 0.0f));
        obj.setDrawScale(scale);
        obj.setTexture(reflectiveScaffolds.get(type));
        obj.setName(name);
        Filter objFilter = new Filter();
        objFilter.categoryBits = 0b00000001;
        objFilter.maskBits     = 0b00011100;
        obj.setFilterData(objFilter);
        addObject(obj);
        seeThroughObstacles.add(obj);
    }

    public void setGoal(float x, float y) {
        float dwidth = goalTile.getRegionWidth() / (scale.x);
        float dheight = goalTile.getRegionHeight() / (scale.y);
        System.out.println(x + " " + y);
        goalDoor = new BoxObstacle(x, y, dwidth, dheight);
        goalDoor.setBodyType(BodyDef.BodyType.StaticBody);
        goalDoor.setDensity(0);
        goalDoor.setFriction(0);
        goalDoor.setRestitution(0);
        goalDoor.setSensor(true);
        goalDoor.setDrawScale(scale);
        goalDoor.setTexture(goalTile);
        goalDoor.setName("goal");
        Filter goalFilter = new Filter();
        goalFilter.categoryBits = 0b00000010;
        goalFilter.maskBits     = 0b00001000;
        goalDoor.setFilterData(goalFilter);
        addObject(goalDoor);
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
        for (TutorialElement tutorialElement : this.tutorialElements) {
            tutorialElement.draw(canvas);
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
        seeThroughObstacles.clear();
    }

    public void updateColorRegions(){
//        DaleColor c1 = colorRegions[colorRegions.length-1].getColor();
//        for(int i=colorRegions.length-1;i>=1;i--){
//            colorRegions[i].setColor(colorRegions[i-1].getColor());
//        }
//        colorRegions[0].setColor(c1);
        for (ColorRegionModel cr : colorRegions){
            cr.switchColor();
        }
    }

}
