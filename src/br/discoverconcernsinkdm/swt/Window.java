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

package br.discoverconcernsinkdm.swt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import org.basex.query.QueryException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.gmt.modisco.omg.kdm.action.BlockUnit;
import org.eclipse.gmt.modisco.omg.kdm.kdm.KDMModel;
import org.eclipse.gmt.modisco.omg.kdm.kdm.Segment;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.modisco.java.discoverer.DiscoverKDMModelFromJavaProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.SWTResourceManager;

import br.discoverconcernsinkdm.concernManager.IXMLLibrary;
import br.discoverconcernsinkdm.concernManager.XMLLibrary;
import br.discoverconcernsinkdm.ioutilities.IUtilities;
import br.discoverconcernsinkdm.ioutilities.Utilities;
import br.discoverconcernsinkdm.miner.CCMining;
import br.discoverconcernsinkdm.miner.IMiner;
import br.discoverconcernsinkdm.persistenceManager.Setup;

public class Window extends Composite {

	private Text text;
	ProgressMonitorDialog dialog = null;
	private Text text_1;
	private Text text_2;
	private Text text_3;
	private Text text_4;
	Setup setup;
	private Table table;
	private Text text_5;
	IXMLLibrary xmlLibrary = new XMLLibrary();
	IWorkspace workspace = ResourcesPlugin.getWorkspace();  
	final Shell shell; 
	String projectName = null;
	private Text text_6;
	final Tree tree;
	int x = 10;
	int y = 27;
	int cont = 0;
	String folder;
	ArrayList<String> selected = new ArrayList<String>(); 
	boolean select;
	boolean cluster;
	boolean controll;
	String pathControll;
	BigDecimal valueText;
	private Text text_8;
	private Text text_9;
	private Text text_10;
	private Text txtClickToLoad;
	private Text text_7;



	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 * @throws IOException 
	 */
	public Window(Composite parent, int style, final String projectName) throws IOException {

		super(parent, SWT.NO_REDRAW_RESIZE);
		this.projectName = projectName;
		setup = new Setup(this.projectName);
		dialog = new ProgressMonitorDialog(parent.getShell()); 
		//Calculate some metrics
		try 
		{
			dialog.run(true, true, new IRunnableWithProgress()
			{ 
				public void run(IProgressMonitor monitor) 
				{
					int totalUnitsOfWork = IProgressMonitor.UNKNOWN;
					monitor.beginTask("Loading project...wait.",totalUnitsOfWork); 
					Resource javaResource = initialSetup(projectName, monitor);
					monitor.setTaskName("Generating KDM file....wait.");
					try {
						generateKDMFile(javaResource);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					monitor.done(); 
				}
			});
		} 
		catch (InvocationTargetException ex) 
		{
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} 
		catch (InterruptedException ex) 
		{
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}


		shell = parent.getShell();
		shell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				try {
					setup.closeModel();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});


		setLayout(new GridLayout(1, false));

		Composite composite = new Composite(this, SWT.BORDER );
		composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
		composite.setForeground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
		GridData gd_composite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_composite.heightHint = 50;
		gd_composite.widthHint = 791;
		composite.setLayoutData(gd_composite);

		Label lblSearchCrosscuttingConcerns = new Label(composite, SWT.NONE);
		lblSearchCrosscuttingConcerns.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblSearchCrosscuttingConcerns.setBackground(SWTResourceManager.getColor(SWT.COLOR_DARK_BLUE));
		lblSearchCrosscuttingConcerns.setFont(SWTResourceManager.getFont("Sans", 18, SWT.NORMAL));
		lblSearchCrosscuttingConcerns.setBounds(170, 10, 562, 29);
		lblSearchCrosscuttingConcerns.setText("Search for Crosscutting Concerns into KDM");
		new Label(this, SWT.NONE);

		final TabFolder tabFolder = new TabFolder(this, SWT.NONE);
		GridData gd_tabFolder = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_tabFolder.widthHint = 783;
		gd_tabFolder.heightHint = 400;
		tabFolder.setLayoutData(gd_tabFolder);

		//Tab 1
		TabItem tab1 = new TabItem(tabFolder, SWT.NONE);
		tab1.setText("Mining of Crosscutting Concern");


		final Group group = new Group(tabFolder, SWT.NONE);


		Label label1 = new Label(group, SWT.NONE);
		label1.setText("Project Name:");
		label1.setBounds(10, 10, 100, 20);
		tab1.setControl(group);

		text = new Text(group, SWT.READ_ONLY | SWT.BORDER | SWT.CENTER);
		text.setBounds(116, 5, 161, 25);
		text.setText(projectName);


		Group grpJavaProyectInformation = new Group(group, SWT.NONE);
		grpJavaProyectInformation.setFont(SWTResourceManager.getFont("Sans", 10, SWT.BOLD));
		grpJavaProyectInformation.setText("Java Project Values");
		grpJavaProyectInformation.setBounds(10, 37, 763, 175);

		Label lblNewLabel = new Label(grpJavaProyectInformation, SWT.NONE);
		lblNewLabel.setBounds(10, 35, 74, 15);
		lblNewLabel.setText("N° Classes:");

		Label lblNewLabel_1 = new Label(grpJavaProyectInformation, SWT.NONE);
		lblNewLabel_1.setBounds(10, 68, 103, 15);
		lblNewLabel_1.setText("N° Interfaces:");

		Label lblNMethdos = new Label(grpJavaProyectInformation, SWT.NONE);
		lblNMethdos.setBounds(10, 106, 90, 15);
		lblNMethdos.setText("N° Methods:");

		text_1 = new Text(grpJavaProyectInformation, SWT.READ_ONLY | SWT.BORDER | SWT.CENTER);
		text_1.setBounds(122, 25, 75, 25);
		text_1.setText(setup.getNumberClasses());

		text_2 = new Text(grpJavaProyectInformation, SWT.READ_ONLY | SWT.BORDER | SWT.CENTER);
		text_2.setBounds(122, 58, 75, 25);
		text_2.setText(setup.getNumberInterfaces());

		text_3 = new Text(grpJavaProyectInformation, SWT.READ_ONLY | SWT.BORDER | SWT.CENTER);
		text_3.setBounds(122, 96, 75, 25);
		text_3.setText(setup.getNumberMethods());

		Label lblNProperties = new Label(grpJavaProyectInformation, SWT.NONE);
		lblNProperties.setBounds(10, 139, 103, 15);
		lblNProperties.setText("N° Properties:");

		text_4 = new Text(grpJavaProyectInformation, SWT.READ_ONLY | SWT.BORDER | SWT.CENTER);
		text_4.setBounds(122, 129, 75, 25);
		text_4.setText(setup.getNumberProperties());

		Label lblMethodsFanin = new Label(grpJavaProyectInformation, SWT.NONE);
		lblMethodsFanin.setBounds(271, 35, 103, 15);
		lblMethodsFanin.setText("Methods Fan-In:");

		table = new Table(grpJavaProyectInformation, SWT.BORDER | SWT.FULL_SELECTION);
		table.setBounds(380, 35, 373, 118);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumn tblclmnNos = new TableColumn(table, SWT.NONE); 
		tblclmnNos.setWidth(150); 
		tblclmnNos.setText("Method"); 

		TableColumn tblclmnEno = new TableColumn(table, SWT.NONE); 
		tblclmnEno.setWidth(2); 
		tblclmnEno.setText("Fan-In"); 

		ArrayList <String[]> arrayList = setup.getFanIn();
		for (int i=0; i< arrayList.size(); i++)
		{
			TableItem item = new TableItem(table, SWT.NONE); 
			String idMethod = (String) arrayList.get(i)[0];
			String count = (String) arrayList.get(i)[1];
			item.setText(0,idMethod);
			item.setText(1,count);
		}

		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1)); 

		//Add dinamically concerns
		viewConcerns(group);


		Button btnNewButton = new Button(group, SWT.NONE);
		btnNewButton.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
		btnNewButton.setBounds(673, 10, 100, 25);
		btnNewButton.setText("Run CCKDM");

		Group grpClusterParameters = new Group(group, SWT.NONE);
		grpClusterParameters.setFont(SWTResourceManager.getFont("Sans", 10, SWT.BOLD));
		grpClusterParameters.setText("Clustering Options");
		grpClusterParameters.setBounds(391, 218, 382, 172);

		Label lblNewLabel_4 = new Label(grpClusterParameters, SWT.NONE);
		lblNewLabel_4.setBounds(222, 16, 150, 15);
		lblNewLabel_4.setText("Levenshtein Threshold");

		final Button btnNewButton_5 = new Button(grpClusterParameters, SWT.CHECK);
		btnNewButton_5.setBounds(47, 47, 161, 54);
		btnNewButton_5.setSelection(true);
		btnNewButton_5.setText("Filtering methods   \n(setters && getters)");

		Label lblNewLabel_5 = new Label(grpClusterParameters, SWT.NONE);
		lblNewLabel_5.setAlignment(SWT.CENTER);
		lblNewLabel_5.setFont(SWTResourceManager.getFont("Sans", 8, SWT.NORMAL));
		lblNewLabel_5.setBounds(245, 130, 27, 15);
		lblNewLabel_5.setText(">=");

		final Scale scale = new Scale(grpClusterParameters, SWT.VERTICAL);
		scale.setMaximum(9);
		scale.setMinimum(1);
		scale.setBounds(271, 37, 27, 87);
		scale.setSelection(5);

		text_10 = new Text(grpClusterParameters, SWT.BORDER | SWT.CENTER | SWT.READ_ONLY);
		text_10.setFont(SWTResourceManager.getFont("Sans", 8, SWT.NORMAL));
		text_10.setBounds(271, 126, 27, 19);
		text_10.setText("0." + String.valueOf(scale.getMaximum() - scale.getSelection() + scale.getMinimum()));

		final Button btnCheckButton = new Button(grpClusterParameters, SWT.CHECK);
		btnCheckButton.setBounds(47, 16, 161, 41);
		btnCheckButton.setText("Deactivate Clustering");

		final Button btnCheckButton_1 = new Button(grpClusterParameters, SWT.CHECK);
		txtClickToLoad = new Text(grpClusterParameters, SWT.BORDER | SWT.CENTER | SWT.READ_ONLY);
		txtClickToLoad.setText("Click to Load File");
		txtClickToLoad.setFont(SWTResourceManager.getFont("Sans", 8, SWT.NORMAL));
		txtClickToLoad.setBounds(47, 130, 117, 15);
		txtClickToLoad.setEnabled(false);

		final Button btnNewButton_3 = new Button(grpClusterParameters, SWT.NONE);
		btnNewButton_3.setFont(SWTResourceManager.getFont("Sans", 7, SWT.NORMAL));
		btnNewButton_3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
				fd.setText("Open");
				folder = workspace.getRoot().getLocation().toFile().getPath().toString();
				fd.setFilterPath(folder +"/"+ projectName + "/");
				String[] filterExt = {"*.csv"};
				fd.setFilterExtensions(filterExt);
				pathControll = fd.open();
				if (pathControll != null)
				{
					String[] name = pathControll.split("\\/");
					txtClickToLoad.setText(name[name.length-1]);
					Program.launch(pathControll);
				}
			}
		});
		btnNewButton_3.setBounds(170, 130, 49, 19);
		btnNewButton_3.setText("Open");
		btnNewButton_3.setEnabled(false);

		final Button btnNewButton_4 = new Button(grpClusterParameters, SWT.NONE);
		btnNewButton_4.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				final IMiner ccMining = new CCMining();
				if (selected.size() > 0)
				{
					try 
					{
						ccMining.controlledAnnotating(projectName, folder, pathControll,selected);
						MessageDialog.openInformation(shell,"CCKDM", "The KDM XMI file has been updated.");

					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (QueryException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				else
				{
					MessageDialog.openInformation(shell,"CCKDM", "You must choose at least one concern to delete");
				}
			}
		});
		btnNewButton_4.setFont(SWTResourceManager.getFont("Sans", 7, SWT.NORMAL));
		btnNewButton_4.setBounds(47, 149, 78, 18);
		btnNewButton_4.setText("Control");
		btnNewButton_4.setEnabled(false);

		btnCheckButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				controll = btnCheckButton_1.getEnabled();

				if (btnCheckButton_1.getSelection())
				{
					scale.setEnabled(false);
					btnNewButton_5.setEnabled(false);
					txtClickToLoad.setEnabled(true);
					btnNewButton_3.setEnabled(true);
					btnNewButton_4.setEnabled(true);
				}
				else
				{
					scale.setEnabled(true);
					btnNewButton_5.setEnabled(true);
					txtClickToLoad.setEnabled(false);
					btnNewButton_3.setEnabled(false);
					btnNewButton_4.setEnabled(false);
				}

			}
		});
		btnCheckButton_1.setBounds(47, 107, 161, 21);
		btnCheckButton_1.setText("Controlled Annotation");



		valueText = new BigDecimal(text_10.getText());

		scale.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event){
				int perspectiveValue=  scale.getMaximum() - scale.getSelection() + scale.getMinimum();
				text_10.setText("0." + String.valueOf(perspectiveValue));
				valueText = new BigDecimal(text_10.getText());
			}
		});

		controll = btnCheckButton_1.getSelection();
		select = btnNewButton_5.getSelection();
		btnNewButton_5.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				select = btnNewButton_5.getSelection();
			}
		});

		cluster = btnCheckButton.getSelection();
		btnCheckButton.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				cluster = btnCheckButton.getSelection();
				if (btnCheckButton.getSelection())
				{
					btnNewButton_5.setEnabled(false);
					btnCheckButton_1.setEnabled(false);
					scale.setEnabled(false);
					text_10.setEnabled(false);
					txtClickToLoad.setEnabled(false);
					btnNewButton_3.setEnabled(false);
					btnNewButton_4.setEnabled(false);
				}
				else
				{
					btnNewButton_5.setEnabled(true);
					btnCheckButton_1.setEnabled(true);
					scale.setEnabled(true);
					text_10.setEnabled(true);
					if (btnCheckButton_1.getSelection())
					{
						txtClickToLoad.setEnabled(true);
						scale.setEnabled(false);
						btnNewButton_5.setEnabled(false);
						btnNewButton_3.setEnabled(true);
						btnNewButton_4.setEnabled(true);

					}
					else
					{
						txtClickToLoad.setEnabled(false);
						btnNewButton_3.setEnabled(false);
						btnNewButton_4.setEnabled(false);
					}
				}
			}
		});



		//Search for crosscutting concerns
		btnNewButton.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				folder = workspace.getRoot().getLocation().toFile().getPath().toString();
				//Prepare file to annotation
				final IMiner ccMining = new CCMining();
				IUtilities utilities = new Utilities();
				utilities.copyFinalFile(projectName);

				if (selected.size() > 0)
				{
					try 
					{
						dialog.run(true, true, new IRunnableWithProgress()
						{ 
							public void run(IProgressMonitor monitor) 
							{
								try 
								{
									int totalUnitsOfWork = IProgressMonitor.UNKNOWN;
									monitor.beginTask("Searching for CC and Annotating KDM", totalUnitsOfWork);
									ccMining.mine(selected,projectName,folder, select, valueText, monitor,cluster);
									monitor.done();
								}
								catch (SQLException e1)
								{
									// TODO Auto-generated catch block
									e1.printStackTrace();
								} 
								catch (QueryException e1) 
								{
									// TODO Auto-generated catch block
									e1.printStackTrace();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						});
					} 
					catch (InvocationTargetException ex) 
					{
						// TODO Auto-generated catch block
						ex.printStackTrace();
					} 
					catch (InterruptedException ex) 
					{
						// TODO Auto-generated catch block
						ex.printStackTrace();
					}

					MessageDialog.openInformation(shell,"CCKDM", "The KDM XMI file has been annotated.");
				}
				else
				{
					MessageDialog.openInformation(shell,"CCKDM", "You must choose at least one concern.");
				}
			}
		});


		//Tab 2
		TabItem tab2 = new TabItem(tabFolder, SWT.NONE);
		tab2.setText("Library Management");

		Group group1 = new Group(tabFolder, SWT.NONE);
		group1.setBounds(10, 37, 763, 175);

		tab2.setControl(group1);


		Label lblNewLabel_2 = new Label(group1, SWT.NONE);
		lblNewLabel_2.setText("Project Name:");
		lblNewLabel_2.setBounds(10, 10, 100, 20);

		text_5 = new Text(group1, SWT.READ_ONLY | SWT.BORDER | SWT.CENTER);
		text_5.setBounds(116, 5, 161, 25);
		text_5.setText(projectName);

		Group grpManageLibrary = new Group(group1, SWT.NONE);
		grpManageLibrary.setFont(SWTResourceManager.getFont("Sans", 10, SWT.BOLD));
		grpManageLibrary.setText("Management");
		grpManageLibrary.setBounds(10, 37, 358, 342);

		text_6 = new Text(grpManageLibrary, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		text_6.setBounds(10, 44, 338, 257);

		Button btnNewButton_1 = new Button(grpManageLibrary, SWT.NONE);
		btnNewButton_1.setBounds(10, 307, 122, 25);
		btnNewButton_1.setText("Add Concern(s)");

		Label lblNewLabel_3 = new Label(grpManageLibrary, SWT.NONE);
		lblNewLabel_3.setFont(SWTResourceManager.getFont("Sans", 9, SWT.NORMAL));
		lblNewLabel_3.setBounds(10, 23, 338, 15);
		lblNewLabel_3.setText("[concern], [package], [element, ...] per line.");

		Button btnNewButton_2 = new Button(grpManageLibrary, SWT.NONE);
		btnNewButton_2.setBounds(264, 307, 84, 25);
		btnNewButton_2.setText("Clear");

		btnNewButton_2.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				text_6.setText("");
			}
		});


		Group grpConcernView = new Group(group1, SWT.NONE);
		grpConcernView.setFont(SWTResourceManager.getFont("Sans", 10, SWT.BOLD));
		grpConcernView.setText("Concern View");
		grpConcernView.setBounds(374, 37, 399, 342);

		tree  = new Tree(grpConcernView, SWT.BORDER | SWT.V_SCROLL | SWT.VIRTUAL);
		tree.setBounds(10, 21, 379, 311);

		//Listener Tree Delete options
		final Menu menu = new Menu(tree);
		tree.setMenu(menu);

		final MenuItem removeItemMenuItem = new MenuItem(menu, SWT.NONE);
		removeItemMenuItem.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(final SelectionEvent e)
			{
				final class NodeValues // a helper class that holds any values we want to save from the node.
				{
					public NodeValues(TreeItem item)
					{
						data = item.getData();
						name = item.getText();
					}
					public String name;
					public Object data;
				}

				TreeItem [] selected = tree.getSelection();
				if (selected.length > 0)
				{
					TreeItem selection = selected[0];

					if (selection.getParentItem() == null)
					{
						if (selection.getText().equals("Persistence") ||
								selection.getText().equals("Logging")     ||
								selection.getText().equals("Authentication")
								)
						{
							MessageDialog.openInformation(shell,"CCKDM", selection.getText() + " can not be removed.");
						}
						else
						{	
							selection.dispose();
							saveTreeXML(tree);
						}
						return;
					}


					TreeItem parent = selection.getParentItem();

					if (parent.getItemCount() == 1)
					{
						MessageDialog.openInformation(shell,"CCKDM", "The concern must have at least one package and one class.");
					}
					else
					{
						Vector<NodeValues> children = new Vector<NodeValues>();
						TreeItem [] items = parent.getItems();

						for (int i = 0; i < items.length; i++)
						{
							if (i == parent.indexOf(selection))
							{
								System.out.println("The selected node is at index " + i);
							}
							else
								children.add(new NodeValues(items[i]));
						}
						assert children.size() == (items.length - 1);
						parent.removeAll();

						for (int i = 0; i < children.size(); i++)
						{
							TreeItem item = new TreeItem(parent, SWT.None, i);
							item.setData(children.elementAt(i).data);
							item.setText(children.elementAt(i).name);
						}
						parent.setExpanded(true);
					}
				}
				saveTreeXML(tree);
			}
		});
		removeItemMenuItem.setText("Remove Item");

		this.generateConcernTree(tree);

		btnNewButton_1.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{

				String folder = workspace.getRoot().getLocation().toFile().getPath().toString();
				boolean check = checkCSV(text_6.getText(),folder +  "/"  + projectName);
				if (check)
				{
					tree.removeAll();
					generateConcernTree(tree);
					MessageDialog.openInformation(shell,"CCKDM","Library updated!");
				}

			}
		});



		//Tab 3
		TabItem tab3 = new TabItem(tabFolder, SWT.NONE);
		tab3.setText("Log Viewer");

		final Group group2 = new Group(tabFolder, SWT.NONE);
		group2.setBounds(10, 37, 763, 175);

		tab3.setControl(group2);

		Label lblConcernIdentification = new Label(group2, SWT.NONE);
		lblConcernIdentification.setBounds(10, 10, 177, 15);
		lblConcernIdentification.setText("Concern Identification Log:  ");

		Label lblLibrary = new Label(group2, SWT.NONE);
		lblLibrary.setFont(SWTResourceManager.getFont("Sans", 10, SWT.BOLD));
		lblLibrary.setBounds(196, 10, 65, 15);
		lblLibrary.setText("Library");

		text_8 = new Text(group2, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		text_8.setEditable(false);
		text_8.setBounds(10, 31, 374, 301);
		text_8.setData("text_8");

		Label lblConcernIdentificationLog = new Label(group2, SWT.NONE);
		lblConcernIdentificationLog.setBounds(399, 10, 177, 15);
		lblConcernIdentificationLog.setText("Concern Identification Log:");

		Label lblClusterKmeans = new Label(group2, SWT.NONE);
		lblClusterKmeans.setFont(SWTResourceManager.getFont("Sans", 10, SWT.BOLD));
		lblClusterKmeans.setBounds(581, 10, 130, 15);
		lblClusterKmeans.setText("Cluster K-Means");

		text_9 = new Text(group2, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		text_9.setEditable(false);
		text_9.setBounds(399, 31, 374, 301);
		text_9.setData("text_9");

		Label lblNewLabel_6 = new Label(group2, SWT.NONE);
		lblNewLabel_6.setBounds(10, 342, 272, 15);
		lblNewLabel_6.setText("*The logs are stored in the following path:");

		Label lblNewLabel_7 = new Label(group2, SWT.NONE);
		lblNewLabel_7.setBounds(292, 342, 460, 15);
		lblNewLabel_7.setText("");

		TabItem tab4 = new TabItem(tabFolder, SWT.NONE);
		tab4.setText("License");

		Group group3 = new Group(tabFolder, SWT.NONE);
		tab4.setControl(group3);

		text_7 = new Text(group3, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.MULTI);
		text_7.setFont(SWTResourceManager.getFont("Sans", 12, SWT.NORMAL));
		text_7.setBounds(10, 10, 763, 380);


		tabFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
				if (tabFolder.getSelection()[0].getText().equals("Mining of Crosscutting Concern"))
				{
					Control [] controls = group.getChildren();
					for (int i=0; i< controls.length; i++)
					{
						if (controls[i] instanceof Group)
						{
							Group grpControl = (Group) controls[i];
							if (grpControl.getText().equals("Available Concerns"))
							{
								grpControl.dispose();
							}
						}	
					}
					selected.clear();
					viewConcerns(group);
				}
				else
				{
					if (tabFolder.getSelection()[0].getText().equals("Log Viewer"))
					{
						Control [] controls = group2.getChildren();
						for (int i=0; i< controls.length; i++)
						{
							if (controls[i] instanceof Text)
							{
								Text text = (Text) controls[i];
								if (text.getData().equals("text_8"))
								{
									IUtilities utilities = new Utilities();
									try {
										text.setText(utilities.getLogs(projectName, 1));
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}

								if (text.getData().equals("text_9"))
								{
									IUtilities utilities = new Utilities();
									try {
										text.setText(utilities.getLogs(projectName, 2));
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}	

							if (controls[i] instanceof Label)
							{
								Label lbl = (Label) controls[i];
								if (lbl.getText().equals(""))
								{
									String folder = workspace.getRoot().getLocation().toFile().getPath().toString();
									lbl.setText(folder + "/" + projectName + "/");
								}
							}

						}
					}
					else
					{
						if (tabFolder.getSelection()[0].getText().equals("License"))
						{
							IUtilities utilities = new Utilities();
							try {
								text_7.setText(utilities.getLicense());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
		});

	}

	public void viewConcerns(final Group group)
	{
		final Group grpSearchForCrosscutting = new Group(group, SWT.NONE);
		grpSearchForCrosscutting.setFont(SWTResourceManager.getFont("Sans", 10, SWT.BOLD));
		grpSearchForCrosscutting.setText("Available Concerns");
		grpSearchForCrosscutting.setBounds(10, 218, 763, 129);


		GridLayout gridLayout = new GridLayout(3,false);	
		grpSearchForCrosscutting.setLayout(gridLayout);

		//Delete Controlls
		Control [] controls = grpSearchForCrosscutting.getChildren();
		for (int i = 0; i< controls.length; i++)
			controls[i].dispose();

		//Add Controlls
		String folder = workspace.getRoot().getLocation().toFile().getPath().toString();
		ArrayList<String> arrayString = xmlLibrary.getConcerns(folder +  "/"  + projectName);
		for (int i=0; i< arrayString.size(); i++)
		{
			final Button a = new Button(grpSearchForCrosscutting, SWT.CHECK);
			a.setText(arrayString.get(i));
			a.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					selected.clear();
					selected.addAll(checkButtonSelected(group));
				}
			});

		}	
		grpSearchForCrosscutting.pack();
		grpSearchForCrosscutting.update();
	}

	public ArrayList<String> checkButtonSelected(Group group)
	{
		ArrayList<String> s = new ArrayList<String>();
		Control [] controls = group.getChildren();

		for (int i=0; i< controls.length; i++)
		{
			if (controls[i] instanceof Group)
			{
				Group grpControl = (Group) controls[i];
				if (grpControl.getText().equals("Available Concerns"))
				{
					Control[] controlsGrp = grpControl.getChildren();
					for (int j=0; j< controlsGrp.length; j++)
					{
						if (controlsGrp[j] instanceof Button)
						{
							Button btn = (Button) controlsGrp[j];
							if (btn.getSelection())
								s.add(btn.getText());
						}
					}
				}
			}	
		}
		return s;
	}


	public void saveTreeXML(Tree tree)
	{
		int q1 = tree.getItemCount();
		String xml = "<ConcernLibrary>";
		for (int i=0 ; i< q1; i++)
		{
			TreeItem treeItemConcern = tree.getItem(i);
			xml = xml + "<Concern name='" + treeItemConcern.getText() + "'>";
			int q2 = treeItemConcern.getItemCount();
			for(int j= 0; j<q2; j++)
			{
				TreeItem treeItemPackage = treeItemConcern.getItem(j);
				xml = xml + "<Package name='" + treeItemPackage.getText() + "'>";
				int q3 = treeItemPackage.getItemCount();
				for (int k=0; k<q3; k++)
				{
					TreeItem treeItemElement = treeItemPackage.getItem(k);
					xml = xml +  "<Element>" + treeItemElement.getText() + "</Element>";
				}
				xml = xml + "</Package>";
			}
			xml = xml + "</Concern>";
		}
		xml = xml + "</ConcernLibrary>";
		String folder = workspace.getRoot().getLocation().toFile().getPath().toString();
		xmlLibrary.writeXML(xml, folder +  "/"  + projectName);
	}

	public void generateConcernTree(Tree tree)
	{
		String folder= workspace.getRoot().getLocation().toFile().getPath().toString(); 
		ArrayList <String> xmlArray = xmlLibrary.readLibrary(folder + "/" + this.projectName);
		TreeItem concern = null;
		TreeItem packages = null;
		TreeItem element = null;

		for (int i = 0; i< xmlArray.size(); i++)
		{
			if (xmlArray.get(i).split("\\|")[0].equals("Concern"))
			{
				concern = new TreeItem(tree, 0);
				concern.setText(xmlArray.get(i).split("\\|")[1]);
			}
			else
			{
				if (xmlArray.get(i).split("\\|")[0].equals("Package"))
				{
					packages = new TreeItem(concern, 0);
					packages.setText(xmlArray.get(i).split("\\|")[1]);
				}
				else
				{
					if (xmlArray.get(i).split("\\|")[0].equals("Element"))
					{
						element = new TreeItem(packages, 0);
						element.setText(xmlArray.get(i).split("\\|")[1]);
					}
				}
			}
		}
	}

	public boolean checkCSV(String text, String path)
	{
		boolean check = false;
		if (text.length() == 0)
		{
			MessageDialog.openError(shell,"CCKDM","The text box is empty");
			check = false;
		}
		else
		{
			xmlLibrary.parseCSV(text,path);
			check = true;
		}
		return check;
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public Resource initialSetup(String projectName, IProgressMonitor monitor)
	{
		EList <KDMModel>  listKdmModel = null;
		KDMModel kdmModel = null;
		Resource javaResource = null;

		try
		{
			//Create DataBase Model
			monitor.subTask("Creating database model.");
			int rtn = setup.setUpModel();

			monitor.subTask("Loading resources.");
			//Load Project
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			IJavaProject javaProject = JavaCore.create(project);

			//If file exist
			String folder= workspace.getRoot().getLocation().toFile().getPath().toString(); 
			File file = new File(folder + "/" + this.projectName +"/" + projectName + "_KDM" + ".xmi");

			//Convert KDM
			monitor.subTask("Getting KDM model.");
			if (!file.exists())
			{
				DiscoverKDMModelFromJavaProject kdmDiscover = new DiscoverKDMModelFromJavaProject();
				kdmDiscover.discoverElement(javaProject, monitor);
				javaResource = kdmDiscover.getTargetModel();
			}
			else
			{
				URI uri = URI.createFileURI(folder + "/" + this.projectName +"/" + projectName + "0_KDM" + ".xmi");
				// Get the resource
				Resource resource =  new XMIResourceImpl();
				resource.unload();
				resource.setURI(uri);
				resource.load(null);
				javaResource = resource;
			}

			//Get KDM Segment
			Segment kdmSegment =  (Segment) javaResource.getContents().get(0);
			listKdmModel = kdmSegment.getModel();						


			//Get Models
			kdmModel = listKdmModel.get(0);

			monitor.subTask("Loading packages.");
			setup.getModelPackages(kdmModel, this.projectName, rtn);
			monitor.subTask("Loading classes.");
			setup.getModelClasses(kdmModel, this.projectName, rtn);
			monitor.subTask("Loading interfaces.");
			setup.getModelInterfaces(kdmModel, this.projectName, rtn);
			monitor.subTask("Loading methods.");
			BlockUnit blockUnit = setup.getModelMethods(kdmModel, this.projectName, rtn);
			monitor.subTask("Loading methods fan-in.");
			setup.getCalls(kdmModel, this.projectName, rtn,blockUnit);
			monitor.subTask("Loading properties.");
			setup.getModelProperties(kdmModel, this.projectName, rtn);

		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}

		return javaResource;
	}

	public void generateKDMFile(Resource javaResource) throws IOException
	{
		//Saving KDM instance
		String folder= workspace.getRoot().getLocation().toFile().getPath().toString(); 


		//If file doesn't exists then create it.
		File file = new File(folder + "/" + this.projectName +"/" + projectName + "_KDM" + ".xmi");
		if (!file.exists())
		{
			FileOutputStream fout = new FileOutputStream(file);
			javaResource.save(fout, null);
			fout.close();
			IUtilities utilities = new Utilities();
			utilities.copyInitialFile(projectName);
		}
	}
}
