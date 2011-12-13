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
package com.mercatis.lighthouse3.ui.branding.mercatis;
 
import java.io.PrintWriter;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.ui.about.ISystemSummarySection;

public class ThirdpartyLibs implements ISystemSummarySection {
	
	private List<Lib> libs;
	Formatter form;

	public ThirdpartyLibs() {
		libs = new LinkedList<Lib>();
		createList();
	}

	public void write(PrintWriter writer) {
		form = new Formatter(writer);
		for (Lib lib : libs) {
			form.format("  %s from %s\n", lib.name, lib.origin);
		}
	}
	
	private class Lib{
		public String name;
		public String origin;
		
		public Lib(String name, String origin) {
			this.name = name;
			this.origin = origin;
		}
	}
	
	private void createList() {
		libs.add(new Lib("Apache ActiveMQ", "http://activemq.apache.org/"));
		libs.add(new Lib("Apache Commons codec", "http://commons.apache.org/codec/"));
		libs.add(new Lib("Apache Commons HTTP Client", "http://hc.apache.org/httpclient-3.x/"));
		libs.add(new Lib("Apache Commons Pool", "http://commons.apache.org/pool/"));
		libs.add(new Lib("Apache Velocity", "http://velocity.apache.org/"));
		libs.add(new Lib("Code Generation Library cglib", "http://cglib.sourceforge.net/"));
		libs.add(new Lib("FamFamFam Silk Icons", "http://www.famfamfam.com/"));
		libs.add(new Lib("SLF4J", "http://www.slf4j.org/"));
		libs.add(new Lib("xmlenc Library", "http://xmlenc.sourceforge.net/"));
		libs.add(new Lib("XmlWriter", "http://code.google.com/p/osjava/wiki/XmlWriter"));
	}
}
