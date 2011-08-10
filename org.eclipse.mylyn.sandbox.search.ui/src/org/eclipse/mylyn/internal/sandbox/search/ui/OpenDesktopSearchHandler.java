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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * @author David Green
 */
public class OpenDesktopSearchHandler extends AbstractUiHandler implements IHandler {

	@Override
	protected Runnable computeUiRunnable(final ExecutionEvent event) {
		return new Runnable() {
			public void run() {
				IWorkbenchWindow window = computeWorkbenchWindow(event);
				if (window == null) {
					window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if (window == null) {
						window = PlatformUI.getWorkbench().getWorkbenchWindows()[0];
					}
				}
				NewSearchUI.openSearchDialog(window, DesktopSearchPage.PAGE_ID);
			}
		};
	}

}
