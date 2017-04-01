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


package br.discoverconcernsinkdm.concernManager

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jface.viewers.TreeViewer
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.xml.sax.SAXException;
import org.apache.commons.collections.CollectionUtils;

import br.discoverconcernsinkdm.concernManager.IXMLLibrary;

import au.com.bytecode.opencsv.CSVReader
import groovy.util.slurpersupport.GPathResult;
import groovy.xml.StreamingMarkupBuilder

public class XMLLibrary implements IXMLLibrary 
{
	public XMLLibrary()
	{


	}

	/* (non-Javadoc)
	 * @see br.discoverconcernsinkdm.librarymanagement.IXMLLibrary#writeXML(java.lang.String, java.lang.Object)
	 */
	@Override
	def writeXML(String xml, path)
	{
		def xmlFile = new File(path + '/ConcernLibrary.xml')
		xmlFile.write(xml);
	}

	/* (non-Javadoc)
	 * @see br.discoverconcernsinkdm.librarymanagement.IXMLLibrary#writeFile(java.lang.Object, java.lang.Object)
	 */
	@Override
	def writeFile(fileName, closure)
	{
		def xmlFile = new File(fileName)
		def writer = xmlFile.newWriter()

		def builder = new StreamingMarkupBuilder()
		def Writable writable = builder.bind closure
		writable.writeTo(writer)
	}

	/* (non-Javadoc)
	 * @see br.discoverconcernsinkdm.librarymanagement.IXMLLibrary#createInitialLibrary(java.lang.String)
	 */
	@Override
	def createInitialLibrary(String path) {

		if (!new File(path + '/ConcernLibrary.xml').exists())
		{
			def xmlClosure = {
				ConcernLibrary {
					Concern(name: 'Persistence') {
						Package(name: 'java.sql'){
							Element('Array')
							Element('Blob')
							Element('CallableStatement')
							Element('Clob')
							Element('Connection')
							Element('DatabaseMetaData')
							Element('ParameterMetaData')
							Element('PreparedStatement')
							Element('Connection')
							Element('ResultSet')
							Element('ResultSetMetaData')
							Element('SavePoint')
							Element('SQLData')
							Element('SQLInput')
							Element('SQLOutput')
							Element('Statement')
							Element('Struct')
							Element('Date')
							Element('DriverManager')
							Element('DriverPropertyInfo')
							Element('SQLPermission')
							Element('Time')
							Element('Timestamp')
							Element('Types')
							Element('BatchUpdateException')
							Element('DataTruncation')
							Element('SQLException')
							Element('SQLWarning')
						}
					}
					Concern(name: 'Logging'){
						Package(name: 'java.util.logging'){
							Element('Filter')
							Element('LoggingMXBean')
							Element('ConsoleHandler')
							Element('ErrorManager')
							Element('FileHandler')
							Element('Formatter')
							Element('Handler')
							Element('Level')
							Element('Logger')
							Element('LoggingPermission')
							Element('LogManager')
							Element('LogRecord')
							Element('MemoryHandler')
							Element('SimpleFormatter')
							Element('SocketHandler')
							Element('StreamHandler')
							Element('XMLFormatter')
						}
					}
					Concern(name: 'Authentication'){
						Package(name:'javax.security.auth'){
							Element('Destroyable')
							Element('Refreshable')
							Element('AuthPermission')
							Element('Policy')
							Element('PrivateCredentialPermission')
							Element('Subject')
							Element('SubjectDomainCombiner')
							Element('DestroyFailedException')
							Element('RefreshFailedException')
						}
						Package(name:'java.security'){
							Element('Principal')
						}
						Package(name:'javax.ejb'){
							Element('SessionBean')
							Element('SessionContext')
						}
					}
				}
			}
			writeFile(path + '/ConcernLibrary.xml', xmlClosure)
		}
	}

	/* (non-Javadoc)
	 * @see br.discoverconcernsinkdm.librarymanagement.IXMLLibrary#readLibrary(java.lang.String)
	 */
	@Override
	public ArrayList<String> readLibrary(String project)
	{
		ArrayList<String> arrayList = new ArrayList<String>();
		def library = new XmlSlurper().parse(new File(project + "/ConcernLibrary.xml"));
		def model = library.depthFirst();
		while (model.hasNext())
		{
			def root = model.next();
			if (root.name().equals("Concern"))
				arrayList.add(root.name() + "|" +root."@name");

			if (root.name().equals("Package"))
				arrayList.add(root.name() + "|" +root."@name");

			if (root.name().equals("Element"))
				arrayList.add(root.name() + "|" +root);
		}
		return arrayList;
	}

