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
package com.mercatis.lighthouse3.ui.operations.ui.wizards.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.mercatis.lighthouse3.domainmodel.operations.Parameter;
import com.mercatis.lighthouse3.domainmodel.operations.ParameterValue;
import com.mercatis.lighthouse3.ui.operations.ui.widgets.ParameterComposite;
import com.mercatis.lighthouse3.ui.operations.ui.widgets.ParameterCompositeStatusListener;

/**
 * This WizardPage shows mandatory and optional parameters for an OperationCall. As a Job contains an OperationCall, this page is suitable for JobWizards as well
 * 
 */
public class OperationCallParameterPage extends WizardPage implements ParameterCompositeStatusListener {

	/**
	 * A Set of possible variants, harvested outside this page and passed via constructor.
	 * <br />If this Set is <code>null</code> or contains only one element, no variant chooser will be shown.
	 */
	private final Set<ParameterVariantKey> variants;
	
	/**
	 * A Map of mandatory parameters, grouped by variants
	 */
	private final Map<ParameterVariantKey, List<Parameter>> mandatoryParameterVariants;
	
	/**
	 * A Map of optional parameters, grouped by variants
	 */
	private final Map<ParameterVariantKey, List<Parameter>> optionalParameterVariants;
	
	/**
	 * The actual selected parameter variant. If there are multiple variants available, the constructor sets the first element as default.
	 * <br />If the selectedVariant is <code>null</code>, the default group will be shown (null variant). 
	 */
	private String selectedVariant;
	
	/**
	 * The WizardPage add controls to this Compopsite. We need it to reconstruct the pagelayout
	 * <br /><i>(eg. clear all elements from the page)</i>
	 */
	private Composite parameterContainer;
	
	/**
	 * A List of currently displayed ParameterComposites.
	 * <br />ParameterComposites deliver entered values. Clear this List when you rebuild the page.
	 */
	private List<ParameterComposite> parameterComposites = new LinkedList<ParameterComposite>();
	
	/**
	 * A ParameterComposite throws an exception if something is typed wrong <i>(eg. NumberFormatException)</i>.
	 * <br />Use this Map to display some userfeedback.
	 * <br />To complete this page, this Map must be empty.
	 */
	private Map<ParameterComposite, Exception> activeViolations = new HashMap<ParameterComposite, Exception>();
	
	/**
	 * Creates an instance of OperationCallParameterPage.
	 * <br />A variant chooser will be shown if you deliver more than one possible variant.
	 * 
	 * @param title The title of this WizardPage
	 * @param variants Possible variants, may be null
	 * @param mandatoryParameterVariants mandatory parameters grouped by variants
	 * @param optionalParameterVariants optional parameters grouped by variants
	 */
	public OperationCallParameterPage(String title, Set<ParameterVariantKey> variants, Map<ParameterVariantKey, List<Parameter>> mandatoryParameterVariants, Map<ParameterVariantKey, List<Parameter>> optionalParameterVariants) {
		super(title);
		setTitle(title);
		this.variants = variants;
		if (variants != null && !variants.isEmpty()) {
			selectedVariant = variants.iterator().next().getKey();
		} else {
			selectedVariant = null;
		}
		this.mandatoryParameterVariants = mandatoryParameterVariants;
		this.optionalParameterVariants = optionalParameterVariants;
		Assert.isNotNull(this.mandatoryParameterVariants, "");
		Assert.isNotNull(this.optionalParameterVariants, "");
	}

	public void layoutChanged(Composite parent, Composite inputWidgetContainer) {
		parent.getParent().setSize(parent.getParent().computeSize(SWT.DEFAULT, SWT.DEFAULT));
		parameterContainer.layout(true, true);
	}

	public void violation(ParameterComposite parameterComposite, Exception e) {
		if (e == null) {
			activeViolations.remove(parameterComposite);
		}
		else {
			activeViolations.put(parameterComposite, e);
		}
		if (activeViolations.isEmpty()) {
			setErrorMessage(null);
		}
		else {
			Exception current = activeViolations.get(parameterComposite);
			String message = null;
			if (current == null) {
				message = activeViolations.values().iterator().next().getMessage();
			}
			else {
				message = current.getMessage();
			}
			setErrorMessage(message.length() == 0 ? null : message);
		}
		setPageComplete(validatePage(false));
	}
	
