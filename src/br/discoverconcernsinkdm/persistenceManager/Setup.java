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

package br.discoverconcernsinkdm.persistenceManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.gmt.modisco.omg.kdm.action.BlockUnit;
import org.eclipse.gmt.modisco.omg.kdm.kdm.KDMModel;

import br.discoverconcernsinkdm.persistenceManager.ConnectionDB;
import br.discoverconcernsinkdm.persistenceManager.Query;
import br.discoverconcernsinkdm.queryManager.IQueries;
import br.discoverconcernsinkdm.queryManager.ModelElementsByOCL;

public class Setup {

	File file;
	String projectName = null;
	IWorkspace workspace = ResourcesPlugin.getWorkspace();  

	public Setup(String projectName) throws IOException
	{
		Properties p = new Properties();
		InputStream in = ConnectionDB.class.getClassLoader().getResourceAsStream("db.properties");
		p.load(in);
		this.projectName = projectName;
		String folder= workspace.getRoot().getLocation().toFile().getPath().toString(); 
		String address = folder + "/" + this.projectName + "/derbyDB";
		file = new File(address);
	}

	public int setUpModel() throws IOException 
	{
		int rtn = 0;
		if (!file.exists())
		{
			rtn = 0;
			try
			{
				ConnectionDB.getInstance(projectName).executeDDL(Query.getQuery("createPackage"));
				ConnectionDB.getInstance(projectName).executeDDL(Query.getQuery("createModule"));
				ConnectionDB.getInstance(projectName).executeDDL(Query.getQuery("createModuleToPackage"));
				ConnectionDB.getInstance(projectName).executeDDL(Query.getQuery("createMethod"));
				ConnectionDB.getInstance(projectName).executeDDL(Query.getQuery("createPropertyLocal"));
				ConnectionDB.getInstance(projectName).executeDDL(Query.getQuery("createPropertyGlobal"));
				ConnectionDB.getInstance(projectName).executeDDL(Query.getQuery("createCalls"));
				ConnectionDB.getInstance(projectName).executeDDL(Query.getQuery("createImports"));
				ConnectionDB.getInstance(projectName).executeDDL(Query.getQuery("createTemporalTable"));
				ConnectionDB.getInstance(projectName).executeUpdateInsert(Query.getQuery("populateTables"),null);
			}
			catch (SQLException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			rtn = -1;
		}
		return rtn;
	}

	public void closeModel() throws SQLException, IOException
	{
		ConnectionDB.getInstance(projectName).close();
	}

	public void getModelClasses(KDMModel kdmModel, String folder, String projectName, int rtn)
	{
		IQueries modelElements = new ModelElementsByOCL(kdmModel, folder, projectName);

		if (rtn == 0)
		{
			try
			{
				modelElements.setModelClasses();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void getModelInterfaces(KDMModel kdmModel, String folder, String projectName, int rtn)
	{
		IQueries modelElements = new ModelElementsByOCL(kdmModel, folder, projectName);

		if (rtn == 0)
		{
			try
			{
				modelElements.setModelInterfaces();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public BlockUnit getModelMethods(KDMModel kdmModel, String folder, String projectName, int rtn)
	{
		IQueries modelElements = new ModelElementsByOCL(kdmModel, folder, projectName);
		BlockUnit blockUnit = null;

		if (rtn == 0)
		{
			try
			{
				blockUnit = modelElements.setModelMethods();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return blockUnit;
	}
	
	public void getCalls(KDMModel kdmModel, String folder, String projectName, int rtn, BlockUnit blockUnit)
	{
		IQueries modelElements = new ModelElementsByOCL(kdmModel, folder, projectName);

		if (rtn == 0)
		{
			try
			{
				modelElements.setCalls(blockUnit);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void getModelProperties(KDMModel kdmModel, String folder, String projectName, int rtn)
	{
		IQueries modelElements = new ModelElementsByOCL(kdmModel, folder, projectName);

		if (rtn == 0)
		{
			try
			{
				modelElements.setModelProperties();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void shutDown() throws IOException
	{
		delete(file);
	}

	private void delete(File file)
	{
		if(file.isDirectory())
		{
			//directory is empty, then delete it
			if(file.list().length==0)
				file.delete();
			else
			{
				//list all the directory contents
				String files[] = file.list();
				for (String temp : files) 
				{
					//construct the file structure
					File fileDelete = new File(file, temp);
					//recursive delete
					delete(fileDelete);
				}
				//check the directory again, if empty then delete it
				if(file.list().length==0)
					file.delete();
			}
		}
		else
		{
			//if file, then delete it
			file.delete();
		}

	}

	public String getNumberClasses() throws IOException
	{
		int count = 0;
		try
		{
			ResultSet rs =	ConnectionDB.getInstance(projectName).executeQuery(Query.getQuery("getNClasses"));
			while (rs.next())
				count = rs.getInt("COUNT");
			rs.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		return Integer.toString(count);
	}

	public String getNumberInterfaces() throws IOException
	{
		int count = 0;
		try
		{
			ResultSet rs =	ConnectionDB.getInstance(projectName).executeQuery(Query.getQuery("getNInterfaces"));
			while (rs.next())
				count = rs.getInt("COUNT");
			rs.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		return Integer.toString(count);
	}

	public String getNumberMethods() throws IOException
	{
		int count = 0;
		try
		{
			ResultSet rs =	ConnectionDB.getInstance(projectName).executeQuery(Query.getQuery("getNMethods"));
			while (rs.next())
				count = rs.getInt("COUNT");
			rs.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		return Integer.toString(count);
	}

	public String getNumberProperties() throws IOException
	{
		int count = 0;
		try
		{
			ResultSet rs =	ConnectionDB.getInstance(projectName).executeQuery(Query.getQuery("getNProperties"));
			while (rs.next())
				count = rs.getInt("CONT");
			rs.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		return Integer.toString(count);
	}


	public ArrayList<String[]> getFanIn() throws IOException
	{
		ArrayList<String[]> item = new ArrayList<String[]>();

		try
		{
			ResultSet rs =	ConnectionDB.getInstance(projectName).executeQuery(Query.getQuery("fanin"));

			while (rs.next())
			{
				String idMethod = rs.getString("IDMETHOD");
				String count = Integer.toString(rs.getInt("COUNT"));
				item.add((new String[] {idMethod, count}));
			}

			rs.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println(e.getMessage());
		}

		return item;
	}
}

