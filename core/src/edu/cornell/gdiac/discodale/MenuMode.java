package edu.cornell.gdiac.discodale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.discodale.models.ColorRegionModel;
import edu.cornell.gdiac.util.ScreenListener;

public class MenuMode implements Screen, InputProcessor {
    /** The type of the menu to display */
    public enum Type {
        START,
        LEVEL_SELECT,
        OPTIONS,
        LEVEL_COMPLETE,
        PAUSE,
    }
    private Type type;
    private Type typePrevious;

    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 1024;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 576;
    /** Scaling factors for changing the resolution. */
    private float sx = 1f;
    private float sy = 1f;

    /** UX element scales and offsets */
    private static final float START_BUTTON_SCALE  = 0.25f;
    private static final int PLAY_OFFSET_Y = 300;
    private static final int OPTIONS_OFFSET_Y = 200;
    private static final int EXIT_OFFSET_Y = 100;
    private static final int TITLE_OFFSET_Y = 450;

    private static final int WINDOW_BG_OFFSET_Y = 10;
    private static final int WINDOW_TITLE_OFFSET_X = 230;
    private static final int WINDOW_TITLE_OFFSET_Y = 515;

    private static final int OPTIONS_RETURN_OFFSET_X = 670;
    private static final int OPTIONS_RETURN_OFFSET_Y = 70;
    private static final int OPTIONS_LABEL_OFFSET_X = 225;
    private static final int OPTIONS_LABEL_2_OFFSET_X = 270;
    private static final int OPTIONS_VOLUME_LABEL_OFFSET_Y = 400;
    private static final int OPTIONS_VOLUME_NUM_OFFSET_X = 750;
    private static final int OPTIONS_BGM_LABEL_OFFSET_Y = 325;
    private static final int OPTIONS_SFX_LABEL_OFFSET_Y = 275;
    private static final int OPTIONS_SLIDE_OFFSET_X = 340;
    private static final int OPTIONS_SLIDE_BGM_OFFSET_Y = 310;
    private static final int OPTIONS_SLIDE_SFX_OFFSET_Y = 260;
    private static final int OPTIONS_ACCESS_OFFSET_X = 500;
    private static final int OPTIONS_ACCESS_OFFSET_Y = 165;
    private static final int OPTIONS_ACCESS_LABEL_OFFSET_Y = 200;

    private static final int LEVEL_BUTTONS_OFFSET_X = 200;
    private static final int LEVEL_BUTTONS_OFFSET_Y = 300;
    private static final int LEVEL_BUTTONS_MARGIN = 160;
    private static final int LEVEL_FONT_MARGIN_X = 22;
    private static final int LEVEL_FONT_MARGIN_Y = 15;
    private static final int LEVEL_BUTTON_ROWS = 2;
    private static final int LEVEL_BUTTON_COLS = 5;

    private static final int COMPLETE_MENU_OFFSET_X = 160;
    private static final int COMPLETE_RESTART_OFFSET_X = 450;
    private static final int COMPLETE_NEXT_OFFSET_X = 590;
    private static final int COMPLETE_BUTTONS_OFFSET_Y = 50;

    private static final int PAUSE_RESUME_OFFSET_Y = 350;
    private static final int PAUSE_RESTART_OFFSET_Y = 265;
    private static final int PAUSE_OPTIONS_OFFSET_Y = 180;
    private static final int PAUSE_MENU_OFFSET_Y = 95;

