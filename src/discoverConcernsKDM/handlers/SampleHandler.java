package discoverConcernsKDM.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import br.discoverconcernsinkdm.ioutilities.IUtilities;
import br.discoverconcernsinkdm.ioutilities.Utilities;


/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class SampleHandler  extends AbstractHandler implements IHandler {
	/**
	 * The constructor.
	 */
	public SampleHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {

		IWorkbench wb = PlatformUI.getWorkbench(); 
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow(); 

		FileDialog fd = new FileDialog(win.getShell(), SWT.OPEN);
		fd.setText("Open");
		String folder = "/";
		fd.setFilterPath(folder);
		String[] filterExt = {"*.xmi"};
		fd.setFilterExtensions(filterExt);
		String pathControll = fd.open();

		if (pathControll != null)
		{
			String[] name = pathControll.split("\\/");
			String fileName = name[name.length-1].substring(0, name[name.length-1].length() - 4);

			//Create Project
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IPath myPath = new Path(fileName);
			if (!root.exists(myPath))
			{
				IProject project = root.getProject(fileName);
				try {

					project.create(null);
					project.open(null);

					//Java Project Nature
					IProjectDescription description;
					description = project.getDescription();
					description.setNatureIds(new String[] { JavaCore.NATURE_ID });
					project.setDescription(description, null);

					//Create Project
					IJavaProject javaProject = JavaCore.create(project);

					//Copy Files
					IUtilities ut = new Utilities();
					ut.copyKDMFile(pathControll, javaProject.getPath().toString(), fileName);

				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else
			{
				MessageDialog.openInformation(win.getShell(),"CCKDM", "The Project already exists!, please delete it.");
			}

		}




		return null;
	}

}
