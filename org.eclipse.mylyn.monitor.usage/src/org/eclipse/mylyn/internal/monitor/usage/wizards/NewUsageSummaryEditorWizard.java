/*******************************************************************************
 * Copyright (c) 2004 - 2006 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylar.internal.monitor.usage.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylar.internal.monitor.core.collection.IUsageCollector;
import org.eclipse.mylar.internal.monitor.core.collection.ViewUsageCollector;
import org.eclipse.mylar.internal.monitor.usage.MonitorFileRolloverJob;
import org.eclipse.mylar.internal.monitor.usage.collectors.PerspectiveUsageCollector;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * @author Meghan Allen
 */
public class NewUsageSummaryEditorWizard extends Wizard implements INewWizard {

	private static final String TITLE = "New Usage Summary Report";

	private UsageSummaryEditorWizardPage usageSummaryPage;

	public NewUsageSummaryEditorWizard() {
		super();
		init();
		setWindowTitle(TITLE);
	}

	private void init() {
		usageSummaryPage = new UsageSummaryEditorWizardPage();
	}

	@Override
	public boolean performFinish() {

		if (!usageSummaryPage.includePerspective() && !usageSummaryPage.includeViews()) {
			return false;
		}

		List<IUsageCollector> collectors = new ArrayList<IUsageCollector>();

		if (usageSummaryPage.includePerspective()) {
			collectors.add(new PerspectiveUsageCollector());
		}
		if (usageSummaryPage.includeViews()) {
			ViewUsageCollector mylarViewUsageCollector = new ViewUsageCollector();
			collectors.add(mylarViewUsageCollector);
		}

		MonitorFileRolloverJob job = new MonitorFileRolloverJob(collectors);
		job.setPriority(MonitorFileRolloverJob.LONG);
		job.schedule();

		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// ignore

	}

	@Override
	public void addPages() {
		addPage(usageSummaryPage);
	}

}
