package org.eclipse.mylyn.emf.ui;

import org.eclipse.mylyn.emf.context.IDomainContextBridge;
import org.eclipse.ui.IEditorPart;

public interface IDomainUIBridge extends IDomainContextBridge {

	boolean acceptsEditor(IEditorPart editorPart);
}
