package edu.cornell.gdiac.discodale.models;

import com.badlogic.gdx.graphics.Color;

public enum DaleColor {
    RED,
    YELLOW,
    BLUE;

    public Color toGdxColor() {
        switch (this) {
            case RED:
                return Color.valueOf("FD3895");
            case BLUE:
                return Color.valueOf("04CDF8");
            case YELLOW:
                return Color.valueOf("02E847");
            default:
                return Color.WHITE;
        }
    }

    public int toColorTexture() {
        switch (this) {
            case RED:
                return 0;
            case BLUE:
                return 1;
            case YELLOW:
                return 2;
            default:
                return -1;
        }
    }
}
