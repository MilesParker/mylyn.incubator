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

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;

public class DesktopSearchLabelProvider extends DecoratingStyledCellLabelProvider implements IPropertyChangeListener {

	public DesktopSearchLabelProvider(IStyledLabelProvider labelProvider) {
		super(labelProvider, PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator(), null);
	}

	@Override
	public void initialize(ColumnViewer viewer, ViewerColumn column) {
		registerListeners();
		setOwnerDrawEnabled(coloredLabels());
		super.initialize(viewer, column);
	}

	private void registerListeners() {
		PlatformUI.getPreferenceStore().addPropertyChangeListener(this);
		JFaceResources.getColorRegistry().addListener(this);
	}

	private void deregisterListeners() {
		JFaceResources.getColorRegistry().removeListener(this);
		PlatformUI.getPreferenceStore().removePropertyChangeListener(this);
	}

	@Override
	public void dispose() {
		deregisterListeners();
		super.dispose();
	}

	private boolean coloredLabels() {
		return PlatformUI.getPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.USE_COLORED_LABELS);
	}

	public void propertyChange(PropertyChangeEvent event) {
		final String property = event.getProperty();
		if (property.equals(JFacePreferences.QUALIFIER_COLOR) || property.equals(JFacePreferences.DECORATIONS_COLOR)
				|| property.equals(IWorkbenchPreferenceConstants.USE_COLORED_LABELS)) {
			if (getViewer() == null || getViewer().getControl().isDisposed()) {
				return;
			}
			Display display = getViewer().getControl().getDisplay();
			display.asyncExec(new Runnable() {
				public void run() {
					refresh();
				}
			});
		}
	}

	protected void refresh() {
		ColumnViewer viewer = getViewer();
		if (viewer != null && !viewer.getControl().isDisposed()) {
			setOwnerDrawEnabled(coloredLabels());
			viewer.refresh();
		}
	}
}
