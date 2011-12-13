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
package com.mercatis.lighthouse3.ui.environment.base.services.impl;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.UIJob;
import com.mercatis.lighthouse3.domainmodel.commons.DeferredEntityLoadRequest;
import com.mercatis.lighthouse3.domainmodel.commons.DomainModelEntity;
import com.mercatis.lighthouse3.domainmodel.commons.EntityLoadHandler;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.adapters.JobWithFuture;


public class FlexibleEntityLoadHandler implements EntityLoadHandler {

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.domainmodel.commons.EntityLoadHandler#performEntityLoad(com.mercatis.lighthouse3.domainmodel.commons.DeferredEntityLoadRequest)
	 */
	public DomainModelEntity performEntityLoad(final DeferredEntityLoadRequest loadRequest) {
		JobWithFuture<DomainModelEntity> job = new JobWithFuture<DomainModelEntity>("Remote Connection") {
			
			@Override
			public IStatus runWithFuture(IProgressMonitor monitor) {
				monitor.beginTask("Remote Connection", IProgressMonitor.UNKNOWN);
				try {
					set(loadRequest.doLoad());
					return Status.OK_STATUS;
				} catch (Exception ex) {
					CommonBaseActivator.getPlugin().getLog().log(new Status(IStatus.ERROR, CommonBaseActivator.PLUGIN_ID, ex.getMessage(), ex));
					return UIJob.errorStatus(ex);
				} finally {
					monitor.done();
				}
			}
			
		};
		job.setUser(true);
		job.setPriority(Job.INTERACTIVE);
		job.schedule();
		
		DomainModelEntity entity = null;
		try {
			entity = job.get();
		} catch (InterruptedException ex) {
			CommonBaseActivator.getPlugin().getLog().log(new Status(IStatus.ERROR, CommonBaseActivator.PLUGIN_ID, ex.getMessage(), ex));
		}
		
		return entity;
	}
	
}
