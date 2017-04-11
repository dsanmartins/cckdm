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

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Export;
import org.basex.core.cmd.Open;

public class XQueryEngine implements IXQueryEngine  {

	Context context; 
	CreateDB createDB;
	String path;
	Open openDb;
	Close closeDb;
	DropDB dropDB;
	Export export;

	public XQueryEngine(String path, String file, String dbName) throws BaseXException
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

	public void createDB(String path, String file, String dbName) throws BaseXException {
		// TODO Auto-generated method stub
		context = new Context();
		createDB = new CreateDB(dbName, path + file);
		createDB.execute(context);
		openDb = new Open(dbName);
	}

	public void openDB() throws BaseXException
	{
		openDb.execute(context);
	}

	public void closeDB() throws BaseXException
	{
		closeDb.execute(context);
	}

	public void exportDB() throws BaseXException
	{
		export.execute(context);
	}

	public void dropDB() throws BaseXException
	{
		dropDB.execute(context);
	}
	
	public Context getContext()
	{
		return this.context;
	}
}
