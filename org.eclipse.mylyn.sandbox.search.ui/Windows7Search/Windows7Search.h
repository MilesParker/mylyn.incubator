/*******************************************************************************
 * Copyright (c) 2011 Tasktop Technologies.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/
// The following ifdef block is the standard way of creating macros which make exporting 
// from a DLL simpler. All files within this DLL are compiled with the WINDOWS7SEARCH_EXPORTS
// symbol defined on the command line. This symbol should not be defined on any project
// that uses this DLL. This way any other project whose source files include this file see 
// WINDOWS7SEARCH_API functions as being imported from a DLL, whereas this DLL sees symbols
// defined with this macro as being exported.
#ifdef WINDOWS7SEARCH_EXPORTS
#define WINDOWS7SEARCH_API __declspec(dllexport)
#else
#define WINDOWS7SEARCH_API __declspec(dllimport)
#endif

// This class is exported from the Windows7Search.dll
class WINDOWS7SEARCH_API CWindows7Search {
public:
	CWindows7Search(void);
	// TODO: add your methods here.
};

extern WINDOWS7SEARCH_API int nWindows7Search;

WINDOWS7SEARCH_API int fnWindows7Search(void);
