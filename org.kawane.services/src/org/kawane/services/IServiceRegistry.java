package org.kawane.services;
import java.util.Collection;

import org.kawane.services.internal.ServiceFactory;


/**
 * Service Registry can be used to use a simple way to exchange services in a Java Virtual Machine.
 *
 * These packages include an OSGI implementation that include OSGI service.
 * This Service registry can also be used in a non OSGI environnment.
 *
 * You can define the java system property "org.kawane.services.core" to true, if you want to force to use the
 * core java service registry instead of using OSGI service registry even if you are using an OSGI framework.
 *
 * @author <a href="maito:legoff.laurent@gmail.com">Laurent Le Goff </a>
 */
public interface IServiceRegistry {

	IServiceRegistry instance = ServiceFactory.createService();

	final int DEPENDENCY_RESOLVED = 0;
	final int DEPENDENCY_UNRESOLVED = 1;

	/**
	 * manage a service
	 * @param <T>
	 * @param serviceClass
	 * @param service
	 */
	void manage(Object service);

	void unmanage(Object service);
	/**
	 * register a service
	 * @param <T>
	 * @param serviceClass
	 * @param service
	 */
	void register(Object service);
	/**
	 * register a service
	 * @param <T>
	 * @param serviceClass
	 * @param service
	 */
	void register(Object service, Class<?> serviceClass);
	/**
	 * unregister a service
	 * @param <T>
	 * @param serviceClass
	 * @param service
	 */
	void unregister(Object service);
	/**
	 * unregister a service
	 * @param <T>
	 * @param serviceClass
	 * @param service
	 */
	void unregister(Object service, Class<?> serviceClass);
	/**
	 * Return the first service corresponding to this class.
	 * @param <T>
	 * @param serviceClass
	 * @return
	 */
	<T> T getService(Class<T> serviceClass);
	/**
	 * Return the first service corresponding to this class.
	 * Use a context object to determine ClassLoader in particular execution environment like OSGI.
	 * @param <T>
	 * @param serviceClass
	 * @param context
	 * @return
	 */
	<T> T getService(Class<T> serviceClass, Object context);
	/**
	 * Return the number of services that implement or extend this class
	 *
	 * @param serviceClass
	 * @param context
	 * @return
	 */
	int getServicesCount(Class<?> serviceClass);
	/**
	 * Return a list of service
	 * @param <T>
	 * @param serviceClass
	 * @return
	 */
	<T> Collection<T> getServices(Class<T> serviceClass);
	/**
	 * Return a list of service
	 * Use a context object to determine ClassLoader in particular execution environment like OSGI.
	 * @param <T>
	 * @param serviceClass
	 * @param context
	 * @return
	 */
	<T> Collection<T> getServices(Class<T> serviceClass, Object context);
	/**
	 * Add a service listener
	 * @param <T>
	 * @param serviceClass
	 * @param listener
	 */
	<T>void addServiceListener(Class<T> serviceClass, IServiceListener<T> listener);
	/**
	 * Add a service listener
	 * @param <T>
	 * @param serviceClass
	 * @param listener
	 */
	<T>void addServiceListener(Class<T> serviceClass, IServiceListener<T> listener, boolean sendExistingServices);
	/**
	 * Remove a service listener
	 * @param <T>
	 * @param serviceClass
	 * @param listener
	 */
	<T>void removeServiceListener(Class<T> serviceClass, IServiceListener<T> listener);
}
