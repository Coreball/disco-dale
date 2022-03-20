package edu.cornell.gdiac.discodale.models;

import com.badlogic.gdx.graphics.Color;

public enum DaleColor {
    RED,
    YELLOW,
    BLUE;

    public Color toGdxColor() {
        switch (this) {
            case RED:
                return Color.RED;
            case BLUE:
                return Color.BLUE;
            case YELLOW:
                return Color.YELLOW;
            default:
                return Color.WHITE;
        }
    }
}
