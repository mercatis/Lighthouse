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
package com.mercatis.lighthouse3.service.jobscheduler;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.mercatis.lighthouse3.domainmodel.operations.Job;
import com.mercatis.lighthouse3.domainmodel.operations.JobRegistry;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallationRegistry;

/**
 * This class provides an implementation for the quartz job interface that
 * executes LH3 jobs.
 */
public class QuartzJobImplementation implements org.quartz.Job {
	/**
	 * This property keeps a logger.
	 */
	protected Logger log = Logger.getLogger(this.getClass());

	public void execute(JobExecutionContext jobContext) throws JobExecutionException {
		JobScheduler jobScheduler = null;
		JobRegistry jobRegistry = null;
		OperationInstallationRegistry operationInstallationRegistry = null;
		
		try {
			jobScheduler = (JobScheduler) jobContext.getJobDetail().getJobDataMap().get("jobScheduler");
			if (jobScheduler == null) {
				log.error("Could not execute scheduled job: job scheduler not accessible");
				return;
			}
			
			jobRegistry = jobScheduler.getJobRegistry();
			if (jobRegistry == null) {
				log.error("Could not execute scheduled job: job registry not accessible");
				return;
			}
			
			operationInstallationRegistry = jobScheduler.getOperationInstallationRegistry();
			if (operationInstallationRegistry == null) {
				log.error("Could not execute scheduled job: operation installation registry not accessible");
				return;
			}
			
			String jobCode = (String) jobContext.getJobDetail().getJobDataMap().get("job");
			if (jobCode == null) {
				log.error("Could not execute scheduled job: job code not accessible");
				return;
			}
			
			Job  job = jobRegistry.findByCode(jobCode);
			if (job == null) {
				log.error("Could not execute scheduled job: job not accessible");
				return;
			}
			
			if (log.isDebugEnabled()) {
				log.debug("Executing scheduled LH3 job " + job.getCode());
			}
			
			operationInstallationRegistry.execute(job.getScheduledCall());
			
		} catch (Throwable ex) {
			log.error("Failed to execute scheduled LH3 job", ex);
		} finally {
			try {
				if (jobRegistry != null) {
					jobRegistry.getUnitOfWork().rollback();
				}
			} catch (Throwable ex) {
				log.error("Could not roll back unit of work", ex);
			}
		}
	}
}
