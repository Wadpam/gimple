package com.wadpam.gimple;

/*
 * #%L
 * com.wadpam.gimple:gimple-maven-plugin
 * %%
 * Copyright (C) 2010 - 2014 Wadpam
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
