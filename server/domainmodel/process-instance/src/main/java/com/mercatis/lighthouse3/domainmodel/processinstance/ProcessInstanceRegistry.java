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

import java.util.List;

import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;

public interface ProcessInstanceRegistry extends DomainModelEntityDAO<ProcessInstance> {

	public abstract List<ProcessInstance> findByProcessInstanceDefinition(ProcessInstanceDefinition processInstanceDefinition);
	
	public abstract List<ProcessInstance> findByProcessTask(ProcessTask processTask);
	
	public abstract List<ProcessInstance> findByProcessTask(ProcessTask processTask, int pageSize, int pageNo);

	public abstract List<ProcessInstance> findAfterInstance(ProcessInstance instance, int maxResults);

	public abstract List<ProcessInstance> findBeforeInstance(ProcessInstance instance, int maxResults);

}
