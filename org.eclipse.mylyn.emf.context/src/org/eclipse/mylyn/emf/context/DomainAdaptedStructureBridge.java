package org.eclipse.mylyn.emf.context;

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.mylyn.context.core.AbstractContextStructureBridge;

public abstract class DomainAdaptedStructureBridge extends
		AbstractContextStructureBridge {

	IDiagramContextBridge delegatedBridge;

	public DomainAdaptedStructureBridge(IDiagramContextBridge delegatedBridge) {
		super();
		this.delegatedBridge = delegatedBridge;
	}

	public abstract String getDomainHandleIdentifier(Object object);
	
	public Object getDomainObject(Object object) {
		// We follow the chain down until the object isn't adaptable to EObject
		// anymore in order to get the actual domain object
		if (object == null) {
			return null;
		}
		if (object instanceof IAdaptable) {
			Object diagramObject = ((IAdaptable) object)
					.getAdapter(delegatedBridge.getDomainBaseClass());
			if (diagramObject != null
					&& delegatedBridge.getDomainBaseClass().isAssignableFrom(
							diagramObject.getClass())) {
				return getDomainObject(diagramObject);
			}
		}
		for (Class<?> domainClass : delegatedBridge.getDomainNodeClasses()) {
			if (domainClass.isAssignableFrom(object.getClass())) {
				return object;
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
		if (domainObject != null && delegatedBridge.getDomainBaseClass().isAssignableFrom(domainObject.getClass())) {
			return getDomainHandleIdentifier(domainObject);
		}
		return null;
	}

	@Override
	public final Object getObjectForHandle(String handle) {
		//We're simply calling this delegated method but renaming to clarify relationship with similar methods.
		return getDomainObjectForHandle(handle);
	}
	
	public abstract Object getDomainObjectForHandle(String handle);

	@Override
	public List<String> getChildHandles(String handle) {
		// ignore
		return null;
	}

	@Override
	public String getLabel(Object object) {
		return object.toString();
	}

	@Override
	public boolean canBeLandmark(String handle) {
		Object object = getObjectForHandle(handle);
		for (Class<?> domainClass : delegatedBridge.getDomainNodeClasses()) {
			if (domainClass.isAssignableFrom(object.getClass())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean canFilter(Object element) {
		// ignore
		return true;
	}

	@Override
	public String getHandleForOffsetInObject(Object resource, int offset) {
		// ignore
		return null;
	}

	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return delegatedBridge.getContentType();
	}
	
	@Override
	public String getContentType(String elementHandle) {
		return getContentType();
	}

}