/**
 * $Id$
 * $URL$
 * TimestampConverter.java - genericdao - Sep 8, 2008 2:29:55 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.azeckoski.reflectutils.converters;

import java.sql.Timestamp;

import org.azeckoski.reflectutils.converters.api.Converter;


/**
 * Passthrough to {@link DateConverter} for {@link Timestamp}
 * @see DateConverter
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class TimestampConverter implements Converter<Timestamp> {

    public Timestamp convert(Object value) {
        return DateConverter.convertToType(Timestamp.class, value);
    }

}
