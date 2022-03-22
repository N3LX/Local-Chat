package com.n3lx.ui.util;

public class Preferences {

    private static THEME theme = THEME.LIGHT;

    private static boolean allowTimestamps = false;

    private Preferences() {
    }

    public static THEME getTheme() {
        return theme;
    }

    public static void setTheme(THEME newTheme) {
        theme = newTheme;
    }

    public static String getThemeCssPath() {
        return switch (theme) {
            case LIGHT -> "stylesheet_light.css";
            case DARK -> "stylesheet_dark.css";
        };
    }

    public static boolean getAllowTimestamps() {
        return allowTimestamps;
    }

    public static void setAllowTimestamps(boolean state) {
        allowTimestamps = state;
    }

    public enum THEME {LIGHT, DARK}

}

