/*******************************************************************************
 * Copyright (c) 2011 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.emf.context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.mylyn.context.core.AbstractContextStructureBridge;

/**
 * @author Benjamin Muskalla
 * @author milesparker
 */
public class EMFStructureBridge extends AbstractContextStructureBridge {

	public static final String EMF_CONTENT_TYPE = "emfModel"; //$NON-NLS-1$

	Map<URI, Resource> resourceCache = new HashMap<URI, Resource>();

	@Override
	public String getContentType() {
		return EMF_CONTENT_TYPE;
	}

	@Override
	/**
	 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=343194
	 */
	public String getHandleIdentifier(Object object) {
		Object domainObject = getDomainObject(object);
		if (domainObject instanceof EObject) {
			EObject eobject = ((EObject) domainObject);
			URI uri = EcoreUtil.getURI(eobject);
			return uri.toString();
		}
		return null;
	}
	
	public EObject getDomainObject(Object object) {
		//We follow the chain down until the object isn't adaptable to EObject anymore in order to get the actual domain object
		if (object instanceof IAdaptable) {
			Object diagramObject = ((IAdaptable) object)
					.getAdapter(EObject.class);
			if (diagramObject instanceof EObject) {
				return getDomainObject(diagramObject);
			}
		} 
		if (object instanceof EModelElement) {
			return (EObject) object;
		}
		return null;
	}

	@Override
	public boolean acceptsObject(Object object) {
		return object instanceof EModelElement
				|| (object instanceof IAdaptable && ((IAdaptable) object)
						.getAdapter(EObject.class) instanceof EModelElement);
	}

	@Override
	public String getParentHandle(String handle) {
		EObject eObject = (EObject) getObjectForHandle(handle);
		return getHandleIdentifier(eObject.eContainer());
	}

	@Override
	public Object getObjectForHandle(String handle) {
		URI uri = URI.createURI(handle);
		ResourceSetImpl resourceSetImpl = new ResourceSetImpl();
		EObject eObject = resourceSetImpl.getEObject(uri, true);
		if (eObject != null) {
			return eObject;
		}
		return resourceSetImpl.getResource(uri, true);
	}

	/**
	 * These are only for determining unique resources..do not use the resources
	 * themselves!
	 * 
	 * @param handle
	 * @return
	 */
	public Resource getUniqueResourceForHandle(String handle) {
		URI uri = URI.createURI(handle);
		uri = uri.trimFragment();
		Resource resource = resourceCache.get(uri);
		if (resource == null) {
			ResourceSetImpl resourceSetImpl = new ResourceSetImpl();
			resource = resourceSetImpl.getResource(uri, true);
			resourceCache.put(uri, resource);
		}
		return resource;
	}

	public void flushResourceCache() {
		if (resourceCache != null) {
			for (Entry<URI, Resource> res : resourceCache.entrySet()) {
				Resource value = res.getValue();
				value.unload();
			}
		}
		resourceCache = new HashMap<URI, Resource>();
	}

	@Override
	public List<String> getChildHandles(String handle) {
		// ignore
		return null;
	}

	@Override
	public String getLabel(Object object) {
		/**
		 * if (object instanceof IResource) { return ((IResource)
		 * object).getName(); } else
		 **/
		if (object instanceof ENamedElement) {
			return ((ENamedElement) object).getName();
		} else if (object instanceof EObject) {
			return ((EObject) object).toString();
		}
		return ((EObject) object).toString();
	}

	@Override
	public boolean canBeLandmark(String handle) {
		Object object = getObjectForHandle(handle);
		return object instanceof EClassifier;
	}

	@Override
	public boolean canFilter(Object element) {
		// ignore
		return true;
	}

	@Override
	public boolean isDocument(String handle) {
		URI uri = URI.createURI(handle);
		return uri.isFile();
	}

	@Override
	public String getHandleForOffsetInObject(Object resource, int offset) {
		// ignore
		return null;
	}

	@Override
	public String getContentType(String elementHandle) {
		return getContentType();
	}

}
