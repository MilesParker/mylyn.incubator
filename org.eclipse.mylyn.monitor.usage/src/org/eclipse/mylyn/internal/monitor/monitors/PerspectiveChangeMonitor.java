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

package org.eclipse.mylar.internal.monitor.monitors;

import org.eclipse.mylar.context.core.InteractionEvent;
import org.eclipse.mylar.monitor.MylarMonitorPlugin;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;

/**
 * @author Leah Findlater and Mik Kersten
 */
public class PerspectiveChangeMonitor extends PerspectiveAdapter {

	public static final String PERSPECTIVE_SAVED = "perspective saved";

	public static final String PERSPECTIVE_OPENED = "perspective opened";

	public static final String PERSPECTIVE_CLOSED = "perspective closed";

	public static final String PERSPECTIVE_CHANGED = "perspective changed";

	public static final String PERSPECTIVE_ACTIVATED = "perspective activated";

	@Override
	public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		String source = this.getPerspectiveId(perspective);

		InteractionEvent interactionEvent = InteractionEvent.makePreference(source, PERSPECTIVE_ACTIVATED);
		MylarMonitorPlugin.getDefault().notifyInteractionObserved(interactionEvent);
	}

	@Override
	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective,
			IWorkbenchPartReference partRef, String changeId) {
		String source = partRef.getId();
		InteractionEvent interactionEvent = InteractionEvent.makePreference(source, PERSPECTIVE_CHANGED + ": "
				+ changeId);
		MylarMonitorPlugin.getDefault().notifyInteractionObserved(interactionEvent);
	}

	@Override
	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
		String source = this.getPerspectiveId(perspective);
		InteractionEvent interactionEvent = InteractionEvent.makePreference(source, PERSPECTIVE_CHANGED + ": "
				+ changeId);
		MylarMonitorPlugin.getDefault().notifyInteractionObserved(interactionEvent);
	}

	@Override
	public void perspectiveClosed(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		String source = this.getPerspectiveId(perspective);
		InteractionEvent interactionEvent = InteractionEvent.makePreference(source, PERSPECTIVE_CLOSED);
		MylarMonitorPlugin.getDefault().notifyInteractionObserved(interactionEvent);
	}

	@Override
	public void perspectiveOpened(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		String source = this.getPerspectiveId(perspective);
		InteractionEvent interactionEvent = InteractionEvent.makePreference(source, PERSPECTIVE_OPENED);
		MylarMonitorPlugin.getDefault().notifyInteractionObserved(interactionEvent);
	}

	@Override
	public void perspectiveSavedAs(IWorkbenchPage page, IPerspectiveDescriptor oldPerspective,
			IPerspectiveDescriptor newPerspective) {
		String source = this.getPerspectiveId(newPerspective);
		InteractionEvent interactionEvent = InteractionEvent.makePreference(source, PERSPECTIVE_SAVED);
		MylarMonitorPlugin.getDefault().notifyInteractionObserved(interactionEvent);
	}

	private String getPerspectiveId(IPerspectiveDescriptor perspective) {
		String id;

		if (perspective instanceof PerspectiveDescriptor) {
			String originalId = ((PerspectiveDescriptor) perspective).getOriginalId();
			if (!originalId.equals(perspective.getId()))
				id = originalId + "[customized]";
			else
				id = perspective.getId();
		} else
			id = perspective.getId();
		return id;
	}

}

/* Perspective listener methods */

// TODO Should we comment out the more detailed perspective listener methods and
// just use this one instead? This one logs the open set of views and editors
// whenever that changes.
/*
 * @Override public void perspectiveChanged(IWorkbenchPage page,
 * IPerspectiveDescriptor perspective, String changeId) {
 * super.perspectiveChanged(page, perspective, changeId);
 * 
 * if(changeId.startsWith("view") || changeId.startsWith("editor")) {
 * IWorkbenchPage workbenchPage =
 * PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 * IViewReference[] viewList = workbenchPage.getViewReferences();
 * IEditorReference[] editorList = workbenchPage.getEditorReferences();
 * 
 * String delta = ""; for(int i = 0; i < viewList.length; i++) { delta = delta +
 * viewList[i].getTitle() + ","; } delta = delta + "Editor (" +
 * editorList.length + " open)";
 * 
 * String source = "perspective." + perspective.getLabel(); InteractionEvent
 * interactionEvent = new InteractionEvent( source, delta );
 * logger.interactionObserved(interactionEvent); } }
 */