    /** Whether this is an active controller */
    protected boolean active;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** The texture for the playButton */
    protected Texture playButton;
    /** The texture for the optionsButton */
    protected Texture optionsButton;
    /** The texture for the exitButton */
    protected Texture exitButton;
    /** The texture for background */
    protected Texture background;
    /** The texture for the options, complete and pause background */
    protected Texture windowBg;
    protected Texture complete;
    /** The texture for title */
    protected Texture title;
    /** The textures for toggle button */
    protected Texture toggleOn;
    protected Texture toggleOff;
    /** The textures for the slide bars */
    protected Texture slideOn;
    protected Texture slideOnLeft;
    protected Texture slideOff;
    protected Texture slideOffRight;
    protected Texture slideThumb;
    /** The textures for the level complete buttons */
    protected Texture exitToMenu;
    protected Texture restart;
    protected Texture nextLevel;
    /** The textures for the pause screen */
    protected Texture resume;
    protected Texture restartPause;
    protected Texture optionsPause;
    protected Texture menuPause;
    /** The texture for level select */
    protected Texture levelSelect;
    /** The texture for level buttons */
    protected Texture levelButton;
    /** The level number pressed; -1 if none */
    private int levelPressed = -1;
    /** The fonts */
    protected BitmapFont displayFont;
    protected BitmapFont titleFont;
    protected BitmapFont buttonFont;
    protected BitmapFont labelFont;
    protected BitmapFont labelFont2;
    private Color labelColor = new Color(0x090537ff);
    /** Background music */
    private Sound theme;
    private long themeId = -1;

    private boolean playPressed;
    private boolean optionsPressed;
    private boolean exitPressed;
    private boolean optionsReturnPressed;
    private boolean accessibilitySelected;
    private boolean bgmPressed, sfxPressed;
    private boolean toMenuPressed, nextLevelPressed, restartPressed;
    private boolean resumePressed;
    private int volumeBgm = 100, volumeSfx = 100;

//    private TextureRegionDrawable test;
//    private Button testButton;

    /** Reference to the game canvas */
    protected GameCanvas canvas;

    public void setType(Type type){
        this.type = type;
    }

    public Type getType(){
        return type;
    }

    public void setAccessibility(boolean useTexture){ accessibilitySelected = useTexture; }

    public int getLevel(){
        return levelPressed;
    }

    public int getVolumeBgm() { return volumeBgm; }

    public int getVolumeSfx() { return volumeSfx; }

