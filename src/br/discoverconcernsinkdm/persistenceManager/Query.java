package br.discoverconcernsinkdm.persistenceManager;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

import br.discoverconcernsinkdm.persistenceManager.Query;


public class Query {

	private static final String propFileName = "queries.properties";
	private static Properties props;

	public static Properties getQueries() throws SQLException {
		InputStream is = Query.class.getClassLoader().getResourceAsStream(propFileName);
		if (is == null){
			throw new SQLException("Unable to load property file: " + propFileName);
		}
		//singleton
		if(props == null)
		{
			props = new Properties();
			try 
			{
				props.loadFromXML(is);
			} catch (IOException e) 
			{
				throw new SQLException("Unable to load property file: " + propFileName + "\n" + e.getMessage());
			}
			finally 
			{
			    if (null != is)
			    {
			        try
			        {
			        	is.close();
			        }
			        catch (Exception e)
			        {
			            e.printStackTrace();
			        }
			    }
			}
		}
		return props;
	}

	public static String getQuery(String query) throws SQLException{
		return getQueries().getProperty(query);
	}

}