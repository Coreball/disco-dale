package edu.cornell.gdiac.discodale.models;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import edu.cornell.gdiac.discodale.GameCanvas;

public class TutorialElement {
    private TextureRegion texture;
    private float x;
    private float y;

    public TutorialElement(TextureRegion texture, float x, float y) {
        this.texture = texture;
        this.x = x;
        this.y = y;
    }

    public void draw(GameCanvas canvas) {
        canvas.draw(this.texture, this.x, this.y);
    }
}
