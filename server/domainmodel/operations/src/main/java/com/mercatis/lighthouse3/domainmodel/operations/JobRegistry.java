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
package com.mercatis.lighthouse3.domainmodel.operations;

import java.util.List;

import com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntityDAO;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;

/**
 * This interface provides a registry of jobs.
 * 
 * The following rules apply with regard persistence:
 * 
 * <ul>
 * <li>a job is loaded completely including the operation call scheduled by it.
 * <li>the operation call is persisted with a job, including the operation
 * installation it refers to.
 * <li>when deleting an operation installation any jobs with operation calls
 * against this installation are deleted as well.
 * <li>when deleting an deployment all jobs with operation calls against
 * operations installed at this deployment vanish with it.
 * </ul>
 */
public interface JobRegistry extends CodedDomainModelEntityDAO<Job> {
	/**
	 * This method returns all jobs installed at a given deployment. I.e., those
	 * jobs, whose operation call targets an operation at a given deployment.
	 * 
	 * @return the list of all jobs installed at given deployment.
	 */
	public List<Job> findAtDeployment(Deployment deployment);
}
