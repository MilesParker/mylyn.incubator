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

package org.eclipse.mylyn.internal.modeling.ecoretools;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecoretools.diagram.outline.EcoreDiagramOutlinePage;
import org.eclipse.emf.ecoretools.diagram.part.EcoreDiagramEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.mylyn.context.core.ContextChangeEvent;
import org.eclipse.mylyn.modeling.gmf.ContextDecoratorProvider;
import org.eclipse.mylyn.modeling.ui.DiagramUiBridge;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.contentoutline.ContentOutline;

/**
 * @author Miles Parker
 */
public class EcoreDiagramDecoratorProvider extends ContextDecoratorProvider {

	private TreeViewer outlineTreeViewer;

	public EcoreDiagramDecoratorProvider() {
		super();
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPageListener(new IPageListener() {

			public void pageOpened(IWorkbenchPage page) {
				// ignore

			}

			public void pageClosed(IWorkbenchPage page) {
				// ignore

			}

			public void pageActivated(IWorkbenchPage page) {
				for (IViewReference vr : page.getViewReferences()) {
					if (vr.getId().equals("org.eclipse.ui.views.ContentOutline")) {
						handleContentOutline(vr.getView(false));
					}
				}
			}
		});
	}

	@Override
	public DiagramUiBridge getDomainUIBridge() {
		return EcoreDiagramUiBridge.getInstance();
	}

	private void handleContentOutline(IWorkbenchPart part) {
		EcoreDiagramOutlinePage outlinePage = (EcoreDiagramOutlinePage) ((ContentOutline) part).getCurrentPage();
		outlineTreeViewer = (TreeViewer) outlinePage.getSite().getSelectionProvider();
		ViewerFilter interestFilter = new ViewerFilter() {

			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				// ignore
				return element instanceof EPackage || isInteresting(element);
			}
		};
		outlineTreeViewer.setFilters(new ViewerFilter[] { interestFilter });
		outlineTreeViewer.refresh();
	}

	@Override
	public void contextChanged(ContextChangeEvent event) {
		super.contextChanged(event);
		if (outlineTreeViewer != null) {
			outlineTreeViewer.refresh();
		}
	}

	@Override
	public void partActivated(IWorkbenchPart part) {
		super.partActivated(part);
		if (part instanceof ContentOutline) {
			handleContentOutline(part);
		}
		if (part instanceof EcoreDiagramEditor && outlineTreeViewer != null) {
			outlineTreeViewer.refresh();
		}
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
		if (part instanceof ContentOutline) {
			handleContentOutline(part);
		}
	}
}
