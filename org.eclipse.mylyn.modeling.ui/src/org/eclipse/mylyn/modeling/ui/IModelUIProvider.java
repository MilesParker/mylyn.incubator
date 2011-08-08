package org.eclipse.mylyn.modeling.ui;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.mylyn.modeling.context.IModelStructureProvider;
import org.eclipse.ui.IWorkbenchPart;

public interface IModelUIProvider extends IModelStructureProvider {

	boolean acceptsPart(IWorkbenchPart part);

	boolean acceptsEditPart(EObject object, EditPart part);
}
