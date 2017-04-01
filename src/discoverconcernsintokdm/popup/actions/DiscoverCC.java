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

package discoverconcernsintokdm.popup.actions;

import java.sql.ResultSet;

import org.eclipse.core.internal.resources.*;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import br.discoverconcernsinkdm.concernManager.IXMLLibrary;
import br.discoverconcernsinkdm.concernManager.XMLLibrary;
import br.discoverconcernsinkdm.swt.Main;



public class DiscoverCC implements IObjectActionDelegate {

	private Shell shell;
	private String projectName = null; //$NON-NLS-1$
	ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell); 
	ResultSet rs;
	IWorkspace workspace = ResourcesPlugin.getWorkspace();  



	/**
	 * Constructor for Action1.
	 */
	public DiscoverCC() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}



	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	@SuppressWarnings("restriction")
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection !=null)
		{
			if (selection instanceof IStructuredSelection)
			{
				IStructuredSelection ss = (IStructuredSelection) selection;
				Object obj = ss.getFirstElement();
				if (obj instanceof Project)	
				{
					Project iProject = (Project) obj;
					projectName = iProject.getName().toString();
					//iProject.getFullPath();
				}	
			}
		}
	}

	public void run(IAction action) 
	{
		// TODO Auto-generated method stub
		//Generate KDM file.

		//Load Library
		String folder= workspace.getRoot().getLocation().toFile().getPath().toString(); 
		IXMLLibrary xmlLibrary = new XMLLibrary();
		xmlLibrary.createInitialLibrary(folder + "/" + projectName);


		//Open Main Window
		Main cv = new Main(projectName);
		cv.setBlockOnOpen(true);
		cv.open();

	}
	

	public void dispose() {
		// TODO Auto-generated method stub

	}



}
