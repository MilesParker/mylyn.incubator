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

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * @author Benjamin Muskalla
 * @author milesparker
 */
public class EMFStructureBridge extends DomainAdaptedStructureBridge {

	public EMFStructureBridge(IDiagramContextBridge delegatedBridge) {
		super(delegatedBridge);
	}

	public String getDomainHandleIdentifier(Object object) {
			EObject eobject = ((EObject) object);
			URI uri = EcoreUtil.getURI(eobject);
			return uri.toString();
	}
	
	/**
	 * Must return a class that is an equivalent (e.g. a deep copy) but not
	 * necessarily the same object as the original mapped object. This allows us
	 * to make generic references to objects that might not have equivalent
	 * mappings in memory, e.g. an EMF object that is loaded from a new resource
	 * set.
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
			//this is a reasonable thing to happen in the case where the resource is no longer available.
		}
		return null;
	}
	
	@Override
	public String getLabel(Object object) {
		String label = delegatedBridge.getLabel(object);
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
		EObject eObject = (EObject) getObjectForHandle(handle);
		if (eObject != null) {
			return getHandleIdentifier(eObject.eContainer());
		}
		return null;
	}
}
