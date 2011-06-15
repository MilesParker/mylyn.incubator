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
package org.eclipse.mylyn.internal.sandbox.search.ui;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * 
 * @author David Green
 */
public class SearchPlugin extends AbstractUIPlugin {

	private static final String ENABLED_16 = "elcl16/";

	private static final String ICONS_FULL = "icons/full/";

	public static final String IMAGE_SEARCH = ICONS_FULL+ENABLED_16+"tsearch_obj.gif";
	
	private static SearchPlugin instance;
	
	public SearchPlugin() {
		instance = this;
	}
	
	public static SearchPlugin getDefault() {
		return instance;
	}
	
	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		registerImage(reg,IMAGE_SEARCH);
	}
	
	private void registerImage(ImageRegistry reg, String image) {
		reg.put(image, ImageDescriptor.createFromURL(FileLocator.find(getBundle(),new Path(image),null)));
	}

	public IDialogSettings getDialogSettings(String sectionName) {
		IDialogSettings settings = super.getDialogSettings();
		IDialogSettings section = settings.getSection(sectionName);
		if (section == null) {
			section = settings.addNewSection(sectionName);
		}
		return section;
	}

}
