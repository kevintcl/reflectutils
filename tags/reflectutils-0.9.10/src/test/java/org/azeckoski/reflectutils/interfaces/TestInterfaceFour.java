/**
 * $Id$
 * $URL$
 * TestInterfaceFour.java - entity-broker - May 5, 2008 1:29:50 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2
 * 
 * A copy of the Apache License, Version 2 has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.azeckoski.reflectutils.interfaces;


/**
 * Test interface that extends 4 others
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
public interface TestInterfaceFour extends TestInterfaceOne, Runnable, Cloneable, Readable {

}
