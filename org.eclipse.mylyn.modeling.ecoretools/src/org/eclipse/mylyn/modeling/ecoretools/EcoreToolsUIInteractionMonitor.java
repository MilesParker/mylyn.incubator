package org.eclipse.mylyn.modeling.ecoretools;

import java.util.Iterator;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.modeling.context.EMFStructureBridge;
import org.eclipse.mylyn.modeling.context.IModelStructureProvider;
import org.eclipse.mylyn.monitor.ui.AbstractUserInteractionMonitor;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.navigator.resources.ProjectExplorer;

public class EcoreToolsUIInteractionMonitor extends
		AbstractUserInteractionMonitor {

	private final EMFStructureBridge structure;

	public EcoreToolsUIInteractionMonitor(EMFStructureBridge structure) {
		this.structure = structure;
	}

	@Override
	protected void handleWorkbenchPartSelection(IWorkbenchPart part,
			ISelection selection, boolean contributeToContext) {
		if (part instanceof ProjectExplorer
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			for (Iterator<?> iterator = structuredSelection.iterator(); iterator
					.hasNext();) {
				Object object = iterator.next();
				if (structure.acceptsObject(object)) {
					handleElementSelection(part, object, contributeToContext);
				}
			}
		}
	}

}
