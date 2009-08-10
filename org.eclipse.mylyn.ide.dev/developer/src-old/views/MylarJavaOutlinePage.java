/*******************************************************************************
 * Copyright (c) 2004 - 2005 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/
/*
 * Created on Aug 6, 2004
  */
package org.eclipse.mylyn.java.ui.editor;

import java.util.*;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.*;
import org.eclipse.jdt.internal.ui.actions.AbstractToggleLinkingAction;
import org.eclipse.jdt.internal.ui.actions.CompositeActionGroup;
import org.eclipse.jdt.internal.ui.dnd.DelegatingDropAdapter;
import org.eclipse.jdt.internal.ui.dnd.JdtViewerDragAdapter;
import org.eclipse.jdt.internal.ui.javaeditor.*;
import org.eclipse.jdt.internal.ui.packageview.SelectionTransferDragAdapter;
import org.eclipse.jdt.internal.ui.packageview.SelectionTransferDropAdapter;
import org.eclipse.jdt.internal.ui.preferences.MembersOrderPreferenceCache;
import org.eclipse.jdt.internal.ui.viewsupport.*;
import org.eclipse.jdt.ui.*;
import org.eclipse.jdt.ui.ProblemsLabelDecorator.ProblemsLabelChangedEvent;
import org.eclipse.jdt.ui.actions.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.util.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchAdapter;
import org.eclipse.ui.part.*;
import org.eclipse.ui.texteditor.*;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

import org.eclipse.mylyn.core.ITaskscapeListener;
import org.eclipse.mylyn.core.MylarPlugin;
import org.eclipse.mylyn.core.model.ITaskscapeNode;
import org.eclipse.mylyn.java.ui.MylarAppearanceAwareLabelProvider;
import org.eclipse.mylyn.tasklist.MylarImages;
import org.eclipse.mylyn.tasklist.TaskListPlugin;

/**
 * @author Mik Kersten
 */
public class MylarJavaOutlinePage extends JavaOutlinePage implements IContentOutlinePage, IAdaptable , IPostSelectionProvider {

    protected ITaskscapeListener modelListener;
    protected JavaEditor fEditor;
    protected MylarAppearanceAwareLabelProvider mylarLabelProvider;
     
    private final ITaskscapeListener MODEL_LISTENER = new ITaskscapeListener() {
        public void interestChanged(ITaskscapeNode info) {
            refresh();
        }

        public void modelUpdated() {
            refresh();
        }
        
        public void presentationSettingsChanged(ITaskscapeListener.PresentationChangeKind kind) {
            refresh();
        } 
        
        public void nodeDeleted(ITaskscapeNode node) {
            refresh();
        }
        
        private void refresh() {
            if (fOutlineViewer != null) {
                PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                    public void run() {
                        try { 
                            if (fOutlineViewer != null && !fOutlineViewer.getTree().isDisposed()) {
                                fOutlineViewer.refresh();
                                fOutlineViewer.expandAll();
                            }
                        } catch (Throwable t) {
                            ContextCorePlugin.fail(t, "Could not update viewer", false);
                        }    
                    }
                });
            }
        }

        public void landmarkAdded(ITaskscapeNode element) { 
            refresh();
        }

        public void landmarkRemoved(ITaskscapeNode element) { 
            refresh();
        }

        public void relationshipsChanged() {
        }

        public void presentationSettingsChanging(ITaskscapeListener.PresentationChangeKind kind) {
            refresh();
        }
    };
    
    private FilterUniterestingAction filterUniterestingAction;
    
    public void extendControl(IActionBars actionBars) {
        ContextCorePlugin.getTaskscapeManager().addListener(MODEL_LISTENER);
        
        mylarLabelProvider = new MylarAppearanceAwareLabelProvider(
                AppearanceAwareLabelProvider.DEFAULT_TEXTFLAGS |  JavaElementLabels.F_APP_TYPE_SIGNATURE,
                AppearanceAwareLabelProvider.DEFAULT_IMAGEFLAGS,
                (ITreeContentProvider)fOutlineViewer.getContentProvider());  
        
        fOutlineViewer.getTree().setBackground(TaskListPlugin.getDefault().getColorMap().BACKGROUND_COLOR);
        fOutlineViewer.setLabelProvider(mylarLabelProvider);
//        fOutlineViewer.setLabelProvider(new MylarFontDecoratingJavaLabelProvider(mylarLabelProvider, true));

        IToolBarManager toolBarManager = actionBars.getToolBarManager();
        filterUniterestingAction = new FilterUniterestingAction();
        if (toolBarManager != null) {   
            toolBarManager.add(new Separator("StartMylar")); //$NON-NLS-1$
            toolBarManager.add(filterUniterestingAction);
        }
        if (filterUniterestingAction.isChecked() && mylarLabelProvider != null) 
            mylarLabelProvider.setInterestFilterEnabled(true);
    }
    
    class FilterUniterestingAction extends Action {
        
        public FilterUniterestingAction() {
            super();
            setText("Filter Uninteresting"); //$NON-NLS-1$
            setImageDescriptor(MylarImages.AUTO_EXPAND);    
            setToolTipText("Filter uninteresting elements"); //$NON-NLS-1$
            
            boolean checked= ContextCore.getPreferenceStore().getBoolean("org.eclipse.mylyn.ui.outline.filter.isChecked"); //$NON-NLS-1$
            valueChanged(checked, false);
        }
        
        public void run() {
            valueChanged(isChecked(), true);
        }
        
        private void valueChanged(final boolean on, boolean store) {
            setChecked(on);
            if (mylarLabelProvider != null) mylarLabelProvider.setInterestFilterEnabled(on);

            if (on) {
                fOutlineViewer.addFilter(MYLAR_OUTLINE_FILTER);
            } else {
                fOutlineViewer.removeFilter(MYLAR_OUTLINE_FILTER);
            }

            if (store)
                ContextCore.getPreferenceStore().setValue("org.eclipse.mylyn.ui.outline.filter.isChecked", on); //$NON-NLS-1$
        }
    }
    
    // TODO: extract and reuse
    static final ViewerFilter MYLAR_OUTLINE_FILTER = new ViewerFilter() {
        public boolean select(Viewer viewer, Object parentElement, Object object) {
            if (object instanceof IJavaElement) {
                IJavaElement element = (IJavaElement)object;
            
                ITaskscapeNode info = ContextCorePlugin.getTaskscapeManager().getDoi(element.getHandleIdentifier());
                return info != null && info.getDegreeOfInterest().getDegreeOfInterest().isInteresting();
            } else {
                return false;
            }
        }
    };
    
    // ---------------------------------------------------------------------
    // BELOW IS COPIED FROM JDT DUE TO LACK OF EXTENSIBILITY, AVOID CHANGING
