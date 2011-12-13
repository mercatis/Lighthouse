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
package com.mercatis.lighthouse3.base.ui.widgets.eventfilter;

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;


public interface InputControlFactory {

	/**
	 * @param parent
	 * @param type
	 * @return
	 */
	public Control getControl(Composite parent, List<Listener> modifyListeners, InputControlType type);
	
	/**
	 * @param <T>
	 * @param parent
	 * @param type
	 * @param choices
	 * @return
	 */
	public <T> Control getControl(Composite parent, List<Listener> modifyListeners, InputControlType type, List<T> choices);
	
	/**
	 * @param <T>
	 * @param parent
	 * @param type
	 * @param choices
	 * @param selectedChoice
	 * @return
	 */
	public <T> Control getControl(Composite parent, List<Listener> modifyListeners, InputControlType type, List<T> choices, T selectedChoice);
	
	/**
	 * @param <T>
	 * @param <V>
	 * @param type
	 * @param control
	 * @return
	 */
	public <T,V> V getValue(InputControlType type, T control);
	
}
