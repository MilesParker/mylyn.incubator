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

package org.eclipse.mylyn.internal.sandbox.ui.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.TransferDragSourceListener;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.context.core.AbstractContextListener;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionContext;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.context.ui.ActiveViewSelectionDragAdapter;
import org.eclipse.mylyn.internal.java.ui.JavaStructureBridge;
import org.eclipse.mylyn.internal.java.ui.JavaUiBridgePlugin;
import org.eclipse.mylyn.internal.sandbox.ui.JavaContextLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

/**
 * @author Mik Kersten
 */
public class ContextHierarchyView extends ViewPart {

	public static final String ID = "org.eclipse.mylyn.ui.views.active.hierarchy";

	private final TreeParent root = new TreeParent("<no hierarchy>");

	private TreeViewer viewer;

	private final Map<String, TreeParent> nodeMap = new HashMap<String, TreeParent>();

	final AbstractContextListener MODEL_LISTENER = new AbstractContextListener() {

		@Override
		public void contextActivated(IInteractionContext taskscape) {
			refreshHierarchy();
		}

		@Override
		public void contextDeactivated(IInteractionContext taskscape) {
			refreshHierarchy();
		}

		@Override
		public void contextCleared(IInteractionContext context) {
			refreshHierarchy();
		}

		@Override
		public void landmarkAdded(IInteractionElement element) {
			refreshHierarchy();
		}

		@Override
		public void landmarkRemoved(IInteractionElement element) {
			refreshHierarchy();
		}
	};

