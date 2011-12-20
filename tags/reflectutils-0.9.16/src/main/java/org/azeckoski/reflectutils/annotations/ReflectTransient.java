/**
 * $Id$
 * $URL$
 * ReflectTransient.java - genericdao - Sep 18, 2008 3:23:49 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.azeckoski.reflectutils.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Indicates that a field or method should be treated as if they were transient
 * when serializing the fields (i.e. skipped over)
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface ReflectTransient { }
