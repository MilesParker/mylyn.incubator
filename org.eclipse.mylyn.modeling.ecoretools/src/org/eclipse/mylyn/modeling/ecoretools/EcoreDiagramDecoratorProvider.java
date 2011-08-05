package org.eclipse.mylyn.modeling.ecoretools;

import org.eclipse.mylyn.modeling.gmf.MylynDecoratorProvider;
import org.eclipse.mylyn.modeling.ui.IModelUIProvider;



public class EcoreDiagramDecoratorProvider extends MylynDecoratorProvider {

	@Override
	public IModelUIProvider getDomainUIBridge() {
		return EcoreDiagramDomainBridge.getInstance();
	}

}
