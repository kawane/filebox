package org.kawane.filebox.network.http.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.kawane.filebox.json.JSONStreamWriter;
import org.kawane.filebox.mime.MimeTypeDatabase;
import org.kawane.filebox.network.http.HttpRequest;

public class JSonFileList {

	private final MimeTypeDatabase mimeTypeDatabase;

	public JSonFileList(MimeTypeDatabase mimeTypeDatabase) {
		this.mimeTypeDatabase = mimeTypeDatabase;
	}

	public String generate(File file, HttpRequest request) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		JSONStreamWriter writer = new JSONStreamWriter(stream);
		writer.beginDocument();
		writer.member("files");
		writer.beginArray();
		for (File child : order(file.listFiles())) {
			if (child.isHidden()) continue;

			writer.beginObject();
			
			writer.member("directory");
			writer.booleanValue(child.isDirectory());
			
			writer.member("mime");
			writer.stringValue(mimeTypeDatabase.searchMimeType(child));
			
			writer.member("name");
			writer.stringValue(child.getName());
			
			writer.member("size");
			writer.longValue(child.length());
			
			writer.endObject();
		}
		writer.endArray();
		writer.endDocument();
		try {
			writer.close();
		} catch (IOException e) {
			// can't happen, there is no link to system.
		}
		try {
			return new String(stream.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return new String(stream.toByteArray());
		}
	}
	
	private Collection<File> order(File... files) {

		List<File> orderedFiles2 = new ArrayList<File>();
		List<File> orderedFiles = Arrays.asList(files);
		Collections.sort(orderedFiles, new Comparator<File>() {
			public int compare(File f1, File f2) {
				return f1.getName().compareTo(f2.getName());
			}
		});
		for (File file : orderedFiles) {
			if (file.isDirectory()) {
				orderedFiles2.add(file);
			}
		}
		for (File file : orderedFiles) {
			if (!file.isDirectory()) {
				orderedFiles2.add(file);
			}
		}
		return orderedFiles2;
	}
}
