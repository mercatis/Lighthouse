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
package com.mercatis.lighthouse3.status.ui;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.mercatis.lighthouse3.domainmodel.environment.StatusCarrier;
import com.mercatis.lighthouse3.domainmodel.status.Status;
import com.mercatis.lighthouse3.domainmodel.status.StatusHistogram;
import com.mercatis.lighthouse3.services.Services;
import com.mercatis.lighthouse3.ui.common.base.CommonBaseActivator;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;

public class LighthouseStatusDecorator implements ILightweightLabelDecorator, EventHandler {

	public static final String id = "com.mercatis.lighthouse3.status.ui.decorator.statuslights";
	public static enum Phase {GREEN, ORANGE, RED, WHITE}
	public static enum Size {x8, x16}
	private static Map<Phase, Map<Size, ImageDescriptor>> descriptors;
	static {
		descriptors = new HashMap<Phase, Map<Size, ImageDescriptor>>();
		for (Phase phase : Phase.values()) {
			Map<Size, ImageDescriptor> s = new HashMap<Size, ImageDescriptor>();
			descriptors.put(phase, s);
			switch (phase) {
			case GREEN:
				s.put(Size.x8, ImageDescriptor.createFromURL(LighthouseStatusDecorator.class.getResource("/icons/green_x8.png")));
				s.put(Size.x16, ImageDescriptor.createFromURL(LighthouseStatusDecorator.class.getResource("/icons/green_x16.png")));
				break;
			case ORANGE:
				s.put(Size.x8, ImageDescriptor.createFromURL(LighthouseStatusDecorator.class.getResource("/icons/orange_x8.png")));
				s.put(Size.x16, ImageDescriptor.createFromURL(LighthouseStatusDecorator.class.getResource("/icons/orange_x16.png")));
				break;
			case RED:
				s.put(Size.x8, ImageDescriptor.createFromURL(LighthouseStatusDecorator.class.getResource("/icons/red_x8.png")));
				s.put(Size.x16, ImageDescriptor.createFromURL(LighthouseStatusDecorator.class.getResource("/icons/red_x16.png")));
				break;
			case WHITE:
				s.put(Size.x8, ImageDescriptor.createFromURL(LighthouseStatusDecorator.class.getResource("/icons/white_x8.png")));
				s.put(Size.x16, ImageDescriptor.createFromURL(LighthouseStatusDecorator.class.getResource("/icons/white_x16.png")));
				break;
			}
		}
	}
	
	public LighthouseStatusDecorator() {
		String filter = "(type=statusAggregationChanged)";
		Services.registerEventHandler(this, "com/mercatis/lighthouse3/event/*", filter);
	}
	
	public void decorate(Object element, IDecoration decoration) {
		LighthouseDomain lighthouseDomain = CommonBaseActivator.getPlugin().getDomainService().getLighthouseDomainByEntity(element);
		if (element instanceof StatusCarrier) {
			StatusHistogram sh = CommonBaseActivator.getPlugin().getStatusService().getStatusHistogramForObject(lighthouseDomain, element);
			if (sh.getError() > 0) {
				decoration.addOverlay(getImageDescriptorForStatus(Status.ERROR, Size.x8), IDecoration.BOTTOM_LEFT);
				decoration.addSuffix(" " + sh.getError() + " status erroneous");
			}
			else if (sh.getStale() > 0) {
				decoration.addOverlay(getImageDescriptorForStatus(Status.STALE, Size.x8), IDecoration.BOTTOM_LEFT);
				decoration.addSuffix(" " + sh.getStale() + " status stale");
			}
			else if (sh.getOk() > 0) {
				decoration.addOverlay(getImageDescriptorForStatus(Status.OK, Size.x8), IDecoration.BOTTOM_LEFT);
				decoration.addSuffix(" " + sh.getOk() + " status ok");
			}
			else if (sh.getNone() > 0) {
				decoration.addOverlay(getImageDescriptorForStatus(Status.NONE, Size.x8), IDecoration.BOTTOM_LEFT);
			}
		}
	}
	
	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
		Services.unregisterEventHandler(this);
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}
	
	public static ImageDescriptor getImageDescriptorForStatus(int status, Size size) {
		switch (status) {
		case Status.STALE:
			return getImageDescriptor(Phase.ORANGE, size);
		case Status.OK:
			return getImageDescriptor(Phase.GREEN, size);
		case Status.ERROR:
			return getImageDescriptor(Phase.RED, size);
		case Status.NONE:
		default:
			return getImageDescriptor(Phase.WHITE, size);
		}
	}
	
	public static ImageDescriptor getImageDescriptor(Phase phase, Size size) {
		return descriptors.get(phase).get(size);
	}

	/* (non-Javadoc)
	 * @see org.osgi.service.event.EventHandler#handleEvent(org.osgi.service.event.Event)
	 */
	public void handleEvent(Event event) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				PlatformUI.getWorkbench().getDecoratorManager().update(LighthouseStatusDecorator.id);
			}
		});
	}
}
