package com.bb.patch.file;

import java.io.File;

import com.bb.patch.file.data.StringList;


/**
 * 파일 수집기.
 *
 */
public class FileCollector {

	
	public static StringList getFileList(String motherDirPath) {
		if (motherDirPath == null || motherDirPath.length() == 0) {
			return null;
		}
		
		File dir = new File(motherDirPath);
		if (!dir.exists()) {
			return null;
		}
		
		StringList resultList = new StringList();
		addFileList(resultList, dir);
		return resultList;
	}
	
	
	private static void addFileList(StringList resultList, File file) {
		if (!file.exists()) {
			return;
		}
		
		if (file.isFile()) {
			resultList.add(file.getAbsolutePath());
			return;
		}
		
		if (file.isDirectory()) {
			File[] fileList = file.listFiles();
			if (fileList != null && fileList.length > 0) {
				int fileCount = fileList.length;
				for (int i=0; i<fileCount; i++) {
					if (fileList[i] == null) {
						continue;
					}
					
					if (!fileList[i].exists()) {
						continue;
					}
					
					if (fileList[i].isFile()) {
						resultList.add(fileList[i].getAbsolutePath());
						
					} else if (fileList[i].isDirectory()) {
						addFileList(resultList, fileList[i]);
					}
				}
			}
		}
		
	}
}
