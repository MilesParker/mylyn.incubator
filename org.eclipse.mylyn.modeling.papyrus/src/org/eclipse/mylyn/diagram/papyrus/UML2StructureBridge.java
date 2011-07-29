package org.eclipse.mylyn.diagram.papyrus;

import org.eclipse.mylyn.emf.context.EMFStructureBridge;
import org.eclipse.mylyn.emf.context.IDomainContextBridge;

public class UML2StructureBridge extends EMFStructureBridge {
	public IDomainContextBridge getDomainContextBridge() {
		return UML2DomainBridge.getInstance();
	};
}
