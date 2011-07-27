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

#include <sstream>
#include <list>


#import "C:/Program Files/Common Files/System/ado/msado15.dll" rename("EOF", "ADOEOF")

void InvokeCallback(JNIEnv *env, jobject obj, jobject callback, std::wstring filename)
{
	jstring j_filename = env->NewString((const jchar *)filename.c_str(), filename.length());

	jclass fileClass = env->FindClass("java/io/File");
	jmethodID fileConstructor = env->GetMethodID(fileClass, "<init>", "(Ljava/lang/String;)V");
	jobject fileObj = env->NewObject(fileClass, fileConstructor, j_filename);

	jclass resultClass = env->FindClass("org/eclipse/mylyn/sandbox/search/ui/SearchResult");
	jmethodID resultConstructor = env->GetMethodID(resultClass, "<init>", "(Ljava/io/File;)V");
	jobject resultObj = env->NewObject(resultClass, resultConstructor, fileObj);

	jclass jc = env->GetObjectClass(callback);
	jmethodID mid = env->GetMethodID(jc, "searchResult","(Lorg/eclipse/mylyn/sandbox/search/ui/SearchResult;)V");
	env->CallObjectMethod(callback, mid, resultObj);
}

bool IsProgressMonitorCancelled(JNIEnv *env,jobject monitor) {
	jclass monitorClass = env->FindClass("org/eclipse/core/runtime/IProgressMonitor");
	jmethodID isCanceledMethod = env->GetMethodID(monitorClass,"isCanceled","()Z");

	jboolean result = env->CallBooleanMethod(monitor,isCanceledMethod);
	return result == JNI_TRUE;
}

std::wstring ReplaceCharWithString(std::wstring source, const wchar_t replaceChar, std::wstring replaceString) 
{ 
	size_t posn = source.find(&replaceChar);
	while (posn != std::wstring::npos)
	{
		source.replace(posn, 1, replaceString);
		posn = source.find(&replaceChar, posn + replaceString.length());
	}
    return source; 
}

std::wstring IntToString(int value) {
	std::wstringstream sstream;
	sstream << value;
	return sstream.str();
}


