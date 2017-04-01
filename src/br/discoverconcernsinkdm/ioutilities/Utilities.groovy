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

package br.discoverconcernsinkdm.ioutilities

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import br.discoverconcernsinkdm.ioutilities.IUtilities;

class Utilities implements IUtilities
{
	IWorkspace workspace = ResourcesPlugin.getWorkspace();


	/* (non-Javadoc)
	 * @see br.discoverconcernsinkdm.utilities.IUtilities#createLibraryCSV(java.lang.String)
	 */
	@Override
	public void createLibraryCSV(String projectName)
	{
		String folder = workspace.getRoot().getLocation().toFile().getPath().toString();
		String file = new File(folder + "/" + projectName + "/" + "LibraryLog.log").text;
		def out = new File(folder + "/" + projectName + "/" + 'Library.csv')
		Collection<String> list1 = new ArrayList<String>();
		Collection<String> list2 = new ArrayList<String>();
		String log="";
		

		String[] str = file.split("CCKDM LOG");


		String concernName="";


		for (int j = 0; j< str.length; j++)
		{
			str[j].eachLine {line->

				String[] process = line.split(" ");

				if (process[0].equals("CONCERN:"))
					concernName = process[1];

				if (process[0].equals("PROPERTY:"))
					list1.add(process[1]);

				if (process[0].equals("METHOD:"))
					list2.add(process[1]);
			}

			list1.sort();
			list2.sort();

			String name="";
			if (!concernName.equals(""))
				log = log + "** " + concernName + " **" + "\n";
			for (int i=0; i< list1.size(); i++)
			{
				if (!list1.getAt(i).equals(name))
				{
					name = list1.getAt(i);
					log = log + name +"," +list1.count(name)+"\n";
				}
			}

			name="";

			for (int i=0; i< list2.size(); i++)
			{
				if (!list2.getAt(i).equals(name))
				{
					name = list2.getAt(i);
					log = log + name +"," +list2.count(name)+"\n";
				}
			}

			log = log + "\n";
			list1.clear();
			list2.clear();
		}
		out.write(log);
	}

