package edu.cornell.gdiac.discodale;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.discodale.models.ColorRegionModel;
import edu.cornell.gdiac.discodale.models.DaleColor;
import edu.cornell.gdiac.discodale.models.ScaffoldType;
import edu.cornell.gdiac.discodale.models.SceneModel;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class LevelLoader {

    /** The texture for brick and reflective walls */
    protected TextureRegion brickTile;
    protected TextureRegion reflectiveTile;
    protected Map<ScaffoldType, TextureRegion> brickScaffolds;
    protected Map<ScaffoldType, TextureRegion> reflectiveScaffolds;
    /** The texture for the exit condition */
    protected TextureRegion goalTile;

    private int tileWidth;
    private int tileHeight;
    private float tileScale;

    private Rectangle levelBounds;
    private Rectangle bounds;
    private Vector2 scale;

    public LevelLoader(TextureRegion brickTile, TextureRegion reflectiveTile, Map<ScaffoldType, TextureRegion> brickScaffolds, Map<ScaffoldType, TextureRegion> reflectiveScaffolds, TextureRegion goalTile, float width, float height) {
        this.brickTile = brickTile;
        this.reflectiveTile = reflectiveTile;
        this.brickScaffolds = brickScaffolds;
        this.reflectiveScaffolds = reflectiveScaffolds;
        this.goalTile = goalTile;
        this.bounds = new Rectangle(0, 0, width, height);
    }

    public SceneModel load(JsonValue json, JsonValue defaults) {
        this.tileWidth = 64; // json.getInt("tilewidth");
        this.tileHeight = 64; // json.getInt("tileheight");
        this.tileScale = this.tileWidth / (float) json.getInt("tilewidth");
        this.bounds = new Rectangle(
                0, 0,
                json.getInt("width"),
                json.getInt("height")
        );
        this.levelBounds = new Rectangle(
                0, 0,
                json.getInt("width") * this.tileWidth,
                json.getInt("height") * this.tileHeight
        );

        this.scale = new Vector2(
                this.levelBounds.getWidth() / this.bounds.getWidth(),
                this.levelBounds.getHeight() / this.bounds.getHeight()
        );

        SceneModel.ColorMovement m = SceneModel.ColorMovement.NO_MOVEMENT;
        boolean darkMode = false;
        for (JsonValue prop : json.get("properties")) {
            if (prop.getString("name").equals("colorMode")) {
                switch (prop.getInt("value")) {
                    case 0:
                        m = SceneModel.ColorMovement.NO_MOVEMENT;
                        break;
                    case 1:
                        m = SceneModel.ColorMovement.SCROLL_HORIZONTAL;
                        break;
                    case 2:
                        m = SceneModel.ColorMovement.SCROLL_VERTICAL;
                        break;
                    case 3:
                        m = SceneModel.ColorMovement.ROTATE;
                        break;
                }
            } else if (prop.getString("name").equalsIgnoreCase("darkMode")) {
                darkMode = prop.getBoolean("value");
            }
        }
        SceneModel model = new SceneModel(this.bounds, m, this.tileWidth);
        model.setBrickTexture(this.brickTile);
        model.setReflectiveTexture(this.reflectiveTile);
        model.setBrickScaffolds(this.brickScaffolds);
        model.setReflectiveScaffolds(this.reflectiveScaffolds);
        model.setGoalTexture(this.goalTile);
        model.setDarkMode(darkMode);

        for (JsonValue layer : json.get("layers")) {
            if (layer.getString("name").equals("colors")) {
                addColors(model, layer);
            } else if (layer.getString("name").equals("platforms")) {
                addPlatforms(model, layer, defaults);
            }
        }

        return model;
    }

    private void addColors(SceneModel model, JsonValue colors) {
        for (JsonValue o : colors.get("objects")) {
            float cx = o.getFloat("x") * this.tileScale;
            float cy = o.getFloat("y") * this.tileScale;
            if (o.getString("name").equalsIgnoreCase("colorwheel")) {
                model.setCenterOfRotation(new Vector2(
                        cx + o.getFloat("width") * this.tileScale /2,
                        this.levelBounds.getHeight() - o.getFloat("height") * this.tileScale /2 - cy
                ));
            } else {
                float[] vertices = toPrimitive(StreamSupport.stream(o.get("polygon").spliterator(), false)
                        .flatMap(p -> Stream.of(p.getFloat("x") * this.tileScale + cx, this.levelBounds.getHeight() - p.getFloat("y") * this.tileScale - cy))
                        .toArray(Float[]::new));
                DaleColor color = mapColor(o.getString("type"));
                DaleColor[] seq = null;
                if (o.has("properties")) for (JsonValue prop : o.get("properties")) {
                    if (prop.getString("name").equalsIgnoreCase("colorseq")) {
                        seq = Arrays.stream(prop.getString("value").split(","))
                                .map(String::trim)
                                .map(Integer::parseInt)
                                .map(LevelLoader::mapColor)
                                .toArray(DaleColor[]::new);
                    }
                }
                ColorRegionModel crm = new ColorRegionModel(color, vertices, seq);
                model.addColorRegion(crm);
            }
        }
    }

    private void addPlatforms(SceneModel model, JsonValue platforms, JsonValue defaults) {
        int[] data = platforms.get("data").asIntArray();
        int width = platforms.getInt("width");
        int height = platforms.getInt("height");
        for (int i = 0; i < height; i++) {
            int count = 1;
            for (int j = 0; j < width; j++) {
                int current = data[j + i * width];
                float[] v;
                v = new float[] {
                        (j + 1) * this.tileWidth, (height - i) * this.tileHeight,
                        (j + 1) * this.tileWidth, (height - (i + 1)) * this.tileHeight,
                        (j + 1 - count) * this.tileWidth, (height - (i + 1)) * this.tileHeight,
                        (j + 1 - count) * this.tileWidth, (height - i) * this.tileHeight,
                };

                v = scaleRect(v);

                switch (current) {
                    case 0:
                        break;
                    case 1:
                        model.addBrickWall(v, "brick" + (j + i * width), defaults);
                        break;
                    case 2:
                        model.addReflectiveWall(v, "reflective" + (j + i * width), defaults);
                        break;
                    case 8:
                        model.setDaleStart(
                                ((j + 1) * this.tileWidth - (float) this.tileWidth / 2) / scale.x,
                                ((height - i) * this.tileHeight - (float) this.tileHeight / 2) / scale.y
                        );
                        break;
                    case 9:
                        model.addFly(
                                ((j + 1) * this.tileWidth - (float) this.tileWidth / 2) / scale.x,
                                ((height - i) * this.tileHeight - (float) this.tileHeight / 2) / scale.y
                        );
                        break;
                    case 11:
                        model.setGoal(
                                ((j + 1) * this.tileWidth - (float) this.tileWidth / 2) / scale.x,
                                ((height - i) * this.tileHeight - (float) this.tileHeight / 2) / scale.y
                        );
                        break;
                    case 12:
                        model.addBrickScaffold(v, "brickscaffold" + (j + i * width), defaults, ScaffoldType.HORIZONTAL);
                        break;
                    case 13:
                        model.addReflectiveScaffold(v, "reflectivescaffold" + (j + i * width), defaults, ScaffoldType.HORIZONTAL);
                        break;
                    case 14:
                        model.addBrickScaffold(v, "brickscaffold" + (j + i * width), defaults, ScaffoldType.VERTICAL);
                        break;
                    case 16:
                        model.addReflectiveScaffold(v, "reflectivescaffold" + (j + i * width), defaults, ScaffoldType.VERTICAL);
                        break;
                    case 18:
                        model.addBrickScaffold(v, "brickscaffold" + (j + i * width), defaults, ScaffoldType.DOWN_LEFT);
                        break;
                    case 19:
                        model.addReflectiveScaffold(v, "reflectivescaffold" + (j + i * width), defaults, ScaffoldType.UP_RIGHT);
                        break;
                    case 20:
                        model.addReflectiveScaffold(v, "reflectivescaffold" + (j + i * width), defaults, ScaffoldType.UP_LEFT);
                        break;
                    case 21:
                        model.addReflectiveScaffold(v, "reflectivescaffold" + (j + i * width), defaults, ScaffoldType.DOWN_RIGHT);
                        break;
                    case 22:
                        model.addReflectiveScaffold(v, "reflectivescaffold" + (j + i * width), defaults, ScaffoldType.DOWN_LEFT);
                        break;
                    case 23:
                        model.addBrickScaffold(v, "brickscaffold" + (j + i * width), defaults, ScaffoldType.UP_RIGHT);
                        break;
                    case 24:
                        model.addBrickScaffold(v, "brickscaffold" + (j + i * width), defaults, ScaffoldType.UP_LEFT);
                        break;
                    case 25:
                        model.addBrickScaffold(v, "brickscaffold" + (j + i * width), defaults, ScaffoldType.DOWN_RIGHT);
                        break;
                }
            }
        }
    }

    private float[] scaleRect(float[] rect) {
        return new float[] {
                rect[0] / this.scale.x, rect[1] / this.scale.y,
                rect[2] / this.scale.x, rect[3] / this.scale.y,
                rect[4] / this.scale.x, rect[5] / this.scale.y,
                rect[6] / this.scale.x, rect[7] / this.scale.y,
        };
    }

    private DaleColor mapColor(String colorType) {
        DaleColor color = null;
        switch (colorType) {
            case "color1":
                color = DaleColor.PINK;
                break;
            case "color2":
                color = DaleColor.BLUE;
                break;
            case "color3":
                color = DaleColor.GREEN;
                break;
            case "color4":
                color = DaleColor.ORANGE;
                break;
            case "color5":
                color = DaleColor.PURPLE;
                break;
        }
        return color;
    }

    private static DaleColor mapColor(int colorType) {
        DaleColor color = DaleColor.PINK;
        switch (colorType) {
            case 1:
                color = DaleColor.PINK;
                break;
            case 2:
                color = DaleColor.BLUE;
                break;
            case 3:
                color = DaleColor.GREEN;
                break;
            case 4:
                color = DaleColor.ORANGE;
                break;
            case 5:
                color = DaleColor.PURPLE;
                break;
        }
        return color;
    }

    private float[] toPrimitive(Float[] arr) {
        float[] fixed = new float[arr.length];
        for (int i = 0; i < arr.length; i++) {
            fixed[i] = arr[i];
        }
        return fixed;
    }
}
