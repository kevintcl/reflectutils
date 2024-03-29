/**
 * $Id$
 * $URL$
 * TestBean.java - genericdao - May 18, 2008 10:20:20 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.azeckoski.reflectutils.classes;

import org.azeckoski.reflectutils.annotations.TestAnnote;
import org.azeckoski.reflectutils.annotations.TestAnnoteClass1;
import org.azeckoski.reflectutils.annotations.TestAnnoteField1;

/**
 * {@link TestAnnoteClass1} <br/>
 * int myInt = 0 ({@link TestAnnote} on getter) <br/>
 * String myString = "woot" ({@link TestAnnoteField1} on private field)  <br/>
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com)
 */
@TestAnnoteClass1
public class TestBean {
    private int myInt = 0;
    @TestAnnoteField1
    private String myString = "woot";
    @TestAnnote
    public int getMyInt() {
        return myInt;
    }
    public void setMyInt(int myInt) {
        this.myInt = myInt;
    }
    public String getMyString() {
        return myString;
    }
    public void setMyString(String myString) {
        this.myString = myString;
    }

    public TestBean() {}
    public TestBean(int myInt) {
        this.myInt = myInt;
    }
    public TestBean(int myInt, String myString) {
        this.myInt = myInt;
        this.myString = myString;
    }
}