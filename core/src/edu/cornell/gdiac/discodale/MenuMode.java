package edu.cornell.gdiac.discodale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.ScreenListener;

public class MenuMode implements Screen, InputProcessor {
    /** The type of the menu to display */
    public enum Type {
        START,
        LEVEL_SELECT
    }
    private Type type;

    /** UX element scales and offsets */
    private static final float START_BUTTON_SCALE  = 0.25f;
    private static final int PLAY_OFFSET_Y = 300;
    private static final int TITLE_OFFSET_Y = 450;

    private static final int LEVEL_BUTTONS_OFFSET_X = 200;
    private static final int LEVEL_BUTTONS_OFFSET_Y = 300;
    private static final int LEVEL_BUTTONS_MARGIN = 160;
    private static final int LEVEL_FONT_MARGIN_X = 22;
    private static final int LEVEL_FONT_MARGIN_Y = 15;

    private static final int LEVEL_BUTTON_ROWS = 2;
    private static final int LEVEL_BUTTON_COLS = 5;

    /** Whether this is an active controller */
    protected boolean active;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** The texture for the playButton */
    protected Texture playButton;
    /** The texture for background */
    protected Texture background;
    /** The texture for title */
    protected Texture title;
    /** The texture for level select */
    protected Texture levelSelect;
    /** The texture for level buttons */
    protected Texture levelButton;
    /** The level number pressed; -1 if none */
    private int levelPressed = -1;
    /** The font for level numbers */
    protected BitmapFont displayFont;

    private boolean playPressed;


    /** Reference to the game canvas */
    protected GameCanvas canvas;

    public void setType(Type type){
        this.type = type;
    }

    public Type getType(){
        return type;
    }

    public MenuMode(GameCanvas canvas){
        this.canvas = canvas;
        active = false;
        type = Type.START;
        canvas.updateCam(canvas.getWidth()/2, canvas.getHeight()/2, 1.0f);
    }

    public int getLevel(){
        return levelPressed;
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
            Gdx.input.setInputProcessor( this );
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
        return inBounds(x, y, canvas.getWidth()/2f-playButton.getWidth()/2f*START_BUTTON_SCALE,
                canvas.getWidth()/2f+playButton.getWidth()/2f*START_BUTTON_SCALE,
                PLAY_OFFSET_Y+playButton.getHeight()/2f*START_BUTTON_SCALE,
                PLAY_OFFSET_Y-playButton.getHeight()/2f*START_BUTTON_SCALE);
    }

    public boolean inBounds(int x, int y, float left, float right, float up, float down){
        return left <= x && x <= right && down <= y && y <= up;
    }

    public void update(float dt) {
    }

    public void draw(){
        canvas.begin();
        canvas.draw(background, Color.WHITE,0, 0, canvas.getWidth(), canvas.getHeight());
        if (type == Type.START)
            drawStart();
        else if (type == Type.LEVEL_SELECT)
            drawLevelSelect();
        canvas.end();
    }

    public void drawStart(){
        Color playTint = playPressed?Color.GRAY:Color.WHITE;
        canvas.draw(playButton, playTint, playButton.getWidth()/2f, playButton.getHeight()/2f,
                canvas.getWidth()/2f, PLAY_OFFSET_Y, 0, START_BUTTON_SCALE, START_BUTTON_SCALE);
        canvas.draw(title, Color.WHITE, title.getWidth()/2f, title.getHeight()/2f,
                canvas.getWidth()/2f, TITLE_OFFSET_Y, 0, 1f, 1f);
    }

