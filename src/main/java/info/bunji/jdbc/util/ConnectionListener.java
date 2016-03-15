/**
 *
 */
package info.bunji.jdbc.util;

import java.util.EventListener;

import info.bunji.jdbc.ConnectionProxy;

/**
 *
 * @author f.kinoshita
 */
public interface ConnectionListener extends EventListener {

	/**
	 *
	 */
	public void newConnecion(ConnectionProxy proxy);
}
