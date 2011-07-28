package org.eclipse.mylyn.gmf.ui;

import org.eclipse.mylyn.emf.context.EcoreDiagramBridge;

public class EcoreDiagramDecoratorProvider extends MylynDecoratorProvider {

	@Override
	public String getContentType() {
		return EcoreDiagramBridge.ECORE_CONTENT_TYPE;
	}

}
