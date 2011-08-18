package org.eclipse.mylyn.modeling.ecoretools;

import org.eclipse.mylyn.modeling.context.IModelStructureProvider;
import org.eclipse.mylyn.modeling.gmf.GMFStructureBridge;

public class EcoreDiagramStructureBridge extends GMFStructureBridge {

	@Override
	public IModelStructureProvider getDomainContextBridge() {
		return EcoreDiagramDomainBridge.getInstance();
	}
}
