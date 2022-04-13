package edu.cornell.gdiac.discodale.models;

import com.badlogic.gdx.graphics.Color;

public enum DaleColor {
    PINK,
    BLUE,
    GREEN,
    PURPLE,
    ORANGE;

    public Color toGdxColor() {
        switch (this) {
            case PINK:
                return Color.valueOf("FD3796");
            case BLUE:
                return Color.valueOf("05CDF9");
            case GREEN:
                return Color.valueOf("0BE748");
            case PURPLE:
                return Color.valueOf("A933F1");
            case ORANGE:
                return Color.valueOf("FD7900");
            default:
                return Color.WHITE;
        }
    }
}
