/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Maarten Meijer - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.sandbox.dev.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.tasks.core.IAttributeContainer;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskContainer;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * Abstract class to display various properties in the Eclipse Properties View.<br />
 * See <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=210639">Bug 210639</a> and <a
 * href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=208275">Bug 208275</a><br />
 * 
 * @author Maarten Meijer
 */
public abstract class AbstractTaskContainerPropertySource implements IPropertySource {

	protected static final String CHILDREN = "children";

	protected static final String DESCENDANDS = "descendents";

	protected static final String IS_CYCLIC = "iscyclic";

	protected static final String SUMMARY = "summary";

	protected static final String HANDLE = "handle";

	private boolean cyclic;

	protected AbstractTaskContainer container;

	protected String description;

	protected IAttributeContainer attributeContainer = null;

	public AbstractTaskContainerPropertySource(AbstractTaskContainer adaptableObject) {
		container = adaptableObject;
		description = container.getClass().getName();
		if (adaptableObject instanceof IAttributeContainer) {
			attributeContainer = (IAttributeContainer) adaptableObject;
		}
	}

	/**
	 * @return an expanded set of all descendants, excluding itself.
	 */
	public Set<ITask> getDescendants(ITaskContainer parent) {
		Set<ITask> childrenWithoutCycles = new HashSet<ITask>();
		this.getDescendantsHelper(parent, childrenWithoutCycles, parent);
		return Collections.unmodifiableSet(childrenWithoutCycles);
	}

	protected void getDescendantsHelper(ITaskContainer parent, Set<ITask> visited, ITaskContainer root) {
		for (ITask child : parent.getChildren()) {
			if (child == root) {
				cyclic = true;
			}
			if (child instanceof ITaskContainer) {
				if (!visited.contains(child) && child != root) {
					visited.add(child);
					getDescendantsHelper((ITaskContainer) child, visited, root);
				}
			}
		}
	}

	/**
	 * @return true if the parent also occurs in its descendants.
	 */
	public boolean containsCyclic(ITaskContainer parent) {
		Set<AbstractTaskContainer> childrenWithoutCycles = new HashSet<AbstractTaskContainer>();
		Set<ITaskContainer> parentStack = new HashSet<ITaskContainer>();
		cyclic = false;
		this.containsCyclicHelper(parent, childrenWithoutCycles, parentStack);
		return cyclic;
	}

	protected void containsCyclicHelper(ITaskContainer parent, Set<AbstractTaskContainer> visited,
			Set<ITaskContainer> parentStack) {
		// fast exit
		if (cyclic) {
			return;
		}

		parentStack.add(parent);
		for (ITask child : parent.getChildren()) {
			if (child instanceof AbstractTaskContainer) {
				if (parentStack.contains(child)) {
					cyclic = true;
					return;
				} else {
					containsCyclicHelper((ITaskContainer) child, visited, parentStack);
				}
			}
		}
		parentStack.remove(parent);
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		TextPropertyDescriptor handle = new TextPropertyDescriptor(HANDLE, "Handle Identifier");
		handle.setCategory(description);
		TextPropertyDescriptor children = new TextPropertyDescriptor(CHILDREN, "Total Children (internal)");
		children.setCategory(description);
		TextPropertyDescriptor descendants = new TextPropertyDescriptor(DESCENDANDS, "Total Descendants");
		descendants.setCategory(description);
		TextPropertyDescriptor cyclic = new TextPropertyDescriptor(IS_CYCLIC, "Cycle in descendants graph?");
		cyclic.setCategory(description);
		IPropertyDescriptor[] these = new IPropertyDescriptor[] { handle, children, descendants, cyclic };
		return appendSpecifics(getAttributesAsProperties(), these);
	}

	public Object getPropertyValue(Object id) {
		if (HANDLE.equals(id)) {
			return container.getHandleIdentifier();
		} else if (CHILDREN.equals(id)) {
			return container.getChildren().size();
		} else if (DESCENDANDS.equals(id)) {
			return getDescendants(container).size();
		} else if (IS_CYCLIC.equals(id)) {
			return containsCyclic(container) ? "Cyclic" : "Not Cyclic";
		} else if (null != attributeContainer) {
			if (attributeContainer.getAttributes().containsKey(id)) {
				return attributeContainer.getAttribute((String) id);
			}
		}
		return null;
	}

	public boolean isPropertySet(Object id) {
		// ignore
		return false;
	}

	public void resetPropertyValue(Object id) {
		// ignore
	}

	public void setPropertyValue(Object id, Object value) {
		// ignore
	}

	public Object getEditableValue() {
		// ignore
		return null;
	}

	public IPropertyDescriptor[] appendSpecifics(IPropertyDescriptor[] specific, IPropertyDescriptor[] these) {
		IPropertyDescriptor[] all = new IPropertyDescriptor[specific.length + these.length];
		System.arraycopy(these, 0, all, 0, these.length);
		System.arraycopy(specific, 0, all, these.length, specific.length);
		return all;
	}

	public IPropertyDescriptor[] getAttributesAsProperties() {
		if (null == attributeContainer) {
			return new IPropertyDescriptor[0];
		}
		String categoryname = IAttributeContainer.class.getCanonicalName();

		List<IPropertyDescriptor> props = new ArrayList<IPropertyDescriptor>();

		Map<String, String> attributes = attributeContainer.getAttributes();
		for (String key : attributes.keySet()) {
			TextPropertyDescriptor desc = new TextPropertyDescriptor(key, key);
			desc.setCategory(categoryname);
			props.add(desc);
		}
		return props.toArray(new IPropertyDescriptor[0]);
	}
}