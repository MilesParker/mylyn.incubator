/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Jeff Pound - Attachments
 *******************************************************************************/

package org.eclipse.mylyn.bugzilla.deprecated;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaAttribute;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCorePlugin;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCustomField;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaOperation;
import org.eclipse.mylyn.internal.bugzilla.core.IBugzillaConstants;
import org.eclipse.mylyn.internal.bugzilla.core.RepositoryConfiguration;
import org.eclipse.mylyn.internal.bugzilla.ui.BugzillaUiPlugin;
import org.eclipse.mylyn.internal.bugzilla.ui.editor.KeywordsDialog;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.provisional.commons.ui.DatePicker;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.deprecated.RepositoryOperation;
import org.eclipse.mylyn.internal.tasks.core.deprecated.RepositoryTaskAttribute;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.deprecated.AbstractRepositoryTaskEditor;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.Section;

/**
 * An editor used to view a bug report that exists on a server. It uses a <code>BugReport</code> object to store the
 * data.
 * 
 * @author Mik Kersten (hardening of prototype)
 * @author Rob Elves
 * @author Jeff Pound (Attachment work)
 */
public class BugzillaTaskEditor extends AbstractRepositoryTaskEditor {

	private static final String LABEL_TIME_TRACKING = "Bugzilla Time Tracking";

	protected Text keywordsText;

	protected Text estimateText;

	protected Text actualText;

	protected Text remainingText;

	protected Text addTimeText;

	protected Text deadlineText;

	protected DatePicker deadlinePicker;

	protected Text votesText;

	protected Text assignedTo;

	/**
	 * Creates a new <code>ExistingBugEditor</code>.
	 */
	public BugzillaTaskEditor(FormEditor editor) {
		super(editor);
		// Set up the input for comparing the bug report to the server
		// CompareConfiguration config = new CompareConfiguration();
		// config.setLeftEditable(false);
		// config.setRightEditable(false);
		// config.setLeftLabel("Local Bug Report");
		// config.setRightLabel("Remote Bug Report");
		// config.setLeftImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT));
		// config.setRightImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT));
		// compareInput = new BugzillaCompareInput(config);
	}

	@Override
	protected boolean supportsCommentSort() {
		return false;
	}

	@Override
	protected void createCustomAttributeLayout(Composite composite) {
		RepositoryTaskAttribute attribute = null;
		try {
			RepositoryConfiguration configuration = BugzillaCorePlugin.getRepositoryConfiguration(this.repository,
					false, new NullProgressMonitor());

			if (configuration != null) {
				List<BugzillaCustomField> customFields = configuration.getCustomFields();
				if (!customFields.isEmpty()) {
					for (BugzillaCustomField bugzillaCustomField : customFields) {
						List<String> optionList = bugzillaCustomField.getOptions();
						attribute = this.taskData.getAttribute(bugzillaCustomField.getName());
						if (attribute == null) {
							RepositoryTaskAttribute newattribute = new RepositoryTaskAttribute(
									bugzillaCustomField.getName(), bugzillaCustomField.getDescription(), false);
							newattribute.setReadOnly(false);
							this.taskData.addAttribute(bugzillaCustomField.getName(), newattribute);
						}
						final RepositoryTaskAttribute cfattribute = this.taskData.getAttribute(bugzillaCustomField.getName());
						Label label = createLabel(composite, cfattribute);
						GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);
						if (optionList != null && !optionList.isEmpty()) {
							GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
							data.horizontalSpan = 1;
							final CCombo attributeCombo = new CCombo(composite, SWT.FLAT | SWT.READ_ONLY);
							getManagedForm().getToolkit().adapt(attributeCombo, true, true);
							attributeCombo.setFont(TEXT_FONT);
							attributeCombo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
							if (hasChanged(cfattribute)) {
								attributeCombo.setBackground(getColorIncoming());
							}
							attributeCombo.setLayoutData(data);

							for (String val : optionList) {
								if (val != null) {
									attributeCombo.add(val);
								}
							}
							String value = cfattribute.getValue();
							if (value == null) {
								value = "";
							}
							if (attributeCombo.indexOf(value) != -1) {
								attributeCombo.select(attributeCombo.indexOf(value));
							}
							attributeCombo.clearSelection();
							attributeCombo.addSelectionListener(new SelectionAdapter() {
								@Override
								public void widgetSelected(SelectionEvent event) {
									if (attributeCombo.getSelectionIndex() > -1) {
										String sel = attributeCombo.getItem(attributeCombo.getSelectionIndex());
										cfattribute.setValue(sel);
										attributeChanged(cfattribute);
										attributeCombo.clearSelection();
									}
								}
							});
						} else {
							Text cfField = createTextField(composite, cfattribute, SWT.FLAT);
							GridDataFactory.fillDefaults().hint(135, SWT.DEFAULT).applyTo(cfField);
						}
					}

					getManagedForm().getToolkit().paintBordersFor(composite);
				}
			}
		} catch (CoreException e) {
			// ignore
		}

