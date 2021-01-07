package com.bb.patch.string;

public class PathUtil {

	public static boolean checkIsFolderPath(String str) {
		if (str == null || str.length() == 0) {
			return false;
		}
		
		str = str.trim();
		
		// 점이 있으면 폴더가 아니다.
		if (str.indexOf(".") > -1) {
			return false;
		}
		
		String oneChar = "";
		
		int len = str.length();
		for (int i=0; i<len; i++) {
			oneChar = str.substring(i, i+1);
			
			// 영어, 콜론, 슬래시, 역슬래시 제외하고 발견되면 폴더가 아니다. (공백도 허용않는다.)
			if (!oneChar.matches("[a-zA-Z]") && !oneChar.equals(":") && !oneChar.equals("/") && !oneChar.equals("\\")) {
				return false;
			}
		}
		
		return true;
	}
	
	
	public static boolean checkContainKorean(String str) {
		if (str == null || str.length() == 0) {
			return false;
		}
		
		String oneChar = "";
		
		int len = str.length();
		for (int i=0; i<len; i++) {
			oneChar = str.substring(i, i+1);
			
			if (oneChar.matches("[가-힣]")) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean checkContainBlank(String str) {
		if (str == null || str.length() == 0) {
			return false;
		}
		
		String oneChar = "";
		
		int len = str.length();
		for (int i=0; i<len; i++) {
			oneChar = str.substring(i, i+1);
			
			if (oneChar.equals(" ")) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 파일 확장자 소문자 형태로 가져오기
	 * 
	 * @param path
	 * @return
	 */
	public static String getLowerExtension(String path) {
		if (path == null || path.length() == 0) {
			return "";
		}
		
		int lastDotIndex = path.lastIndexOf(".");
		if (lastDotIndex > -1) {
			return path.substring(lastDotIndex + 1).toLowerCase();
		}
		
		return "";
	}
	
	
	/**
	 * 파일 패스 보정. 역슬래시를 슬래시로 변경한다.
	 * 
	 * @param path
	 * @return
	 */
	public static String revisePath(String path) {
		if (path == null || path.length() == 0) {
			return "";
		}
		
		// 역슬래시를 슬래시로 변경
		while (path.indexOf("\\") > -1) {
			path = path.replace("\\", "/");
		}
		
		// 연속된 슬래시를 슬래시 1개로 변경
		while (path.indexOf("//") > -1) {
			path = path.replace("//", "/");
		}
		
		return path.trim();
	}
	
	
	public static String makeEndPath(String path) {
		
		path = StringUtil.makeToSlashPath(path);
		
		int slashIdx = getIndexOfWorkspaceFolderSlash(path);
		if (slashIdx > -1) {
			path = path.substring(slashIdx + 1);
		}
		
		if (StringUtil.indexOfIgnoreCase(path, "c:/") > -1) {
			path = StringUtil.replaceIgnoreCase(path, "c:/", "");
		}
		
		if (path.indexOf(":") > -1) {
			// 경로에 콜론이 존재할 경우 제거
			path = path.replace(":", "");
		}
		
		return path;
	}


	public static int getIndexOfWorkspaceFolderSlash(String path) {
		
		path = StringUtil.makeToSlashPath(path);
		
		int folderIdx = StringUtil.indexOfIgnoreCase(path, "/workspaces/");
		if (folderIdx > -1) {
			int newIndex = path.indexOf("/", folderIdx + 12);
			if (newIndex > -1) {
				int resultIndex = path.indexOf("/", newIndex + 1);
				if (resultIndex > -1) {
					return resultIndex;
				}
			}
			
		}
		
		folderIdx = StringUtil.indexOfIgnoreCase(path, "/workspace/");
		if (folderIdx > -1) {
			int newIndex = path.indexOf("/", folderIdx + 11);
			if (newIndex > -1) {
				int resultIndex = path.indexOf("/", newIndex + 1);
				if (resultIndex > -1) {
					return resultIndex;
				}
			}
		}
		
		folderIdx = StringUtil.indexOfIgnoreCase(path, "/gits/");
		if (folderIdx > -1) {
			int newIndex = path.indexOf("/", folderIdx + 6);
			if (newIndex > -1) {
				int resultIndex = path.indexOf("/", newIndex + 1);
				if (resultIndex > -1) {
					return resultIndex;
				}
			}
		}
		
		folderIdx = StringUtil.indexOfIgnoreCase(path, "/git/");
		if (folderIdx > -1) {
			int newIndex = path.indexOf("/", folderIdx + 5);
			if (newIndex > -1) {
				int resultIndex = path.indexOf("/", newIndex + 1);
				if (resultIndex > -1) {
					return resultIndex;
				}
			}
		}
		
		return -1;
	}
}