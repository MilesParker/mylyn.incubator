package org.eclipse.mylar.monitor.reports.ui.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.mylar.core.MylarPlugin;
import org.eclipse.mylar.monitor.reports.MylarReportsPlugin;
import org.eclipse.mylar.tasklist.MylarTasklistPlugin;
import org.eclipse.mylar.tasklist.ui.ComboSelectionDialog;
import org.eclipse.mylar.tasklist.ui.views.TaskListView;
import org.eclipse.mylar.ui.actions.ToggleContextCaptureAction;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Contributes a menu item to the task list that opens a dialog box that allows
 * the user to select a task data directory to use.
 * 
 * @author Wesley Coelho
 */
public class SwitchTaskDataFolderAction extends Action implements IViewActionDelegate {

	protected final static String TITLE = "Switch Task Data Folder";

	protected final static String PROMPT = "Select the folder to switch to:";

	protected final static String MAIN_LOCAL_DATA_DIR = "Main Task Data Folder";

	protected Shell shell = null;

	public void init(IViewPart view) {
		shell = view.getViewSite().getShell();
	}

	public void run(IAction action) {

		String[] folderStrings = getFolderStrings();
		String targetFolder = null;
		if (folderStrings != null) {
			targetFolder = showComboSelectionDialog(folderStrings);
		}

		if (targetFolder != null && !targetFolder.trim().equals("")) {
			switchTaskDataFolder(targetFolder);
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// No action required
	}

	/**
	 * Prompts the user for the name of the task data folder to switch to.
	 * Returns the string selected by the user or the empty string if no valid
	 * input was given.
	 */
	protected String showComboSelectionDialog(String[] folderStrings) {
		ComboSelectionDialog dialog = new ComboSelectionDialog(shell, TITLE, PROMPT, folderStrings, 0);
		dialog.open();

		return dialog.getSelectedString();
	}

	/**
	 * Returns an array of the string names of the folders in the main data
	 * directory plus the default folder.
	 * 
	 * This performs validation and will alert the user of the following
	 * failures: - No shared task folder specified - No subfolders with task
	 * data
	 * 
	 * Will return null on failure (Public for testing only)
	 */
	public String[] getFolderStrings() {
		try {
			List<String> folders = new ArrayList<String>();

			// Check that a shared data path has been specified
			String sharedDataPath = MylarReportsPlugin.getDefault().getRootSharedDataDirectory();
			if (sharedDataPath.trim().equals("")) {
				MessageDialog.openError(shell, "Switch Task Folder Error",
						"Please specify a root shared task folder in the Mylar Reports preference page.");
				return null;
			}

			// Check that the directory is accessible
			File rootDir = new File(sharedDataPath);
			if (!rootDir.exists() || !rootDir.isDirectory()) {
				MessageDialog.openError(shell, "Switch Task Folder Error",
						"The root folder specified in the Mylar Reports preference page could not be found.");
				return null;
			}

			// Add the option to switch back to the main local data directory
			if (MylarPlugin.getDefault().isSharedDataDirectoryEnabled()) {
				folders.add(MAIN_LOCAL_DATA_DIR);
			}

			// Add the list of folders with task data
			File[] files = rootDir.listFiles();
			for (int i = 0; i < files.length; i++) {
				File currFile = files[i];
				if (currFile.isDirectory() && containsTaskData(currFile)
						&& !MylarPlugin.getDefault().getMylarDataDirectory().endsWith(currFile.getName())) {
					folders.add(currFile.getName());
				}
			}

			// Check that at least one folder was found to switch to
			if (!(folders.size() > 0)) {
				MessageDialog
						.openError(shell, "Switch Task Folder Error",
								"No task data folders were found in the root folder specified in the Mylar Reports preference page.");
				return null;
			}

			String[] folderStrings = new String[folders.size()];
			for (int i = 0; i < folderStrings.length; i++) {
				folderStrings[i] = folders.get(i);
			}

			return folderStrings;

		} catch (RuntimeException e) {
			MylarPlugin.fail(e, "Could not create list of task folders to switch to.", true);
			return null;
		}
	}

	/**
	 * Returns true if the specified folder contains task data. Currently just
	 * checks if it contains a task list file.
	 */
	protected boolean containsTaskData(File folder) {
		File[] files = folder.listFiles();
		for (int i = 0; i < files.length; i++) {
			File currFile = files[i];
			if (currFile.isFile() && currFile.getName().equals(MylarTasklistPlugin.DEFAULT_TASK_LIST_FILE)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Switches the current data folder to the specified folder. Specify only
	 * the folder name and not the full path.
	 * 
	 * Note: This currently always resumes capture when returning to the main
	 * data directory (and suspends capture when switching to a shared
	 * directory).
	 * 
	 * (Public for testing only)
	 */
	public void switchTaskDataFolder(String targetFolder) {

		MylarTasklistPlugin.getDefault().saveTaskListAndContexts();

		if (targetFolder.equals(MAIN_LOCAL_DATA_DIR)) {
			MylarPlugin.getDefault().setSharedDataDirectoryEnabled(false);
			MylarTasklistPlugin.getDefault().setDataDirectory(MylarPlugin.getDefault().getMylarDataDirectory());
			(new ToggleContextCaptureAction()).resume(); // TODO: don't use
															// actions directly
			TaskListView.getDefault().indicateSharedFolder("");
			MylarPlugin.getContextManager().setActivationHistorySuppressed(false);
		} else {
			String dataDirPath = MylarReportsPlugin.getDefault().getRootSharedDataDirectory() + File.separator
					+ targetFolder;
			MylarPlugin.getDefault().setSharedDataDirectory(dataDirPath);
			MylarPlugin.getDefault().setSharedDataDirectoryEnabled(true);
			MylarTasklistPlugin.getDefault().setDataDirectory(dataDirPath);
			(new ToggleContextCaptureAction()).pause();
			TaskListView.getDefault().indicateSharedFolder(targetFolder);
			MylarPlugin.getContextManager().setActivationHistorySuppressed(true);
		}
	}

}
