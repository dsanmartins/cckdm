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

package br.discoverconcernsinkdm.miner;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.basex.query.QueryException;
import org.eclipse.core.runtime.IProgressMonitor;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import Jama.Matrix;
import au.com.bytecode.opencsv.CSVReader;
import br.discoverconcernsinkdm.concernManager.IXMLLibrary;
import br.discoverconcernsinkdm.concernManager.XMLLibrary;
import br.discoverconcernsinkdm.ioutilities.IUtilities;
import br.discoverconcernsinkdm.ioutilities.Utilities;
import br.discoverconcernsinkdm.persistenceManager.ConnectionDB;
import br.discoverconcernsinkdm.persistenceManager.Query;
import br.discoverconcernsinkdm.tagManager.Annotation;
import br.discoverconcernsinkdm.tagManager.KDMAnnotation;


public class CCMining implements IMiner
{
	List<String> params = new ArrayList<String>();
	IXMLLibrary xmlLibrary = new XMLLibrary();
	Random random = new Random();
	Matrix matrix;
	Matrix varMatrix;
	String log1 = "";
	String log2 = "";
	Date date = new Date();
	IUtilities utilities = new Utilities();

	public void mine(ArrayList<String> concerns, String projectName, String folder, boolean select, BigDecimal threshold, IProgressMonitor monitor, boolean cluster) throws IOException, SQLException, QueryException
	{	
		String concern = "";
		for (int i = 0; i< concerns.size(); i++)
		{
			concern = concerns.get(i);
			monitor.subTask("Mining by library, concern: " + concern + ".");
			List<String> centroids = mineByLibrary(concern, projectName, folder, select, threshold);

			if (!cluster)
			{
				monitor.subTask("Mining by cluster, concern: " + concern + ".");
				mineByCluster(projectName, folder, threshold, select, concern, centroids);	
			}
		}
		monitor.subTask("Generating logs.");
		utilities.saveLogs(log1,projectName,1);
		utilities.createLibraryCSV(projectName);
		if (!cluster)
		{
			utilities.saveLogs(log2,projectName,2);
			utilities.createClusterCSV(projectName);
		}

		//Java Annotations
		Annotation kdmAnnotation = new KDMAnnotation(folder + "/" + projectName +"/",   projectName + "_KDM" + ".xmi", "DBKDM");
		kdmAnnotation.javaAnnotation(projectName,concerns, folder + "/" + projectName +"/");

	}

