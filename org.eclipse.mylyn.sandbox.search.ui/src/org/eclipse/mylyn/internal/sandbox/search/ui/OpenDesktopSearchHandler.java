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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author David Green
 */
public class OpenDesktopSearchHandler extends AbstractHandler implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		Display display = null;
		final IWorkbenchWindow workbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
		if (workbenchWindow != null) {
			display = workbenchWindow.getShell().getDisplay();
		}
		if (display == null) {
			display = Display.getDefault();
		}
		display.asyncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow window = workbenchWindow;
				if (window == null) {
					window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if (window == null) {
						window = PlatformUI.getWorkbench().getWorkbenchWindows()[0];
					}
				}
				NewSearchUI.openSearchDialog(window, DesktopSearchPage.PAGE_ID);
			}
		});
		return null;
	}

}
