package com.mercatis.lighthouse3.ui.common.base;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import com.mercatis.lighthouse3.domainmodel.environment.Deployment;
import com.mercatis.lighthouse3.domainmodel.environment.Environment;
import com.mercatis.lighthouse3.domainmodel.environment.ProcessTask;
import com.mercatis.lighthouse3.domainmodel.environment.SoftwareComponent;
import com.mercatis.lighthouse3.ui.environment.base.adapters.impl.DomainBoundEntityAdapterFactory;
import com.mercatis.lighthouse3.ui.environment.base.adapters.impl.EnvironmentContextAdapterFactory;
import com.mercatis.lighthouse3.ui.environment.base.adapters.impl.HierarchicalEntityAdapterFactory;
import com.mercatis.lighthouse3.ui.environment.base.message.center.ILighthouseDomainBroadCaster;
import com.mercatis.lighthouse3.ui.environment.base.message.center.impl.LighthouseDomainBroadCasterImpl;
import com.mercatis.lighthouse3.ui.environment.base.model.DeploymentContainer;
import com.mercatis.lighthouse3.ui.environment.base.model.EnvironmentContainer;
import com.mercatis.lighthouse3.ui.environment.base.model.LighthouseDomain;
import com.mercatis.lighthouse3.ui.environment.base.model.Location;
import com.mercatis.lighthouse3.ui.environment.base.model.ProcessTaskContainer;
import com.mercatis.lighthouse3.ui.environment.base.model.SoftwareComponentContainer;
import com.mercatis.lighthouse3.ui.environment.base.services.DomainService;
import com.mercatis.lighthouse3.ui.environment.base.services.LighthouseNatureService;
import com.mercatis.lighthouse3.ui.environment.base.services.impl.DomainServiceImpl;
import com.mercatis.lighthouse3.ui.environment.base.services.impl.LighthouseNatureServiceImpl;
import com.mercatis.lighthouse3.ui.event.base.services.EventService;
import com.mercatis.lighthouse3.ui.event.base.services.impl.EventServiceImpl;
import com.mercatis.lighthouse3.ui.status.base.adapters.impl.StatusContextAdapterFactory;
import com.mercatis.lighthouse3.ui.status.base.model.StatusEditingObject;
import com.mercatis.lighthouse3.ui.status.base.service.StatusService;
import com.mercatis.lighthouse3.ui.status.base.service.impl.StatusServiceImpl;

