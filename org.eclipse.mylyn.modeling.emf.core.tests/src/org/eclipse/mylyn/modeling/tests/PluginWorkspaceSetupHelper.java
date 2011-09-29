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

package org.eclipse.mylyn.modeling.tests;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.mylyn.commons.sdk.util.CommonTestUtil;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.internal.context.core.InteractionContext;
import org.eclipse.mylyn.internal.context.core.InteractionContextScaling;
import org.eclipse.pde.internal.core.natures.PDE;
import org.eclipse.pde.internal.core.natures.PluginProject;

/**
 * @author Miles Parker
 */
public class PluginWorkspaceSetupHelper {

	private static final String HELPER_CONTEXT_ID = "helper-context";

	private static boolean isSetup = false;

	private static InteractionContext taskscape;

	private static IWorkspaceRoot workspaceRoot;

	public static void clearDoiModel() throws CoreException {
		ContextCore.getContextManager().deleteContext(HELPER_CONTEXT_ID);
		taskscape = new InteractionContext(HELPER_CONTEXT_ID, new InteractionContextScaling());
	}

	//TODO replace with org.eclipse.mylyn.context.tests.support.ContextTestUtil methods as available in targeted version of Mylyn Context build API
	public static IWorkspaceRoot setupWorkspace() throws CoreException, IOException, InvocationTargetException,
			InterruptedException {
		if (isSetup) {
			clearDoiModel();
			return workspaceRoot;
		}
		taskscape = new InteractionContext(HELPER_CONTEXT_ID, new InteractionContextScaling());

		workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		isSetup = true;

		return workspaceRoot;
	}

	public static IJavaProject createJavaPluginProjectFromZip(String projectName, String zipFileName)
			throws CoreException, ZipException, IOException {
		IProject project = createProject(projectName);
		ZipFile zip = new ZipFile(CommonTestUtil.getFile(PluginWorkspaceSetupHelper.class, "testdata/projects/"
				+ zipFileName));

		CommonTestUtil.unzip(zip, project.getLocation().toFile());

		project.refreshLocal(IResource.DEPTH_INFINITE, null);

		IJavaProject javaProject = createPluginProject(project);
		return javaProject;
	}

	//Copied from org.eclipse.mylyn.context.tests.support.ContextTestUtil

	private static IProject createProject(String projectName) throws CoreException {

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(projectName);
		if (!project.exists()) {
			project.create(new NullProgressMonitor());
		} else {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		}

		if (!project.isOpen()) {
			project.open(new NullProgressMonitor());
		}

		return project;
	}

	//Copied from org.eclipse.mylyn.context.tests.support.ContextTestUtil

	private static IJavaProject createPluginProject(IProject project) throws CoreException, JavaModelException {

		if (project == null) {
			return null;
		}

		IJavaProject javaProject = JavaCore.create(project);

		// create bin folder
		IFolder binFolder = project.getFolder("bin");
		if (!binFolder.exists()) {
			binFolder.create(false, true, null);
		}

		// set java nature
		IProjectDescription description = project.getDescription();
		description.setNatureIds(new String[] { PDE.PLUGIN_NATURE, JavaCore.NATURE_ID });
		project.setDescription(description, null);

		// create output folder
		IPath outputLocation = binFolder.getFullPath();
		javaProject.setOutputLocation(outputLocation, null);

		PluginProject pluginProject = new PluginProject();
		pluginProject.setProject(project);
		pluginProject.configure();

		return javaProject;
	}

}
