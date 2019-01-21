package com.bb.patch.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;

import com.bb.patch.common.CConst;
import com.bb.patch.exception.MsgException;
import com.bb.patch.file.FileCollector;
import com.bb.patch.file.FileController;
import com.bb.patch.file.data.StringList;
import com.bb.patch.file.data.UniqueStringList;
import com.bb.patch.form.AlterForm;
import com.bb.patch.form.PatchForm;
import com.bb.patch.string.StringUtil;

public class MainController {
	private StringBuffer logBuffer = null;
	
	public void executePatch() {
		
		try {
			copyPathFileToCurrentSpace();
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String revisePath(String path) {
		if (path == null) {
			return "";
		}
		
		path = path.trim();
		
		while (path.indexOf("\\") > -1) {
			path = path.replace("\\", "/");
		}
		
		while (path.indexOf("//") > -1) {
			path = path.replace("//", "/");
		}
		
		return path;
	}
	
	/**
	 * 스트링 리스트에서 의미있는 파일 경로만 골라내서 담는다. 중복 제거.
	 * 
	 * @param oldPathList
	 * @return
	 */
	public ArrayList<String> getListMeaningfulFilePathOnly(ArrayList<String> oldPathList, String targetFolderText) {
		
		// 보정
		targetFolderText = revisePath(targetFolderText);
		
		
		if (oldPathList == null || oldPathList.size() == 0) {
			printErrLog("입력한 내용이 없습니다.");
			return null;
		}
		
		int oldCount = oldPathList.size();
		
		// 중복제거용 해시맵
		HashMap<String, String> mapToAvoidDupl = new HashMap<String, String>();
		
		
		/**
		 * 보정
		 */
		ArrayList<String> resultList = new ArrayList<String>();
		
		// 공백, 첫글자 샵(주석), 파일이 아닌 경우 제외한다.
		// 보정.
		String oneInputPath = null;
		
		File file = null;
		
		for(int i=0; i<oldCount;i++) {
			oneInputPath = oldPathList.get(i);
			
			oneInputPath = revisePath(oneInputPath);
			if (oneInputPath.length() == 0) {
				continue;
			}
			
			if (oneInputPath.startsWith("#")) {
				// 첫 글자 샵은 주석으로 인식
				continue;
			}
			
			
			if (targetFolderText.length() > 0) {
				
				if (oneInputPath.startsWith(targetFolderText)) {
					file = new File(oneInputPath);
				} else {
					oneInputPath = revisePath(targetFolderText + "/" + oneInputPath);
					file = new File(oneInputPath);
				}
				
			} else {
				file = new File(oneInputPath);
			}
			
			
			if (!file.exists()) {
				printErrLog("파일이 존재하지 않으므로 skip합니다. : " + oneInputPath);
				continue;
			}
			
			if (!file.isFile()) {
				printErrLog("디렉토리이므로 skip합니다. : " + oneInputPath);
				continue;
			}
			
			if (mapToAvoidDupl.get(oneInputPath) != null) {
				printErrLog("이미 추가한 경로이므로 skip합니다. : " + oneInputPath);
				continue;
			}
			
			resultList.add(oneInputPath);
			mapToAvoidDupl.put(oneInputPath, "1");
		}
		
		return resultList;
	}
	
	
	// AUTO 버튼 클릭시 수행
	public void autoButtonClicked() {
		
		// 대상파일 인풋박스 보정
		String targetFolderText = PatchForm.targetFolderText.getText();
		if (targetFolderText != null && targetFolderText.length() > 0) {
			targetFolderText = revisePath(targetFolderText);
			
			int slashIdx = StringUtil.getIndexOfWorkspaceFolderSlash(targetFolderText);
			if (slashIdx > -1) {
				targetFolderText = targetFolderText.substring(0, slashIdx);
			}
			
			PatchForm.targetFolderText.setText(targetFolderText);
		}

		
		String inputText = PatchForm.targetPathList.getText();
		
		if (inputText == null || inputText.trim().length() == 0) {
			return;
			
		} else {
			inputText = inputText.trim();
		}
		
		// 엔터값으로 split
		ArrayList<String> inputList = StringUtil.splitMulti(inputText, "\r\n", "\r", "\n", ";");
		
		if (inputList == null || inputList.size() == 0) {
			return;
		}
		
//		Vector resultPathVector = new Vector();
		HashMap mapToCheckDupl = new HashMap();
		StringBuffer resultPathBuffer = new StringBuffer();
		
		String oneInput = null;
		int count = inputList.size();
		for (int i=0; i<count; i++) {
			oneInput = inputList.get(i);
			
			// 공백 제낀다.
			if (oneInput == null || oneInput.length() == 0) {
				continue;
			}
			
			// 한글 섞여있으면 제낀다.
//			if (oneInput.matches(".*[가-힣].*")) {
//				continue;
//			}
			
			// 영어 없으면 제낀다.
			if (!oneInput.matches(".*[a-zA-Z].*")) {
				continue;
			}
			
			// 점이나 슬래시, 역슬래시 없으면 제낀다. (파일경로가 아님)
			// =>180525 폴더 경로도 허용한다.
//			if (oneInput.indexOf(".") < 0) {
//				continue;
//			}
			
			// 점이나 슬래시, 역슬래시 없으면 제낀다. (파일경로가 아님)
			if (oneInput.indexOf("/") < 0 && oneInput.indexOf("\\") < 0) {
				continue;
			}
			
			oneInput = revisePath(oneInput);
			int slashIdx = StringUtil.getIndexOfWorkspaceFolderSlash(oneInput);
			
			// 타겟 폴더 내용이 없다면, 타겟 패스 활용하여 채워준다.
			if (PatchForm.targetFolderText.getText() == null || PatchForm.targetFolderText.getText().length() == 0) {
				if (slashIdx > -1) {
					String newDirPath = oneInput.substring(0, slashIdx);
					PatchForm.targetFolderText.setText(newDirPath);
				}
			}
			
			if (slashIdx > -1) {
				oneInput = oneInput.substring(slashIdx);
			}
			
			oneInput = oneInput.trim();
			
			// 콜론이 없고 트렁크가 있을 경우... svn에서 가져온 changeLog이다.
			if (oneInput.indexOf(":") < 0) {
				boolean reviseTrunkLog = false;
				if (oneInput.startsWith("M /trunk/")) {
					reviseTrunkLog = true;
					
				} else if (oneInput.startsWith("A /trunk/")) {
					reviseTrunkLog = true;
					
				} else if (oneInput.startsWith("R /trunk/")) {
					// R도 패치로 포함시킨다. 이름변경인듯.
					reviseTrunkLog = true;
					
				} else if (oneInput.startsWith("D /trunk/")) {
					// 지운건 보정하지 말고 두자.(잘보이게)
					// => 180525 지운건 날려버리자. 로그에 쓰면 그만이다.
					AlterForm.open("경로 [" + oneInput + "]는 SVN changeLog상 삭제한 이력(D /trunk/)입니다. 패치할 파일인지 다시 확인하시고 해당 라인을 지워주십시오.");
					return;
				}
				
				// svn changeLog에서 가져온 파일경로 보정한다. 보정하지 않아도 무방하지만 서비스임.
				if (reviseTrunkLog) {
					String dirChunk = null;
					int iidx = -1;
					
					dirChunk = "/webapp/";
					iidx = oneInput.indexOf(dirChunk);
					if (iidx > -1 &&
						oneInput.indexOf("/src/") < 0 && oneInput.indexOf("/classes/") < 0 && oneInput.indexOf("/config/") < 0 &&
						!oneInput.endsWith(".java") && !oneInput.endsWith(".class")) {
						
						// 자바 및 클래스가 아닌 파일만 고치자.
						oneInput = oneInput.substring(iidx);
						
					} else {
						dirChunk = "/src/";
						iidx = oneInput.indexOf(dirChunk);
						if (iidx > -1 &&
							oneInput.indexOf("/webapp/") < 0 && oneInput.indexOf("/classes/") < 0 && oneInput.indexOf("/config/") < 0 &&
							oneInput.toLowerCase().endsWith(".java")) {
							
							// 자바 파일만 고치자.
							oneInput = oneInput.substring(iidx);
							
						} else {
							dirChunk = "/classes/";
							iidx = oneInput.indexOf(dirChunk);
							if (iidx > -1 &&
								oneInput.indexOf("/webapp/") < 0 && oneInput.indexOf("/src/") < 0 && oneInput.indexOf("/config/") < 0 &&
								oneInput.toLowerCase().endsWith(".class")) {
								
								// 클래스 파일만 고치자.
								oneInput = oneInput.substring(iidx);
							} else {
								
								dirChunk = "/config/";
								iidx = oneInput.indexOf(dirChunk);
								if (iidx > -1 &&
									oneInput.indexOf("/webapp/") < 0 && oneInput.indexOf("/src/") < 0 && oneInput.indexOf("/classes/") < 0 &&
									!oneInput.endsWith(".java") && !oneInput.endsWith(".class")) {
									
									// 자바 및 클래스가 아닌 파일만 고치자.
									oneInput = oneInput.substring(iidx);
								} else {
									
								}
							}
						}
					}
				}
				
			}
			
			oneInput = oneInput.trim();
			
			// 중복제외하고 추가.
			if (mapToCheckDupl.get(oneInput) == null) {
				mapToCheckDupl.put(oneInput, "1");
				resultPathBuffer.append(oneInput);
				resultPathBuffer.append("\r\n");
			}
		}
		
		if (resultPathBuffer.toString().endsWith("\r\n")) {
			resultPathBuffer.deleteCharAt(resultPathBuffer.length() - 1);
			resultPathBuffer.deleteCharAt(resultPathBuffer.length() - 1);
		}
		
		PatchForm.targetPathList.setText(resultPathBuffer.toString());
	}
	
	
	public void copyPathFileToCurrentSpace() {
		
		// 시작
		logBuffer = new StringBuffer();
		
		try {
			// 복사금지 패턴
			StringList forbiddenFilePatternList = null;
			
			try {
				forbiddenFilePatternList = makeForbiddenFilePatternList();
				
			} catch (MsgException e) {
				AlterForm.open(e.getMessage());
				return;
				
			} catch (Exception e) {
				throw e;
			}
			
			
			
			
			// 대상 폴더
			String targetFolderText = PatchForm.targetFolderText.getText();
			if (targetFolderText != null && targetFolderText.length() > 0) {
				targetFolderText = targetFolderText.trim();
				targetFolderText = targetFolderText.replace("\\", "/");
				
				if (targetFolderText.length() > 0) {
					if (!targetFolderText.endsWith("/")) {
						targetFolderText = targetFolderText + "/";
					}
				}
			}
			
			
			
			
			String inputText = PatchForm.targetPathList.getText();
			
			boolean bDirCopyMode = false;
			if (inputText == null || inputText.trim().length() == 0) {
				if (targetFolderText != null && targetFolderText.length() > 0) {
					File dirObj = new File(targetFolderText);
					if (dirObj.exists() && dirObj.isDirectory()) {
						// 입력값이 없으나 폴더가 제대로 입력된 경우 폴더 복사 모드로 인지
						bDirCopyMode = true;
						
					} else {
						AlterForm.open("폴더가 아니거나 존재하지 않는 경로입니다. [" + targetFolderText + "]");
						return;
					}
					
				} else {
					AlterForm.open("입력값이 없습니다.");
					return;
				}
			}
			
			
			
			
			FileController fileCtrl = new FileController(this);
			
			ArrayList<String> inputList = null;
			if (bDirCopyMode) {
				// 폴더 복사 모드
				inputList = FileCollector.getFileList(targetFolderText);
				if (inputList == null || inputList.size() == 0) {
					printErrLog("폴더 내의 파일이 존재하지 않습니다. [" + targetFolderText + "]");
				}
				
			} else {
				// 일반 복사 모드
				inputText = inputText.trim();
				
				// 엔터값으로 split
				ArrayList<String> oldInputList = StringUtil.splitMulti(inputText, "\r\n", "\r", "\n", ";");
				
				// 의미있는 파일 패스만 얻기
				inputList = getListMeaningfulFilePathOnly(oldInputList, targetFolderText);
				if (inputList == null || inputList.size() == 0) {
					printErrLog("패치 대상이 존재하지 않습니다.");
				}
			}

			
			int count = inputList.size();
			printLog("패치 대상 개수 : " + count);
			
			
			UniqueStringList resPathToPrint = new UniqueStringList();
			
			String oneInputPath = "";
			
			for (int i=0; i<count; i++) {
				printLog( (i+1) + "/" + count);
				// oneInputPath = inputList[i];
				oneInputPath = inputList.get(i);
				
				if (oneInputPath == null || oneInputPath.trim().length() == 0) {
					continue;
					
				} else {
					oneInputPath = oneInputPath.trim();
				}
				
				if (oneInputPath.startsWith("#")) {
					// 첫 글자 샵은 주석으로 인식
					continue;
				}
				
				if (matchPatternList(oneInputPath, forbiddenFilePatternList)) {
					printLog("복사 금지 패턴 : " + oneInputPath);
					continue;
				}
				
				if (!fileCtrl.copyAndPasteFile(bDirCopyMode, oneInputPath, resPathToPrint)) {
					printErrLog("실패! " + oneInputPath);
				}
			}
			
			
			// 결과 출력
			printResultPaths(resPathToPrint);
			
			if (logBuffer != null && logBuffer.length() > 0) {
				AlterForm.open("종료." + "\r\n" + logBuffer.toString(), CConst.errLogWidth, CConst.errLogHeight);
				
			} else {
				AlterForm.open("종료. 로그없음.");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void printResultPaths(UniqueStringList resPathToPrint) {
		if (resPathToPrint != null && resPathToPrint.size() > 0) {
			printLog("==================================================");
			int resPathCount = resPathToPrint.size();
			printLog("총 파일 개수 : " + resPathCount + "개");
			
			// 스트링 정렬
			Collections.sort(resPathToPrint);
			
			for (int i=0; i<resPathCount; i++) {
				printLog(resPathToPrint.get(i));
			}
			printLog("==================================================");
		}
	}
	
	
	public StringList makeForbiddenFilePatternList() throws MsgException, Exception {

		String extString = PatchForm.forbiddenFileText.getText();
		
		if (extString == null || extString.trim().length() == 0) {
			return null;
		}
		
		extString = extString.toLowerCase();
		
		if (extString.indexOf(".java") > -1 || extString.indexOf(".class") > -1) {
			throw new MsgException("복사금지 패턴에 .java 또는 .class를 기입할 수 없습니다.");
		}
		
		if (extString.indexOf("<") > -1 || extString.indexOf(">") > -1) {
			throw new MsgException("복사금지 패턴에 특수문자 '<' 또는 '>' 를 기입할 수 없습니다.");
		}
		
		if (extString.indexOf("[") > -1 || extString.indexOf("]") > -1) {
			throw new MsgException("복사금지 패턴에 특수문자 '[' 또는 ']' 를 기입할 수 없습니다.");
		}
		
		StringList extStringList = StringUtil.splitMulti(extString, ",", ";");
		return extStringList;
	}
	
	
	public boolean matchPatternList(String str, StringList patternList) {
		if (str == null || str.length() == 0) {
			return false;
		}
		
		if (patternList == null || patternList.size() == 0) {
			return false;
		}
		
		String onePattern = null;
		int patternCount = patternList.size();
		for (int i=0; i<patternCount; i++) {
			onePattern = StringUtil.parseStirng(patternList.get(i));
			if (onePattern.trim().length() == 0) {
				continue;
			} else {
				onePattern = onePattern.trim();
			}
			
			if (onePattern.indexOf("\\") > -1) {
				onePattern = onePattern.replace("\\", "/");
			}
			
			while (onePattern.indexOf("//") > -1) {
				onePattern = onePattern.replace("//", "/");
			}
			
			if (onePattern.indexOf(".") > -1) {
				onePattern = onePattern.replace(".", "\\.");
			}
			
			if (onePattern.indexOf("*") > -1) {
				onePattern = onePattern.replace("*", ".*");
			}
			
			if (onePattern.indexOf("?") > -1) {
				onePattern = onePattern.replace("?", "");
			}
			
			if (onePattern.indexOf("[") > -1) {
				onePattern = onePattern.replace("[", "");
			}
			
			if (onePattern.indexOf("]") > -1) {
				onePattern = onePattern.replace("]", "");
			}
			
			if (onePattern.indexOf("<") > -1) {
				onePattern = onePattern.replace("<", "");
			}
			
			if (onePattern.indexOf(">") > -1) {
				onePattern = onePattern.replace(">", "");
			}
			
			if (str.matches(onePattern)) {
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * 결과내역 간단히 출력
	 */
	public void pirntResult(ArrayList<String> resultFilePathList) {
		
		// 날짜얻기
		StringBuffer today = new StringBuffer();
		Calendar cal = Calendar.getInstance();
		today.append(String.format("%04d", cal.get(cal.YEAR)));
		today.append(String.format("%02d", cal.get(cal.MONTH) + 1));
		today.append(String.format("%02d", cal.get(cal.DAY_OF_MONTH)));
		
		File currentDir = new File("");
		String currentPath = currentDir.getAbsolutePath().replace("\\", "/");
		String resultPreText = currentPath + "/" + today.toString();
		
		// 결과내역 기록되는 텍스트
		String resultFileSuffix = "";
		File resultFile = null;
		
		int cnt = 1;
		while (true) {
			resultFileSuffix = "_" + cnt;
			resultFile = new File(resultPreText + "_" + cnt + ".txt");
			if (resultFile.exists()) {
				cnt++;
				continue;
			}
			
			try {
				resultFile.createNewFile();
				break;
				
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
		
		if (resultFile == null || !resultFile.exists()) {
			printErrLog("ERROR!");
			return;
		}
		
		int fileCount = resultFilePathList.size();
		
		// 검산
		File f = null;
		String onePath = "";
		
		for (int i=0; i<fileCount; i++) {
			onePath = resultFilePathList.get(i);
			f = new File(onePath);
			if (!f.exists()) {
				printErrLog("파일이 없습니다. : " + onePath);
			}
			if (!f.isFile()) {
				printErrLog("파일 형태가 아닙니다. : " + onePath);
			}
		}

		addTextln(logBuffer, "==LINUX COMMAND=========================");
		addTextln(logBuffer, "==(파일개수 : " + fileCount + ")============");
		
		addText(logBuffer, "jar -cvf backup_" + today.toString() + resultFileSuffix + ".jar ");
		for (int i=0; i<fileCount; i++) {
			if (resultFilePathList.get(i) != null) {
				if (i<(fileCount-1)) {
					addTextln(logBuffer, resultFilePathList.get(i).trim() + " \\");
				} else {
					// last element
					addTextln(logBuffer, resultFilePathList.get(i).trim());
				}
			}
		}
		
		addTextln(logBuffer, "========================================");
		
		FileController fileCtrl = new FileController(this);
		fileCtrl.writeFile(resultFile, logBuffer);
		
		System.out.println(logBuffer.toString());
		System.out.println("결과파일 ====>" + resultFile.getAbsolutePath());
		
	}
	
	public void printLog(String str) {
		System.out.println(str);
		logBuffer.append(str);
		logBuffer.append("\r\n");
	}
	
	public void printErrLog(String str) {
		str = "[ERROR!] " + str;
		System.err.println(str);
		logBuffer.append(str);
		logBuffer.append("\r\n");
	}
	
	public void addTextln(StringBuffer buff, String str) {
		if (buff == null) {
			buff = new StringBuffer();
		}
		
		buff.append(str);
		buff.append("\r\n");
	}
	
	public void addText(StringBuffer buff, String str) {
		if (buff == null) {
			buff = new StringBuffer();
		}
		
		buff.append(str);
	}
}