    public MenuMode(GameCanvas canvas){
        this.canvas = canvas;
        active = false;
        type = Type.START;
        canvas.updateCam(canvas.getWidth()/2, canvas.getHeight()/2, 1.0f,
                new Rectangle(0, 0, 32, 18), 32);
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

    private boolean inPlayBounds(int x, int y){
        return inBounds(x, y, canvas.getWidth()/2f-playButton.getWidth()/2f*START_BUTTON_SCALE*sx,
                canvas.getWidth()/2f+playButton.getWidth()/2f*START_BUTTON_SCALE*sx,
                (PLAY_OFFSET_Y+playButton.getHeight()/2f*START_BUTTON_SCALE)*sy,
                (PLAY_OFFSET_Y-playButton.getHeight()/2f*START_BUTTON_SCALE)*sy);
    }

    private boolean inOptionsBounds(int x, int y){
        return inBounds(x, y, canvas.getWidth()/2f-optionsButton.getWidth()/2f*START_BUTTON_SCALE*sx,
                canvas.getWidth()/2f+optionsButton.getWidth()/2f*START_BUTTON_SCALE*sx,
                (OPTIONS_OFFSET_Y+optionsButton.getHeight()/2f*START_BUTTON_SCALE)*sy,
                (OPTIONS_OFFSET_Y-optionsButton.getHeight()/2f*START_BUTTON_SCALE)*sy);
    }

    private boolean inExitBounds(int x, int y){
        return inBounds(x, y, canvas.getWidth()/2f-exitButton.getWidth()/2f*START_BUTTON_SCALE*sx,
                canvas.getWidth()/2f+exitButton.getWidth()/2f*START_BUTTON_SCALE*sx,
                (EXIT_OFFSET_Y+exitButton.getHeight()/2f*START_BUTTON_SCALE)*sy,
                (EXIT_OFFSET_Y-exitButton.getHeight()/2f*START_BUTTON_SCALE)*sy);
    }

    private boolean inOptionsReturnBounds(int x, int y){
        return inBounds(x, y, OPTIONS_RETURN_OFFSET_X * sx, (OPTIONS_RETURN_OFFSET_X + 120f) * sx,
                OPTIONS_RETURN_OFFSET_Y * sy, (OPTIONS_RETURN_OFFSET_Y - 20f) * sy);
    }

    private boolean inToggleBounds(int x, int y){
        return inBounds(x, y, OPTIONS_ACCESS_OFFSET_X*sx, (OPTIONS_ACCESS_OFFSET_X + toggleOff.getWidth())*sx,
                (OPTIONS_ACCESS_OFFSET_Y + toggleOff.getHeight()) * sy, OPTIONS_ACCESS_OFFSET_Y * sy);
    }

    private boolean inBgmBounds(int x, int y){
        return inBounds(x, y, (OPTIONS_SLIDE_OFFSET_X - slideThumb.getWidth()) * sx,
                (OPTIONS_SLIDE_OFFSET_X + slideOn.getWidth() + slideThumb.getWidth()) * sx,
                (OPTIONS_SLIDE_BGM_OFFSET_Y + slideThumb.getHeight() / 2f) * sy,
                (OPTIONS_SLIDE_BGM_OFFSET_Y - slideThumb.getHeight() / 2f) * sy);
    }

    private boolean inSfxBounds(int x, int y){
        return inBounds(x, y, (OPTIONS_SLIDE_OFFSET_X - slideThumb.getWidth()) * sx,
                (OPTIONS_SLIDE_OFFSET_X + slideOn.getWidth() + slideThumb.getWidth()) * sx,
                (OPTIONS_SLIDE_SFX_OFFSET_Y + slideThumb.getHeight() / 2f) * sy,
                (OPTIONS_SLIDE_SFX_OFFSET_Y - slideThumb.getHeight() / 2f) * sy);
    }

    private boolean inToMenuBounds(int x, int y){
        return inBounds(x, y, COMPLETE_MENU_OFFSET_X * sx,
                (COMPLETE_MENU_OFFSET_X + exitToMenu.getWidth()) * sx,
                (COMPLETE_BUTTONS_OFFSET_Y + exitToMenu.getHeight()) * sy, COMPLETE_BUTTONS_OFFSET_Y * sy);
    }

    private boolean inRestartBounds(int x, int y){
        return inBounds(x, y, COMPLETE_RESTART_OFFSET_X * sx,
                (COMPLETE_RESTART_OFFSET_X + restart.getWidth()) * sx,
                (COMPLETE_BUTTONS_OFFSET_Y +  restart.getHeight()) * sy, COMPLETE_BUTTONS_OFFSET_Y * sy);
    }

    private boolean inNextLevelBounds(int x, int y){
        return inBounds(x, y, COMPLETE_NEXT_OFFSET_X * sx,
                (COMPLETE_NEXT_OFFSET_X + nextLevel.getWidth()) * sx,
                (COMPLETE_BUTTONS_OFFSET_Y + nextLevel.getHeight()) * sy , COMPLETE_BUTTONS_OFFSET_Y * sy);
    }

    private boolean inResumeBounds(int x, int y){
        return inBounds(x, y, canvas.getWidth() / 2f - resume.getWidth() / 2f * sx,
                canvas.getWidth() / 2f + resume.getWidth() / 2f * sx,
                (PAUSE_RESUME_OFFSET_Y + resume.getHeight()) * sy, PAUSE_RESUME_OFFSET_Y * sy);
    }

    private boolean inRestartPauseBounds(int x, int y){
        return inBounds(x, y, canvas.getWidth() / 2f - restartPause.getWidth() / 2f * sx,
                canvas.getWidth() / 2f + restartPause.getWidth() / 2f * sx,
                (PAUSE_RESTART_OFFSET_Y + restartPause.getHeight()) * sy, PAUSE_RESTART_OFFSET_Y * sy);
    }

    private boolean inOptionsPauseBounds(int x, int y){
        return inBounds(x, y, canvas.getWidth() / 2f - optionsPause.getWidth() / 2f * sx,
                canvas.getWidth() / 2f + optionsPause.getWidth() / 2f * sx,
                (PAUSE_OPTIONS_OFFSET_Y + optionsPause.getHeight()) * sy, PAUSE_OPTIONS_OFFSET_Y * sy);
    }

    private boolean inMenuPauseBounds(int x, int y){
        return inBounds(x, y, canvas.getWidth() / 2f - menuPause.getWidth() / 2f * sx,
                canvas.getWidth() / 2f + menuPause.getWidth() / 2f * sx,
                (PAUSE_MENU_OFFSET_Y + menuPause.getHeight()) * sy, PAUSE_MENU_OFFSET_Y * sy);
    }

    public boolean inBounds(int x, int y, float left, float right, float up, float down){
        return left <= x && x <= right && down <= y && y <= up;
    }

    public void update(float dt) {
        themeId = SoundPlayer.loopSound(theme, themeId, volumeBgm / 100f);
    }

    public void draw(){
        canvas.begin();
        canvas.draw(background, Color.WHITE,0, 0, STANDARD_WIDTH, STANDARD_HEIGHT);
        if (type == Type.START)
            drawStart();
        else if (type == Type.LEVEL_SELECT)
            drawLevelSelect();
        else if (type == Type.OPTIONS)
            drawOptions();
        else if (type == Type.LEVEL_COMPLETE)
            drawComplete();
        else if (type == Type.PAUSE)
            drawPause();
        canvas.end();
    }

    public void drawStart(){
        canvas.draw(title, Color.WHITE, title.getWidth()/2f, title.getHeight()/2f,
                STANDARD_WIDTH/2f, TITLE_OFFSET_Y, 0, 1f, 1f);

        canvas.draw(playButton, playPressed?Color.GRAY:Color.WHITE, playButton.getWidth()/2f,
                playButton.getHeight()/2f,STANDARD_WIDTH/2f, PLAY_OFFSET_Y, 0,
                START_BUTTON_SCALE, START_BUTTON_SCALE);
        canvas.draw(optionsButton, optionsPressed?Color.GRAY:Color.WHITE, optionsButton.getWidth()/2f,
                optionsButton.getHeight()/2f, STANDARD_WIDTH/2f, OPTIONS_OFFSET_Y, 0,
                START_BUTTON_SCALE, START_BUTTON_SCALE);
        canvas.draw(exitButton, exitPressed?Color.GRAY:Color.WHITE, exitButton.getWidth()/2f,
                exitButton.getHeight()/2f, STANDARD_WIDTH/2f, EXIT_OFFSET_Y, 0,
                START_BUTTON_SCALE, START_BUTTON_SCALE);
    }

    public void drawLevelSelect(){
        canvas.draw(levelSelect, Color.WHITE, levelSelect.getWidth()/2f, levelSelect.getHeight()/2f,
                STANDARD_WIDTH/2f, TITLE_OFFSET_Y, 0, 1f, 1f);
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
                canvas.drawText(Integer.toString(num + 1), displayFont,
                        x - LEVEL_FONT_MARGIN_X, y + LEVEL_FONT_MARGIN_Y);
            }
        }
    }

    public void drawOptions(){
        canvas.draw(windowBg, STANDARD_WIDTH/2f - windowBg.getWidth()/2f, WINDOW_BG_OFFSET_Y);
        canvas.drawText("options", titleFont, WINDOW_TITLE_OFFSET_X, WINDOW_TITLE_OFFSET_Y);
        canvas.drawText("return", buttonFont, OPTIONS_RETURN_OFFSET_X, OPTIONS_RETURN_OFFSET_Y);

        canvas.drawText("Volume", labelFont, OPTIONS_LABEL_OFFSET_X, OPTIONS_VOLUME_LABEL_OFFSET_Y);
        canvas.drawText("BGM", labelFont2, OPTIONS_LABEL_2_OFFSET_X, OPTIONS_BGM_LABEL_OFFSET_Y);
        canvas.drawText("SFX", labelFont2, OPTIONS_LABEL_2_OFFSET_X, OPTIONS_SFX_LABEL_OFFSET_Y);

        drawSliders();

        canvas.drawText("Accessibility Mode", labelFont, OPTIONS_LABEL_OFFSET_X, OPTIONS_ACCESS_LABEL_OFFSET_Y);
        canvas.draw(accessibilitySelected?toggleOn:toggleOff, OPTIONS_ACCESS_OFFSET_X,OPTIONS_ACCESS_OFFSET_Y);
    }

    private void drawSliders(){
        canvas.draw(slideOn, Color.WHITE, OPTIONS_SLIDE_OFFSET_X, OPTIONS_SLIDE_BGM_OFFSET_Y,
                volumeBgm * slideOn.getWidth() / 100f, slideOn.getHeight());
        canvas.draw(slideOff, Color.WHITE, OPTIONS_SLIDE_OFFSET_X + volumeBgm * slideOn.getWidth() / 100f,
                 OPTIONS_SLIDE_BGM_OFFSET_Y, (100 - volumeBgm) * slideOff.getWidth() / 100f, slideOff.getHeight());
        canvas.draw(slideOnLeft, OPTIONS_SLIDE_OFFSET_X - slideOnLeft.getWidth(), OPTIONS_SLIDE_BGM_OFFSET_Y);
        canvas.draw(slideOffRight, OPTIONS_SLIDE_OFFSET_X + slideOff.getWidth(), OPTIONS_SLIDE_BGM_OFFSET_Y);

        canvas.draw(slideOn, Color.WHITE, OPTIONS_SLIDE_OFFSET_X, OPTIONS_SLIDE_SFX_OFFSET_Y,
                volumeSfx * slideOn.getWidth() / 100f, slideOn.getHeight());
        canvas.draw(slideOff, Color.WHITE, OPTIONS_SLIDE_OFFSET_X + volumeSfx * slideOn.getWidth() / 100f,
                OPTIONS_SLIDE_SFX_OFFSET_Y, (100 - volumeSfx) * slideOff.getWidth() / 100f, slideOff.getHeight());
        canvas.draw(slideOnLeft, OPTIONS_SLIDE_OFFSET_X - slideOnLeft.getWidth(), OPTIONS_SLIDE_SFX_OFFSET_Y);
        canvas.draw(slideOffRight, OPTIONS_SLIDE_OFFSET_X + slideOff.getWidth(), OPTIONS_SLIDE_SFX_OFFSET_Y);

        canvas.draw(slideThumb,
                OPTIONS_SLIDE_OFFSET_X + volumeBgm * slideOn.getWidth() / 100f - slideThumb.getWidth() / 2f,
                OPTIONS_SLIDE_BGM_OFFSET_Y + slideOn.getHeight() / 2f - slideThumb.getHeight() / 2f);
        canvas.draw(slideThumb,
                OPTIONS_SLIDE_OFFSET_X + volumeSfx * slideOn.getWidth() / 100f - slideThumb.getWidth() / 2f,
                OPTIONS_SLIDE_SFX_OFFSET_Y + slideOn.getHeight() / 2f - slideThumb.getHeight() / 2f);

        canvas.drawText(Integer.toString(volumeBgm), labelFont2, OPTIONS_VOLUME_NUM_OFFSET_X, OPTIONS_BGM_LABEL_OFFSET_Y);
        canvas.drawText(Integer.toString(volumeSfx), labelFont2, OPTIONS_VOLUME_NUM_OFFSET_X, OPTIONS_SFX_LABEL_OFFSET_Y);
    }

    public void drawComplete() {
        canvas.draw(windowBg, STANDARD_WIDTH/2f - windowBg.getWidth()/2f, WINDOW_BG_OFFSET_Y);
        canvas.drawText("Level completed!", titleFont, WINDOW_TITLE_OFFSET_X, WINDOW_TITLE_OFFSET_Y);
        canvas.draw(complete, STANDARD_WIDTH/2f - complete.getWidth()/2f,
                STANDARD_HEIGHT/2f - complete.getHeight()/2f);
        canvas.draw(exitToMenu, toMenuPressed?Color.GRAY:Color.WHITE, COMPLETE_MENU_OFFSET_X,
                COMPLETE_BUTTONS_OFFSET_Y, exitToMenu.getWidth(), exitToMenu.getHeight());
        canvas.draw(restart, restartPressed?Color.GRAY:Color.WHITE, COMPLETE_RESTART_OFFSET_X,
                COMPLETE_BUTTONS_OFFSET_Y, restart.getWidth(), restart.getHeight());
        canvas.draw(nextLevel, nextLevelPressed?Color.GRAY:Color.WHITE, COMPLETE_NEXT_OFFSET_X,
                COMPLETE_BUTTONS_OFFSET_Y, nextLevel.getWidth(), nextLevel.getHeight());
    }

    public void drawPause() {
        canvas.draw(windowBg, STANDARD_WIDTH/2f - windowBg.getWidth()/2f, WINDOW_BG_OFFSET_Y);
        canvas.drawText("paused", titleFont, WINDOW_TITLE_OFFSET_X, WINDOW_TITLE_OFFSET_Y);
        canvas.draw(resume, resumePressed?Color.GRAY:Color.WHITE, STANDARD_WIDTH/2f - resume.getWidth()/2f,
                PAUSE_RESUME_OFFSET_Y, resume.getWidth(), resume.getHeight());
        canvas.draw(restartPause, restartPressed?Color.GRAY:Color.WHITE,
                STANDARD_WIDTH/2f - restartPause.getWidth()/2f, PAUSE_RESTART_OFFSET_Y,
                restartPause.getWidth(), restartPause.getHeight());
        canvas.draw(optionsPause, optionsPressed?Color.GRAY:Color.WHITE,
                STANDARD_WIDTH/2f - optionsPause.getWidth()/2f, PAUSE_OPTIONS_OFFSET_Y,
                optionsPause.getWidth(), optionsPause.getHeight());
        canvas.draw(menuPause, toMenuPressed?Color.GRAY:Color.WHITE,
                STANDARD_WIDTH/2f - menuPause.getWidth()/2f, PAUSE_MENU_OFFSET_Y,
                menuPause.getWidth(), menuPause.getHeight());
    }

    public void gatherAssets(AssetDirectory directory) {
        playButton = directory.getEntry("menu:play", Texture.class);
        optionsButton = directory.getEntry("menu:options", Texture.class);
        exitButton = directory.getEntry("menu:exit", Texture.class);
        background = directory.getEntry("menu:bg", Texture.class);
        windowBg = directory.getEntry("menu:windowbg", Texture.class);
        complete = directory.getEntry("menu:complete", Texture.class);
        title = directory.getEntry("menu:title", Texture.class);
        levelSelect = directory.getEntry("menu:level", Texture.class);
        levelButton = directory.getEntry("menu:levelbutton", Texture.class);
        toggleOn = directory.getEntry("menu:toggleon", Texture.class);
        toggleOff = directory.getEntry("menu:toggleoff", Texture.class);
        slideOn = directory.getEntry("menu:slideon", Texture.class);
        slideOnLeft = directory.getEntry("menu:slideonleft", Texture.class);
        slideOff = directory.getEntry("menu:slideoff", Texture.class);
        slideOffRight = directory.getEntry("menu:slideoffright", Texture.class);
        slideThumb = directory.getEntry("menu:slidethumb", Texture.class);
        exitToMenu = directory.getEntry("menu:exittomenu", Texture.class);
        restart = directory.getEntry("menu:restart", Texture.class);
        nextLevel = directory.getEntry("menu:nextlevel", Texture.class);
        resume = directory.getEntry("menu:resume", Texture.class);
        restartPause = directory.getEntry("menu:restartpause", Texture.class);
        optionsPause = directory.getEntry("menu:optionspause", Texture.class);
        menuPause = directory.getEntry("menu:menu", Texture.class);
        displayFont = directory.getEntry("shared:alienitalic", BitmapFont.class);
        titleFont = directory.getEntry("shared:alien", BitmapFont.class);
        buttonFont = directory.getEntry("shared:aliensmall", BitmapFont.class);
        labelFont = directory.getEntry("shared:gothic", BitmapFont.class);
        labelFont2 = directory.getEntry("shared:gothicsmall", BitmapFont.class);
        theme = directory.getEntry("theme", Sound.class);

        displayFont.setColor(Color.BLACK);
        titleFont.setColor(labelColor);
        labelFont.setColor(labelColor);
        labelFont2.setColor(labelColor);
        buttonFont.setColor(labelColor);
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
        Gdx.input.setInputProcessor( this );
    }


    @Override
    public void resize(int width, int height) {
        canvas.width = width;
        canvas.height = height;
        // Compute the drawing scale
        sx = ((float)width)/STANDARD_WIDTH;
        sy = ((float)height)/STANDARD_HEIGHT;
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
        else if (type == Type.LEVEL_SELECT)
            return touchDownLevel(screenX, screenY);
        else if (type == Type.OPTIONS)
            return touchDownOptions(screenX, screenY);
        else if (type == Type.LEVEL_COMPLETE)
            return touchDownComplete(screenX, screenY);
        else if (type == Type.PAUSE)
            return touchDownPause(screenX, screenY);
        else
            return false;
    }

    private boolean touchDownStart(int screenX, int screenY){
        if (inPlayBounds(screenX, screenY)) {
            playPressed = true;
            return false;
        } else if (inOptionsBounds(screenX, screenY)){
            optionsPressed = true;
            return false;
        } else if (inExitBounds(screenX, screenY)){
            exitPressed = true;
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
                if (inBounds((int) (screenX / sx), (int) (screenY / sy), left, right, up, down)){
                    levelPressed = i * LEVEL_BUTTON_COLS + j;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean touchDownOptions(int screenX, int screenY){
        if (inOptionsReturnBounds(screenX, screenY)) {
            optionsReturnPressed = true;
            return false;
        } else if (inToggleBounds(screenX, screenY)){
            accessibilitySelected = !accessibilitySelected;
            return false;
        } else if (inBgmBounds(screenX, screenY)) {
            bgmPressed = true;
            return false;
        } else if (inSfxBounds(screenX, screenY)) {
            sfxPressed = true;
            return false;
        }
        return true;
    }

    private boolean touchDownComplete(int screenX, int screenY){
        if (inToMenuBounds(screenX, screenY)) {
            toMenuPressed = true;
            return false;
        } else if (inRestartBounds(screenX, screenY)){
            restartPressed = true;
            return false;
        } else if (inNextLevelBounds(screenX, screenY)) {
            nextLevelPressed = true;
            return false;
        }
        return true;
    }

    private boolean touchDownPause(int screenX, int screenY){
        if (inResumeBounds(screenX, screenY)) {
            resumePressed = true;
            return false;
        } else if (inRestartPauseBounds(screenX, screenY)) {
            restartPressed = true;
            return false;
        } else if (inOptionsPauseBounds(screenX, screenY)) {
            optionsPressed = true;
            return false;
        } else if (inMenuPauseBounds(screenX, screenY)) {
            toMenuPressed = true;
            return false;
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        screenY = canvas.getHeight() - screenY;
        if (type == Type.START)
            return touchUpStart(screenX, screenY);
        else if (type == Type.LEVEL_SELECT)
            return touchUpLevel(screenX, screenY);
        else if (type == Type.OPTIONS)
            return touchUpOptions(screenX, screenY);
        else if (type == Type.LEVEL_COMPLETE)
            return touchUpComplete(screenX, screenY);
        else if (type == Type.PAUSE)
            return touchUpPause(screenX, screenY);
        else
            return false;
    }

    private boolean touchUpStart(int screenX, int screenY){
        boolean result = true;
        if (inPlayBounds(screenX, screenY) && playPressed) {
            typePrevious = Type.START;
            this.type = Type.LEVEL_SELECT;
            result = false;
        } else if (inOptionsBounds(screenX, screenY) && optionsPressed) {
            typePrevious = Type.START;
            accessibilitySelected = ColorRegionModel.getDisplay();
            this.type = Type.OPTIONS;
            result = false;
        } else if (inExitBounds(screenX, screenY) && exitPressed) {
            typePrevious = Type.START;
            listener.exitScreen(this, Constants.EXIT_QUIT);
            result = false;
        }
        playPressed = false;
        optionsPressed = false;
        exitPressed = false;
        return result;
    }

    private boolean touchUpLevel(int screenX, int screenY){
        if (levelPressed != -1){
            int i = levelPressed / LEVEL_BUTTON_COLS;
            int j = levelPressed % LEVEL_BUTTON_COLS;
            int left = LEVEL_BUTTONS_OFFSET_X + j * LEVEL_BUTTONS_MARGIN - levelButton.getWidth() / 2;
            int right = LEVEL_BUTTONS_OFFSET_X + j * LEVEL_BUTTONS_MARGIN + levelButton.getWidth() / 2;
            int up = LEVEL_BUTTONS_OFFSET_Y - i * LEVEL_BUTTONS_MARGIN + levelButton.getHeight() / 2;
            int down = LEVEL_BUTTONS_OFFSET_Y - i * LEVEL_BUTTONS_MARGIN - levelButton.getHeight() / 2;
            if (inBounds((int) (screenX / sx), (int) (screenY / sy), left, right, up, down)){
                typePrevious = Type.LEVEL_SELECT;
                listener.exitScreen(this, Constants.EXIT_LEVEL);
            }
            levelPressed = -1;
        }
        return false;
    }

    private boolean touchUpOptions(int screenX, int screenY){
        boolean result = true;
        if (inOptionsReturnBounds(screenX, screenY) && optionsReturnPressed){
            ColorRegionModel.setDisplay(accessibilitySelected);
            this.type = typePrevious;
            typePrevious = Type.OPTIONS;
            result = false;
        }
        sfxPressed = false;
        bgmPressed = false;
        optionsReturnPressed = false;
        return result;
    }

    private boolean touchUpComplete(int screenX, int screenY){
        boolean result = true;
        if (inToMenuBounds(screenX, screenY) && toMenuPressed) {
            typePrevious = Type.LEVEL_COMPLETE;
            this.type = Type.START;
            result = false;
        } else if (inRestartBounds(screenX, screenY) && restartPressed) {
            typePrevious = Type.LEVEL_COMPLETE;
            listener.exitScreen(this, Constants.EXIT_LEVEL);
            result = false;
        } else if (inNextLevelBounds(screenX, screenY) && nextLevelPressed) {
            typePrevious = Type.LEVEL_COMPLETE;
            listener.exitScreen(this, Constants.EXIT_NEXT);
            result = false;
        }
        toMenuPressed = false;
        restartPressed = false;
        nextLevelPressed = false;
        return result;
    }

    private boolean touchUpPause(int screenX, int screenY) {
        boolean result = true;
        if (inResumeBounds(screenX, screenY) && resumePressed) {
            typePrevious = Type.PAUSE;
            listener.exitScreen(this, Constants.EXIT_RESUME);
            result = false;
        } else if (inRestartPauseBounds(screenX, screenY) && restartPressed) {
            typePrevious = Type.PAUSE;
            listener.exitScreen(this, Constants.EXIT_LEVEL);
            result = false;
        } else if (inOptionsPauseBounds(screenX, screenY) && optionsPressed) {
            typePrevious = Type.PAUSE;
            this.type = Type.OPTIONS;
            result = false;
        } else if (inMenuPauseBounds(screenX, screenY) && toMenuPressed) {
            typePrevious = Type.PAUSE;
            this.type = Type.START;
            result = false;
        }
        resumePressed = false;
        restartPressed = false;
        optionsPressed = false;
        toMenuPressed = false;
        return result;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (bgmPressed) {
            volumeBgm = Math.max(0, Math.min(100,
                    (int) ((screenX - OPTIONS_SLIDE_OFFSET_X * sx) * 100 / slideOn.getWidth() / sx)));
            return false;
        } else if (sfxPressed) {
            volumeSfx = Math.max(0, Math.min(100,
                    (int) ((screenX - OPTIONS_SLIDE_OFFSET_X * sx) * 100 / slideOn.getWidth() / sx)));
            return false;
        }
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
