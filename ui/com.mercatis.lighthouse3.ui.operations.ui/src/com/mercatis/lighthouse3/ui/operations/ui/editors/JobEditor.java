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
package com.mercatis.lighthouse3.ui.operations.ui.editors;

import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import com.mercatis.lighthouse3.base.ui.editors.AbstractExtendableFormEditor;
import com.mercatis.lighthouse3.base.ui.editors.GenericEditorInput;
import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.domainmodel.operations.Job;
import com.mercatis.lighthouse3.domainmodel.operations.Operation;
import com.mercatis.lighthouse3.domainmodel.operations.OperationCall;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallation;
import com.mercatis.lighthouse3.domainmodel.operations.Parameter;
import com.mercatis.lighthouse3.domainmodel.operations.ParameterValue;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.operations.base.OperationBase;
import com.mercatis.lighthouse3.ui.operations.base.OperationsChangedListener;
import com.mercatis.lighthouse3.ui.operations.ui.OperationsUI;
import com.mercatis.lighthouse3.ui.operations.ui.editors.pages.JobEditorParameterPage;
import com.mercatis.lighthouse3.ui.operations.ui.editors.pages.JobEditorPropertiesPage;


public class JobEditor extends AbstractExtendableFormEditor implements OperationsChangedListener {
	
	public static final String ID = "com.mercatis.lighthouse3.ui.operations.editors.JobEditor";
	
	private JobEditorPropertiesPage cronPage;
	private JobEditorParameterPage parameterPage;

	private LighthouseDomain lighthouseDomain;
	private Job job;
	private OperationCall operationCall;
	private OperationInstallation operationInstallation;
	private Operation operation;

	@Override
	public String getFactoryExtensionPoint() {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		OperationBase.addOperationsChangedListener(this);

		job = ((GenericEditorInput<Job>) getEditorInput()).getEntity();
		operationCall = job.getScheduledCall();
		operationInstallation = operationCall.getTarget();
		lighthouseDomain = OperationBase.getJobService().getLighthouseDomainForJob(job);
		operation = OperationBase.getOperationService().findByCode(lighthouseDomain,
				operationInstallation.getInstalledOperationCode());
		Operation specializedOperation = OperationBase.getOperationService().findInstalled(lighthouseDomain, operationInstallation);
		if (specializedOperation != null)
			operation = specializedOperation;
		setPartName(LabelConverter.getLabel(job));
	}

	@Override
	public void dispose() {
		OperationBase.removeOperationsChangedListener(this);
		super.dispose();
	}

	@Override
	public void save() {
		cronPage.updateModel();
		if (parameterPage != null && parameterPage.isDirty()) {
			operationCall.getParameterValues().clear();
			for (Entry<Parameter, List<ParameterValue>> entry : parameterPage.getValues().entrySet()) {
				for (ParameterValue value : entry.getValue()) {
					operationCall.addParameterValue(value);
				}
			}
			parameterPage.markSave();
		}
		OperationBase.getJobService().update(job);
		editorDirtyStateChanged();
	}

	@Override
	protected void addPages() {
		try {
			cronPage = new JobEditorPropertiesPage(this, operation, operationCall);
			setPageText(addPage(cronPage), "Properties");
			
			if (operation.getParameters() != null && !operation.getParameters().isEmpty()) {
				parameterPage = new JobEditorParameterPage(this, job, operation, operationCall);
				setPageText(addPage(parameterPage), "Parameter");
			}
		} catch (PartInitException e) {
			OperationsUI.getPlugin().getLog().log(new Status(IStatus.ERROR, OperationsUI.PLUGIN_ID, e.getMessage(), e));
		}
	}

	public void operationsChanged(LighthouseDomain lighthouseDomain, Object source, String property, Object oldValue,
			Object newValue) {
		if (source != null && source.equals(job)) {
			setPartName(LabelConverter.getLabel(job));
		}
	}
}
