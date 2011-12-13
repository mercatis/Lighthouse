/**
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
package com.mercatis.lighthouse3.domainmodel.events.aggregation;


import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.generationjava.io.xml.XmlEncXmlWriter;
import com.generationjava.io.xml.XmlWriter;

/**
 * This class holds an aggregation result.
 * It basically holds a list of <code>AggregationIntervalResult</code>.
 * Using the <code>Iterator</code> and <code>Iterable</code> interfaces,
 * it can be used in a for each loop.
 */
public class Aggregation implements Iterator<AggregationIntervalResult>, Iterable<AggregationIntervalResult>{

	/**
	 * <code>HashMap</code> representation of a list of <code>AggregationIntervalResults</code>.
	 */
	private HashMap<String, HashMap<String, Object>> intervalResult = new HashMap<String, HashMap<String, Object>>();
	
	/**
	 * Holds a list of keys for iterating through the intervalResult
	 */
	private ArrayList<String> keys;
	
	/**
	 * Holds the current index of the keys list for iterating.
	 */
	private int counter;
	
	/**
	 * Returns a single interval result by using a certain interval name.
	 * 
	 * @param interval
	 * 			  the name of the interval to be returned.
	 * @return the interval result as <code>AggregationIntervalResult</code>.
	 */
	public AggregationIntervalResult getIntervalResult(String interval) {
		if(intervalExists(interval))
			return new AggregationIntervalResult(interval, this.intervalResult.get(interval));
		else
			throw new AggregationException("The interval '" + interval + "' does not exist.", null);
	}
	
	/**
	 * Returns a multidimensional <code>HashMap</code> of all results
	 * 
	 * @return the results as a multidimensional <code>HashMap</code>
	 */
	public HashMap<String, HashMap<String, Object>> getResultAsMap() {
		return this.intervalResult;
	}
	
	/**
	 * Checks if a certain interval exists
	 * 
	 * @param interval
	 * 			  the interval name to be checked
	 * @return true if it exists, false otherwise
	 */
	public boolean intervalExists(String interval) {
		return this.intervalResult.containsKey(interval);
	}
	
	/**
	 * Adds an <code>AggregationIntervalResult</code> to 
	 * the list of interval results
	 * 
	 * @param result
	 * 			  the <code>AggregationIntervalResult</code> to be added
	 */
	public void add(AggregationIntervalResult result) {
		this.intervalResult.put(result.getIntervalName(), result.getGroupsAsMap());
	}
	
	/**
	 * Returns a list of all interval names.
	 * 
	 * @return a list of all interval names.
	 */
	public List<String> getIntervalOverview() {
		return new ArrayList<String>(this.intervalResult.keySet());
	}
	
	/**
	 * Initiates the iterator by saving all keys of the interval result 
	 * <code>HashMap</code>.
	 */
	private void initIterator() {
		this.keys = new ArrayList<String>(this.intervalResult.keySet());
		this.counter = 0;
	}

	/**
	 * Checks whether there is one more iteration step or not
	 * 
	 * @return true if there is one more step, false otherwise
	 */
	public boolean hasNext() {
		return counter < keys.size();
	}

	/**
	 * Returns the next <code>AggregationIntervalResult</code> in an iteration.
	 * 
	 * @return the next interval result.
	 */
	public AggregationIntervalResult next() {
		return new AggregationIntervalResult(this.keys.get(this.counter), this.intervalResult.get(this.keys.get(this.counter++)));
	}

	/**
	 * remove is not implemented
	 */
	public void remove() {
		throw new UnsupportedOperationException("remove() is not implemented.");
	}

	/**
	 * Returns the Iterator for iteration.
	 * Necessary for the use of for each loops.
	 * 
	 * @return the iterator for iteration
	 */
	public Iterator<AggregationIntervalResult> iterator() {
		initIterator();
		return this;
	}
	
