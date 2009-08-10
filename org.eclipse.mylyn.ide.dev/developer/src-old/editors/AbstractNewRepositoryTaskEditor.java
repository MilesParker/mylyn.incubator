/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.tasks.ui.deprecated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.DateRange;
import org.eclipse.mylyn.internal.tasks.core.ITaskList;
import org.eclipse.mylyn.internal.tasks.core.TaskCategory;
import org.eclipse.mylyn.internal.tasks.ui.ScheduleDatePicker;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTaskOutlineNode;
import org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTaskSelection;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @deprecated Do not use. This class is pending for removal: see bug 237552.
 */
@Deprecated
public abstract class AbstractNewRepositoryTaskEditor extends AbstractRepositoryTaskEditor {

	private static final int DESCRIPTION_WIDTH = 79 * 7; // 500;

	private static final int DESCRIPTION_HEIGHT = 10 * 14;

	private static final int DEFAULT_FIELD_WIDTH = 150;

	private static final int DEFAULT_ESTIMATED_TIME = 1;

	private static final String LABEL_SUMBIT = "Submit";

	private static final String ERROR_CREATING_BUG_REPORT = "Error creating bug report";

	protected ScheduleDatePicker scheduledForDate;

	protected Spinner estimatedTime;

	@Deprecated
	protected String newSummary = "";

	protected Button addToCategory;

	protected CCombo categoryChooser;

	/**
	 * @author Raphael Ackermann (bug 195514)
	 */
	protected class TabVerifyKeyListener implements VerifyKeyListener {

		public void verifyKey(VerifyEvent event) {
			// if there is a tab key, do not "execute" it and instead select the Attributes section
			if (event.keyCode == SWT.TAB) {
				event.doit = false;
				focusAttributes();
			}
		}
	}

	public AbstractNewRepositoryTaskEditor(FormEditor editor) {
		super(editor);
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) {
		if (!(input instanceof NewTaskEditorInput)) {
			return;
		}

		initTaskEditor(site, (RepositoryTaskEditorInput) input);

		setTaskOutlineModel(RepositoryTaskOutlineNode.parseBugReport(taskData, false));
		newSummary = taskData.getSummary();
	}

	@Override
	protected void createDescriptionLayout(Composite composite) {
		FormToolkit toolkit = this.getManagedForm().getToolkit();
		Section section = toolkit.createSection(composite, ExpandableComposite.TITLE_BAR);
		section.setText(getSectionLabel(SECTION_NAME.DESCRIPTION_SECTION));
		section.setExpanded(true);
		section.setLayout(new GridLayout());
		section.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite descriptionComposite = toolkit.createComposite(section);
		GridLayout descriptionLayout = new GridLayout();

		descriptionComposite.setLayout(descriptionLayout);
		GridData descriptionData = new GridData(GridData.FILL_BOTH);
		descriptionData.grabExcessVerticalSpace = true;
		descriptionComposite.setLayoutData(descriptionData);
		section.setClient(descriptionComposite);

		descriptionTextViewer = addTextEditor(repository, descriptionComposite, taskData.getDescription(), true,
				SWT.FLAT | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		descriptionTextViewer.setEditable(true);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = DESCRIPTION_WIDTH;
		gd.minimumHeight = DESCRIPTION_HEIGHT;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		descriptionTextViewer.getControl().setLayoutData(gd);
		descriptionTextViewer.getControl().setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);

		addDuplicateDetection(descriptionComposite);

		toolkit.paintBordersFor(descriptionComposite);
	}

