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

package org.eclipse.mylyn.tests.performance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * @author Mik Kersten
 */
public class UiTestUtil {

	public static int countItemsInTree(Tree tree) {
		List<TreeItem> collectedItems = new ArrayList<TreeItem>();
		collectTreeItemsInView(tree.getItems(), collectedItems);
		return collectedItems.size();
	}

	public static void collectTreeItemsInView(TreeItem[] items, List<TreeItem> collectedItems) {
		if (items.length > 0) {
			for (TreeItem childItem : Arrays.asList(items)) {
				collectedItems.add(childItem);
				collectTreeItemsInView(childItem.getItems(), collectedItems);
			}
		}
	}

	public static List<Object> getAllData(Tree tree) {
		List<TreeItem> items = new ArrayList<TreeItem>();
		collectTreeItemsInView(tree.getItems(), items);
		List<Object> dataList = new ArrayList<Object>();
		for (TreeItem item : items) {
			dataList.add(item.getData());
		}
		return dataList;
	}

	/**
	 * Ensures that the editor area is visible.
	 */
	public static void closeWelcomeView() {
		IViewReference[] views = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow()
				.getActivePage()
				.getViewReferences();
		for (IViewReference view : views) {
			if ("org.eclipse.ui.internal.introview".equals(view.getId())) {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(view);
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().resetPerspective();
				return;
			}
		}
	}

	public static IViewPart openView(String id) throws PartInitException {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(id);
	}

	public static void closeAllEditors() {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
	}

}
