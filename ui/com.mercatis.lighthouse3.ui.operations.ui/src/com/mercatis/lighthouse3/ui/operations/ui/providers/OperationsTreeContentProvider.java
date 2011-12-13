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
package com.mercatis.lighthouse3.ui.operations.ui.providers;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.mercatis.lighthouse3.ui.operations.base.model.Category;


public class OperationsTreeContentProvider implements ITreeContentProvider {
	
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof List<?>) {
			return ((List<?>)parentElement).toArray();
		}
		else if (parentElement instanceof Category<?>) {
			return ((Category<?>)parentElement).getOperations().toArray();
		}
		return null;
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof List<?>) {
			return !((List<?>)element).isEmpty();
		}
		else if (element instanceof Category<?>) {
			return !((Category<?>)element).getOperations().isEmpty();
		}
		return false;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
