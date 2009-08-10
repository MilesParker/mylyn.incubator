/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.tasks.ui.editors;

import org.eclipse.jface.viewers.IElementComparer;

/**
 * @deprecated Do not use. This class is pending for removal: see bug 237552.
 */
@Deprecated
public class RepositoryTaskOutlineComparer implements IElementComparer {

	public boolean equals(Object a, Object b) {
		if ((a instanceof IRepositoryTaskSelection) && (b instanceof IRepositoryTaskSelection)) {
			IRepositoryTaskSelection s1 = (IRepositoryTaskSelection) a;
			IRepositoryTaskSelection s2 = (IRepositoryTaskSelection) b;

			// An IRepositoryTaskSelection is uniquely defined by its handle and
			// its contents
			return ((ContentOutlineTools.getHandle(s1).equals(ContentOutlineTools.getHandle(s2))) && ((s1.getContents() == null) ? (s2.getContents() == null)
					: s1.getContents().equals(s2.getContents())));
		}
		return a.equals(b);
	}

	public int hashCode(Object element) {
		if (element instanceof IRepositoryTaskSelection) {
			IRepositoryTaskSelection sel = (IRepositoryTaskSelection) element;

			// An IRepositoryTaskSelection is uniquely defined by its handle and
			// its contents
			return (ContentOutlineTools.getHandle(sel) + sel.getContents()).hashCode();
		}
		return element.hashCode();
	}
}
