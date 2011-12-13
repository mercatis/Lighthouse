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
package com.mercatis.lighthouse3.base.ui.provider;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import com.mercatis.lighthouse3.base.UIBase;
import com.mercatis.lighthouse3.domainmodel.commons.CodedDomainModelEntity;

/**
 * Simple to use class, that makes common labeling for a specific entity homogeneous.
 */
public class LabelConverter {

	/**
	 * This extentionpoint is asked for converters
	 */
	private static final String CONVERTER_EXTENSION_POINT_ID = "com.mercatis.lighthouse3.base.ui.provider.labelconverter";

	/**
	 * Maps all convertes to the classname they are responsible for. 
	 */
	private static Map<String, ILabelConverterHelper> converters = new HashMap<String, ILabelConverterHelper>();

	/**
	 * Lookup the hepler for the given objects class and provide a label.
	 * If no converter is found, a RuntimeException will be generated.
	 * 
	 * @param obj
	 * @return label
	 */
	public static String getLabel(Object obj) {
		Assert.isLegal(obj != null);
			
		String label = null;
		if (converters.isEmpty()) {
			refresh();
		}
		
		ILabelConverterHelper converter = findConverter(obj.getClass());
		
		if (converter == null) {
			if (obj instanceof String) {
				return (String) obj;
			}
			
			throw new RuntimeException("No LabelConverter for '" + obj.getClass().getName() + "' available.");
		}
		
		label = converter.getLabelForObject(obj);
		if ((label == null || label.length() == 0) && obj instanceof CodedDomainModelEntity) {
			label = formatCode(((CodedDomainModelEntity) obj).getCode());
		}
		if (label == null || label.length() == 0)
			throw new RuntimeException("getLabel(" + (obj != null ? obj.getClass().getName() : "null")
					+ ") unsupported");
		return label;
	}

	/**
	 * This method provides a uniform way to format a code (like in CodedDomainModelEntity).
	 * 
	 * @param code
	 * @return
	 */
	public static String formatCode(String code) {
		return "[" + code + "]";
	}

	/**
	 * Clear the stored converters and reread the plugin.xml
	 */
	public static void refresh() {
		converters.clear();
		for (IConfigurationElement element : Platform.getExtensionRegistry().getConfigurationElementsFor(CONVERTER_EXTENSION_POINT_ID)) {
			try {
				String clazz = element.getAttribute("Object");
				ILabelConverterHelper lch = (ILabelConverterHelper) element.createExecutableExtension("Converter");
				converters.put(clazz, lch);
			} catch (CoreException e) {
				UIBase.getDefault().getLog().log(new Status(IStatus.ERROR, UIBase.PLUGIN_ID, e.getMessage(), e));
			} catch (InvalidRegistryObjectException e) {
				UIBase.getDefault().getLog().log(new Status(IStatus.ERROR, UIBase.PLUGIN_ID, e.getMessage(), e));
			}
		}
	}
	
	/**
	 * Lokkup the converter for the given class or one of its implemented interfaces or superclasses.
	 * 
	 * @param clazz
	 * @return the converter
	 */
	private static ILabelConverterHelper findConverter(Class<?> clazz) {
		if (clazz == null)
			return null;
		
		ILabelConverterHelper result = converters.get(clazz.getName());
		if (result == null) {
			Class<?>[] interfaces = clazz.getInterfaces();
			for (Class<?> iface : interfaces) {
				result = findConverter(iface);
				if (result != null)
					break;
			}
		}
		if (result == null) {
			Class<?> superclazz = clazz.getSuperclass();
			result = findConverter(superclazz);
		}
		
		return result;
	}
}
