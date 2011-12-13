package com.mercatis.lighthouse.ui.product.mercatis;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProductProvider;

public class LighthouseProductProvider implements IProductProvider {

	public String getName() {
		return "mercatis Lighthouse Product Provider";
	}

	public IProduct[] getProducts() {
		return new IProduct[] { new LighthouseProduct() };
	}

}
