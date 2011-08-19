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

package org.eclipse.mylyn.modeling.emf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.mylyn.modeling.context.DomainDelegatedStructureBridge;

/**
 * @author Benjamin Muskalla
 * @author Miles Parker
 */
public abstract class EmfStructureBridge extends DomainDelegatedStructureBridge {

	@Override
	public String getDomainHandleIdentifier(Object object) {
		EObject eobject = ((EObject) object);
		URI uri = EcoreUtil.getURI(eobject);
		return uri.toString();
	}

	/**
	 * Must return a class that is an equivalent (e.g. a deep copy) but not necessarily the same object as the original
	 * mapped object. This allows us to make generic references to objects that might not have equivalent mappings in
	 * memory, e.g. an EMF object that is loaded from a new resource set.
	 */
	@Override
	public Object getDomainObjectForHandle(String handle) {
		URI uri = URI.createURI(handle);
		ResourceSetImpl resourceSetImpl = new ResourceSetImpl();
		try {
			EObject eObject = resourceSetImpl.getEObject(uri, true);
			if (eObject != null) {
				return eObject;
			}
			return resourceSetImpl.getResource(uri, true);
		} catch (WrappedException e) {
			// this is a reasonable thing to happen in the case where the resource is no longer available.
		}
		return null;
	}

	@Override
	public List<String> getChildHandles(String handle) {
		Object domainObject = getDomainObjectForHandle(handle);
		if (domainObject instanceof EObject) {
			List<String> childHandles = new ArrayList<String>();
			EObject eo = (EObject) domainObject;
			for (EObject child : eo.eContents()) {
				childHandles.add(getDomainHandleIdentifier(child));
			}
			return childHandles;
		}
		return Collections.emptyList();
	}

	@Override
	public String getLabel(Object object) {
		String label = getDomainContextBridge().getLabel(object);
		if (label != null) {
			return label;
		}
		if (object instanceof ENamedElement) {
			return ((ENamedElement) object).getName();
		}
		return super.getLabel(object.toString());
	}

	@Override
	public boolean isDocument(String handle) {
		URI uri = URI.createURI(handle);
		return uri.isFile();
	}

	@Override
	public String getParentHandle(String handle) {
		Object object = getObjectForHandle(handle);
		if (object instanceof EObject) {
			EObject eObject = (EObject) object;
			if (eObject.eContainer() != null) {
				return getHandleIdentifier(eObject.eContainer());
			} else {
				//must be base package
				return getHandleIdentifier(eObject.eResource());
			}
		}
		//Resources don't have parents, unless we want to get the file hierarchy, which probably isn't what we want.
		return null;
	}

}
