package br.discoverconcernsinkdm.queryManager;

import org.basex.core.BaseXException;
import org.basex.core.Context;

public interface IXQueryEngine {

	public abstract void createDB(String path, String file, String dbName) throws BaseXException;
	
	public abstract void openDB() throws BaseXException;

	public abstract void closeDB() throws BaseXException;

	public abstract void exportDB() throws BaseXException;

	public abstract void dropDB() throws BaseXException;
	
	public Context getContext();
}