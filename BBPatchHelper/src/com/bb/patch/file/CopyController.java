package com.bb.patch.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;


public class CopyController {
	
	
	public static boolean copyFileOrDirByPath(String originFilePath, String newFilePath, ArrayList<String> whitePatternList, ArrayList<String> blackPatternList) throws Exception {
		return copyFileOrDir(new File(originFilePath), new File(newFilePath), whitePatternList, blackPatternList);
	}
	
	
	public static boolean copyFileOrDir(File originFile, File newFile, ArrayList<String> whitePatternList, ArrayList<String> blackPatternList) throws Exception {
		
		if (!originFile.exists()) {
			throw new Exception("File not exists : " + originFile.getAbsolutePath());
		}
		
		if (originFile.isDirectory()) {
			return copyDirCore(originFile, newFile, whitePatternList, blackPatternList);
		} else {
			return copyFileCore(originFile, newFile, whitePatternList, blackPatternList);
		}
	}
	
	
	private static boolean checkValidPathByWhitePatternList(String targetPath, ArrayList<String> patternList) {
		if (targetPath == null || targetPath.trim().length() == 0) {
			return false;
		} else {
			targetPath = targetPath.trim();
			targetPath = revisePath(targetPath);
		}
		
		if (patternList == null || patternList.size() == 0) {
			return true;
		}
		
		int count = patternList.size();
		
		String pattern = null;
		for (int i=0; i<count; i++) {
			pattern = patternList.get(i);
			if (pattern == null || pattern.length() == 0) {
				continue;
			}
			
			if (targetPath.matches(pattern)) {
				return true;
			}
		}
		
		return false;
	}
	
	
	private static boolean checkValidPathByBlackPatternList(String targetPath, ArrayList<String> patternList) {
		if (targetPath == null || targetPath.trim().length() == 0) {
			return false;
		} else {
			targetPath = targetPath.trim();
			targetPath = revisePath(targetPath);
		}
		
		if (patternList == null || patternList.size() == 0) {
			return true;
		}
		
		int count = patternList.size();
		
		String pattern = null;
		for (int i=0; i<count; i++) {
			pattern = patternList.get(i);
			if (pattern == null || pattern.length() == 0) {
				continue;
			}
			
			if (targetPath.matches(pattern)) {
				return false;
			}
		}
		
		return true;
	}
	
	
	private static boolean copyDirCore(File originDir, File newDir, ArrayList<String> whitePatternList, ArrayList<String> blackPatternList) throws Exception {
		
		// 패턴 검사
		if (whitePatternList != null && whitePatternList.size() > 0) {
			boolean invalidWhitePattern = checkValidPathByWhitePatternList(newDir.getPath(), whitePatternList);
			if (!invalidWhitePattern) {
				System.err.println("copyDirCore invalidWhitePattern : " + newDir.getPath());
				return false;
			}
		}
		
		// 패턴 검사
		if (blackPatternList != null && blackPatternList.size() > 0) {
			boolean invalidBlackPattern = checkValidPathByBlackPatternList(newDir.getPath(), blackPatternList);
			if (!invalidBlackPattern) {
				System.err.println("copyDirCore invalidBlackPattern : " + newDir.getPath());
				return false;
			}
		}
		
		
		if (!newDir.exists()) {
			newDir.mkdirs();
			System.out.println("copyDirCore mkdirs : " + newDir.getAbsolutePath());
		}
		
		boolean success = true;
		boolean oneSuccess = false;
		
		String[] fileList = originDir.list();
		if (fileList != null && fileList.length > 0) {
			int fileCount = fileList.length;
			String fileNameAndExt = "";
			for (int i=0; i<fileCount; i++) {
				fileNameAndExt = fileList[i];
				if (fileNameAndExt == null || fileNameAndExt.length() == 0) {
					continue;
				}
				
				String originPath = revisePath(originDir.getAbsolutePath() + "\\" + fileNameAndExt);
				String newPath = revisePath(newDir.getAbsolutePath() + "\\" + fileNameAndExt);
				File orginFileObj = new File(originPath);
				File newFileObj = new File(newPath);
				
				// 패턴 검사
				if (whitePatternList != null && whitePatternList.size() > 0) {
					boolean invalidWhitePattern = checkValidPathByWhitePatternList(originPath, whitePatternList);
					if (!invalidWhitePattern) {
						System.err.println("copyDirCore invalidWhitePattern : " + originPath);
						continue;
					}
				}
				
				// 패턴 검사
				if (blackPatternList != null && blackPatternList.size() > 0) {
					boolean invalidBlackPattern = checkValidPathByBlackPatternList(originPath, blackPatternList);
					if (!invalidBlackPattern) {
						System.err.println("copyDirCore invalidBlackPattern : " + originPath);
						continue;
					}
				}
				
				if (!orginFileObj.exists()) {
					throw new Exception("copyDirCore : File not exists : " + orginFileObj.getAbsolutePath());
				}
				
				if (orginFileObj.isDirectory()) {
					oneSuccess = copyDirCore(orginFileObj, newFileObj, whitePatternList, blackPatternList);
					if (!oneSuccess) {
						success = false;
					}
					
				} else {
					oneSuccess = copyFileCore(orginFileObj, newFileObj, whitePatternList, blackPatternList);
					if (!oneSuccess) {
						success = false;
					}
				}
				
			}
		}
		
		return success;
	}
	
	
	private static boolean copyFileCore(File originFile, File newFile, ArrayList<String> whitePatternList, ArrayList<String> blackPatternList) throws Exception {
		
		FileInputStream inputStream = null;
		FileOutputStream outputStream = null;
		FileChannel fcin = null;
		FileChannel fcout = null;
		
		try {
			String originPath = revisePath(originFile.getAbsolutePath());
			if (!originFile.exists()) {
				throw new Exception("copyFileCore : File not exists : " + originPath);
			}
			
			// 패턴 검사
			if (whitePatternList != null && whitePatternList.size() > 0) {
				boolean invalidWhitePattern = checkValidPathByWhitePatternList(originPath, whitePatternList);
				if (!invalidWhitePattern) {
					System.err.println("copyDirCore invalidWhitePattern : " + originPath);
					return false;
				}
			}
			
			// 패턴 검사
			if (blackPatternList != null && blackPatternList.size() > 0) {
				boolean invalidBlackPattern = checkValidPathByBlackPatternList(originPath, blackPatternList);
				if (!invalidBlackPattern) {
					System.err.println("copyDirCore invalidBlackPattern : " + originPath);
					return false;
				}
			}
			
			boolean mkdir = makeParentDir(newFile.getAbsolutePath());
			
			if (mkdir) {
				inputStream = new FileInputStream(originFile);         
				outputStream = new FileOutputStream(newFile);
				     
				fcin =  inputStream.getChannel();
				fcout = outputStream.getChannel();
				     
				long size = fcin.size();
				fcin.transferTo(0, size, fcout);
				
				System.out.println("copyFileCore transferTo : " + newFile.getAbsolutePath());
				return true;
			}

		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {
			close(inputStream);
			close(outputStream);
			close(fcin);
			close(fcout);
		}
		
		return false;
	}
	
	
	private static void close(FileInputStream inputStream) {
		try {
			if (inputStream != null) {
				inputStream.close();
			}
		} catch (Exception e) {
			// 무시
		} finally {
			inputStream = null;
		}
	}
	
	
	private static void close(FileOutputStream outputStream) {
		try {
			if (outputStream != null) {
				outputStream.close();
			}
		} catch (Exception e) {
			// 무시
		} finally {
			outputStream = null;
		}
	}
	
	
	private static void close(FileChannel channel) {
		try {
			if (channel != null) {
				channel.close();
			}
		} catch (Exception e) {
			// 무시
		} finally {
			channel = null;
		}
	}
	
	
	/**
	  * 특정 파일패스의 부모 폴더가 없을 경우 만든다.
	  * 
	  * @param filePath
	  * @return
	  */
	private static boolean makeParentDir(String filePath) {

		if (filePath == null || filePath.trim().length() == 0) {
			System.err.println("makeParentDir : filePath == null || filePath.length() == 0");
			return false;

		} else {
			filePath = filePath.trim();
		}

		if (filePath.indexOf("/") > -1) {
			filePath = filePath.replace("/", "\\");
		}

		while (filePath.indexOf("\\\\") > -1) {
			filePath = filePath.replace("\\\\", "\\");
		}

		// 필요한 디렉토리 만들기
		int lastSlashPos = filePath.lastIndexOf("\\");

		if (lastSlashPos > -1) {
			File d = new File(filePath.substring(0, lastSlashPos));
			if (!d.exists()) {
				d.mkdirs();
			}

		} else {
			System.err.println("makeParentDir : lastSlashPos not exists");
			return false;
		}

		return true;
	}
	
	
	private static String revisePath(String path) {
		if (path == null) {
			return "";
		}
		
		path = path.trim();
		
		path = path.replace("\\", "/");
		while(path.indexOf("//") > -1) {
			path = path.replace("//", "/");
		}
		
		return path;
	}
}