	class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			// ignore
		}

		public void dispose() {
			// don't care when we are disposed
		}

		public Object[] getElements(Object parent) {
			Object[] types = new Object[root.getChildren().length];
			for (int i = 0; i < root.getChildren().length; i++) {
				types[i] = root.getChildren()[i].getElement();
			}
			return types;
		}

		public Object getParent(Object child) {
			return null;
			// return ((TreeParent)child).getParent();
		}

		public Object[] getChildren(Object parent) {
			if (parent instanceof IType) {
				TreeParent node = nodeMap.get(((IType) parent).getHandleIdentifier());
				if (node != null) {
					Object[] types = new Object[node.getChildren().length];
					for (int i = 0; i < node.getChildren().length; i++) {
						types[i] = node.getChildren()[i].getElement();
					}
					return types;
				}
			}
			// if (parent instanceof TreeParent) {
			// return ((TreeParent)parent).getChildren();
			// }
			return new Object[0];
		}

		public boolean hasChildren(Object parent) {
			if (parent instanceof IType) {
				TreeParent node = nodeMap.get(((IType) parent).getHandleIdentifier());
				if (node != null) {
					return node.getChildren().length > 0;
				}
			}
			return false;
		}
	}

	private void refreshHierarchy() {
		refreshHierarchy(true);
	}

	/**
	 * Public for testing.
	 */
	public void refreshHierarchy(boolean asyncRefreshMode) {
		try {
			if (root != null && root.getChildren().length > 0) {
				root.removeAllChildren();
			}
			nodeMap.clear();
			Set<IInteractionElement> landmarks = ContextCore.getContextManager().getActiveLandmarks();
			for (IInteractionElement node : landmarks) {
				IJavaElement element = null;
				if (node.getContentType().equals(JavaStructureBridge.CONTENT_TYPE)) {
					element = JavaCore.create(node.getHandleIdentifier());
				}
				if (element != null && element instanceof IType && element.exists()) {
					IType type = (IType) element;
					ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
					if (hierarchy != null) {
						IType[] supertypes = hierarchy.getAllSuperclasses(type);
						List<IType> hierarchyTypes = new ArrayList<IType>(Arrays.asList(supertypes));
						Collections.reverse(hierarchyTypes);
						hierarchyTypes.add(type);
						addHierarchy(root, hierarchyTypes);
					}
				}
			}

			if (!asyncRefreshMode) { // for testing
				refreshViewer();
			} else {
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
						refreshViewer();
					}
				});
			}
		} catch (Throwable t) {
			StatusHandler.log(new Status(IStatus.ERROR, JavaUiBridgePlugin.ID_PLUGIN, "Could not update viewer", t));
		}
	}

	private void addHierarchy(TreeParent node, List<IType> hierarchyTypes) {
		if (hierarchyTypes.isEmpty()) {
			return;
		}
		IType type = hierarchyTypes.get(0);
		if (!type.equals(node.getElement())) {
			TreeParent newNode = nodeMap.get(type.getHandleIdentifier());
			if (newNode == null) {
				newNode = new TreeParent(type);
				nodeMap.put(type.getHandleIdentifier(), newNode);
				node.addChild(newNode);
			}
			addHierarchy(newNode, hierarchyTypes.subList(1, hierarchyTypes.size()));
		}
	}

	private void refreshViewer() {
		try {
			if (viewer != null && !viewer.getTree().isDisposed()) {
				viewer.getControl().setRedraw(false);
				viewer.refresh();
				viewer.expandAll();
				viewer.getControl().setRedraw(true);
			}
		} catch (Throwable t) {
			StatusHandler.log(new Status(IStatus.ERROR, JavaUiBridgePlugin.ID_PLUGIN, "Could not update viewer", t));
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		try {
			ContextCore.getContextManager().addListener(MODEL_LISTENER);
			refreshHierarchy();

			viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
			viewer.setContentProvider(new ViewContentProvider());
			viewer.setLabelProvider(new DecoratingLabelProvider(JavaContextLabelProvider.createJavaUiLabelProvider(),
					PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));
			viewer.setInput(getViewSite());

			viewer.addOpenListener(new IOpenListener() {
				public void open(OpenEvent event) {
					StructuredSelection selection = (StructuredSelection) viewer.getSelection();
					if (selection.getFirstElement() != null && selection.getFirstElement() instanceof IJavaElement) {
						IJavaElement element = (IJavaElement) selection.getFirstElement();
						try {
							IEditorPart part = JavaUI.openInEditor(element);
							JavaUI.revealInEditor(part, element);
						} catch (Throwable t) {
							StatusHandler.log(new Status(IStatus.ERROR, JavaUiBridgePlugin.ID_PLUGIN,
									"Could not open type", t));
						}
					}
				}
			});
			hookContextMenu();
			contributeToActionBars();

			initDrop();
			initDrag();
			getSite().setSelectionProvider(getViewer());
		} catch (Throwable t) {
			StatusHandler.log(new Status(IStatus.ERROR, JavaUiBridgePlugin.ID_PLUGIN, "Create failed", t));
		}
	}

	private void initDrop() {
		Transfer[] types = new Transfer[] { LocalSelectionTransfer.getInstance() };
		viewer.addDropSupport(DND.DROP_MOVE, types, new ActiveViewDropAdapter(viewer));
	}

	private void initDrag() {
		int ops = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
		Transfer[] transfers = new Transfer[] { LocalSelectionTransfer.getInstance(), ResourceTransfer.getInstance() };
		TransferDragSourceListener[] dragListeners = new TransferDragSourceListener[] { new ActiveViewSelectionDragAdapter(
				viewer) };//, new ActiveViewResourceDragAdapter(viewer) };
		viewer.addDragSupport(ops, transfers, new ActiveViewDelegatingDragAdapter(dragListeners));
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ContextHierarchyView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public TreeViewer getViewer() {
		return viewer;
	}
}

class TreeParent implements IAdaptable {
	protected IJavaElement element;

	protected TreeParent parent;

	private final List<TreeParent> children;

	private String relationshipName;

	public TreeParent(IJavaElement element) {
		this.element = element;
		children = new ArrayList<TreeParent>();
	}

	public TreeParent(String relationshipName) {
		this.relationshipName = relationshipName;
		children = new ArrayList<TreeParent>();
	}

	public String getName() {
		return element.getElementName();
	}

	public void setParent(TreeParent parent) {
		this.parent = parent;
	}

	public TreeParent getParent() {
		return parent;
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class key) {
		return element;
	}

	public IJavaElement getElement() {
		return element;
	}

	@Override
	public String toString() {
		if (getElement() == null) {
			return relationshipName;
		} else {
			return getName();
		}
	}

	public void addChild(TreeParent child) {
		children.add(child);
		child.setParent(this);
	}

	public void removeAllChildren() {
		for (TreeParent node : children) {
			if (node != null) {
				node.setParent(null);
			}
		}
		children.clear();
	}

	public void removeChild(TreeParent child) {
		children.remove(child);
		child.setParent(null);
	}

	public TreeParent[] getChildren() {
		return children.toArray(new TreeParent[children.size()]);
	}

	public boolean hasChildren() {
		return children.size() > 0;
	}
}
