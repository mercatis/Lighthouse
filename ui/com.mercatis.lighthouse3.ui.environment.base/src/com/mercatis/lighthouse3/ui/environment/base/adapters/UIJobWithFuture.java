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

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;


public abstract class UIJobWithFuture<V> extends UIJob {

	private final ReentrantLock lock = new ReentrantLock();
	
	private final Condition completeCondition = lock.newCondition();
	
	private boolean completed = false;
	
	private V future;
	
	public UIJobWithFuture(String name) {
		super(name);
		this.setUser(true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {
		lock.lock();
		try {
			runWithFuture(monitor);
			completed = true;
			completeCondition.signal();
		} catch(InvocationTargetException ex) {
			return UIJob.errorStatus(ex);
		} catch(InterruptedException ex) {
			return UIJob.errorStatus(ex);
		} finally {
			lock.unlock();
		}
		return Status.OK_STATUS;
	}
	
	public abstract void runWithFuture(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException;

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
