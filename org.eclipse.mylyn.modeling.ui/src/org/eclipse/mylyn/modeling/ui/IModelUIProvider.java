package org.eclipse.mylyn.modeling.ui;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.mylyn.modeling.context.IModelStructureProvider;
import org.eclipse.ui.IEditorPart;

public interface IModelUIProvider extends IModelStructureProvider {

	boolean acceptsEditor(IEditorPart editorPart);

	boolean acceptsEditPart(EObject object, IGraphicalEditPart part);
}