	/**
	 * Converts the Aggregation into an XML representation.
	 * 
	 * @throws IOException
	 * @return the XML representation
	 */
	@SuppressWarnings("unchecked")
	public String toXML() throws IOException  {



		
		StringWriter result = new StringWriter();
		XmlWriter xml = new XmlEncXmlWriter(result);
		
		xml.writeEntity("aggregation");
		
		for(AggregationIntervalResult intervalResult : this) {
			xml.writeEntity("interval");
			xml.writeAttribute("name", intervalResult.getIntervalName());
			
			Map<String, Object> map = intervalResult.getGroupsAsMap();
			List<String> keys = intervalResult.getGroupOverview();
			for(String key : keys) {
				xml.writeEntity("group");
				xml.writeAttribute("name", key);
				
				if(map.get(key) instanceof HashMap) {
					HashMap<String, Integer> resultMap = (HashMap<String, Integer>)map.get(key);
					Set<String> keySet = resultMap.keySet();
					
					xml.writeEntity("resultMap");
					
					for(String identification : keySet) {
						xml.writeEntityWithText("identifier", identification);
						xml.writeEntityWithText("value", resultMap.get(identification));
					}
					
					xml.endEntity();
					
				} else if(map.get(key) instanceof Date) {
					Date date = (Date)map.get(key);
					xml.writeEntityWithText("resultDate", date.getTime());
					
				} else if(map.get(key) instanceof Integer) {
					int integer = Integer.parseInt(String.valueOf(map.get(key)));
					xml.writeEntityWithText("resultInteger", integer);
					
				} else if(map.get(key) instanceof Long) {
					long resultLong = Long.parseLong(String.valueOf(map.get(key)));
					xml.writeEntityWithText("resultLong", resultLong);
					
				} else if(map.get(key) instanceof Double) {
					double resultDouble = Double.parseDouble(String.valueOf(map.get(key)));
					xml.writeEntityWithText("resultDouble", resultDouble);
					
				} else if(map.get(key) instanceof Float) {
					float resultFloat = Float.parseFloat(String.valueOf(map.get(key)));
					xml.writeEntityWithText("resultFloat", resultFloat);
					
				}
				
				xml.endEntity();
			}
			
			xml.endEntity();
		}
		
		xml.endEntity();
		
		String xmlResult = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + result.toString();
		
		return xmlResult;
	}
	
	/**
	 * Fills the attributes of an Aggregation with the content
	 * of an XML representation.
	 * 
	 * @param xml
	 * 			  the XML that holds the Aggregation information
	 */
	public void fromXML(String xml) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        InputSource is = new InputSource();
	        is.setCharacterStream(new StringReader(xml));
	        Document doc = db.parse(is);
	        
	        this.intervalResult = new HashMap<String,HashMap<String,Object>>();
	        
	        NodeList nodes = doc.getElementsByTagName("interval");
	        
	        for(int x = 0; x < nodes.getLength(); x++) {
	        	Element element = (Element)nodes.item(x);
	        	
	        	String intervalName = element.getAttribute("name");
	        	HashMap<String, Object> resultMap = new HashMap<String, Object>();	        	
	        	
	        	NodeList groups = element.getChildNodes();
	        	for(int y = 0; y < groups.getLength(); y++) {
	        		Node groupNode = groups.item(y);
	        		
	        		if(groupNode != null && groupNode.getNodeType() == Node.ELEMENT_NODE && groupNode.getNodeName().equals("group"))
	        		{
	        		String groupName = groupNode.getAttributes().item(0).getNodeValue();
	        		
	        		Element firstChild = (Element)groupNode.getFirstChild();
	        		
	        		if(firstChild.getNodeName().equals("resultInteger")) {
	        			int resultInteger = Integer.parseInt(firstChild.getTextContent());
	        			resultMap.put(groupName, resultInteger);
	        			
	        		} else if(firstChild.getNodeName().equals("resultLong")) {
	        			long resultLong = Long.parseLong(firstChild.getTextContent());
	        			resultMap.put(groupName, resultLong);
	        			
	        		} else if(firstChild.getNodeName().equals("resultDouble")) {
	        			double resultDouble = Double.parseDouble(firstChild.getTextContent());
	        			resultMap.put(groupName, resultDouble);
	        			
	        		} else if(firstChild.getNodeName().equals("resultFloat")) {
	        			float resultFloat = Float.parseFloat(firstChild.getTextContent());
	        			resultMap.put(groupName, resultFloat);
	        			
	        		} else if(firstChild.getNodeName().equals("resultDate")) {
	        			long resultDate = Long.parseLong(firstChild.getTextContent());
	        			resultMap.put(groupName, new Date(resultDate));
	        			
	        		} else if(firstChild.getNodeName().equals("resultMap")) {
	        			HashMap<String, Integer> countResult = new HashMap<String, Integer>();
	        			
	        			NodeList list = firstChild.getChildNodes();
	        			
        				Node count = list.item(0);
        				boolean loop = true;
        				while(loop)
	        				if(count != null && count.getNodeType() == Node.ELEMENT_NODE)
	    	        		{
		        				String identifier = count.getTextContent();
		        				count = count.getNextSibling();
		        				int countInteger = Integer.parseInt(count.getTextContent());
		        				countResult.put(identifier, countInteger);
		        				if(count.getNextSibling() == null)
		        					break;
		        				else
		        					count = count.getNextSibling();
	    	        		}
        				
	        				resultMap.put(groupName, countResult);
	        			}
	        		}
	        	}
	        	
	        	this.add(new AggregationIntervalResult(intervalName, resultMap));
	        }	        
	        
		} catch(Exception e) {
			throw new AggregationException("Received result xml is not readable. Reason: " + e.getMessage(), null);
		}
	}
}
