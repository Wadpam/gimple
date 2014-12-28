package com.wadpam.gimple;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by sosandstrom on 2014-12-28.
 */
public class GimpleMojoTest {

    @Test
    public void testGetNextDevelopmentVersion() {
        assertEquals("1.1-SNAPSHOT", GimpleMojo.getNextDevelopmentVersion("1.0"));
        assertEquals("10-SNAPSHOT", GimpleMojo.getNextDevelopmentVersion("9"));
        assertEquals("11.38.50-SNAPSHOT", GimpleMojo.getNextDevelopmentVersion("11.38.49"));
    }
}
