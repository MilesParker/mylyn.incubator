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

package org.eclipse.mylyn.modeling.gmf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramGraphicalViewer;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.CreateDecoratorsOperation;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorProvider;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorTarget;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.mylyn.context.core.AbstractContextListener;
import org.eclipse.mylyn.context.core.ContextChangeEvent;
import org.eclipse.mylyn.context.core.ContextChangeEvent.ContextChangeKind;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.modeling.context.DomainAdaptedStructureBridge;
import org.eclipse.mylyn.modeling.ui.IModelUIProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public abstract class MylynDecoratorProvider extends AbstractProvider implements IDecoratorProvider, IPartListener {

	public static final String MYLYN_MARKER = "mylyn-marker";

	public static final String MYLYN_DETAIL = "mylyn-detail";

	public static final String MYLYN_INTERESTING = "mylyn-interesting";

	public static final String MYLYN_BORING = "mylyn-boring";

	private Map<String, Collection<ContextDecorator>> decoratorsForModel;

	private Map<RootEditPart, RevealMouseListener> listenerForRoot;

	private DomainAdaptedStructureBridge structure;

	private boolean anyContextActive;

	private AbstractContextListener contextListenerAdapter = new AbstractContextListener() {
		public void contextChanged(ContextChangeEvent event) {
			MylynDecoratorProvider.this.contextChanged(event);
		}
	};

	public MylynDecoratorProvider() {
		ContextCore.getContextManager().addListener(contextListenerAdapter);
		decoratorsForModel = new HashMap<String, Collection<ContextDecorator>>();
		listenerForRoot = new HashMap<RootEditPart, RevealMouseListener>();
		// workbench should be active as this is instantiated by GMF
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow.getActivePage() == null) {
			activeWorkbenchWindow.addPageListener(new IPageListener() {

				@Override
				public void pageOpened(IWorkbenchPage page) {
				}

				@Override
				public void pageClosed(IWorkbenchPage page) {
				}

				@Override
				public void pageActivated(IWorkbenchPage page) {
					page.addPartListener(MylynDecoratorProvider.this);
				}
			});
		} else {
			// Not sure if we'll ever get this situation, but it's worth covering
			activeWorkbenchWindow.getActivePage().addPartListener(MylynDecoratorProvider.this);
		}
	}

	public boolean provides(IOperation operation) {
		if (operation instanceof CreateDecoratorsOperation) {
			CreateDecoratorsOperation cdo = (CreateDecoratorsOperation) operation;
			IDecoratorTarget target = cdo.getDecoratorTarget();
			IGraphicalEditPart targetPart = (IGraphicalEditPart) target.getAdapter(IGraphicalEditPart.class);
			return accepts(targetPart);
		}
		return false;
	}

	private boolean accepts(IGraphicalEditPart targetPart) {
		if (targetPart instanceof ConnectionEditPart) {
			ConnectionEditPart connection = (ConnectionEditPart) targetPart;
			// TODO How could there not be graphical? Why doesn't the GEF API
			// make that assumption?
			IGraphicalEditPart connectionSource = (IGraphicalEditPart) connection.getSource();
			IGraphicalEditPart connectionTarget = (IGraphicalEditPart) connection.getTarget();
			// Only care if we care about sources and target
			return accepts(connectionSource) && accepts(connectionTarget);
		} else {
			Object candidate = getStructure().getDomainObject(targetPart.getModel());
			if (candidate instanceof EObject) {
				EObject domainObject = (EObject) candidate;
				return getStructure().acceptsObject(domainObject)
						&& getDomainUIBridge().acceptsEditPart(domainObject, targetPart);
			}
		}
		return false;
	}

	public void createDecorators(IDecoratorTarget target) {
		IGraphicalEditPart targetPart = (IGraphicalEditPart) target.getAdapter(IGraphicalEditPart.class);
		if (targetPart instanceof ConnectionEditPart) {
			ConnectionEditPart connectionPart = (ConnectionEditPart) targetPart;
			EObject domainSource = (EObject) getStructure().getDomainObject(connectionPart.getSource().getModel());
			EObject domainTarget = (EObject) getStructure().getDomainObject(connectionPart.getTarget().getModel());
			ContextDecorator mylynDecorator = new EdgeDecorator(this, target, domainSource, domainTarget);
			target.installDecorator(MYLYN_DETAIL, mylynDecorator);
			addDecorator(domainSource, mylynDecorator);
			addDecorator(domainTarget, mylynDecorator);
		} else {
			Object model = targetPart.getModel();
			View view = null;
			if (model instanceof View) {
				view = (View) model;
				model = view.getElement();
			}
			EObject domainObject = (EObject) getStructure().getDomainObject(model);
			ContextDecorator mylynDecorator = new NodeDecorator(this, target, domainObject);
			addDecorator(domainObject, mylynDecorator);
		}
	}

	private void addDecorator(EObject domainObject, ContextDecorator mylynDecorator) {
		String handle = structure.getHandleIdentifier(domainObject);
		Collection<ContextDecorator> list = decoratorsForModel.get(handle);
		if (list == null) {
			list = new HashSet<ContextDecorator>();
			decoratorsForModel.put(handle, list);
		}
		list.add(mylynDecorator);
	}

	private Collection<RootEditPart> getRootEditParts() {
		Collection<RootEditPart> parts = new ArrayList<RootEditPart>();
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null) {
			IEditorReference[] editorReferences = activeWorkbenchWindow.getActivePage().getEditorReferences();
			for (IEditorReference reference : editorReferences) {
				IEditorPart editor = reference.getEditor(false);
				final RootEditPart root = getRootEditPart(editor);
				if (root != null) {
					parts.add(root);
				}
			}
		}
		return parts;
	}

	private RootEditPart getRootEditPart(IWorkbenchPart editor) {
		if (getDomainUIBridge().acceptsPart(editor)) {
			if (editor instanceof DiagramEditor) {
				DiagramEditor de = (DiagramEditor) editor;
				return de.getDiagramEditPart().getRoot();
			} else {
				// Seems to be the only way to get Papyrus root edit
				// part w/o explicit dependencies..
				IDiagramGraphicalViewer viewer = (IDiagramGraphicalViewer) editor
						.getAdapter(IDiagramGraphicalViewer.class);
				return viewer.getRootEditPart();
			}
		}
		return null;
	}

	public boolean isInteresting(EObject object) {
		IInteractionElement interation = ContextCore.getContextManager().getActiveContext()
				.get(getStructure().getHandleIdentifier(object));
		return interation != null && interation.getInterest().isInteresting();
	}

	/**
	 * Should Mylyn manage this object?
	 * 
	 * @param object
	 * @return
	 */
	public boolean isFocussed() {
		return anyContextActive;
	}

	private void deactivate() {
		anyContextActive = false;
		for (Collection<ContextDecorator> values : decoratorsForModel.values()) {
			for (ContextDecorator decorator : values) {
				decorator.deactivate();
			}
		}
		for (RootEditPart root : getRootEditParts()) {
			RevealMouseListener revealMouseListener = listenerForRoot.get(root);
			if (revealMouseListener != null) {
				root.getViewer().getControl().removeMouseMoveListener(revealMouseListener);
			}
			root.refresh();
		}
		decoratorsForModel.clear();
		listenerForRoot.clear();
	}

	private void deactivate(IWorkbenchPart part) {
		if (part instanceof DiagramEditor) {
			DiagramEditor editor = (DiagramEditor) part;
			for (Collection<ContextDecorator> values : decoratorsForModel.values()) {
				Collection<ContextDecorator> removedDecorators = new HashSet<ContextDecorator>();
				for (ContextDecorator decorator : values) {
					IGraphicalEditPart editPart = (IGraphicalEditPart) decorator.getTarget().getAdapter(
							IGraphicalEditPart.class);
					if (editPart.getDiagramEditDomain() == editor.getDiagramEditDomain()) {
						removedDecorators.add(decorator);
					}
				}
				values.removeAll(removedDecorators);
			}
			listenerForRoot.remove(editor.getDiagramEditPart().getRoot());
		}
	}

	private void activate() {
		anyContextActive = true;
		refresh();
	}

	void refresh(RootEditPart root) {
		RevealMouseListener revealMouseListener = listenerForRoot.get(root);
		if (revealMouseListener == null) {
			IFigure rootFigure = ((AbstractGraphicalEditPart) root.getViewer().getRootEditPart()).getFigure();
			revealMouseListener = new RevealMouseListener(rootFigure);
			listenerForRoot.put(root, revealMouseListener);
			root.getViewer().getControl().addMouseMoveListener(revealMouseListener);
		}
		root.refresh();
	}

	void refresh(IWorkbenchPart part) {
		RootEditPart rootEditPart = getRootEditPart(part);
		if (rootEditPart != null) {
			refresh(rootEditPart);
		}
	}

	void refresh() {
		for (RootEditPart root : getRootEditParts()) {
			refresh(root);
		}
		for (Collection<ContextDecorator> values : decoratorsForModel.values()) {
			refresh(values);
		}
	}

	void refresh(IInteractionElement element) {
		if (element.getContentType().equals(getDomainUIBridge().getContentType())) {
			Collection<ContextDecorator> values = decoratorsForModel.get(element.getHandleIdentifier());
			if (values != null) {
				refresh(values);
			}
		}
	}

	private void refresh(Collection<ContextDecorator> values) {
		for (ContextDecorator decorator : values) {
			decorator.refresh();
		}
	}
	
	void refresh(ContextChangeEvent event) {
		List<IInteractionElement> elements = event.getElements();
		for (IInteractionElement element : elements) {
			refresh(element);
		}
	}

	public DomainAdaptedStructureBridge getStructure() {
		if (structure == null) {
			structure = (DomainAdaptedStructureBridge) ContextCore.getStructureBridge(getDomainUIBridge()
					.getContentType());
		}
		return structure;
	}

	@Override
	public void partActivated(IWorkbenchPart part) {
		refresh(part);
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		deactivate(part);
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
		// deactivate(part);
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
	}

	public void contextChanged(ContextChangeEvent event) {
		if (event.getEventKind() == ContextChangeKind.ACTIVATED) {
			activate();
		} else if (event.getEventKind() == ContextChangeKind.DEACTIVATED) {
			deactivate();
		} else {
			refresh(event);
		}
	}
	
	public RevealMouseListener getListenerForRoot(RootEditPart part) {
		return listenerForRoot.get(part);
	}
	
	public abstract IModelUIProvider getDomainUIBridge();
}
