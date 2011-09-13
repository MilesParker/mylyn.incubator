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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramGraphicalViewer;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.CreateDecoratorsOperation;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorProvider;
import org.eclipse.gmf.runtime.diagram.ui.services.decorator.IDecoratorTarget;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.mylyn.context.core.AbstractContextListener;
import org.eclipse.mylyn.context.core.ContextChangeEvent;
import org.eclipse.mylyn.context.core.ContextChangeEvent.ContextChangeKind;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.modeling.ui.ModelingUiPlugin;
import org.eclipse.mylyn.modeling.context.DomainModelContextStructureBridge;
import org.eclipse.mylyn.modeling.ui.DiagramUiBridge;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * @author Miles Parker
 */
public abstract class ContextDecoratorProvider extends AbstractProvider implements IDecoratorProvider, IPartListener {

	public static final String MYLYN_MARKER = "mylyn-marker"; //$NON-NLS-1$

	public static final String MYLYN_DETAIL = "mylyn-detail"; //$NON-NLS-1$

	public static final String MYLYN_INTERESTING = "mylyn-interesting"; //$NON-NLS-1$

	public static final String MYLYN_BORING = "mylyn-boring"; //$NON-NLS-1$

	private final Map<String, Collection<ContextDecorator>> decoratorsForModel;

	private final Map<RootEditPart, RevealMouseListener> listenerForRoot;

	private DomainModelContextStructureBridge structure;

	private boolean anyContextActive;

	private boolean enabled;

	private final AbstractContextListener contextListenerAdapter = new AbstractContextListener() {
		@Override
		public void contextChanged(ContextChangeEvent event) {
			ContextDecoratorProvider.this.contextChanged(event);
		}
	};

	private final Collection<RootEditPart> diagramParts;

