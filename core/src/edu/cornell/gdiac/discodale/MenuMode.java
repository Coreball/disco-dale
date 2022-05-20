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
import edu.cornell.gdiac.util.FilmStrip;
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
    private static int STANDARD_WIDTH  = 1920;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 1080;
    /** Scaling factors for changing the resolution. */
    private float sx = 1f;
    private float sy = 1f;

    /** UX element scales and offsets */
    private static final int PLAY_OFFSET_Y = 534;
    private static final int OPTIONS_OFFSET_Y = 356;
    private static final int EXIT_OFFSET_Y = 178;
    private static final int TITLE_OFFSET_Y = 850;

    private static final int WINDOW_BG_OFFSET_Y = 30;
    private static final int WINDOW_TITLE_OFFSET_X = 420;
    private static final int WINDOW_TITLE_OFFSET_Y = 930;

    private static final int OPTIONS_RETURN_OFFSET_X = 1275;
    private static final int OPTIONS_RETURN_OFFSET_Y = 125;
    private static final int OPTIONS_CLEAR_SAVE_OFFSET_X = 800;
    private static final int OPTIONS_CLEAR_SAVE_OFFSET_Y = 125;
    private static final int OPTIONS_LABEL_OFFSET_X = 420;
    private static final int OPTIONS_LABEL_2_OFFSET_X = 480;
    private static final int OPTIONS_VOLUME_LABEL_OFFSET_Y = 712;
    private static final int OPTIONS_VOLUME_NUM_OFFSET_X = 1335;
    private static final int OPTIONS_BGM_LABEL_OFFSET_Y = 578;
    private static final int OPTIONS_SFX_LABEL_OFFSET_Y = 490;
    private static final int OPTIONS_SLIDE_OFFSET_X = 605;
    private static final int OPTIONS_SLIDE_BGM_OFFSET_Y = 552;
    private static final int OPTIONS_SLIDE_SFX_OFFSET_Y = 463;
    private static final int OPTIONS_ACCESS_OFFSET_X = 890;
    private static final int OPTIONS_ACCESS_OFFSET_Y = 294;
    private static final int OPTIONS_ACCESS_LABEL_OFFSET_Y = 356;

    private static final int LEVEL_BUTTONS_OFFSET_X = 356;
    private static final int LEVEL_BUTTONS_OFFSET_Y = 534;
    private static final int LEVEL_BUTTONS_MARGIN = 285;
    private static final int LEVEL_BUTTON_ROWS = 2;
    private static final int LEVEL_BUTTON_COLS = 5;
    private static final int LEVEL_PAGES = 3;
    private static final int LEVEL_PAGE_OFFSET_X_LEFT = 150;
    private static final int LEVEL_PAGE_OFFSET_X_RIGHT = 1700;
    private static final int LEVEL_PAGE_OFFSET_Y = 380;
    private static final int LEVEL_BEST_TIME_OFFSET_X = -15;
    private static final int LEVEL_BEST_TIME_OFFSET_Y = -50;

    private static final int COMPLETE_LEVEL_TIME_OFFSET_X = 420;
    private static final int COMPLETE_LEVEL_TIME_OFFSET_Y = 850;
    private static final int COMPLETE_MENU_OFFSET_X = 300;
    private static final int COMPLETE_RESTART_OFFSET_X = 825;
    private static final int COMPLETE_NEXT_OFFSET_X = 1100;
    private static final int COMPLETE_BUTTONS_OFFSET_Y = 90;

    private static final int PAUSE_LABEL_OFFSET_X = 1025;
    private static final int PAUSE_LABEL_OFFSET_Y = 200;
    private static final int PAUSE_ANIM_OFFSET_X = 303;
    private static final int PAUSE_ANIM_OFFSET_Y = 105;
    private static final int PAUSE_BUTTONS_OFFSET_X = 425;
    private static final int PAUSE_RESUME_OFFSET_Y = 700;
    private static final int PAUSE_RESTART_OFFSET_Y = 550;
    private static final int PAUSE_OPTIONS_OFFSET_Y = 400;
    private static final int PAUSE_MENU_OFFSET_Y = 250;


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

    private static final int WIN_FRAME = 24;
    protected FilmStrip win;
    protected float winFrame;

    private static final int PAUSE_ANIM_FRAME = 4;
    protected Texture[] pause = new Texture[PAUSE_ANIM_FRAME];
    protected int pause_anim_frame = 0;
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
    protected Texture[] levelButton = new Texture[LEVEL_PAGES];
    protected Texture[] levelPageSwitch = new Texture[LEVEL_PAGES];
    /** Current page the level select screen is in */
    private int levelPage = 0;
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
    private boolean clearSavePressed;
    private boolean accessibilitySelected;
    private boolean bgmPressed, sfxPressed;
    private boolean toMenuPressed, nextLevelPressed, restartPressed;
    private boolean resumePressed;
    private boolean levelPageLeftPressed, levelPageRightPressed;
    private int volumeBgm = 100, volumeSfx = 100;

    private int ticks = 0;

    /** Completion screen level time to show, in seconds */
    private float completedLevelTime;
    /** Whether to show "new best" time */
    private boolean showNewBestTime;

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

    public void setLevel(int level) { levelPressed = level; }

    public int getVolumeBgm() { return volumeBgm; }

    public int getVolumeSfx() { return volumeSfx; }

    public MenuMode(GameCanvas canvas){
        this.canvas = canvas;
        active = false;
        type = Type.START;
        canvas.updateCam(canvas.getWidth()/2, canvas.getHeight()/2, 1/sx,
                new Rectangle(0, 0, 32, 18), 32);
    }

    /**
     * Set the completed-level time
     * @param completedLevelTime time to show, in seconds
     */
    public void setCompletedLevelTime(float completedLevelTime) {
        this.completedLevelTime = completedLevelTime;
    }

    /**
     * Set whether to show "new best" for the time
     * @param showNewBestTime true if show new best
     */
    public void setShowNewBestTime(boolean showNewBestTime) {
        this.showNewBestTime = showNewBestTime;
    }

    /**
     * Format float seconds into a nicer string
     * @param seconds seconds to format
     * @return seconds string with two decimal places
     */
    private String formatSecondsString(float seconds) {
        return String.format("%.2f", seconds);
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
        return inBounds(x, y, canvas.getWidth()/2f-playButton.getWidth()/2f*sx,
                canvas.getWidth()/2f+playButton.getWidth()/2f*sx,
                (PLAY_OFFSET_Y+playButton.getHeight()/2f)*sy,
                (PLAY_OFFSET_Y-playButton.getHeight()/2f)*sy);
    }

    private boolean inOptionsBounds(int x, int y){
        return inBounds(x, y, canvas.getWidth()/2f-optionsButton.getWidth()/2f*sx,
                canvas.getWidth()/2f+optionsButton.getWidth()/2f*sx,
                (OPTIONS_OFFSET_Y+optionsButton.getHeight()/2f)*sy,
                (OPTIONS_OFFSET_Y-optionsButton.getHeight()/2f)*sy);
    }

    private boolean inExitBounds(int x, int y){
        return inBounds(x, y, canvas.getWidth()/2f-exitButton.getWidth()/2f*sx,
                canvas.getWidth()/2f+exitButton.getWidth()/2f*sx,
                (EXIT_OFFSET_Y+exitButton.getHeight()/2f)*sy,
                (EXIT_OFFSET_Y-exitButton.getHeight()/2f)*sy);
    }

    private boolean inOptionsReturnBounds(int x, int y){
        return inBounds(x, y, OPTIONS_RETURN_OFFSET_X * sx, (OPTIONS_RETURN_OFFSET_X + 200f) * sx,
                OPTIONS_RETURN_OFFSET_Y * sy, (OPTIONS_RETURN_OFFSET_Y - 40f) * sy);
    }

    private boolean inClearSaveBounds(int x, int y) {
        return inBounds(x, y, OPTIONS_CLEAR_SAVE_OFFSET_X * sx, (OPTIONS_CLEAR_SAVE_OFFSET_X + 440f) * sx,
                OPTIONS_CLEAR_SAVE_OFFSET_Y * sy, (OPTIONS_CLEAR_SAVE_OFFSET_Y - 40f) * sy);
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
        return inBounds(x, y, PAUSE_BUTTONS_OFFSET_X * sx,
                (PAUSE_BUTTONS_OFFSET_X + resume.getWidth()) * sx,
                (PAUSE_RESUME_OFFSET_Y + resume.getHeight()) * sy, PAUSE_RESUME_OFFSET_Y * sy);
    }

    private boolean inRestartPauseBounds(int x, int y){
        return inBounds(x, y, PAUSE_BUTTONS_OFFSET_X * sx,
                (PAUSE_BUTTONS_OFFSET_X + restartPause.getWidth()) * sx,
                (PAUSE_RESTART_OFFSET_Y + restartPause.getHeight()) * sy, PAUSE_RESTART_OFFSET_Y * sy);
    }

    private boolean inOptionsPauseBounds(int x, int y){
        return inBounds(x, y, PAUSE_BUTTONS_OFFSET_X * sx,
                (PAUSE_BUTTONS_OFFSET_X + optionsPause.getWidth()) * sx,
                (PAUSE_OPTIONS_OFFSET_Y + optionsPause.getHeight()) * sy, PAUSE_OPTIONS_OFFSET_Y * sy);
    }

    private boolean inMenuPauseBounds(int x, int y){
        return inBounds(x, y, PAUSE_BUTTONS_OFFSET_X * sx,
                (PAUSE_BUTTONS_OFFSET_X + menuPause.getWidth()) * sx,
                (PAUSE_MENU_OFFSET_Y + menuPause.getHeight()) * sy, PAUSE_MENU_OFFSET_Y * sy);
    }

    private boolean inLevelPreviousBounds(int x, int y){
        return inBounds(x, y,  (LEVEL_PAGE_OFFSET_X_LEFT - levelPageSwitch[0].getWidth() / 2f) * sx,
                (LEVEL_PAGE_OFFSET_X_LEFT + levelPageSwitch[0].getWidth() / 2f) * sx,
                (LEVEL_PAGE_OFFSET_Y + levelPageSwitch[0].getHeight() / 2f) * sy,
                (LEVEL_PAGE_OFFSET_Y - levelPageSwitch[0].getHeight() / 2f) * sy);
    }

    private boolean inLevelNextBounds(int x, int y){
        return inBounds(x, y,  (LEVEL_PAGE_OFFSET_X_RIGHT - levelPageSwitch[0].getWidth() / 2f) * sx,
                (LEVEL_PAGE_OFFSET_X_RIGHT + levelPageSwitch[0].getWidth() / 2f) * sx,
                (LEVEL_PAGE_OFFSET_Y + levelPageSwitch[0].getHeight() / 2f) * sy,
                (LEVEL_PAGE_OFFSET_Y - levelPageSwitch[0].getHeight() / 2f) * sy);
    }

    public boolean inBounds(int x, int y, float left, float right, float up, float down){
        return left <= x && x <= right && down <= y && y <= up;
    }

    public void update(float dt) {
        themeId = SoundPlayer.loopSound(theme, themeId, volumeBgm / 100f);
        ticks++;
        if (type == Type.PAUSE && ticks % 30 == 0)
            pause_anim_frame = (pause_anim_frame + 1) % PAUSE_ANIM_FRAME;
        if (type == Type.LEVEL_COMPLETE && ticks % 5 == 0)
            winFrame = (winFrame + 1) % WIN_FRAME;
    }

    public void draw(){
        canvas.begin();
//        canvas.draw(background, Color.WHITE,0, 0, STANDARD_WIDTH, STANDARD_HEIGHT);
        canvas.draw(background,Color.WHITE,0, 0, canvas.getWidth(), canvas.getHeight());
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
                canvas.getWidth()/2f, TITLE_OFFSET_Y*sy, 0, sx, sy);

        canvas.draw(playButton, playPressed?Color.GRAY:Color.WHITE, playButton.getWidth()/2f,
                playButton.getHeight()/2f,canvas.getWidth()/2f, PLAY_OFFSET_Y*sy, 0,
                sx, sy);
        canvas.draw(optionsButton, optionsPressed?Color.GRAY:Color.WHITE, optionsButton.getWidth()/2f,
                optionsButton.getHeight()/2f, canvas.getWidth()/2f, OPTIONS_OFFSET_Y*sy, 0,
                sx, sy);
        canvas.draw(exitButton, exitPressed?Color.GRAY:Color.WHITE, exitButton.getWidth()/2f,
                exitButton.getHeight()/2f, canvas.getWidth()/2f, EXIT_OFFSET_Y*sy, 0,
                sx, sy);
    }

    public void drawLevelSelect(){
        canvas.draw(levelSelect, Color.WHITE, levelSelect.getWidth()/2f, levelSelect.getHeight()/2f,
                canvas.getWidth()/2f, TITLE_OFFSET_Y*sy, 0, sx, sy);
        int x, y, num;
        float bestTime;
        Color tint;
        for (int i = 0; i < LEVEL_BUTTON_ROWS; i++){
            for (int j = 0; j < LEVEL_BUTTON_COLS; j++){
                num = i * LEVEL_BUTTON_COLS + j + levelPage * LEVEL_BUTTON_COLS * LEVEL_BUTTON_ROWS;
                tint = levelPressed == num ? Color.GRAY : Color.WHITE;
                x = LEVEL_BUTTONS_OFFSET_X + j * LEVEL_BUTTONS_MARGIN;
                y = LEVEL_BUTTONS_OFFSET_Y - i * LEVEL_BUTTONS_MARGIN;
                canvas.draw(levelButton[levelPage], tint, levelButton[0].getWidth()/2f,
                        levelButton[0].getHeight()/2f, x*sx, y*sy, 0, sx, sy);
                canvas.drawTextCentered(Integer.toString(num + 1), displayFont, x * sx, y * sy);
                bestTime = SaveManager.getInstance().getBestTime("level" + (num + 1));
                if (bestTime != -1) {
                    canvas.drawTextCentered(formatSecondsString(bestTime), buttonFont,
                            (x + LEVEL_BEST_TIME_OFFSET_X) * sx, (y + LEVEL_BEST_TIME_OFFSET_Y) * sy);
                }
            }
        }
        if (levelPage != 0) {
            tint = levelPageLeftPressed ? Color.GRAY : Color.WHITE;
            canvas.draw(levelPageSwitch[levelPage], tint, levelPageSwitch[0].getWidth()/2f,
                    levelPageSwitch[0].getHeight()/2f,
                    LEVEL_PAGE_OFFSET_X_LEFT * sx, LEVEL_PAGE_OFFSET_Y * sy, 0, -sx, -sy);
        }
        if (levelPage != LEVEL_PAGES - 1) {
            tint = levelPageRightPressed ? Color.GRAY : Color.WHITE;
            canvas.draw(levelPageSwitch[levelPage], tint, levelPageSwitch[0].getWidth()/2f,
                    levelPageSwitch[0].getHeight()/2f,
                    LEVEL_PAGE_OFFSET_X_RIGHT * sx, LEVEL_PAGE_OFFSET_Y * sy, 0, sx, sy);
        }
    }

    public void drawOptions(){
        canvas.draw(windowBg, Color.WHITE, 0, 0,
                canvas.getWidth()/2f - windowBg.getWidth()/2f * sx, WINDOW_BG_OFFSET_Y * sy,0, sx, sy);
        canvas.drawText("options", titleFont, WINDOW_TITLE_OFFSET_X * sx, WINDOW_TITLE_OFFSET_Y * sy);
        canvas.drawText("return", buttonFont, OPTIONS_RETURN_OFFSET_X * sx, OPTIONS_RETURN_OFFSET_Y * sy);
        canvas.drawText("[GRAY]clear save data[]", buttonFont, OPTIONS_CLEAR_SAVE_OFFSET_X * sx, OPTIONS_CLEAR_SAVE_OFFSET_Y * sy);

        canvas.drawText("Volume", labelFont, OPTIONS_LABEL_OFFSET_X * sx, OPTIONS_VOLUME_LABEL_OFFSET_Y * sy);
        canvas.drawText("BGM", labelFont2, OPTIONS_LABEL_2_OFFSET_X * sx, OPTIONS_BGM_LABEL_OFFSET_Y * sy);
        canvas.drawText("SFX", labelFont2, OPTIONS_LABEL_2_OFFSET_X * sx, OPTIONS_SFX_LABEL_OFFSET_Y * sy);

        drawSliders();

        canvas.drawText("Accessibility Mode", labelFont,
                OPTIONS_LABEL_OFFSET_X * sx, OPTIONS_ACCESS_LABEL_OFFSET_Y * sy);
        canvas.draw(accessibilitySelected ? toggleOn : toggleOff, Color.WHITE,
                0, 0, OPTIONS_ACCESS_OFFSET_X * sx,OPTIONS_ACCESS_OFFSET_Y * sy, 0, sx, sy);
    }

    private void drawSliders(){
        canvas.draw(slideOn, Color.WHITE, OPTIONS_SLIDE_OFFSET_X * sx, OPTIONS_SLIDE_BGM_OFFSET_Y * sy,
                volumeBgm * slideOn.getWidth() / 100f * sx, slideOn.getHeight() * sy);
        canvas.draw(slideOff, Color.WHITE, (OPTIONS_SLIDE_OFFSET_X + volumeBgm * slideOn.getWidth() / 100f) * sx,
                 OPTIONS_SLIDE_BGM_OFFSET_Y * sy, (100 - volumeBgm) * slideOff.getWidth() / 100f * sx,
                slideOff.getHeight() * sy);
        canvas.draw(slideOnLeft, Color.WHITE, 0, 0, (OPTIONS_SLIDE_OFFSET_X - slideOnLeft.getWidth()) * sx,
                OPTIONS_SLIDE_BGM_OFFSET_Y * sy, 0, sx, sy);
        canvas.draw(slideOffRight, Color.WHITE, 0, 0, (OPTIONS_SLIDE_OFFSET_X + slideOff.getWidth()) * sx,
                OPTIONS_SLIDE_BGM_OFFSET_Y * sy, 0, sx, sy);

        canvas.draw(slideOn, Color.WHITE, OPTIONS_SLIDE_OFFSET_X * sx, OPTIONS_SLIDE_SFX_OFFSET_Y * sy,
                volumeSfx * slideOn.getWidth() / 100f * sx, slideOn.getHeight() * sy);
        canvas.draw(slideOff, Color.WHITE, (OPTIONS_SLIDE_OFFSET_X + volumeSfx * slideOn.getWidth() / 100f) * sx,
                OPTIONS_SLIDE_SFX_OFFSET_Y * sy, (100 - volumeSfx) * slideOff.getWidth() / 100f * sx,
                slideOff.getHeight() * sy);
        canvas.draw(slideOnLeft, Color.WHITE, 0, 0, (OPTIONS_SLIDE_OFFSET_X - slideOnLeft.getWidth()) * sx,
                OPTIONS_SLIDE_SFX_OFFSET_Y * sy, 0, sx, sy);
        canvas.draw(slideOffRight, Color.WHITE, 0, 0, (OPTIONS_SLIDE_OFFSET_X + slideOff.getWidth()) * sx,
                OPTIONS_SLIDE_SFX_OFFSET_Y * sy, 0, sx, sy);

        canvas.draw(slideThumb, Color.WHITE, 0, 0,
                (OPTIONS_SLIDE_OFFSET_X + volumeBgm * slideOn.getWidth() / 100f - slideThumb.getWidth() / 2f) * sx,
                (OPTIONS_SLIDE_BGM_OFFSET_Y + slideOn.getHeight() / 2f - slideThumb.getHeight() / 2f) * sy,
                0, sx, sy);
        canvas.draw(slideThumb, Color.WHITE, 0, 0,
                (OPTIONS_SLIDE_OFFSET_X + volumeSfx * slideOn.getWidth() / 100f - slideThumb.getWidth() / 2f) * sx,
                (OPTIONS_SLIDE_SFX_OFFSET_Y + slideOn.getHeight() / 2f - slideThumb.getHeight() / 2f) * sy,
                0, sx, sy);

        canvas.drawText(Integer.toString(volumeBgm), labelFont2,
                OPTIONS_VOLUME_NUM_OFFSET_X * sx, OPTIONS_BGM_LABEL_OFFSET_Y * sy);
        canvas.drawText(Integer.toString(volumeSfx), labelFont2,
                OPTIONS_VOLUME_NUM_OFFSET_X * sx, OPTIONS_SFX_LABEL_OFFSET_Y * sy);
    }

    public void drawComplete() {
        canvas.draw(windowBg, Color.WHITE, 0, 0,
                canvas.getWidth()/2f - windowBg.getWidth()/2f * sx, WINDOW_BG_OFFSET_Y * sy,0, sx, sy);
        canvas.drawText("Level " + (levelPressed + 1) + " completed!",
                titleFont, WINDOW_TITLE_OFFSET_X * sx, WINDOW_TITLE_OFFSET_Y * sy);

        win.setFrame((int)winFrame);
        canvas.draw(win, Color.WHITE, win.getRegionWidth()/2f, win.getRegionHeight()/2f,
                canvas.getWidth()/2f, canvas.getHeight()/2.5f, 0, sx * 1.5f, sy * 1.5f);

        String timeString = showNewBestTime
                ? "Time: [#FD3796]" + formatSecondsString(completedLevelTime) + " New Best![]"
                : "Time: " + formatSecondsString(completedLevelTime);
        canvas.drawText(timeString, titleFont, COMPLETE_LEVEL_TIME_OFFSET_X * sx, COMPLETE_LEVEL_TIME_OFFSET_Y * sy);


        canvas.draw(exitToMenu, toMenuPressed?Color.GRAY:Color.WHITE, COMPLETE_MENU_OFFSET_X * sx,
                COMPLETE_BUTTONS_OFFSET_Y * sy, exitToMenu.getWidth() * sx, exitToMenu.getHeight() * sy);
        canvas.draw(restart, restartPressed?Color.GRAY:Color.WHITE, COMPLETE_RESTART_OFFSET_X * sx,
                COMPLETE_BUTTONS_OFFSET_Y * sy, restart.getWidth() * sx, restart.getHeight() * sy);
        canvas.draw(nextLevel, nextLevelPressed?Color.GRAY:Color.WHITE, COMPLETE_NEXT_OFFSET_X * sx,
                COMPLETE_BUTTONS_OFFSET_Y * sy, nextLevel.getWidth() * sx, nextLevel.getHeight() * sy);
    }

    public void drawPause() {
        canvas.draw(windowBg, Color.WHITE, 0, 0,
                canvas.getWidth()/2f - windowBg.getWidth()/2f * sx, WINDOW_BG_OFFSET_Y * sy,0, sx, sy);
        canvas.draw(pause[pause_anim_frame], Color.WHITE,0, 0,
                PAUSE_ANIM_OFFSET_X * sx, PAUSE_ANIM_OFFSET_Y * sy, 0, sx, sy);
        titleFont.setColor(Color.WHITE);
        canvas.drawText("paused", titleFont, PAUSE_LABEL_OFFSET_X * sx, PAUSE_LABEL_OFFSET_Y * sy);
        titleFont.setColor(labelColor);
        canvas.draw(resume, resumePressed?Color.GRAY:Color.WHITE, PAUSE_BUTTONS_OFFSET_X * sx,
                PAUSE_RESUME_OFFSET_Y * sy, resume.getWidth() * sx, resume.getHeight() * sy);
        canvas.draw(restartPause, restartPressed?Color.GRAY:Color.WHITE,
                PAUSE_BUTTONS_OFFSET_X * sx, PAUSE_RESTART_OFFSET_Y * sy,
                restartPause.getWidth() * sx, restartPause.getHeight() * sy);
        canvas.draw(optionsPause, optionsPressed?Color.GRAY:Color.WHITE,
                PAUSE_BUTTONS_OFFSET_X * sx, PAUSE_OPTIONS_OFFSET_Y * sy,
                optionsPause.getWidth() * sx, optionsPause.getHeight() * sy);
        canvas.draw(menuPause, toMenuPressed?Color.GRAY:Color.WHITE,
                PAUSE_BUTTONS_OFFSET_X * sx, PAUSE_MENU_OFFSET_Y * sy,
                menuPause.getWidth() * sx, menuPause.getHeight() * sy);
    }

    public void gatherAssets(AssetDirectory directory) {
        playButton = directory.getEntry("menu:play", Texture.class);
        optionsButton = directory.getEntry("menu:options", Texture.class);
        exitButton = directory.getEntry("menu:exit", Texture.class);
        background = directory.getEntry("menu:bg", Texture.class);
        windowBg = directory.getEntry("menu:windowbg", Texture.class);
        for (int i = 0; i < PAUSE_ANIM_FRAME; i++){
            pause[i] = directory.getEntry("menu:pause" + (i+1), Texture.class);
        }
        win = new FilmStrip(directory.getEntry("menu:win", Texture.class), 4, 6);
        title = directory.getEntry("menu:title", Texture.class);
        levelSelect = directory.getEntry("menu:level", Texture.class);
        for (int i = 0; i < LEVEL_PAGES; i++){
            levelButton[i] = directory.getEntry("menu:levelbutton" + (i+1), Texture.class);
            levelPageSwitch[i] = directory.getEntry("menu:levelpage" + (i+1), Texture.class);
        }
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
        titleFont.getData().markupEnabled = true;
        buttonFont = directory.getEntry("shared:aliensmall", BitmapFont.class);
        buttonFont.getData().markupEnabled = true;
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
        canvas.width = canvas.getWidth();
        canvas.height = canvas.getHeight();
        active = true;
        Gdx.input.setInputProcessor( this );
    }


    @Override
    public void resize(int width, int height) {
        // Compute the drawing scale
        sx = ((float)width)/STANDARD_WIDTH;
        sy = ((float)height)/STANDARD_HEIGHT;
        resizeFont();
        canvas.width = width;
        canvas.height = height;
    }

    private void resizeFont(){
        titleFont.getData().setScale(sx, sy);
        buttonFont.getData().setScale(sx, sy);
        displayFont.getData().setScale(sx, sy);
        labelFont.getData().setScale(sx, sy);
        labelFont2.getData().setScale(sx, sy);
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
                left = x - levelButton[0].getWidth() / 2f;
                right = x + levelButton[0].getWidth() / 2f;
                up = y + levelButton[0].getHeight() / 2f;
                down = y - levelButton[0].getHeight() / 2f;
                if (inBounds((int) (screenX / sx), (int) (screenY / sy), left, right, up, down)){
                    levelPressed = i * LEVEL_BUTTON_COLS + j + levelPage * LEVEL_BUTTON_ROWS * LEVEL_BUTTON_COLS;
                    return true;
                }
            }
        }
        if (levelPage != 0 && inLevelPreviousBounds(screenX, screenY)) {
            levelPageLeftPressed = true;
            return true;
        }
        if (levelPage != LEVEL_PAGES - 1 && inLevelNextBounds(screenX, screenY)) {
            levelPageRightPressed = true;
            return true;
        }
        return false;
    }

    private boolean touchDownOptions(int screenX, int screenY){
        if (inOptionsReturnBounds(screenX, screenY)) {
            optionsReturnPressed = true;
            return false;
        } else if (inClearSaveBounds(screenX, screenY)) {
            clearSavePressed = true;
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
            levelPressed = -1;
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
            int i = (levelPressed - levelPage * LEVEL_BUTTON_COLS * LEVEL_BUTTON_ROWS) / LEVEL_BUTTON_COLS;
            int j = (levelPressed - levelPage * LEVEL_BUTTON_COLS * LEVEL_BUTTON_ROWS) % LEVEL_BUTTON_COLS;
            int left = LEVEL_BUTTONS_OFFSET_X + j * LEVEL_BUTTONS_MARGIN - levelButton[0].getWidth() / 2;
            int right = LEVEL_BUTTONS_OFFSET_X + j * LEVEL_BUTTONS_MARGIN + levelButton[0].getWidth() / 2;
            int up = LEVEL_BUTTONS_OFFSET_Y - i * LEVEL_BUTTONS_MARGIN + levelButton[0].getHeight() / 2;
            int down = LEVEL_BUTTONS_OFFSET_Y - i * LEVEL_BUTTONS_MARGIN - levelButton[0].getHeight() / 2;
            if (inBounds((int) (screenX / sx), (int) (screenY / sy), left, right, up, down)){
                typePrevious = Type.LEVEL_SELECT;
                listener.exitScreen(this, Constants.EXIT_LEVEL);
            }
            levelPressed = -1;
        } else if (levelPageLeftPressed && inLevelPreviousBounds(screenX, screenY)){
            levelPage -= 1;
        } else if (levelPageRightPressed && inLevelNextBounds(screenX, screenY)){
            levelPage += 1;
        }
        levelPageLeftPressed  = false;
        levelPageRightPressed  = false;
        return false;
    }

    private boolean touchUpOptions(int screenX, int screenY){
        boolean result = true;
        if (inOptionsReturnBounds(screenX, screenY) && optionsReturnPressed){
            ColorRegionModel.setDisplay(accessibilitySelected);
            this.type = typePrevious;
            typePrevious = Type.OPTIONS;
            result = false;
        } else if (inClearSaveBounds(screenX, screenY) && clearSavePressed) {
            SaveManager.getInstance().clearBestTimes();
            result = false; // what does this do
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
