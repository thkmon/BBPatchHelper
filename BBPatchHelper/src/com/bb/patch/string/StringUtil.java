package com.bb.patch.string;

import java.util.ArrayList;

import com.bb.patch.file.data.StringList;

public class StringUtil {
	
	
	public static String parseStirng(Object obj) {
		if (obj == null) {
			return "";
		}
		return obj.toString();
	}
	
	
	public static String parseStirng(Object obj, String defaultStr) {
		if (obj == null) {
			return defaultStr;
		}
		
		String resultStr = obj.toString();
		if (resultStr.length() == 0) {
			return defaultStr;
		}
		
		return resultStr;
	}
	
	
	public static String replaceIgnoreCase(String org, String pre, String post) {

		if (org == null || org.length() == 0) {
			return org;
		}
		
		if (pre == null || pre.length() == 0) {
			return org;
		}
		
		if (post == null) {
			return org;
		}

		int orgLen = org.length();
		int preLen = pre.length();
		
		StringBuffer resBuffer = new StringBuffer();
		
		for (int i=0; i<orgLen; i++) {
			if (i+preLen > orgLen) {
				resBuffer.append(org.substring(i,i+1));
				continue;
			}
			
			if (org.substring(i, i+preLen).equalsIgnoreCase(pre)) {
				resBuffer.append(post);
				i = i+preLen-1;
				continue;
				
			} else {
				resBuffer.append(org.substring(i,i+1));
			}
		}
		
		if (resBuffer == null || resBuffer.toString().length() == 0) {
			return org;
			
		} else {
			return resBuffer.toString();			
		}
	}
	
	
	public static int indexOfIgnoreCase(String fullStr, String targetSlice) {
		
		if (fullStr == null || fullStr.length() == 0) {
			return -1;
		}
		
		if (targetSlice == null || targetSlice.length() == 0) {
			return -1;
		}

		int fullLen = fullStr.length();
		int targetLen = targetSlice.length();
		
		for (int i=0; i<fullLen; i++) {
			if (i+targetLen > fullLen) {
				break;
			}
			
			if (fullStr.substring(i, i+targetLen).equalsIgnoreCase(targetSlice)) {
				return i;
			}
		}
		
		return -1;
	}
	
	
	public static StringList splitMulti(String fullStr, String... delimeters) {
		
		StringList resList = new StringList();
		
		if (fullStr == null || fullStr.length() == 0) {
			return null;
		}
		
		if (delimeters == null) {
			System.out.println("splitMulti : delimeters are null");
			return null;
		}
		
		int deliCount = delimeters.length;
		if (deliCount < 1) {
			System.out.println("splitMulti : delimeters' count is 0");
			return null;
		}
		
		StringBuffer contentStack = new StringBuffer();
		
		int fullLen = fullStr.length();
		String oneDeli = "";
		
		boolean isDeli = false;

		for (int i=0; i<fullLen; i++) {
			isDeli = false;
			
			for (int k=0; k<deliCount; k++) {
				oneDeli = delimeters[k];
				if (oneDeli == null || oneDeli.length() == 0) {
					continue;
				}
				
				if (i+oneDeli.length() > fullLen) {
					continue;
				}
				
				if (fullStr.substring(i, i+oneDeli.length()).equals(oneDeli)) {
					resList.add(contentStack.toString());
					contentStack.delete(0, contentStack.length());
					
					// oneDeli 로 자른다.
					isDeli = true;
					break;
				}				
			}
			
			if (!isDeli) {
				contentStack.append(fullStr.substring(i, i+1));
			}
		}
		
		if (contentStack.length() > 0) {
			resList.add(contentStack.toString());
		}
		
		return resList;
	}
	
	
	public static void printList(ArrayList<String> list, boolean printIndexNum) {
		System.out.println(getStringList(list, printIndexNum));
	}
	
	
	public static String getStringList(ArrayList<String> list, boolean printIndexNum) {
		if (list == null) {
			return "list is null";
		}
		
		int size = list.size();
		
		if (size == 0) {
			return "list is empty";
		}
		
		StringBuffer result = new StringBuffer();
		
		String oneStr = "";
		for (int i=0; i<size; i++) {
			oneStr = list.get(i);
			
			if (printIndexNum) {
				result.append(i);
				result.append(" : ");
			}
			
			if (oneStr == null) {
				result.append("null");
			} else {
				result.append(oneStr);
			}
			
			result.append("\r\n");
		}
		
		return result.toString();
	}
	
	
//	/**
//	 * 어레이리스트에 스트링을 추가한다.(중복 아닐 경우만)
//	 * @param list
//	 * @param str
//	 */
//	public static void addToStringListNotDupl(ArrayList<String> list, String str) {
//		try {
//			
//			if (str == null) {
//				str = "";
//			}
//			
//			if (list == null) {
//				list = new ArrayList<String>();
//			}
//			
//			boolean isDupl = false;
//			int size = list.size();
//			
//			for (int i=0; i<size; i++) {
//				if (list.get(i) != null && list.get(i).equals(str)) {
//					isDupl = true;
//					break;
//				}
//			}
//			
//			if (!isDupl) {
//				list.add(str);
//			}
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	
	public static String revisePath(String path) {
		return makeToSlashPath(path);
	}
	
	
	public static String makeToSlashPath(String path) {
		if (path == null || path.trim().length() == 0) {
			return "";
			
		} else {
			path = path.trim();
		}
		
		path = path.replace("\\", "/");
		
		while (path.indexOf("//") > -1) {
			path = path.replace("//", "/");
		}
		
		return path;
	}
	
	
	/*
	public static int getIndex(String target, String axisStr, String findStr, int num, boolean bIgnoreCase) {
		if (target == null || target.length() == 0) {
			return -1;
		}
		
		if (axisStr == null || axisStr.length() == 0) {
			return -1;
		}
		
		if (bIgnoreCase) {
			target = target.toLowerCase();
			axisStr = axisStr.toLowerCase();
		}
		
		int axisIndex = -1;
		axisIndex = target.indexOf(axisStr);
		// axisStr 못 찾으면 무효
		if (axisIndex < 0) {
			return -1;
		}
		
		for (int i=0; i<num; i++) {
			axisIndex = target.indexOf(findStr, axisIndex);
			
			// 1번이라도 못찾으면 무효
			if (axisIndex < 0) {
				return -1;
			}
			
			if (i<(num-1)) {
				axisIndex = axisIndex + 1;
			}
		}
		
		return axisIndex;
	}
	*/
}