/**
 * $Id$
 * $URL$
 * LongConverter.java - genericdao - Sep 8, 2008 11:29:32 AM - azeckoski
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

import org.azeckoski.reflectutils.converters.api.Converter;



/**
 * Long passthrough
 * @see NumberConverter for more details
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class LongConverter implements Converter<Long> {

    public Long convert(Object value) {
        return NumberConverter.convertToType(Long.class, value);
    }

}
