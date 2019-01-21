package com.bb.patch.common;

import java.awt.Color;

import com.bb.patch.file.data.StringMap;
import com.bb.patch.file.text.PropFileController;
import com.bb.patch.string.StringUtil;


public class CConst {
	
	// 프로그램 버전
	public static String version = "190121";
	
	private static final StringMap optionPropFile = new PropFileController().readPropFile("option.properties");
	public static String getOption(String keyText, String defaultStr) {
		if (optionPropFile == null) {
			return defaultStr;
		}
		return StringUtil.parseStirng(optionPropFile.get(keyText), defaultStr);
	}
	
	// targetDir == "C:\\NANUM\\workspaces\\SmartFlowOSEWork\\SmartFlowOSE3.6WORK";
	// targetDir == "C:\\NANUM\\workspaces\\프로젝트명\\폴더명\\";
	public static String targetDir = getOption("TARGET_DIR", "");
	
	public static String classDir = getOption("CLASS_DIR", "");
	
	public static boolean bJavaToClass = getOption("JAVA_TO_CLASS", "1").equals("1");
	
	public static String forbiddenFile = getOption("FORBIDDEN_FILE", "*.xml, *.conf, *.config, *.properties");
	
	public static String destDir = getOption("RESULT_DIR", "C:/0_patch");
	
	
	//	연노란색
	//	public static Color buttonColor = new Color(255, 255, 200);
	
	//	청록색에 가까운 하늘색.
	//	public static Color buttonColor = new Color(200, 230, 230);
	
	//	연노란색. (171029)
	// public static Color buttonColor = new Color(255, 255, 200);
	
	// 회색. (181005)
	// public static Color buttonColor = new Color(200, 200, 200);
	
	// 연한군청색. (190121)
	public static Color buttonColor = new Color(200, 200, 255);
	public static Color buttonTextColor = new Color(0, 0, 0);

	public static int winWidth = 600;
	public static int winHeight = 520;
	
	public static int errLogWidth = 600;
	public static int errLogHeight = 520;
	
	public static Color formBackgroundColor = new Color(230, 230, 230);
	
	
	// 171029 (원주 가기 전날 토요일)
	// 1. 버튼 색깔 다시 되돌림 (하늘색 -> 노란색)
	// 2. 결과폴더 날짜에 따라 자동생성 되도록 수정.
	// 3. AUTO 버튼에 중복제거 기능 추가.
	
	
	// 180516(수) 목표
	// 1. 디폴트 설정 ini(txt 파일)에 저장 => option.properties 생성함.

	
	// 180525(금)
	// 1. SQL 파일 금지 (option.properties에 반영함)
	// 2. 특정확장자 변경 -> 패턴으로 변경
	
	
	// TO_DO_LIST
	// 1. 리사이즈에 따라 인풋박스 등 모양 변경
	// 2. (클래스 위치가 정해지지 않은 경우) 패키지가 없는 경우 java 로 컴파일되는 버그 수정해야 함.
	// 3. 파일 똑바로 세기
	// 4. 결과 경로 보기좋게 나오도록 만들기
	// 5. 폴더 복사 기능 (폴더 허용 기능) 추가하고 싶음
	
	// 181002(화)
	// 1. 리사이즈에 따라 인풋박스 등 모양 변경 : 아직 처리하지 못함.
	// 2. (클래스 위치가 정해지지 않은 경우) 패키지가 없는 경우 java 로 컴파일되는 버그 수정해야 함. : 테스트 결과 이상없음.
	// 3. 파일 똑바로 세기 : 처리불가. 의미를 알 수 없음.
	// 4. 결과 경로 보기좋게 나오도록 만들기
	// 5. 폴더 복사 기능 (폴더 허용 기능) 추가하고 싶음
}