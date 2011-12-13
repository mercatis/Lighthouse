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
/**
 * 
 */
package com.mercatis.lighthouse3.ui.operations.base.service.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.operations.Job;
import com.mercatis.lighthouse3.domainmodel.operations.JobRegistry;
import com.mercatis.lighthouse3.services.JobRegistryFactoryService;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.operations.base.OperationBase;
import com.mercatis.lighthouse3.ui.operations.base.service.JobService;


public class JobServiceImpl implements JobService, LighthouseDomainListener {

	private Map<LighthouseDomain, JobRegistry> registries;
	
	private BundleContext context;

	public JobServiceImpl(BundleContext context) {
		this.context = context;
		this.registries = new HashMap<LighthouseDomain, JobRegistry>();
	}
	
	private JobRegistry getRegistry(LighthouseDomain lighthouseDomain) {
		ServiceReference ref = context.getServiceReference(JobRegistryFactoryService.class.getName());
		if (ref != null) {
			JobRegistry registry = ((JobRegistryFactoryService) context.getService(ref)).getRegistry(lighthouseDomain.getProject());
			registries.put(lighthouseDomain, registry);
			return registry;
		}
		
		return null;
	}

	public void delete(Job job) {
		LighthouseDomain lighthouseDomain = getLighthouseDomainForJob(job);
		JobRegistry registry = getRegistry(lighthouseDomain);
		registry.delete(job);
		OperationBase.fireOperationsChanged(lighthouseDomain, job, null, null, null);
	}

	public Job findByCode(LighthouseDomain lighthouseDomain, String code) {
		JobRegistry registry = getRegistry(lighthouseDomain);
		return registry.findByCode(code);
	}
	
	public List<Job> findAtDeployment(Deployment deployment) {
		LighthouseDomain lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(deployment);
		JobRegistry registry = getRegistry(lighthouseDomain);
		List<Job> jobs = registry.findAtDeployment(deployment);
		Collections.sort(jobs, jobComparator);
		return jobs;
	}

	public void persist(Job job) {
		LighthouseDomain lighthouseDomain = getLighthouseDomainForJob(job);
		JobRegistry registry = getRegistry(lighthouseDomain);
		registry.persist(job);
		OperationBase.fireOperationsChanged(lighthouseDomain, job, null, null, null);
	}

	public void update(Job job) {
		LighthouseDomain lighthouseDomain = getLighthouseDomainForJob(job);
		JobRegistry registry = getRegistry(lighthouseDomain);
		registry.update(job);
		OperationBase.fireOperationsChanged(lighthouseDomain, job, null, null, null);
	}
	
	public LighthouseDomain getLighthouseDomainForJob(Job job) {
		return CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(job.getScheduledCall().getTarget().getInstallationLocation());
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener#closeDomain(com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain)
	 */
	public void closeDomain(LighthouseDomain domain) {
		registries.remove(domain);
	}

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.ui.environment.base.message.listener.LighthouseDomainListener#openDomain(com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain)
	 */
	public void openDomain(LighthouseDomain domain) {
	}
	
	private Comparator<Job> jobComparator = new Comparator<Job>() {
		public int compare(Job arg0, Job arg1) {
			return getDisplayNameForJob(arg0).compareTo(getDisplayNameForJob(arg1));
		}
	};
	
	private String getDisplayNameForJob(Job job) {
		return job.getLongName() != null && job.getLongName().length() > 0
				? job.getLongName()
				: job.getCode();
	}
}