//  public class JavaOutlinePage extends Page implements IContentOutlinePage, IAdaptable , IPostSelectionProvider {

            
    static Object[] NO_CHILDREN= new Object[0];
    
            /**
             * The element change listener of the java outline viewer.
             * @see IElementChangedListener
             */
            class ElementChangedListener implements IElementChangedListener {
                
                public void elementChanged(final ElementChangedEvent e) {
                    
                    if (getControl() == null)
                        return;
                        
                    Display d= getControl().getDisplay();
                    if (d != null) {
                        d.asyncExec(new Runnable() {
                            public void run() {
                                ICompilationUnit cu= (ICompilationUnit) fInput;
                                IJavaElement base= cu;
                                if (fTopLevelTypeOnly) {
                                    base= getMainType(cu);
                                    if (base == null) {
                                        if (fOutlineViewer != null)
                                            fOutlineViewer.refresh(true);
                                        return;
                                    }
                                }
                                IJavaElementDelta delta= findElement(base, e.getDelta());
                                if (delta != null && fOutlineViewer != null) {
                                    fOutlineViewer.reconcile(delta);
                                }
                            }
                        });
                    }
                }
                
                private boolean isPossibleStructuralChange(IJavaElementDelta cuDelta) {
                    if (cuDelta.getKind() != IJavaElementDelta.CHANGED) {
                        return true; // add or remove
                    }
                    int flags= cuDelta.getFlags();
                    if ((flags & IJavaElementDelta.F_CHILDREN) != 0) {
                        return true;
                    }
                    return (flags & (IJavaElementDelta.F_CONTENT | IJavaElementDelta.F_FINE_GRAINED)) == IJavaElementDelta.F_CONTENT;
                }
                
                protected IJavaElementDelta findElement(IJavaElement unit, IJavaElementDelta delta) {
                    
                    if (delta == null || unit == null)
                        return null;
                    
                    IJavaElement element= delta.getElement();
                    
                    if (unit.equals(element)) {
                        if (isPossibleStructuralChange(delta)) {
                            return delta;
                        }
                        return null;
                    }
                        
                    
                    if (element.getElementType() > IJavaElement.CLASS_FILE)
                        return null;
                        
                    IJavaElementDelta[] children= delta.getAffectedChildren();
                    if (children == null || children.length == 0)
                        return null;
                        
                    for (int i= 0; i < children.length; i++) {
                        IJavaElementDelta d= findElement(unit, children[i]);
                        if (d != null)
                            return d;
                    }
                    
                    return null;
                }
            }
          
            static class NoClassElement extends WorkbenchAdapter implements IAdaptable {
                /*
                 * @see java.lang.Object#toString()
                 */
                public String toString() {
                    return JavaEditorMessages.getString("JavaOutlinePage.error.NoTopLevelType"); //$NON-NLS-1$
                }
        
                /*
                 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
                 */
                public Object getAdapter(Class clas) {
                    if (clas == IWorkbenchAdapter.class)
                        return this;
                    return null;
                }
            }
            
            /**
             * Content provider for the children of an ICompilationUnit or
             * an IClassFile
             * @see ITreeContentProvider
             */
            class ChildrenProvider implements ITreeContentProvider {
             
                private Object[] NO_CLASS= new Object[] {new NoClassElement()};
                private ElementChangedListener fListener;
                
                protected boolean matches(IJavaElement element) {
                    if (element.getElementType() == IJavaElement.METHOD) {
                        String name= element.getElementName();
                        return (name != null && name.indexOf('<') >= 0);
                    }
                    return false;
                }
                
                protected IJavaElement[] filter(IJavaElement[] children) {
                    boolean initializers= false;
                    for (int i= 0; i < children.length; i++) {
                        if (matches(children[i])) {
                            initializers= true;
                            break;
                        }
                    }
                            
                    if (!initializers)
                        return children;
                        
                    Vector v= new Vector();
                    for (int i= 0; i < children.length; i++) {
                        if (matches(children[i]))
                            continue;
                        v.addElement(children[i]);
                    }
                    
                    IJavaElement[] result= new IJavaElement[v.size()];
                    v.copyInto(result);
                    return result;
                }
                
                public Object[] getChildren(Object parent) {
                    if (parent instanceof IParent) {
                        IParent c= (IParent) parent;
                        try {
                            return filter(c.getChildren());
                        } catch (JavaModelException x) {
                            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=38341
                            // don't log NotExist exceptions as this is a valid case
                            // since we might have been posted and the element
                            // removed in the meantime.
                            if (JavaPlugin.isDebug() || !x.isDoesNotExist())
                                JavaPlugin.log(x);
                        }
                    }
                    return NO_CHILDREN;
                }
                
                public Object[] getElements(Object parent) {
                    if (fTopLevelTypeOnly) {
                        if (parent instanceof ICompilationUnit) {
                            try {
                                IType type= getMainType((ICompilationUnit) parent);
                                return type != null ? type.getChildren() : NO_CLASS;
                            } catch (JavaModelException e) {
                                JavaPlugin.log(e);
                            }
                        } else if (parent instanceof IClassFile) {
                            try {
                                IType type= getMainType((IClassFile) parent);
                                return type != null ? type.getChildren() : NO_CLASS;
                            } catch (JavaModelException e) {
                                JavaPlugin.log(e);
                            }                           
                        }
                    }
                    return getChildren(parent);
                }
                
                public Object getParent(Object child) {
                    if (child instanceof IJavaElement) {
                        IJavaElement e= (IJavaElement) child;
                        return e.getParent();
                    }
                    return null;
                }
                
                public boolean hasChildren(Object parent) {
                    if (parent instanceof IParent) {
                        IParent c= (IParent) parent;
                        try {
                            IJavaElement[] children= filter(c.getChildren());
                            return (children != null && children.length > 0);
                        } catch (JavaModelException x) {
                            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=38341
                            // don't log NotExist exceptions as this is a valid case
                            // since we might have been posted and the element
                            // removed in the meantime.
                            if (JavaPlugin.isDebug() || !x.isDoesNotExist())
                                JavaPlugin.log(x);
                        }
                    }
                    return false;
                }
                
                public boolean isDeleted(Object o) {
                    return false;
                }
                
                public void dispose() {
                    if (fListener != null) {
                        JavaCore.removeElementChangedListener(fListener);
                        fListener= null;
                    }       
                }
                
                /*
                 * @see IContentProvider#inputChanged(Viewer, Object, Object)
                 */
                public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                    boolean isCU= (newInput instanceof ICompilationUnit);
                                    
                    if (isCU && fListener == null) {
                        fListener= new ElementChangedListener();
                        JavaCore.addElementChangedListener(fListener);
                    } else if (!isCU && fListener != null) {
                        JavaCore.removeElementChangedListener(fListener);
                        fListener= null;
                    }
                }
            }
            
            
            class JavaOutlineViewer extends TreeViewer {
                
                /**
                 * Indicates an item which has been reused. At the point of
                 * its reuse it has been expanded. This field is used to
                 * communicate between <code>internalExpandToLevel</code> and
                 * <code>reuseTreeItem</code>.
                 */
                private Item fReusedExpandedItem;
                private boolean fReorderedMembers;
                private boolean fForceFireSelectionChanged;
                
                public JavaOutlineViewer(Tree tree) {
                    super(tree);
                    setAutoExpandLevel(ALL_LEVELS);
                    setUseHashlookup(true);
                }
                
                /**
                 * Investigates the given element change event and if affected
                 * incrementally updates the Java outline.
                 * 
                 * @param delta the Java element delta used to reconcile the Java outline
                 */
                public void reconcile(IJavaElementDelta delta) {
                    fReorderedMembers= false;
                    fForceFireSelectionChanged= false;
                    if (getSorter() == null) {
                        if (fTopLevelTypeOnly
                            && delta.getElement() instanceof IType
                            && (delta.getKind() & IJavaElementDelta.ADDED) != 0)
                        {
                            refresh(true);

                        } else {
                            Widget w= findItem(fInput);
                            if (w != null && !w.isDisposed())
                                update(w, delta);
                            if (fForceFireSelectionChanged)
                                fireSelectionChanged(new SelectionChangedEvent(getSite().getSelectionProvider(), this.getSelection()));
                            if (fReorderedMembers) {
                                refresh(false);
                                fReorderedMembers= false;
                        }
                        }
                    } else {
                        // just for now
                        refresh(true);
                    }
                }
                
                /*
                 * @see TreeViewer#internalExpandToLevel
                 */
                protected void internalExpandToLevel(Widget node, int level) {
                    if (node instanceof Item) {
                        Item i= (Item) node;
                        if (i.getData() instanceof IJavaElement) {
                            IJavaElement je= (IJavaElement) i.getData();
                            if (je.getElementType() == IJavaElement.IMPORT_CONTAINER || isInnerType(je)) {
                                if (i != fReusedExpandedItem) {
                                    setExpanded(i, false);
                                    return;
                                }
                            }
                        }
                    }
                    super.internalExpandToLevel(node, level);
                }
                                
                protected void reuseTreeItem(Item item, Object element) {
                    
                    // remove children
                    Item[] c= getChildren(item);
                    if (c != null && c.length > 0) {
                        
                        if (getExpanded(item))
                            fReusedExpandedItem= item;
                        
                        for (int k= 0; k < c.length; k++) {
                            if (c[k].getData() != null)
                                disassociate(c[k]);
                            c[k].dispose();
                        }
                    }
                    
                    updateItem(item, element);
                    updatePlus(item, element);
                    internalExpandToLevel(item, ALL_LEVELS);
                    
                    fReusedExpandedItem= null;
                    fForceFireSelectionChanged= true;
                }
                
                protected boolean mustUpdateParent(IJavaElementDelta delta, IJavaElement element) {
                    if (element instanceof IMethod) {
                        if ((delta.getKind() & IJavaElementDelta.ADDED) != 0) {
                            try {
                                return ((IMethod)element).isMainMethod();
                            } catch (JavaModelException e) {
                                JavaPlugin.log(e.getStatus());
                            }
                        }
                        return "main".equals(element.getElementName()); //$NON-NLS-1$
                    }
                    return false;
                }
                
                /*
                 * @see org.eclipse.jface.viewers.AbstractTreeViewer#isExpandable(java.lang.Object)
                 */
                public boolean isExpandable(Object element) {
                    if (hasFilters()) {
                        return getFilteredChildren(element).length > 0;
                    }
                    return super.isExpandable(element);
                }
                
                protected ISourceRange getSourceRange(IJavaElement element) throws JavaModelException {
                    if (element instanceof ISourceReference)
                        return ((ISourceReference) element).getSourceRange();
                    if (element instanceof IMember && !(element instanceof IInitializer))
                        return ((IMember) element).getNameRange();
                    return null;
                }
                
                protected boolean overlaps(ISourceRange range, int start, int end) {
                    return start <= (range.getOffset() + range.getLength() - 1) && range.getOffset() <= end;
                }
                
                protected boolean filtered(IJavaElement parent, IJavaElement child) {
                    
                    Object[] result= new Object[] { child };
                    ViewerFilter[] filters= getFilters();
                    for (int i= 0; i < filters.length; i++) {
                        result= filters[i].filter(this, parent, result);
                        if (result.length == 0)
                            return true;
                    }
                    
                    return false;
                }
                
                protected void update(Widget w, IJavaElementDelta delta) {
                    
                    Item item;
                    
                    IJavaElement parent= delta.getElement();
                    IJavaElementDelta[] affected= delta.getAffectedChildren();
                    Item[] children= getChildren(w);

                    boolean doUpdateParent= false;
                    boolean doUpdateParentsPlus= false;
                                        
                    Vector deletions= new Vector();
                    Vector additions= new Vector();             

                    for (int i= 0; i < affected.length; i++) {
                        IJavaElementDelta affectedDelta= affected[i];
                        IJavaElement affectedElement= affectedDelta.getElement();
                        int status= affected[i].getKind();

                        // find tree item with affected element
                        int j;
                        for (j= 0; j < children.length; j++)
                            if (affectedElement.equals(children[j].getData()))
                                break;
                        
                        if (j == children.length) {
                            // remove from collapsed parent
                            if ((status & IJavaElementDelta.REMOVED) != 0) {
                                doUpdateParentsPlus= true;
                                continue;
                            }                           
                            // addition
                            if ((status & IJavaElementDelta.CHANGED) != 0 &&                            
                                (affectedDelta.getFlags() & IJavaElementDelta.F_MODIFIERS) != 0 &&
                                !filtered(parent, affectedElement))
                            {
                                additions.addElement(affectedDelta);
                            }
                            continue;
                        }

                        item= children[j];

                        // removed                          
                        if ((status & IJavaElementDelta.REMOVED) != 0) {
                            deletions.addElement(item);
                            doUpdateParent= doUpdateParent || mustUpdateParent(affectedDelta, affectedElement);

                        // changed                          
                        } else if ((status & IJavaElementDelta.CHANGED) != 0) {
                            int change= affectedDelta.getFlags();
                            doUpdateParent= doUpdateParent || mustUpdateParent(affectedDelta, affectedElement);
                            
                            if ((change & IJavaElementDelta.F_MODIFIERS) != 0) {
                                if (filtered(parent, affectedElement))
                                    deletions.addElement(item);
                                else
                                    updateItem(item, affectedElement);
                            }
                            
                            if ((change & IJavaElementDelta.F_CONTENT) != 0)
                                updateItem(item, affectedElement);
                                
                            if ((change & IJavaElementDelta.F_CHILDREN) != 0)
                                update(item, affectedDelta);                                                                
                            
                            if ((change & IJavaElementDelta.F_REORDER) != 0)
                                fReorderedMembers= true;
                        }
                    }
                    
                    // find all elements to add
                    IJavaElementDelta[] add= delta.getAddedChildren();
                    if (additions.size() > 0) {
                        IJavaElementDelta[] tmp= new IJavaElementDelta[add.length + additions.size()];
                        System.arraycopy(add, 0, tmp, 0, add.length);
                        for (int i= 0; i < additions.size(); i++)
                            tmp[i + add.length]= (IJavaElementDelta) additions.elementAt(i);
                        add= tmp;
                    }
                    
                    // add at the right position
                    go2: for (int i= 0; i < add.length; i++) {
                        
                        try {
                            
                            IJavaElement e= add[i].getElement();
                            if (filtered(parent, e))
                                continue go2;
                                
                            doUpdateParent= doUpdateParent || mustUpdateParent(add[i], e);
                            ISourceRange rng= getSourceRange(e);
                            int start= rng.getOffset();
                            int end= start + rng.getLength() - 1;
                            int nameOffset= Integer.MAX_VALUE;
                            if (e instanceof IField) {
                                ISourceRange nameRange= ((IField) e).getNameRange();
                                if (nameRange != null)
                                    nameOffset= nameRange.getOffset();
                            }
                            
                            Item last= null;
                            item= null;
                            children= getChildren(w);
                            
                            for (int j= 0; j < children.length; j++) {
                                item= children[j];
                                IJavaElement r= (IJavaElement) item.getData();
                                
                                if (r == null) {
                                    // parent node collapsed and not be opened before -> do nothing
                                    continue go2;
                                }
                                
                                    
                                try {
                                    rng= getSourceRange(r);

                                    // multi-field declarations always start at 
                                    // the same offset. They also have the same
                                    // end offset if the field sequence is terminated
                                    // with a semicolon. If not, the source range
                                    // ends behind the identifier / initializer
                                    // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=51851
                                    boolean multiFieldDeclaration= 
                                        r.getElementType() == IJavaElement.FIELD 
                                            && e.getElementType() == IJavaElement.FIELD
                                            && rng.getOffset() == start;

                                    // elements are inserted by occurrence
                                    // however, multi-field declarations have
                                    // equal source ranges offsets, therefore we
                                    // compare name-range offsets.
                                    boolean multiFieldOrderBefore= false;
                                    if (multiFieldDeclaration) {
                                        if (r instanceof IField) {
                                            ISourceRange nameRange= ((IField) r).getNameRange();
                                            if (nameRange != null) {
                                                if (nameRange.getOffset() > nameOffset)
                                                    multiFieldOrderBefore= true;
                                            }
                                        }
                                    }
                                    
                                    if (!multiFieldDeclaration && overlaps(rng, start, end)) {
                                        
                                        // be tolerant if the delta is not correct, or if 
                                        // the tree has been updated other than by a delta
                                        reuseTreeItem(item, e);
                                        continue go2;
                                        
                                    } else if (multiFieldOrderBefore || rng.getOffset() > start) {
                                        
                                        if (last != null && deletions.contains(last)) {
                                            // reuse item
                                            deletions.removeElement(last);
                                            reuseTreeItem(last, e);
                                        } else {
                                            // nothing to reuse
                                            createTreeItem(w, e, j);
                                        }
                                        continue go2;
                                    }
                                    
                                } catch (JavaModelException x) {
                                    // stumbled over deleted element
                                }
                                
                                last= item;
                            }
                        
                            // add at the end of the list
                            if (last != null && deletions.contains(last)) {
                                // reuse item
                                deletions.removeElement(last);
                                reuseTreeItem(last, e);
                            } else {
                                // nothing to reuse
                                createTreeItem(w, e, -1);
                            }
                        
                        } catch (JavaModelException x) {
                            // the element to be added is not present -> don't add it
                        }
                    }
                    
                    
                    // remove items which haven't been reused
                    Enumeration e= deletions.elements();
                    while (e.hasMoreElements()) {
                        item= (Item) e.nextElement();
                        disassociate(item);
                        item.dispose();
                    }
                    
                    if (doUpdateParent)
                        updateItem(w, delta.getElement());
                    if (!doUpdateParent && doUpdateParentsPlus && w instanceof Item)
                        updatePlus((Item)w, delta.getElement());
                }
                

                                
                /*
                 * @see ContentViewer#handleLabelProviderChanged(LabelProviderChangedEvent)
                 */
                protected void handleLabelProviderChanged(LabelProviderChangedEvent event) {
                    Object input= getInput();
                    if (event instanceof ProblemsLabelChangedEvent) {
                        ProblemsLabelChangedEvent e= (ProblemsLabelChangedEvent) event;
                        if (e.isMarkerChange() && input instanceof ICompilationUnit) {
                            return; // marker changes can be ignored
                        }
                    }
                    // look if the underlying resource changed
                    Object[] changed= event.getElements();
                    if (changed != null) {
                        IResource resource= getUnderlyingResource();
                        if (resource != null) {
                            for (int i= 0; i < changed.length; i++) {
                                if (changed[i] != null && changed[i].equals(resource)) {
                                    // change event to a full refresh
                                    event= new LabelProviderChangedEvent((IBaseLabelProvider) event.getSource());
                                    break;
                                }
                            }
                        }
                    }
                    super.handleLabelProviderChanged(event);
                }
                
                private IResource getUnderlyingResource() {
                    Object input= getInput();
                    if (input instanceof ICompilationUnit) {
                        ICompilationUnit cu= (ICompilationUnit) input;
                        cu= JavaModelUtil.toOriginal(cu);
                        return cu.getResource();        
                    } else if (input instanceof IClassFile) {
                        return ((IClassFile) input).getResource();
                    }
                    return null;
                }               
                

            }
                
            class LexicalSortingAction extends Action {
                
                private JavaElementSorter fSorter= new JavaElementSorter();         

                public LexicalSortingAction() {
                    super();
                    PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.LEXICAL_SORTING_OUTLINE_ACTION);
                    setText(JavaEditorMessages.getString("JavaOutlinePage.Sort.label")); //$NON-NLS-1$
                    JavaPluginImages.setLocalImageDescriptors(this, "alphab_sort_co.gif"); //$NON-NLS-1$
                    setToolTipText(JavaEditorMessages.getString("JavaOutlinePage.Sort.tooltip")); //$NON-NLS-1$
                    setDescription(JavaEditorMessages.getString("JavaOutlinePage.Sort.description")); //$NON-NLS-1$
                    
                    boolean checked= JavaPlugin.getDefault().getPreferenceStore().getBoolean("LexicalSortingAction.isChecked"); //$NON-NLS-1$
                    valueChanged(checked, false);
                }
                
                public void run() {
                    valueChanged(isChecked(), true);
                }
                
                private void valueChanged(final boolean on, boolean store) {
                    setChecked(on);
                    BusyIndicator.showWhile(fOutlineViewer.getControl().getDisplay(), new Runnable() {
                        public void run() {
                            fOutlineViewer.setSorter(on ? fSorter : null);                      }
                    });

                    if (store)
                        JavaPlugin.getDefault().getPreferenceStore().setValue("LexicalSortingAction.isChecked", on); //$NON-NLS-1$
                }
            }

        class ClassOnlyAction extends Action {

            public ClassOnlyAction() {
                super();
                PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.GO_INTO_TOP_LEVEL_TYPE_ACTION);
                setText(JavaEditorMessages.getString("JavaOutlinePage.GoIntoTopLevelType.label")); //$NON-NLS-1$
                setToolTipText(JavaEditorMessages.getString("JavaOutlinePage.GoIntoTopLevelType.tooltip")); //$NON-NLS-1$
                setDescription(JavaEditorMessages.getString("JavaOutlinePage.GoIntoTopLevelType.description")); //$NON-NLS-1$
                JavaPluginImages.setLocalImageDescriptors(this, "gointo_toplevel_type.gif"); //$NON-NLS-1$

                IPreferenceStore preferenceStore= JavaPlugin.getDefault().getPreferenceStore();
                boolean showclass= preferenceStore.getBoolean("GoIntoTopLevelTypeAction.isChecked"); //$NON-NLS-1$
                setTopLevelTypeOnly(showclass);
            }

            /*
             * @see org.eclipse.jface.action.Action#run()
             */
            public void run() {
                setTopLevelTypeOnly(!fTopLevelTypeOnly);
            }

            private void setTopLevelTypeOnly(boolean show) {
                fTopLevelTypeOnly= show;
                setChecked(show);
                fOutlineViewer.refresh(false);
                
                IPreferenceStore preferenceStore= JavaPlugin.getDefault().getPreferenceStore(); 
                preferenceStore.setValue("GoIntoTopLevelTypeAction.isChecked", show); //$NON-NLS-1$
            }
        }

        /**
         * This action toggles whether this Java Outline page links
         * its selection to the active editor.
         * 
         * @since 3.0
         */
        public class ToggleLinkingAction extends AbstractToggleLinkingAction {
        
            JavaOutlinePage fJavaOutlinePage;
        
            /**
             * Constructs a new action.
             * 
             * @param outlinePage the Java outline page
             */
            public ToggleLinkingAction(JavaOutlinePage outlinePage) {
                boolean isLinkingEnabled= PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_SYNC_OUTLINE_ON_CURSOR_MOVE);
                setChecked(isLinkingEnabled);
                fJavaOutlinePage= outlinePage;
            }
    
            /**
             * Runs the action.
             */
            public void run() {
                PreferenceConstants.getPreferenceStore().setValue(PreferenceConstants.EDITOR_SYNC_OUTLINE_ON_CURSOR_MOVE, isChecked());

                // MOD: start
                if (fEditor instanceof MylarClassFileEditor) {
                    if (isChecked() && fEditor != null)
                        ((MylarClassFileEditor)fEditor).synchronizeOutlinePage(((MylarClassFileEditor)fEditor).computeHighlightRangeSourceReference(), false);
                } else if (fEditor instanceof MylarCompilationUnitEditor) {
                    if (isChecked() && fEditor != null)
                        ((MylarCompilationUnitEditor)fEditor).synchronizeOutlinePage(((MylarCompilationUnitEditor)fEditor).computeHighlightRangeSourceReference(), false);
                }
                // MOD: end
            }
    
        }


    /** A flag to show contents of top level type only */
    private boolean fTopLevelTypeOnly;
            
    private IJavaElement fInput;
    private String fContextMenuID;
    private Menu fMenu;
    private JavaOutlineViewer fOutlineViewer;
