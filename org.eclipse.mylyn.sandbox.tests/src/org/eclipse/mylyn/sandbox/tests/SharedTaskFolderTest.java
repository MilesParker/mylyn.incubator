package org.eclipse.mylyn.sandbox.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import junit.framework.TestCase;

import org.eclipse.mylyn.context.core.ContextCorePlugin;
import org.eclipse.mylyn.internal.context.core.InteractionContext;
import org.eclipse.mylyn.internal.sandbox.ui.SandboxUiPlugin;
import org.eclipse.mylyn.internal.sandbox.ui.actions.SwitchTaskDataFolderAction;
import org.eclipse.mylyn.internal.tasks.core.LocalTask;
import org.eclipse.mylyn.monitor.core.InteractionEvent;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.ui.TaskListManager;
import org.eclipse.mylyn.tasks.ui.TasksUiPlugin;

/**
 * Tests changing the shared task directory
 * currently in use.
 * 
 * @author Wesley Coelho
 */
public class SharedTaskFolderTest extends TestCase{

	private File sharedDataRootDir = null;
	private File bobsDataDir = null;
	private File jillsDataDir = null;
	private String originalMainDataDir = null;
	private String originalSharedDataDir = null;
	private TaskListManager manager = TasksUiPlugin.getTaskListManager(); 
	
	/**
	 * Set up a shared task directory structure by creating some data
	 * in the main directory and copying it to the shared directories.
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		//Get the original main data directory so that it can be reset later
		originalMainDataDir = TasksUiPlugin.getDefault().getDataDirectory();
		
		//Create a task to make sure there is some data in the main directory
		createAndSaveTask("Task1");
		
		//Create the shared data directory structure
		sharedDataRootDir = new File(TasksUiPlugin.getDefault().getDataDirectory() + File.separator + "SharedDataDir");
		sharedDataRootDir.mkdir();
		assertTrue(sharedDataRootDir.exists());	
		
		bobsDataDir = new File(sharedDataRootDir.getPath() + File.separator + "Bob");
		bobsDataDir.mkdir();
		assertTrue(bobsDataDir.exists());
		
		jillsDataDir = new File(sharedDataRootDir.getPath() + File.separator + "Jill");
		jillsDataDir.mkdir();
		assertTrue(jillsDataDir.exists());

		//Start the shared data dirs off with copies of the main data
		File mainDataDir = new File(originalMainDataDir);
		for ( File currFile : mainDataDir.listFiles()) {
			File destFile = new File(bobsDataDir.getPath() + File.separator + currFile.getName());
			copy(currFile, destFile);
			destFile = new File(jillsDataDir.getPath() + File.separator + currFile.getName());
			copy(currFile, destFile);
		}
		
		//Set the shared data dir
		originalSharedDataDir = SandboxUiPlugin.getDefault().getSharedDataDirectoryManager().getSharedDataDirectory();
//		MylarReportsPlugin.getDefault().getPreferenceStore().setValue(MylarReportsPlugin.SHARED_TASK_DATA_ROOT_DIR, sharedDataRootDir.getPath());
		SandboxUiPlugin.getDefault().getSharedDataDirectoryManager().setSharedDataDirectory(sharedDataRootDir.getPath());
		assertFalse(TasksUiPlugin.getDefault().getDataDirectory().equals(sharedDataRootDir.getPath()));
	}
	
	/**
	 * Tests moving the main mylar data directory to another location
	 */
	public void testSharedDataDirSwitching(){
		SwitchTaskDataFolderAction switchAction = new SwitchTaskDataFolderAction();
		
		//Create a task to appear in the main data dir only
		ITask mainDataDirTask = createAndSaveTask("Main Dir Task");
		
		//Check the options of folders to switch to
		String[] sharedDataFolderOptions = switchAction.getFolderStrings();
		//Note that index 0 is a string indicating a switch back to the main data directory
		assertTrue(sharedDataFolderOptions[0].equals("Bob"));
		assertTrue(sharedDataFolderOptions[1].equals("Jill"));
		
		//Switch to Bob's folder
		switchAction.switchTaskDataFolder(sharedDataFolderOptions[0]);
		
		//Check that the task created in the main data dir isn't there
		File mainDataDirTaskFile = ContextCorePlugin.getContextManager().getFileForContext(mainDataDirTask.getHandleIdentifier());
//		File mainDataDirTaskFile = new File(ContextCorePlugin.getDefault().getDataDirectory() + File.separator + mainDataDirTask.getContextPath() + MylarTaskListPlugin.FILE_EXTENSION);
		assertFalse(mainDataDirTaskFile.exists());
		assertNull(manager.getTaskList().getTask(mainDataDirTask.getHandleIdentifier()));
		
		fail(); // uncomment below
		//Create a new task in bob's task data folder only and check that it exists in the right place
//		ITask bobsTask = createAndSaveTask("Bob's Task");
//		
//		File bobsTaskFile = new File(bobsDataDir.getPath() + File.separator + 
//				ContextCorePlugin.getContextManager().getFileForContext(mainDataDirTask.getHandleIdentifier()).getName());
//		assertTrue(bobsTaskFile.exists());
//		bobsTaskFile = new File(ContextCorePlugin.getDefault().getDataDirectory() + File.separator + bobsTask.getContextPath() + MylarTaskListPlugin.FILE_EXTENSION);
//		assertTrue(bobsTaskFile.exists());
//		assertNotNull(manager.getTaskForHandle(bobsTask.getHandleIdentifier(), false));
//		
//		//Switch to Jill's folder
//		switchAction.switchTaskDataFolder(sharedDataFolderOptions[1]);
//		
//		//Check that Bob's task isn't there
//		bobsTaskFile = new File(ContextCorePlugin.getDefault().getDataDirectory() + File.separator + bobsTask.getContextPath() + MylarTaskListPlugin.FILE_EXTENSION);
//		assertFalse(bobsTaskFile.exists());
//		assertNull(manager.getTaskForHandle(bobsTask.getHandleIdentifier(), false));		
//		
//		//Switch back to Bob's folder
//		switchAction.switchTaskDataFolder(sharedDataFolderOptions[0]);
//		
//		//Check that bob's task is still there
//		bobsTaskFile = new File(ContextCorePlugin.getDefault().getDataDirectory() + File.separator + bobsTask.getContextPath() + MylarTaskListPlugin.FILE_EXTENSION);
//		assertTrue(bobsTaskFile.exists());
//		assertNotNull(manager.getTaskForHandle(bobsTask.getHandleIdentifier(), false));			
//		
//		//Switch back to the main data folder
//		sharedDataFolderOptions = switchAction.getFolderStrings();
//		switchAction.switchTaskDataFolder(sharedDataFolderOptions[0]);
//		
//		//Check that the main task is there
//		mainDataDirTaskFile = new File(ContextCorePlugin.getDefault().getDataDirectory() + File.separator + mainDataDirTask.getContextPath() + MylarTaskListPlugin.FILE_EXTENSION);
//		assertTrue(mainDataDirTaskFile.exists());
//		assertNotNull(manager.getTaskForHandle(mainDataDirTask.getHandleIdentifier(), false));			
		
	}
		
