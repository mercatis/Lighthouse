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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import javax.jms.Queue;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import com.mercatis.lighthouse3.commons.messaging.JmsConnection;
import com.mercatis.lighthouse3.commons.messaging.JmsProvider;
import com.mercatis.lighthouse3.domainmodel.operations.Job;
import com.mercatis.lighthouse3.domainmodel.operations.JobRegistry;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallationRegistry;
import com.mercatis.lighthouse3.persistence.commons.hibernate.UnitOfWorkImplementation;
import com.mercatis.lighthouse3.persistence.operations.hibernate.OperationInstallationRegistryImplementation;
import com.mercatis.lighthouse3.service.commons.rest.DomainModelEntityResource;
import com.mercatis.lighthouse3.service.commons.rest.ResourceEventListener;
import com.mercatis.lighthouse3.service.commons.rest.ResourceEventTopicSubscriber;
import com.mercatis.lighthouse3.service.jobscheduler.rest.JobSchedulerServiceContainer;

/**
 * This class implements the job scheduler. It is responsible for scheduling all
 * jobs it is responsible for according to their schedule expression.
 */
public class JobScheduler implements ResourceEventListener {

    /**
     * This property keeps a logger.
     */
    protected Logger log = Logger.getLogger(getClass());
    /**
     * Keeps the number of the present service monitor instance, ranging from 1
     * to <code>totalInstances</code>.
     */
    private int instanceNumber = 1;
    /**
     * Keeps the total number of instances of the status monitor service.
     */
    private int totalInstances = 1;
    /**
     * The JMS provider to use to create a listener for job updates
     */
    private JmsProvider jmsProvider = null;
    /**
     * The JMS queue on which operation call execution requests are being
     * published.
     */
    private Queue queueOperationCalls = null;
    /**
     * The job update listener.
     */
    private ResourceEventTopicSubscriber jobUpdateListener = null;
    /**
     * The JMS connection for executing operation calls.
     */
    private JmsConnection operationCallExecutor = null;
    /**
     * The job registry to use
     */
    private JobRegistry jobRegistry = null;
    /**
     * The operation installation registry tied to the present job scheduler
     * service.
     */
    private OperationInstallationRegistryImplementation operationInstallationRegistry = null;

    /**
     * This method returns the operation installation registry tied to the
     * present job scheduler service.
     *
     * @return the operation installation registry.
     */
    public OperationInstallationRegistry getOperationInstallationRegistry() {
        return operationInstallationRegistry;
    }

	public JobRegistry getJobRegistry() {
		return jobRegistry;
	}

    /**
     * This predicate determines whether the present instance of the job
     * scheduler service is responsible for a job with a given ID.
     *
     * @param jobId
     *            the ID of the job for which responsibility is to be
     *            determined.
     * @return <code>true</code> iff the present job scheduler instance is
     *         responsible for scheduling a given job.
     */
    public boolean isResponsibleFor(long jobId) {
        if (jobId == 0) {
            return false;
        } else {
            return (jobId % totalInstances + 1) == instanceNumber;
        }
    }

    /**
     * This predicate determines whether the present instance of the job
     * scheduler service is responsible for a job with a given code.
     *
     * @param jobCode
     *            the code of the job for which responsibility is to be
     *            determined.
     * @return <code>true</code> iff the present job scheduler instance is
     *         responsible for scheduling a given job.
     */
    public boolean isResponsibleFor(String jobCode) {
        return schedulesJob(jobCode) || isResponsibleFor(getIdForJob(jobCode));
    }

    /**
     * This predicate determines whether the present instance of the job
     * scheduler service is responsible for a given job.
     *
     * @param job
     *            the job for which responsibility is to be determined.
     * @return <code>true</code> iff the present job scheduler instance is
     *         responsible for scheduling a given job.
     */
    public boolean isResponsibleFor(Job job) {
        if (job.getId() != 0) {
            return isResponsibleFor(job.getId());
        } else {
            return isResponsibleFor(job.getCode());
        }
    }

    /**
     * This method returns the ID of a job given its code.
     *
     * @param jobCode
     *            the code of the job to return the ID for.
     * @return the ID
     */
    private long getIdForJob(String jobCode) {
        Job job = null;
        
        try {
        	job = jobRegistry.findByCode(jobCode);
        } catch (Exception ex) {
        	if (!log.isDebugEnabled()) {
        		log.warn("Could not find job: " + jobCode);
        	} else {
        		log.warn("Could not find job: " + jobCode, ex);
        	}
        }

        if (job == null) {
            return 0;
        } else {
            return job.getId();
        }
    }
    /**
     * This property contains the quartz scheduler for jobs.
     */
    private Scheduler quartz = null;

