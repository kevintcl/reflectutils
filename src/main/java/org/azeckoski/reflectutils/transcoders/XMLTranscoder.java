/**
 * $Id$
 * $URL$
 * XMLEncoder.java - entity-broker - Sep 15, 2008 6:36:42 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.azeckoski.reflectutils.transcoders;

import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.azeckoski.reflectutils.ArrayUtils;
import org.azeckoski.reflectutils.ConstructorUtils;
import org.azeckoski.reflectutils.DateUtils;
import org.azeckoski.reflectutils.FieldUtils;
import org.azeckoski.reflectutils.ReflectUtils;
import org.azeckoski.reflectutils.ClassFields.FieldsFilter;
import org.azeckoski.reflectutils.map.ArrayOrderedMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Provides methods for encoding and decoding XML <br/>
 * Note that the XML parser always trashes the root node currently
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class XMLTranscoder implements Transcoder {

    private static final String ELEMENT = "element";

    public String getHandledFormat() {
        return "xml";
    }

    public String encode(Object object, String name, Map<String, Object> properties) {
        String encoded = "";
        if (object != null) {
            // only set the name if this is not null to preserve the "null" tag
            if (name == null || "".equals(name)) {
                name = DATA_KEY;
            }
        }
        encoded = XMLTranscoder.makeXML(object, name, properties, humanOutput, includeNulls, includeClass, includeClassField, maxLevel);
        return encoded;
    }

    public Map<String, Object> decode(String string) {
//        Object decode = new XMLparser(string).getObject();
//        if (decode instanceof Map) {
//            decoded = (Map<String, Object>) decode;
//        } else {
//            // if the result is not a map then simply put the result into a map
//            decoded = new ArrayOrderedMap<String, Object>();
//            decoded.put(Transcoder.DATA_KEY, decode);
//        }
        return new XMLparser(string).getMap();
    }

    /**
     * Default constructor:
     * See other constructors for options
     */
    public XMLTranscoder() {}

    private boolean humanOutput = false;
    private boolean includeNulls = true;
    private boolean includeClass = false;
    private boolean includeClassField = false;

    /**
     * @param humanOutput if true then enable human readable output (includes indentation and line breaks)
     * @param includeNulls if true then create output tags for null values
     * @param includeClassField if true then include the value from the "getClass()" method as "class" when encoding beans and maps
     */
    public XMLTranscoder(boolean humanOutput, boolean includeNulls, boolean includeClassField) {
        this.humanOutput = humanOutput;
        this.includeNulls = includeNulls;
        this.includeClassField = includeClassField;
    }

    /**
     * @param humanOutput if true then enable human readable output (includes indentation and line breaks)
     * @param includeNulls if true then create output tags for null values
     * @param includeClassField if true then include the value from the "getClass()" method as "class" when encoding beans and maps
     * @param includeClass if true then add in class tips to the XML output
     */
    public XMLTranscoder(boolean humanOutput, boolean includeNulls, boolean includeClassField, boolean includeClass) {
        this.humanOutput = humanOutput;
        this.includeNulls = includeNulls;
        this.includeClassField = includeClassField;
        this.includeClass = includeClass;
    }

    private int maxLevel = 7;
    /**
     * @param maxLevel the number of objects to follow when traveling through the object,
     * 0 means only the fields in the initial object, default is 7
     */
    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }


    public static final char SPACE = ' ';
    public static final char AMP   = '&';
    /**
     * single quote (')
     */
    public static final char APOS  = '\'';
    public static final char BANG  = '!';
    public static final char EQ    = '=';
    public static final char GT    = '>';
    public static final char LT    = '<';
    public static final char QUEST = '?';
    public static final char QUOT  = '"';
    public static final char SLASH = '/';
    public static final char EOL   = '\n';


    /**
     * Convert an object into a well-formed, element-normal XML string.
     * @param object any object
     * @return the XML string version of the object
     */
    public static String makeXML(Object object) {
        return makeXML(object, null, null, false, true, false, false, 7);
    }

    /**
     * Convert an object into a well-formed, element-normal XML string.
     * @param object any object
     * @param tagName (optional) enclosing root tag
     * @param properties (optional) optional properties to add into the encoded data
     * @param humanOutput true of human readable output
     * @param includeNulls true to include null values when generating tags
     * @param maxLevel TODO
     * @return the XML string version of the object
     */
    public static String makeXML(Object object, String tagName, Map<String, Object> properties, boolean humanOutput, boolean includeNulls, boolean includeClass, boolean includeClassField, int maxLevel) {
        return toXML(object, tagName, 0, maxLevel, humanOutput, includeNulls, includeClass, includeClassField, properties);
    }

    @SuppressWarnings("unchecked")
    protected static String toXML(Object object, String tagName, int level, int maxLevel, boolean humanOutput, boolean includeNulls, boolean includeClass, boolean includeClassField, Map<String, Object> properties) {
        StringBuilder sb = new StringBuilder();

        if (object == null) {
            if (includeNulls) {
                // nulls are empty tags always
                tagName = validate(tagName == null ? "null" : tagName);
                makeLevelSpaces(sb, level, humanOutput);
                sb.append(LT);
                sb.append(tagName);
                sb.append(SLASH);
                sb.append(GT);
                makeEOL(sb, humanOutput);
            }
        } else {
            Class<?> type = ConstructorUtils.getWrapper(object.getClass());
            if ( ConstructorUtils.isClassSimple(type) ) {
                // Simple (String, Number, etc.)
                tagName = validate(tagName == null ? makeElementName(type) : tagName);
                String value = "";
                makeLevelSpaces(sb, level, humanOutput);
                sb.append(LT);
                sb.append(tagName);
                if (Date.class.isAssignableFrom(type) || Calendar.class.isAssignableFrom(type)) {
                    // date
                    Date d = null;
                    if (Date.class.isAssignableFrom(type)) {
                        d = (Date) object;
                    } else {
                        d = ((Calendar) object).getTime();
                    }
                    value = d.getTime()+"";
                    sb.append(" type='date' date='");
                    sb.append( DateUtils.makeDateISO8601(d) );
                    sb.append(APOS);
                } else if (Number.class.isAssignableFrom(type)) {
                    // number
                    sb.append(" type='number'");
                    if (includeClass) {
                        makeClassName(sb, type);
                    }
                    value = object.toString();
                } else if (Boolean.class.isAssignableFrom(type)) {
                    // boolean
                    value = object.toString();
                    sb.append(" type='boolean'");
                } else {
                    value = escapeForXML( object.toString() );
                }
                sb.append(GT);
                sb.append(value);
                sb.append(LT);
                sb.append(SLASH);
                sb.append(tagName);
                sb.append(GT);
                makeEOL(sb, humanOutput);
            } else if ( ConstructorUtils.isClassArray(type) ) {
                // ARRAY
                tagName = validate(tagName == null ? "array" : tagName);
                int length = ArrayUtils.size((Object[])object);
                Class<?> elementType = ArrayUtils.type((Object[])object);
                makeLevelSpaces(sb, level, humanOutput);
                sb.append(LT);
                sb.append(tagName);
                sb.append(" type='array' length='");
                sb.append(length);
                sb.append(APOS);
                if (includeClass) {
                    sb.append(" component='");
                    sb.append( ConstructorUtils.getTypeFromInnerCollection(elementType).getName() );
                    sb.append(APOS);
                }
                sb.append(GT);
                makeEOL(sb, humanOutput);
                for (int i = 0; i < length; ++i) {
                    sb.append( toXML(Array.get(object, i), makeElementName(elementType), level+1, maxLevel, humanOutput, includeNulls, includeClass, includeClassField, properties) );
                }
                makeLevelSpaces(sb, level, humanOutput);
                sb.append(LT);
                sb.append(SLASH);
                sb.append(tagName);
                sb.append(GT);
                makeEOL(sb, humanOutput);
            } else if ( ConstructorUtils.isClassCollection(type) ) {
                // COLLECTION
                tagName = validate(tagName == null ? "collection" : tagName);
                Collection<Object> collection = (Collection) object;
                makeLevelSpaces(sb, level, humanOutput);
                sb.append(LT);
                sb.append(tagName);
                sb.append(" type='collection' size='");
                sb.append(collection.size());
                sb.append(APOS);
                if (includeClass) {
                    makeClassName(sb, ConstructorUtils.getTypeFromInnerCollection(type));
                }
                sb.append(GT);
                makeEOL(sb, humanOutput);
                for (Object element : collection) {
                    Class<?> elementType = null;
                    if (element != null) {
                        elementType = element.getClass();
                    }
                    sb.append( toXML(element, makeElementName(elementType), level+1, maxLevel, humanOutput, includeNulls, includeClass, includeClassField, properties) );
                }
                makeLevelSpaces(sb, level, humanOutput);
                sb.append(LT);
                sb.append(SLASH);
                sb.append(tagName);
                sb.append(GT);
                makeEOL(sb, humanOutput);
            } else {
                // must be a bean or map, make sure it is a map
                tagName = validate(tagName == null ? makeElementName(type) : tagName);
                // special handling for certain object types
                String special = TranscoderUtils.checkObjectSpecial(object);
                if (special != null) {
                    if ("".equals(special)) {
                        // skip this one entirely
                    } else {
                        // just use the value in special to represent this
                        makeLevelSpaces(sb, level, humanOutput);
                        sb.append(LT);
                        sb.append(tagName);
                        sb.append(GT);
                        sb.append( escapeForXML(special) );
                        sb.append(LT);
                        sb.append(SLASH);
                        sb.append(tagName);
                        sb.append(GT);
                        makeEOL(sb, humanOutput);
                    }
                } else {
                    // normal handling
                    if (maxLevel <= level) {
                        // if the max level was reached then stop
                        sb.append(LT);
                        sb.append(tagName);
                        sb.append(GT);
                        sb.append( "MAX level reached (" );
                        sb.append( level );
                        sb.append( "):" );
                        sb.append( escapeForXML(object.toString()) );
                        sb.append(LT);
                        sb.append(SLASH);
                        sb.append(tagName);
                        sb.append(GT);
                        makeEOL(sb, humanOutput);
                    } else {
                        String xmlType = "bean";
                        Map<String, Object> map = null;
                        if (Map.class.isAssignableFrom(type)) {
                            xmlType = "map";
                            map = (Map<String, Object>) object;
                        } else {
                            // reflect over objects
                            map = ReflectUtils.getInstance().getObjectValues(object, FieldsFilter.SERIALIZABLE, false);
                        }
                        // add in the optional properties if it makes sense to do so
                        if (level == 0 && properties != null && ! properties.isEmpty()) {
                            map.putAll(properties);
                        }
                        makeLevelSpaces(sb, level, humanOutput);
                        sb.append(LT);
                        sb.append(tagName);
                        sb.append(" type='");
                        sb.append(xmlType);
                        sb.append(APOS);
                        sb.append(" size='");
                        sb.append(map.size());
                        sb.append(APOS);
                        if (includeClass) {
                            makeClassName(sb, ConstructorUtils.getTypeFromInnerCollection(type));
                        }
                        sb.append(GT);
                        makeEOL(sb, humanOutput);
                        for (Entry<String, Object> entry : map.entrySet()) {
                            if (entry.getKey() != null) {
                                sb.append( toXML(entry.getValue(), entry.getKey().toString(), level+1, maxLevel, humanOutput, includeNulls, includeClass, includeClassField, properties) );
                            }
                        }
                        makeLevelSpaces(sb, level, humanOutput);
                        sb.append(LT);
                        sb.append(SLASH);
                        sb.append(tagName);
                        sb.append(GT);
                        makeEOL(sb, humanOutput);
                    }
                }
            }
        }
        return sb.toString();
    }

    protected static String makeElementName(Class<?> type) {
        String name = ELEMENT;
        if (type != null) {
            if (Map.class.isAssignableFrom(type)) {
                // use the default "element"
            } else {
                String simpleName = type.getSimpleName().toLowerCase();
                // strip off the [] for arrays
                int index = simpleName.indexOf('[');
                if (index == 0) {
                    // weird to have [] at the beginning so just use default
                } else if (index > 0) {
                    name = simpleName.substring(0, index);
                } else {
                    // not array so just use the class name
                    // TODO maybe handle this prettier with by adding in "-" and stuff?
                    name = simpleName;
                }
            }
        }
        return name;
    }

    protected static void makeClassName(StringBuilder sb, Class<?> type) {
        if (type != null) {
            sb.append(" class='");
            sb.append( type.getName() );
            sb.append(APOS);
        }
    }

    protected static void makeEOL(StringBuilder sb, boolean includeEOL) {
        if (includeEOL) {
            sb.append(EOL);
        }
    }

    protected static final String SPACES = "  ";
    protected static void makeLevelSpaces(StringBuilder sb, int level, boolean includeEOL) {
        if (includeEOL) {
            for (int i = 0; i < level; i++) {
                sb.append(SPACES);
            }
        }
    }

    /**
     * Escape a string for XML encoding: replace special characters with XML escapes:
     * <pre>
     * &amp; <small>(ampersand)</small> is replaced by &amp;amp;
     * &lt; <small>(less than)</small> is replaced by &amp;lt;
     * &gt; <small>(greater than)</small> is replaced by &amp;gt;
     * &quot; <small>(double quote)</small> is replaced by &amp;quot;
     * </pre>
     * @param string The string to be escaped.
     * @return The escaped string.
     */
    public static String escapeForXML(String string) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, len = string.length(); i < len; i++) {
            char c = string.charAt(i);
            switch (c) {
            case AMP:
                sb.append("&amp;");
                break;
            case LT:
                sb.append("&lt;");
                break;
            case GT:
                sb.append("&gt;");
                break;
            case QUOT:
                sb.append("&quot;");
                break;
            default:
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Validates that a string contains no spaces and is non-null/non-empty
     * Throw an exception if the string contains whitespace. 
     * Whitespace is not allowed in tagNames and attributes.
     * @param string any string
     * @throws IllegalArgumentException
     */
    public static String validate(String string) {
        if (string == null) {
            throw new IllegalArgumentException("string is NULL");
        }
        int i, length = string.length();
        if (length == 0) {
            throw new IllegalArgumentException("Empty string.");
        }
        for (i = 0; i < length; i += 1) {
            if (Character.isWhitespace(string.charAt(i))) {
                throw new IllegalArgumentException("'" + string + "' contains a space character.");
            }
        }
        return string;
    }



    // DECODER

    protected SAXParserFactory parserFactory = null;
    protected SAXParser parser = null;
    protected SAXParser getParser() {
        if (parserFactory == null) {
            parserFactory = SAXParserFactory.newInstance();
            parserFactory.setValidating(true);
            parserFactory.setNamespaceAware(true);
        }
        if (parser == null) {
            try {
                parser = parserFactory.newSAXParser();
            } catch (ParserConfigurationException e) {
                throw new RuntimeException("Failed to get XML parser from factory: " + parserFactory, e);
            } catch (SAXException e) {
                throw new RuntimeException("Failed to get XML parser from factory: " + parserFactory, e);
            }
        } else {
            parser.reset();
        }
        return parser;
    }

    /**
     * Use SAX to process the XML document
     */
    public class XMLparser extends DefaultHandler {
        private String xml = null;
        private Map<String, Object> map = null;
        public XMLparser(String xml) {
            if (xml == null || "".equals(xml)) {
                throw new IllegalArgumentException("xml cannot be null or empty");
            }
            this.xml = xml;
            this.map = new ArrayOrderedMap<String, Object>();
            containerStack.push( new Container(this.map) ); // init the holder stack (causes root node to be trashed)
            parseXML(xml);
        }

        /**
         * @return the map which contains the data parsed out of the xml string
         */
        public Map<String, Object> getMap() {
            return map;
        }

        protected void parseXML(String xml) {
            try {
                getParser().parse( new ByteArrayInputStream(xml.getBytes()), this );
            } catch (SAXException se) {
                throw new IllegalArgumentException("Failed to parse xml ("+xml+"): " + se.getMessage(), se);
            } catch (IOException ie) {
                throw new RuntimeException("Failed to convert XML string ("+xml+") into inputstream", ie);
            }
        }

        // handle the XML parsing

        /**
         * Adds the value to the container using the given key,
         * if the key already exists in the container then the container needs to be switched
         * over to a collection and its contents moved, then the stack needs to be updated,
         * and finally the parent container needs to have it's value replaced
         */
        @SuppressWarnings("unchecked")
        protected void add(Container container, String key, Object value) {
            // first we need to make sure this container is on the stack
//            if (containerStack.peek() != container) {
//                containerStack.push( new Container(container.getContainer(), key, value) );
//            }
            // now do the add
            Class<?> type = container.getContainer().getClass();
            if ( ConstructorUtils.isClassMap(type)) {
                Map<String, Object> m = (Map)container.getContainer();
                if (m.containsKey(key)) {
                    // this should have been a collection so replace the map and move elements over to collection
                    Collection collection = (Collection) makeContainerObject(Types.COLLECTION);
                    for (Entry entry : m.entrySet()) {
                        collection.add( entry.getValue());
                    }
                    collection.add(value);
                    // now replace the container in the stack
                    int endPosition = containerStack.size()-1;
                    int containerPosition = endPosition;
                    if (container != containerStack.peek() && containerPosition != 0) {
                        containerPosition--;
                    }
                    Container current = containerStack.get(containerPosition);
                    current.replaceContainer(collection); // update container and replace the value in the parent object in the container
                    // finally we need to get the next thing in the stack to point back at the new parent
                    if (containerPosition < endPosition) {
                        // there is another container on the stack which needs to be replaced
                        containerStack.set(endPosition, new Container(collection, 1, value) );
                    }
                } else {
                    m.put(key, value);
                }
            } else if ( ConstructorUtils.isClassCollection(type)) {
                Collection collection = ((Collection)container.getContainer());
                collection.add(value);
                // make sure the parent index is correct
                if (container != containerStack.peek()) {
                    containerStack.peek().updateIndex(collection.size() - 1);
                }
            } else {
                // bean or something we hope
                try {
                    ReflectUtils.getInstance().setFieldValue(container.getContainer(), key, value);
                } catch (RuntimeException e) {
                    throw new RuntimeException("Unknown container type ("+type+") and could not set field on container: " + container, e);
                }
            }
        }

        private Stack<String> tagStack = new Stack<String>();
        private Stack<Container> containerStack = new Stack<Container>();

        private CharArrayWriter contents = new CharArrayWriter();
        private Types currentType = null;
        // this should be false when there are no contents to read
        private boolean currentContents = false;

        // Event Handlers
        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
            contents.reset();
            tagStack.push(localName);
            if (tagStack.size() > containerStack.size() + 1) {
                // add a new container to the stack, use the types info from the parent
                Container lastContainer = containerStack.peek();
                Object newContainerObject = makeContainerObject(currentType);
                String parentName = ( tagStack.size() > 1 ? tagStack.get(tagStack.size()-2) : tagStack.peek() );
                containerStack.push( new Container(lastContainer.getContainer(), parentName, newContainerObject) );
                add(lastContainer, parentName, newContainerObject);
            }
            currentType = getDataType(attributes);
            currentContents = false;
        }

        @Override
        public void endElement(String uri, String localName, String name) throws SAXException {
            if (tagStack.size() > containerStack.size()) {
                // only add data when we are above a container
                Object val = null;
                if (currentContents) {
                    String content = unescapeXML(contents.toString().trim());
                    val = content;
                    if (Types.BOOLEAN.equals(currentType)) {
                        val = Boolean.valueOf(content);
                    } else if (Types.NUMBER.equals(currentType)) {
                        try {
                            val = number(content);
                        } catch (NumberFormatException e) {
                            val = content;
                        }
                    } else if (Types.DATE.equals(currentType)) {
                        try {
                            val = new Date(Long.valueOf(content));
                        } catch (NumberFormatException e) {
                            val = content;
                        }
                    }
                }
                // put the value into the current container
                add(containerStack.peek(), localName, val);
            }
            if (tagStack.isEmpty()) {
                throw new IllegalStateException("tag stack is out of sync, empty while still processing tags: " + localName);
            } else {
                tagStack.pop();
            }
            // now we need to remove the current container if we are done with it
            while (tagStack.size() < containerStack.size()) {
                if (containerStack.size() <= 1) break;
                containerStack.pop();
            }
            contents.reset();
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            // get the text out of the element
            contents.write(ch, start, length);
            currentContents = true;
        }

        @Override
        public String toString() {
            return "parser: " + xml + " => " + map;
        }
    }

    public static String unescapeXML(String string) {
        return string.replace("&lt;","<").replace("&gt;", ">").replace("&quot;", "\"").replace("&amp;", "&");
    }

    private static enum Types {STRING,NUMBER,BOOLEAN,DATE,ARRAY,COLLECTION,MAP,BEAN}; 

    protected static Types getDataType(Attributes attributes) {
        Types elementType = Types.STRING;
        String value = attributes.getValue("", "type");
        if (value != null) {
            if ("boolean".equals(value)) {
                elementType = Types.BOOLEAN;
            } else if ("number".equals(value)) {
                elementType = Types.NUMBER;
            } else if ("date".equals(value)) {
                elementType = Types.DATE;
            } else if ("array".equals(value)) {
                elementType = Types.ARRAY;
            } else if ("collection".equals(value)) {
                elementType = Types.COLLECTION;
            } else if ("map".equals(value)) {
                elementType = Types.MAP;
            } else if ("bean".equals(value)) {
                elementType = Types.BEAN;
            }
        }
        return elementType;
    }

    protected static Class<?> getDataClass(Attributes attributes) {
        Class<?> type = String.class;
        String value = attributes.getValue("", "type");
        if (value != null) {
            if (value.startsWith("class ")) {
                value = value.substring(6);
            }
            // TODO handle the classes?
        }
        return type;
    }

    protected static Object makeContainerObject(Types type) {
        Object newContainer = null;
        if (Types.ARRAY.equals(type) 
                || Types.COLLECTION.equals(type)) {
            newContainer = new Vector<Object>();
        } else {
            // bean, map, unknown
            newContainer = new ArrayOrderedMap<String, Object>();
        }
        return newContainer;
    }

    /**
     * Converts a string into a number
     * @param s the string
     * @return the number
     * @throws NumberFormatException if the string is not a number
     */
    @SuppressWarnings("fallthrough")
    protected static Number number(String s) {
        int length = s.length();
        boolean isFloatingPoint = false;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
            case '.':
            case 'e':
            case 'E':
                isFloatingPoint = true;
            case '-':
            case '+':
                length--;
            }
        }

        // more friendly handling of numbers
        Number num = null;
        if (isFloatingPoint) {
            if (length < 10) {
                num = Float.valueOf(s);
            } else if (length < 17) {
                num = Double.valueOf(s);
            } else {
                num = new BigDecimal(s);
            }
        } else {
            if (length < 10) {
                num = Integer.valueOf(s);
            } else if (length < 19) {
                num = Long.valueOf(s);
            } else {
                num = new BigInteger(s);
            }
        }
        return num;
    }

    protected static class Container {
        private boolean root = false;
        public void setRoot(boolean root) {
            this.root = root;
        }
        public boolean isRoot() {
            return root;
        }
        public Object parent;
        public Object getParent() {
            return parent;
        }
        public String key;
        public String getKey() {
            return key;
        }
        public int index;
        public int getIndex() {
            return index;
        }
        public Object container;
        public Object getContainer() {
            return container;
        }
        public void updateIndex(int index) {
            if (index < 0) {
                throw new IllegalArgumentException("invalid index: " + index);
            }
            this.index = index;
            key = null;
        }
        /**
         * Replace the container with a new one based on the parent and settings in this Container
         */
        public void replaceContainer(Object container) {
            if (container == null) {
                throw new IllegalArgumentException("No null params allowed");
            }
            if (key != null) {
                FieldUtils.getInstance().setFieldValue(parent, key, container);
            } else if (index >= 0) {
                FieldUtils.getInstance().setIndexedValue(parent, index, container);
            }
            // if not key or index then do nothing except replacing the value
            this.container = container;
        }
        /**
         * Use if parent is non-existent (i.e. this is the root)
         */
        public Container(Object container) {
            if (container == null) {
                throw new IllegalArgumentException("No null params allowed");
            }
            this.container = container;
            this.root = true;
        }
        /**
         * Use if parent is keyed
         */
        public Container(Object parent, String key, Object container) {
            if (parent == null || key == null || container == null) {
                throw new IllegalArgumentException("No null params allowed");
            }
            this.container = container;
            this.key = key;
            this.parent = parent;
        }
        /**
         * Use if parent is indexed
         */
        public Container(Object parent, int index, Object container) {
            if (parent == null || index < 0 || container == null) {
                throw new IllegalArgumentException("No null params or index < 0 allowed");
            }
            this.container = container;
            this.index = index;
            this.parent = parent;
        }
        @Override
        public String toString() {
            return "C:root="+root+":parent="+(parent==null?parent:parent.getClass().getSimpleName())+":key="+key+":index="+index+":container="+(container==null?container:container.getClass().getSimpleName());
        }
    }

}