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
package com.mercatis.lighthouse3.ui.environment.handlers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map.Entry;
import java.util.Properties;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import com.mercatis.lighthouse3.base.UIBase;
import com.mercatis.lighthouse3.base.ui.handlers.AbstractStructuredSelectionHandler;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;

/**
 * Reads in the project configuration and puts all variables into a xml-file.
 * 
 */
public class ExportDomainSettingsHandler extends AbstractStructuredSelectionHandler {

	/* (non-Javadoc)
	 * @see com.mercatis.lighthouse3.base.ui.handlers.AbstractStructuredSelectionHandler#execute(java.lang.Object)
	 */
	@Override
	protected void execute(Object element) throws ExecutionException {
		if (element instanceof LighthouseDomain) {
			String[] pluginIds = UIBase.getLighthousePluginIDs();
			
			LighthouseDomain lighthouseDomain = (LighthouseDomain) element;
			FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
			dialog.setFileName(lighthouseDomain.getProject().getName() + ".xml");
			dialog.setOverwrite(true);
			dialog.setText("Export project file");
			String filename = dialog.open();
			if (filename != null) {
				File file = new File(filename);
				try {
					OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
					writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
					writer.write("<LIGHTHOUSE_EXPORT domain=\"" + lighthouseDomain.getProject().getName() + "\">\n");
					for (String pluginId : pluginIds) {
						writePropertiesToXML(pluginId, lighthouseDomain, writer);
					}
					writer.write("</LIGHTHOUSE_EXPORT>\n");
					writer.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	private void writePropertiesToXML(String pluginId, LighthouseDomain lighthouseDomain, Writer writer) {
		try {
			writer.write("\t<PLUGIN id=\"" + pluginId + "\">\n");
			Properties properties = CommonBaseActivator.getPlugin().getDomainService().exportDomainConfiguration(lighthouseDomain, pluginId);
			for (Entry<Object, Object> entry : properties.entrySet()) {
				writer.write("\t\t<PROPERTY key=\"" + entry.getKey() + "\"");
				if (entry.getValue() != null && ((String)entry.getValue()).length() > 0) {
					String value = (String)entry.getValue();
					writer.write(">" + value + "</PROPERTY>\n");
				} else {
					writer.write(" />\n");
				}
			}
			writer.write("\t</PLUGIN>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
