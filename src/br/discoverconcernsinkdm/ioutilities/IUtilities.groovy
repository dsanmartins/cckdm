package br.discoverconcernsinkdm.ioutilities;

import java.io.IOException;

public interface IUtilities {

	public abstract void createLibraryCSV(String projectName);

	public abstract void createClusterCSV(String projectName);

	public abstract void copyInitialFile(String projectName);

	public abstract void copyKDMFile(String file, String path, String fileName);

	public abstract void copyFinalFile(String projectName);

	public abstract void saveLogs(String log, String projectName, int op);

	public abstract String getLicense() throws IOException;

	public abstract String getLogs(String projectName, int op)
			throws IOException;

}