package edu.cornell.gdiac.discodale.models;

import com.badlogic.gdx.graphics.Color;

public enum DaleColor {
    PINK,
    GREEN,
    BLUE;

    public Color toGdxColor() {
        switch (this) {
            case PINK:
                return Color.valueOf("FD3895");
            case GREEN:
                return Color.valueOf("02E847");
            case BLUE:
                return Color.valueOf("04CDF8");
            default:
                return Color.WHITE;
        }
    }

    public int toColorTexture() {
        switch (this) {
            case PINK:
                return 0;
            case BLUE:
                return 1;
            case GREEN:
                return 2;
            default:
                return -1;
        }
    }
}
