/**
 * $Id$
 * $URL$
 * BigIntegerConverter.java - genericdao - Sep 8, 2008 11:22:47 AM - azeckoski
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

import java.math.BigInteger;

import org.azeckoski.reflectutils.converters.api.Converter;



/**
 * BigInteger passthrough
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class BigIntegerConverter implements Converter<BigInteger> {
    public BigInteger convert(Object value) {
        return NumberConverter.convertToType(BigInteger.class, value);
    }
}
