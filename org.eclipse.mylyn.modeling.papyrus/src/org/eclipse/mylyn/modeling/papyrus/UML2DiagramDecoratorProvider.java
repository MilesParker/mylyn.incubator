package org.eclipse.mylyn.modeling.papyrus;

import org.eclipse.mylyn.modeling.gmf.MylynDecoratorProvider;
import org.eclipse.mylyn.modeling.ui.IModelUIProvider;


public class UML2DiagramDecoratorProvider extends MylynDecoratorProvider {

	@Override
	public IModelUIProvider getDomainUIBridge() {
		return UML2DomainBridge.getInstance();
	}

}
