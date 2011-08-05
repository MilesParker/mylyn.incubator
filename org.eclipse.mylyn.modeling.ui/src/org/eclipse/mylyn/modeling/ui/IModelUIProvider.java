package org.eclipse.mylyn.modeling.ui;

import org.eclipse.mylyn.modeling.context.IModelStructureProvider;
import org.eclipse.ui.IEditorPart;

public interface IModelUIProvider extends IModelStructureProvider {

	boolean acceptsEditor(IEditorPart editorPart);
}
