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
package com.mercatis.lighthouse3.domainmodel.status;

import java.io.IOException;
import java.io.StringWriter;

import com.generationjava.io.xml.XmlEncXmlWriter;
import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;

/**
 * This class keeps a simple count of a bunch of status.
 */
public class StatusHistogram {
	/**
	 * Number of status in OK state
	 */
	private int ok = 0;

	/**
	 * Returns the number of status in <code>OK</code> state in the histogram
	 * 
	 * @return the number of status in <code>OK</code> state
	 */
	public int getOk() {
		return this.ok;
	}

	/**
	 * Sets the number of status in <code>OK</code> state in the histogram
	 * 
	 * @param ok
	 *            the number
	 */
	public void setOk(int ok) {
		this.ok = ok;
	}

	/**
	 * Increments the number of status in <code>OK</code> state in the histogram
	 */
	public void incOk() {
		this.ok++;
	}

	/**
	 * Number of status in ERROR state
	 */
	private int error = 0;

	/**
	 * Returns the number of status in <code>ERROR</code> state in the histogram
	 * 
	 * @return the number of status in <code>ERROR</code> state
	 */
	public int getError() {
		return this.error;
	}

	/**
	 * Sets the number of status in <code>ERROR</code> state in the histogram
	 * 
	 * @param error
	 *            the number
	 */
	public void setError(int error) {
		this.error = error;
	}

	/**
	 * Increments the number of status in <code>ERROR</code> state in the
	 * histogram
	 */
	public void incError() {
		this.error++;
	}

	/**
	 * Number of status in <code>STALE</code> state
	 */
	private int stale = 0;

	/**
	 * Returns the number of status in <code>STALE</code> state in the histogram
	 * 
	 * @return the number of status in <code>STALE</code> state
	 */
	public int getStale() {
		return this.stale;
	}

	/**
	 * Sets the number of status in <code>STALE</code> state in the histogram
	 * 
	 * @param stale
	 *            the number
	 */
	public void setStale(int stale) {
		this.stale = stale;
	}

	/**
	 * Increments the number of status in <code>STALE</code> state in the
	 * histogram
	 */
	public void incStale() {
		this.stale++;
	}

	/**
	 * Number of status in <code>NONE</code> state
	 */
	private int none = 0;

	/**
	 * Returns the number of status in <code>NONE</code> state in the histogram
	 * 
	 * @return the number of status in <code>NONE</code> state
	 */
	public int getNone() {
		return this.none;
	}

	/**
	 * Sets the number of status in <code>NONE</code> state in the histogram
	 * 
	 * @param none
	 *            the number
	 */
	public void setNone(int none) {
		this.none = none;
	}

	/**
	 * Increments the number of status in <code>NONE</code> state in the
	 * histogram
	 */
	public void incNone() {
		this.none++;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + error;
		result = prime * result + none;
		result = prime * result + ok;
		result = prime * result + stale;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StatusHistogram other = (StatusHistogram) obj;
		if (error != other.error)
			return false;
		if (none != other.none)
			return false;
		if (ok != other.ok)
			return false;
		if (stale != other.stale)
			return false;
		return true;
	}

	/**
	 * Returns an XML representation of the present status histogram.
	 *
	 * @return the XML representation.
	 */
        public String toXml() {
		StringWriter result = new StringWriter();
		XmlWriter xml = new XmlEncXmlWriter(result);

                toXml(xml);

		return result.toString();

        }

        /**
         * Writes the XML representation of the present status histogram to the XmlWriter
         * 
         * @param xml
         */
	public void toXml(XmlWriter xml) {
		try {
			xml.writeEntity("StatusHistogram");
			xml.writeAttribute("xmlns", XmlMuncher.MERCATIS_NS);

			xml.writeEntityWithText("ok", this.getOk());
			xml.writeEntityWithText("error", this.getError());
			xml.writeEntityWithText("stale", this.getStale());
			xml.writeEntityWithText("none", this.getNone());

			xml.endEntity();
		} catch (IOException e) {
		}
	}

	/**
	 * This method parses the XML representation of a status histogram into the
	 * present histogram.
	 * 
	 * @param xml
	 *            the XML representation of a status histogram
	 * @throw XMLSerializationException in case of a parsing error.
	 */
	public void fromXml(String xml) {
		XmlMuncher xmlMuncher = null;
		try {
			xmlMuncher = new XmlMuncher(xml);
		} catch (Exception e) {
			throw new XMLSerializationException("Could not parse status histogram", e);
		}

		if (!this.getClass().getSimpleName().equals(xmlMuncher.getRootElementName()))
			throw new XMLSerializationException("Invalid root element found for status histogram", null);

		try {

			this.setOk(Integer.parseInt(xmlMuncher.readValueFromXml("/*/:ok")));
			this.setError(Integer.parseInt(xmlMuncher.readValueFromXml("/*/:error")));
			this.setStale(Integer.parseInt(xmlMuncher.readValueFromXml("/*/:stale")));
			this.setNone(Integer.parseInt(xmlMuncher.readValueFromXml("/*/:none")));
		} catch (Exception e) {
			throw new XMLSerializationException("Could not parse status histogram", e);
		}
	}
}