package model;

import java.io.Serializable;

public class ConflictObject implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7267830585794417461L;
	private int version;
	private String content;
	private String fileName;
	private String extension;
	
	public ConflictObject(int version, String content, String fileName, String extension) {
		this.version = version;
		this.content = content;
		this.fileName = fileName;
		this.extension = extension;
	}
	
	public String getExtension() {
		return extension;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	@Override
	public String toString() {
		
		return fileName + " " + version;
	}
	
	
	

}
