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

package org.eclipse.mylyn.internal.emf.ui;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.mylyn.context.core.AbstractContextStructureBridge;

/**
 * @author Benjamin Muskalla
 */
public class EmfStructureBridge extends AbstractContextStructureBridge {

	private static final String ECORE_CONTENT_TYPE = "emf"; //$NON-NLS-1$

	@Override
	public String getContentType() {
		return ECORE_CONTENT_TYPE;
	}

	@Override
	public String getHandleIdentifier(Object object) {
		if (object instanceof EObject) {
			EObject eobject = ((EObject) object);
			IFile file = ResourcesPlugin.getWorkspace()
					.getRoot()
					.getFile(new Path(eobject.eResource().getURI().toString()));
			return file.getFullPath().toString() + ";" + eobject.eResource().getURIFragment(eobject);
		}
		return null;
	}

	@Override
	public String getParentHandle(String handle) {
		return getHandleIdentifier(((EObject) getObjectForHandle(handle)).eContainer());
	}

	@Override
	public Object getObjectForHandle(String handle) {
		String[] fileName = handle.split(";"); //$NON-NLS-1$
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(fileName[0]));

		ResourceSetImpl resourceSetImpl = new ResourceSetImpl();
		URI uri = URI.createURI(file.getFullPath().toString());
		Resource resource = resourceSetImpl.getResource(uri, true);
		EObject eObject = resource.getEObject(fileName[1]);
		return eObject;
	}

	@Override
	public List<String> getChildHandles(String handle) {
		// ignore
		return null;
	}

	@Override
	public String getLabel(Object object) {
		return ((EObject) object).toString();
	}

	@Override
	public boolean canBeLandmark(String handle) {
		// ignore
		return false;
	}

	@Override
	public boolean acceptsObject(Object object) {
		return object instanceof EObject;
	}

	@Override
	public boolean canFilter(Object element) {
		// ignore
		return false;
	}

	@Override
	public boolean isDocument(String handle) {
		// ignore
		return false;
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