    public void drawLevelSelect(){
        canvas.draw(levelSelect, Color.WHITE, levelSelect.getWidth()/2f, levelSelect.getHeight()/2f,
                canvas.getWidth()/2f, TITLE_OFFSET_Y, 0, 1f, 1f);
        int x, y, num;
        Color tint;
        for (int i = 0; i < LEVEL_BUTTON_ROWS; i++){
            for (int j = 0; j < LEVEL_BUTTON_COLS; j++){
                num = i * LEVEL_BUTTON_COLS + j;
                tint = levelPressed == num ? Color.GRAY : Color.WHITE;
                x = LEVEL_BUTTONS_OFFSET_X + j * LEVEL_BUTTONS_MARGIN;
                y = LEVEL_BUTTONS_OFFSET_Y - i * LEVEL_BUTTONS_MARGIN;
                canvas.draw(levelButton, tint, levelButton.getWidth()/2f, levelButton.getHeight()/2f,
                        x, y, 0, 1f, 1f);
                displayFont.setColor(Color.BLACK);
                canvas.drawText(Integer.toString(num + 1), displayFont,
                        x - LEVEL_FONT_MARGIN_X, y + LEVEL_FONT_MARGIN_Y);
            }
        }
    }

    public void gatherAssets(AssetDirectory directory) {
        playButton = directory.getEntry("menu:play", Texture.class);
        background = directory.getEntry("menu:bg", Texture.class);
        title = directory.getEntry("menu:title", Texture.class);
        levelSelect = directory.getEntry("menu:level", Texture.class);
        levelButton = directory.getEntry("menu:levelbutton", Texture.class);
        displayFont = directory.getEntry("shared:alien", BitmapFont.class);
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
        screenY = canvas.getHeight() - screenY;
        if (type == Type.START)
            return touchDownStart(screenX, screenY);
        else
            return touchDownLevel(screenX, screenY);

    }

    private boolean touchDownStart(int screenX, int screenY){
        if (inPlayBounds(screenX, screenY)) {
            playPressed = true;
            return false;
        }
        return true;
    }

    private boolean touchDownLevel(int screenX, int screenY) {
        float x, y, left, right, up, down;
        for (int i = 0; i < LEVEL_BUTTON_ROWS; i++){
            for (int j = 0; j < LEVEL_BUTTON_COLS; j++) {
                x = LEVEL_BUTTONS_OFFSET_X + j * LEVEL_BUTTONS_MARGIN;
                y = LEVEL_BUTTONS_OFFSET_Y - i * LEVEL_BUTTONS_MARGIN;
                left = x - levelButton.getWidth() / 2f;
                right = x + levelButton.getWidth() / 2f;
                up = y + levelButton.getHeight() / 2f;
                down = y - levelButton.getHeight() / 2f;
                if (inBounds(screenX, screenY, left, right, up, down)){
                    levelPressed = i * LEVEL_BUTTON_COLS + j;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        screenY = canvas.getHeight() - screenY;
        if (type == Type.START)
            return touchUpStart(screenX, screenY);
        else
            return touchUpLevel(screenX, screenY);
    }

    private boolean touchUpStart(int screenX, int screenY){
        if (inPlayBounds(screenX, screenY) && playPressed) {
            playPressed = false;
            this.type = Type.LEVEL_SELECT;
            return false;
        }
        playPressed = false;
        return true;
    }

    private boolean touchUpLevel(int screenX, int screenY){
        if (levelPressed != -1){
            int i = levelPressed / LEVEL_BUTTON_COLS;
            int j = levelPressed % LEVEL_BUTTON_COLS;
            int left = LEVEL_BUTTONS_OFFSET_X + j * LEVEL_BUTTONS_MARGIN - levelButton.getWidth() / 2;
            int right = LEVEL_BUTTONS_OFFSET_X + j * LEVEL_BUTTONS_MARGIN + levelButton.getWidth() / 2;
            int up = LEVEL_BUTTONS_OFFSET_Y - i * LEVEL_BUTTONS_MARGIN + levelButton.getHeight() / 2;
            int down = LEVEL_BUTTONS_OFFSET_Y - i * LEVEL_BUTTONS_MARGIN - levelButton.getHeight() / 2;
            if (inBounds(screenX, screenY, left, right, up, down)){
                listener.exitScreen(this, Constants.EXIT_LEVEL);
                levelPressed = -1;
                return false;
            }
            levelPressed = -1;
        }
        return false;
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
