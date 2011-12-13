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
package com.mercatis.lighthouse3.ui.operations.ui.wizards;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import com.mercatis.lighthouse3.base.ui.provider.LabelConverter;
import com.mercatis.lighthouse3.domainmodel.operations.Job;
import com.mercatis.lighthouse3.domainmodel.operations.Operation;
import com.mercatis.lighthouse3.domainmodel.operations.OperationCall;
import com.mercatis.lighthouse3.domainmodel.operations.OperationInstallation;
import com.mercatis.lighthouse3.domainmodel.operations.Parameter;
import com.mercatis.lighthouse3.domainmodel.operations.ParameterValue;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.operations.base.OperationBase;
import com.mercatis.lighthouse3.ui.operations.base.model.OperationInstallationWrapper;
import com.mercatis.lighthouse3.ui.operations.ui.OperationsUI;
import com.mercatis.lighthouse3.ui.operations.ui.wizards.pages.OperationCallParameterPage;
import com.mercatis.lighthouse3.ui.operations.ui.wizards.pages.OperationCallParameterPage.ParameterVariantKey;


public class OperationCallWizard extends Wizard implements INewWizard {

	private OperationCallParameterPage parameterPage;
	private OperationInstallation opertationInstallation;
	private Operation operation;
	private LighthouseDomain lighthouseDomain;

	public OperationCallWizard() {
		super();
		setWindowTitle("Execute Operation");
	}

	public OperationCallWizard(OperationInstallation operationInstallation) {
		this();
		initFromOperationInstallation(operationInstallation);
	}

	public OperationCallWizard(OperationInstallationWrapper operationInstallationLoader) {
		this(operationInstallationLoader.getOperationInstallation());
	}

	@Override
	public boolean canFinish() {
		if (parameterPage == null) {
			return false;
		}
		return parameterPage.isPageComplete();
	}

	@Override
	public void addPages() {
		List<Parameter> mandatoryParameters = new ArrayList<Parameter>();
		List<Parameter> optionalParameters = new ArrayList<Parameter>();
		for (Parameter parameter : this.operation.getParameters()) {
			if (!parameter.isOptional()) {
				mandatoryParameters.add(parameter);
			} else {
				optionalParameters.add(parameter);
			}
		}
		
		Set<ParameterVariantKey> variants = new HashSet<ParameterVariantKey>();
		Map<ParameterVariantKey, List<Parameter>> mandatoryParameterVariants = new HashMap<ParameterVariantKey, List<Parameter>>();
		Map<ParameterVariantKey, List<Parameter>> optionalParameterVariants = new HashMap<ParameterVariantKey, List<Parameter>>();
		for (Parameter parameter : operation.getParameters()) {
			ParameterVariantKey key = new ParameterVariantKey(parameter.getVariant());
			variants.add(key);
			List<Parameter> parameterVariant;
			if (parameter.isOptional()) {
				parameterVariant = optionalParameterVariants.get(key);
				if (parameterVariant == null) {
					parameterVariant = new LinkedList<Parameter>();
					optionalParameterVariants.put(key, parameterVariant);
				}
			} else {
				parameterVariant = mandatoryParameterVariants.get(key);
				if (parameterVariant == null) {
					parameterVariant = new LinkedList<Parameter>();
					mandatoryParameterVariants.put(key, parameterVariant);
				}
			}
			parameterVariant.add(parameter);
		}
		
		parameterPage = new OperationCallParameterPage(LabelConverter.getLabel(operation), variants, mandatoryParameterVariants, optionalParameterVariants);
		addPage(parameterPage);
	}

	@Override
	public void createPageControls(Composite pageContainer) {
		super.createPageControls(pageContainer);

		try {
			Field finishButton = getContainer().getClass().getDeclaredField("finishButton");
			finishButton.setAccessible(true);
			((Button) finishButton.get(getContainer())).setText("&Execute");
		} catch (Exception e) {
			OperationsUI.getPlugin().getLog().log(new Status(IStatus.ERROR, OperationsUI.PLUGIN_ID, e.getMessage(), e));
		}
	}

	@Override
	public boolean performFinish() {
		try {
			OperationCall call = new OperationCall(opertationInstallation);
			String variant = parameterPage.getSelectedVariant();
			if (variant != null && variant.length() > 0)
				call.setVariant(variant);
			Map<Parameter, List<ParameterValue>> values = parameterPage.getValues();
			for (Entry<Parameter, List<ParameterValue>> entry : values.entrySet()) {
				for (ParameterValue value : entry.getValue()) {
					call.addParameterValue(value);
				}
			}
			OperationBase.getOperationInstallationService().execute(lighthouseDomain, call);
			return true;
		} catch (Exception e) {
			new MessageDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Execution error", null, e
					.getMessage(), MessageDialog.ERROR, new String[] { "OK" }, 0).open();
			return false;
		}
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		if (selection.getFirstElement() instanceof OperationInstallation) {
			initFromOperationInstallation((OperationInstallation) selection.getFirstElement());
		} else if (selection.getFirstElement() instanceof Job) {
			initFromOperationInstallation(((Job) selection.getFirstElement()).getScheduledCall().getTarget());
		} else if (selection.getFirstElement() instanceof OperationInstallationWrapper) {
			initFromOperationInstallationWrapper((OperationInstallationWrapper) selection.getFirstElement());
		}
	}

	private void initFromOperationInstallation(OperationInstallation operationInstallation) {
		this.opertationInstallation = operationInstallation;
		lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(
				opertationInstallation.getInstallationLocation());
		operation = OperationBase.getOperationService().findInstalled(lighthouseDomain, opertationInstallation);
	}

	private void initFromOperationInstallationWrapper(OperationInstallationWrapper wrapper) {
		opertationInstallation = wrapper.getOperationInstallation();
		lighthouseDomain = wrapper.getLighthouseDomain();
		operation = wrapper.getOperation();
	}
}
