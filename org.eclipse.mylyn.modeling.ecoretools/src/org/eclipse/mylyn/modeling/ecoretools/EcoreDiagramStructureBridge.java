package org.eclipse.mylyn.modeling.ecoretools;

import org.eclipse.mylyn.modeling.context.EMFStructureBridge;
import org.eclipse.mylyn.modeling.context.IModelStructureProvider;

public class EcoreDiagramStructureBridge extends EMFStructureBridge {

	@Override
	public IModelStructureProvider getDomainContextBridge() {
		return EcoreDiagramDomainBridge.getInstance();
	}
}