	private final IPropertyChangeListener preferenceListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(ModelingUiPlugin.FOCUSSING_ENABLED)) {
				enabled = Boolean.parseBoolean(event.getNewValue().toString());
				refresh();
			}
		}
	};

	public ContextDecoratorProvider() {
		ContextCore.getContextManager().addListener(contextListenerAdapter);
		decoratorsForModel = new HashMap<String, Collection<ContextDecorator>>();
		listenerForRoot = new HashMap<RootEditPart, RevealMouseListener>();
		// workbench should be active as this is instantiated by GMF
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow.getActivePage() == null) {
			activeWorkbenchWindow.addPageListener(new IPageListener() {

				public void pageOpened(IWorkbenchPage page) {
				}

				public void pageClosed(IWorkbenchPage page) {
				}

				public void pageActivated(IWorkbenchPage page) {
					page.addPartListener(ContextDecoratorProvider.this);
				}
			});
		} else {
			// Not sure if we'll ever get this situation, but it's worth covering
			activeWorkbenchWindow.getActivePage().addPartListener(ContextDecoratorProvider.this);
		}
		diagramParts = new HashSet<RootEditPart>();

		ModelingUiPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(preferenceListener);
		enabled = ModelingUiPlugin.getDefault().getPreferenceStore().getBoolean(ModelingUiPlugin.FOCUSSING_ENABLED);
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
						&& getDomainUIBridge().acceptsViewObject(domainObject, targetPart);
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
			ContextDecorator edgeDecorator = new EdgeDecorator(this, target, domainSource, domainTarget);
			target.installDecorator(MYLYN_DETAIL, edgeDecorator);
			addDecorator(domainSource, edgeDecorator);
			addDecorator(domainTarget, edgeDecorator);
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

	public Collection<RootEditPart> getRootEditParts() {
		return diagramParts;
	}

	private RootEditPart getRootEditPart(IWorkbenchPart editor) {
		if (getDomainUIBridge().acceptsPart(editor)) {
			if (editor instanceof DiagramEditor) {
				DiagramEditor de = (DiagramEditor) editor;
				return de.getDiagramEditPart().getRoot();
			} else {
				// Seems to be the only way to get Papyrus root edit
				// part w/o explicit dependencies..
				IDiagramGraphicalViewer viewer = (IDiagramGraphicalViewer) editor.getAdapter(IDiagramGraphicalViewer.class);
				if (viewer != null) {
					return viewer.getRootEditPart();
				}
			}
		}
		return null;
	}

	private IInteractionElement getRecentInteraction(EObject object) {
		return ContextCore.getContextManager().getActiveContext().get(getStructure().getHandleIdentifier(object));
	}

	public boolean isInteresting(EObject object) {
		IInteractionElement interation = getRecentInteraction(object);
		return interation != null && interation.getInterest().isInteresting();
	}

	public boolean isLandmark(EObject object) {
		IInteractionElement interation = getRecentInteraction(object);
		return interation != null && interation.getInterest().isLandmark();
	}

	/**
	 * Should Mylyn manage this object?
	 * 
	 * @param object
	 * @return
	 */
	public boolean isFocussed() {
		return enabled && anyContextActive;
	}

	private void deactivate(IWorkbenchPart part) {
		RootEditPart rootEditPart = getRootEditPart(part);
		if (rootEditPart != null) {
			//TODO are we confident that this won't be removed?
			diagramParts.remove(rootEditPart);
			for (Collection<ContextDecorator> values : decoratorsForModel.values()) {
				Collection<ContextDecorator> removedDecorators = new HashSet<ContextDecorator>();
				for (ContextDecorator decorator : values) {
					IGraphicalEditPart decoratorEditPart = (IGraphicalEditPart) decorator.getTarget().getAdapter(
							IGraphicalEditPart.class);
					if (decoratorEditPart.getRoot() == rootEditPart) {
						decorator.deactivate();
						removedDecorators.add(decorator);
					}
				}
				values.removeAll(removedDecorators);
			}
			listenerForRoot.remove(rootEditPart);
		}
	}

	void refresh(RootEditPart root) {
		diagramParts.add(root);
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
		if (anyContextActive) {
			for (RootEditPart root : getRootEditParts()) {
				refresh(root);
			}
			for (Collection<ContextDecorator> values : decoratorsForModel.values()) {
				refresh(values);
			}
		} else {
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
	}

	void refresh(IInteractionElement element) {
		if (element.getContentType().equals(getDomainUIBridge().getContentType())) {
			String handleIdentifier = element.getHandleIdentifier();
			Collection<ContextDecorator> values = decoratorsForModel.get(handleIdentifier);
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

	public DomainModelContextStructureBridge getStructure() {
		if (structure == null) {
			structure = (DomainModelContextStructureBridge) ContextCore.getStructureBridge(getDomainUIBridge().getContentType());
		}
		return structure;
	}

	public void partActivated(IWorkbenchPart part) {
		refresh(part);
	}

	public void partBroughtToTop(IWorkbenchPart part) {
		refresh(part);
	}

	public void partClosed(IWorkbenchPart part) {
		deactivate(part);
	}

	public void partDeactivated(IWorkbenchPart part) {
	}

	public void partOpened(IWorkbenchPart part) {
		if (getDomainUIBridge().acceptsPart(part) && part instanceof IEditorPart) {
			IEditorPart ep = (IEditorPart) part;
//			if (ep instanceof IEditingDomainProvider) {
//				IEditingDomainProvider edp = (IEditingDomainProvider) ep;
//				EditingDomain editingDomain = edp.getEditingDomain();
//				if (editingDomain instanceof AdapterFactoryEditingDomain) {
//					AdapterFactory adapterFactory = ((AdapterFactoryEditingDomain) editingDomain).getAdapterFactory();
//					System.err.println(adapterFactory);
//				}
//			}
		}
	}

	public void contextChanged(ContextChangeEvent event) {
		if (event.getEventKind() == ContextChangeKind.ACTIVATED) {
			anyContextActive = true;
			refresh();
		} else if (event.getEventKind() == ContextChangeKind.DEACTIVATED) {
			anyContextActive = false;
			refresh();
		} else {
			refresh(event);
		}
	}

	public RevealMouseListener getListenerForRoot(RootEditPart part) {
		return listenerForRoot.get(part);
	}

	public abstract DiagramUiBridge getDomainUIBridge();
}
