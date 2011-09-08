package org.eclipse.mylyn.modeling.context;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.mylyn.context.core.AbstractContextStructureBridge;

/**
 * Provides support for using a simple structure provider to define domain objects which should be managed by Mylyn.
 * Consumers should typically only need to override abstract methods.
 * 
 * @author Miles Parker
 */
public abstract class DomainModelContextStructureBridge extends AbstractContextStructureBridge {

	public abstract String getDomainHandleIdentifier(Object object);

	/**
	 * Discovers the domain object for an arbitrary diagram class. Implementors generally should not override.
	 * 
	 * @param object
	 * @return
	 */
	public Object getDomainObject(Object object) {
		// We follow the chain down until the object isn't adaptable to EObject
		// anymore in order to get the actual domain object
		if (object == null) {
			return null;
		}
		if (object instanceof IAdaptable) {
			Object diagramObject = ((IAdaptable) object).getAdapter(getDomainBaseNodeClass());
			if (diagramObject != null && getDomainBaseNodeClass().isAssignableFrom(diagramObject.getClass())) {
				return getDomainObject(diagramObject);
			}
		}
		// don't want to look at all classes unless it's relevant
		if (getDomainBaseNodeClass().isAssignableFrom(object.getClass())) {
			for (Class<?> domainClass : getDomainNodeClasses()) {
				if (domainClass.isAssignableFrom(object.getClass())) {
					return object;
				}
			}
		}
		if (getDomainBaseEdgeClass().isAssignableFrom(object.getClass())) {
			for (Class<?> domainClass : getDomainEdgeClasses()) {
				if (domainClass.isAssignableFrom(object.getClass())) {
					return object;
				}
			}
		}
		return null;
	}

	/**
	 * If it's a domain object, we accept it. Implementors generally should not override.
	 */
	@Override
	public boolean acceptsObject(Object object) {
		return getDomainObject(object) != null;
	}

	/**
	 * Delegates any diagram handle requests to the appropriate domain object. Implementors generally should not
	 * override.
	 */
	@Override
	public String getHandleIdentifier(Object object) {
		Object domainObject = getDomainObject(object);
		if (domainObject != null && getDomainBaseNodeClass().isAssignableFrom(domainObject.getClass())) {
			return getDomainHandleIdentifier(domainObject);
		}
		return null;
	}

	/**
	 * Simply returns the domain object. Implementors generally should not override.
	 */
	@Override
	public Object getObjectForHandle(String handle) {
		// We're simply calling this delegated method but renaming to clarify
		// relationship with similar methods.
		return getDomainObjectForHandle(handle);
	}

	/**
	 * Override to specify a mapping between a unique handle and the domain object.
	 * 
	 * @param handle
	 * @return
	 */
	public abstract Object getDomainObjectForHandle(String handle);

	/**
	 * Override to specify an appropriate domain label.
	 */
	@Override
	public String getLabel(Object object) {
		return object.toString();
	}

	/**
	 * Defines all domain nodes as potential landmarks. Override if different behavior is needed.
	 */
	@Override
	public boolean canBeLandmark(String handle) {
		Object object = getObjectForHandle(handle);
		if (object != null) {
			for (Class<?> domainClass : getDomainNodeClasses()) {
				if (domainClass.isAssignableFrom(object.getClass())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Assumes that all objects can be filtered against.
	 */
	@Override
	public boolean canFilter(Object element) {
		// TODO Is this true? Can ee filter?
		return true;
	}

	/**
	 * Returns null. Doesn't seem appropriate for models.
	 */
	@Override
	public String getHandleForOffsetInObject(Object resource, int offset) {
		return null;
	}

	/**
	 * All elements within a domain should share the same content type. Implementors generally should not override
	 * unless this is not the case.
	 */
	@Override
	public String getContentType(String elementHandle) {
		return getContentType();
	}

	/**
	 * Override to provide a unique content type for the given domain. Perhaps we should consider using the domain model
	 * name (e.g. "foo" for foo.ecore) for EMF-based models.
	 */
	@Override
	public abstract String getContentType();

	/**
	 * Override to return the most specific class that covers all potential model nodes. For example, in the Ecore
	 * editor implementation this is ENamedObject as that is the only model class that is a super type for EClass, EEnum
	 * and EPackage.
	 * 
	 * @return
	 */
	public abstract Class<?> getDomainBaseNodeClass();

	/**
	 * Override to return all classes that have <i>explicit</i> diagram nodes represented in the model that should be
	 * managed by Mylyn. These should not be generic classes but the most specific appropriate interface for a given
	 * diagram node.
	 * 
	 * @return
	 */
	public abstract Class<?>[] getDomainNodeClasses();

	/**
	 * Override to return the most specific class that covers all potential model edges. For example, in the Ecore
	 * editor implementation this is EReference.
	 * 
	 * @return
	 */
	public abstract Class<?> getDomainBaseEdgeClass();

	/**
	 * Override to return all classes that have <i>explicit</i> diagram nodes represented in the model that should be
	 * managed by Mylyn. For example in Ecore, this is simply EReference, but in other domain models with multiple edge
	 * types all of those types should be included here.
	 * 
	 * @return
	 */
	public abstract Class<?>[] getDomainEdgeClasses();
}