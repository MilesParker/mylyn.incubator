package org.eclipse.mylyn.modeling.context;

/**
 * @author Miles Parker
 */
public interface IModelStructureProvider {

	String getContentType();

	Class<?> getDomainBaseNodeClass();

	Class<?>[] getDomainNodeClasses();

	Class<?> getDomainBaseEdgeClass();

	Class<?>[] getDomainEdgeClasses();

	String getLabel(Object object);
}