	/* (non-Javadoc)
	 * @see br.discoverconcernsinkdm.librarymanagement.IXMLLibrary#parseCSV(java.lang.String, java.lang.String)
	 */
	@Override
	public void parseCSV(String text, String path)
	{
		InputStream is = new ByteArrayInputStream( text.getBytes( "UTF-8" ));
		CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(is)));
		String [] nextLine;
		while ((nextLine = reader.readNext()) != null)
			addConcernXMLLibrary(nextLine, path);
	}

	private void addConcernXMLLibrary(String[] line, String path)
	{
		//Search concern name
		String concernName = line[0];
		GPathResult library = new XmlSlurper().parse(new File(path + "/ConcernLibrary.xml"));

		//Check if line is well formed
		if (line.length > 2)
		{
			String concernNameElement = null;
			library.Concern.findAll {it.@name.equals(concernName)}.each{ it ->

				concernNameElement = it.@name;
			}
			//Check Concern Name exists
			if (concernNameElement != null)
			{

				String packageName = line[1];
				String packageNameElement = null;
				library.Concern.Package.findAll {it.@name.equals(packageName)}.each{ it ->

					packageNameElement = it.@name;
				}

				//Check package name;
				if (packageNameElement != null)
				{
					def arrayListElement = new ArrayList();
					library.Concern.Package.find{it.@name == packageName}.Element.each{
						arrayListElement.add(it.text());
					}

					//Check elements
					if (arrayListElement.size() > 0)
					{
						ArrayList arrayElement = new ArrayList();
						for (int i = 2; i< line.length; i++)
							arrayElement.add(line[i]);

						List list = new ArrayList(CollectionUtils.subtract(arrayElement,arrayListElement));

						//Check if list has elements then add elements
						if (list.size() > 0)
						{
							def outputBuilder = new StreamingMarkupBuilder()
							def packages = library.'Concern'.'Package'.find{it.@name == packageName}
							String result;

							packages.appendNode{

								for (int i= 0; i< list.size(); i++)
								{
									Element(list.get(i));
								}
							}
							result = outputBuilder.bind{ mkp.yield library };
							new File(path + '/ConcernLibrary.xml').write(result);
						}
					}
				}
				else
				{
					//If package name is null then add the new package with their elements
					packageNameElement = line[1];
					ArrayList arrayElement = new ArrayList();
					for (int i = 2; i< line.length; i++)
						arrayElement.add(line[i]);

					def outputBuilder = new StreamingMarkupBuilder()
					def concerns = library.Concern.find {it.@name.equals(concernName)}

					concerns.appendNode{
						Package(name: packageNameElement){

							for (int i= 0; i< arrayElement.size(); i++)
							{
								Element(arrayElement.get(i));
							}
						}
					}
					String result = outputBuilder.bind{ mkp.yield library };
					new File(path + '/ConcernLibrary.xml').write(result);


				}
			}
			else
			{
				concernNameElement = line[0];
				String packageNameElement = line[1];

				ArrayList arrayElement = new ArrayList();
				for (int i = 2; i< line.length; i++)
					arrayElement.add(line[i]);

				//Add a new Concern with their elements
				library.appendNode{
					Concern(name: concernNameElement){
						Package(name: packageNameElement){
							for (int i= 0; i< arrayElement.size(); i++)
							{
								Element(arrayElement.get(i));
							}
						}
					}
				};

				def outputBuilder = new StreamingMarkupBuilder();
				String result = outputBuilder.bind{ mkp.yield library };
				new File(path + '/ConcernLibrary.xml').write(result);
			}
		}
	}

	/* (non-Javadoc)
	 * @see br.discoverconcernsinkdm.librarymanagement.IXMLLibrary#getConcerns(java.lang.String)
	 */
	@Override
	public ArrayList<String> getConcerns(String path)
	{
		ArrayList<String> concernName = new ArrayList<String>();
		GPathResult library = new XmlSlurper().parse(new File(path + "/ConcernLibrary.xml"));
		def concerns = library.'Concern'.findAll();
		for (int i= 0; i< concerns.size(); i++)
			concernName.add(concerns[i].@name.toString());
		return concernName;
	}

	/* (non-Javadoc)
	 * @see br.discoverconcernsinkdm.librarymanagement.IXMLLibrary#getPackages(java.lang.String, java.lang.String)
	 */
	@Override
	public ArrayList<String> getPackages(String path, String concern)
	{
		ArrayList<String> packageName = new ArrayList<String>();
		GPathResult library = new XmlSlurper().parse(new File(path + "/ConcernLibrary.xml"));
		def packages = library.Concern.find{it.@name == concern}.Package;
		packages.each{ it ->
			
			packageName.add(it.@name.toString());	
		}
		return packageName;
	}
	
	/* (non-Javadoc)
	 * @see br.discoverconcernsinkdm.librarymanagement.IXMLLibrary#getElements(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ArrayList<String> getElements(String path, String concern, String packageName)
	{
		ArrayList<String> elementName = new ArrayList<String>();
		GPathResult library = new XmlSlurper().parse(new File(path + "/ConcernLibrary.xml"));
		def packages = library.Concern.find{it.@name == concern}.Package;
		def pack = packages.find{it.@name == packageName} 
		
		pack.Element.each{ it ->
			
			elementName.add(it.text());			
		}
		return elementName;
	}
}