	/* (non-Javadoc)
	 * @see br.discoverconcernsinkdm.utilities.IUtilities#createClusterCSV(java.lang.String)
	 */
	@Override
	public void createClusterCSV(String projectName)
	{
		String folder = workspace.getRoot().getLocation().toFile().getPath().toString();
		String fileContents = new File(folder + "/" + projectName + "/" + "ClusterLog.log").text;
		def out = new File(folder + "/" + projectName + "/" + 'Cluster.csv')

		String[] str = fileContents.split("CCKDM LOG");
		String concernName="";
		String log = "";

		for (int z=0; z<str.length; z++)
		{
			str[z].eachLine {line->
				String[] process = line.split(" ");
				if (process[0].equals("CONCERN:"))
					concernName = process[1];
			}
			if (!concernName.equals(""))
			{
				log = log + "\n";
				log = log + "** "+ concernName  +" **" + "\n";
			}
			String [] r = str[z].split("\\-------------------------------------------");
			Collection<String> collections = new ArrayList<String>();
			int param = 0;
			for (int i=0; i< r.length; i++)
			{
				if (r[i].contains("CLUSTER CENTROID"))
				{
					if (!collections.empty)
					{
						collections.sort();
						String concatena=",";
						for (int j=0; j< collections.size(); j++)
						{
							String t = collections.getAt(j).split("\\|")[0];
							t = t.charAt(2);
							int n = Integer.parseInt(t);
							if (1 - n >= 0)
							{
								concatena = concatena + collections.getAt(j).substring(3,collections.getAt(j).length()) + ",";
								log = log + concatena;
							}
							else
								log = log +",,";

							if (2 - n >= 0)
							{
								concatena = collections.getAt(j).substring(3,collections.getAt(j).length()) + ",";
								log = log + (concatena);
							}
							else
								log = log + (",");
							if (3 - n >= 0)
							{
								concatena = collections.getAt(j).substring(3,collections.getAt(j).length()) + ",";
								log = log + (concatena);
							}
							else
								log = log + (",");

							if (4 - n >= 0)
							{
								concatena = collections.getAt(j).substring(3,collections.getAt(j).length()) + ",";
								log = log + (concatena);
							}
							else
								log = log + (",");
							if (5 - n >= 0)
							{
								concatena = collections.getAt(j).substring(3,collections.getAt(j).length())+ ",";
								log = log + (concatena);
							}
							else
								log = log + (",");
							if (6 - n >= 0)
							{
								concatena = collections.getAt(j).substring(3,collections.getAt(j).length()) + ",";
								log = log + (concatena);
							}
							else
								log = log + (",");
							if (7 - n >= 0)
							{
								concatena = collections.getAt(j).substring(3,collections.getAt(j).length()) + ",";
								log = log + (concatena);
							}
							else
								log = log + (",");
							if (8 - n >= 0)
							{
								concatena = collections.getAt(j).substring(3,collections.getAt(j).length()) + ",";
								log = log + (concatena);
							}
							else
								log = log + (",");
							if (9 - n >= 0)
							{
								concatena = collections.getAt(j).substring(3,collections.getAt(j).length()) + ",";
								log = log + (concatena);
							}
							else
								log = log + (",");
							log = log + ("\n");
						}
						collections.clear();
					}
					int value = r[i].indexOf("CLUSTER CENTROID");
					String centroid = r[i].substring(value, r[i].length()).split(" ")[2];
					centroid = centroid + "," + "0.1" + "," + "0.2" + "," + "0.3" + "," + "0.4" + "," + "0.5" + "," + "0.6" + "," + "0.7" + "," + "0.8" + "," + "0.9";
					centroid = centroid.replaceAll("[\n\r]","");
					log = log + (centroid + "\n");
				}
				if(r[i].contains("PROPERTY:"))
				{
					String[] props = r[i].split(" ");
					String value = props[4].substring(0, 3).replaceAll(",", ".") + "|" + props[1] + "|" + props[8] + "|" + props[6];
					value = value.replaceAll("[\n\r]","");
					int val = value.indexOf("METHOD");
					try
					{
						value = value.substring(0,val);
					}
					catch(StringIndexOutOfBoundsException e)
					{
						e.printStackTrace();
					}
					collections.add(value);
				}
				else
				{
					if(r[i].contains("METHOD:"))
					{
						String[] props = r[i].split(" ");
						String value = props[4].substring(0, 3).replaceAll(",", ".") + "|" + props[1] + "|" + props[6];
						value = value.replaceAll("[\n\r]","");
						collections.add(value);
					}
				}
			}
			if (!collections.empty)
			{
				collections.sort();
				String concatena=",";
				for (int j=0; j< collections.size(); j++)
				{
					String t = collections.getAt(j).split("\\|")[0];
					t = t.charAt(2);
					int n = Integer.parseInt(t);
					if (1 - n >= 0)
					{
						concatena = concatena + collections.getAt(j).substring(3,collections.getAt(j).length()) + ",";
						log = log + (concatena);
					}
					else
						log = log + (",,");
					if (2 - n >= 0)
					{
						concatena = collections.getAt(j).substring(3,collections.getAt(j).length()) + ",";
						log = log + (concatena);
					}
					else
						log = log + (",");

					if (3 - n >= 0)
					{
						concatena = collections.getAt(j).substring(3,collections.getAt(j).length()) + ",";
						log = log + (concatena);
					}
					else
						log = log + (",");

					if (4 - n >= 0)
					{
						concatena = collections.getAt(j).substring(3,collections.getAt(j).length()) + ",";
						log = log + (concatena);
					}
					else
						log = log + (",");

					if (5 - n >= 0)
					{
						concatena = collections.getAt(j).substring(3,collections.getAt(j).length())+ ",";
						log = log + (concatena);
					}
					else
						log = log + (",");

					if (6 - n >= 0)
					{
						concatena = collections.getAt(j).substring(3,collections.getAt(j).length()) + ",";
						log = log + (concatena);
					}
					else
						log = log + (",");

					if (7 - n >= 0)
					{
						concatena = collections.getAt(j).substring(3,collections.getAt(j).length()) + ",";
						log = log + (concatena);
					}
					else
						log = log + (",");

					if (8 - n >= 0)
					{
						concatena = collections.getAt(j).substring(3,collections.getAt(j).length()) + ",";
						log = log + (concatena);
					}
					else
						log = log + (",");

					if (9 - n >= 0)
					{
						concatena = collections.getAt(j).substring(3,collections.getAt(j).length()) + ",";
						log = log + (concatena);
					}
					else
						log = log + (",");

					log = log + ("\n");
				}
				collections.clear();
			}
		}
		out.write(log);
	}


