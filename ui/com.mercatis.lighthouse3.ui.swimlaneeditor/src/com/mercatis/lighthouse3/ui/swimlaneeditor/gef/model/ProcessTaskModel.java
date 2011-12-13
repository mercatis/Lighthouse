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
package com.mercatis.lighthouse3.ui.swimlaneeditor.gef.model;

import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;

/**
 * Represents the model of one node (ProcessTask) in the SwimlaneEditor.
 * 
 */
public class ProcessTaskModel implements Comparable<ProcessTaskModel> {

	/**
	 * The wrapped ProcessTask
	 */
	private ProcessTask processTask;
	
	/**
	 * Used for layouting
	 */
	private int orderNumber;
	
	private boolean starter;
	private boolean stopper;

	public ProcessTaskModel(ProcessTask processTask) {
		this.setProcessTask(processTask);
	}

	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}

	public int getOrderNumber() {
		return this.orderNumber;
	}

	public void setProcessTask(ProcessTask processTask) {
		this.processTask = processTask;
	}

	public ProcessTask getProcessTask() {
		return processTask;
	}

	public int compareTo(ProcessTaskModel other) {
		return new Integer(other.orderNumber).compareTo(new Integer(this.orderNumber));
	}

	public void setStop(boolean stopper) {
		this.stopper = stopper;
	}

	public boolean isStopper() {
		return stopper;
	}

	public void setStarter(boolean starter) {
		this.starter = starter;
	}

	public boolean isStarter() {
		return starter;
	}
}
