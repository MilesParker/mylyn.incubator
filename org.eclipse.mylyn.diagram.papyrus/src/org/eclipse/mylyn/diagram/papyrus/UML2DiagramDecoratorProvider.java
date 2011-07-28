package org.eclipse.mylyn.diagram.papyrus;

import org.eclipse.mylyn.gmf.ui.MylynDecoratorProvider;


public class UML2DiagramDecoratorProvider extends MylynDecoratorProvider {

	@Override
	public String getContentType() {
		return UML2DiagramBridge.UML2_CONTENT_TYPE;
	}

}
