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
package com.mercatis.lighthouse3.domainmodel.commons;

import java.io.IOException;
import java.util.Map;

import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;

/**
 * This base class subsumes basis characteristics of persistent domain model
 * entity classes identified by unique codes.
 */
public abstract class CodedDomainModelEntity extends DomainModelEntity {

	private static final long serialVersionUID = 9221525396993939351L;

	/**
	 * The code string uniquely identifying the domain model entity amongst its
	 * class.
	 */
	private String code = null;

	/**
	 * Set the code string uniquely identifying the domain model entity amongst
	 * its class.
	 * 
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Sets the code string uniquely identifying the domain model entity amongst
	 * its class.
	 * 
	 * @param code
	 *            the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}
	
	/**
	 * This method returns a path identifier for the given coded domain model entity.
	 * It has a URL-like form: <code>entityClassInLowerCase://a/path</code>. For
	 * coded domain model entities the path is just the code of the entity.
	 * 
	 * @return the path for the entity.
	 */
	@Override
	public String getPath() {
		return this.getClass().getSimpleName().toLowerCase() + "://" + this.getCode();
	}

	@Override
	public void writeEntityReference(String referenceTagName, XmlWriter xml) throws IOException {
		String code = this.getCode();
		if (code != null) {
			xml.writeEntity(referenceTagName);
			xml.writeEntityWithText("code", code);
			xml.endEntity();
		}
	}

	@Override
	protected void fillRootElement(XmlWriter xml) throws IOException {
		super.fillRootElement(xml);

		if (this.getCode() != null)
			xml.writeEntityWithText("code", this.getCode());
	}

	@Override
	protected void readPropertiesFromXml(XmlMuncher xmlDocument) {
		super.readPropertiesFromXml(xmlDocument);

		this.setCode(xmlDocument.readValueFromXml("/*/:code"));

	}

	@Override
	public Map<String, String> toQueryParameters() {
		Map<String, String> parameters = super.toQueryParameters();

		if (this.getCode() != null)
			parameters.put("code", this.getCode());

		return parameters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!this.getClass().isInstance(obj))
			return false;

		if (this.getCode() == null)
			return false;

		CodedDomainModelEntity that = (CodedDomainModelEntity) obj;

		return this.getCode().equals(that.getCode());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.getCode() == null) ? 0 : this.getCode().hashCode());

		return result;
	}

}
