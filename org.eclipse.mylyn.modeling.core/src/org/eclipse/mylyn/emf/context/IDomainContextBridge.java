package org.eclipse.mylyn.emf.context;


public interface IDomainContextBridge {
	
	String getContentType();

	Class<?> getDomainBaseClass();

	Class<?>[] getDomainNodeClasses();

	String getLabel(Object object);
}
