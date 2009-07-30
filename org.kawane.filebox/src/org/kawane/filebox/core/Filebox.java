package org.kawane.filebox.core;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Filebox implements  Observable {

	public static final String NAME = "name";
	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final String PROPERTIES = "properties";

	public static final String FILEBOXES = "fileboxes";

	final private List<Filebox> fileboxes = new ArrayList<Filebox>();
	final protected Observable.Stub obs = new Observable.Stub();

	final private Preferences preferences = Globals.getPreferences();

	protected String name;
	protected String host;
	protected int port;

	protected boolean connected = false;

	public Filebox() {
		String preferencesName = preferences.getName();
		this.name = preferencesName == null ? "Me" : preferencesName;

		this.port = preferences.getPort();
		this.host = "localhost";
	}

	public int getFileboxesCount() {
		return fileboxes.size();
	}

	public List<Filebox> getFileboxes() {
		return Collections.unmodifiableList(fileboxes);
	}

	public Filebox getFilebox(int index) {
		return fileboxes.get(index);
	}

	public void addFilebox(Filebox newFilebox) {
		addFilebox(0, newFilebox);
	}

	public void addFilebox(int index, Filebox newFilebox) {
		fileboxes.add(index, newFilebox);
		obs.fireIndexedPropertyChange(this, FILEBOXES, index, null, newFilebox);
	}

	public Filebox removeFilebox(Filebox filebox) {
		int index = fileboxes.indexOf(filebox);
		return removeFilebox(index);
	}

	public Filebox removeFilebox(int index) {
		Filebox oldFilebox = fileboxes.remove(index);
		obs.fireIndexedPropertyChange(this, FILEBOXES, index, oldFilebox, null);
		return oldFilebox;
	}

	public void clearFileboxes() {
		while ( !fileboxes.isEmpty() ) removeFilebox(0);
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		String oldValue = this.host;
		this.host = host;
		obs.firePropertyChange(this, HOST, oldValue, host);
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		int oldValue = this.port;
		this.port = port;
		obs.firePropertyChange(this, PORT, oldValue, port);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		String oldValue = this.name;
		this.name = name;
		obs.firePropertyChange(this, NAME, oldValue, name);
	}

	/** connects this to fileboxes network */
	public void connect() {
		if ( connected ) return;
		// TODO connect
	}

	/** disconnects this from fileboxes network */
	public void disconnect() {
		if ( !connected ) return;

		// TODO disconnect
	}

	/** @return true if the filebox is connected. */
	public boolean isConnected() {
		return connected;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		obs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		obs.removePropertyChangeListener(listener);
	}
}