	/* (non-Javadoc)
	 * @see br.discoverconcernsinkdm.utilities.IUtilities#copyInitialFile(java.lang.String)
	 */
	@Override
	public void copyInitialFile(String projectName)
	{
		String folder = workspace.getRoot().getLocation().toFile().getPath().toString();
		( new AntBuilder ( ) ).copy ( file : folder + "/" + projectName +"/" + projectName + "_KDM" + ".xmi"  ,
				tofile : folder + "/" + projectName +"/" + projectName + "0_KDM" + ".xmi" );
	}
	
	/* (non-Javadoc)
	 * @see br.discoverconcernsinkdm.utilities.IUtilities#copyKDMFile(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void copyKDMFile(String file, String path, String fileName)
	{
		String folder = workspace.getRoot().getLocation().toFile().getPath().toString();
		String fileContents = new File(file).text;
		new File(folder + path  + "/" + fileName + "_KDM.xmi").withWriter{ it << fileContents }
		copyInitialFile(fileName);
	}

	/* (non-Javadoc)
	 * @see br.discoverconcernsinkdm.utilities.IUtilities#copyFinalFile(java.lang.String)
	 */
	@Override
	public void copyFinalFile(String projectName)
	{
		String folder = workspace.getRoot().getLocation().toFile().getPath().toString();
		boolean fileSuccessfullyDeleted =  new File(folder + "/" + projectName +"/" + projectName + "_KDM" + ".xmi").delete();

		if (fileSuccessfullyDeleted)
		{
			( new AntBuilder ( ) ).copy ( file : folder + "/" + projectName +"/" + projectName + "0_KDM" + ".xmi"   ,
					tofile : folder + "/" + projectName +"/" + projectName + "_KDM" + ".xmi");
		}
	}

	/* (non-Javadoc)
	 * @see br.discoverconcernsinkdm.utilities.IUtilities#saveLogs(java.lang.String, java.lang.String, int)
	 */
	@Override
	public void saveLogs(String log, String projectName, int op)
	{
		String folder = workspace.getRoot().getLocation().toFile().getPath().toString();
		String nameFile = "";
		if (op == 1)
		{
			File f = new File(folder + "/" + projectName + "/" + "LibraryLog.log");
			f.write(log);
		}
		else
		if (op == 2)
		{
			File f = new File(folder + "/" + projectName + "/" + "ClusterLog.log");
			f.write(log);
		}
	}
	
	/* (non-Javadoc)
	 * @see br.discoverconcernsinkdm.utilities.IUtilities#getLicense()
	 */
	@Override
	public String getLicense() throws IOException
	{

		InputStream is = this.class.getClassLoader().getResourceAsStream("files/LICENSE");
		String f = is.text
		return f;	
	}

	/* (non-Javadoc)
	 * @see br.discoverconcernsinkdm.utilities.IUtilities#getLogs(java.lang.String, int)
	 */
	@Override
	public String getLogs(String projectName, int op) throws IOException
	{
		String folder = workspace.getRoot().getLocation().toFile().getPath().toString();
		String fileContents ="";
		if (op == 1)
		{
			File f = new File(folder + "/" + projectName + "/LibraryLog.log");
			if (!f.isFile())
			{
				//throw new IOException("Error creating new file: " + f.getAbsolutePath());
			}
			else
				fileContents = f.text;

		}
		else
		{
			File f = new File(folder + "/" + projectName + "/ClusterLog.log")
			if (!f.isFile())
			{
				//throw new IOException("Error creating new file: " + f.getAbsolutePath());
			}
			else
				fileContents = f.text;
		}
		return fileContents;
	}
}
