package org.eclipse.mylyn.emf.context;


public interface IDiagramContextBridge {
	
	String getContentType();

	Class<?> getDomainBaseClass();

	Class<?>[] getDomainNodeClasses();

	String getLabel(Object object);
}
