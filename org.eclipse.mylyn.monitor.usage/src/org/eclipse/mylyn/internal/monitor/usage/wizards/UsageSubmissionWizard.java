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
package org.eclipse.mylar.internal.monitor.usage.wizards;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.mylar.core.MylarStatusHandler;
import org.eclipse.mylar.internal.core.util.ZipFileUtil;
import org.eclipse.mylar.internal.monitor.usage.InteractionEventLogger;
import org.eclipse.mylar.internal.monitor.usage.MylarUsageMonitorPlugin;
import org.eclipse.mylar.monitor.core.InteractionEvent;
import org.eclipse.mylar.monitor.usage.IBackgroundPage;
import org.eclipse.mylar.monitor.usage.IQuestionnairePage;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * A wizard for uploading the Mylar statistics to a website
 * 
 * @author Shawn Minto
 */
public class UsageSubmissionWizard extends Wizard implements INewWizard {

	public static final String LOG = "log";

	public static final String STATS = "usage";

	public static final String QUESTIONAIRE = "questionaire";

	public static final String BACKGROUND = "background";

	private static final String ORG_ECLIPSE_PREFIX = "org.eclipse.";

	public static final int HTTP_SERVLET_RESPONSE_SC_OK = 200;

	public static final int SIZE_OF_INT = 8;

	private boolean failed = false;

	private boolean displayBackgroundPage = false;

	/** The id of the user */
	private int uid;

	private final File monitorFile = MylarUsageMonitorPlugin.getDefault().getMonitorLogFile();

	// private final File logFile =
	// MylarMonitorPlugin.getDefault().getLogFile();

	private UsageUploadWizardPage uploadPage;

	// private GetNewUserIdPage getUidPage;

	private IQuestionnairePage questionnairePage;

	private IBackgroundPage backgroundPage;

	private boolean performUpload = true;

	public UsageSubmissionWizard() {
		super();
		setTitles();
		init(true);
	}

	public UsageSubmissionWizard(boolean performUpload) {
		super();
		setTitles();
		init(performUpload);
	}

	private void setTitles() {
		super.setDefaultPageImageDescriptor(MylarUsageMonitorPlugin.imageDescriptorFromPlugin(
				MylarUsageMonitorPlugin.PLUGIN_ID, "icons/wizban/banner-user.gif"));
		super.setWindowTitle("Mylar Feedback");
	}

	private void init(boolean performUpload) {
		this.performUpload = performUpload;
		setNeedsProgressMonitor(true);
		uid = MylarUsageMonitorPlugin.getDefault().getPreferenceStore().getInt(MylarUsageMonitorPlugin.PREF_USER_ID);
		if (uid == 0 || uid == -1) {
			uid = this.getNewUid();
			MylarUsageMonitorPlugin.getDefault().getPreferenceStore().setValue(MylarUsageMonitorPlugin.PREF_USER_ID,
					uid);
		}
		uploadPage = new UsageUploadWizardPage(this);
		if (MylarUsageMonitorPlugin.getDefault().isBackgroundEnabled()) {
			IBackgroundPage page = MylarUsageMonitorPlugin.getDefault().getStudyParameters().getBackgroundPage();
			backgroundPage = page;
		}
		if (MylarUsageMonitorPlugin.getDefault().isQuestionnaireEnabled() && performUpload) {
			IQuestionnairePage page = MylarUsageMonitorPlugin.getDefault().getStudyParameters().getQuestionnairePage();
			questionnairePage = page;
		}
		super.setForcePreviousAndNextButtons(true);
	}

	private File questionnaireFile = null;

	private File backgroundFile = null;

