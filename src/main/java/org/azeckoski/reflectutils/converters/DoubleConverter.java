/**
 * $Id$
 * $URL$
 * DoubleConverter.java - genericdao - Sep 8, 2008 11:27:44 AM - azeckoski
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
 * Double passthrough
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class DoubleConverter implements Converter<Double> {

    public Double convert(Object value) {
        return NumberConverter.convertToType(Double.class, value);
    }

}
