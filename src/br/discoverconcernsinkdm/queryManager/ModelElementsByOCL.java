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

package br.discoverconcernsinkdm.queryManager;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.iter.Iter;
import org.basex.query.value.item.Item;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmt.modisco.infra.query.ModelQuery;
import org.eclipse.gmt.modisco.infra.query.ModelQuerySet;
import org.eclipse.gmt.modisco.infra.query.core.AbstractModelQuery;
import org.eclipse.gmt.modisco.infra.query.core.ModelQuerySetCatalog;
import org.eclipse.gmt.modisco.infra.query.runtime.ModelQueryResult;
import org.eclipse.gmt.modisco.omg.kdm.action.BlockUnit;
import org.eclipse.gmt.modisco.omg.kdm.action.Calls;
import org.eclipse.gmt.modisco.omg.kdm.code.AbstractCodeElement;
import org.eclipse.gmt.modisco.omg.kdm.code.ClassUnit;
import org.eclipse.gmt.modisco.omg.kdm.code.CodeModel;
import org.eclipse.gmt.modisco.omg.kdm.code.InterfaceUnit;
import org.eclipse.gmt.modisco.omg.kdm.code.MethodUnit;
import org.eclipse.gmt.modisco.omg.kdm.code.Package;
import org.eclipse.gmt.modisco.omg.kdm.code.ParameterUnit;
import org.eclipse.gmt.modisco.omg.kdm.code.Signature;
import org.eclipse.gmt.modisco.omg.kdm.code.StorableUnit;
import org.eclipse.gmt.modisco.omg.kdm.kdm.KDMModel;

import br.discoverconcernsinkdm.persistenceManager.ConnectionDB;
import br.discoverconcernsinkdm.persistenceManager.Query;

public class ModelElementsByOCL implements IQueries {

	KDMModel context;

	List<String> paramsPackage = new ArrayList<String>();
	List<String> paramsModule = new ArrayList<String>();
	List<String> paramsPackageModule = new ArrayList<String>();
	List<String> paramsImports = new ArrayList<String>();
	List<String> paramsMethod = new ArrayList<String>();
	List<String> paramsProperty = new ArrayList<String>();
	List<String> paramsImp = new ArrayList<String>();
	String projectName = null;
	String folder = null;

	public ModelElementsByOCL(KDMModel k, String folder, String projectName)
	{
		this.projectName = projectName;
		context = k;
		this.folder = folder;
	}

