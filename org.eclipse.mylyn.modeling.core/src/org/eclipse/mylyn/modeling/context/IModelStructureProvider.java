package org.eclipse.mylyn.modeling.context;


public interface IModelStructureProvider {
	
	String getContentType();

	Class<?> getDomainBaseClass();

	Class<?>[] getDomainNodeClasses();

	String getLabel(Object object);
}