    /**
     * This method returns the quartz job group name used for the jobs of the
     * present job scheduler.
     *
     * @return the job group name
     */
    private String jobGroupName() {
        return "LH3-JOBS-" + instanceNumber;
    }

    /**
     * This method checks whether a certain job is scheduled by the scheduler.
     *
     * @param jobCode
     *            the job code to check.
     * @return <code>true</code> in case the job is scheduled, otherwise false.
     */
    synchronized public boolean schedulesJob(String jobCode) {
        try {
            List<String> jobList = Arrays.asList(quartz.getJobNames(jobGroupName()));
            return jobList.contains(jobCode);
        } catch (SchedulerException e) {
            log.error("Could not check whether job is scheduled by scheduler", e);

            return false;
        }
    }

    /**
     * This method creates a quartz job detail for a given LH3 job.
     *
     * @param job
     *            the job to create a detail for
     * @return the job detail
     */
    private JobDetail createDetailForJob(Job job) {
        JobDetail jobDetail = new JobDetail(job.getCode(), jobGroupName(), QuartzJobImplementation.class);

        jobDetail.getJobDataMap().put("job", job.getCode());
        jobDetail.getJobDataMap().put("jobScheduler", this);

        return jobDetail;
    }

    /**
     * Start scheduling of a job with a given code.
     *
     * @param jobCode
     *            the code of the job to schedule
     */
    synchronized private void startSchedulingOfJob(String jobCode) {
        if (log.isDebugEnabled()) {
            log.debug("Starting scheduling of Job with code: " + jobCode);
        }

        Job job = jobRegistry.findByCode(jobCode);
        if (job == null) {
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Starting scheduling of Job: " + job.toXml());
        }

        try {
            quartz.scheduleJob(createDetailForJob(job), new CronTrigger(job.getCode() + "-TRIGGER",
                    "LH3-TRIGGERS", job.getScheduleExpression()));

            if (log.isDebugEnabled()) {
                log.debug("Quartz scheduling of Job succeeded: " + job.getCode());
            }
        } catch (SchedulerException e) {
        	if (e.getMessage().equals("Based on configured schedule, the given trigger will never fire.")) {
        		String msg = "Skipping scheduling of job "+jobCode+", the given trigger ("+job.getScheduleExpression()+") will never fire.";
        		log.warn(msg, null);
        		// debug message with stack trace, just to be sure
        		log.debug(msg, e);
        	} else {
        		log.error("Could not schedule job", e);
        	}
        } catch (ParseException e) {
            log.error("Could not schedule job", e);
        }
    }

    /**
     * Restart the scheduling of a job with a given code.
     *
     * @param jobCode
     *            the code of the job to reschedule
     */
    synchronized private void restartSchedulingOfJob(String jobCode) {
        stopSchedulingOfJob(jobCode);
        startSchedulingOfJob(jobCode);
    }

    /**
     * Stop scheduling of a job with a given code.
     *
     * @param jobCode
     *            the code of the job no longer to schedule
     */
    synchronized private void stopSchedulingOfJob(String jobCode) {
        if (log.isDebugEnabled()) {
            log.debug("Stopping scheduling of Job with code: " + jobCode);
        }
        try {
            quartz.deleteJob(jobCode, jobGroupName());
            if (log.isDebugEnabled()) {
                log.debug("Quartz descheduling of Job succeeded: " + jobCode);
            }
        } catch (SchedulerException e) {
            log.error("Could not stop scheduling of job", e);
        }
    }

    /**
     * This method stops the scheduling of all jobs.
     */
    synchronized public void stopSchedulingOfAllJobs() {
        try {
            quartz.shutdown();
        } catch (SchedulerException e) {
            log.error("Could not stop scheduling of jobs", e);
        }
    }

    public void entityCreated(DomainModelEntityResource<?> isNull, String jobCode) {
        if (isResponsibleFor(jobCode)) {
            startSchedulingOfJob(jobCode);
        }

        try {
            jobRegistry.getUnitOfWork().rollback();
        } catch (Exception e) {
            log.error("Could not rollback unit of work.", e);
        }
    }

    public void entityDeleted(DomainModelEntityResource<?> isNull, String jobCode) {
        if (isResponsibleFor(jobCode)) {
            stopSchedulingOfJob(jobCode);
        }

        try {
            jobRegistry.getUnitOfWork().rollback();
        } catch (Exception e) {
            log.error("Could not rollback unit of work.", e);
        }
    }

