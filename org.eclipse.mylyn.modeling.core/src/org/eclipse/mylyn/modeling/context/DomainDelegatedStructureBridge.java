package org.eclipse.mylyn.modeling.context;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.mylyn.context.core.AbstractContextStructureBridge;

/**
 * Provides support for using a simple structure provider to define appropriate domain classes that can then determine
 * appropriate responses for structure bridge.
 * 
 * @author Miles Parker
 */
public abstract class DomainDelegatedStructureBridge extends AbstractContextStructureBridge {

	public abstract String getDomainHandleIdentifier(Object object);

	public Object getDomainObject(Object object) {
		// We follow the chain down until the object isn't adaptable to EObject
		// anymore in order to get the actual domain object
		if (object == null) {
			return null;
		}
		if (object instanceof IAdaptable) {
			Object diagramObject = ((IAdaptable) object).getAdapter(getDomainContextBridge().getDomainBaseNodeClass());
			if (diagramObject != null
					&& getDomainContextBridge().getDomainBaseNodeClass().isAssignableFrom(diagramObject.getClass())) {
				return getDomainObject(diagramObject);
			}
		}
		// don't want to look at all classes unless it's relevant
		if (getDomainContextBridge().getDomainBaseNodeClass().isAssignableFrom(object.getClass())) {
			for (Class<?> domainClass : getDomainContextBridge().getDomainNodeClasses()) {
				if (domainClass.isAssignableFrom(object.getClass())) {
					return object;
				}
			}
		}
		if (getDomainContextBridge().getDomainBaseEdgeClass().isAssignableFrom(object.getClass())) {
			for (Class<?> domainClass : getDomainContextBridge().getDomainEdgeClasses()) {
				if (domainClass.isAssignableFrom(object.getClass())) {
					return object;
				}
			}
		}
		return null;
	}

	@Override
	public boolean acceptsObject(Object object) {
		return getDomainObject(object) != null;
	}

	@Override
	public String getHandleIdentifier(Object object) {
		Object domainObject = getDomainObject(object);
		if (domainObject != null
				&& getDomainContextBridge().getDomainBaseNodeClass().isAssignableFrom(domainObject.getClass())) {
			return getDomainHandleIdentifier(domainObject);
		}
		return null;
	}

	@Override
	public final Object getObjectForHandle(String handle) {
		// We're simply calling this delegated method but renaming to clarify
		// relationship with similar methods.
		return getDomainObjectForHandle(handle);
	}

	public abstract Object getDomainObjectForHandle(String handle);

	@Override
	public String getLabel(Object object) {
		return object.toString();
	}

	@Override
	public boolean canBeLandmark(String handle) {
		Object object = getObjectForHandle(handle);
		if (object != null) {
			for (Class<?> domainClass : getDomainContextBridge().getDomainNodeClasses()) {
				if (domainClass.isAssignableFrom(object.getClass())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean canFilter(Object element) {
		// TODO Is this true? Can wee filter?
		return true;
	}

	@Override
	public String getHandleForOffsetInObject(Object resource, int offset) {
		// Seems only appropriate for text.
		return null;
	}

	@Override
	public String getContentType() {
		return getDomainContextBridge().getContentType();
	}

	@Override
	public String getContentType(String elementHandle) {
		return getContentType();
	}

	public abstract IModelStructureProvider getDomainContextBridge();
}