	/**
	 * If no ParameterComposite reports an error, this WizardPage should be valid.
	 * @param silent When validating silent, no user feedback will be shown.
	 * @return <code>true</code> if this WizardPage is valid
	 */
	private boolean validatePage(boolean silent) {
		for (ParameterComposite composite : parameterComposites) {
			if (composite.getCurrentErrorStatus(silent) != null)
				return false;
		}
		return activeViolations.isEmpty();
	}

	public void createControl(Composite parent) {
		parameterContainer = new Composite(parent, SWT.NONE);
		parameterContainer.setLayout(new GridLayout(2, false));
		setControl(parameterContainer);
		placeControls();
	}
	
	/**
	 * Removes all controls from the parameterContainer, cleares parameterComposites and activeViolations and rebuilds the page.
	 */
	private void placeControls() {
		//Clearing
		parameterContainer.setLayoutDeferred(true);
		for (Control control : parameterContainer.getChildren()) {
			control.dispose();
		}
		parameterComposites.clear();
		activeViolations.clear();
		
		//Building
		
		//provide a combobox if more than one variant is possible
		if (variants.size() > 1) {
			Label categoryLabel = new Label(parameterContainer, SWT.NONE);
			categoryLabel.setText("Group:");
			categoryLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false));
			GridData dropdownData = new GridData(GridData.FILL, GridData.CENTER, true, false);
			final Combo variantChooser = new Combo(parameterContainer, SWT.DROP_DOWN | SWT.READ_ONLY);
			variantChooser.setLayoutData(dropdownData);
			for (ParameterVariantKey key : variants) {
				if (" ".equals(key.getKey())) //TODO use data fields in combo to provide unique keys
					throw new RuntimeException("This is a BadIdeaException. Don't use \" \" as key for variants!");
				if (key.getKey() == null) {
					variantChooser.add(" ");
				} else {
					variantChooser.add(key.getKey());
				}
			}
			if (selectedVariant != null) {
				variantChooser.select(variantChooser.indexOf(selectedVariant));
			}
			
