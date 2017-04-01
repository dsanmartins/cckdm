package br.discoverconcernsinkdm.concernManager;

import java.util.ArrayList;



public interface IXMLLibrary {

	public abstract def writeXML(String xml, path);

	public abstract def writeFile(fileName, closure);

	public abstract def createInitialLibrary(String path);

	public abstract ArrayList<String> readLibrary(String project);

	public abstract void parseCSV(String text, String path);

	public abstract ArrayList<String> getConcerns(String path);

	public abstract ArrayList<String> getPackages(String path, String concern);

	public abstract ArrayList<String> getElements(String path, String concern, String packageName);

}