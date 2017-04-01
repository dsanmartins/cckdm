/*
 *   Copyright 2013 Daniel Gustavo San Martín Santibáñez

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package br.discoverconcernsinkdm.tagManager;

import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Export;
import org.basex.core.cmd.Open;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.iter.Iter;
import org.basex.query.value.Value;
import org.basex.query.value.item.Item;

import br.discoverconcernsinkdm.persistenceManager.Query;



public class KDMAnnotation implements Annotation  {

	Context context; 
	CreateDB createDB;
	String path;
	Open openDb;
	Close closeDb;
	DropDB dropDB;
	Export export;
	static Logger log = Logger.getLogger(KDMAnnotation.class);

	public KDMAnnotation(String path, String file, String dbName) throws BaseXException
	{
		// Create a database from a local or remote XML document or XML String
		context = new Context();
		createDB = new CreateDB(dbName, path + file);
		createDB.execute(context);
		this.path = path + file; 
		openDb = new Open(dbName);
		closeDb = new Close();
		export = new Export(this.path); 
		dropDB = new DropDB(dbName);
	}

	@Override
	/*
	 * 
	 */
	public void createDB(String path, String file, String dbName) throws BaseXException {
		// TODO Auto-generated method stub
		context = new Context();
		createDB = new CreateDB(dbName, path + file);
		createDB.execute(context);
		openDb = new Open(dbName);
	}

	/* (non-Javadoc)
	 * @see br.discoverconcernsinkdm.xquery.Annotation#openDB()
	 */
	@Override
	public void openDB() throws BaseXException
	{
		openDb.execute(context);
	}

	/* (non-Javadoc)
	 * @see br.discoverconcernsinkdm.xquery.Annotation#closeDB()
	 */
	@Override
	public void closeDB() throws BaseXException
	{
		closeDb.execute(context);
	}

	/* (non-Javadoc)
	 * @see br.discoverconcernsinkdm.xquery.Annotation#exportDB()
	 */
	@Override
	public void exportDB() throws BaseXException
	{
		export.execute(context);
	}

	/* (non-Javadoc)
	 * @see br.discoverconcernsinkdm.xquery.Annotation#dropDB()
	 */
	@Override
	public void dropDB() throws BaseXException
	{
		dropDB.execute(context);
	}


	/* (non-Javadoc)
	 * @see br.discoverconcernsinkdm.xquery.Annotation#annotation(java.lang.String, java.util.ArrayList, java.util.ArrayList)
	 */
	@Override
	public void storableUnitAnnotation(String concern, ArrayList<String> arrayProperty) throws BaseXException, SQLException, QueryException
	{
		this.openDB();
		for (int i=0; i<arrayProperty.size(); i++)
		{
			String [] var = arrayProperty.get(i).split("\\|");
			String module = var[0];
			String method = var[1];
			String property = var[2];
			String kind = var[3];


			if (method.equals("-"))
			{
				if (kind.equals("static"))
				{
					String query = Query.getQuery("check-2");
					QueryProcessor proc = new QueryProcessor(query, context);
					proc.bind("concern", concern);
					proc.bind("class", module);
					proc.bind("property", property);
					Iter iter = proc.iter();
					String rtn = ""; 
					for(Item item; (item = iter.next()) != null;)
						rtn = item.toJava().toString();

					proc.close();
					log.info("STATIC-IN-CLASS");
					log.info("Concern name: " + concern);
					log.info("Module name: " + module);
					log.info("Method name: " + method);
					log.info("Property name: " + property);
					log.info("Type of Artifact: "  + kind);
					log.info("Length of Return: "+rtn.length() + " Name: " + rtn);
					log.info("---------------------------------------------------");
					if (!rtn.equals(concern))
					{
						query = Query.getQuery("propertyAnnotation-2");
						proc = new QueryProcessor(query, context);
						String tag = "<attribute tag=\"concern\" value=\"" + concern + "\"/>";
						proc.bind("tag", tag);
						proc.bind("class", module);
						proc.bind("property", property);
						proc.bind("kind", kind);

						// Execute the query
						proc.execute();
						proc.close();
					}
				}
				else
				{
					if (kind.equals("global"))
					{	
						String query = Query.getQuery("check-2");
						QueryProcessor proc = new QueryProcessor(query, context);
						proc.bind("concern", concern);
						proc.bind("class", module);
						proc.bind("property", property);
						Iter iter = proc.iter();
						String rtn = ""; 
						for(Item item; (item = iter.next()) != null;) 
							rtn = item.toJava().toString();
						
						proc.close();
						log.info("GLOBAL-IN-CLASS");
						log.info("Concern name: " + concern);
						log.info("Module name: " + module);
						log.info("Method name: " + method);
						log.info("Property name: " + property);
						log.info("Type of Artifact: "  + kind);
						log.info("Length of Return: "+rtn.length() + " Name: " + rtn);
						log.info("---------------------------------------------------");
						if (!rtn.equals(concern))
						{
							query = Query.getQuery("propertyAnnotation-2");
							proc = new QueryProcessor(query, context);
							String tag = "<attribute tag=\"concern\" value=\"" + concern + "\"/>";
							proc.bind("tag", tag);
							proc.bind("class", module);
							proc.bind("property", property);

							// Execute the query
							proc.execute();
							proc.close();
						}
					}
				}
			}
			else
			{
				if (kind.equals("local") || kind.equals("static"))
				{
					String query = Query.getQuery("check-1");
					QueryProcessor proc = new QueryProcessor(query, context);
					proc.bind("concern", concern);
					proc.bind("class", module);
					proc.bind("method", method);
					proc.bind("property", property);
					proc.bind("kind", kind);
					Iter iter = proc.iter();
					String rtn = ""; 
					for(Item item; (item = iter.next()) != null;) 
						rtn = item.toJava().toString();
					
					proc.close();

					log.info("LOCAL-OR-STATIC-IN-METHOD");
					log.info("Concern name: " + concern);
					log.info("Module name: " + module);
					log.info("Method name: " + method);
					log.info("Property name: " + property);
					log.info("Type of Artifact: "  + kind);
					log.info("Length of Return: "+rtn.length() + " Name: " + rtn);
					log.info("---------------------------------------------------");

					if (!rtn.equals(concern))
					{
						query = Query.getQuery("propertyAnnotation-1");
						proc = new QueryProcessor(query, context);
						String tag = "<attribute tag=\"concern\" value=\"" + concern + "\"/>";
						proc.bind("tag", tag);
						proc.bind("class", module);
						proc.bind("method", method);
						proc.bind("property", property);
						proc.bind("kind", kind);

						// Execute the query
						proc.execute();
						proc.close();

						//If the property belongs to a method then annotate it too
						ArrayList<String> arrayMethod = new ArrayList<String>();
						arrayMethod.add(module +"|"+method);
						this.methodUnitAnnotation(concern, arrayMethod,true);
					}
				}
			}
		}
		this.exportDB();
		this.closeDB();
		this.dropDB();
		context.close();
	}

	public void methodUnitAnnotation(String concern, ArrayList<String> arrayMethod, boolean internal) throws BaseXException, SQLException, QueryException
	{
		if (!internal)
			this.openDB();
		for (int i=0; i<arrayMethod.size(); i++)
		{
			String [] var = arrayMethod.get(i).split("\\|");
			String module = var[0];
			String method = var[1];

			String query = Query.getQuery("check-3");
			QueryProcessor proc = new QueryProcessor(query, context);
			proc.bind("concern", concern);
			proc.bind("class", module);
			proc.bind("method", method);
			Iter iter = proc.iter();
			String rtn = ""; 
			for(Item item; (item = iter.next()) != null;) 
				rtn = item.toJava().toString();

			proc.close();
			
			log.info("METHOD");
			log.info("Concern name: " + concern);
			log.info("Module name: " + module);
			log.info("Method name: " + method);
			log.info("Length of Return: "+rtn.length() + " Name: " + rtn);
			log.info("---------------------------------------------------");

			if (!rtn.equals(concern))
			{
				query = Query.getQuery("methodAnnotation");
				proc = new QueryProcessor(query, context);
				String tag = "<attribute tag=\"concern\" value=\"" + concern + "\"/>";
				proc.bind("tag", tag);
				proc.bind("class", module);
				proc.bind("method", method);

				// Execute the query
				proc.execute();
				proc.close();
			}
		}
		if (!internal)
		{
			this.exportDB();
			this.closeDB();
			this.dropDB();
			context.close();
		}
	}

	public void annotationRemove(ArrayList<String> arrayProperty, ArrayList<String> arrayMethod) throws BaseXException, SQLException, QueryException
	{
		this.openDB();

		for (int i=0; i<arrayProperty.size(); i++)
		{
			String [] var = arrayProperty.get(i).split("\\|");
			String module = var[0];
			String method = var[1];
			String property = var[2];
			String kind = var[3];


			if (method.equals("-"))
			{
				if (kind.equals("static"))
				{
					String query = Query.getQuery("propertyAnnotationRemove-2");
					QueryProcessor proc = new QueryProcessor(query, context);
					proc.bind("class", module);
					proc.bind("property", property);
					proc.bind("kind", kind);

					// Execute the query
					proc.execute();
					proc.close();
				}
				else
				{
					if (kind.equals("global"))
					{				
						String query = Query.getQuery("propertyAnnotationRemove-2");
						QueryProcessor proc = new QueryProcessor(query, context);
						proc.bind("class", module);
						proc.bind("property", property);

						// Execute the query
						proc.execute();
						proc.close();
					}
				}
			}
			else
			{
				if (kind.equals("local") || kind.equals("static"))
				{
					String query = Query.getQuery("propertyAnnotationRemove-1");
					QueryProcessor proc = new QueryProcessor(query, context);
					proc.bind("class", module);
					proc.bind("method", method);
					proc.bind("property", property);
					proc.bind("kind", kind);

					// Execute the query
					proc.execute();
					proc.close();
				}
			}
		}


		for (int i=0; i<arrayMethod.size(); i++)
		{
			String [] var = arrayMethod.get(i).split("\\|");
			String module = var[0];
			String method = var[1];

			String query = Query.getQuery("methodAnnotationRemove");
			QueryProcessor proc = new QueryProcessor(query, context);
			proc.bind("class", module);
			proc.bind("method", method);

			// Execute the query
			proc.execute();
			proc.close();
		}
		//Export DataBase
		this.exportDB();
		// Close the database context
		this.closeDB();
		this.dropDB();
		context.close();
	}

	public void javaAnnotation(String projectName, ArrayList<String> concerns, String path) throws BaseXException, SQLException, QueryException
	{
		this.openDB();
		String position = "";
		String location = "";
		String query = Query.getQuery("javaAnnotationMining");
		QueryProcessor proc = new QueryProcessor(query, context);
		ArrayList<String> arrayAddress = new ArrayList<String>();
		ArrayList<String> arrayLocation = new ArrayList<String>();
		ArrayList<String> packageNames = new ArrayList<String>();
		ArrayList<String> methodAnnotation = new ArrayList<String>();

		// Execute the query
		Iter iter = proc.iter();
		Item item;

		while ((item = iter.next()) != null) 
		{
			String array[] = item.toJava().toString().split("\\/");
			for (int i=0; i< array.length; i++)
			{
				if (array[i].contains("model"))
					position = position + (Integer.parseInt(array[i].split("\\.")[1]) + 1);
				else
					if (array[i].contains("codeElement"))
						position = position + "_" + (Integer.parseInt(array[i].split("\\.")[1]) +1);
					else
						location = location + "_" +array[i];
			}
			if (!position.equals(""))
				arrayAddress.add(position);
			if (!position.equals(""))
				arrayLocation.add(location);
			position = "";
			location = "";
		}
		proc.close();

		String packageName = "";
		for (int i= 0; i<arrayAddress.size() ; i++)
		{
			int size = (arrayAddress.get(i).split("\\_")).length;
			for (int j=2; j<=size; j++)
			{
				query = Query.getQuery("getTypeAnnotation_"+ j);
				proc = new QueryProcessor(query, context);
				proc.bind("elementPosition", arrayAddress.get(i));
				packageName = packageName + "." + proc.execute().toString();
			}
			packageNames.add(packageName.substring(1,packageName.length())+ "_"+i);
			packageName = "";
			proc.close();
		}
		this.closeDB();

		//Search package in our concern library
		this.createDB(path,"ConcernLibrary.xml","CL");
		this.openDB();
		for (String concern: concerns)
		{	
			query = Query.getQuery("getConcernLibrary");
			proc = new QueryProcessor(query, context);
			proc.bind("elementPosition", concern);
			// Execute the query
			iter = proc.iter();

			while ((item = iter.next()) != null) 
			{
				String array = item.toJava().toString();
				for (int k=0; k<packageNames.size() ; k++)
				{
					if (packageNames.get(k).split("\\_")[0].equals(array))
					{
						String loc = arrayLocation.get(Integer.parseInt(packageNames.get(k).split("\\_")[1]));
						loc = loc.substring(4,loc.length());
						methodAnnotation.add(loc + "_" + concern);
					}
				}	
			}
		}
		proc.close();
		this.closeDB();

		//Tag KDM with annotations
		this.createDB(path, projectName + "_KDM" + ".xmi", "DBKDM");
		for (int i = 0; i<methodAnnotation.size();i++)
		{
			this.openDB();
			String locArr[] = methodAnnotation.get(i).split("\\_");
			query = Query.getQuery("check-3");
			proc = new QueryProcessor(query, context);
			proc.bind("concern", locArr[2].toLowerCase());
			proc.bind("class", locArr[1]);
			proc.bind("method", locArr[0]);
			iter = proc.iter();
			String rtn = ""; 
			for(Item it; (it = iter.next()) != null;) 
				rtn = it.toJava().toString();
			
			proc.close();
			this.closeDB();

			if (!rtn.equals(locArr[2].toLowerCase()))
			{
				this.openDB();
				query = Query.getQuery("methodAnnotation");
				proc = new QueryProcessor(query, context);
				proc.bind("method", locArr[0]);
				proc.bind("class", locArr[1]);
				String tag = "<attribute tag=\"concern\" value=\"" + locArr[2].toLowerCase() + "\"/>";
				proc.bind("tag", tag);
				// Execute the query
				proc.execute();
				proc.close();
				this.exportDB();
				this.closeDB();
			}
		}
		this.dropDB();
		context.close();
	}
}