	@Override
	public void setModelClasses() throws Exception 
	{
		IXQueryEngine iQuery = new XQueryEngine(folder + "/" + projectName +"/",   projectName + "_KDM" + ".xmi", "DBKDM");
		iQuery.openDB();
		String query = Query.getQuery("getPathClasses");
		QueryProcessor proc = new QueryProcessor(query, iQuery.getContext());
		Iter iter = proc.iter();
		String rtn = ""; 
		ArrayList<String> classList = new ArrayList<String>();
		for(Item item; (item = iter.next()) != null;) 
		{
			rtn = item.toJava().toString();
			rtn = rtn.split("model")[1].replaceAll("Q\\{\\}", "");
			rtn = "//model"+rtn;
			classList.add(rtn);
		}
		proc.close();

		query = Query.getQuery("dynamicQuery");
		for (String r : classList)
		{
			query = query + " let $a:= " + r + " return data($a/@name) ";
			proc = new QueryProcessor(query, iQuery.getContext());
			iter = proc.iter();
			rtn = ""; 
			for(Item item; (item = iter.next()) != null;)
				rtn = item.toJava().toString();
		
			//Package of a class
			String packageName = this.getPackageName(r,iQuery.getContext());
			paramsPackage.add(packageName +"|");
			Set<String> set1 = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
			set1.addAll(paramsPackage);
			paramsPackage = new ArrayList<String>(set1);
	
			//Class
			paramsModule.add(rtn + "|" + "Class");
			Set<String> set2 = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
			set2.addAll(paramsModule);
			paramsModule = new ArrayList<String>(set2);
			
			//Package and Module
			paramsPackageModule.add(packageName + "|" + rtn);
			Set<String> set3 = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
			set3.addAll(paramsPackageModule);
			paramsPackageModule = new ArrayList<String>(set3);
			
			query = "";
			proc.close();
			
			//Imports of a class
			setImportClass(rtn, iQuery.getContext());
		}
		iQuery.closeDB();
		iQuery.dropDB();
		iQuery.getContext().close();

		//Insert Packages
		try
		{
			ConnectionDB.getInstance(projectName).addBatch(Query.getQuery("insertPackage"), paramsPackage);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		paramsPackage.clear();
		
		//Insert Modules
		try
		{
			ConnectionDB.getInstance(projectName).addBatch(Query.getQuery("insertModule"), paramsModule);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		paramsModule.clear();

		//Insert Module - Package
		try
		{
			ConnectionDB.getInstance(projectName).addBatch(Query.getQuery("insertPackageModule"), paramsPackageModule);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		paramsPackageModule.clear();

	}

	private void setImportClass(String classe, Context context) throws IOException, SQLException, QueryException
	{
		String query = Query.getQuery("getImportsPath");
		QueryProcessor proc = new QueryProcessor(query, context);
		proc.bind("class", classe);
		Iter iter = proc.iter();
		String rtn = ""; 
		ArrayList<String> importList = new ArrayList<String>();
		for(Item item; (item = iter.next()) != null;) 
		{
			rtn = item.toJava().toString();
			rtn = rtn.replace("/0/", "//");
			rtn = "\"" + rtn + "\"" ;
			importList.add(getTransformationPath(rtn));
		}
		proc.close();
		for (String path: importList)
		{
			query = Query.getQuery("getImportsName");
			query = query + " " + path +"/data(@name)";
			proc = new QueryProcessor(query, context);
			iter = proc.iter();
			rtn = ""; 
			for(Item item; (item = iter.next()) != null;)
			{
				rtn = item.toJava().toString();
				paramsImports.add(classe + "|" + rtn);
			}
			proc.close();

		}	
		//Insert Imports Class
		try
		{
			ConnectionDB.getInstance(projectName).addBatch(Query.getQuery("insertImports"), paramsImports);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		paramsImports.clear();
	}

	private void setImportInterface(InterfaceUnit interfaceUnit) throws IOException, SQLException, QueryException
	{
		IXQueryEngine iQuery = new XQueryEngine(folder + "/" + projectName +"/",   projectName + "_KDM" + ".xmi", "DBKDM");
		iQuery.openDB();
		String query = Query.getQuery("getImportsPath");
		QueryProcessor proc = new QueryProcessor(query, iQuery.getContext());
		proc.bind("class", interfaceUnit.getName());
		Iter iter = proc.iter();
		String rtn = ""; 
		ArrayList<String> importList = new ArrayList<String>();
		for(Item item; (item = iter.next()) != null;) 
		{
			rtn = item.toJava().toString();
			rtn = rtn.replace("/0/", "//");
			rtn = "\"" + rtn + "\"" ;
			importList.add(getTransformationPath(rtn));
		}
		proc.close();
		for (String path: importList)
		{
			query = Query.getQuery("getImportsName");
			query = query + " " + path +"/data(@name)";
			proc = new QueryProcessor(query, iQuery.getContext());
			iter = proc.iter();
			rtn = ""; 
			for(Item item; (item = iter.next()) != null;)
			{
				rtn = item.toJava().toString();
				paramsImports.add(interfaceUnit.getName() + "|" + rtn);
			}
			proc.close();

		}	
		iQuery.closeDB();
		iQuery.dropDB();
		iQuery.getContext().close();

		//Insert Imports Class
		try
		{
			ConnectionDB.getInstance(projectName).addBatch(Query.getQuery("insertImports"), paramsImports);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		paramsImports.clear();
	}

	@Override
	public void setModelInterfaces() throws Exception 
	{

		// Get the model query set catalog.
		ModelQuerySetCatalog catalog = ModelQuerySetCatalog.getSingleton(); 

		// Get the query set named "My".
		ModelQuerySet modelQuerySet = catalog.getModelQuerySet("QuerySetKDM");

		// Select in the "My" query set a query named "myQuery".
		// modelQueryDescription is a model element.
		ModelQuery modelQueryDescription = null;
		for (ModelQuery modelQuery : modelQuerySet.getQueries())
		{
			if (modelQuery.getName().equals("getInterfaceOCL")) 
			{
				modelQueryDescription = modelQuery;
				break;
			}
		}
		if (modelQueryDescription == null) 
		{
			throw new Exception();
		}

		//Get a java instance of the querySet
		AbstractModelQuery myModelQuery = catalog.getModelQueryImpl(modelQueryDescription);


		//the model query set evaluation
		ModelQueryResult result = myModelQuery.evaluate(context);
		if (result.getException() != null)
		{
			throw new Exception();
		}

		@SuppressWarnings("unchecked")
		Iterator<InterfaceUnit> it = ((HashSet<InterfaceUnit>) result.getValue()).iterator();

		ArrayList<String> temp = new ArrayList<String>();

		while (it.hasNext())
		{
			InterfaceUnit interfaceUnit = (InterfaceUnit) it.next();

			if (interfaceUnit.getAttribute().size() > 0)
			{
				if (interfaceUnit.getAttribute().get(0).getTag().equals("export"))
				{
					paramsModule.add(interfaceUnit.getName() + "|" + "Interface");

					//Insert Class Imports
					setImportInterface(interfaceUnit);

					if (interfaceUnit.eContainer() instanceof Package)
						//Insert Class - Package
						paramsPackage.add(getContainer(interfaceUnit.eContainer()) + "|" + interfaceUnit.getName());
					else
						//Nested Class
						if (interfaceUnit.eContainer() instanceof ClassUnit)
							//Insert Class - Package
							paramsImp.add(getContainer(interfaceUnit.eContainer()) + "|" +interfaceUnit.getName());
					temp.add(interfaceUnit.getName());
				}
			}
		}

		//Inserts
		try
		{
			ConnectionDB.getInstance(projectName).addBatch(Query.getQuery("insertModule"), paramsModule);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		paramsModule.clear();

		try
		{
			ConnectionDB.getInstance(projectName).addBatch(Query.getQuery("insertPackageModule"), paramsPackage);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		paramsPackage.clear();
	}

	@Override
	public BlockUnit setModelMethods() throws Exception 
	{

		// Get the model query set catalog.
		ModelQuerySetCatalog catalog = ModelQuerySetCatalog.getSingleton(); 

		// Get the query set named "My".
		ModelQuerySet modelQuerySet = catalog.getModelQuerySet("QuerySetKDM");

		// Select in the "My" query set a query named "myQuery".
		// modelQueryDescription is a model element.
		ModelQuery modelQueryDescription = null;
		for (ModelQuery modelQuery : modelQuerySet.getQueries())
		{
			if (modelQuery.getName().equals("getMethodsOCL")) 
			{
				modelQueryDescription = modelQuery;
				break;
			}
		}
		if (modelQueryDescription == null) 
		{
			throw new Exception();
		}

		//Get a java instance of the querySet
		AbstractModelQuery myModelQuery = catalog.getModelQueryImpl(modelQueryDescription);


		//the model query set evaluation
		ModelQueryResult result = myModelQuery.evaluate(context);
		if (result.getException() != null)
		{
			throw new Exception();
		}

		@SuppressWarnings("unchecked")
		Iterator<MethodUnit> it = ((HashSet<MethodUnit>) result.getValue()).iterator();

		MethodUnit method = null;
		BlockUnit blockUnit = null;
		Signature signature = null;
		ParameterUnit paramUnit = null;
		int check = 0;

		while (it.hasNext())
		{
			method = (MethodUnit) it.next();
			if (method.getAttribute().size() > 0)
			{

				if (method.getAttribute().get(0).getTag().equals("export"))
				{
					String name = method.getName(); 
					String module = getContainer(method.eContainer());
					String type = method.getKind().getName();
					String visibility = method.getExport().getName();
					String rtn = "";


					EList<AbstractCodeElement> elements = method.getCodeElement();
					for (int i=0 ; i<elements.size() ; i++)
						if (elements.get(i) instanceof Signature )
							signature = (Signature) elements.get(i);


					String sign = "";
					EList<ParameterUnit> parameterUnit = signature.getParameterUnit();
					for (int i=0 ; i<parameterUnit.size() ; i++)
					{
						if (parameterUnit.get(i) instanceof ParameterUnit )
						{
							paramUnit = (ParameterUnit) parameterUnit.get(i);
							if (paramUnit.getKind().getName().equals("return"))
								rtn = paramUnit.getType().getName();

							if(paramUnit.getName() != null)
							{
								sign = sign + paramUnit.getType().getName() + "-" + paramUnit.getName() + "-";
							}

						}
					}

					if (sign.length() == 0)
						sign = "void";

					paramsMethod.add(name + "|" + module + "|" + sign + "|" + type + "|" + visibility + "|" + rtn);

					if (check == 0)
					{
						elements = method.getCodeElement();
						for (int i=0 ; i<elements.size() ; i++)
							if (elements.get(i) instanceof BlockUnit )
							{
								blockUnit = (BlockUnit) elements.get(i);
								check = 1;
							}
					}
				}
			}
		}

		try
		{
			ConnectionDB.getInstance(projectName).addBatch(Query.getQuery("insertMethod"), paramsMethod);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		paramsMethod.clear();

		return blockUnit;
	}

	@Override
	public void setCalls(BlockUnit blockUnit) throws Exception 
	{

		// Get the model query set catalog.
		ModelQuerySetCatalog catalog = ModelQuerySetCatalog.getSingleton(); 

		// Get the query set named "My".
		ModelQuerySet modelQuerySet = catalog.getModelQuerySet("QuerySetKDM");

		// Select in the "My" query set a query named "myQuery".
		// modelQueryDescription is a model element.
		ModelQuery modelQueryDescription = null;
		for (ModelQuery modelQuery : modelQuerySet.getQueries())
		{
			if (modelQuery.getName().equals("getCallsOCL")) 
			{
				modelQueryDescription = modelQuery;
				break;
			}
		}
		if (modelQueryDescription == null) 
		{
			throw new Exception();
		}

		//Get a java instance of the querySet
		AbstractModelQuery myModelQuery = catalog.getModelQueryImpl(modelQueryDescription);


		//the model query set evaluation
		ModelQueryResult result = myModelQuery.evaluate(blockUnit);
		if (result.getException() != null)
		{
			throw new Exception();
		}

		@SuppressWarnings("unchecked")
		Iterator<Calls> it = ((HashSet<Calls>) result.getValue()).iterator();


		while (it.hasNext())
		{
			boolean hasRow = false;
			Calls call = (Calls) it.next();

			try
			{
				if (call.getFrom().getName().equals("method invocation"))
				{
					String nameMethod = call.getTo().getName();
					String container = getContainer(call.getTo());

					String caller = getContainerMethod(call.getFrom());
					String containerCaller = getContainer(call.getFrom());

					if (!(containerCaller.equals("CodeModel")))
					{	

						//Get Method Signature
						Signature signature =null;
						ParameterUnit paramUnit = null;
						EList<AbstractCodeElement> elements = call.getTo().getCodeElement();
						for (int i=0 ; i<elements.size() ; i++)
						{
							if (elements.get(i) instanceof Signature )
								signature = (Signature) elements.get(i);
						}

						String sign1 = "";
						EList<ParameterUnit> parameterUnit = signature.getParameterUnit();
						for (int i=0 ; i<parameterUnit.size() ; i++)
						{
							if (parameterUnit.get(i) instanceof ParameterUnit )
							{
								paramUnit = (ParameterUnit) parameterUnit.get(i);
								if(paramUnit.getName() != null)
								{
									sign1 = sign1 + paramUnit.getType().getName() + "-" + paramUnit.getName() + "-";
								}

							}
						}

						if (sign1.length() == 0)
							sign1 = "void";

						//Get Method Signature
						Signature signature1 =null;
						ParameterUnit paramUnit1 = null;


						MethodUnit mu = getContainerMethodUnit( call.getFrom());

						EList<AbstractCodeElement> elements1 = mu.getCodeElement();
						for (int i=0 ; i<elements1.size() ; i++)
						{
							if (elements1.get(i) instanceof Signature )
								signature1 = (Signature) elements1.get(i);
						}
						String sign2 = "";
						EList<ParameterUnit> parameterUnit1 = signature1.getParameterUnit();
						for (int i=0 ; i<parameterUnit1.size() ; i++)
						{
							if (parameterUnit1.get(i) instanceof ParameterUnit )
							{
								paramUnit1 = (ParameterUnit) parameterUnit1.get(i);
								if(paramUnit1.getName() != null)
								{
									sign2 = sign2 + paramUnit1.getType().getName() + "-" + paramUnit1.getName() + "-";
								}

							}
						}

						if (sign2.length() == 0)
							sign2 = "void";

						paramsImp.add(container);
						paramsImp.add(nameMethod);
						paramsImp.add(sign1);

						try
						{
							ResultSet rs =	ConnectionDB.getInstance(projectName).executeQuery(Query.getQuery("selectMethod"), paramsImp);
							while (rs.next())
							{
								int count = rs.getInt("COUNT");
								if (count > 0)
									hasRow = true;
							}
							rs.close();

						}
						catch (SQLException e)
						{
							e.printStackTrace();
							System.out.println(e.getMessage());
						}
						paramsImp.clear();

						if (hasRow)
							paramsMethod.add(container + "|" + nameMethod + "|" + sign1 + "|" + containerCaller + "|" + caller + "|" + sign2);
					}
				}
			}
			catch(NullPointerException e)
			{
				e.printStackTrace();
			}
		}

		try
		{
			ConnectionDB.getInstance(projectName).addBatch(Query.getQuery("insertCalls"), paramsMethod);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		paramsMethod.clear();
	}

	@Override
	public void setModelProperties() throws Exception 
	{

		// Get the model query set catalog.
		ModelQuerySetCatalog catalog = ModelQuerySetCatalog.getSingleton(); 

		// Get the query set named "My".
		ModelQuerySet modelQuerySet = catalog.getModelQuerySet("QuerySetKDM");

		// Select in the "My" query set a query named "myQuery".
		// modelQueryDescription is a model element.
		ModelQuery modelQueryDescription = null;
		for (ModelQuery modelQuery : modelQuerySet.getQueries())
		{
			if (modelQuery.getName().equals("getPropertiesOCL")) 
			{
				modelQueryDescription = modelQuery;
				break;
			}
		}
		if (modelQueryDescription == null) 
		{	
			throw new Exception();
		}

		//Get a java instance of the querySet
		AbstractModelQuery myModelQuery = catalog.getModelQueryImpl(modelQueryDescription);


		//the model query set evaluation
		ModelQueryResult result = myModelQuery.evaluate(context);
		if (result.getException() != null)
		{
			throw new Exception();
		}

		@SuppressWarnings("unchecked")
		Iterator<StorableUnit> it = ((HashSet<StorableUnit>) result.getValue()).iterator();



		while (it.hasNext())
		{
			StorableUnit property = (StorableUnit) it.next();

			if (property.getAttribute().size() > 0)
			{

				if (property.getAttribute().get(0).getTag().equals("export"))
				{
					String name = property.getName();
					String module = getContainer(property.eContainer());
					String method = getContainerMethod(property.eContainer());
					String kind = null;
					String type = property.getType().getName();

					if (property.getKind().getName().equals("global"))
						kind = "global";
					else
						if (property.getKind().getName().equals("local"))
							kind = "local";
						else
							if (property.getKind().getName().equals("static"))
								kind = "static";


					if (method.equals("CodeModel"))
						paramsProperty.add(module + "|" + name + "|" + kind + "|" + type);
					else
					{	
						Signature signature = null;
						ParameterUnit paramUnit = null;
						EList<AbstractCodeElement> elements = (getContainerMethodUnit(property.eContainer())).getCodeElement();
						for (int i=0 ; i<elements.size() ; i++)
							if (elements.get(i) instanceof Signature )
								signature = (Signature) elements.get(i);


						String sign = "";
						EList<ParameterUnit> parameterUnit = signature.getParameterUnit();
						for (int i=0 ; i<parameterUnit.size() ; i++)
						{
							if (parameterUnit.get(i) instanceof ParameterUnit )
							{
								paramUnit = (ParameterUnit) parameterUnit.get(i);
								if(paramUnit.getName() != null)
								{
									sign = sign + paramUnit.getType().getName() + "-" + paramUnit.getName() + "-";
								}

							}
						}

						if (sign.length() == 0)
							sign = "void";

						paramsImp.add(module + "|" + method + "|" + sign + "|" + name + "|" + kind + "|" + type);						
					}
				}
			}
		}

		//Insert PropertyGlobal
		try
		{
			ConnectionDB.getInstance(projectName).addBatch(Query.getQuery("insertPropertyGlobal"), paramsProperty);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		paramsProperty.clear();

		//Insert PropertyLocal
		try
		{
			ConnectionDB.getInstance(projectName).addBatch(Query.getQuery("insertPropertyLocal"), paramsImp);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		paramsImp.clear();

	}

	private String getContainerMethod(EObject object)
	{
		//System.out.println(object.toString());
		if (!(object instanceof MethodUnit) && !(object instanceof CodeModel))
			return getContainerMethod(object.eContainer());
		else
		{
			if (object instanceof MethodUnit)
				return ((MethodUnit) object).getName();
			else
				if (object instanceof CodeModel)
					return ((CodeModel) object).eClass().getName();
				else
					return null;
		}
	}

	private MethodUnit getContainerMethodUnit(EObject object)
	{
		if (!(object instanceof MethodUnit))
			return getContainerMethodUnit(object.eContainer());
		else
		{
			if (object instanceof MethodUnit)
				return ((MethodUnit) object);
			else
				return null;
		}
	}

	private String getContainer(EObject object)
	{
		//System.out.println(object.toString());
		if (!(object instanceof ClassUnit) && !(object instanceof InterfaceUnit) && !(object instanceof CodeModel) && !(object instanceof Package))
			return getContainer(object.eContainer());
		else
		{
			if (object instanceof ClassUnit)
				return ((ClassUnit) object).getName();
			else
			{
				if (object instanceof InterfaceUnit)
					return ((InterfaceUnit) object).getName();
				else
				{
					if (object instanceof CodeModel)
						return ((CodeModel) object).eClass().getName();
					else
					{
						if (object instanceof Package)
							return ((Package) object).getName();
						else
							return null;
					}

				}
			}
		}
	}

	public String getTransformationPath(String str) throws SQLException, QueryException
	{
		String newStr ="";
		str = str.replaceAll("@", "");
		str = str.substring(3, str.length()-1);

		String line[] = str.split("\\/");
		for (int i = 0; i<line.length; i++)
		{
			String subline[] = line[i].split("\\.");
			newStr = newStr + subline[0]+"["+(Integer.parseInt(subline[1]) + 1) +"]/";
		}
		str = "//"+newStr.substring(0, newStr.length()-1);
		return str;
	}

	public String getPackageName(String token , Context context) throws SQLException, QueryException, BaseXException
	{
		token = token.substring(2);
		String tokens[] = token.split("\\/");
		String path = "//" + tokens[0];
		String pkgName = "";
		for (int i = 1 ; i<tokens.length; i++)
		{
			String query1 = Query.getQuery("dynamicQuery");
			path = path + "/" + tokens[i];
			query1 = query1 + " let $a:= " + path + " return data($a/@xsi:type) ";
			QueryProcessor proc = new QueryProcessor(query1, context);
			Iter iter = proc.iter();
			String rtn = ""; 
			for(Item item; (item = iter.next()) != null;)
				rtn = item.toJava().toString();
			proc.close();
			if (rtn.equals("code:Package"))
			{
				String query2 = Query.getQuery("dynamicQuery");
				query2 = " let $a:= " + path + " return data($a/@name) ";
				proc = new QueryProcessor(query2, context);
				iter = proc.iter();
				rtn = ""; 
				for(Item item; (item = iter.next()) != null;)
					rtn = item.toJava().toString();

				pkgName = pkgName + "." + rtn;
				proc.close();
			}
			else
				break;
		}
		return pkgName.substring(1);
	}
}
