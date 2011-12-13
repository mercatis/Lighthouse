
package com.mercatis.lighthouse3.ui.propertytesters;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.TreeSelection;

import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;


public class IsDomainOpenPropertyTester extends PropertyTester {

	/**
	 * 
	 */
	public IsDomainOpenPropertyTester() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof TreeSelection) {
			Object element = ((TreeSelection)receiver).getFirstElement();
			if (element instanceof LighthouseDomain) {
				return ((LighthouseDomain)element).getProject().isOpen();
			}
		}
		return true;
	}

}