	/**
	 * @author Raphael Ackermann (modifications) (bug 195514)
	 * @param composite
	 */
	@Override
	protected void createSummaryLayout(Composite composite) {
		addSummaryText(composite);
		if (summaryTextViewer != null) {
			summaryTextViewer.prependVerifyKeyListener(new TabVerifyKeyListener());
			// TODO: Eliminate this and newSummary field when api can be changed
			summaryTextViewer.getTextWidget().addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					String sel = summaryText.getText();
					if (!(newSummary.equals(sel))) {
						newSummary = sel;
					}
				}
			});
		}
	}

	@Override
	protected void createAttachmentLayout(Composite comp) {
		// currently can't attach while creating new bug
	}

	@Override
	protected void createCommentLayout(Composite comp) {
		// ignore
	}

	@Override
	protected void createNewCommentLayout(Composite comp) {
		createPlanningLayout(comp);
	}

	protected void createPlanningLayout(Composite comp) {
		Section section = createSection(comp, "Personal Planning");
		section.setLayout(new GridLayout());
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		section.setExpanded(true);

		Composite sectionClient = getManagedForm().getToolkit().createComposite(section);
		section.setClient(sectionClient);
		GridLayout layout = new GridLayout();
		layout.numColumns = 7;
		layout.makeColumnsEqualWidth = false;
		sectionClient.setLayout(layout);
		GridData clientDataLayout = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		sectionClient.setLayoutData(clientDataLayout);

		// Scheduled date
		getManagedForm().getToolkit().createLabel(sectionClient, "Scheduled for:");
		// label.setForeground(toolkit.getColors().getColor(FormColors.TITLE));
		scheduledForDate = new ScheduleDatePicker(sectionClient, null, SWT.FLAT);
		scheduledForDate.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		scheduledForDate.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
//		Calendar newTaskSchedule = TaskActivityUtil.getCalendar();
//		int scheduledEndHour = TasksUiPlugin.getDefault().getPreferenceStore().getInt(
//				TasksUiPreferenceConstants.PLANNING_ENDHOUR);
		// If past scheduledEndHour set for following day
//		if (newTaskSchedule.get(Calendar.HOUR_OF_DAY) >= scheduledEndHour) {
//			TaskActivityUtil.snapForwardNumDays(newTaskSchedule, 1);
//		} else {
//			TaskActivityUtil.snapEndOfWorkDay(newTaskSchedule);
//		}
//		scheduledForDate.setDate(newTaskSchedule);
//		Button removeReminder = getManagedForm().getToolkit().createButton(sectionClient, "Clear",
//				SWT.PUSH | SWT.CENTER);
//		removeReminder.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				scheduledForDate.setDate(null);
//			}
//		});

		ImageHyperlink clearReminder = getManagedForm().getToolkit().createImageHyperlink(sectionClient, SWT.NONE);
		clearReminder.setImage(CommonImages.getImage(CommonImages.REMOVE));
		clearReminder.setToolTipText("Clear");
		clearReminder.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				scheduledForDate.setScheduledDate(null);
			}
		});

		// 1 Blank column after Reminder clear button
		Label dummy = getManagedForm().getToolkit().createLabel(sectionClient, "");
		GridData dummyLabelDataLayout = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		dummyLabelDataLayout.horizontalSpan = 1;
		dummyLabelDataLayout.widthHint = 30;
		dummy.setLayoutData(dummyLabelDataLayout);

		// Estimated time
		getManagedForm().getToolkit().createLabel(sectionClient, "Estimated hours:");
		// label.setForeground(toolkit.getColors().getColor(FormColors.TITLE));
		// estimatedTime = new Spinner(sectionClient, SWT.FLAT);
		estimatedTime = new Spinner(sectionClient, SWT.FLAT);
		estimatedTime.setDigits(0);
		estimatedTime.setMaximum(100);
		estimatedTime.setMinimum(0);
		estimatedTime.setIncrement(1);
		estimatedTime.setSelection(DEFAULT_ESTIMATED_TIME);
		estimatedTime.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		GridData estimatedDataLayout = new GridData();
		estimatedDataLayout.widthHint = 30;
		estimatedTime.setLayoutData(estimatedDataLayout);
		// getManagedForm().getToolkit().createLabel(sectionClient, "hours ");
		// label.setForeground(toolkit.getColors().getColor(FormColors.TITLE));

		ImageHyperlink clearEstimated = getManagedForm().getToolkit().createImageHyperlink(sectionClient, SWT.NONE);
		clearEstimated.setImage(CommonImages.getImage(CommonImages.REMOVE));
		clearEstimated.setToolTipText("Clear");
		clearEstimated.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				estimatedTime.setSelection(0);
			}
		});

		getManagedForm().getToolkit().paintBordersFor(sectionClient);
	}

	@Override
	protected void addRadioButtons(Composite buttonComposite) {
		// Since NewBugModels have no special submitting actions,
		// no radio buttons are required.
	}

	@Override
	protected void createCustomAttributeLayout(Composite composite) {
		// ignore
	}

	@Override
	protected void saveTaskOffline(IProgressMonitor progressMonitor) {
		taskData.setSummary(newSummary);
		taskData.setDescription(descriptionTextViewer.getTextWidget().getText());
		updateEditorTitle();
	}

	/**
	 * A listener for selection of the summary textbox.
	 */
	protected class DescriptionListener implements Listener {
		public void handleEvent(Event event) {
			fireSelectionChanged(new SelectionChangedEvent(selectionProvider, new StructuredSelection(
					new RepositoryTaskSelection(taskData.getTaskId(), taskData.getRepositoryUrl(),
							taskData.getConnectorKind(), "New Description", false, taskData.getSummary()))));
		}
	}

	@Override
	protected void validateInput() {
		// ignore
	}

	@Override
	public boolean isDirty() {
		return true;
	}

	/**
	 * @author Raphael Ackermann (bug 198526)
	 */
	@Override
	public void setFocus() {
		if (summaryText != null) {
			summaryText.setFocus();
		}
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * Creates the button layout. This displays options and buttons at the bottom of the editor to allow actions to be
	 * performed on the bug.
	 */
	@Override
	protected void createActionsLayout(Composite formComposite) {
		Section section = getManagedForm().getToolkit().createSection(formComposite, ExpandableComposite.TITLE_BAR);

		section.setText(getSectionLabel(SECTION_NAME.ACTIONS_SECTION));
		section.setExpanded(true);
		section.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, true).applyTo(section);

		Composite buttonComposite = getManagedForm().getToolkit().createComposite(section);
		buttonComposite.setLayout(new GridLayout(4, false));
		buttonComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		section.setClient(buttonComposite);

		addToCategory = getManagedForm().getToolkit().createButton(buttonComposite, "Add to Category", SWT.CHECK);
		categoryChooser = new CCombo(buttonComposite, SWT.FLAT | SWT.READ_ONLY);
		categoryChooser.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		categoryChooser.setLayoutData(GridDataFactory.swtDefaults().hint(150, SWT.DEFAULT).create());
		getManagedForm().getToolkit().adapt(categoryChooser, true, true);
		categoryChooser.setFont(TEXT_FONT);
		ITaskList taskList = TasksUiInternal.getTaskList();
		List<AbstractTaskCategory> categories = new ArrayList<AbstractTaskCategory>(taskList.getCategories());
		Collections.sort(categories, new Comparator<AbstractTaskContainer>() {

			public int compare(AbstractTaskContainer c1, AbstractTaskContainer c2) {
				if (c1.equals(TasksUiPlugin.getTaskList().getDefaultCategory())) {
					return -1;
				} else if (c2.equals(TasksUiPlugin.getTaskList().getDefaultCategory())) {
					return 1;
				} else {
					return c1.getSummary().compareToIgnoreCase(c2.getSummary());
				}
			}

		});

		for (IRepositoryElement category : categories) {
			categoryChooser.add(category.getSummary());
		}

		categoryChooser.select(0);
		categoryChooser.setEnabled(false);
		categoryChooser.setData(categories);
		addToCategory.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				categoryChooser.setEnabled(addToCategory.getSelection());
			}

		});

		GridDataFactory.fillDefaults().hint(DEFAULT_FIELD_WIDTH, SWT.DEFAULT).span(3, SWT.DEFAULT).applyTo(
				categoryChooser);

		addActionButtons(buttonComposite);

		getManagedForm().getToolkit().paintBordersFor(buttonComposite);
	}

	/**
	 * Returns the {@link AbstractTaskContainer category} the new task belongs to
	 * 
	 * @return {@link AbstractTaskContainer category} where the new task must be added to, or null if it must not be
	 *         added to the task list
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected AbstractTaskCategory getCategory() {
		int index = categoryChooser.getSelectionIndex();
		if (addToCategory.getSelection() && index != -1) {
			return ((List<AbstractTaskCategory>) categoryChooser.getData()).get(index);
		}
		return null;
	}

	@Override
	protected void addActionButtons(Composite buttonComposite) {
		FormToolkit toolkit = new FormToolkit(buttonComposite.getDisplay());
		submitButton = toolkit.createButton(buttonComposite, LABEL_SUMBIT, SWT.NONE);
		GridData submitButtonData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		submitButtonData.widthHint = 100;
		submitButton.setImage(CommonImages.getImage(TasksUiImages.REPOSITORY_SUBMIT));
		submitButton.setLayoutData(submitButtonData);
		submitButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				submitToRepository();
			}
		});
		submitButton.setToolTipText("Submit to " + this.repository.getRepositoryUrl());
	}

	protected boolean prepareSubmit() {
		submitButton.setEnabled(false);
		showBusy(true);

		if (summaryText != null && summaryText.getText().trim().equals("")) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openInformation(AbstractNewRepositoryTaskEditor.this.getSite().getShell(),
							ERROR_CREATING_BUG_REPORT, "A summary must be provided with new bug reports.");
					summaryText.setFocus();
					submitButton.setEnabled(true);
					showBusy(false);
				}
			});
			return false;
		}

		if (descriptionTextViewer != null && descriptionTextViewer.getTextWidget().getText().trim().equals("")) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openInformation(AbstractNewRepositoryTaskEditor.this.getSite().getShell(),
							ERROR_CREATING_BUG_REPORT, "A summary must be provided with new reports.");
					descriptionTextViewer.getTextWidget().setFocus();
					submitButton.setEnabled(true);
					showBusy(false);
				}
			});
			return false;
		}

		return true;
	}

	@Override
	protected void createPeopleLayout(Composite composite) {
		// ignore, new editor doesn't have people section
	}

	@Override
	public AbstractTask updateSubmittedTask(String id, IProgressMonitor monitor) throws CoreException {
		final AbstractTask newTask = super.updateSubmittedTask(id, monitor);

		if (newTask != null) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					DateRange selectedDate = null;
					if (scheduledForDate != null) {
						selectedDate = scheduledForDate.getScheduledDate();
					}
					if (selectedDate != null) {
						TasksUiPlugin.getTaskActivityManager().setScheduledFor(newTask, selectedDate);
					}

					if (estimatedTime != null) {
						newTask.setEstimatedTimeHours(estimatedTime.getSelection());
					}

					Object selectedObject = null;
					if (TaskListView.getFromActivePerspective() != null) {
						selectedObject = ((IStructuredSelection) TaskListView.getFromActivePerspective()
								.getViewer()
								.getSelection()).getFirstElement();
					}

					if (selectedObject instanceof TaskCategory) {
						TasksUiInternal.getTaskList().addTask(newTask, ((TaskCategory) selectedObject));
					}
				}
			});
		}

		return newTask;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		new MessageDialog(null, "Operation not supported", null,
				"Save of un-submitted new tasks is not currently supported.\nPlease submit all new tasks.",
				MessageDialog.INFORMATION, new String[] { IDialogConstants.OK_LABEL }, 0).open();
		monitor.setCanceled(true);
		return;
	}

	@Override
	public boolean searchForDuplicates() {
		// called so that the description text is set on taskData before we
		// search for duplicates
		this.saveTaskOffline(new NullProgressMonitor());
		return super.searchForDuplicates();
	}

	@Override
	protected boolean supportsRefreshAttributes() {
		// see bug 212475
		return false;
	}

}