	@Override
	public boolean performFinish() {

		// int numEvents =
		// MylarMonitorPlugin.getPrefs().getInt(MylarMonitorPlugin.PREF_NUM_USER_EVENTS);
		// int numSinceLastPhase =
		// MylarMonitorPlugin.NEXT_PHASE_EVENT_THRESHOLD
		// -
		// MylarMonitorPlugin.getPrefs().getInt(MylarMonitorPlugin.PREF_NUM_USER_EVENTS)
		// -
		// MylarMonitorPlugin.getPrefs().getInt(MylarMonitorPlugin.PREF_NUM_USER_EVENTS_LAST_PHASE);
		// ContextCorePlugin.log("Number user events: " + numEvents, this);
		// ContextCorePlugin.log("Number events needed: " + numSinceLastPhase,
		// this);
		// ContextCorePlugin.log("Date next release: " +
		// DateUtil.getFormattedDateTime(MylarMonitorPlugin.NEXT_RELEASE_AVAILABLE.getTimeInMillis()),
		// this);

		if (!performUpload)
			return true;
		if (MylarUsageMonitorPlugin.getDefault().isQuestionnaireEnabled() && performUpload && questionnairePage != null) {
			questionnaireFile = questionnairePage.createFeedbackFile();
		}
		if (MylarUsageMonitorPlugin.getDefault().isBackgroundEnabled() && performUpload && displayBackgroundPage
				&& backgroundPage != null) {
			backgroundFile = backgroundPage.createFeedbackFile();
		}

		// final WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
		// protected void execute(final IProgressMonitor monitor) throws
		// CoreException {
		// monitor.beginTask("Uploading user statistics", 3);
		// performUpload(monitor);
		// monitor.done();
		// }
		// };

		Job j = new Job("Upload User Statistics") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					monitor.beginTask("Uploading user statistics", 3);
					performUpload(monitor);
					monitor.done();
					// op.run(monitor);
					return Status.OK_STATUS;
				} catch (Exception e) {
					MylarStatusHandler.log(e, "Error uploading statistics");
					return new Status(Status.ERROR, MylarUsageMonitorPlugin.PLUGIN_ID, Status.ERROR,
							"Error uploading statistics", e);
				}
			}
		};
		// j.setUser(true);
		j.setPriority(Job.DECORATE);
		j.schedule();
		return true;
	}

	public void performUpload(IProgressMonitor monitor) {
		if (MylarUsageMonitorPlugin.getDefault().isBackgroundEnabled() && performUpload && backgroundFile != null) {
			upload(backgroundFile, BACKGROUND, monitor);

			if (failed) {
				failed = false;
			}

			if (backgroundFile.exists()) {
				backgroundFile.delete();
			}
		}

		if (MylarUsageMonitorPlugin.getDefault().isQuestionnaireEnabled() && performUpload && questionnaireFile != null) {
			upload(questionnaireFile, QUESTIONAIRE, monitor);

			if (failed) {
				failed = false;
			}

			if (questionnaireFile.exists()) {
				questionnaireFile.delete();
			}
		}
		File zipFile = zipFilesForUpload();
		if (zipFile == null)
			return;

		upload(zipFile, STATS, monitor);

		if (zipFile.exists()) {
			zipFile.delete();
		}

		if (!failed) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					// popup a dialog telling the user that the upload was good
					MessageDialog
							.openInformation(Display.getCurrent().getActiveShell(), "Successful Upload",
									"Your usage statistics have been successfully uploaded.\n Thank you for participating.");
				}
			});

			// clear the files
			// if (!monitorFile.delete()) {
			// MylarStatusHandler.log("Unable to delete the monitor file",
			// this);
			// }
		}

		MylarUsageMonitorPlugin.getDefault().getInteractionLogger().startMonitoring();
		MylarUsageMonitorPlugin.setPerformingUpload(false);
		return;
	}

	public boolean performCancel() {
		MylarUsageMonitorPlugin.getDefault().userCancelSubmitFeedback(new Date(), true);
		return true;
	}

	@Override
	public boolean canFinish() {
		if (!performUpload) {
			return true;// getUidPage.isPageComplete();
		} else {
			return this.getContainer().getCurrentPage() == uploadPage || !performUpload;
		}
	}

	public UsageUploadWizardPage getUploadPage() {
		return uploadPage;
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
	 *      org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// no initialization needed
	}

	@Override
	public void addPages() {
		if (MylarUsageMonitorPlugin.getDefault().isQuestionnaireEnabled() && performUpload && questionnairePage != null) {
			addPage(questionnairePage);
		}
		if (performUpload) {
			addPage(uploadPage);
		}
	}

	public void addBackgroundPage() {
		if (MylarUsageMonitorPlugin.getDefault().isBackgroundEnabled() && backgroundPage != null) {
			addPage(backgroundPage);
			displayBackgroundPage = true;
		}
	}

	/**
	 * Method to upload a file to a cgi script
	 * 
	 * @param f
	 *            The file to upload
	 */
	private void upload(File f, String type, IProgressMonitor monitor) {
		if (failed)
			return;

		int status = 0;

		try {
			String servletUrl = MylarUsageMonitorPlugin.getDefault().getStudyParameters().getServletUrl();
			final PostMethod filePost = new PostMethod(servletUrl);

			Part[] parts = { new FilePart("temp.txt", f) };

			filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));

			final HttpClient client = new HttpClient();

			status = client.executeMethod(filePost);
			filePost.releaseConnection();

		} catch (final Exception e) {
			// there was a problem with the file upload so throw up an error
			// dialog to inform the user and log the exception
			failed = true;
			if (e instanceof NoRouteToHostException || e instanceof UnknownHostException) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
						MessageDialog.openError(null, "Error Uploading", "There was an error uploading the file"
								+ ": \n" + "No network connection.  Please try again later");
					}
				});
			} else {
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
						MessageDialog.openError(null, "Error Uploading", "There was an error uploading the file"
								+ ": \n" + e.getClass().getCanonicalName());
					}
				});
				MylarStatusHandler.log(e, "failed to upload");
			}
		}

		monitor.worked(1);

		final String filedesc = f.getName();

		final int httpResponseStatus = status;

		if (status == 401) {
			// The uid was incorrect so inform the user
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(null, "Error Uploading", "There was an error uploading the " + filedesc
							+ ": \n" + "Your uid was incorrect: " + uid + "\n");
				}
			});
		} else if (status == 407) {
			failed = true;
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					MessageDialog
							.openError(null, "Error Uploading",
									"Could not upload because proxy server authentication failed.  Please check your proxy server settings.");
				}
			});
		} else if (status != 200) {
			failed = true;
			// there was a problem with the file upload so throw up an error
			// dialog to inform the user
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(null, "Error Uploading", "There was an error uploading the " + filedesc
							+ ": \n" + "HTTP Response Code " + httpResponseStatus + "\n" + "Please try again later");
				}
			});
		} else {
			// the file was uploaded successfully
		}

	}

	/*
	 * 
	 * 
	 * String uploadFile; String uploadScript; if (type.equals(STATS) ||
	 * type.equals(LOG)) { uploadFile = "usage statistics file"; uploadScript =
	 * MylarUsageMonitorPlugin.getDefault().getStudyParameters().getScriptsUrl() +
	 * MylarUsageMonitorPlugin.getDefault().getStudyParameters().getScriptsUpload(); }
	 * else { uploadFile = "questionnaire"; uploadScript =
	 * MylarUsageMonitorPlugin.getDefault().getStudyParameters().getScriptsUrl() +
	 * MylarUsageMonitorPlugin.getDefault().getStudyParameters().getScriptsQuestionnaire(); }
	 * 
	 * 
	 * 
	 * 
	 * 
	 * Part[] parts; if (type.equals(STATS)) { Part[] p = { new FilePart("MYLAR" +
	 * uid, MylarUsageMonitorPlugin.UPLOAD_FILE_LABEL + "-" +
	 * MylarUsageMonitorPlugin.VERSION + extensionVersion + "-" + STATS + "-" +
	 * uid + "-" + DateUtil.getFormattedDateTime(time) + ".zip", f) }; parts =
	 * p; uploadFile = "usage statistics file"; } else if (type.equals(LOG)) {
	 * Part[] p = { new FilePart("MYLAR" + uid,
	 * MylarUsageMonitorPlugin.UPLOAD_FILE_LABEL + "-" +
	 * MylarUsageMonitorPlugin.VERSION + extensionVersion + "-" + LOG + "-" +
	 * uid + "-" + DateUtil.getFormattedDateTime(time) + ".txt", f) }; parts =
	 * p; uploadFile = "mylar log file"; } else if (type.equals(QUESTIONAIRE)) {
	 * Part[] p = { new FilePart("MYLAR" + uid,
	 * MylarUsageMonitorPlugin.UPLOAD_FILE_LABEL + "-" +
	 * MylarUsageMonitorPlugin.VERSION + extensionVersion + "-" + QUESTIONAIRE +
	 * "-" + uid + "-" + DateUtil.getFormattedDateTime(time) + ".txt", f) };
	 * parts = p; uploadFile = "questionnaire"; } else if
	 * (type.equals(BACKGROUND)) { Part[] p = { new FilePart("MYLAR" + uid,
	 * MylarUsageMonitorPlugin.UPLOAD_FILE_LABEL + "-" +
	 * MylarUsageMonitorPlugin.VERSION + extensionVersion + "-" + BACKGROUND +
	 * "-" + uid + "-" + DateUtil.getFormattedDateTime(time) + ".txt", f) };
	 * parts = p; uploadFile = "background"; } else { failed = true; return; }
	 */

	public String getMonitorFileName() {
		return monitorFile.getAbsolutePath();
	}

	// public String getLogFileName(){
	// return logFile.getAbsolutePath();
	// }

	/** The status from the http request */
	private int status;

	/** the response for the http request */
	private String resp;

	public int getExistingUid(String firstName, String lastName, String emailAddress, boolean anonymous) {
		if (failed)
			return -1;
		try {

			// TODO, do this method properly
			// create a new post method
			final GetMethod getUidMethod = new GetMethod(MylarUsageMonitorPlugin.getDefault().getStudyParameters()
					.getServletUrl()
					+ MylarUsageMonitorPlugin.getDefault().getStudyParameters().getServletUrl());

			NameValuePair first = new NameValuePair("firstName", firstName);
			NameValuePair last = new NameValuePair("lastName", lastName);
			NameValuePair email = new NameValuePair("email", emailAddress);
			NameValuePair job = new NameValuePair("jobFunction", "");
			NameValuePair size = new NameValuePair("companySize", "");
			NameValuePair buisness = new NameValuePair("companyBuisness", "");
			NameValuePair contact = new NameValuePair("contact", "");
			NameValuePair anon = null;
			if (anonymous) {
				anon = new NameValuePair("anonymous", "true");
			} else {
				anon = new NameValuePair("anonymous", "false");
			}

			if (MylarUsageMonitorPlugin.getDefault().usingContactField())
				getUidMethod.setQueryString(new NameValuePair[] { first, last, email, job, size, buisness, anon,
						contact });
			else
				getUidMethod.setQueryString(new NameValuePair[] { first, last, email, job, size, buisness, anon });

			// create a new client and upload the file
			final HttpClient client = new HttpClient();
			MylarUsageMonitorPlugin.getDefault().configureProxy(client);

			ProgressMonitorDialog pmd = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
			pmd.run(false, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Get User Id", 1);

					try {
						status = client.executeMethod(getUidMethod);

						resp = getData(getUidMethod.getResponseBodyAsStream());

						// release the connection to the server
						getUidMethod.releaseConnection();
					} catch (final Exception e) {
						// there was a problem with the file upload so throw up
						// an error
						// dialog to inform the user and log the exception
						failed = true;
						if (e instanceof NoRouteToHostException || e instanceof UnknownHostException) {
							PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
								public void run() {
									MessageDialog.openError(null, "Error Uploading",
											"There was an error getting a new user id: \n"
													+ "No network connection.  Please try again later");
								}
							});
						} else {
							PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
								public void run() {
									MessageDialog.openError(null, "Error Uploading",
											"There was an error getting a new user id: \n"
													+ e.getClass().getCanonicalName() + e.getMessage());
								}
							});
							MylarStatusHandler.log(e, "error uploading");
						}
					}
					monitor.worked(1);
					monitor.done();
				}
			});

			if (status != 200) {
				// there was a problem with the file upload so throw up an error
				// dialog to inform the user

				failed = true;

				// there was a problem with the file upload so throw up an error
				// dialog to inform the user
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
						MessageDialog.openError(null, "Error Getting User ID",
								"There was an error getting a user id: \n" + "HTTP Response Code " + status + "\n"
										+ "Please try again later");
					}
				});
			} else {
				resp = resp.substring(resp.indexOf(":") + 1).trim();
				uid = Integer.parseInt(resp);
				MylarUsageMonitorPlugin.getDefault().getPreferenceStore().setValue(
						MylarUsageMonitorPlugin.PREF_USER_ID, uid);
				return uid;
			}

		} catch (final Exception e) {
			// there was a problem with the file upload so throw up an error
			// dialog to inform the user and log the exception
			failed = true;
			if (e instanceof NoRouteToHostException || e instanceof UnknownHostException) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
						MessageDialog.openError(null, "Error Uploading", "There was an error getting a new user id: \n"
								+ "No network connection.  Please try again later");
					}
				});
			} else {
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
						MessageDialog.openError(null, "Error Uploading", "There was an error getting a new user id: \n"
								+ e.getClass().getCanonicalName());
					}
				});
				MylarStatusHandler.log(e, "error uploading");
			}
		}
		return -1;
	}

	public int getNewUid() {
		final PostMethod filePost = new PostMethod(MylarUsageMonitorPlugin.DEFAULT_UPLOAD_SERVER
				+ MylarUsageMonitorPlugin.DEFAULT_UPLOAD_SERVLET_ID);

		filePost.addParameter(new NameValuePair("MylarUserID", ""));
		final HttpClient client = new HttpClient();
		int status = 0;

		try {
			status = client.executeMethod(filePost);

			if (status == HTTP_SERVLET_RESPONSE_SC_OK) {
				InputStream inputStream = filePost.getResponseBodyAsStream();
				byte[] buffer = new byte[SIZE_OF_INT];
				int numBytesRead = inputStream.read(buffer);
				int uid = new Integer(new String(buffer, 0, numBytesRead)).intValue();
				filePost.releaseConnection();

				return uid;
			} else {
				return -1;
			}

		} catch (final Exception e) {
			// there was a problem with the file upload so throw up an error
			// dialog to inform the user and log the exception
			return -1;

		}
	}

	public int getNewUid(String firstName, String lastName, String emailAddress, boolean anonymous, String jobFunction,
			String companySize, String companyFunction, boolean contactEmail) {
		if (failed)
			return -1;
		try {
			addBackgroundPage();

			final PostMethod filePost = new PostMethod(MylarUsageMonitorPlugin.DEFAULT_UPLOAD_SERVER
					+ MylarUsageMonitorPlugin.DEFAULT_UPLOAD_SERVLET_ID);
			filePost.addParameter(new NameValuePair("MylarUserID", ""));
			final HttpClient client = new HttpClient();
			int status = 0;

			try {
				status = client.executeMethod(filePost);

				if (status == 202) {
					InputStream inputStream = filePost.getResponseBodyAsStream();
					byte[] buffer = new byte[8];
					int numBytesRead = inputStream.read(buffer);
					int uid = new Integer(new String(buffer, 0, numBytesRead)).intValue();
					filePost.releaseConnection();

					return uid;
				} else {
					return -1;
				}

			} catch (final Exception e) {
				// there was a problem with the file upload so throw up an error
				// dialog to inform the user and log the exception
			}

			// NameValuePair first = new NameValuePair("firstName", firstName);
			// NameValuePair last = new NameValuePair("lastName", lastName);
			// NameValuePair email = new NameValuePair("email", emailAddress);
			// NameValuePair job = new NameValuePair("jobFunction",
			// jobFunction);
			// NameValuePair size = new NameValuePair("companySize",
			// companySize);
			// NameValuePair buisness = new NameValuePair("companyBuisness",
			// companyFunction);
			// NameValuePair contact = null;
			// if (contactEmail) {
			// contact = new NameValuePair("contact", "true");
			// } else {
			// contact = new NameValuePair("contact", "false");
			// }
			// NameValuePair anon = null;
			// if (anonymous) {
			// anon = new NameValuePair("anonymous", "true");
			// } else {
			// anon = new NameValuePair("anonymous", "false");
			// }

			if (status != 200) {
				// there was a problem with the file upload so throw up an error
				// dialog to inform the user

				failed = true;

				// there was a problem with the file upload so throw up an error
				// dialog to inform the user
				MessageDialog.openError(null, "Error Getting User ID", "There was an error getting a user id: \n"
						+ "HTTP Response Code " + status + "\n" + "Please try again later");
			} else {
				resp = resp.substring(resp.indexOf(":") + 1).trim();
				uid = Integer.parseInt(resp);
				MylarUsageMonitorPlugin.getDefault().getPreferenceStore().setValue(
						MylarUsageMonitorPlugin.PREF_USER_ID, uid);
				return uid;
			}

		} catch (Exception e) {
			// there was a problem with the file upload so throw up an error
			// dialog to inform the user and log the exception
			failed = true;
			if (e instanceof NoRouteToHostException || e instanceof UnknownHostException) {
				MessageDialog.openError(null, "Error Uploading", "There was an error getting a new user id: \n"
						+ "No network connection.  Please try again later");
			} else {
				MessageDialog.openError(null, "Error Uploading", "There was an error getting a new user id: \n"
						+ e.getClass().getCanonicalName());
				MylarStatusHandler.log(e, "error uploading");
			}
		}
		return -1;
	}

	private String getData(InputStream i) {
		String s = "";
		String data = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(i));
		try {
			while ((s = br.readLine()) != null)
				data += s;
		} catch (IOException e) {
			MylarStatusHandler.log(e, "error uploading");
		}
		return data;
	}

	public int getUid() {
		return uid;
	}

	public boolean failed() {
		return failed;
	}

	private File processMonitorFile(File monitorFile) {
		File processedFile = new File("processed-" + MylarUsageMonitorPlugin.MONITOR_LOG_NAME + ".xml");
		InteractionEventLogger logger = new InteractionEventLogger(processedFile);
		logger.startMonitoring();
		List<InteractionEvent> eventList = logger.getHistoryFromFile(monitorFile);

		if (eventList.size() > 0) {
			for (InteractionEvent event : eventList) {
				if (event.getOriginId().startsWith(ORG_ECLIPSE_PREFIX)) {
					logger.interactionObserved(event);
				}
			}
		}

		return processedFile;
	}

	private File zipFilesForUpload() {
		MylarUsageMonitorPlugin.setPerformingUpload(true);
		MylarUsageMonitorPlugin.getDefault().getInteractionLogger().stopMonitoring();

		List<File> files = new ArrayList<File>();
		File monitorFile = MylarUsageMonitorPlugin.getDefault().getMonitorLogFile();
		File fileToUpload = this.processMonitorFile(monitorFile);
		files.add(fileToUpload);

		MylarUsageMonitorPlugin.getDefault().getInteractionLogger().startMonitoring();
		try {
			File zipFile = File.createTempFile(uid + ".", ".zip");
			ZipFileUtil.createZipFile(zipFile, files);
			return zipFile;
		} catch (Exception e) {
			MylarStatusHandler.log(e, "error uploading");
			return null;
		}
	}
}
