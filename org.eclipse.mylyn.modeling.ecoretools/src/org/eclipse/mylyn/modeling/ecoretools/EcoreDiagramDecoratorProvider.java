package org.eclipse.mylyn.modeling.ecoretools;

import org.eclipse.mylyn.gmf.ui.MylynDecoratorProvider;



public class EcoreDiagramDecoratorProvider extends MylynDecoratorProvider {

	@Override
	public String getContentType() {
		return EcoreDiagramDomainBridge.ECORE_CONTENT_TYPE;
	}

}
