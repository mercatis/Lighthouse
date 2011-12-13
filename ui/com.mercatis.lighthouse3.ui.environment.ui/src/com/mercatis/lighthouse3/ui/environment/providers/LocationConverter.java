package com.mercatis.lighthouse3.ui.environment.providers;

import com.mercatis.lighthouse3.base.ui.provider.ILabelConverterHelper;
import com.mercatis.lighthouse3.ui.environment.base.model.Location;

public class LocationConverter implements ILabelConverterHelper{

	public String getLabelForObject(Object obj) {
		return ((Location)obj).getLabel();
	}

}
