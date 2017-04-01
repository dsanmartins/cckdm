package br.discoverconcernsinkdm.tagManager;

import java.sql.SQLException;
import java.util.ArrayList;

import org.basex.core.BaseXException;
import org.basex.query.QueryException;

public interface Annotation {

	public abstract void createDB(String path, String file, String dbName) throws BaseXException;
	
	public abstract void openDB() throws BaseXException;

	public abstract void closeDB() throws BaseXException;

	public abstract void exportDB() throws BaseXException;

	public abstract void dropDB() throws BaseXException;

	public abstract void storableUnitAnnotation(String concern, ArrayList<String> arrayProperty) throws BaseXException, SQLException, QueryException;
	
	public abstract void methodUnitAnnotation(String concern, ArrayList<String> arrayMethod, boolean internal) throws BaseXException, SQLException, QueryException;
	
	public abstract void annotationRemove(ArrayList<String> arrayProperty, ArrayList<String> arrayMethod) throws BaseXException, SQLException, QueryException;
	
	public abstract void javaAnnotation(String projectName,ArrayList<String> concerns, String path) throws BaseXException,  SQLException, QueryException;
}