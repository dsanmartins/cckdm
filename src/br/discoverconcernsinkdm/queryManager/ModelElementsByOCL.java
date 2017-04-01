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
import java.util.Iterator;
import java.util.List;
import java.util.HashSet;

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
import org.eclipse.gmt.modisco.omg.kdm.code.AbstractCodeRelationship;
import org.eclipse.gmt.modisco.omg.kdm.code.ClassUnit;
import org.eclipse.gmt.modisco.omg.kdm.code.CodeModel;
import org.eclipse.gmt.modisco.omg.kdm.code.Imports;
import org.eclipse.gmt.modisco.omg.kdm.code.InterfaceUnit;
import org.eclipse.gmt.modisco.omg.kdm.code.MethodUnit;
import org.eclipse.gmt.modisco.omg.kdm.code.ParameterUnit;
import org.eclipse.gmt.modisco.omg.kdm.code.Signature;
import org.eclipse.gmt.modisco.omg.kdm.code.StorableUnit;
import org.eclipse.gmt.modisco.omg.kdm.code.impl.PackageImpl;
import org.eclipse.gmt.modisco.omg.kdm.code.Package;
import org.eclipse.gmt.modisco.omg.kdm.kdm.KDMModel;

import br.discoverconcernsinkdm.persistenceManager.ConnectionDB;
import br.discoverconcernsinkdm.persistenceManager.Query;
import br.discoverconcernsinkdm.queryManager.IQueries;

public class ModelElementsByOCL implements IQueries {

	KDMModel context;

	List<String> paramsPackage = new ArrayList<String>();
	List<String> paramsImports = new ArrayList<String>();
	List<String> paramsModule = new ArrayList<String>();
	List<String> paramsMethod = new ArrayList<String>();
	List<String> paramsProperty = new ArrayList<String>();
	List<String> paramsImp = new ArrayList<String>();
	String projectName = null;


	public ModelElementsByOCL(KDMModel k, String projectName)
	{
		this.projectName = projectName;
		context = k;
	}


	/* (non-Javadoc)
	 * @see br.discoverconcernsinkdm.queries.Queries#setModelPackages()
	 */
	@Override
	public void setModelPackages() throws Exception
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
			if (modelQuery.getName().equals("getPackagesOCL")) 
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
		Iterator<Package> it = ((HashSet <Package>) result.getValue()).iterator();


		ArrayList<String> temp = new ArrayList<String>();

		while (it.hasNext())
		{
			Package pkg = (Package) it.next();
			temp.add(pkg.getName());
		}

		HashSet<String> hs = new HashSet<String>();
		hs.addAll(temp);
		temp.clear();
		temp.addAll(hs);

		for (int i=0; i< temp.size(); i++)
		{
			paramsPackage.add(temp.get(i) +"|");
		}


		try
		{
			ConnectionDB.getInstance(projectName).addBatch(Query.getQuery("insertPackage"), paramsPackage);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		paramsPackage.clear();

	}

	/* (non-Javadoc)
	 * @see br.discoverconcernsinkdm.queries.Queries#setModelClasses()
	 */
	@Override
	public void setModelClasses() throws Exception 
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
			if (modelQuery.getName().equals("getClassOCL")) 
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
		Iterator<ClassUnit> it = ((HashSet <ClassUnit>) result.getValue()).iterator();

		ArrayList<String> temp = new ArrayList<String>();

		while (it.hasNext())
		{
			ClassUnit classe = (ClassUnit) it.next();
			if (classe.getAttribute().size() > 0 && !classe.getName().equals("Anonymous type"))
			{	
				if (classe.getAttribute().get(0).getTag().equals("export"))
				{
					int index = temp.indexOf(classe.getName());

					if (index == -1)
					{
						paramsModule.add(classe.getName() + "|" + "Class");
						//Insert Class Imports
						setImportClass(classe, null);

						//Insert Class - Package
						if (classe.eContainer() instanceof Package)
							paramsPackage.add(getContainer(classe.eContainer()) + "|" + classe.getName());
						else
							//Nested Class
							if (classe.eContainer() instanceof ClassUnit)
								paramsImp.add(getContainer(classe.eContainer()) + "|" + classe.getName());
						temp.add(classe.getName());
					}
				}
			}
		}

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
			ConnectionDB.getInstance(projectName).addBatch(Query.getQuery("insertPackageModule"), paramsPackage);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		paramsPackage.clear();

		//Insert nested Classes
		try
		{
			ConnectionDB.getInstance(projectName).addBatch(Query.getQuery("insertModuleModule"), paramsImp);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		paramsImp.clear();

	}


	private void setImportClass(ClassUnit classe, InterfaceUnit interfaceUnit) throws IOException
	{
		Imports imports = null;
		EObject packages = null;
		String namePackage = "";


		if (interfaceUnit == null)
		{
			EList<AbstractCodeRelationship> elements = classe.getCodeRelation();

			for (int i=0; i< elements.size(); i++)
			{
				if (elements.get(i) instanceof Imports)
				{
					imports = (Imports) elements.get(i);
					packages = imports.getTo().eContainer();
					while (packages instanceof PackageImpl)
					{
						namePackage =  ((PackageImpl) packages).getName() + "." + namePackage;
						packages = packages.eContainer();
					}
					namePackage = namePackage + imports.getTo().getName();

					if (namePackage.startsWith("java") || namePackage.startsWith("javax"))
						paramsImports.add(classe.getName() + "|" + namePackage);

					namePackage = "";
				}
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
		else
		{
			if (classe == null)
			{
				EList<AbstractCodeRelationship> elements = interfaceUnit.getCodeRelation();

				for (int i=0; i< elements.size(); i++)
				{
					if (elements.get(i) instanceof Imports)
					{
						imports = (Imports) elements.get(i);
						packages = imports.getTo().eContainer();
						while (packages instanceof PackageImpl)
						{
							namePackage =  ((PackageImpl) packages).getName() + "." + namePackage;
							packages = packages.eContainer();
						}
						namePackage = namePackage + imports.getTo().getName();

						if (namePackage.startsWith("java") || namePackage.startsWith("javax"))
							paramsImports.add(interfaceUnit.getName() + "|" + namePackage);

						namePackage = "";
					}
				}

				//Inserts Interface - Imports
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
		}
	}

	/* (non-Javadoc)
	 * @see br.discoverconcernsinkdm.queries.Queries#setModelInterfaces()
	 */
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
					setImportClass(null,interfaceUnit);

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

		try
		{
			ConnectionDB.getInstance(projectName).addBatch(Query.getQuery("insertModuleModule"), paramsImp);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		paramsImp.clear();

	}

	/* (non-Javadoc)
	 * @see br.discoverconcernsinkdm.queries.Queries#setModelMethods()
	 */
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

	/* (non-Javadoc)
	 * @see br.discoverconcernsinkdm.queries.Queries#setCalls(org.eclipse.gmt.modisco.omg.kdm.action.BlockUnit)
	 */
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


	/* (non-Javadoc)
	 * @see br.discoverconcernsinkdm.queries.Queries#setModelProperties()
	 */
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
}
