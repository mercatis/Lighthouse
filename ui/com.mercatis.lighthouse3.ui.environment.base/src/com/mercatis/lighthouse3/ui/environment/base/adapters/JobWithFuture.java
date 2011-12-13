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
package com.mercatis.lighthouse3.ui.environment.base.adapters;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.UIJob;


public abstract class JobWithFuture<V> extends Job {

	public JobWithFuture(String name) {
		super(name);
	}

	private final ReentrantLock lock = new ReentrantLock();
	
	private final Condition completeCondition = lock.newCondition();
	
	private boolean completed = false;
	
	private V future;
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		lock.lock();
		IStatus status = null;
		try {
			status = runWithFuture(monitor);
			completed = true;
			completeCondition.signal();
		} catch (Exception ex) {
			status = UIJob.errorStatus(ex);
		} finally {
			lock.unlock();
		}
		return status;
	}
	
	protected abstract IStatus runWithFuture(IProgressMonitor monitor);
	
	public V get() throws InterruptedException {
		lock.lock();
		try {
			while (!completed) {
				completeCondition.await();
			}
			return future;
		} finally {
			lock.unlock();
		}
	}
	
	protected void set(V result) {
		this.future = result;
	}

}