public class CommonBaseActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.mercatis.lighthouse3.ui.common.base";
	public static final String OLD_STATUS_PLUGIN_ID = "com.mercatis.lighthouse3.ui.status.base";
	public static final String OLD_EVENT_PLUGIN_ID = "com.mercatis.lighthouse3.ui.event.base";
	public static final String OLD_ENVIRONMENT_PLUGIN_ID = "com.mercatis.lighthouse3.ui.environment.base";

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static CommonBaseActivator getPlugin() {
		return plugin;
	}

	private File defaultTemplateFolder = null;

	// The shared instance
	private static CommonBaseActivator plugin;
	private StatusService statusService;

	private EventService eventService;
	private StatusContextAdapterFactory statusContextAdapterFactory;
	private EnvironmentContextAdapterFactory environmentContextAdapterFactory;
	
	private static DomainService domainService;
	
	private static LighthouseNatureService lighthouseNatureService;
	
	private ILighthouseDomainBroadCaster lighthouseDomainBroadcaster;
	
	private HierarchicalEntityAdapterFactory environmentAdapterFactory;
		
	private DomainBoundEntityAdapterFactory domainBoundEntityAdapterFactory;

	/**
	 * The constructor
	 */
	public CommonBaseActivator() {
	}

	private void copyTemplates() {
		String[] fileNames = { "templates/notification_head.vm", "templates/notification_body.vm" };
		for (String fileName : fileNames) {
			try {
				InputStream is = getClass().getResourceAsStream(fileName);
				FileOutputStream fos = new FileOutputStream(defaultTemplateFolder.getAbsolutePath() + File.separator
						+ fileName.substring(fileName.lastIndexOf("/") + 1), false);
				int read = 0;
				byte[] buffer = new byte[1024];
				while (true) {
					read = is.read(buffer);
					if (read == -1)
						break;
					fos.write(buffer, 0, read);
				}
			} catch (FileNotFoundException e) {
				getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage(), e));
			} catch (IOException e) {
				getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage(), e));
			}
		}
	}

	public File getDefaultTemplateDir() {
		return defaultTemplateFolder;
	}

	public DomainService getDomainService() {
		if (domainService == null)
			throw new IllegalStateException("DomainPlugin stopped.");
		return domainService;
	}

	public EventService getEventService() {
		return eventService;
	}

	public ILighthouseDomainBroadCaster getLighthouseDomainBroadCaster() {
		if (lighthouseDomainBroadcaster == null)
			lighthouseDomainBroadcaster = new LighthouseDomainBroadCasterImpl(); 
		return lighthouseDomainBroadcaster;
	}
	
	public LighthouseNatureService getNatureService() {
		if (lighthouseNatureService == null)
			throw new IllegalStateException("DomainPlugin stopped.");
		return lighthouseNatureService;
	}
	
	/**
	 * Returns the status Service
	 * 
	 * @return
	 */
	public StatusService getStatusService() {
		if (statusService == null)
			throw new IllegalStateException("StatusPlugin stopped.");
		return statusService;
	}

	protected void registerAdapters() {
		environmentContextAdapterFactory = new EnvironmentContextAdapterFactory();
		Platform.getAdapterManager().registerAdapters(environmentContextAdapterFactory, LighthouseDomain.class);
		Platform.getAdapterManager().registerAdapters(environmentContextAdapterFactory, DeploymentContainer.class);
		Platform.getAdapterManager().registerAdapters(environmentContextAdapterFactory, Location.class);
		Platform.getAdapterManager().registerAdapters(environmentContextAdapterFactory, EnvironmentContainer.class);
		Platform.getAdapterManager().registerAdapters(environmentContextAdapterFactory, ProcessTaskContainer.class);
		Platform.getAdapterManager().registerAdapters(environmentContextAdapterFactory, SoftwareComponentContainer.class);
		Platform.getAdapterManager().registerAdapters(environmentContextAdapterFactory, Deployment.class);
		Platform.getAdapterManager().registerAdapters(environmentContextAdapterFactory, Environment.class);
		Platform.getAdapterManager().registerAdapters(environmentContextAdapterFactory, ProcessTask.class);
		Platform.getAdapterManager().registerAdapters(environmentContextAdapterFactory, SoftwareComponent.class);
		
		environmentAdapterFactory = new HierarchicalEntityAdapterFactory();
		Platform.getAdapterManager().registerAdapters(environmentAdapterFactory, LighthouseDomain.class);
		Platform.getAdapterManager().registerAdapters(environmentAdapterFactory, DeploymentContainer.class);
		Platform.getAdapterManager().registerAdapters(environmentAdapterFactory, SoftwareComponentContainer.class);
		Platform.getAdapterManager().registerAdapters(environmentAdapterFactory, ProcessTaskContainer.class);
		Platform.getAdapterManager().registerAdapters(environmentAdapterFactory, EnvironmentContainer.class);
		Platform.getAdapterManager().registerAdapters(environmentAdapterFactory, Environment.class);
		Platform.getAdapterManager().registerAdapters(environmentAdapterFactory, ProcessTask.class);
		Platform.getAdapterManager().registerAdapters(environmentAdapterFactory, SoftwareComponent.class);
		Platform.getAdapterManager().registerAdapters(environmentAdapterFactory, Location.class);
		Platform.getAdapterManager().registerAdapters(environmentAdapterFactory, Deployment.class);
		
		domainBoundEntityAdapterFactory = new DomainBoundEntityAdapterFactory();
		Platform.getAdapterManager().registerAdapters(domainBoundEntityAdapterFactory, LighthouseDomain.class);
		Platform.getAdapterManager().registerAdapters(domainBoundEntityAdapterFactory, DeploymentContainer.class);
		Platform.getAdapterManager().registerAdapters(domainBoundEntityAdapterFactory, SoftwareComponentContainer.class);
		Platform.getAdapterManager().registerAdapters(domainBoundEntityAdapterFactory, ProcessTaskContainer.class);
		Platform.getAdapterManager().registerAdapters(domainBoundEntityAdapterFactory, EnvironmentContainer.class);
		Platform.getAdapterManager().registerAdapters(domainBoundEntityAdapterFactory, Environment.class);
		Platform.getAdapterManager().registerAdapters(domainBoundEntityAdapterFactory, ProcessTask.class);
		Platform.getAdapterManager().registerAdapters(domainBoundEntityAdapterFactory, SoftwareComponent.class);
		Platform.getAdapterManager().registerAdapters(domainBoundEntityAdapterFactory, Location.class);
		Platform.getAdapterManager().registerAdapters(domainBoundEntityAdapterFactory, Deployment.class);

		statusContextAdapterFactory = new StatusContextAdapterFactory();
		Platform.getAdapterManager().registerAdapters(statusContextAdapterFactory, Status.class);
		Platform.getAdapterManager().registerAdapters(statusContextAdapterFactory, StatusEditingObject.class);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		registerAdapters();

		File workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
		defaultTemplateFolder = new File(workspace, "templates");
		if (!defaultTemplateFolder.exists()) {
			defaultTemplateFolder.mkdir();
			copyTemplates();
		}
		
		lighthouseNatureService = new LighthouseNatureServiceImpl();
		eventService = new EventServiceImpl(context);
		domainService = new DomainServiceImpl(context);
		statusService = new StatusServiceImpl(context);		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		unregisterAdapters();
		statusService.closeAllLighthouseDomains();
		statusService = null;
		eventService.closeAllConnectionsToServer();
		eventService = null;
		domainService = null;
		lighthouseNatureService = null;
		super.stop(context);
		plugin = null;
	}
	
	protected void unregisterAdapters() {
		Platform.getAdapterManager().unregisterAdapters(statusContextAdapterFactory);
		statusContextAdapterFactory = null;
		Platform.getAdapterManager().unregisterAdapters(environmentAdapterFactory);
		environmentAdapterFactory = null;
		Platform.getAdapterManager().unregisterAdapters(domainBoundEntityAdapterFactory);
		domainBoundEntityAdapterFactory = null;
		Platform.getAdapterManager().unregisterAdapters(statusContextAdapterFactory);
		statusContextAdapterFactory = null;
	}

}
