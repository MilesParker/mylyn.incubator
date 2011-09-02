/*******************************************************************************
 * Copyright (c) 2011 Tasktop Technologies.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.sandbox.search.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.sandbox.search.ui.SearchResult;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

class OpenFileAction extends org.eclipse.ui.actions.OpenFileAction {

	private final IWorkbenchPage page;

	public OpenFileAction(IWorkbenchPage page) {
		super(page);
		this.page = page;
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		return !selection.isEmpty();
	}

	@Override
	public void run() {
		super.run();

		List<SearchResult> unopenable = new ArrayList<SearchResult>();

		List<?> selectedNonResources = getSelectedNonResources();
		for (Object object : selectedNonResources) {
			if (object instanceof SearchResult) {
				SearchResult result = (SearchResult) object;

				IFileStore store = EFS.getLocalFileSystem().getStore(new Path(result.getFile().getParent()));
				final IFileStore fileStore = store.getChild(result.getFile().getName());
				IFileInfo info = fileStore.fetchInfo();
				if (!info.isDirectory() && info.exists()) {
					try {
						IDE.openEditorOnFileStore(page, fileStore);
					} catch (PartInitException e) {
						String message = NLS.bind(Messages.OpenFileAction_CannotOpenFile, new String[] {
								result.getFile().getName(), e.getMessage() });
						SearchPlugin.getDefault()
								.getLog()
								.log(new Status(IStatus.ERROR, SearchPlugin.BUNDLE_ID, message, e));
						MessageDialog.openError(page.getWorkbenchWindow().getShell(),
								Messages.OpenFileAction_OpenFileErrorTitle, message);
					}
				} else {
					unopenable.add(result);
				}
			}
		}
		if (!unopenable.isEmpty()) {
			MessageDialog.openError(page.getWorkbenchWindow().getShell(), Messages.OpenFileAction_OpenFileErrorTitle,
					NLS.bind(Messages.OpenFileAction_OpenFileErrorMessage, unopenable.size()));
		}
	}

}
