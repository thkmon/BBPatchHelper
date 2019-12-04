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
}