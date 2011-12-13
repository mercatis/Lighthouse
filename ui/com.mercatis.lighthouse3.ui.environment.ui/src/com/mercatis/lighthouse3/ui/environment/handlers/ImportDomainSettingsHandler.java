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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.mercatis.lighthouse3.ui.environment.wizards.NewLighthouseDomainWizard;

/**
 * Read in a xml-file and try to prefill a DomainWizard with values.
 * 
 */
public class ImportDomainSettingsHandler extends AbstractHandler implements ContentHandler {

	private String projectName;
	private String currentPlugin;
	private String currentProperty;
	private Map<String, Map<String, String>> properties = new HashMap<String, Map<String, String>>();
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			File file = selectFile();
			if (file != null) {
				InputSource source = new InputSource(new FileInputStream(file));
				XMLReader reader = XMLReaderFactory.createXMLReader();
				reader.setContentHandler(this);
				reader.parse(source);
			}
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private File selectFile() {
		FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
		dialog.setText("Select file to import");
		String fileName = dialog.open();
		File file = null;
		if (fileName != null) {
			file = new File(fileName);
		}
		return file;
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#endDocument()
	 */
	public void endDocument() throws SAXException {
		NewLighthouseDomainWizard wizard = new NewLighthouseDomainWizard();

		WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(), wizard);
		wizard.setProperty("projectName", projectName);
		for (Map<String, String> values : properties.values()) {
			for (Entry<String, String> entry : values.entrySet()) {
				wizard.setProperty(entry.getKey(), entry.getValue());
			}
		}
		dialog.open();
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if (qName.equals("LIGHTHOUSE_EXPORT")) {
			projectName = atts.getValue(0);
		} else if (qName.equals("PLUGIN")) {
			currentPlugin = atts.getValue(0);
			properties.put(currentPlugin, new HashMap<String, String>());
		} else if (qName.equals("PROPERTY")) {
			currentProperty = atts.getValue(0);
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length) throws SAXException {
		StringBuilder builder = new StringBuilder(length);
		for (int i = start; i < start + length; i++) {
			builder.append(ch[i]);
		}
		String value = builder.toString().trim();
		if (value.length() > 0 && currentPlugin != null && currentProperty != null) {
			properties.get(currentPlugin).put(currentProperty, value);
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String qName) throws SAXException {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
	 */
	public void endPrefixMapping(String prefix) throws SAXException {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
	 */
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
	 */
	public void processingInstruction(String target, String data) throws SAXException {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
	 */
	public void setDocumentLocator(Locator locator) {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
	 */
	public void skippedEntity(String name) throws SAXException {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#startDocument()
	 */
	public void startDocument() throws SAXException {
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
	 */
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
	}
}
