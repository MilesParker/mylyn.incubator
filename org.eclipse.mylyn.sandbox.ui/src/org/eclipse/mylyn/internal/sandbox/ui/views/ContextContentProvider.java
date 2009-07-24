/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.context.core.AbstractContextStructureBridge;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.context.core.IInteractionRelation;
import org.eclipse.mylyn.internal.context.core.InteractionContextRelation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewSite;

/**
 * @author Mik Kersten
 */
public class ContextContentProvider implements IStructuredContentProvider, ITreeContentProvider {

	private IViewSite site = null;

	private Tree tree;

	private Shell shell = null;

	private final boolean landmarkOnlyMode;

	public ContextContentProvider(Tree tree, IViewSite site, boolean landmarkOnlyMode) {
		this.tree = tree;
		this.site = site;
		this.landmarkOnlyMode = landmarkOnlyMode;
	}

	public ContextContentProvider(Shell shell, boolean landmarkOnlyMode) {
		this.shell = shell;
		this.landmarkOnlyMode = landmarkOnlyMode;
	}

	public ContextContentProvider(Tree tree, Shell shell, boolean landmarkOnlyMode) {
		this.shell = shell;
		this.tree = tree;
		this.landmarkOnlyMode = landmarkOnlyMode;
	}

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		// don't care when the input changes
	}

	public void dispose() {
		// don't care when we are disposed
	}

	public Object[] getElements(Object parent) {
		if (matchesParent(parent)) {
			List<IInteractionElement> nodes;
			if (landmarkOnlyMode) {
				Set<IInteractionElement> landmarks = ContextCore.getContextManager().getActiveLandmarks();
				nodes = new ArrayList<IInteractionElement>();
				for (IInteractionElement node : landmarks) {
					if (!node.getContentType().equals(ContextCore.CONTENT_TYPE_RESOURCE)
							&& !node.getInterest().isPredicted()) {
						// && node.getRelations().size() > 0) {
						nodes.add(node);
					}
				}
			} else {
				nodes = ContextCore.getContextManager().getActiveContext().getAllElements();
			}
			List<Object> resolvedNodes = new ArrayList<Object>();
			for (IInteractionElement node : nodes) {
				AbstractContextStructureBridge bridge = ContextCore.getStructureBridge(node.getContentType());
				Object object = bridge.getObjectForHandle(node.getHandleIdentifier());
				if (object != null) {
					resolvedNodes.add(object);
				}
			}
			return resolvedNodes.toArray();
		}
		return getChildren(parent);
	}

	private boolean matchesParent(Object parent) {
		if (site != null && parent.equals(site)) {
			return true;
		} else if (shell != null && parent.equals(shell)) {
			return true;
		} else {
			return false;
		}
	}

	public Object getParent(Object child) {
		return null;
	}

	public Object[] getChildren(Object parent) {
		if (parent == null) {
			return new Object[0];
		}
		if (parent instanceof InteractionContextRelation) {
			IInteractionRelation edge = (IInteractionRelation) parent;
			IInteractionElement source = ContextCore.getContextManager().getElement(
					((IInteractionRelation) parent).getSource().getHandleIdentifier());

			return getAllTagetsForSource(source, edge.getRelationshipHandle());
		} else {
			IInteractionElement node;
			if (parent instanceof IInteractionElement) {
				node = (IInteractionElement) parent;
			} else {
				AbstractContextStructureBridge bridge = ContextCore.getStructureBridge(parent);
				node = ContextCore.getContextManager().getElement(bridge.getHandleIdentifier(parent));
			}
			if (node != null) {
				return getAllEdgeTypes(node.getRelations());
			} else {
				return new Object[0];
			}
		}
	}

	private boolean isRootItem(Object object) {
		boolean isRootItem = false;
		TreeItem[] items = tree.getItems();
		for (TreeItem item : items) {
			if (object.equals(item.getData())) {
				isRootItem = true;
			}
		}
		return isRootItem;
	}

	private Object[] getAllTagetsForSource(IInteractionElement source, String kind) {
		Collection<InteractionContextRelation> edges = source.getRelations();
		List<Object> targets = new ArrayList<Object>();
		for (InteractionContextRelation edge : edges) {
			if (edge.getRelationshipHandle().equals(kind)) {
				IInteractionElement target = edge.getTarget();
				AbstractContextStructureBridge bridge = ContextCore.getStructureBridge(target.getContentType());
				Object object = bridge.getObjectForHandle(target.getHandleIdentifier());
				if (object != null) {
					targets.add(object);
				}
			}
		}

		return targets.toArray();
	}

	private Object[] getAllEdgeTypes(Collection<InteractionContextRelation> edges) {
		Map<String, IInteractionRelation> map = new HashMap<String, IInteractionRelation>();
		for (IInteractionRelation edge : edges) {
			IInteractionRelation edgeType = map.get(edge.getRelationshipHandle());
			if (edgeType == null) {
				edgeType = edge;
				map.put(edge.getRelationshipHandle(), edgeType);
			}
		}
		Object[] edgeTypes = new Object[map.size()];
		int index = 0;
		for (IInteractionRelation value : map.values()) {
			edgeTypes[index] = value;
			index++;
		}
		return edgeTypes;
	}

	public boolean hasChildren(Object parent) {
		if (parent instanceof IInteractionRelation) {
			return true;
		} else {
			return isRootItem(parent);
		}
	}
}
