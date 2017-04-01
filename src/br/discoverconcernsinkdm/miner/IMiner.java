package br.discoverconcernsinkdm.miner;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.basex.query.QueryException;
import org.eclipse.core.runtime.IProgressMonitor;

public interface IMiner {

	public void mine(ArrayList<String> a, String b, String c, boolean d, BigDecimal e, 
			         IProgressMonitor f, boolean g) throws IOException, SQLException, QueryException;
	public List<String> mineByLibrary(String a, String b, String c, 
									  boolean d, BigDecimal e) throws IOException, SQLException, QueryException;
	public void mineByCluster(String a, String b, BigDecimal c, 
							  boolean d, String e, List<String> f) throws IOException, SQLException, QueryException;
	
	public void controlledAnnotating(String projectName, String folder,String path) throws IOException, SQLException, QueryException;
	
}