	/**
	 * Creates a task with an interaction event and checks that it has
	 * been properly saved in the currently active data directory
	 */
	protected ITask createAndSaveTask(String taskName){
		
		//Create the task and add it to the root of the task list
		ITask newTask = new LocalTask("" + Calendar.getInstance().getTimeInMillis(), taskName);
		manager.getTaskList().moveToRoot(newTask);
		InteractionContext mockContext = ContextCorePlugin.getContextManager().loadContext(newTask.getHandleIdentifier());//, newTask.getContextPath());
		InteractionEvent event = new InteractionEvent(InteractionEvent.Kind.EDIT,"structureKind","handle","originId");
		mockContext.parseEvent(event);
		ContextCorePlugin.getContextManager().activateContext(mockContext);

		fail(); // uncomment below
//		//Save the context file and check that it exists
//		ContextCorePlugin.getContextManager().saveContext(mockContext.getId());//, newTask.getContextPath());
//		File taskFile = new File(ContextCorePlugin.getDefault().getDataDirectory() + File.separator + newTask.getContextPath() + MylarContextManager.CONTEXT_FILE_EXTENSION);
//		assertTrue(ContextCorePlugin.getContextManager().hasContext(newTask.getContextPath()));
//		assertTrue(taskFile.exists());			
		
		return newTask;
	}
		
	protected void tearDown() throws Exception{
		
		//Reset the shared directory to the original value
		SandboxUiPlugin.getDefault().getSharedDataDirectoryManager().setSharedDataDirectory(originalSharedDataDir);
//		MylarReportsPlugin.getDefault().getPreferenceStore().setValue(MylarReportsPlugin.SHARED_TASK_DATA_ROOT_DIR, originalSharedDataDir);
		
		//Delete the test shared data directories
		deleteDir(bobsDataDir);
		deleteDir(jillsDataDir);
		deleteDir(sharedDataRootDir);
		
		super.tearDown();
	}
	
	private void deleteDir(File targetDir){
		File[] files = targetDir.listFiles();
		for (File file : files) {
			file.delete();
		}
		
		targetDir.delete();
		assertFalse(targetDir.exists());
	}
	
	// Note: Copied from MylarTaskListPlugin
	private boolean copy(File src, File dst) {
		try {
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dst);

			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			return true;
		} catch (IOException ioe) {
			return false;
		}
	}
	
}
