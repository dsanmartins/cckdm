package br.discoverconcernsinkdm.queryManager;

import org.eclipse.gmt.modisco.omg.kdm.action.BlockUnit;

public interface IQueries {

	public abstract void setModelPackages() throws Exception;

	public abstract void setModelClasses() throws Exception;

	public abstract void setModelInterfaces() throws Exception;

	public abstract BlockUnit setModelMethods() throws Exception;

	public abstract void setCalls(BlockUnit blockUnit) throws Exception;

	public abstract void setModelProperties() throws Exception;

}