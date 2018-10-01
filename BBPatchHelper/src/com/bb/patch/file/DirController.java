package com.bb.patch.file;

import java.io.File;
import java.util.ArrayList;

public class DirController {
	
	public ArrayList<String> getPathListInDir(String dirPath) throws Exception {
		
		try {
			ArrayList<String> resultList = new ArrayList<String>();
			getPathListInDir(resultList, dirPath);
			return resultList;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	protected void getPathListInDir(ArrayList<String> resultList, String dirPath) throws Exception {
		
		if (dirPath == null || dirPath.trim().length() == 0) {
			throw new Exception("dirPath is null or empty");
		}
		
		File dir = new File(dirPath);
		
		if (!dir.exists()) {
			throw new Exception("'" + dirPath + "' is not exist");
		}
		
		if (!dir.isDirectory()) {
			throw new Exception("'" + dirPath + "' is not directory");
		}
		
		if (dir.list() == null) {
			return;
		}
		
		int count = dir.list().length;
		
		if (count < 1) {
			return;
		}
		
		String onePath = "";
		for (int i=0; i<count; i++) {
			onePath = dir.getAbsolutePath() + "\\" + dir.list()[i];
			
			if (onePath == null || onePath.length() == 0) {
				continue;
			}
			
			File file = new File(onePath);
			
			if (!file.exists()) {
				continue;
			}
			
			if (file.isFile()) {
				onePath = onePath.replace("\\", "/");
				// System.out.println(onePath);
				resultList.add(onePath);
				continue;
				
			} else if (file.isDirectory()) {
				getPathListInDir(resultList, onePath);
			}
		}
	}
}
