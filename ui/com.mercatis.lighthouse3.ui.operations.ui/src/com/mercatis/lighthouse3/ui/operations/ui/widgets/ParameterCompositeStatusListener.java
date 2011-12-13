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
package com.mercatis.lighthouse3.ui.operations.ui.widgets;

import org.eclipse.swt.widgets.Composite;



public interface ParameterCompositeStatusListener {
	
	/**
	 * Will be fired on modification of a parameter.
	 * <br />If an input widget has a problem with a parameter value, you get an exception, otherwise null.
	 * 
	 * @param parameterComposite The composite that changed
	 * @param e Exception with message of a input widget that reports an error.
	 */
	public void violation(ParameterComposite parameterComposite, Exception e);
	
	/**
	 * Due to some strange behaviors of some swt containers...
	 * <br />Will be fired when controls are added/removed.
	 * 
	 * @param parent Composite where the ParameterComposite adds its controls
	 * @param inputWidgetContainer Composite that holds the widgets added/removed
	 */
	public void layoutChanged(Composite parent, Composite inputWidgetContainer);
}
