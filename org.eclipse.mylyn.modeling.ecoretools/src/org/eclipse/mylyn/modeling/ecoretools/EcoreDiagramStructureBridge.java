package org.eclipse.mylyn.modeling.ecoretools;

import org.eclipse.mylyn.emf.context.EMFStructureBridge;
import org.eclipse.mylyn.emf.context.IDomainContextBridge;

public class EcoreDiagramStructureBridge extends EMFStructureBridge {

	@Override
	public IDomainContextBridge getDomainContextBridge() {
		return EcoreDiagramDomainBridge.getInstance();
	}
}
