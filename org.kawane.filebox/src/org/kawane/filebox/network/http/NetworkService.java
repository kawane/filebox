/**
 * Filebox developed by Kawane.
 * LGPL License.
 */
package org.kawane.filebox.network.http;


/**
 *
 * A {@link NetworkService} is able to handle {@link HttpRequest} to propose a network service.
 *
 * @author Jean-Charles Roger
 *
 */
public interface NetworkService {

	/** Handle a request for a given Filebox. */
	public void handleRequest(HttpRequest request, HttpResponse response);

}
