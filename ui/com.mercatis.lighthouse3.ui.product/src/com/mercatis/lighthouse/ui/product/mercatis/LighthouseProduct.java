package com.mercatis.lighthouse.ui.product.mercatis;

import org.eclipse.core.runtime.IProduct;
import org.osgi.framework.Bundle;

public class LighthouseProduct implements IProduct {

	public String getApplication() {
		return "com.mercatis.lighthouse3.ui.application.LighthouseApplication";
	}

	public String getName() {
		return "Lighthouse";
	}

	public String getDescription() {
		return "description";
	}

	public String getId() {
		return "com.mercatis.lighthouse.ui.product.mercatis.LighthouseProduct";
	}

	public String getProperty(String key) {
		if (key.equals("appName"))
			return "Lighthouse";
		
		return null;
	}

	public Bundle getDefiningBundle() {
		return null;
	}

}
