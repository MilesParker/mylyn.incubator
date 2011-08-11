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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
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
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public abstract class MylynDecoratorProvider extends AbstractProvider implements
		IDecoratorProvider {

	public static final String MYLYN_MARKER = "mylyn-marker";

	public static final String MYLYN_DETAIL = "mylyn-detail";

	public static final String MYLYN_INTERESTING = "mylyn-interesting";

	public static final String MYLYN_BORING = "mylyn-boring";

	private Map<String, MylynDecorator> decoratorForModel;

	private DomainAdaptedStructureBridge structure;

	private boolean anyContextActive;

	AbstractContextListener contextListenerAdapter = new AbstractContextListener() {
		public void contextChanged(ContextChangeEvent event) {
			MylynDecoratorProvider.this.contextChanged(event);
		}
	};

	public MylynDecoratorProvider() {
		ContextCore.getContextManager().addListener(contextListenerAdapter);
		decoratorForModel = new HashMap<String, MylynDecorator>();
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
	}

	public boolean provides(IOperation operation) {
		if (operation instanceof CreateDecoratorsOperation) {
			CreateDecoratorsOperation cdo = (CreateDecoratorsOperation) operation;
			IDecoratorTarget target = cdo.getDecoratorTarget();
			View view = (View) target.getAdapter(View.class);
			Object candidate = getStructure().getDomainObject(view);
			if (candidate instanceof EObject) {
				EObject domainObject = (EObject) candidate;
				IGraphicalEditPart targetPart = (IGraphicalEditPart) target
						.getAdapter(IGraphicalEditPart.class);
				return getStructure().acceptsObject(domainObject)
						&& getDomainUIBridge().acceptsEditPart(domainObject,
								targetPart);
			}
		}
		return false;
	}

	public void hookEditor() {
		// decorationFigure.addMouseMotionListener(new
		// MouseMotionListener.Stub() {
		// public void mouseEntered(MouseEvent me) {
		// Animation.markBegin();
		// // decorationFigure.setAlpha(150);
		// // part.getFigure().getLayoutManager().layout(part.getFigure());
		// getDecoratorTarget().removeDecoration(lastDecoration);
		// Animation.run(2000);
		// }
		//
		// // public void mouseExited(MouseEvent me) {
		// // Animation.markBegin();
		// // decorationFigure.setAlpha(255);
		// // decorationFigure.validate();
		// // // part.getFigure().revalidate();
		// // // getDecoratorTarget().removeDecoration(lastDecoration);
		// // Animation.run(2000);
		// // }
		// });
		// part.getFigure().addMouseMotionListener(new
		// MouseMotionListener.Stub() {
		// @Override
		// public void mouseExited(MouseEvent me) {
		// Animation.markBegin();
		// // decorationFigure.setAlpha(0);
		// // decorationFigure.validate();
		// lastDecoration = getDecoratorTarget().addDecoration(
		// decorationFigure, new NodeLocator(decorated), false);
		// Animation.run(2000);
		// }
		// });
	}

	public void createDecorators(IDecoratorTarget target) {
		IGraphicalEditPart targetPart = (IGraphicalEditPart) target
				.getAdapter(IGraphicalEditPart.class);
		Object model = targetPart.getModel();
		if (model instanceof View) {
			model = ((View) model).getElement();
		}
		EObject domainObject = (EObject) getStructure().getDomainObject(model);
		MylynDecorator mylynDecorator = new MylynDecorator(this, target,
				domainObject);
		target.installDecorator(MYLYN_DETAIL, mylynDecorator);
		decoratorForModel.put(structure.getHandleIdentifier(domainObject),
				mylynDecorator);
	}

	private void refreshEditors() {
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null) {
			IEditorReference[] editorReferences = activeWorkbenchWindow
					.getActivePage().getEditorReferences();
			for (IEditorReference reference : editorReferences) {
				IEditorPart editor = reference.getEditor(false);
				RootEditPart root = getRootEditPart(editor);
				if (root != null) {
					root.refresh();
				}
			}
		}
	}

	private RootEditPart getRootEditPart(IEditorPart editor) {
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
		IInteractionElement interation = ContextCore.getContextManager()
				.getActiveContext()
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

	public void contextChanged(ContextChangeEvent event) {
		if (event.getEventKind() == ContextChangeKind.ACTIVATED) {
			anyContextActive = true;
			for (Entry<String, MylynDecorator> entry : decoratorForModel
					.entrySet()) {
				entry.getValue().refresh();
			}
			// for (IInteractionElement element : event.getContext()
			// .getAllElements()) {
			// refreshElement(element);
			// }
		} else if (event.getEventKind() == ContextChangeKind.DEACTIVATED) {
			anyContextActive = false;
			for (Entry<String, MylynDecorator> entry : decoratorForModel
					.entrySet()) {
				entry.getValue().deactivate();
			}
			decoratorForModel.clear();
		} else {
			List<IInteractionElement> elements = event.getElements();
			for (IInteractionElement element : elements) {
				refreshElement(element);
			}
		}
		refreshEditors();
	}

	private void refreshElement(IInteractionElement element) {
		if (element.getContentType().equals(
				getDomainUIBridge().getContentType())) {
			MylynDecorator mylynDecorator = decoratorForModel.get(element
					.getHandleIdentifier());
			if (mylynDecorator != null) {
				mylynDecorator.refresh();
			}
		}
	}

	public abstract IModelUIProvider getDomainUIBridge();

	public DomainAdaptedStructureBridge getStructure() {
		if (structure == null) {
			structure = (DomainAdaptedStructureBridge) ContextCore
					.getStructureBridge(getDomainUIBridge().getContentType());
		}
		return structure;
	}
}