    public void entityUpdated(DomainModelEntityResource<?> isNull, String jobCode) {
        if (isResponsibleFor(jobCode)) {
            restartSchedulingOfJob(jobCode);
        }

        try {
            jobRegistry.getUnitOfWork().rollback();
        } catch (Exception e) {
            log.error("Could not rollback unit of work.", e);
        }
    }

    /**
     * This method loads all jobs from the database for which the present job
     * scheduler service is responsible.
     */
    @SuppressWarnings("unchecked")
    public void loadJobsFromDatabase() {
        if (jobRegistry == null) {
            return;
        }

        Session currentSession = ((UnitOfWorkImplementation) jobRegistry.getUnitOfWork()).getCurrentSession();

        List<String> jobCodesInDatabase = (List<String>) currentSession.createQuery(
                "select job.code from Job job where mod(job.id, :totalInstances) + 1 = :instanceNumber").setParameter(
                "totalInstances", totalInstances).setParameter("instanceNumber", instanceNumber).list();

        for (String jobCodeInDatabase : jobCodesInDatabase) {
            startSchedulingOfJob(jobCodeInDatabase);
        }

        jobRegistry.getUnitOfWork().rollback();
    }

    /**
     * This method sets up the quartz scheduler
     */
    private void setUpQuartzScheduler() {
        try {
            quartz = new StdSchedulerFactory().getScheduler();
            quartz.start();
        } catch (SchedulerException e) {
            log.error("Could not start quartz scheduler", e);
        }
    }

    /**
     * This method sets up the job update listener.
     */
    private void setUpOperationCallExecutor() {
        if (jmsProvider == null) {
            return;
        }

        String clientId = null;
        try {
            clientId = InetAddress.getLocalHost().getHostAddress() + "#service-job-scheduler-operation-caller#"
                    + System.currentTimeMillis();
            jmsProvider.setClientId(clientId);
        } catch (UnknownHostException e) {
        }
        jmsProvider.setClientId(clientId);

        operationCallExecutor = new JmsConnection(jmsProvider);

        operationInstallationRegistry.setOperationExecutionConnection(operationCallExecutor);
        operationInstallationRegistry.setOperationExecutionQueue(queueOperationCalls);
    }

    /**
     * This method stops the job scheduler closing all JMS connections et al.
     */
    public void stop() {
        stopSchedulingOfAllJobs();

        if (jobUpdateListener != null) {
            jobUpdateListener.stopJmsConnection();
        }

        if (operationCallExecutor != null) {
            operationCallExecutor.close();
        }
    }

    /**
     * This is the constructor of the job scheduler.
     *
     * @param jobSchedulerInstancesCurrent
     *            the number of the current instance of the job scheduler
     * @param jobSchedulerInstancesNumber
     *            the total number of instances of the job scheduler running
     * @param jobRegistry
     *            the job registry to use.
     * @param operationInstallationRegistry
     *            the operation installation registry to use.
     * @param jmsProvider
     *            the JMS provider to use to create a listener for job updates
     * @param queueOperationCalls
     *            the queue on which operation call execution requests are
     *            issued.
     */
    public JobScheduler(int jobSchedulerInstancesCurrent, int jobSchedulerInstancesNumber,
            JobSchedulerServiceContainer jobSchedulerServiceContainer, JobRegistry jobRegistry,
            OperationInstallationRegistry operationInstallationRegistry, JmsProvider jmsProvider,
            Queue queueOperationCalls) {

        if (log.isDebugEnabled())
            log.debug("Starting Job Scheduler, instance " + jobSchedulerInstancesCurrent + " of instances " + jobSchedulerInstancesNumber);
        
        instanceNumber = jobSchedulerInstancesCurrent;
        totalInstances = jobSchedulerInstancesNumber;
        this.jmsProvider = jmsProvider;
        this.queueOperationCalls = queueOperationCalls;
        this.jobRegistry = jobRegistry;
        this.operationInstallationRegistry = (OperationInstallationRegistryImplementation) operationInstallationRegistry;

        setUpOperationCallExecutor();
        setUpQuartzScheduler();

        loadJobsFromDatabase();

        if (jobSchedulerServiceContainer != null) {
            jobUpdateListener = new ResourceEventTopicSubscriber(Job.class, jobSchedulerServiceContainer, this, jobSchedulerServiceContainer.getConfiguration());
        }

    }
}