void PerformSearch(JNIEnv *env, jobject obj, jobject callback, std::wstring searchText, std::list<std::wstring>& patterns,int maximumResults, jobject monitor)
{

	ADODB::_ConnectionPtr connection = NULL;
	ADODB::_RecordsetPtr recordset = NULL;
	try {
		HRESULT hr = ::CoInitialize(NULL);
		if (!SUCCEEDED(hr))
			return;

		hr = connection.CreateInstance(__uuidof(ADODB::Connection));
		if (!SUCCEEDED(hr))
			return;

		hr = recordset.CreateInstance(__uuidof(ADODB::Recordset));
		if (!SUCCEEDED(hr))
			return;

		connection->CursorLocation = ADODB::adUseClient;
		hr = connection->Open(L"Provider=Search.CollatorDSO;Extended Properties='Application=Windows';", L"", L"", ADODB::adConnectUnspecified);
		if (!SUCCEEDED(hr))
			return;

		
		searchText = ReplaceCharWithString(searchText, '\'', L"''");

		std::wstring filenameMatcher = searchText;
		filenameMatcher = ReplaceCharWithString(filenameMatcher, '%', L"\\%");
		filenameMatcher = ReplaceCharWithString(filenameMatcher, '*', L"%");
		
		// see FREETEXT http://msdn.microsoft.com/en-us/library/bb231268(v=vs.85).aspx
		// see CONTAINS http://msdn.microsoft.com/en-us/library/bb231270(v=vs.85).aspx

		std::wstring query = L"SELECT TOP ";
		query += IntToString(maximumResults);
		query += L" System.ItemPathDisplay from SystemIndex WHERE ";
		// limit to documents, pictures and video
		query += L"(System.Kind = 'document' or System.Kind = 'picture' or System.Kind = 'video' or System.Kind is null) AND ";
		// content search
		query += L"(FREETEXT('\"";
		query += searchText;
		query += L"\"') OR ";
		// filename search
		query += L"(System.ItemName LIKE '%";
		query += filenameMatcher;
		query += L"%'))";

		if (patterns.size() > 0) {
			bool hasFilenameSearch = true;
			std::wstring anyFile(L"*");

			for(std::list<std::wstring>::iterator iterator = patterns.begin(); iterator != patterns.end(); iterator++) {
				std::wstring pattern = *iterator;
				if (pattern == anyFile) {
					hasFilenameSearch = false;
					break;
				}
			}
			if (hasFilenameSearch) {
				query += L" AND (";
				for(std::list<std::wstring>::iterator iterator = patterns.begin(); iterator != patterns.end(); iterator++) {
					std::wstring pattern = *iterator;
					if (pattern.size() == 0) {
						continue;
					}
					pattern = ReplaceCharWithString(pattern, '*', L"%");
					if (iterator != patterns.begin()) {
						query += L" OR ";
					}
					query += L"(System.ItemName LIKE '";
					if (pattern[0] != '%') {
						query += L"%";
					}
					query += pattern;
					query += L"')";
				}
				query += L")";
			}
		}
		
		//std::cout << "Query:\n";
		//std::cout << query;
		//std::cout << "\n";
		//std::cout << std::flush;
		
		hr = recordset->Open(query.c_str(), connection.GetInterfacePtr(), ADODB::adOpenForwardOnly, ADODB::adLockReadOnly, ADODB::adCmdText);
		if (!SUCCEEDED(hr)) {
			std::cout << "Open Failed\n" << std::flush;
			return;
		}

		int count = maximumResults;
		
		
		while(!recordset->ADOEOF)
		{
			_variant_t var = recordset->Fields->GetItem(L"System.ItemPathDisplay")->GetValue();
			std::wstring filename = (const wchar_t*)_bstr_t(var.bstrVal);
			InvokeCallback(env, obj, callback, filename);

			if (IsProgressMonitorCancelled(env,monitor)) {
				break;
			}

			if (--count < 0) {
				break;
			}

			hr = recordset->MoveNext();
			if (!SUCCEEDED(hr)) {
				std::cout << "Move Next Failed\n" << std::flush;
				break;
			}
		}
	} catch (_com_error &e)	{
		_tprintf(_T("\tCOM Error code = %08lx\n"), e.Error());
	}


	if (recordset != NULL && recordset->State == ADODB::adStateOpen)
		recordset->Close();
	if (connection != NULL && connection->State == ADODB::adStateOpen)
		connection->Close();   

	::CoUninitialize();
}

std::wstring JStringToWString (JNIEnv *env, jstring javaString)
{
	const jchar *javaChars = env->GetStringChars(javaString, NULL);
	jsize length = env->GetStringLength(javaString);
	std::wstring wideString((wchar_t *)javaChars, length);
	env->ReleaseStringChars(javaString, javaChars);
	return wideString;
}

JNIEXPORT void JNICALL Java_org_eclipse_mylyn_internal_sandbox_search_ui_windows_WindowsSearchProvider_performNativeSearch (JNIEnv *env, jobject obj, jobject criteria, jobject callback, jobject monitor)
{
	
	jclass jc = env->GetObjectClass(criteria);
	jmethodID mid = env->GetMethodID(jc, "getText","()Ljava/lang/String;");
	jstring jSearchText = (jstring)env->CallObjectMethod(criteria, mid);
	std::wstring searchText = JStringToWString(env, jSearchText);

	jmethodID getPatternId = env->GetMethodID(jc, "getFilenamePatterns","()[Ljava/lang/String;");
	jobjectArray patternArray = (jobjectArray)env->CallObjectMethod(criteria, getPatternId);
	unsigned int patternCount = env->GetArrayLength(patternArray);
	

	// FIXME FIXME FIXME
	jmethodID getMaximumResultsId = env->GetMethodID(jc,"getMaximumResults","()I");
	jint maximumResults = (jint) env->CallIntMethod(criteria,getMaximumResultsId);
	

	std::list<std::wstring> patterns;
	for (unsigned int i=0; i<patternCount; i++)	{
		jstring jPattern = (jstring)env->GetObjectArrayElement(patternArray, i);
		std::wstring pattern = JStringToWString(env, jPattern);
		patterns.push_back(pattern);
	}

	PerformSearch(env, obj, callback, searchText, patterns,(int) maximumResults, monitor);
}
