package com.bb.patch.file.data;

import java.util.ArrayList;
import java.util.HashMap;


public class UniqueStringList extends ArrayList<String> {
	
	
	HashMap<String, Integer> map = new HashMap<String, Integer>();
	
	
	public boolean add(String str) {
		if (str == null || str.trim().length() == 0) {
			return false;
		} else {
			str = str.trim();
		}
		
		if (map.get(str) == null) {
			map.put(str, 1);
			super.add(str);
			return true;
		}
		
		return false;
	}

}