//    private JavaEditor fEditor;
    
    private MemberFilterActionGroup fMemberFilterActionGroup;
        
    private ListenerList fSelectionChangedListeners= new ListenerList();
    private ListenerList fPostSelectionChangedListeners= new ListenerList();
    private Hashtable fActions= new Hashtable();
    
    private TogglePresentationAction fTogglePresentation;
    private GotoAnnotationAction fPreviousAnnotation;
    private GotoAnnotationAction fNextAnnotation;
    private TextEditorAction fShowJavadoc;
    private IAction fUndo;
    private IAction fRedo;
    
    private ToggleLinkingAction fToggleLinkingAction;
    
    private CompositeActionGroup fActionGroups;

    private IPropertyChangeListener fPropertyChangeListener;
    /**
     * Custom filter action group.
     * @since 3.0
     */
    private CustomFiltersActionGroup fCustomFiltersActionGroup;
    
    public MylarJavaOutlinePage(String contextMenuID, JavaEditor editor) {
        super(contextMenuID, editor);  // TODO: wierd overriding of consturction
        
        Assert.isNotNull(editor);
        
        fContextMenuID= contextMenuID;
        fEditor= editor;
        
        fTogglePresentation= new TogglePresentationAction();
        fPreviousAnnotation= new GotoAnnotationAction("PreviousAnnotation.", false); //$NON-NLS-1$
        fNextAnnotation= new GotoAnnotationAction("NextAnnotation.", true); //$NON-NLS-1$
        fShowJavadoc= (TextEditorAction) fEditor.getAction("ShowJavaDoc"); //$NON-NLS-1$
        fUndo= fEditor.getAction(ITextEditorActionConstants.UNDO);
        fRedo= fEditor.getAction(ITextEditorActionConstants.REDO);
        
        fTogglePresentation.setEditor(editor);
        fPreviousAnnotation.setEditor(editor);
        fNextAnnotation.setEditor(editor);  
        
        fPropertyChangeListener= new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                doPropertyChange(event);
            }
        };
        JavaPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(fPropertyChangeListener);
    }
    
    /**
     * Returns the primary type of a compilation unit (has the same
     * name as the compilation unit).
     * 
     * @param compilationUnit the compilation unit
     * @return returns the primary type of the compilation unit, or
     * <code>null</code> if is does not have one
     */
    protected IType getMainType(ICompilationUnit compilationUnit) {
        
        if (compilationUnit == null)
            return null;
        
        String name= compilationUnit.getElementName();
        int index= name.indexOf('.');
        if (index != -1)
            name= name.substring(0, index);
        IType type= compilationUnit.getType(name);
        return type.exists() ? type : null;
    }

    /**
     * Returns the primary type of a class file.
     * 
     * @param classFile the class file
     * @return returns the primary type of the class file, or <code>null</code>
     * if is does not have one
     */
    protected IType getMainType(IClassFile classFile) {
        try {
            IType type= classFile.getType();
            return type != null && type.exists() ? type : null;
        } catch (JavaModelException e) {
            return null;    
        }
    }
    
    /* (non-Javadoc)
     * Method declared on Page
     */
    public void init(IPageSite pageSite) {
        super.init(pageSite);
    }
    
    private void doPropertyChange(PropertyChangeEvent event) {
        if (fOutlineViewer != null) {
            if (MembersOrderPreferenceCache.isMemberOrderProperty(event.getProperty())) {
                fOutlineViewer.refresh(false);
            }
        }
    }   
    
    /*
     * @see ISelectionProvider#addSelectionChangedListener(ISelectionChangedListener)
     */
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        if (fOutlineViewer != null)
            fOutlineViewer.addSelectionChangedListener(listener);
        else
            fSelectionChangedListeners.add(listener);
    }
    
    /*
     * @see ISelectionProvider#removeSelectionChangedListener(ISelectionChangedListener)
     */
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        if (fOutlineViewer != null)
            fOutlineViewer.removeSelectionChangedListener(listener);
        else
            fSelectionChangedListeners.remove(listener);
    }
    
    /*
     * @see ISelectionProvider#setSelection(ISelection)
     */
    public void setSelection(ISelection selection) {
        if (fOutlineViewer != null)
            fOutlineViewer.setSelection(selection);     
    }   
    
    /*
     * @see ISelectionProvider#getSelection()
     */
    public ISelection getSelection() {
        if (fOutlineViewer == null)
            return StructuredSelection.EMPTY;
        return fOutlineViewer.getSelection();
    }
    
    /*
     * @see org.eclipse.jface.text.IPostSelectionProvider#addPostSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
     */
    public void addPostSelectionChangedListener(ISelectionChangedListener listener) {
        if (fOutlineViewer != null)
            fOutlineViewer.addPostSelectionChangedListener(listener);
        else
            fPostSelectionChangedListeners.add(listener);
    }
    
    /*
     * @see org.eclipse.jface.text.IPostSelectionProvider#removePostSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
     */
    public void removePostSelectionChangedListener(ISelectionChangedListener listener) {
        if (fOutlineViewer != null)
            fOutlineViewer.removePostSelectionChangedListener(listener);
        else
            fPostSelectionChangedListeners.remove(listener);    
    }
    
    private void registerToolbarActions(IActionBars actionBars) {
        
        IToolBarManager toolBarManager= actionBars.getToolBarManager();
        if (toolBarManager != null) {   
            toolBarManager.add(new LexicalSortingAction());
            
            fMemberFilterActionGroup= new MemberFilterActionGroup(fOutlineViewer, "org.eclipse.jdt.ui.JavaOutlinePage"); //$NON-NLS-1$
            fMemberFilterActionGroup.contributeToToolBar(toolBarManager);

            fCustomFiltersActionGroup.fillActionBars(actionBars);
            
            IMenuManager menu= actionBars.getMenuManager();
            menu.add(new Separator("EndFilterGroup")); //$NON-NLS-1$
            
            fToggleLinkingAction= new ToggleLinkingAction(this);
            menu.add(new ClassOnlyAction());        
            menu.add(fToggleLinkingAction);
        }
    }
    
    /*
     * @see IPage#createControl
     */
    public void createControl(Composite parent) {
        
        Tree tree= new Tree(parent, SWT.MULTI);

        AppearanceAwareLabelProvider lprovider= new AppearanceAwareLabelProvider(
            AppearanceAwareLabelProvider.DEFAULT_TEXTFLAGS |  JavaElementLabels.F_APP_TYPE_SIGNATURE,
            AppearanceAwareLabelProvider.DEFAULT_IMAGEFLAGS
        );

        fOutlineViewer= new JavaOutlineViewer(tree);        
        initDragAndDrop();
        fOutlineViewer.setContentProvider(new ChildrenProvider());
        fOutlineViewer.setLabelProvider(new DecoratingJavaLabelProvider(lprovider));
        
        Object[] listeners= fSelectionChangedListeners.getListeners();
        for (int i= 0; i < listeners.length; i++) {
            fSelectionChangedListeners.remove(listeners[i]);
            fOutlineViewer.addSelectionChangedListener((ISelectionChangedListener) listeners[i]);
        }
        
        listeners= fPostSelectionChangedListeners.getListeners();
        for (int i= 0; i < listeners.length; i++) {
            fPostSelectionChangedListeners.remove(listeners[i]);
            fOutlineViewer.addPostSelectionChangedListener((ISelectionChangedListener) listeners[i]);
        }
                        
        MenuManager manager= new MenuManager(fContextMenuID, fContextMenuID);
        manager.setRemoveAllWhenShown(true);
        manager.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager m) {
                contextMenuAboutToShow(m);
            }
        });
        fMenu= manager.createContextMenu(tree);
        tree.setMenu(fMenu);
        
        IPageSite site= getSite();
        site.registerContextMenu(JavaPlugin.getPluginId() + ".outline", manager, fOutlineViewer); //$NON-NLS-1$
        site.setSelectionProvider(fOutlineViewer);

        // we must create the groups after we have set the selection provider to the site
        fActionGroups= new CompositeActionGroup(new ActionGroup[] {
                new OpenViewActionGroup(this), 
                new CCPActionGroup(this),
                new GenerateActionGroup(this),
                new RefactorActionGroup(this), 
                new JavaSearchActionGroup(this)});
                
        // register global actions
        IActionBars bars= site.getActionBars();
        
        bars.setGlobalActionHandler(ITextEditorActionConstants.UNDO, fUndo);
        bars.setGlobalActionHandler(ITextEditorActionConstants.REDO, fRedo);
        bars.setGlobalActionHandler(ITextEditorActionConstants.PREVIOUS, fPreviousAnnotation);
        bars.setGlobalActionHandler(ITextEditorActionConstants.NEXT, fNextAnnotation);
        bars.setGlobalActionHandler(JdtActionConstants.SHOW_JAVA_DOC, fShowJavadoc);
        bars.setGlobalActionHandler(ITextEditorActionDefinitionIds.TOGGLE_SHOW_SELECTED_ELEMENT_ONLY, fTogglePresentation);
        bars.setGlobalActionHandler(ITextEditorActionDefinitionIds.GOTO_NEXT_ANNOTATION, fNextAnnotation);
        bars.setGlobalActionHandler(ITextEditorActionDefinitionIds.GOTO_PREVIOUS_ANNOTATION, fPreviousAnnotation);
        
        
        fActionGroups.fillActionBars(bars);

        IStatusLineManager statusLineManager= bars.getStatusLineManager();
        if (statusLineManager != null) {
            StatusBarUpdater updater= new StatusBarUpdater(statusLineManager);
            fOutlineViewer.addPostSelectionChangedListener(updater);
        }
        // Custom filter group
        fCustomFiltersActionGroup= new CustomFiltersActionGroup("org.eclipse.jdt.ui.JavaOutlinePage", fOutlineViewer); //$NON-NLS-1$

        registerToolbarActions(bars);
                
        fOutlineViewer.setInput(fInput);    
    }

    public void dispose() {
        
        if (fEditor == null)
            return;
            
        if (fMemberFilterActionGroup != null) {
            fMemberFilterActionGroup.dispose();
            fMemberFilterActionGroup= null;
        }
        
        if (fCustomFiltersActionGroup != null) {
            fCustomFiltersActionGroup.dispose();
            fCustomFiltersActionGroup= null;
        }
            
            
        fEditor.outlinePageClosed();
        fEditor= null;

        fSelectionChangedListeners.clear();
        fSelectionChangedListeners= null;
        
        fPostSelectionChangedListeners.clear();
        fPostSelectionChangedListeners= null;

        if (fPropertyChangeListener != null) {
            JavaPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(fPropertyChangeListener);
            fPropertyChangeListener= null;
        }
        
        if (fMenu != null && !fMenu.isDisposed()) {
            fMenu.dispose();
            fMenu= null;
        }
        
        if (fActionGroups != null)
            fActionGroups.dispose();
            
        fTogglePresentation.setEditor(null);
        fPreviousAnnotation.setEditor(null);
        fNextAnnotation.setEditor(null);    
        
        fOutlineViewer= null;
        
        super.dispose();
    }
    
    public Control getControl() {
        if (fOutlineViewer != null)
            return fOutlineViewer.getControl();
        return null;
    }
    
    public void setInput(IJavaElement inputElement) {
        fInput= inputElement;   
        if (fOutlineViewer != null)
            fOutlineViewer.setInput(fInput);
    }
        
    public void select(ISourceReference reference) {
        if (fOutlineViewer != null) {
            
            ISelection s= fOutlineViewer.getSelection();
            if (s instanceof IStructuredSelection) {
                IStructuredSelection ss= (IStructuredSelection) s;
                List elements= ss.toList();
                if (!elements.contains(reference)) {
                    s= (reference == null ? StructuredSelection.EMPTY : new StructuredSelection(reference));
                    fOutlineViewer.setSelection(s, true);
                }
            }
        }
    }
    
    public void setAction(String actionID, IAction action) {
        Assert.isNotNull(actionID);
        if (action == null)
            fActions.remove(actionID);
        else
            fActions.put(actionID, action);
    }
    
    public IAction getAction(String actionID) {
        Assert.isNotNull(actionID);
        return (IAction) fActions.get(actionID);
    }

    /*
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class key) {
        if (key == IShowInSource.class) {
            return getShowInSource();
        }
        if (key == IShowInTargetList.class) {
            return new IShowInTargetList() {
                public String[] getShowInTargetIds() {
                    return new String[] { JavaUI.ID_PACKAGES };
                }

            };
        }
        if (key == IShowInTarget.class) {
            return getShowInTarget();
        }

        return null;
    }

    /**
     * Convenience method to add the action installed under the given actionID to the
     * specified group of the menu.
     * 
     * @param menu      the menu manager
     * @param group     the group to which to add the action
     * @param actionID  the ID of the new action
     */
    protected void addAction(IMenuManager menu, String group, String actionID) {
        IAction action= getAction(actionID);
        if (action != null) {
            if (action instanceof IUpdate)
                ((IUpdate) action).update();
                
            if (action.isEnabled()) {
                IMenuManager subMenu= menu.findMenuUsingPath(group);
                if (subMenu != null)
                    subMenu.add(action);
                else
                    menu.appendToGroup(group, action);
            }
        }
    }
     
    protected void contextMenuAboutToShow(IMenuManager menu) {
        
        JavaPlugin.createStandardGroups(menu);
                
        IStructuredSelection selection= (IStructuredSelection)getSelection();
        fActionGroups.setContext(new ActionContext(selection));
        fActionGroups.fillContextMenu(menu);
    }
    
    /*
     * @see Page#setFocus()
     */
    public void setFocus() {
        if (fOutlineViewer != null)
            fOutlineViewer.getControl().setFocus();
    }
    
    /**
     * Checks whether a given Java element is an inner type.
     * 
     * @param element the java element
     * @return <code>true</code> iff the given element is an inner type
     */
    private boolean isInnerType(IJavaElement element) {
        
        if (element != null && element.getElementType() == IJavaElement.TYPE) {
            IType type= (IType)element;
            try {
                return type.isMember();
            } catch (JavaModelException e) {
                IJavaElement parent= type.getParent();
                if (parent != null) {
                    int parentElementType= parent.getElementType();
                    return (parentElementType != IJavaElement.COMPILATION_UNIT && parentElementType != IJavaElement.CLASS_FILE);
                }
            }
        }
        
        return false;       
    }
    
    /**
     * Returns the <code>IShowInSource</code> for this view.
     * 
     * @return the {@link IShowInSource}
     */
    protected IShowInSource getShowInSource() {
        return new IShowInSource() {
            public ShowInContext getShowInContext() {
                return new ShowInContext(
                    null,
                    getSite().getSelectionProvider().getSelection());
            }
        };
    }

    /**
     * Returns the <code>IShowInTarget</code> for this view.
     * 
     * @return the {@link IShowInTarget}
     */
    protected IShowInTarget getShowInTarget() {
        return new IShowInTarget() {
            public boolean show(ShowInContext context) {
                ISelection sel= context.getSelection();
                if (sel instanceof ITextSelection) {
                    ITextSelection tsel= (ITextSelection) sel;
                    int offset= tsel.getOffset();
                    
                    
                    // MOD: start
                    IJavaElement element= null; 
                    if (fEditor instanceof MylarClassFileEditor) {
                            element = ((MylarClassFileEditor)fEditor).getElementAt(offset);
                    } else if (fEditor instanceof MylarCompilationUnitEditor) {
                            element = ((MylarCompilationUnitEditor)fEditor).getElementAt(offset);
                    }
                    // MOD: end
                    
                    
                    if (element != null) {
                        setSelection(new StructuredSelection(element));
                        return true;
                    }
                }
                return false;
            }
        };
    }
    
    private void initDragAndDrop() {
        int ops= DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
        Transfer[] transfers= new Transfer[] {
            LocalSelectionTransfer.getInstance()
            };
        
        // Drop Adapter
        TransferDropTargetListener[] dropListeners= new TransferDropTargetListener[] {
            new SelectionTransferDropAdapter(fOutlineViewer)
        };
        fOutlineViewer.addDropSupport(ops | DND.DROP_DEFAULT, transfers, new DelegatingDropAdapter(dropListeners));
        
        // Drag Adapter
        TransferDragSourceListener[] dragListeners= new TransferDragSourceListener[] {
            new SelectionTransferDragAdapter(fOutlineViewer)
        };
        fOutlineViewer.addDragSupport(ops, transfers, new JdtViewerDragAdapter(fOutlineViewer, dragListeners));
    }
 }
