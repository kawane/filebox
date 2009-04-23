package org.kawane.filebox.core.internal;

import java.io.File;

import org.kawane.filebox.core.Filebox;
import org.kawane.filebox.core.IFileboxRegistry;
import org.kawane.services.ServiceRegistry;
import org.kawane.services.advanced.ServiceInjector;
import org.kawane.services.advanced.ServiceManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	protected static final String CONFIG_FILENAME = "filebox.properties";

	protected File configurationFile;

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		String configurationProperty = System.getProperty("osgi.configuration.area");
		if(configurationProperty == null || configurationProperty.length() ==0) {
			configurationProperty = System.getProperty("osgi.syspath");
		}
		if(configurationProperty != null && configurationProperty.length() !=0) {
			configurationProperty = configurationProperty.replace("file:", "");
			configurationFile = new File(configurationProperty, CONFIG_FILENAME);
		}
		// configuration file
		if (configurationFile == null) {
			// create on folder where the process run
			configurationFile = new File(CONFIG_FILENAME);
		}

		IFileboxRegistry fileboxRegistry = new FileboxRegistry();
		ServiceRegistry.instance.register(IFileboxRegistry.class, fileboxRegistry);

		// initialize filebox application
		Filebox filebox = new Filebox(configurationFile);
		new ServiceManager(filebox, Filebox.class);

	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
	}

}
