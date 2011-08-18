package org.eclipse.mylyn.modeling.papyrus;

import org.eclipse.mylyn.modeling.context.IModelStructureProvider;
import org.eclipse.mylyn.modeling.gmf.GMFStructureBridge;

public class UML2StructureBridge extends GMFStructureBridge {
	public IModelStructureProvider getDomainContextBridge() {
		return UML2DomainBridge.getInstance();
	};
}
