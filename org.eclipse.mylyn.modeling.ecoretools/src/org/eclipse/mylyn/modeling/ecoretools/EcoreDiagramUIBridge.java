package org.eclipse.mylyn.modeling.ecoretools;

import org.eclipse.mylyn.modeling.ui.DiagramUIBridge;
import org.eclipse.mylyn.modeling.ui.IModelUIProvider;

public class EcoreDiagramUIBridge extends DiagramUIBridge {
	public IModelUIProvider getDomainUIBridge() {
		return EcoreDiagramDomainBridge.getInstance();
	};
}
