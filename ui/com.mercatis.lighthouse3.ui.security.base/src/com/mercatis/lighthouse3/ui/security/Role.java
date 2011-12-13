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
package com.mercatis.lighthouse3.ui.security;

import java.util.LinkedList;
import java.util.List;


public enum Role {

	DEPLOYMENT_DELETE("deleteDeployment"),
	DEPLOYMENT_INSTALL("installDeployment"),
	DEPLOYMENT_MODIFY("modifyDeployment"),
	DEPLOYMENT_UNINSTALL("uninstallDeployment"),
	DEPLOYMENT_VIEW("viewDeployment"),
	
	ENVIRONMENT_CREATE("createEnvironment"),
	ENVIRONMENT_DELETE("deleteEnvironment"),
	ENVIRONMENT_DRAG("dragEnvironment"),
	ENVIRONMENT_DROP("dropEnvironment"),
	ENVIRONMENT_MODIFY("modifyEnvironment"),
	ENVIRONMENT_VIEW("viewEnvironment"),
	
	JOB_EXECUTE("executeJob"),
	JOB_INSTALL("installJob"),
	JOB_MODIFY("modifyJob"),
	JOB_UNINSTALL("uninstallJob"),
	
	OPERATION_EXECUTE("executeOperation"),
	OPERATION_INSTALL("installOperation"),
	OPERATION_UNINSTALL("uninstallOperation"),
	
	PROCESS_TASK_CREATE("createProcessTask"),
	PROCESS_TASK_DELETE("deleteProcessTask"),
	PROCESS_TASK_DRAG("dragProcessTask"),
	PROCESS_TASK_DROP("dropProcessTask"),
	PROCESS_TASK_MODEL("modelProcessTask"),
	PROCESS_TASK_MODIFY("modifyProcessTask"),
	PROCESS_TASK_VIEW("viewProcessTask"),
	
	SOFTWARE_COMPONENT_CREATE("createSoftwareComponent"),
	SOFTWARE_COMPONENT_DELETE("deleteSoftwareComponent"),
	SOFTWARE_COMPONENT_DEPLOY("deploySoftwareComponent"),
	SOFTWARE_COMPONENT_DRAG("dragSoftwareComponent"),
	SOFTWARE_COMPONENT_DROP("dropSoftwareComponent"),
	SOFTWARE_COMPONENT_MODIFY("modifySoftwareComponent"),
	SOFTWARE_COMPONENT_VIEW("viewSoftwareComponent"),
	
	STATUS_CLEAR("clearStatus"),
	STATUS_CREATE("createStatus"),
	STATUS_DELETE("deleteStatus"),
	STATUS_MODIFY("modifyStatus"),
	STATUS_VIEW("viewStatus"),
	
	PERMISSIONS_MODIFY("modifyPermissions");

	private final String role;
	
	Role(String perm) {
		this.role = perm;
	}
	
	public String roleAsString() {
		return this.role;
	}
	
	public static List<Role> getViewRoles() {
		List<Role> viewRoles = new LinkedList<Role>();
		viewRoles.add(Role.DEPLOYMENT_VIEW);
		viewRoles.add(Role.ENVIRONMENT_VIEW);
		viewRoles.add(Role.SOFTWARE_COMPONENT_VIEW);
		viewRoles.add(Role.STATUS_VIEW);
		viewRoles.add(Role.PROCESS_TASK_VIEW);
		return viewRoles;
	}
	
}
