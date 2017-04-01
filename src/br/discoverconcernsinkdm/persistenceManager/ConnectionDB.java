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

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import br.discoverconcernsinkdm.persistenceManager.ConnectionDB;

public final class ConnectionDB {


	public Connection conn;
	private PreparedStatement statement;
	private static ConnectionDB db;
	String projectName = null;
	IWorkspace workspace = ResourcesPlugin.getWorkspace();  

	private ConnectionDB(String projectName) throws SQLException, IOException
	{
		Properties p = new Properties();
		InputStream in = ConnectionDB.class.getClassLoader().getResourceAsStream("db.properties");
		p.load(in);
		
		String folder= workspace.getRoot().getLocation().toFile().getPath().toString(); 
		this.projectName = projectName;
		String driver = p.getProperty("derby.driver");
		String url = p.getProperty("derby.url");
		String dbName =  folder + "/" + this.projectName + "/derbyDB;create=true";

		try 
		{
			Class.forName(driver).newInstance();
			this.conn = DriverManager.getConnection(url + dbName);
		}
		catch (Exception sqle) {
			sqle.printStackTrace();
		}
	}
	/**
	 *
	 * @return MysqlConnect Database connection object
	 * @throws IOException 
	 */
	public static synchronized ConnectionDB getInstance(String projectName) throws SQLException, IOException 
	{	
		if ( db == null ) 
		{
			db = new ConnectionDB(projectName);
		}
		return db;
	}

	/**
	 * @param insertQuery String The Insert query
	 * @return boolean 
	 * @throws XQException 
	 */



	public void executeDDL(String query) throws SQLException  
	{
		statement = db.conn.prepareStatement(query);
		statement.execute();
	}


	public ResultSet executeQuery(String query, List<String> params) throws SQLException  
	{
		statement = db.conn.prepareStatement(query);

		if (params != null)
			for (int i=0; i< params.size(); i++)
				statement.setString(i+1, (String) params.get(i));
		ResultSet rs = statement.executeQuery();
		return rs;
	} 

	public ResultSet executeQuery(String query) throws SQLException  
	{
		statement = db.conn.prepareStatement(query);
		ResultSet rs = statement.executeQuery();
		return rs;
	} 

	public int executeUpdateInsert(String query, List<String> params) throws SQLException  
	{
		statement = db.conn.prepareStatement(query);
		if (params != null)
			for (int i=0; i< params.size(); i++)
				statement.setString(i+1, (String) params.get(i).toString());
		int rs = statement.executeUpdate();
		return rs;
	} 
	
	public int[] addBatch(String query, List<String> params) throws SQLException  
	{
		this.conn.setAutoCommit(false);
		statement = db.conn.prepareStatement(query);
		if (params != null)
			for (int i=0; i< params.size(); i++)
			{
				String[] l = params.get(i).split("\\|");
				for (int j = 0; j< l.length; j++)
					statement.setString(j+1, (String) l[j].toString());
				statement.addBatch();
			}
		
		int [] res = statement.executeBatch();
		this.conn.commit();
		this.conn.setAutoCommit(true);
		return res;
	} 
		
	public int executeUpdateInsert(String query) throws SQLException  
	{
		statement = db.conn.prepareStatement(query);
		int rs = statement.executeUpdate();
		return rs;
	} 


	public void close()
	{
		Properties p = new Properties();
		InputStream in = ConnectionDB.class.getClassLoader().getResourceAsStream("db.properties");
		boolean gotSQLExc = false;
		String url="";
		String shutdown="";
		try
		{
			p.load(in);

			url = p.getProperty("derby.url");
			shutdown =  p.getProperty("derby.shutdown");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		try
		{
			if (this.statement != null) this.statement.close();
			if (this.conn != null) this.conn.close();
			db = null;
			DriverManager.getConnection(url + shutdown);
		}
		catch (SQLException ex)
		{
			if ( ex.getSQLState().equals("XJ015") ) {
				gotSQLExc = true;
			}
		}
	

		if (!gotSQLExc) {
			System.out.println("Database did not shut down normally");
		} else {
			System.out.println("Database shut down normally");
		}
		System.gc();
	}

}    