			variantChooser.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}

				public void widgetSelected(SelectionEvent e) {
					selectedVariant = variantChooser.getText();
					if (" ".equals(selectedVariant)) {
						selectedVariant = null;
					}
					placeControls();
				}
			});
		}

		//prepare scrolling
		ScrolledComposite scroller = new ScrolledComposite(parameterContainer, SWT.V_SCROLL);
		GridData scrollerLayoutData = new GridData(GridData.FILL, GridData.BEGINNING, true, true);
		scrollerLayoutData.horizontalSpan = 2;
		scroller.setLayoutData(scrollerLayoutData);
		scroller.setExpandHorizontal(true);
		Composite parameterContainingComposite = new Composite(scroller, SWT.NONE);
		scroller.setContent(parameterContainingComposite);
		initializeDialogUnits(parameterContainer);
		GridLayout compositeLayout = new GridLayout(2, false);
		parameterContainingComposite.setLayout(compositeLayout);

		GridData mandatoryGroupLayoutData = new GridData(GridData.FILL, GridData.BEGINNING, true, false);
		mandatoryGroupLayoutData.horizontalSpan = 2;
		boolean anythingVisible = false;
		boolean isFirstDisplayedParameter = true;
		boolean groupVisible = false;
		List<Parameter> mandatoryParameters = mandatoryParameterVariants.get(new ParameterVariantKey(selectedVariant));
		if (mandatoryParameters != null && !mandatoryParameters.isEmpty()) {
			Group mandatoryWidgetGroup = new Group(parameterContainingComposite, SWT.NONE);
			mandatoryWidgetGroup.setText("Mandatory parameters");
			mandatoryWidgetGroup.setLayoutData(mandatoryGroupLayoutData);
			mandatoryWidgetGroup.setLayout(new GridLayout(2, false));
			for (Parameter parameter : mandatoryParameters) {
				anythingVisible = groupVisible = anythingVisible | placeparameter(mandatoryWidgetGroup, parameter, isFirstDisplayedParameter);
				isFirstDisplayedParameter = false;
			}
			if (!groupVisible)
				mandatoryWidgetGroup.dispose();
		}
		
		GridData optionalGroupLayoutData = new GridData(GridData.FILL, GridData.BEGINNING, true, false);
		optionalGroupLayoutData.horizontalSpan = 2;
		List<Parameter> optionalParameters = optionalParameterVariants.get(new ParameterVariantKey(selectedVariant));
		groupVisible = false;
		if (optionalParameters != null && !optionalParameters.isEmpty()) {
			Group optionalWidgetGroup = new Group(parameterContainingComposite, SWT.NONE);
			optionalWidgetGroup.setText("Optional parameters");
			optionalWidgetGroup.setLayoutData(optionalGroupLayoutData);
			optionalWidgetGroup.setLayout(new GridLayout(2, false));
			for (Parameter parameter : optionalParameters) {
				anythingVisible = groupVisible = anythingVisible | placeparameter(optionalWidgetGroup, parameter, isFirstDisplayedParameter);
				isFirstDisplayedParameter = false;
			}
			if (!groupVisible)
				optionalWidgetGroup.dispose();
		}
		
		if (anythingVisible)
			setDescription("Enter values for this parameters.");
		else
			setDescription("This operation group has no parameters.");
		
		//initial size computation, otherwise the composites will be very small in the scroller
		parameterContainingComposite.setSize(parameterContainingComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		parameterContainer.setLayoutDeferred(false);
		parameterContainer.layout(true);
		setPageComplete(validatePage(true));
		setErrorMessage(null);
		setMessage(null);
	}
	
	/**
	 * Adds a ParameterComposite <i>(two widgets)</i> to the parent.
	 * <br />The ParameterComposite will be created with the given parameter.
	 * 
	 * @param parent Parent composite where the ParameterComposite places its widgets.
	 * @param parameter The parameter to be displayed
	 * @param focus It <code>true</code> I'll try to focus the input field of the ParameterComposite
	 * @return true if the parameter is visible placed
	 */
	private boolean placeparameter(Composite parent, Parameter parameter, boolean focus) {
		List<ParameterValue> values = new ArrayList<ParameterValue>();
		if (parameter.hasDefaultValue()) {
			values.add(parameter.createValue(parameter.getDefaultValue()));
		}
		ParameterComposite pc = new ParameterComposite(parent, parameter, values);
		pc.addCompositeStatusListener(this);
		parameterComposites.add(pc);
		if (focus) {
			pc.setFocus();
		}
		return !parameter.isHidden();
	}
	
	/**
	 * Returns the values entered by an user.
	 * <br />Erroneous inputs will be converted to a <code>null</code> value.
	 * 
	 * @return A Map with the parameter as key and a list of values as value.
	 */
	public Map<Parameter, List<ParameterValue>> getValues() {
		Map<Parameter, List<ParameterValue>> values = new HashMap<Parameter, List<ParameterValue>>();
		for (ParameterComposite composite : parameterComposites) {
			if (composite.isEnabled())
				values.put(composite.getParameter(), composite.getValues());
		}
		return values;
	}
	
	/**
	 * This class is intended to wrap parameter varinants due to their ability to be <code>null</code>.
	 * <br />A Map can contain a key, instanciated like: <code>new ParameterVariantKey(null)</code>
	 */
	public static class ParameterVariantKey {
		
		private String key;
		
		/**
		 * Creates an instance of ParameterVariantKey
		 * 
		 * @param key The key may be null
		 */
		public ParameterVariantKey(String key) {
			this.key = key;
		}
		
		public String getKey() {
			return key;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj.getClass().equals(this.getClass())) {
				String otherKey = ((ParameterVariantKey)obj).key;
				if (otherKey == null && key == null)
					return true;
				if (otherKey == null && key != null)
					return false;
				if (otherKey != null && key == null)
					return false;
				return otherKey.equals(key);
			}
			return false;
		}

		@Override
		public int hashCode() {
			int hashCode = 17;
			if (key != null)
				hashCode *= key.hashCode();
			return hashCode;
		}
	}

	/**
	 * @return the selectedVariant
	 */
	public String getSelectedVariant() {
		return selectedVariant;
	}
}
