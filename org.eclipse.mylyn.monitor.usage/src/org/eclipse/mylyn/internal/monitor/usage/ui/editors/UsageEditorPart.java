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

package org.eclipse.mylar.internal.monitor.usage.ui.editors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylar.core.MylarStatusHandler;
import org.eclipse.mylar.monitor.core.collection.IUsageCollector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.EditorPart;

/**
 * @author Mik Kersten
 * @author Meghan Allen (re-factoring)
 */
public class UsageEditorPart extends EditorPart {

	protected UsageStatsEditorInput editorInput;

	protected FormToolkit toolkit;

	protected ScrolledForm sform;

	protected Composite editorComposite;

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@SuppressWarnings("deprecation")
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		editorInput = (UsageStatsEditorInput) input;
		setPartName(editorInput.getName());
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		sform = toolkit.createScrolledForm(parent);
		sform.getBody().setLayout(new TableWrapLayout());
		editorComposite = sform.getBody();

		createActionSection(editorComposite, toolkit);
		createSummaryStatsSection(editorComposite, toolkit);
	}

	@Override
	public void setFocus() {
	}

	protected void createActionSection(Composite parent, FormToolkit toolkit) {
		Section section = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR);
		section.setText("Actions");
		section.setLayout(new TableWrapLayout());
		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		Composite container = toolkit.createComposite(section);
		section.setClient(container);
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 2;
		container.setLayout(layout);

		Button exportHtml = toolkit.createButton(container, "Export as HTML", SWT.PUSH | SWT.CENTER);
		exportHtml.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				exportToHtml();
			}
		});

		Button export = toolkit.createButton(container, "Export as CSV Files", SWT.PUSH | SWT.CENTER);
		export.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				exportToCSV();
			}
		});
	}

	protected void createSummaryStatsSection(Composite parent, FormToolkit toolkit) {
		for (IUsageCollector collector : editorInput.getReportGenerator().getLastParsedSummary().getCollectors()) {
			List<String> summary = collector.getReport();
			if (!summary.isEmpty()) {
				Section summarySection = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR);
				summarySection.setText(collector.getReportTitle());
				summarySection.setLayout(new TableWrapLayout());
				summarySection.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

				Composite summaryContainer = toolkit.createComposite(summarySection);

				summarySection.setClient(summaryContainer);
				TableWrapLayout layout = new TableWrapLayout();

				// layout.numColumns = 2;
				summaryContainer.setLayout(layout);
				summaryContainer.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

				Composite browserComposite = new Composite(summaryContainer, SWT.NULL);

				browserComposite.setLayout(new GridLayout());
				Browser browser = new Browser(browserComposite, SWT.NONE);

				GridData browserLayout = new GridData(GridData.FILL_HORIZONTAL);
				browserLayout.heightHint = 300;
				browserLayout.widthHint = 800;
				browser.setLayoutData(browserLayout);
				String htmlText = "<html><head><LINK REL=STYLESHEET HREF=\"http://eclipse.org/default_style.css\" TYPE=\"text/css\"></head><body>\n";
				for (String description : summary)
					htmlText += description;
				htmlText += "</body></html>";
				browser.setText(htmlText);
				// if (description.equals(ReportGenerator.SUMMARY_SEPARATOR)) {
				// toolkit.createLabel(summaryContainer,
				// "---------------------------------");
				// toolkit.createLabel(summaryContainer,
				// "---------------------------------");
				// } else {
				// Label label = toolkit.createLabel(summaryContainer,
				// description);
				// if (!description.startsWith("<h"));
				// label.setForeground(toolkit.getColors().getColor(FormColors.TITLE));
				// label.setLayoutData(new
				// TableWrapData(TableWrapData.FILL_GRAB));
				// }
				// }
			}
		}
	}

	protected void exportToCSV() {

		// Ask the user to pick a directory into which to place multiple CSV
		// files
		try {
			DirectoryDialog dialog = new DirectoryDialog(getSite().getWorkbenchWindow().getShell());
			dialog.setText("Specify a directory for the CSV files");
			String directoryName = dialog.open();

			File outputFile;
			FileOutputStream outputStream;

			String filename = directoryName + File.separator + "Usage.csv";
			outputFile = new File(filename);

			outputStream = new FileOutputStream(outputFile, false);

			// Delegate to all collectors
			for (IUsageCollector collector : editorInput.getReportGenerator().getCollectors()) {
				collector.exportAsCSVFile(directoryName);
			}

			outputStream.flush();
			outputStream.close();

		} catch (SWTException swe) {
			MylarStatusHandler.log(swe, "unable to get directory name");
		} catch (FileNotFoundException e) {
			MylarStatusHandler.log(e, "could not resolve file");
		} catch (IOException e) {
			MylarStatusHandler.log(e, "could not write to file");
		}
	}

	protected void exportToHtml() {
		File outputFile;
		try {
			FileDialog dialog = new FileDialog(getSite().getWorkbenchWindow().getShell());
			dialog.setText("Specify a file name");
			dialog.setFilterExtensions(new String[] { "*.html", "*.*" });

			String filename = dialog.open();
			if (!filename.endsWith(".html"))
				filename += ".html";
			outputFile = new File(filename);
			// outputStream = new FileOutputStream(outputFile, true);
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
			writer.write("<html><head>"
			// + "<link rel=\"stylesheet\"
					// href=\"http://eclipse.org/mylar/style.css\"
					// type=\"text/css\"></head><body>"
					);
			for (IUsageCollector collector : editorInput.getReportGenerator().getCollectors()) {
				writer.write("<h3>" + collector.getReportTitle() + "</h3>");
				for (String reportLine : collector.getReport()) {
					writer.write(reportLine);
				}
				writer.write("<br><hr>");
			}
			writer.write("</body></html>");
			writer.close();
		} catch (FileNotFoundException e) {
			MylarStatusHandler.log(e, "could not resolve file");
		} catch (IOException e) {
			MylarStatusHandler.log(e, "could not write to file");
		}
	}

}
