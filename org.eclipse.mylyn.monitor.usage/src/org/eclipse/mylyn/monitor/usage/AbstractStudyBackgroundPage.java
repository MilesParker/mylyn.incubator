/*******************************************************************************
 * Copyright (c) 2004 - 2006 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.monitor.usage;

import java.io.File;

import org.eclipse.jface.wizard.IWizardPage;

/**
 * Extend to provide a custom background page.
 * 
 * @author Leah Findlater
 * @author Mik Kersten
 * @since	2.0
 */
public abstract class AbstractStudyBackgroundPage implements IWizardPage {

	public abstract File createFeedbackFile();
}
