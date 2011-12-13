/*
 * Copyright 2011 mercatis Technologies AG
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mercatis.lighthouse3.commons.commons;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.w3c.dom.DOMException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The XML Muncher is a simple helper class providing simple convenience methods
 * for processing XML data.
 */
public class XmlMuncher {
	
	private static class SaxParserPoolFactory implements PoolableObjectFactory {
		
		private SAXParserFactory saxParserFactory;
		
		public SaxParserPoolFactory() {
			saxParserFactory = SAXParserFactory.newInstance();
			saxParserFactory.setNamespaceAware(true);
		}
		
		public boolean validateObject(Object arg0) {
			return true;
		}
		
		public void passivateObject(Object arg0) throws Exception {
		}
		
		public Object makeObject() throws Exception {
			return saxParserFactory.newSAXParser();
		}
		
		public void destroyObject(Object arg0) throws Exception {
		}
		
		public void activateObject(Object arg0) throws Exception {
			((SAXParser) arg0).reset();
		}
	}
	
	private static final GenericObjectPool pool = new GenericObjectPool(new SaxParserPoolFactory());
	
	static {
		pool.setMaxActive(-1);
		pool.setMinIdle(10);
		pool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_GROW);
	}

    /**
     * This separator is used to encode multiple element values in one XML
     * element.
     */
    public static final String VALUE_ENUMERATION_SEPARATOR = ";;;;";

    /**
     * This separator is used to encode intervalls of element values in one XML
     * element.
     */
    public static final String VALUE_INTERVAL_SEPARATOR = ";--;";

    /**
     * The mercatis LH3 namespace.
     */
    public static final String MERCATIS_NS = "http://www.mercatis.com/lighthouse3";

    /**
     * The XPath operator to address any element.
     */
    public final static String XPATH_ANY_ELEMENT = "*";

    /**
     * The XPath operator to address any direct child element.
     */
    public final static String XPATH_DIRECT_CHILD = "/:";

    /**
     * The XPath operator to address any indirect child element.
     */
    public final static String XPATH_INDIRECT_CHILD = "//:";

    /**
     * This method tokenizes a given XPath expression
     *
     * @param xpathExpression the expression to tokenize
     * @return the tokens, XPath operators and element names in a sequence.
     */
    private List<String> tokenizeXPathExpression(String xpathExpression) {
        List<String> xpathTokens = new LinkedList<String>();

        String currentToken = "";
        boolean currentTokenIsOperator = false;

        StringTokenizer splinters = new StringTokenizer(xpathExpression, "/:*", true);

        while (splinters.hasMoreTokens()) {
            String splinter = splinters.nextToken();

            if (currentTokenIsOperator) {
                if ("/".equals(splinter) || "*".equals(splinter) || ":".equals(splinter)) {
                    if (("*".equals(splinter) && !"".equals(currentToken)) || "*".equals(currentToken)) {
                        xpathTokens.add(currentToken);
                        currentToken = splinter;
                    } else {
                        currentToken += splinter;
                    }
                } else {
                    if (!"".equals(currentToken))
                        xpathTokens.add(currentToken);
                    currentTokenIsOperator = false;
                    currentToken = splinter;
                }
            } else {
                if ("/".equals(splinter) || "*".equals(splinter)) {
                    if (!"".equals(currentToken))
                        xpathTokens.add(currentToken);
                    currentTokenIsOperator = true;
                    currentToken = splinter;
                } else {
                    currentToken += splinter;
                }
            }
        }

        if (!currentToken.equals(""))
            xpathTokens.add(currentToken);

        return xpathTokens;
    }

    /**
     * This method translates an XPath expression into a regular expression that
     * matches on the path of an XML element iff the XPath expression would
     * match that element as well.
     * <p/>
     * Note that only simple expressions on elements using *, /:, and //: are
     * supported.
     *
     * @param xpathExpression the XPath expression to translate
     * @return the regular expression equivalent to the XPath expression.
     * @throws XPathExpressionException in case of trouble with the XPath expression.
     */
    private Pattern translateXPathExpressionToRegexp(String xpathExpression) throws XPathExpressionException {
        StringBuilder regularExpression = new StringBuilder("^");

        for (String token : this.tokenizeXPathExpression(xpathExpression)) {
            if (XPATH_DIRECT_CHILD.equals(token))
                regularExpression.append(Pattern.quote("/"));
            else if (XPATH_ANY_ELEMENT.equals(token))
                regularExpression.append("([A-Za-z0-9_:]+)");
            else if (XPATH_INDIRECT_CHILD.equals(token))
                regularExpression.append("(").append(Pattern.quote("/")).append("[A-Za-z0-9_:]*)+");
            else
                regularExpression.append("(").append(Pattern.quote(token)).append(")");
        }
        
        try {
            return Pattern.compile(regularExpression.toString());
        } catch (PatternSyntaxException e) {
            throw new XPathExpressionException(e);
        }
    }

    /**
     * This static property keeps a cache of already translated XPath
     * expressions.
     */
    static private Map<String, Pattern> xpathTranslationCache = Collections
            .synchronizedMap(new HashMap<String, Pattern>());

    /**
     * This method translates an XPath expression into a regular expression that
     * matches on the path of an XML element iff the XPath expression would
     * match that element as well.
     * <p/>
     * Note that only simple expressions on elements using *, /:, and //: are
     * supported.
     * <p/>
     * Expressions are cached
     *
     * @param xpathExpression the XPath expression to translate
     * @return the regular expression equivalent to the XPath expression.
     * @throws XPathExpressionException in case of trouble with the XPath expression.
     */
    public Pattern xpathExpressionToRegexp(String xpathExpression) throws XPathExpressionException {
        if (xpathTranslationCache.containsKey(xpathExpression))
            return xpathTranslationCache.get(xpathExpression);
        else {
            Pattern regularExpression = this.translateXPathExpressionToRegexp(xpathExpression);
            xpathTranslationCache.put(xpathExpression, regularExpression);
            return regularExpression;
        }
    }

    /**
     * This map keeps track of all element paths within the XML document being
     * munched plus the values of the elements at these paths.
     */
    private List<LinkedHashMap<String, String>> documentPaths = new LinkedList<LinkedHashMap<String, String>>();

    /**
     * This method adds a path to the document paths.
     *
     * @param documentPaths the document paths to add to
     * @param path          the path to add
     * @param value         the value the path points to
     */
    private void addDocumentPath(List<LinkedHashMap<String, String>> documentPaths, String path, String value) {

        if ((value != null) && documentPaths.get(documentPaths.size() - 1).containsKey(path) && (documentPaths.get(documentPaths.size() - 1).get(path) != null)) {
            documentPaths.add(new LinkedHashMap<String, String>());
        }

        if ((value != null) && documentPaths.get(documentPaths.size() - 1).containsKey(path) && (documentPaths.get(documentPaths.size() - 1).get(path) == null)) {
            documentPaths.get(documentPaths.size() - 1).remove(path);
        }

        if ((value == null) && documentPaths.get(documentPaths.size() - 1).containsKey(path)) {
            documentPaths.add(new LinkedHashMap<String, String>());
        }

        documentPaths.get(documentPaths.size() - 1).put(path, value);
    }

    /**
     * This property keeps the name of the root element.
     */
    private String rootElementName = null;

    private static final String EMPTY_STRING = "";
    
    /**
     * This method returns the value reached by the given XPath expression in
     * the XML document being munched.
     *
     * @param xpathExpression the XPath expression to evaluate.
     * @return the value reached by the expression. If nothing is reached,
     *         <code>null</code> is returned.
     * @throws XPathExpressionException in case of an error
     */
    public String readValueFromXml(String xpathExpression) {
        String result = null;

        try {
        	Pattern pattern = this.xpathExpressionToRegexp(xpathExpression);
            boolean valueFound = false;

            Iterator<LinkedHashMap<String, String>> documentPaths = this.documentPaths.iterator();

            while (documentPaths.hasNext() && !valueFound) {
            	Iterator<Entry<String, String>> currentPaths = documentPaths.next().entrySet().iterator();
                
                while (currentPaths.hasNext() && !valueFound) {
                    Entry<String, String> path = currentPaths.next();

                    if (pattern.matcher(path.getKey()).matches()) {
                        if (!EMPTY_STRING.equals(path.getValue()) && (path.getValue() != null)) {
                            result = path.getValue();
                            valueFound = true;
                        }
                    }
                }
            }

        } catch (XPathExpressionException e) {
            throw new DOMException((short) 0, "Could not evaluate XPathExpression: " + e.getMessage());
        }

        return result;
    }

    /**
     * This method returns the list of string values reached by the given XPath
     * expression in the XML document being munched.
     *
     * @param xpathExpression the XPath expression to evaluate.
     * @return the list of values reached by the expression.
     * @throws XPathExpressionException in case of an error
     */
    public List<String> readValuesFromXml(String xpathExpression) {
        List<String> result = new ArrayList<String>();

        try {

            Iterator<LinkedHashMap<String, String>> documentPaths = this.documentPaths.iterator();

            while (documentPaths.hasNext()) {
                Iterator<Entry<String, String>> currentPaths = documentPaths.next().entrySet().iterator();;

                while (currentPaths.hasNext()) {
                    Entry<String, String> path = currentPaths.next();

                    if (this.xpathExpressionToRegexp(xpathExpression).matcher(path.getKey()).matches()) {
                        if (!"".equals(path.getValue()) && (path.getValue() != null))
                            result.add(path.getValue());
                    }
                }
            }

        } catch (XPathExpressionException e) {
            throw new DOMException((short) 0, "Could not evaluate XPathExpression: " + e.getMessage());

        }

        return result;
    }

    /**
     * This method returns the local name of the root element of the document
     * being munched.
     *
     * @return the root element name. This may be <code>null</code> in case the
     *         present muncher was created by the
     *         <code>getSubMunchersForContext()</code> method or the XML
     *         document represented by the muncher is empty.
     */
    public String getRootElementName() {
        return this.rootElementName;
    }

    /**
     * This predicate indicates whether the XML document represented by the
     * given muncher is empty.
     *
     * @return <code>true</code> iff the document is empty.
     */
    public boolean isEmpty() {
        return (this.documentPaths.size() == 1) && (this.documentPaths.get(0).isEmpty());
    }

    /**
     * This core constructor creates an XmlMuncher on an empty document.
     */
    public XmlMuncher() {
        this.documentPaths = new ArrayList<LinkedHashMap<String, String>>();
        this.documentPaths.add(new LinkedHashMap<String, String>());
    }

    /**
     * The constructor, which sets up the XML muncher against an XML document
     *
     * @param xml the XML document to munch.
     * @throws DOMException an exception is thrown in case of an error during XML parsing
     */
    public XmlMuncher(String xml) throws DOMException {
    	SAXParser saxParser = null;
        try {
        	saxParser = (SAXParser) pool.borrowObject();
            saxParser.parse(new InputSource(new StringReader(xml)), new DefaultHandler() {

                private LinkedList<String> currentPathStack = new LinkedList<String>();
                private StringBuilder currentValue;

                @Override
                public void characters(char[] ch, int start, int length) throws SAXException {
                    if (this.currentValue != null)
                        this.currentValue.append(ch, start, length);
                }

                @Override
                public void startElement(String uri, String localName, String name, Attributes attributes)
                        throws SAXException {

                    this.currentValue = new StringBuilder();
                    String newPath = null;

                    if (this.currentPathStack.isEmpty()) {
                        newPath = "/" + localName;
                        rootElementName = localName;
                        documentPaths.add(new LinkedHashMap<String, String>());
                    } else {
                        newPath = this.currentPathStack.getFirst() + "/" + localName;
                    }

                    this.currentPathStack.add(0, newPath);

                    addDocumentPath(documentPaths, newPath, null);

                }

                @Override
                public void endElement(String uri, String localName, String name) throws SAXException {
                    String currentPath = this.currentPathStack.removeFirst();

                    if (currentValue != null && !"".equals(this.currentValue.toString()))
                        addDocumentPath(documentPaths, currentPath, this.currentValue.toString());

                    this.currentValue = null;
                }
            });

        } catch (ParserConfigurationException e) {
            throw new DOMException((short) 0, "SAX-Parser configuration exception caught while munching XML document: "
                    + e.getMessage());
        } catch (SAXException e) {
            throw new DOMException((short) 0, "SAX parsing exception caught while munching XML document: "
                    + e.getMessage());
        } catch (IOException e) {
            throw new DOMException((short) 0, "IO exception caught while munching XML document: " + e.getMessage());
        } catch (Exception e) {
        	throw new DOMException((short) 0, "exception caught while munching XML document: " + e.getMessage());
		} finally {
				try {
					pool.returnObject(saxParser);
				} catch (Exception e) {
					throw new DOMException((short) 0, "exception caught while munching XML document: " + e.getMessage());
				}
		}
    }

    /**
     * This method takes a given document path and localizes - i.e. truncates -
     * it to the suffix left over when matching the given XPath expression
     * against the path.
     *
     * @param xpathExpression the XPath expression translated to a regular expression
     * @param path            the path to localize
     * @return <code>null</code>, if the XPath expression does not match the
     *         given path, the localized path suffix otherwise. Note that
     *         <code>""</code> is returned as a suffix when the XPath expression
     *         fully matches the path.
     */
    private String localizePath(Pattern xpathExpression, String path) {
        String localizedPath = null;

        Matcher matcher = xpathExpression.matcher(path);
        if (matcher.find()) {
        	String replacement = "/" + matcher.group(matcher.groupCount());
        	localizedPath = matcher.replaceFirst(replacement);
        }

        return localizedPath;
    }

    /**
     * This method produces sub munchers out of the current XML muncher limited
     * to a certain context. This allows one to query repeating complex element
     * structures.
     *
     * @param xpathExpression the XPath expression defining the context.
     * @return a list of new munchers.
     * @throws XPathExpressionException in case the XPath expression is invalid.
     */
    public List<XmlMuncher> getSubMunchersForContext(String xpathExpression) throws DOMException {
        List<XmlMuncher> subMunchers = new ArrayList<XmlMuncher>();
        XmlMuncher currentMuncher = new XmlMuncher();

        Pattern translation = null;
        try {
            translation = this.translateXPathExpressionToRegexp(xpathExpression);
        } catch (XPathExpressionException e) {
            throw new DOMException((short) 0, "Could not evaluate XPathExpression: " + e.getMessage());
        }

        for (LinkedHashMap<String, String> currentPathBatch : this.documentPaths) {
            Iterator<Entry<String, String>> currentPaths = currentPathBatch.entrySet().iterator();;

            while (currentPaths.hasNext()) {
                Entry<String, String> currentPath = currentPaths.next();

                String localizedPath = this.localizePath(translation, currentPath.getKey());

                if (localizedPath != null && !localizedPath.substring(1).contains("/")) {
                    if (!currentMuncher.isEmpty()) {
                        subMunchers.add(currentMuncher);
                        currentMuncher = new XmlMuncher();
                    }
                } else if (localizedPath != null) {
                    currentMuncher.addDocumentPath(currentMuncher.documentPaths, localizedPath, currentPath.getValue());
                }
            }

        }

        if (!currentMuncher.isEmpty()) {
            subMunchers.add(currentMuncher);
        }

        return subMunchers;
    }

    /**
     * This static method returns the value reached by the given XPath
     * expression in the XML document being passed.
     *
     * @param xml             the XML document as a string
     * @param xpathExpression the XPath expression to evaluate.
     * @return the value reached by the expression. If nothing is reached,
     *         <code>null</code> is returned.
     * @throws XPathExpressionException in case of an error
     */
    public static String readValueFromXml(String xml, String xpathExpression) {
        return new XmlMuncher(xml).readValueFromXml(xpathExpression);
    }

    /**
     * This static method returns the list of string values reached by the given
     * XPath expression in the XML document being passed.
     *
     * @param xml             the XML document as a string
     * @param xpathExpression the XPath expression to evaluate.
     * @return the list of values reached by the expression.
     * @throws XPathExpressionException in case of an error
     */
    public static List<String> readValuesFromXml(String xml, String xpathExpression) {
        return new XmlMuncher(xml).readValuesFromXml(xpathExpression);
    }

    /**
     * This static method splits a string value up into its elements, using the
     * separator <code>VALUE_ENUMERATION_SEPARATOR</code>.
     *
     * @param value the value to split up.
     * @return the elements
     */
    public static List<String> getValueEnumerationElements(String value) {
        List<String> result = new ArrayList<String>();

        String[] values = value.split(VALUE_ENUMERATION_SEPARATOR);

        for (int i = 0; i < values.length; i++) {
            result.add(values[i]);
        }

        return result;
    }

    /**
     * This static method splits a string value up into its elements, using the
     * separator <code>VALUE_INTERVAL_SEPARATOR</code>.
     *
     * @param value the value to split up.
     * @return the elements
     */
    public static List<String> getValueIntervalElements(String value) {
        List<String> result = new ArrayList<String>();

        String[] values = value.split(VALUE_INTERVAL_SEPARATOR);

        for (int i = 0; i < values.length; i++) {
            result.add(values[i]);
        }

        return result;
    }

    /**
     * This static helper method converts a Java <code>date</code> into XML
     * Schema <code>dateTime</code> format.
     *
     * @param date the Java date to convert.
     * @return the date in XML Schema format.
     */
    public static String javaDateToXmlDateTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String xmlDateTime = sdf.format(date);
        xmlDateTime = xmlDateTime.substring(0, xmlDateTime.length() - 2) + ":"
                + xmlDateTime.substring(xmlDateTime.length() - 2, xmlDateTime.length());
        return xmlDateTime;
    }

    /**
     * This static helper method converts XML Schema <code>dateTime</code>
     * format into Java native <code>Date</code>.
     *
     * @param dateTime an XML Schema compliant date/time value
     * @return the equivalent Java date or <code>null</code> in case the passed
     *         value isn't an XML Schema date/time value.
     */
    public static Date xmlDateTimeToJavaDate(String dateTime) {
        String xmlDateTime = dateTime.substring(0, dateTime.length() - 3)
                + dateTime.substring(dateTime.length() - 2, dateTime.length());

        SimpleDateFormat sdf = null;
        if (dateTime.contains("."))
            sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        else
            sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        try {
            return sdf.parse(xmlDateTime);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * This static helper method converts a Java byte array into XML Schema
     * <code>binary</code> format.
     *
     * @param byteArray the byte array to convert
     * @return the byte array in XML Schema binary format
     */
    public static String byteArrayToXmlBinary(byte[] byteArray) {
        return new String(Base64.encodeBase64(byteArray));
    }

    /**
     * This static helper method converts XML Schema <code>binary</code> format
     * in a Java byte array.
     *
     * @param binary the XML Schema binary data to convert
     * @return the byte array or null if the binary format passed is not valid
     *         base64.
     */
    public static byte[] xmlBinaryToByteArray(String binary) {
        return new Base64().decode(binary.getBytes());
    }

    /**
     * This static helper method converts text data in XML Schema <code>binary</code> format
     * in a Java char array.
     *
     * @param binary the XML Schema binary data to convert
     * @return the char array or null if the binary format passed is not valid
     *         base64.
     */
    public static char[] xmlBinaryToCharArray(String binary) {
        byte[] bytes = new Base64().decode(binary.getBytes());

        return new String(bytes).toCharArray();
	}

    /**
     * This static helper method converts a Java char array into XML Schema
     * <code>binary</code> format.
     *
     * @param chars the char array to convert
     * @return the char array in XML Schema binary format
     */
    public static String charArrayToXmlBinary(char[] chars) {
        return byteArrayToXmlBinary(new String(chars).getBytes());
    }
}
