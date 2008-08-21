/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.sandbox.ui.hyperlinks;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.ui.JavaUIMessages;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.progress.UIJob;

/**
 * Truncated class of JavaStackTraceFileHyperlink, open a hyperlink using OpenTypeAction
 * 
 * @author Rob Elves
 * @author Jingwen Ou
 * @author David Green fix bug 244352
 */
public class JavaResourceHyperlink implements IHyperlink {

	private final IRegion region;

	private final String typeName;

	public JavaResourceHyperlink(IRegion region, String typeName) {
		this.region = region;
		this.typeName = typeName;
	}

	public IRegion getHyperlinkRegion() {
		return region;
	}

	public String getHyperlinkText() {
		return "Open " + typeName;
	}

	public String getTypeLabel() {
		return null;
	}

	public void open() {
		startSourceSearch(typeName);
	}

	/**
	 * Starts a search for the type with the given name. Reports back to 'searchCompleted(...)'.
	 * 
	 * @param typeName
	 *            the type to search for
	 */
	protected void startSourceSearch(final String typeName) {
		Job search = new Job("Searching...") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					// search for the type in the workspace

					final List<IType> results = new ArrayList<IType>();

					SearchRequestor collector = new SearchRequestor() {
						@Override
						public void acceptSearchMatch(SearchMatch match) throws CoreException {
							Object element = match.getElement();
							if (element instanceof IType) {
								results.add((IType) element);
							}
						}
					};

					// do a case-sensitive search for the class name see bug 244352

					SearchEngine engine = new SearchEngine();
					SearchPattern pattern = SearchPattern.createPattern(typeName, IJavaSearchConstants.TYPE,
							IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH
									| SearchPattern.R_CASE_SENSITIVE);
					engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
							SearchEngine.createWorkspaceScope(), collector, monitor);

					searchCompleted(results, typeName, null);

				} catch (CoreException e) {
					searchCompleted(null, typeName, e.getStatus());
				}
				return Status.OK_STATUS;
			}

		};
		search.schedule();
	}

	protected void searchCompleted(final List<IType> sources, final String typeName, final IStatus status) {
		UIJob job = new UIJob("link search complete") { //$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (sources.size() > 1) {
					openTypeDialog(sources, typeName);
				} else if (sources.size() == 1 && sources.get(0) != null) {
					IType type = sources.get(0);
					processSearchResult(type, typeName);
				} else {
					MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							"Open Type", "Type could not be located.");
				}

				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	/**
	 * The search succeeded with the given result
	 * 
	 * @param source
	 *            resolved source object for the search
	 * @param typeName
	 *            type name searched for
	 */
	protected void processSearchResult(Object source, String typeName) {
		IDebugModelPresentation presentation = JDIDebugUIPlugin.getDefault().getModelPresentation();
		IEditorInput editorInput = presentation.getEditorInput(source);
		if (editorInput != null) {
			String editorId = presentation.getEditorId(editorInput, source);
			if (editorId != null) {
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(editorInput,
							editorId);

				} catch (CoreException e) {
					MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							"Open Type", "Failed to open type.");
				}
			}
		}
	}

	private void openTypeDialog(final List<IType> sources, final String typeName) {
		ListDialog dialog = new ListDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		dialog.setInput(sources.toArray());
		dialog.setContentProvider(new ArrayContentProvider());
		dialog.setLabelProvider(new JavaElementLabelProvider() {
			@Override
			public StyledString getStyledText(Object element) {
				IType type = (IType) element;
				StyledString styledString = super.getStyledText(type);

				IPackageFragment fragment = type.getPackageFragment();
				if (fragment != null) {
					styledString.append(JavaElementLabels.CONCAT_STRING);
					styledString.append(super.getText(fragment));
				}

				IJavaProject project = type.getJavaProject();
				if (project != null) {
					styledString.append(JavaElementLabels.CONCAT_STRING);
					styledString.append(super.getText(project));
				}

				return styledString;
			}

			@Override
			public String getText(Object element) {
				IType type = (IType) element;
				StringBuilder builder = new StringBuilder();
				builder.append(super.getText(type));

				IPackageFragment fragment = type.getPackageFragment();
				if (fragment != null) {
					builder.append(JavaElementLabels.CONCAT_STRING);
					builder.append(super.getText(fragment));
				}

				IJavaProject project = type.getJavaProject();
				if (project != null) {
					builder.append(JavaElementLabels.CONCAT_STRING);
					builder.append(super.getText(project));
				}

				return builder.toString();
			}
		});

		dialog.setTitle("Open Hyperlink");
		dialog.setMessage("More than one types are detected, please select one:");
		dialog.setHelpAvailable(false);

		int result = dialog.open();
		if (result != IDialogConstants.OK_ID) {
			return;
		}

		Object[] types = dialog.getResult();
		if (types != null && types.length > 0) {
			IType type = null;
			for (Object type2 : types) {
				type = (IType) type2;
				try {
					JavaUI.openInEditor(type, true, true);
				} catch (CoreException x) {
					ExceptionHandler.handle(x, JavaUIMessages.OpenTypeAction_errorTitle,
							JavaUIMessages.OpenTypeAction_errorMessage);
				}
			}
		}
	}
}
