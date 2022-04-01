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

    private Rectangle bounds = new Rectangle(0, 0, Constants.DEFAULT_WIDTH, Constants.DEFAULT_HEIGHT);
    private Vector2 scale = new Vector2(1024 / this.bounds.getWidth(), 576 / this.bounds.getHeight());

    public LevelLoader(TextureRegion wallTile, TextureRegion platformTile, TextureRegion goalTile) {
        this.wallTile = wallTile;
        this.platformTile = platformTile;
        this.goalTile = goalTile;
    }

    public SceneModel load(JsonValue json, JsonValue defaults) {
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
            float[] vertices = toPrimitive(StreamSupport.stream(o.get("polygon").spliterator(), false)
                    .flatMap(p -> Stream.of(p.getFloat("x") + cx, 576 - p.getFloat("y") - cy))
                    .toArray(Float[]::new));
            DaleColor color = mapColor(o.getString("type"));
            ColorRegionModel crm = new ColorRegionModel(color, vertices);
            model.addColorRegion(crm);
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
                                width * 32, (height - i) * 32,
                                width * 32, (height - (i + 1)) * 32,
                                (width - count - 1) * 32, (height - (i + 1)) * 32,
                                (width - count - 1) * 32, (height - i) * 32,
                        };
                    } else {
                        v = new float[] {
                                j * 32, (height - i) * 32,
                                j * 32, (height - (i + 1)) * 32,
                                (j - count) * 32, (height - (i + 1)) * 32,
                                (j - count) * 32, (height - i) * 32,
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
