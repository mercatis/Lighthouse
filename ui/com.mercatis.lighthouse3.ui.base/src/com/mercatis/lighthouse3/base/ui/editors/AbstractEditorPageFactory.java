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
package com.mercatis.lighthouse3.base.ui.editors;

import java.util.List;

import org.eclipse.ui.forms.editor.FormEditor;

/**
 * Implement a factory, to provide additional editor pages to an editor.
 * Each factory can either add pages to the end, or add pages at the beginning (displayed editor tabs).
 * 
 */
public abstract class AbstractEditorPageFactory implements Comparable<AbstractEditorPageFactory> {
	
	/**
	 * Position of the pages that have to be added to the editor.
	 */
	public static enum FactoryPosition {
		/**
		 * Pages are added to the start of the editors tablist..
		 */
		BEFORE_STATIC,
		
		/**
		 * Pages are added to the end of the editors tablist.
		 */
		AFTER_STATIC
	}
	
	/**
	 * When more than one factory is used to add pages at the same factory position, an ordernumber
	 * can be used to define which page factory goes first (or last)
	 */
	private int orderNumber;
	
	/**
	 * Implement this method to return the derivated instances of AbstractLighthouseEditorPage.
	 * If your editor page is not capable of derivating that class,
	 * it's possible to use the wrapping functionallity in the AbstractLighthouseEditorPage.
	 * 
	 * @param editor The editor instance that should add the pages.
	 * @return A list of pages to be added to the editor
	 */
	public abstract List<AbstractLighthouseEditorPage> getPages(FormEditor editor);
	
	/**
	 * There is a mixed mode possible in the editors so that not all
	 * pages are added dynamically.
	 * <br />The Editor will add pages in that order:
	 * <ol>
	 * <li>BEFORE_STATIC</li>
	 * <li>static pages</li>
	 * <li>AFTER_STATIC</li>
	 * </ol>
	 * @return
	 */
	public abstract FactoryPosition getFactoryPosition();

	public int compareTo(AbstractEditorPageFactory o) {
		return new Integer(orderNumber).compareTo(new Integer(o.orderNumber));
	}
	
	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}
}
