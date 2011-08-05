package org.eclipse.mylyn.modeling.papyrus;

import org.eclipse.mylyn.modeling.context.EMFStructureBridge;
import org.eclipse.mylyn.modeling.context.IModelStructureProvider;

public class UML2StructureBridge extends EMFStructureBridge {
	public IModelStructureProvider getDomainContextBridge() {
		return UML2DomainBridge.getInstance();
	};
}
