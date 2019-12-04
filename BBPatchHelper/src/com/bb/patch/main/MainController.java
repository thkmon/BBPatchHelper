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
import com.bb.patch.string.PathUtil;
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

		for (int i = 0; i < oldCount; i++) {
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
			
			// 클라이언트 파일을 찾을 수 없을 경우 vue 프레임워크일 수도 있으니 /frontend/src/ 하위에서 찾아본다.
			if (!file.exists()) {
				String lowerExt = PathUtil.getLowerExtension(file.getAbsolutePath());
				// if (lowerExt.equals("htm") || lowerExt.equals("html") || lowerExt.equals("js") || lowerExt.equals("jsp") || lowerExt.equals("css") || lowerExt.equals("vue")) {
				if (!lowerExt.equals("java") && !lowerExt.equals("class")) {
					String newPath = file.getAbsolutePath().replace("\\src\\", "\\frontend\\src\\");
					File newFile = new File(newPath);
					if (newFile.exists()) {
						if (!oneInputPath.equals(newPath)) {
							printLog("vue 관련 경로 수정함. 수정된 파일 복제 대상경로 : " + oneInputPath + " ===> " + newPath);
						}
						
						oneInputPath = newPath;
						file = newFile;
					}
				}
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

	// REVISE 버튼 클릭시 수행
	public void reviseButtonClicked() {

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

		// 경고 메시지 담는다.
		StringBuffer alertMsgBuffer = new StringBuffer();

		HashMap mapToCheckDupl = new HashMap();
		StringBuffer resultPathBuffer = new StringBuffer();

		String oneInput = null;
		int count = inputList.size();
		for (int i = 0; i < count; i++) {
			oneInput = inputList.get(i);

			// 공백은 대상이 아니다.
			if (oneInput == null || oneInput.length() == 0) {
				continue;
			}

			// 영어 없으면 대상이 아니다.
			if (!oneInput.matches(".*[a-zA-Z].*")) {
				continue;
			}

			// 점이 없고, 폴더가 아니면 대상이 아니다. (폴더는 허용한다.)
			if (oneInput.indexOf(".") < 0 && !PathUtil.checkIsFolderPath(oneInput)) {
				continue;
			}

			// 한글 포함, 그리고 문자열 도중에 공백이 포함되었을 경우 대상이 아니다. (한글 주석으로 판단)
			if (PathUtil.checkContainKorean(oneInput) && PathUtil.checkContainBlank(oneInput.trim())) {
				continue;
			}

			// 슬래시, 역슬래시 없으면 대상이 아니다. (파일경로가 아님)
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

			boolean bSvnChangeLog = false;

			// 콜론이 없고 트렁크가 있을 경우... svn에서 가져온 changeLog이다.
			if (oneInput.indexOf(":") < 0) {
				if (oneInput.startsWith("M /trunk/") || oneInput.startsWith("M /")) {
					bSvnChangeLog = true;

				} else if (oneInput.startsWith("A /trunk/") || oneInput.startsWith("A /")) {
					bSvnChangeLog = true;

				} else if (oneInput.startsWith("R /trunk/") || oneInput.startsWith("R /")) {
					// R도 패치로 포함시킨다. 이름변경인듯.
					bSvnChangeLog = true;

				} else if (oneInput.startsWith("D /trunk/") || oneInput.startsWith("D /")) {
					// 삭제 이력 : 날려버리자.
					if (alertMsgBuffer.length() > 0) {
						alertMsgBuffer.append("\r\n");
					}
					alertMsgBuffer.append("경로 [" + oneInput + "]는 SVN changeLog상 삭제한 이력(D /trunk/)이므로 skip 합니다.");

					// 지운건 날려버리자.
					continue;
				}
			}

			// svn changeLog에서 가져온 파일경로 보정한다.
			if (bSvnChangeLog) {
				int tempIndex = -1;

				tempIndex = oneInput.indexOf("/src/");
				if (tempIndex > -1) {
					oneInput = oneInput.substring(tempIndex);
				} else {
					tempIndex = oneInput.indexOf("/webapp/");
					if (tempIndex > -1) {
						oneInput = oneInput.substring(tempIndex);
					} else {
						tempIndex = oneInput.indexOf("/webapps/");
						if (tempIndex > -1) {
							oneInput = oneInput.substring(tempIndex);
						} else {
							tempIndex = oneInput.indexOf("/config/");
							if (tempIndex > -1) {
								oneInput = oneInput.substring(tempIndex);
							} else {
								tempIndex = oneInput.indexOf("/classes/");
								if (tempIndex > -1) {
									oneInput = oneInput.substring(tempIndex);
								} else {
									tempIndex = oneInput.indexOf("/bin/");
									if (tempIndex > -1) {
										oneInput = oneInput.substring(tempIndex);
									} else {
										// 만약 /turnk/가 존재한다면, trunk 다음 슬래시부터 시작하도록 자른다.
										int trunkIndex = oneInput.indexOf("/trunk/");
										if (trunkIndex > -1) {
											int slash1Index = oneInput.indexOf("/", trunkIndex);
											int slash2Index = oneInput.indexOf("/", slash1Index + 1);
											if (slash1Index > -1 && slash2Index > -1) {
												oneInput = oneInput.substring(slash2Index);
											}
										} else {
											// 이도저도 아니면 두번째 슬래시부터 시작하도록 자른다.
											int slash1Index = oneInput.indexOf("/");
											int slash2Index = oneInput.indexOf("/", slash1Index + 1);
											if (slash1Index > -1 && slash2Index > -1) {
												oneInput = oneInput.substring(slash2Index);
											}
										}

									}
								}
							}
						}
					}
				}
			}

			oneInput = oneInput.trim();
			
			if (oneInput.equals("/") || oneInput.equals("\\")) {
				// 슬래시나 역슬래시만 남았을 경우 제거한다.
				continue;
			}

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
		
		// 라인현황 업데이트
		PatchForm.updateLineCountLabel();
		
		// 경고 메시지 있을 경우 띄워준다.
		if (alertMsgBuffer != null && alertMsgBuffer.length() > 0) {
			AlterForm.open(alertMsgBuffer.toString());
		}
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

			// 진짜 클래스 폴더패스를 찾는다. 인풋박스에 입력한 값으로 폴더 존재하는지 검사해보고, 없으면 .classpath 파일을 읽어내서 찾아낸다.
			String realClassFolderPath = getRealClassFolderPath();
			
			int count = inputList.size();
			printLog("패치 대상 개수 : " + count);

			UniqueStringList resPathToPrint = new UniqueStringList();

			String oneInputPath = "";

			for (int i = 0; i < count; i++) {
				printLog((i + 1) + "/" + count);
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

				if (!fileCtrl.copyAndPasteFile(realClassFolderPath, bDirCopyMode, oneInputPath, resPathToPrint)) {
					printErrLog("실패! " + oneInputPath);
				}
			}

			// 결과 출력
			printResultPaths(resPathToPrint);

			if (logBuffer != null && logBuffer.length() > 0) {
				AlterForm.open("결과." + "\r\n" + logBuffer.toString(), CConst.errLogWidth, CConst.errLogHeight);

			} else {
				AlterForm.open("결과. 로그없음.");
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

			for (int i = 0; i < resPathCount; i++) {
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
		for (int i = 0; i < patternCount; i++) {
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

		for (int i = 0; i < fileCount; i++) {
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
		for (int i = 0; i < fileCount; i++) {
			if (resultFilePathList.get(i) != null) {
				if (i < (fileCount - 1)) {
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
	
	/**
	 * 진짜 클래스 폴더패스를 찾는다. 인풋박스에 입력한 값으로 폴더 존재하는지 검사해보고, 없으면 .classpath 파일을 읽어내서 찾아낸다.
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getRealClassFolderPath() throws Exception {
		boolean replaceJavaToCls = PatchForm.javaToClassCheckBox.isSelected();
		
		// java 대신 class 가져오기 체크한 경우만 클래스 패스 필요하다.
		if (!replaceJavaToCls) {
			return "";
		}
		
		// 대상파일 인풋박스 내용
		String targetFolderText = StringUtil.parseStirng(PatchForm.targetFolderText.getText());
		String inputClassPath = StringUtil.parseStirng(PatchForm.classFolderText.getText()).trim();
		
		// 1. 일단 인풋박스에 입력해둔 클래스 패스가 유효한지 검사한다.
		if (inputClassPath.indexOf(":") > -1) {
			// C드라이브부터 입력했을 경우
			File firstClassDir = new File(inputClassPath);
			if (firstClassDir.exists() && firstClassDir.isDirectory()) {
				String resultPath = PathUtil.revisePath(firstClassDir.getAbsolutePath());
				printLog("% 클래스 폴더 패스 : " + resultPath);
				return resultPath;
			}
		}
		
		// 2. 계속해서 인풋박스에 입력해둔 클래스 패스가 유효한지 검사한다. (예 : 대상파일 +/classes)
		if (targetFolderText.length() > 0) {
			if (inputClassPath.length() > 0) {
				// 대상파일 인풋박스 내용
				String firstClassPath = PathUtil.revisePath(targetFolderText + "/" + inputClassPath);
				File firstClassDir = new File(firstClassPath);
				if (firstClassDir.exists() && firstClassDir.isDirectory()) {
					String resultPath = PathUtil.revisePath(firstClassDir.getAbsolutePath());
					printLog("%% 클래스 폴더 패스 : " + resultPath);
					return resultPath;
				}
			}
		}
		
		// 3. 인풋박스에 입력해둔 클래스 패스가 유효하지 않으면, .classpath 파일을 찾아 읽어본다.
		if (targetFolderText.length() > 0) {
			String infoFilePath = PathUtil.revisePath(targetFolderText + "/" + ".classpath");
			File infoFileObj = new File(infoFilePath);
			if (infoFileObj.exists() && infoFileObj.isFile()) {
				
				String parsedClassPath = "";
				
				ArrayList<String> infoFileContent = null;
				try {
					FileController fileCtrl = new FileController(this);
					infoFileContent = fileCtrl.readFile(infoFileObj);
					
					if (infoFileContent != null && infoFileContent.size() > 0) {
						String oneLine = null;
						int fileLineCount = infoFileContent.size();
						for (int i=0; i<fileLineCount; i++) {
							oneLine = StringUtil.parseStirng(infoFileContent.get(i));
							
							// <classpathentry kind="output" path="classes"/>
							
							int idx1 = oneLine.indexOf("<");
							if (idx1 < 0) {
								continue;
							}
							
							int idx2 = oneLine.indexOf("classpathentry", idx1 + 1);
							if (idx2 < 0) {
								continue;
							}
							
							int idx3 = oneLine.indexOf("kind=\"output\"", idx2 + 1);
							if (idx3 < 0) {
								continue;
							}
							
							int idx4 = oneLine.indexOf("path=\"", idx3 + 1);
							if (idx4 < 0) {
								continue;
							}
							
							int idx5 = oneLine.indexOf("\"", idx4 + 1);
							if (idx5 < 0) {
								continue;
							}
							
							int idx6 = oneLine.indexOf("\"", idx5 + 1);
							if (idx6 < 0) {
								continue;
							}
							
							parsedClassPath = oneLine.substring(idx5 + 1, idx6);
							break;
						}
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				if (parsedClassPath != null && parsedClassPath.length() > 0) {
					// 대상파일 인풋박스 내용
					String secondClassPath = PathUtil.revisePath(targetFolderText + "/" + parsedClassPath);
					File secondClassDir = new File(secondClassPath);
					if (secondClassDir.exists() && secondClassDir.isDirectory()) {
						String resultPath = PathUtil.revisePath(secondClassDir.getAbsolutePath());
						printLog("%%% 클래스 폴더 패스 : " + resultPath);
						return resultPath;
					}
				}
			}
		}
		
		return "";
	}
}
