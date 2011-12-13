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
package com.mercatis.lighthouse3.domainmodel.processinstance;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.generationjava.io.xml.XmlWriter;
import com.mercatis.lighthouse3.commons.commons.XmlMuncher;
import com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.commons.XMLSerializationException;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTaskRegistry;

public class ProcessInstanceDefinition extends CodedDomainModelEntity {

	private static final long serialVersionUID = -7067471084886067465L;
	
	private ProcessTask processTask;
	
	private Set<String> rules = new HashSet<String>();

	public ProcessTask getProcessTask() {
		return processTask;
	}

	public void setProcessTask(ProcessTask processTask) {
		this.processTask = processTask;
	}

	public Set<String> getRules() {
		return this.rules;
	}

	public void setRules(Set<String> rules) {
		this.rules = rules;
	}
	
	public void addRule(String rule) {
		this.rules.add(rule);
	}
	
	@Override
	protected void fillRootElement(XmlWriter xml) throws IOException {
		super.fillRootElement(xml);
		this.processTask.writeEntityReference("processTask", xml);
		
		// rules
		xml.writeEntity("rules");
		for (String rule : this.getRules()) {
			xml.writeEntityWithText("rule", rule);
		}
		xml.endEntity();
	}
	
	@Override
	protected void readPropertiesFromXml(XmlMuncher xml) {
		super.readPropertiesFromXml(xml);
		
		List<String> rules = xml.readValuesFromXml("/*/rules/rule");
		this.setRules(new HashSet<String>(rules));
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected void resolveEntityReferencesFromXml(XmlMuncher xml, DomainModelEntityDAO... resolversForEntityReferences) {
		if (resolversForEntityReferences.length == 0 || !resolversForEntityReferences[0].getManagedType().equals(ProcessTask.class)) {
			throw new XMLSerializationException(
					"XML deserialization of ProcessInstanceDefinition requires reference to a ProcessTaskRegistry as 1st resolverForEntityReferences.",
					null);
		}
		ProcessTaskRegistry processTaskRegistry = (ProcessTaskRegistry) resolversForEntityReferences[0];
		
		super.resolveEntityReferencesFromXml(xml, resolversForEntityReferences);
		
		String processTaskCode = xml.readValueFromXml("/*/:processTask/:code");
		ProcessTask processTask = processTaskRegistry.findByCode(processTaskCode);
		this.setProcessTask(processTask);
	}

	@Override
	public Map<String, String> toQueryParameters() {
		Map<String, String> parameters = super.toQueryParameters();

		if (this.getProcessTask() != null) {
			parameters.put("processTaskCode", this.getProcessTask().getCode());
		}
		
		return parameters;
	}
	
	public void fromQueryParameters(Map<String, String> queryParameters, ProcessTaskRegistry processTaskRegistry) {
		// resolve ProcessTask
		String processTaskCode = queryParameters.get("processTaskCode");
		if (processTaskCode != null) {
			this.setProcessTask(processTaskRegistry.findByCode(processTaskCode));
		}
		
		this.fromQueryParameters(queryParameters);
	}
	
}
