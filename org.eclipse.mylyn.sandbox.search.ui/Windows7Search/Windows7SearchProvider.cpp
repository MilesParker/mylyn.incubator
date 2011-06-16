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
#include "stdafx.h"
#include "Windows7Search.h"
#include "Windows7SearchProvider.h"

#import "C:/Program Files/Common Files/System/ado/msado15.dll" rename("EOF", "ADOEOF")

//void InvokeCallback(JNIEnv *env, jobject obj, jobject callback, std::string filename)
//{
//	jstring j_filename = env->NewStringUTF(filename.c_str());
//
//	jclass jc = env->GetObjectClass(obj);
//	jmethodID mid = env->GetMethodID(jc, "onSearchItemFound","(Lorg/eclipse/mylyn/internal/sandbox/search/ui/SearchCallback;Ljava/lang/String;)V");
//	env->CallObjectMethod(obj, mid, callback, j_filename);
//
//	// this crashes the JVM
//	// env->ReleaseStringUTFChars(j_filename, filename.c_str());
//}

void InvokeCallback(JNIEnv *env, jobject obj, jobject callback, std::string filename)
{
	jstring j_filename = env->NewStringUTF(filename.c_str());

	jclass fileClass = env->FindClass("java/io/File");
	jmethodID fileConstructor = env->GetMethodID(fileClass, "<init>", "(Ljava/lang/String;)V");
	jobject fileObj = env->NewObject(fileClass, fileConstructor, j_filename);

	jclass resultClass = env->FindClass("org/eclipse/mylyn/internal/sandbox/search/ui/SearchResultItem");
	jmethodID resultConstructor = env->GetMethodID(resultClass, "<init>", "(Ljava/io/File;)V");
	jobject resultObj = env->NewObject(resultClass, resultConstructor, fileObj);

	jclass jc = env->GetObjectClass(callback);
	jmethodID mid = env->GetMethodID(jc, "searchResult","(Lorg/eclipse/mylyn/internal/sandbox/search/ui/SearchResultItem;)V");
	env->CallObjectMethod(callback, mid, resultObj);
}

void PerformSearch(JNIEnv *env, jobject obj, jobject callback, std::string searchText)
{
	::CoInitialize(NULL);

	ADODB::_ConnectionPtr connection = NULL;
	HRESULT hr = connection.CreateInstance(__uuidof(ADODB::Connection));

	ADODB::_RecordsetPtr recordset = NULL;
	hr = recordset.CreateInstance(__uuidof(ADODB::Recordset));

	connection->CursorLocation = ADODB::adUseClient;
	connection->Open(L"Provider=Search.CollatorDSO;Extended Properties='Application=Windows';", L"", L"", ADODB::adConnectUnspecified);

	std::string query = "SELECT System.ItemPathDisplay from SystemIndex WHERE System.ItemName LIKE '%";
	query += searchText;
	query += "%'";

	recordset->Open(query.c_str(), connection.GetInterfacePtr(), ADODB::adOpenForwardOnly, ADODB::adLockReadOnly, ADODB::adCmdText);

	while(!recordset->ADOEOF)
	{
		_variant_t var = recordset->Fields->GetItem(L"System.ItemPathDisplay")->GetValue();
		// std::cout << static_cast<char *>(_bstr_t(var.bstrVal)) << std::endl;
		std::string filename = (const char*)_bstr_t(var.bstrVal);
		InvokeCallback(env, obj, callback, filename);

		recordset->MoveNext();
	};    

	recordset->Close();
	connection->Close();    
	::CoUninitialize();
}

JNIEXPORT void JNICALL Java_org_eclipse_mylyn_internal_sandbox_search_ui_Windows7SearchProvider_performNativeSearch (JNIEnv *env, jobject obj, jobject criteria, jobject callback)
{
	jclass jc = env->GetObjectClass(criteria);
	jmethodID mid = env->GetMethodID(jc, "getText","()Ljava/lang/String;");
	jstring result = (jstring)env->CallObjectMethod(criteria, mid);
	const char *searchText = env->GetStringUTFChars(result, 0);

	PerformSearch(env, obj, callback, searchText);
	env->ReleaseStringUTFChars(result, searchText);
}
