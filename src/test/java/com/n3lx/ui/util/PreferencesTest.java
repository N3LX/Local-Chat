package com.n3lx.ui.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PreferencesTest {

    @Test
    public void testGetThemeCssPathOnStartup() {
        //Expect the initial value to always be LIGHT
        assertEquals(Preferences.THEME.LIGHT, Preferences.getTheme());
        assertEquals("stylesheet_light.css", Preferences.getThemeCssPath());

        //Check if switching the themes correctly influences the getThemeCssPath() method
        Preferences.setTheme(Preferences.THEME.DARK);
        assertEquals("stylesheet_dark.css", Preferences.getThemeCssPath());

        Preferences.setTheme(Preferences.THEME.LIGHT);
        assertEquals("stylesheet_light.css", Preferences.getThemeCssPath());
    }

}
