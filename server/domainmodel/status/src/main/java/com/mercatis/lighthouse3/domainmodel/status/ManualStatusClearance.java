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

import com.generationjava.io.xml.XmlWriter;

/**
 * This class captures manual status clearances in the status change history.
 * The new status resulting from such a status is always <code>NONE</code>.
 */
public class ManualStatusClearance extends StatusChange {
	/**
	 * This property contains optional information about the person who cleared
	 * the status.
	 */
	private String clearer = null;

	/**
	 * This methods returns optional information about the person who cleared
	 * the status.
	 * 
	 * @return the clearer information.
	 */
	public String getClearer() {
		return this.clearer;
	}

	/**
	 * The method can be used to set optional information about the person who
	 * cleared the status.
	 * 
	 * @param clearer
	 *            clearer information
	 */
	public void setClearer(String clearer) {
		this.clearer = clearer;
	}

	/**
	 * This property contains an optional explanation for the status change.
	 */
	private String reason = null;

	/**
	 * This method returns an optional explanation for the status change.
	 * 
	 * @return the explanation
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * This method can be used to set an optional explanation for the status
	 * change.
	 * 
	 * @param reason
	 *            the explanation
	 */
	public void setReason(String reason) {
		this.reason = reason;
	}

	@Override
	protected void fillStatusChangeElement(XmlWriter xml) throws IOException {
		super.fillStatusChangeElement(xml);
		if (this.getClearer() != null)
			xml.writeEntityWithText("clearer", this.getClearer());
		if (this.getReason() != null)
			xml.writeEntityWithText("reason", this.getReason());

	}

	/**
	 * The constructor for a manual status clearance.
	 * 
	 */
	public ManualStatusClearance() {
		this.setNewStatus(Status.NONE);
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		ManualStatusClearance clone = (ManualStatusClearance) super.clone();
		clone.clearer = this.clearer;
		clone.reason = this.reason;
		
		return clone;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((clearer == null) ? 0 : clearer.hashCode());
		result = prime * result + ((reason == null) ? 0 : reason.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ManualStatusClearance other = (ManualStatusClearance) obj;
		if (clearer == null) {
			if (other.clearer != null)
				return false;
		} else if (!clearer.equals(other.clearer))
			return false;
		if (reason == null) {
			if (other.reason != null)
				return false;
		} else if (!reason.equals(other.reason))
			return false;
		return true;
	}
}
