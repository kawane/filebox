package org.kawane.filebox.core.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import javax.jmdns.impl.JmDNSImpl;

import org.kawane.filebox.core.discovery.FileboxService;
import org.kawane.filebox.core.discovery.IFileboxServiceListener;
import org.kawane.filebox.core.discovery.IServiceDiscovery;
import org.osgi.service.log.LogService;

public class ServiceDiscovery implements ServiceListener, IServiceDiscovery {

	private static LogService logger = Activator.getInstance().getLogger();

	private JmDNSImpl dns;
	private ServiceInfo serviceInfo;
	private String name;
	private Collection<IFileboxServiceListener> listeners = new HashSet<IFileboxServiceListener>();
	private Map<String, String> properties;
	private int port;
	private Object waitInitialization = new Object();

	public ServiceDiscovery(String name, int port, Map<String, String> properties) {
		this.name = name;
		this.properties = properties;
		this.port = port;
	}

	public String getName() {
		return name;
	}

	public int getPort() {
		return port;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public String getHostname() {
		return dns.getHostName();
	}

	public void start() {
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			public void run() {
				try {
					synchronized (waitInitialization) {
						dns = new JmDNSImpl();
						serviceInfo = ServiceInfo.create(FILEBOX_TYPE, name, port, FILEBOX_WEIGHT, FILEBOX_PRIORITY, new Hashtable<String, String>(
							properties));
						dns.registerService(serviceInfo);
						dns.addServiceListener(FILEBOX_TYPE, ServiceDiscovery.this);
					}
					dns.close();
				} catch (Throwable e) {
					logger.log(LogService.LOG_ERROR, "An Error Occured", e);
				}
			}
		};
		timer.schedule(task, 500);
	}

	public void stop() {
		if(dns != null) {
			synchronized (waitInitialization) {
				//TODO is this close method call really nesessary in zeroconf protocol: http://www.zeroconf.org/
				dns.close();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.kawane.filebox.core.internal.IServiceDiscovery#getServices()
	 */
	public Collection<FileboxService> getServices() {
		Collection<FileboxService> services = new ArrayList<FileboxService>();
		if(dns != null) {
			ServiceInfo[] servicesInfo = dns.list(FILEBOX_TYPE);
			for (ServiceInfo serviceInfo : servicesInfo) {
				FileboxService fileboxService = createFileboxService(serviceInfo);
				services.add(fileboxService);
			}
		}
		return services;
	}

	/* (non-Javadoc)
	 * @see org.kawane.filebox.core.internal.IServiceDiscovery#addServiceListener(org.kawane.filebox.core.discovery.IFileboxServiceListener)
	 */
	public void addServiceListener(IFileboxServiceListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.kawane.filebox.core.internal.IServiceDiscovery#removeServiceListener(org.kawane.filebox.core.discovery.IFileboxServiceListener)
	 */
	public void removeServiceListener(IFileboxServiceListener listener) {
		listeners.remove(listener);
	}

	private FileboxService createFileboxService(ServiceInfo serviceInfo) {
		Map<String, String> properties = new HashMap<String, String>();
		@SuppressWarnings("unchecked")
		Enumeration<String> propertyNames = serviceInfo.getPropertyNames();
		while (propertyNames.hasMoreElements()) {
			String propertyName = propertyNames.nextElement();
			properties.put(propertyName, serviceInfo.getPropertyString(propertyName));
		}
		FileboxService fileboxService = new FileboxService(serviceInfo.getHostAddress(), serviceInfo.getPort(), serviceInfo.getName(), properties);
		return fileboxService;
	}

	public void serviceAdded(ServiceEvent event) {
		// the service is added but not resolved, not interesting for our application
	}

	public void serviceRemoved(ServiceEvent event) {
		logger.log(LogService.LOG_INFO, "A service has been removed");
		HashSet<IFileboxServiceListener> listenersCopy = new HashSet<IFileboxServiceListener>(listeners);
		for (IFileboxServiceListener listener : listenersCopy) {
			listener.serviceRemoved(createFileboxService(event.getInfo()));
		}
	}

	public void serviceResolved(ServiceEvent event) {
		logger.log(LogService.LOG_INFO, "A service has been added");
		HashSet<IFileboxServiceListener> listenersCopy = new HashSet<IFileboxServiceListener>(listeners);
		for (IFileboxServiceListener listener : listenersCopy) {
			listener.serviceAdded(createFileboxService(event.getInfo()));
		}
	}

}