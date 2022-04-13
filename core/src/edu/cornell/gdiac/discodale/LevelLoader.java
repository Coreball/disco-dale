package edu.cornell.gdiac.discodale;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.discodale.models.ColorRegionModel;
import edu.cornell.gdiac.discodale.models.DaleColor;
import edu.cornell.gdiac.discodale.models.SceneModel;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class LevelLoader {

    /** The texture for walls and platforms */
    protected TextureRegion wallTile;
    protected TextureRegion platformTile;
    /** The texture for the exit condition */
    protected TextureRegion goalTile;

    private int tileWidth;
    private int tileHeight;

    private Rectangle canvasBounds; // = new Rectangle(0, 0, Constants.DEFAULT_WIDTH, Constants.DEFAULT_HEIGHT);
    private Rectangle levelBounds;
    private Rectangle bounds;
    private Vector2 scale;
//    private Vector2 scale = new Vector2(1024 / Constants.DEFAULT_WIDTH, 576 / Constants.DEFAULT_HEIGHT);

    public LevelLoader(TextureRegion wallTile, TextureRegion platformTile, TextureRegion goalTile, float width, float height) {
        this.wallTile = wallTile;
        this.platformTile = platformTile;
        this.goalTile = goalTile;
        this.bounds = new Rectangle(0, 0, width, height);
    }

    public SceneModel load(JsonValue json, JsonValue defaults, Rectangle canvasBounds) {
        this.tileWidth = json.getInt("tilewidth");
        this.tileHeight = json.getInt("tileheight");
        this.canvasBounds = new Rectangle(canvasBounds);
        this.levelBounds = new Rectangle(
                0, 0,
                json.getInt("width") * this.tileWidth,
                json.getInt("height") * this.tileHeight
        );

//        System.out.println(this.levelBounds.toString());

        this.scale = new Vector2(
                this.levelBounds.getWidth() / this.bounds.getWidth(),
                this.levelBounds.getHeight() / this.bounds.getHeight()
        );

        SceneModel.ColorMovement m = SceneModel.ColorMovement.NO_MOVEMENT;
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
            }
        }
        SceneModel model = new SceneModel(this.bounds, m);
        model.setWallTexture(this.wallTile);
        model.setGoalTexture(this.goalTile);
        model.setPlatformTexture(this.platformTile);

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
            float cx = o.getFloat("x");
            float cy = o.getFloat("y");
            if (o.getString("name").equalsIgnoreCase("colorwheel")) {
                model.setCenterOfRotation(new Vector2(
                        cx + o.getFloat("width"),
                        this.levelBounds.getHeight() - o.getFloat("height") - cy
                ));
            } else {
                float[] vertices = toPrimitive(StreamSupport.stream(o.get("polygon").spliterator(), false)
                        .flatMap(p -> Stream.of(p.getFloat("x") + cx, this.levelBounds.getHeight() - p.getFloat("y") - cy))
                        .toArray(Float[]::new));
                DaleColor color = mapColor(o.getString("type"));
                ColorRegionModel crm = new ColorRegionModel(color, vertices);
                model.addColorRegion(crm);
            }
        }
    }

    private void addPlatforms(SceneModel model, JsonValue platforms, JsonValue defaults) {
        int[] data = platforms.get("data").asIntArray();
        int width = platforms.getInt("width");
        int height = platforms.getInt("height");
        for (int i = 0; i < height; i++) {
            int last = 0;
            int count = 1;
            for (int j = 0; j < width; j++) {
                int current = data[j + i * width];
                if (current != last || j == width - 1) {
                    float[] v;
                    if (j == width - 1) {
                        v = new float[] {
                                width * this.tileWidth, (height - i) * this.tileHeight,
                                width * this.tileWidth, (height - (i + 1)) * this.tileHeight,
                                (width - count - 1) * this.tileWidth, (height - (i + 1)) * this.tileHeight,
                                (width - count - 1) * this.tileWidth, (height - i) * this.tileHeight,
                        };
                    } else {
                        v = new float[] {
                                j * this.tileWidth, (height - i) * this.tileHeight,
                                j * this.tileWidth, (height - (i + 1)) * this.tileHeight,
                                (j - count) * this.tileWidth, (height - (i + 1)) * this.tileHeight,
                                (j - count) * this.tileWidth, (height - i) * this.tileHeight,
                        };
                    }

                    v = scaleRect(v);

                    switch (last) {
                        case 0:
                            break;
                        case 1:
                            model.addPlatform(v, "platform" + (j + i * width), defaults);
                            break;
                        case 2:
                            model.addWall(v, "wall" + (j + i * width), defaults);
                            break;
                        case 8:
                            model.setDaleStart(
                                    (j * this.tileWidth - (float) this.tileWidth / 2) / scale.x,
                                    ((height - i) * this.tileHeight - (float) this.tileHeight / 2) / scale.y
                            );
                            break;
                        case 9:
                            model.addFly(
                                    (j * this.tileWidth - (float) this.tileWidth / 2) / scale.x,
                                    ((height - i) * this.tileHeight - (float) this.tileHeight / 2) / scale.y
                            );
                            break;
                        case 11:
                            model.setGoal(
                                    (j * this.tileWidth - (float) this.tileWidth / 2) / scale.x,
                                    ((height - i) * this.tileHeight - (float) this.tileHeight / 2) / scale.y
                            );
                            break;
                    }
                    count = 1;
                    if (j == width - 1) last = 0;
                    else last = current;
                } else {
                    count++;
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
        DaleColor color = DaleColor.RED;
        switch (colorType) {
            case "color1":
                color = DaleColor.RED;
                break;
            case "color2":
                color = DaleColor.BLUE;
                break;
            case "color3":
                color = DaleColor.YELLOW;
                break;
            case "color4":
                color = DaleColor.YELLOW;
                break;
            case "color5":
                color = DaleColor.YELLOW;
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
