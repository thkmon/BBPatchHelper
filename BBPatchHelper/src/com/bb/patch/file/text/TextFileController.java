package com.bb.patch.file.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import com.bb.patch.file.data.StringList;

public class TextFileController {
	
	public StringList readTextFile(String textFilePath) {
		
		StringList resultList = null;
		
		BufferedReader reader = null;
		File file = null;
		
		try {
			file = new File(textFilePath);
			if (!file.exists()) {
				return null;
			}
			
			if (!file.isFile()) {
				return null;
			}
			
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			
			resultList = new StringList();
			
			String line = "";
			while((line = reader.readLine()) != null) {
				resultList.add(line);
			}

			// 객체 닫기
			reader.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
			
		} finally {
			// 객체 닫기
			closeBufferedReader(reader);
			file = null;
		}
		
		return resultList;
	}
	
	
	private void closeBufferedReader(BufferedReader reader) {
		try {
			if (reader != null) {
				reader.close();
			}
		} catch (Exception e) {
			// 무시
		} finally {
			reader = null;
		}
	}
}
