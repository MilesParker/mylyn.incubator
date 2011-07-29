package org.eclipse.mylyn.modeling.ecoretools;

import org.eclipse.mylyn.emf.ui.DiagramUIBridge;
import org.eclipse.mylyn.emf.ui.IDomainUIBridge;

public class EcoreDiagramUIBridge extends DiagramUIBridge {
	public IDomainUIBridge getDomainUIBridge() {
		return EcoreDiagramDomainBridge.getInstance();
	};
}
