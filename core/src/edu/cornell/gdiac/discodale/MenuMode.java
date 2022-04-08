package edu.cornell.gdiac.discodale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.ScreenListener;

public class MenuMode implements Screen, InputProcessor {

    /** UX element scales and offsets */
    private static float BUTTON_SCALE  = 0.25f;
    private static int PLAY_OFFSET_Y = 300;
    private static int TITLE_OFFSET_Y = 450;

    /** Whether this is an active controller */
    protected boolean active;

    /** Listener that will update the player mode */
    private ScreenListener listener;
    /** The texture for the playButton */
    protected Texture playButton;
    /** The texture for background */
    protected Texture background;
    /** The texture for background */
    protected Texture title;

    private boolean playPressed;


    /** Reference to the game canvas */
    protected GameCanvas canvas;

    public MenuMode(GameCanvas canvas){
        this.canvas = canvas;
        active = false;
    }

    /**
     * Called when the Screen should render itself.
     *
     * We defer to the other methods update() and draw().  However, it is VERY important
     * that we only quit AFTER a draw.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();
        }
    }

    public void setCanvas(GameCanvas canvas) {
        this.canvas = canvas;
//        this.scale = canvas.getWidth() / bounds.getWidth();
//        this.scale = canvas.getHeight() / bounds.getHeight();
    }

    public boolean inPlayBounds(int x, int y){
        return canvas.getWidth()/2f-playButton.getWidth()/2f*BUTTON_SCALE < x &&
                canvas.getWidth()/2f+playButton.getWidth()/2f*BUTTON_SCALE > x &&
                PLAY_OFFSET_Y-playButton.getHeight()/2f*BUTTON_SCALE < y &&
                PLAY_OFFSET_Y+playButton.getHeight()/2f*BUTTON_SCALE > y;
    }

    public void update(float dt) {
        if(active){
            Gdx.input.setInputProcessor( this );
        }
    }

    public void draw(){
        canvas.begin();
        canvas.draw(background, Color.WHITE,0, 0, canvas.getWidth(), canvas.getHeight());
        Color playTint = playPressed?Color.GRAY:Color.WHITE;
        canvas.draw(playButton, playTint, playButton.getWidth()/2, playButton.getHeight()/2,
                canvas.getWidth()/2, PLAY_OFFSET_Y, 0, BUTTON_SCALE, BUTTON_SCALE);
        canvas.draw(title, Color.WHITE, title.getWidth()/2, title.getHeight()/2,
                canvas.getWidth()/2, TITLE_OFFSET_Y, 0, 1f, 1f);
        canvas.end();
    }

    public void gatherAssets(AssetDirectory directory) {
        playButton = directory.getEntry("menu:play", Texture.class);
        background = directory.getEntry("menu:bg", Texture.class);
        title = directory.getEntry("menu:title", Texture.class);
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    @Override
    public void show() {
        active = true;
    }


    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        active = false;
        Gdx.input.setInputProcessor( null );
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Flip to match graphics coordinates
        screenY = canvas.getHeight()-screenY;

        if (inPlayBounds(screenX, screenY)) {
            playPressed = true;
            return false;
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        screenY = canvas.getHeight()-screenY;
        if (inPlayBounds(screenX, screenY) && playPressed) {
            listener.exitScreen(this, Constants.EXIT_LEVEL);
            return false;
        }
        playPressed = false;
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