	public List<String> mineByLibrary(String concern, String projectName, String folder, boolean select, BigDecimal threshold) throws IOException, SQLException, QueryException
	{
		ArrayList<String> arrayPackage = xmlLibrary.getPackages(folder + "/" + projectName, concern);
		ArrayList<String> arrayProperty = new ArrayList<String>();
		ArrayList<String> arrayMethod = new ArrayList<String>();

		log1 = log1 + "*******************************************" + "\n";
		log1 = log1 + "CCKDM LOG: LIBRARY " + "Date: "+ date.toString() + "\n";
		log1 = log1 + "*******************************************" + "\n";
		log1 = log1 + "CONCERN: "  + concern + "\n"; 
		log1 = log1 + "*******************************************" + "\n";


		for (int i = 0; i< arrayPackage.size(); i++)
		{
			ArrayList<String> arrayElement = xmlLibrary.getElements(folder + "/" + projectName, concern, arrayPackage.get(i));
			params.add(arrayPackage.get(i) + '%' );
			params.add(arrayPackage.get(i) + '%' );

			//Add elements of java package
			for (int j=0 ; j< arrayElement.size(); j++)
			{
				List<String> tmp = new ArrayList<String>();
				tmp.add(arrayElement.get(j));
				try
				{
					ConnectionDB.getInstance(projectName).executeUpdateInsert(Query.getQuery("insertTemporal"), tmp);
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
				tmp.clear();
			}

			//Property mine

			log1 = log1 + "PACKAGE NAME: " + arrayPackage.get(i) + "\n";
			log1 = log1 + "-------------------------------------------" + "\n";

			try
			{
				ResultSet rs =	ConnectionDB.getInstance(projectName).executeQuery(Query.getQuery("ccPropertyLibrary"), params);
				while (rs.next())
				{
					String module = rs.getString("MODULE");
					String method = rs.getString("METHOD");
					//String method_type = rs.getString("METHOD_TYPE");
					//String method_kind = rs.getString("METHOD_RTN");
					String property = rs.getString("PROPERTY");
					//String property_type = rs.getString("PROPERTY_TYPE");
					String property_kind = rs.getString("PROPERTY_KIND");

					log1 = log1 + "PROPERTY: " + property + "\n";
					log1 = log1 + "CLASS CONTAINER: " + module + "\n";

					//Annotation
					arrayProperty.add(module + "|" + method + "|" + property + "|" + property_kind);
					log1 = log1 + "METHOD CONTAINER: " + method + "\n" ;

					log1 = log1 +  "-------------------------------------------" + "\n";

				}
				rs.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
			params.clear();
			params.add(arrayPackage.get(i) + '%');

			//Method mine
			try
			{
				ResultSet rs =	ConnectionDB.getInstance(projectName).executeQuery(Query.getQuery("ccMethodLibrary"), params);
				while (rs.next())
				{
					String module = rs.getString("MODULE");
					String method = rs.getString("METHOD");
					//String method_type = rs.getString("METHOD_TYPE");
					//String method_kind = rs.getString("METHOD_KIND");


					if (select)
					{
						if (!method.startsWith("get") && !method.startsWith("set") )
						{
							if (method.length() > 1)
							{
								//Annotation
								arrayMethod.add(module + "|" + method);

								log1 = log1 + "METHOD: " + method + "\n";
								log1 = log1 + "CLASS CONTAINER: " + module + "\n";
								log1 = log1 +  "-------------------------------------------" + "\n";
							}
						}

					}
					else
					{
						if (method.length() > 1)
						{
							//Annotation
							arrayMethod.add(module + "|" + method);

							log1 = log1 + "METHOD: " + method + "\n";
							log1 = log1 + "CLASS CONTAINER: " + module + "\n";
							log1 = log1 +  "-------------------------------------------" + "\n";
						}
					}

				}
				rs.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}			

			try
			{
				ConnectionDB.getInstance(projectName).executeUpdateInsert(Query.getQuery("deleteTemporal"));
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
			arrayElement.clear();
			params.clear();
		}

		Annotation kdmAnnotation = new KDMAnnotation(folder + "/" + projectName +"/",  projectName + "_KDM" + ".xmi", "DBKDM");
		kdmAnnotation.storableUnitAnnotation(concern,arrayProperty);
		kdmAnnotation = new KDMAnnotation(folder + "/" + projectName +"/",  projectName + "_KDM" + ".xmi", "DBKDM");
		kdmAnnotation.methodUnitAnnotation(concern,arrayMethod, false);


		List<String> centroids = new ArrayList<String>();
		//Properties length > 1
		for (int i=0; i<arrayProperty.size(); i++)
			if (arrayProperty.get(i).split("\\|")[2].length() > 1)
				centroids.add(arrayProperty.get(i).split("\\|")[2]);

		//Methods  length > 1
		for (int i=0; i<arrayMethod.size(); i++)
		{
			String name1 = arrayMethod.get(i).split("\\|")[1]; 
			if (select)
			{
				if (!name1.startsWith("get") && !name1.startsWith("set"))
				{
					if (name1.length() > 1)
						centroids.add(name1);
				}

			}
			else
				if (name1.length() > 1)
					centroids.add(name1);
		}

		HashSet<String> hs = new HashSet<String>();
		hs.addAll(centroids);
		centroids.clear();
		centroids.addAll(hs);

		return centroids;
	}

	public void mineByCluster(String projectName, String folder, BigDecimal threshold, boolean select, String concern, List<String> centroids) throws IOException, SQLException, QueryException
	{
		List <String> stringSignature = new ArrayList<String>();
		List<String> strings = new ArrayList<String>();
		ArrayList<String> arrayProperty = new ArrayList<String>();
		ArrayList<String> arrayMethod = new ArrayList<String>();


		//Get Property Strings
		try
		{
			ResultSet rs =	ConnectionDB.getInstance(projectName).executeQuery(Query.getQuery("getAllProperties"));
			while (rs.next())
			{
				String module = rs.getString("IDMODULE");
				String method = rs.getString("METHOD");
				String property = rs.getString("IDPROPERTY");
				String property_kind = rs.getString("KIND");

				//Annotation
				stringSignature.add(module + "|" +  method + "|" + property + "|" + property_kind);
				strings.add(property);
			}
			rs.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		//Get Method Strings
		try
		{
			ResultSet rs;
			if (select)
				rs = ConnectionDB.getInstance(projectName).executeQuery(Query.getQuery("getAllMethodsFilter"));
			else
				rs = ConnectionDB.getInstance(projectName).executeQuery(Query.getQuery("getAllMethods"));

			while (rs.next())
			{
				String module = rs.getString("IDMODULE");
				String method = rs.getString("IDMETHOD");
				stringSignature.add(module + "|" + method);
				strings.add(method);
			}
			rs.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		String[] stringArray = new String[strings.size()];
		stringArray = strings.toArray(stringArray);
		matrix = new Matrix(centroids.size(),stringArray.length);

		//Get Values for the matrix
		getValues(stringArray,centroids);

		DecimalFormat df = new DecimalFormat("#.##");
		//Preparing cluster
		prepareCluster();

		log2 = log2 + "*******************************************" + "\n";
		log2 = log2 + "CCKDM LOG: K-MEANS " + "Date: "+ date.toString() + "\n";			 
		log2 = log2 + "*******************************************" + "\n";
		log2 = log2 + "CONCERN: "  + concern + " | " + "Threshold: " + threshold.toString() + "\n";

		for (int i = 0; i< varMatrix.getRowDimension(); i++)
		{
			log2 = log2 + "*******************************************" + "\n";
			log2 = log2 + "CLUSTER CENTROID: " + centroids.get(i) + "\n";
			log2 = log2 + "-------------------------------------------" + "\n";
			for (int j=0; j< varMatrix.getColumnDimension(); j++)
			{
				if (varMatrix.get(i, j) != -1 && varMatrix.get(i, j) >= threshold.doubleValue() && varMatrix.get(i, j) <= 0.9 ) 
				{
					if (stringSignature.get(j).split("\\|").length == 4)
					{
						log2 = log2 + "PROPERTY: " + stringArray[j] + " | "+ "L.V.: " + df.format(varMatrix.get(i, j)) + "\n";
						log2 = log2 + "CLASS CONTAINER: " + stringSignature.get(j).split("\\|")[0] + "\n" + 
								"METHOD CONTAINER: " + stringSignature.get(j).split("\\|")[1] + "\n";
						log2 = log2 + "-------------------------------------------" + "\n";
						arrayProperty.add(stringSignature.get(j));
					}
					else
					{
						if (stringSignature.get(j).split("\\|").length == 2)
						{
							log2 = log2 + "METHOD: " + stringArray[j] + " | "+  "L.V.: " + df.format(varMatrix.get(i, j)) + "\n";
							log2 = log2 + "CLASS CONTAINER: " + stringSignature.get(j).split("\\|")[0] + "\n";
							log2 = log2 + "-------------------------------------------" + "\n";
							arrayMethod.add(stringSignature.get(j));
						}
					}
				}
			}

		}
		log2 = log2 + "\n";


		//Annotation
		Annotation kdmAnnotation = new KDMAnnotation(folder + "/" + projectName +"/",  projectName + "_KDM" + ".xmi", "DBKDM");
		kdmAnnotation.storableUnitAnnotation(concern,arrayProperty);
		kdmAnnotation = new KDMAnnotation(folder + "/" + projectName +"/",  projectName + "_KDM" + ".xmi", "DBKDM");
		kdmAnnotation.methodUnitAnnotation(concern,arrayMethod,false);

	}

	private void prepareCluster()
	{
		varMatrix = matrix.copy();

		double var=0;
		int row = 0;

		for (int i=0; i< varMatrix.getColumnDimension(); i++)
		{
			for (int j=0; j<varMatrix.getRowDimension(); j++)
			{
				if (varMatrix.get(j, i) > var )
				{
					var = varMatrix.get(j, i);
					row = j;
				}
			}

			for (int k=0; k<varMatrix.getRowDimension(); k++ )
				if (row != k)
					varMatrix.set(k, i, -1);
			var = 0;
		}
	}

	private void getValues(String [] stringArray, List<String> centroids)
	{

		AbstractStringMetric levenshtein = new Levenshtein();
		//Add levenshtein values to the matrix.
		for (int i = 0; i< centroids.size(); i++)
		{
			float[] comparation = levenshtein.batchCompareSet(stringArray, centroids.get(i));
			for (int j=0; j< comparation.length; j++)
				matrix.set(i, j,comparation[j]);
		}
	}

	public void controlledAnnotating(String projectName, String folder,String path, ArrayList<String> selected) throws IOException, SQLException, QueryException
	{
		for (String select : selected)
		{
			File input = new File(path);
			int lineLength = 0;
			ArrayList<String> p = new ArrayList<String>();
			ArrayList<String> m = new ArrayList<String>();

			if (input.exists())
			{
				CSVReader reader = new CSVReader(new FileReader(path));
				String [] nextLine;

				while ((nextLine = reader.readNext()) != null)
				{
					lineLength = nextLine.length;

					if (nextLine[lineLength-1].equals("X") || nextLine[lineLength-1].equals("x"))
					{
						for (int i=0; i< lineLength; i++)
						{
							if (nextLine[i].split("\\|").length == 4)
							{
								String[] property = nextLine[i].split("\\|");
								System.out.println(property);
								if (property[2].equals("-"))
								{
									p.add(property[3] + "|" + property[2] + "|" + property[1]+ "|" + "static");
									p.add(property[3] + "|" + property[2] + "|" + property[1]+ "|" + "global");
								}
								else
								{
									p.add(property[3] + "|" + property[2] + "|" + property[1]+ "|" + "local");
									p.add(property[3] + "|" + property[2] + "|" + property[1]+ "|" + "static");
								}	
							}
							else
							{
								if (nextLine[i].split("\\|").length == 3)
								{
									String[] method = nextLine[i].split("\\|");
									m.add(method[2] + "|" + method[1]); 
								}
							}	
						}
					}
				}

				HashSet<String> hs = new HashSet<String>();

				hs.addAll(p);
				p.clear();
				p.addAll(hs);
				hs.clear();

				hs.addAll(m);
				m.clear();
				m.addAll(hs);
				hs.clear();

				//Annotation
				Annotation kdmAnnotation = new KDMAnnotation(folder + "/" + projectName +"/",  projectName + "_KDM" + ".xmi","DBKDM");
				kdmAnnotation.annotationRemove(p, m, select);

				p.clear();
				m.clear();
				reader.close();
			}
		}
	}

}
