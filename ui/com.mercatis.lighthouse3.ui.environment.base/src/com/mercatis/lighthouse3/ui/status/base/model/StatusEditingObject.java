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
package com.mercatis.lighthouse3.ui.status.base.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import com.mercatis.lighthouse3.domainmodel.status.Status;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.status.base.service.StatusService;

/**
 * Instances of this class are used to wrap a status for editing, to keep the original object untouched.
 * 
 */
public class StatusEditingObject {
	private Status status;
	private String code, longName, description, contact, contactEmail;
	private int clearanceType;
	private long stalenessIntervalInMsecs;
	private boolean enabledForAggregation;

	public StatusEditingObject(Status status) {
		this.status = status;
		if (status == null)
			throw new RuntimeException("You can't init a StatusEditingObject with null status!");
		loadFromModel();
	}
	
	public boolean isDirty() {
		if (!code.equals(getNullcheckedString(status.getCode())))
			return true;
		if (!longName.equals(getNullcheckedString(status.getLongName())))
			return true;
		if (!description.equals(getNullcheckedString(status.getDescription())))
			return true;
		if (!contact.equals(getNullcheckedString(status.getContact())))
			return true;
		if (!contactEmail.equals(getNullcheckedString(status.getContactEmail())))
			return true;
		if (clearanceType != status.getClearanceType())
			return true;
		if (stalenessIntervalInMsecs != status.getStalenessIntervalInMsecs())
			return true;
		if (enabledForAggregation != status.isEnabled())
			return true;
		return false;
	}
	
	public void updateModel() {
		status.setCode(code);
		status.setLongName(longName);
		status.setDescription(description);
		status.setContact(contact);
		status.setContactEmail(contactEmail);
		status.setClearanceType(clearanceType);
		status.setStalenessIntervalInMsecs(stalenessIntervalInMsecs);
		status.setEnabled(enabledForAggregation);
	}

	public void loadFromModel() {
		code = getNullcheckedString(status.getCode());
		longName = getNullcheckedString(status.getLongName());
		description = getNullcheckedString(status.getDescription());
		contact = getNullcheckedString(status.getContact());
		contactEmail = getNullcheckedString(status.getContactEmail());
		clearanceType = status.getClearanceType();
		stalenessIntervalInMsecs = status.getStalenessIntervalInMsecs();
		enabledForAggregation = status.isEnabled();
	}

	private String getNullcheckedString(String string) {
		return string == null ? "" : string;
	}
	
	public String getCode() {
		return status.getCode();
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getLongName() {
		return longName;
	}
	
	public void setLongName(String longName) {
		this.longName = longName;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getContact() {
		return contact;
	}
	
	public void setContact(String contact) {
		this.contact = contact;
	}
	
	public String getContactEmail() {
		return contactEmail;
	}
	
	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}
	
	public int getClearanceType() {
		return clearanceType;
	}
	
	public void setClearanceType(int clearanceType) {
		this.clearanceType = clearanceType;
	}
	
	public long getStalenessIntervalInMsecs() {
		return stalenessIntervalInMsecs;
	}
	
	public void setStalenessIntervalInMsecs(long stalenessIntervalInMsecs) {
		this.stalenessIntervalInMsecs = stalenessIntervalInMsecs;
	}
	
	public boolean isEnabledForAggregation() {
		return this.enabledForAggregation;
	}
	
	public void setEnabledForAggregation(boolean enabled) {
		this.enabledForAggregation = enabled;
	}
	
	public Status getStatus() {
		return status;
	}

	public void refresh(LighthouseDomain lighthouseDomain) {
		StatusService service = CommonBaseActivator.getPlugin().getStatusService(); 
		int pageSize = service.getStatusConfiguration(lighthouseDomain).getStatusPageSize();
		int pageNo = service.getStatusConfiguration(lighthouseDomain).getStatusPageNo();
		this.status = service.refresh(getStatus(), pageSize, pageNo);
		loadFromModel();
	}
	
	public static String readTemplateIntoString(File template) {
		if (template == null)
			return "";
		StringBuffer stringBuffer = null;
		try {
			FileInputStream fileInputStream = new FileInputStream(template);
			stringBuffer = new StringBuffer(fileInputStream.available());
			byte[] buffer = new byte[fileInputStream.available()];
			int readcount;
			do {
				readcount = fileInputStream.read(buffer);
				if (readcount == -1)
					break;
				stringBuffer.append(new String(buffer, 0, readcount));
			} while (readcount != -1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stringBuffer != null ? stringBuffer.toString() : "";
	}
}