		attribute = this.taskData.getAttribute(BugzillaAttribute.DEPENDSON.getKey());
		if (attribute != null && !attribute.isReadOnly()) {
			Label label = createLabel(composite, attribute);
			GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);

			Composite textFieldComposite = getManagedForm().getToolkit().createComposite(composite);
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(textFieldComposite);
			GridLayoutFactory.swtDefaults().margins(1, 3).spacing(0, 3).applyTo(textFieldComposite);

			GridData textData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
			textData.horizontalSpan = 1;
			textData.widthHint = 135;

			final Text text = createTextField(textFieldComposite, attribute, SWT.FLAT);
			text.setLayoutData(textData);
			getManagedForm().getToolkit().paintBordersFor(textFieldComposite);
		}

		attribute = this.taskData.getAttribute(BugzillaAttribute.BLOCKED.getKey());
		if (attribute != null && !attribute.isReadOnly()) {
			Label label = createLabel(composite, attribute);
			GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);
			Composite textFieldComposite = getManagedForm().getToolkit().createComposite(composite);
			GridLayout textLayout = new GridLayout();
			textLayout.marginWidth = 1;
			textLayout.marginHeight = 3;
			textLayout.verticalSpacing = 3;
			textFieldComposite.setLayout(textLayout);
			GridData textData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			textData.horizontalSpan = 1;
			textData.widthHint = 135;
			final Text text = createTextField(textFieldComposite, attribute, SWT.FLAT);
			text.setLayoutData(textData);
			getManagedForm().getToolkit().paintBordersFor(textFieldComposite);
		}

		String dependson = taskData.getAttributeValue(BugzillaAttribute.DEPENDSON.getKey());
		String blocked = taskData.getAttributeValue(BugzillaAttribute.BLOCKED.getKey());
		boolean addHyperlinks = (dependson != null && dependson.length() > 0)
				|| (blocked != null && blocked.length() > 0);

		if (addHyperlinks) {
			getManagedForm().getToolkit().createLabel(composite, "");
			addBugHyperlinks(composite, BugzillaAttribute.DEPENDSON.getKey());
		}

		if (addHyperlinks) {
			getManagedForm().getToolkit().createLabel(composite, "");
			addBugHyperlinks(composite, BugzillaAttribute.BLOCKED.getKey());
		}

		// NOTE: urlText should not be back ported to 3.3 due to background color failure
		attribute = this.taskData.getAttribute(BugzillaAttribute.BUG_FILE_LOC.getKey());
		if (attribute != null && !attribute.isReadOnly()) {
			Label label = createLabel(composite, attribute);
			GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);
			TextViewer urlTextViewer = addTextEditor(repository, composite, attribute.getValue(), //
					false, SWT.FLAT);
			final StyledText urlText = urlTextViewer.getTextWidget();
			urlText.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
			urlText.setIndent(2);
			final RepositoryTaskAttribute urlAttribute = attribute;

			urlTextViewer.setEditable(true);
			urlTextViewer.addTextListener(new ITextListener() {
				public void textChanged(TextEvent event) {
					String newValue = urlText.getText();
					if (!newValue.equals(urlAttribute.getValue())) {
						urlAttribute.setValue(newValue);
						attributeChanged(urlAttribute);
					}
				}
			});
			GridData textData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			textData.horizontalSpan = 1;
			textData.widthHint = 135;
			urlText.setLayoutData(textData);
			if (hasChanged(attribute)) {
				urlText.setBackground(getColorIncoming());
			}
		}

		attribute = this.taskData.getAttribute(BugzillaAttribute.STATUS_WHITEBOARD.getKey());
		if (attribute == null) {
			this.taskData.setAttributeValue(BugzillaAttribute.STATUS_WHITEBOARD.getKey(), "");
			attribute = this.taskData.getAttribute(BugzillaAttribute.STATUS_WHITEBOARD.getKey());
		}
		if (attribute != null && !attribute.isReadOnly()) {
			Label label = createLabel(composite, attribute);
			GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);
			Text whiteboardField = createTextField(composite, attribute, SWT.FLAT);
			GridDataFactory.fillDefaults().hint(135, SWT.DEFAULT).applyTo(whiteboardField);
		}

		try {
			addKeywordsList(composite);
		} catch (IOException e) {
			MessageDialog.openInformation(null, "Attribute Display Error",
					"Could not retrieve keyword list, ensure proper configuration in "
							+ TasksUiPlugin.LABEL_VIEW_REPOSITORIES + "\n\nError reported: " + e.getMessage());
		}

		addVoting(composite);

		// If groups is available add roles
		if (taskData.getAttribute(BugzillaAttribute.GROUP.getKey()) != null) {
			addRoles(composite);
		}

		if (taskData.getAttribute(BugzillaAttribute.ESTIMATED_TIME.getKey()) != null) {
			addBugzillaTimeTracker(getManagedForm().getToolkit(), composite);
		}
	}

	private boolean hasCustomAttributeChanges() {
		if (taskData == null) {
			return false;
		}
		String customAttributeKeys[] = { BugzillaAttribute.BUG_FILE_LOC.getKey(), BugzillaAttribute.DEPENDSON.getKey(),
				BugzillaAttribute.BLOCKED.getKey(), BugzillaAttribute.KEYWORDS.getKey(),
				BugzillaAttribute.VOTES.getKey(), BugzillaAttribute.REPORTER_ACCESSIBLE.getKey(),
				BugzillaAttribute.CCLIST_ACCESSIBLE.getKey(), BugzillaAttribute.ESTIMATED_TIME.getKey(),
				BugzillaAttribute.REMAINING_TIME.getKey(), BugzillaAttribute.ACTUAL_TIME.getKey(),
				BugzillaAttribute.DEADLINE.getKey(), BugzillaAttribute.STATUS_WHITEBOARD.getKey() };
		for (String key : customAttributeKeys) {
			RepositoryTaskAttribute attribute = taskData.getAttribute(key);
			if (hasChanged(attribute)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean hasVisibleAttributeChanges() {
		return super.hasVisibleAttributeChanges() || this.hasCustomAttributeChanges();

	}

	private void addBugHyperlinks(Composite composite, String key) {
		Composite hyperlinksComposite = getManagedForm().getToolkit().createComposite(composite);
		RowLayout rowLayout = new RowLayout();
		rowLayout.marginBottom = 0;
		rowLayout.marginLeft = 0;
		rowLayout.marginRight = 0;
		rowLayout.marginTop = 0;
		rowLayout.spacing = 0;
		hyperlinksComposite.setLayout(new RowLayout());
		String values = taskData.getAttributeValue(key);

		if (values != null && values.length() > 0) {
			for (String bugNumber : values.split(",")) {
				final String bugId = bugNumber.trim();
				final String bugUrl = repository.getRepositoryUrl() + IBugzillaConstants.URL_GET_SHOW_BUG + bugId;
				final AbstractTask task = (AbstractTask) TasksUiInternal.getTaskList().getTask(
						repository.getRepositoryUrl(), bugId);
				createTaskListHyperlink(hyperlinksComposite, bugId, bugUrl, task);
			}
		}
	}

	protected void addRoles(Composite parent) {
		Section rolesSection = getManagedForm().getToolkit().createSection(parent, ExpandableComposite.SHORT_TITLE_BAR);
		rolesSection.setText("Users in the roles selected below can always view this bug");
		rolesSection.setDescription("(The assignee can always see a bug, and this section does not take effect unless the bug is restricted to at least one group.)");
		GridLayout gl = new GridLayout();
		GridData gd = new GridData(SWT.FILL, SWT.NONE, false, false);
		gd.horizontalSpan = 4;
		rolesSection.setLayout(gl);
		rolesSection.setLayoutData(gd);

		Composite rolesComposite = getManagedForm().getToolkit().createComposite(rolesSection);
		GridLayout attributesLayout = new GridLayout();
		attributesLayout.numColumns = 4;
		attributesLayout.horizontalSpacing = 5;
		attributesLayout.verticalSpacing = 4;
		rolesComposite.setLayout(attributesLayout);
		GridData attributesData = new GridData(GridData.FILL_BOTH);
		attributesData.horizontalSpan = 1;
		attributesData.grabExcessVerticalSpace = false;
		rolesComposite.setLayoutData(attributesData);
		rolesSection.setClient(rolesComposite);

		RepositoryTaskAttribute attribute = taskData.getAttribute(BugzillaAttribute.REPORTER_ACCESSIBLE.getKey());
		if (attribute == null) {
			taskData.setAttributeValue(BugzillaAttribute.REPORTER_ACCESSIBLE.getKey(), "0");
			attribute = taskData.getAttribute(BugzillaAttribute.REPORTER_ACCESSIBLE.getKey());
		}
		Button button = addButtonField(rolesComposite, attribute, SWT.CHECK);
		if (hasChanged(attribute)) {
			button.setBackground(getColorIncoming());
		}

		attribute = null;
		attribute = taskData.getAttribute(BugzillaAttribute.CCLIST_ACCESSIBLE.getKey());
		if (attribute == null) {
			taskData.setAttributeValue(BugzillaAttribute.CCLIST_ACCESSIBLE.getKey(), "0");
			attribute = taskData.getAttribute(BugzillaAttribute.CCLIST_ACCESSIBLE.getKey());
		}
		button = addButtonField(rolesComposite, attribute, SWT.CHECK);
		if (hasChanged(attribute)) {
			button.setBackground(getColorIncoming());
		}
	}

	@Override
	protected boolean hasContentAssist(RepositoryTaskAttribute attribute) {
		return BugzillaAttribute.NEWCC.getKey().equals(attribute.getId());
	}

	@Override
	protected boolean hasContentAssist(RepositoryOperation repositoryOperation) {
		BugzillaOperation operation;
		try {
			operation = BugzillaOperation.valueOf(repositoryOperation.getKnobName());
		} catch (RuntimeException e) {
			// FIXME: ?
			StatusHandler.log(new Status(IStatus.INFO, BugzillaUiPlugin.ID_PLUGIN, "Unrecognized operation: "
					+ repositoryOperation.getKnobName(), e));
			operation = null;
		}

		if (operation != null && operation == BugzillaOperation.reassign) {
			return true;
		} else {
			return false;
		}
	}

	private Button addButtonField(Composite rolesComposite, RepositoryTaskAttribute attribute, int style) {
		if (attribute == null) {
			return null;
		}
		String name = attribute.getName();
		if (hasOutgoingChange(attribute)) {
			name += "*";
		}

		final Button button = getManagedForm().getToolkit().createButton(rolesComposite, name, style);
		if (!attribute.isReadOnly()) {
			button.setData(attribute);
			button.setSelection(attribute.getValue().equals("1"));
			button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
			button.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					String sel = "1";
					if (!button.getSelection()) {
						sel = "0";
					}
					RepositoryTaskAttribute a = (RepositoryTaskAttribute) button.getData();
					a.setValue(sel);
					attributeChanged(a);
				}
			});
		}
		return button;
	}

	protected void addBugzillaTimeTracker(FormToolkit toolkit, Composite parent) {

		Section timeSection = toolkit.createSection(parent, ExpandableComposite.SHORT_TITLE_BAR);
		timeSection.setText(LABEL_TIME_TRACKING);
		GridLayout gl = new GridLayout();
		GridData gd = new GridData(SWT.FILL, SWT.NONE, false, false);
		gd.horizontalSpan = 4;
		timeSection.setLayout(gl);
		timeSection.setLayoutData(gd);

		Composite timeComposite = toolkit.createComposite(timeSection);
		gl = new GridLayout(4, false);
		timeComposite.setLayout(gl);
		gd = new GridData();
		gd.horizontalSpan = 5;
		timeComposite.setLayoutData(gd);

		RepositoryTaskAttribute attribute = this.taskData.getAttribute(BugzillaAttribute.ESTIMATED_TIME.getKey());
		if (attribute != null && !attribute.isReadOnly()) {
			createLabel(timeComposite, attribute);
			estimateText = createTextField(timeComposite, attribute, SWT.FLAT);
			estimateText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		}

		Label label = toolkit.createLabel(timeComposite, "Current Estimate:");
		label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		float total = 0;
		try {
			total = (Float.parseFloat(taskData.getAttributeValue(BugzillaAttribute.ACTUAL_TIME.getKey())) + Float.parseFloat(taskData.getAttributeValue(BugzillaAttribute.REMAINING_TIME.getKey())));
		} catch (Exception e) {
			// ignore likely NumberFormatException
		}

		Text currentEstimate = toolkit.createText(timeComposite, "" + total);
		currentEstimate.setFont(TEXT_FONT);
		currentEstimate.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		currentEstimate.setEditable(false);

		attribute = this.taskData.getAttribute(BugzillaAttribute.ACTUAL_TIME.getKey());
		if (attribute != null) {

			createLabel(timeComposite, attribute);
			Text actualText = createTextField(timeComposite, attribute, SWT.FLAT);
			actualText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
			actualText.setEditable(false);
		}

		// Add Time
		taskData.setAttributeValue(BugzillaAttribute.WORK_TIME.getKey(), "0");
		final RepositoryTaskAttribute addTimeAttribute = this.taskData.getAttribute(BugzillaAttribute.WORK_TIME.getKey());
		if (addTimeAttribute != null) {

			createLabel(timeComposite, addTimeAttribute);
			addTimeText = toolkit.createText(timeComposite,
					taskData.getAttributeValue(BugzillaAttribute.WORK_TIME.getKey()), SWT.BORDER);
			addTimeText.setFont(TEXT_FONT);
			addTimeText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
			addTimeText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					addTimeAttribute.setValue(addTimeText.getText());
					attributeChanged(addTimeAttribute);
				}
			});
		}
		attribute = this.taskData.getAttribute(BugzillaAttribute.REMAINING_TIME.getKey());
		if (attribute != null) {
			createLabel(timeComposite, attribute);
			createTextField(timeComposite, attribute, SWT.FLAT);
		}

		attribute = this.taskData.getAttribute(BugzillaAttribute.DEADLINE.getKey());
		if (attribute != null) {
			createLabel(timeComposite, attribute);

			Composite dateWithClear = toolkit.createComposite(timeComposite);
			GridLayout layout = new GridLayout(2, false);
			layout.marginWidth = 1;
			dateWithClear.setLayout(layout);

			deadlinePicker = new DatePicker(dateWithClear, /* SWT.NONE */SWT.BORDER,
					taskData.getAttributeValue(BugzillaAttribute.DEADLINE.getKey()), false, 0);
			deadlinePicker.setFont(TEXT_FONT);
			deadlinePicker.setDatePattern("yyyy-MM-dd");
			if (hasChanged(attribute)) {
				deadlinePicker.setBackground(getColorIncoming());
			}
			deadlinePicker.addPickerSelectionListener(new SelectionListener() {

				public void widgetDefaultSelected(SelectionEvent e) {
					// ignore
				}

				public void widgetSelected(SelectionEvent e) {
					Calendar cal = deadlinePicker.getDate();
					if (cal != null) {
						Date d = cal.getTime();
						SimpleDateFormat f = (SimpleDateFormat) DateFormat.getDateInstance();
						f.applyPattern("yyyy-MM-dd");

						taskData.setAttributeValue(BugzillaAttribute.DEADLINE.getKey(), f.format(d));
						attributeChanged(taskData.getAttribute(BugzillaAttribute.DEADLINE.getKey()));
						// TODO goes dirty even if user presses cancel
						// markDirty(true);
					} else {
						taskData.setAttributeValue(BugzillaAttribute.DEADLINE.getKey(), "");
						attributeChanged(taskData.getAttribute(BugzillaAttribute.DEADLINE.getKey()));
						deadlinePicker.setDate(null);
					}
				}
			});

			ImageHyperlink clearDeadlineDate = toolkit.createImageHyperlink(dateWithClear, SWT.NONE);
			clearDeadlineDate.setImage(CommonImages.getImage(CommonImages.REMOVE));
			clearDeadlineDate.setToolTipText("Clear");
			clearDeadlineDate.addHyperlinkListener(new HyperlinkAdapter() {

				@Override
				public void linkActivated(HyperlinkEvent e) {
					taskData.setAttributeValue(BugzillaAttribute.DEADLINE.getKey(), "");
					attributeChanged(taskData.getAttribute(BugzillaAttribute.DEADLINE.getKey()));
					deadlinePicker.setDate(null);
				}
			});

		}

		timeSection.setClient(timeComposite);
	}

	protected void addKeywordsList(Composite attributesComposite) throws IOException {
		// newLayout(attributesComposite, 1, "Keywords:", PROPERTY);
		final RepositoryTaskAttribute attribute = taskData.getAttribute(RepositoryTaskAttribute.KEYWORDS);
		if (attribute == null) {
			return;
		}
		Label label = createLabel(attributesComposite, attribute);
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);

		// toolkit.createText(attributesComposite, keywords)
		keywordsText = createTextField(attributesComposite, attribute, SWT.FLAT);
		keywordsText.setFont(TEXT_FONT);
		GridData keywordsData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		keywordsData.horizontalSpan = 2;
		keywordsData.widthHint = 200;
		keywordsText.setLayoutData(keywordsData);

		Button changeKeywordsButton = getManagedForm().getToolkit().createButton(attributesComposite, "Edit...",
				SWT.FLAT);
		GridData keyWordsButtonData = new GridData();
		changeKeywordsButton.setLayoutData(keyWordsButtonData);
		changeKeywordsButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {

				String keywords = attribute.getValue();

				Shell shell = null;
				if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
					shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				} else {
					shell = new Shell(PlatformUI.getWorkbench().getDisplay());
				}

				List<String> validKeywords = new ArrayList<String>();
				try {
					validKeywords = BugzillaCorePlugin.getRepositoryConfiguration(repository, false,
							new NullProgressMonitor()).getKeywords();
				} catch (Exception ex) {
					// ignore
				}

				KeywordsDialog keywordsDialog = new KeywordsDialog(shell, keywords, validKeywords);
				int responseCode = keywordsDialog.open();

				String newKeywords = keywordsDialog.getSelectedKeywordsString();
				if (responseCode == Window.OK && keywords != null) {
					keywordsText.setText(newKeywords);
					attribute.setValue(newKeywords);
					attributeChanged(attribute);
				} else {
					return;
				}

			}

		});
	}

	protected void addVoting(Composite attributesComposite) {
		Label label = getManagedForm().getToolkit().createLabel(attributesComposite, "Votes:");
		label.setForeground(getManagedForm().getToolkit().getColors().getColor(IFormColors.TITLE));
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);
		Composite votingComposite = getManagedForm().getToolkit().createComposite(attributesComposite);
		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		votingComposite.setLayout(layout);

		RepositoryTaskAttribute votesAttribute = taskData.getAttribute(BugzillaAttribute.VOTES.getKey());

		votesText = createTextField(votingComposite, votesAttribute, SWT.FLAT | SWT.READ_ONLY);
		votesText.setFont(TEXT_FONT);
		GridDataFactory.fillDefaults().minSize(30, SWT.DEFAULT).hint(30, SWT.DEFAULT).applyTo(votesText);

		if (votesAttribute != null && hasChanged(votesAttribute)) {
			votesText.setBackground(getColorIncoming());
		}
		votesText.setEditable(false);

		Hyperlink showVotesHyperlink = getManagedForm().getToolkit().createHyperlink(votingComposite, "Show votes",
				SWT.NONE);
		showVotesHyperlink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				if (BugzillaTaskEditor.this.getEditor() instanceof TaskEditor) {
					TasksUiUtil.openUrl(repository.getRepositoryUrl() + IBugzillaConstants.URL_SHOW_VOTES
							+ taskData.getTaskId());
				}
			}
		});

		Hyperlink voteHyperlink = getManagedForm().getToolkit().createHyperlink(votingComposite, "Vote", SWT.NONE);
		voteHyperlink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				if (BugzillaTaskEditor.this.getEditor() instanceof TaskEditor) {
					TasksUiUtil.openUrl(repository.getRepositoryUrl() + IBugzillaConstants.URL_VOTE
							+ taskData.getTaskId());
				}
			}
		});
	}

	@Override
	protected void validateInput() {

	}

	@Override
	protected String getHistoryUrl() {
		if (repository != null && taskData != null) {
			return repository.getRepositoryUrl() + IBugzillaConstants.URL_BUG_ACTIVITY + taskData.getTaskId();
		} else {
			return null;
		}
	}

	/**
	 * @author Frank Becker (bug 198027) FIXME: A lot of duplicated code here between this and NewBugzillataskEditor
	 */
	@Override
	protected void addAssignedTo(Composite peopleComposite) {
		RepositoryTaskAttribute assignedAttribute = taskData.getAttribute(RepositoryTaskAttribute.USER_ASSIGNED);
		if (assignedAttribute != null) {
			String bugzillaVersion;
			try {
				bugzillaVersion = BugzillaCorePlugin.getRepositoryConfiguration(repository, false,
						new NullProgressMonitor()).getInstallVersion();
			} catch (CoreException e1) {
				// ignore
				bugzillaVersion = "2.18";
			}
			if (bugzillaVersion.compareTo("3.1") < 0) {
				// old bugzilla workflow is used
				super.addAssignedTo(peopleComposite);
				return;
			}
			Label label = createLabel(peopleComposite, assignedAttribute);
			GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);
			assignedTo = createTextField(peopleComposite, assignedAttribute, SWT.FLAT);
			GridDataFactory.fillDefaults().hint(150, SWT.DEFAULT).applyTo(assignedTo);
			assignedTo.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					String sel = assignedTo.getText();
					RepositoryTaskAttribute a = taskData.getAttribute(RepositoryTaskAttribute.USER_ASSIGNED);
					if (!(a.getValue().equals(sel))) {
						a.setValue(sel);
						markDirty(true);
					}
				}
			});
			ContentAssistCommandAdapter adapter = applyContentAssist(assignedTo,
					createContentProposalProvider(assignedAttribute));
			ILabelProvider propsalLabelProvider = createProposalLabelProvider(assignedAttribute);
			if (propsalLabelProvider != null) {
				adapter.setLabelProvider(propsalLabelProvider);
			}
			adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);

			FormToolkit toolkit = getManagedForm().getToolkit();
			Label dummylabel = toolkit.createLabel(peopleComposite, "");
			GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(dummylabel);
			RepositoryTaskAttribute attribute = taskData.getAttribute(BugzillaAttribute.SET_DEFAULT_ASSIGNEE.getKey());
			if (attribute == null) {
				taskData.setAttributeValue(BugzillaAttribute.SET_DEFAULT_ASSIGNEE.getKey(), "0");
				attribute = taskData.getAttribute(BugzillaAttribute.SET_DEFAULT_ASSIGNEE.getKey());
			}
			addButtonField(peopleComposite, attribute, SWT.CHECK);
		}
	}

	@Override
	protected boolean attributeChanged(RepositoryTaskAttribute attribute) {
		if (attribute == null) {
			return false;
		}

		// Support comment wrapping for bugzilla 2.18
		if (attribute.getId().equals(BugzillaAttribute.NEW_COMMENT.getKey())) {
			if (repository.getVersion().startsWith("2.18")) {
				attribute.setValue(BugzillaUiPlugin.formatTextToLineWrap(attribute.getValue(), true));
			}
		}
		return super.attributeChanged(attribute);
	}

	@Override
	protected void addSelfToCC(Composite composite) {

		// XXX: Work around for adding QAContact to People section. Update once bug#179254 is complete
		boolean haveRealName = false;
		RepositoryTaskAttribute qaContact = taskData.getAttribute(BugzillaAttribute.QA_CONTACT_NAME.getKey());
		if (qaContact == null) {
			qaContact = taskData.getAttribute(BugzillaAttribute.QA_CONTACT.getKey());
		} else {
			haveRealName = true;
		}
		if (qaContact != null) {
			Label label = createLabel(composite, qaContact);
			GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);
			Text textField;
			if (qaContact.isReadOnly()) {
				textField = createTextField(composite, qaContact, SWT.FLAT | SWT.READ_ONLY);
			} else {
				textField = createTextField(composite, qaContact, SWT.FLAT);
				ContentAssistCommandAdapter adapter = applyContentAssist(textField,
						createContentProposalProvider(qaContact));
				ILabelProvider propsalLabelProvider = createProposalLabelProvider(qaContact);
				if (propsalLabelProvider != null) {
					adapter.setLabelProvider(propsalLabelProvider);
				}
				adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
			}
			GridDataFactory.fillDefaults().grab(true, false).applyTo(textField);
			if (haveRealName) {
				textField.setText(textField.getText() + " <"
						+ taskData.getAttributeValue(BugzillaAttribute.QA_CONTACT.getKey()) + ">");
			}
		}

		super.addSelfToCC(composite);

	}

}
