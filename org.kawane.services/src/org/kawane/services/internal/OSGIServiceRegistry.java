package org.kawane.services.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kawane.services.IServiceListener;
import org.kawane.services.IServiceRegistry;
import org.kawane.services.Service;
import org.kawane.services.advanced.ManagedService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.packageadmin.PackageAdmin;

class OSGIServiceRegistry implements IServiceRegistry {

	private static Logger logger = Logger.getLogger(OSGIServiceRegistry.class.getName());

	private PackageAdmin packageAdmin;

	private Map<IServiceListener<?>, ServiceListener> listeners = new HashMap<IServiceListener<?>, ServiceListener>();
	private Map<ServiceReference, OSGIServiceReg> servicesRegistry = new HashMap<ServiceReference, OSGIServiceReg>();
	private Map<OSGIServiceReg, ServiceRegistration> registrations = new HashMap<OSGIServiceReg, ServiceRegistration>();

	private BundleContext serviceContext;

	public OSGIServiceRegistry(PackageAdmin packageAdmin) {
		this.packageAdmin = packageAdmin;
		Bundle bundle = packageAdmin.getBundle(getClass());
		serviceContext = bundle.getBundleContext();
		serviceContext.addServiceListener(new ServiceListener() {
			public void serviceChanged(ServiceEvent event) {
				if (event.getType() == ServiceEvent.UNREGISTERING) {
					garbageService(event.getServiceReference());
				}
			}
		});
	}

	protected void garbageService(ServiceReference serviceReference) {
		OSGIServiceReg reg = servicesRegistry.remove(serviceReference);
		if (reg != null) {
			registrations.remove(reg);
		}
	}

	public void manage(Object service) {
		Collection<Service> serviceClasses = Util.getServicesClasses(service.getClass(), new HashSet<Service>(1));
		if(serviceClasses.size()>0) {
		for (Service serviceClass : serviceClasses) {
			ManagedService.manage(serviceClass, service);
		}
		} else {
			ManagedService.manage(null, service);
		}
	}

	public void unmanage(Object service) {
		ManagedService.unmanage(service);
	}

	public void register(Object service) {
		if(service == null) return;
		Collection<Service> serviceClasses = Util.getServicesClasses(service.getClass(), new HashSet<Service>(1));
		for (Service serviceClass : serviceClasses) {
			register(service, serviceClass.value());
		}
	}

	public void register(Object service, Class<?> serviceClass) {
		if(service == null) return;
		Bundle bundle = packageAdmin.getBundle(service.getClass());
		BundleContext context = bundle.getBundleContext();
		register(service, serviceClass, context);
	}

	private void register(Object service, Class<?> serviceClass, BundleContext context) {
		OSGIServiceReg reg = new OSGIServiceReg(serviceClass, service);
		if(!registrations.containsKey(reg)) {
			ServiceRegistration serviceRegistration = context.registerService(serviceClass.getName(), service, null);
			registrations.put(reg, serviceRegistration);
			servicesRegistry.put(serviceRegistration.getReference(), reg);
		}
	}

	public void unregister(Object service, Class<?> serviceClass) {
		if (service == null)
			return;
		OSGIServiceReg serviceReg = new OSGIServiceReg(serviceClass, service);
		ServiceRegistration registration = registrations.remove(serviceReg);
		if (registration == null)
			return;
		servicesRegistry.remove(registration.getReference());
		registration.unregister();
	}

	public void unregister(Object service) {
		if (service == null)
			return;
		for (Service serviceClass : Util.getServicesClasses(service.getClass(), new HashSet<Service>(1))) {
			unregister(service, serviceClass.value());
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T getService(Class<T> serviceClass) {
		ServiceReference serviceReference = serviceContext.getServiceReference(serviceClass.getName());
		if(serviceReference == null) return null;
		return (T) serviceContext.getService(serviceReference);
	}

	@SuppressWarnings("unchecked")
	public <T> T getService(Class<T> serviceClass, Object context) {
		Class<?> contextClass = context.getClass();
		if(context instanceof Class) {
			contextClass = (Class<?>)context;
		}
		BundleContext bundleContext = packageAdmin.getBundle(contextClass).getBundleContext();
		ServiceReference serviceReference = bundleContext.getServiceReference(serviceClass.getName());
		if(serviceReference == null) return null;
		return (T) bundleContext.getService(serviceReference);
	}

	public <T> Collection<T> getServices(Class<T> serviceClass) {
		return getServices(serviceClass, serviceContext);
	}

	public <T> Collection<T> getServices(Class<T> serviceClass, Object context) {
		Class<?> contextClass = context.getClass();
		if(context instanceof Class) {
			contextClass = (Class<?>)context;
		}
		BundleContext bundleContext = packageAdmin.getBundle(contextClass).getBundleContext();
		return getServices(serviceClass, bundleContext);
	}

	@SuppressWarnings("unchecked")
	protected <T> ArrayList<T> getServices(Class<T> serviceClass, BundleContext context) {
		ArrayList<T> services = new ArrayList<T>();
		try {
			ServiceReference[] serviceReferences = context.getServiceReferences(serviceClass.getName(), null);
			if (serviceReferences != null) {
				for (ServiceReference serviceReference : serviceReferences) {
					services.add((T) context.getService(serviceReference));
				}
			}
		} catch (InvalidSyntaxException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		}
		return services;
	}

	public int getServicesCount(Class<?> serviceClass) {
		try {
			ServiceReference[] serviceReferences = serviceContext.getServiceReferences(serviceClass.getName(), null);
			if (serviceReferences != null) {
				return serviceReferences.length;
			}
		} catch (InvalidSyntaxException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		}
		return 0;
	}

	public <T> void addServiceListener(Class<T> serviceClass, IServiceListener<T> listener) {
		addServiceListener(serviceClass, listener, false);
	}

	public <T> void addServiceListener(final Class<T> serviceClass, final IServiceListener<T> listener, boolean sendExistingServices) {
		Bundle bundle = packageAdmin.getBundle(listener.getClass());
		final BundleContext context = bundle.getBundleContext();

		String filter = "(" + Constants.OBJECTCLASS + "=" + serviceClass.getName() + ")";
		try {
			ServiceListener osgiServiceListener = new ServiceListener() {
				@SuppressWarnings("unchecked")
				public void serviceChanged(ServiceEvent event) {
					try {
						switch (event.getType()) {
						case ServiceEvent.REGISTERED:
							listener.serviceAdded(serviceClass, (T) context.getService(event.getServiceReference()));
							break;
						case ServiceEvent.UNREGISTERING:
							listener.serviceRemoved(serviceClass, (T) context.getService(event.getServiceReference()));
							break;
						}
					} catch (Throwable e) {
						logger.log(Level.SEVERE, "An Error Occured", e);
					}
				}
			};
			listeners.put(listener, osgiServiceListener);
			context.addServiceListener(osgiServiceListener, filter);
		} catch (InvalidSyntaxException e) {
			logger.log(Level.SEVERE, "An Error Occured", e);
		}
		if (sendExistingServices) {
			for (T service : getServices(serviceClass, context)) {
				listener.serviceAdded(serviceClass, service);
			}
		}
	}

	public <T> void removeServiceListener(Class<T> serviceClass, IServiceListener<T> listener) {
		Bundle bundle = packageAdmin.getBundle(listener.getClass());
		BundleContext context = bundle.getBundleContext();
		ServiceListener osgiServiceListener = listeners.remove(listener);
		context.removeServiceListener(osgiServiceListener);
	}

}
