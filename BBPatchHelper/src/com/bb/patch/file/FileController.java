package com.bb.patch.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;

import com.bb.patch.file.data.UniqueStringList;
import com.bb.patch.form.PatchForm;
import com.bb.patch.main.MainController;
import com.bb.patch.string.PathUtil;
import com.bb.patch.string.StringUtil;

public class FileController {
	
	
	private MainController mainCtrl = null;
	public FileController(MainController mainCtrl) {
		this.mainCtrl = mainCtrl;
	}
	

	public boolean copyAndPasteFile(String realClassFolderPath, boolean bDirCopyMode, String path, UniqueStringList resultFilePathListToPrint, UniqueStringList resultCorePathListToPrint, UniqueStringList resultforbiddenPathListToPrint) throws Exception {
		if (path == null || path.trim().length() == 0) {
			printErrLog("파일 경로가 없습니다.");
			return false;
		}
		
		// 패스 보정
		path = escapePath(path);
		
		try {
			// webapp 경로 포함된 경우만 가져오기
			boolean bCheckedGetWebappDirOnly = PatchForm.getWebappDirOnlyCheckBox.isSelected();
			String webappDirText = "/webapp/";
			String webappDirText2 = "\\webapp\\";
			
			File originFile = new File(path);
			
			// 파일일 경우
			printLog("파일 복제 대상경로 : " + path);
			
			// 폴더 복사일 경우에는 java 대신 class 가져오기 적용하지 않는다.
			if (!bDirCopyMode) {
				
				String newPath = path;
				newPath = replacePathFindToReplace(realClassFolderPath, newPath);
				
				if (!path.equals(newPath)) {
					printLog("경로 수정함. 수정된 파일 복제 대상경로 : " + path + " ===> " + newPath);
					path = newPath;
				}
			}
			
			if (!originFile.exists()) {
				printErrLog("해당 경로에 파일이 존재하지 않습니다. path is '" + path + "'");
				return false;
			}

			if (!originFile.isFile()) {
				printErrLog("해당 경로는 파일이 아닙니다. path is '" + path + "'");
				return false;
			}
			
			String endPath = path;
			endPath = PathUtil.makeEndPath(endPath);
			
			String destDirPath = PatchForm.destDirText.getText();
			if (destDirPath == null || destDirPath.length() == 0) {
				// destDirPath = "C:\\patch_result";
				printErrLog("결과 폴더를 알 수 없습니다.");
				return false;
			}
			
			if (!destDirPath.matches("[a-zA-Z]:.*")) {
				printErrLog("결과 폴더는 드라이브명으로 시작해야 합니다. (ex : C:, D:, E:)");
				return false;
			}
			
			File currentDir = new File(destDirPath);
			if (!currentDir.exists()) {
				printLog("존재하지 않는 폴더생성 : " + destDirPath);
				currentDir.mkdirs();
			}
			String currentDirPath = currentDir.getAbsolutePath().replace("\\", "/");
			String resultPath = currentDirPath + "/" + endPath;
			resultPath = StringUtil.makeToSlashPath(resultPath);

			String corePath1 = "/" + endPath;
			corePath1 = StringUtil.makeToSlashPath(corePath1);
			
			printLog("수정시간 : " + getFileModifyDateTime(originFile));
			printLog("파일 복제 결과경로 : " + resultPath);
			
			// webapp 경로 포함된 경우만 가져오기
			if (bCheckedGetWebappDirOnly) {
				if (resultPath.indexOf(webappDirText) < 0 && resultPath.indexOf(webappDirText2) < 0) {
					printLog("webapp 경로 포함되지 않았으므로 제외 : " + resultPath);
					
					resultforbiddenPathListToPrint.add(corePath1);
					printLog("==================================================");
					return true;
				}
			}
			
			
			if (originFile.exists()) {
				boolean copySuccess = true;
						
				File f = new File(path);
				if (!f.exists()) {
					copySuccess = false;
					printErrLog("존재하지 않습니다. : " + path);
				}
				
				if (!f.isFile()) {
					copySuccess = false;
					printErrLog("파일이 아닙니다. : " + path);
				}
				
				// 실제 카피한다.
				if (copySuccess) {
					copySuccess = copyFile(path, resultPath);
				}
				
				// 폴더 경로 얻기
				{
					// 이너클래스 얻기
					int lastSlashPos = path.lastIndexOf("/");
					String dirPath = path.substring(0, lastSlashPos);
					
					// 파일 이름만 얻기
					String fileNameOnly = path.substring(lastSlashPos + 1);
					String extOnly = "";
					
					int lastDotPos = fileNameOnly.lastIndexOf(".");
					if (lastDotPos > -1) {
						extOnly = fileNameOnly.substring(lastDotPos + 1);
						fileNameOnly = fileNameOnly.substring(0, lastDotPos);
					}
					
					if (extOnly != null) {
						if (extOnly.equalsIgnoreCase("class")) {

//							System.out.println("fileNameOnly : " + fileNameOnly);
							
//							fileNameOnly = fileNameOnly.replace("/", "");
//							fileNameOnly = fileNameOnly.replace("\\", "");
							
							// 리스트
							File dir = new File(dirPath);
							File[] fList = dir.listFiles();
							File innerClassFile = null;
							int fCount = fList.length;
							for (int i=0; i<fCount; i++) {
								innerClassFile = fList[i];
								
								if (!innerClassFile.exists()) {
									continue;
								}
								
								if (!innerClassFile.isFile()) {
									continue;
								}
								
								String innerClassFileName = innerClassFile.getAbsoluteFile().getName();
								if (innerClassFileName.startsWith(fileNameOnly + "$") &&
									innerClassFileName.toLowerCase().endsWith(".class")) {
									
									printLog("이너클래스 추가 : " + StringUtil.makeToSlashPath(innerClassFile.getAbsolutePath()));
									
									String endPath2 = innerClassFile.getAbsolutePath();
									endPath2 = PathUtil.makeEndPath(endPath2);
									
									// currentDir = new File("");
									// currentDirPath = currentDir.getAbsolutePath().replace("\\", "/");
									
									currentDirPath = destDirPath;
									
									String resultPath2 = currentDirPath + "/" + endPath2;
									resultPath2 = StringUtil.makeToSlashPath(resultPath2);
									
									String corePath2 = "/" + endPath2;
									corePath2 = StringUtil.makeToSlashPath(corePath2);
									
									boolean innerCopySuccess = copyFile(innerClassFile.getAbsolutePath(), resultPath2);
									if (innerCopySuccess) {
										resultFilePathListToPrint.add(resultPath2);
										resultCorePathListToPrint.add(corePath2);
										
									} else {
										printErrLog("복사 실패 : " + resultPath2);
									}
								}
							}
							// MMapFile$MMapIterator
						}
					}
					
				}
				
				// 유효한 파일패스만 넣는다. 나중에 출력해주기 위함.
				if (copySuccess) {
					resultFilePathListToPrint.add(resultPath);
					resultCorePathListToPrint.add(corePath1);
				} else {
					printErrLog("복사 실패 : " + resultPath.trim());
				}
				
			} else {
				printErrLog("파일이 존재하지 않습니다! " + path);
			}
			
			
			printLog("==================================================");
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public String replacePathFindToReplace(String realClassFolderPath, String originPath) {
		if (originPath == null || originPath.length() == 0) {
			return "";
		}
		
		String resultPath = "";
		
		try {
			resultPath = originPath;
			
			boolean replaceJavaToCls = false;
			replaceJavaToCls = PatchForm.javaToClassCheckBox.isSelected();
			
			if (replaceJavaToCls) {
				
				if (resultPath.lastIndexOf(".java") > -1) {
					// 자바일 경우에만 치환
					
					// 파일 경로로 패키지 위치를 알아내는 방식은 예외가 존재한다.
					// 실제로 클래스 파일 내용을 읽어서 패키지를 알아내자.
					resultPath = getRealClassFilePath(realClassFolderPath, resultPath);
					
					if (!originPath.equals(resultPath)) {
						printLog("class 관련 경로 수정함. 수정된 파일 복제 대상경로 : " + originPath + " ===> " + resultPath);
					}
					
				} else if (resultPath.indexOf("/src/") > -1) {
					// 스프링 고려 : 마이바티스 sql용 xml, jaxb.properties 등 가져오도록 수정.
					int idxSrc = resultPath.indexOf("/src/");
					int idxCom = resultPath.indexOf("/com/", idxSrc);
					
					if (idxCom > -1) {
						String tempFilePath = realClassFolderPath + resultPath.substring(idxCom);
						File file = new File(tempFilePath);
						if (file.exists()) {
							resultPath = tempFilePath;
							
							if (!originPath.equals(resultPath)) {
								printLog("classes 폴더 내의 경로 수정함. 수정된 파일 복제 대상경로 : " + originPath + " ===> " + resultPath);
							}
						}
					}
				}
			}
			
		} catch (Exception e) {
			printErrLog("replacePathFindToReplace : " + e.getMessage());
			e.printStackTrace();
			return originPath;
		}
		
		return resultPath;
	}
	
	private String getRealClassFilePath(String realClassFolderPath, String javaPath) {
		String originPath = javaPath;
		
		String resultPath = javaPath;
		
		BufferedReader reader = null;
		File file = null;
		
		try {
			
			file = new File(javaPath);
			if (!file.exists()) {
				return null;
			}
			
			if (!file.isFile()) {
				return null;
			}
			
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));

			String line = "";
			StringBuffer buff = new StringBuffer();
			int len = 0;
			String oneCh = "";
			String twoCh = "";
			
			
			String packagePath = null;
			
			boolean multiCommentMode = false;
			boolean doubleQuoteMode = false;
			lineByLineLoop : while((line = reader.readLine()) != null) {
				
				try {
					int idxPackage = buff.indexOf("package ");
					if (idxPackage < 0) {
						idxPackage = buff.indexOf("package	");
					}
					
					if (idxPackage > -1) {
						int idxSemiColon = buff.indexOf(";", idxPackage + 1);
						if (idxSemiColon > -1) {
							packagePath = buff.substring(idxPackage + 7, idxSemiColon).trim();
							packagePath = packagePath.replace(".", "/");
							packagePath = packagePath.replace(" ", "");
							break lineByLineLoop;
							
						} else {
							// 세미콜론없음. 말이 안되므로(손상된 자바파일이므로) 무시.
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					printErrLog(e.getMessage());
				}
				
				// 한줄 읽을때마다 비우기.
				// 비워도 되고 안비워도 되는데 비우자.
				buff.delete(0, buff.length());
				
				
				len = line.length();
				for (int i=0; i<len; i++) {
					twoCh = substringNotError(line, i, i+2);
					
					if (!doubleQuoteMode) {
						if (!multiCommentMode) {
							if (twoCh.equals("/*")) {
								multiCommentMode = true;
							}
							
						} else {
							if (twoCh.equals("*/")) {
								multiCommentMode = false;
								i = i + 1;
								continue;
							}
						}
						
						if (multiCommentMode) {
							continue;
						}
					}
					
					oneCh = substringNotError(line, i, i+1);
					
					// 추가코드.
					if (!doubleQuoteMode) {
						if (oneCh.equals("\"")) {
							// -> 정석으로 파싱할 수도 있지만(아래 코드  모두 정상동작하지만)
							// 따옴표까지 나왔으면 어차피 패키지가 없는거다. 속도를 위해 패키지를 비우고 달려보자.
							packagePath = null;
							break lineByLineLoop;
						}
					}
						
						
					// 사실 따옴표 나온 순간부터 끝장났다고 봐야한다.(패키지 나오기 전에 따옴표 나오기 불가능.)
					// 패키지 없다고 봐야함. 일단 정석으로 파싱해보자.
					if (!doubleQuoteMode) {
						if (oneCh.equals("\"")) {
							doubleQuoteMode = true;
						}
						
					} else {
						if (oneCh.equals("\"")) {
							doubleQuoteMode = false;
							continue;
						}
					}
					
					if (doubleQuoteMode) {
						continue;
					}
					
					if (twoCh.equals("//")) {
						// 한줄주석 나오면 다음줄로 이동한다.
						continue lineByLineLoop;
					}
					
					buff.append(oneCh);
				}
			}


			// 객체 닫기
			reader.close();
			
			
				
			resultPath = javaPath;
			int dotJavaPos = resultPath.lastIndexOf(".java");
			resultPath = resultPath.substring(0, dotJavaPos) + ".class";
			
			if (resultPath.indexOf(" ") > -1) {
				resultPath = resultPath.replace(" ", "");
			}
			
			if (resultPath.indexOf("\\") > -1) {
				resultPath = resultPath.replace("\\", "/");
			}
			
			while (resultPath.indexOf("//") > -1) {
				resultPath = resultPath.replace("//", "/");
			}
			
			
			// 클래스파일 인풋박스 보정
			String classFolderText = StringUtil.parseStirng(realClassFolderPath).trim();
			
			// 200306 공통 클래스 패스가 없을 경우, 다시 말해 [대상 폴더 (비워도 됨)] 인풋박스가 실제 비워져 있을 경우, 개별 java 파일마다 각각의 class 패스를 찾아온다.
			if (classFolderText.length() == 0) {
				if (originPath != null && originPath.indexOf(":") > -1) {
					int slashIdx = PathUtil.getIndexOfWorkspaceFolderSlash(originPath);
					if (slashIdx > -1) {
						String oneWorkspacePath = originPath.substring(0, slashIdx);
						String oneClassDirPath = this.mainCtrl.getRealClassFolderPathByDotClasspathFile(oneWorkspacePath);
						if (oneClassDirPath != null && oneClassDirPath.length() > 0) {
							classFolderText = oneClassDirPath;
						}
					}
				}
			}
			
			String tmpPath = resultPath;
			int srcPos = resultPath.indexOf("/src/");
			int lastSlashPos = resultPath.lastIndexOf("/");
			if (srcPos > -1 && lastSlashPos > srcPos) {
				// String FileDirPath = resultPath.substring(0, srcPos);
				String classFileName = resultPath.substring(lastSlashPos + 1);
				
				// 패키지 없음
				if (packagePath == null || packagePath.length() == 0) {
					packagePath = "";
				} else {
					packagePath = packagePath.trim();
				}
				
				// C드라이브부터 입력했을 경우
//				if (classFolderText.indexOf(":") > -1) {
//					resultPath = classFolderText + "/" + packagePath + "/" + classFileName;
//					
//				} else {
//					resultPath = FileDirPath + "/" + classFolderText + "/" + packagePath + "/" + classFileName;
//				}
				
				resultPath = classFolderText + "/" + packagePath + "/" + classFileName;
				
				// 슬래시 중복 없도록 패스 보정
				resultPath = StringUtil.revisePath(resultPath);
				
			} else {
				printErrLog("잘못된 파일 패스입니다. 청크 /src/ 또는 마지막슬래시(/)를 찾을 수 없습니다. : " + tmpPath);
				return originPath;
			}
			
			
			
			
			File newClsFile = new File(resultPath);
			
			if (!newClsFile.exists()) {
				printErrLog("존재하지 않는 파일입니다. : " + resultPath);
				return originPath;
			}
			
			if (!newClsFile.isFile()) {
				printErrLog("파일이 아닙니다. : " + resultPath);
				return originPath;
			}
			
			// package com.nanum.smart.memo.bean;
			
		} catch (Exception e) {
			return originPath;
		} finally {
			
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (Exception e) {
				// 무시
			} finally {
				reader = null;
			}
			
			file = null;
		}
		
		return resultPath;
	}
	
	
	private String substringNotError(String str, int beginIdx, int endIdxNotIncluded) {
		if (str == null || str.length() == 0) {
			return "";
		}
		
		if (beginIdx < 0) {
			beginIdx = 0;
		}
		
		int len = str.length();
		if (endIdxNotIncluded > len) {
			endIdxNotIncluded = len;
		}
		
		return str.substring(beginIdx, endIdxNotIncluded);
	}
	
	
	public boolean createParentDirs(String outFilePath) {
		outFilePath = StringUtil.makeToSlashPath(outFilePath);
		int lastSlashDot = outFilePath.lastIndexOf("/");
		if (outFilePath.lastIndexOf("/") > 0) {
			File dir = new File(outFilePath.substring(0, lastSlashDot));
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}
		
		return true;
	}

	
	public boolean copyFile(String inFilePath, String outFilePath) {
		
		FileInputStream inputStream = null;
		FileOutputStream outputStream = null;
		
		try {
//			outFilePath = StringUtil.makeToSlashPath(outFilePath);
//			int lastSlashDot = outFilePath.lastIndexOf("/");
//			if (outFilePath.lastIndexOf("/") > 0) {
//				File dir = new File(outFilePath.substring(0, lastSlashDot));
//				if (!dir.exists()) {
//					dir.mkdirs();
//				}
//			}
			createParentDirs(outFilePath);
			
			File f = new File(outFilePath);
			if (f.exists()) {
				f.delete();
			}
			
			f.createNewFile();
			
			inputStream = new FileInputStream(inFilePath);
			outputStream = new FileOutputStream(outFilePath);
	
			FileChannel fcin = inputStream.getChannel();
			FileChannel fcout = outputStream.getChannel();
	
			long size = fcin.size();
			fcin.transferTo(0, size, fcout);
	
			fcout.close();
			fcin.close();
	
			outputStream.close();
			inputStream.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
			
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (Exception ie) {
				inputStream = null;
			}
			
			try {
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (Exception ie) {
				outputStream = null;
			}
		}
		
		return true;
	}

	
	public boolean makeNewFile(String filePath) throws Exception {
		
		// 패스 보정
		filePath = escapePath(filePath);
		
		File file = new File(filePath);
		if (file.exists()) {
			return false;
		}
		
		if (!file.exists()) {
			
			// 폴더 만든다.
			String newFolderPath = getFolderPath(filePath);
			File newFolder = new File(newFolderPath);
			if (!newFolder.exists()) {
				newFolder.mkdirs();
			}
			
			file.createNewFile();
		}
		return false;
	}
	
	
	// 패스 보정
	public String escapePath(String path) throws Exception {
		if (path == null || path.trim().length() == 0) {
			// return "";
			throw new Exception("escapePath : path is null or empty");
		}
		
		path = path.trim();
		path = path.replace("\\", "/");
		while (path.indexOf("//") >= 0) {
			path = path.replace("//", "/");
		}
		
		return path;
	}
	
	
	public String getFolderPath(String path) throws Exception {
		if (path == null || path.trim().length() == 0) {
			// return "";
			throw new Exception("getFolderPath : path is null or empty");
		}
		
		int lastSlashIdx = path.lastIndexOf("/");
		if (lastSlashIdx < 0) {
			return "";
			// 슬래시가 없다고???
			// throw new Exception
		}
		
		return path.substring(0, lastSlashIdx);
	}
	
	
	// 파일의 수정시간 알아내기
	public String getFileModifyDateTime(File f) {
		
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(f.lastModified());
			return getTodayDateTime(cal);
			
		} catch (Exception e) {
			e.printStackTrace();
			return "알 수 없는 날짜";
		}
	}
	
	private String getTodayDateTime(Calendar cal) throws Exception {
		StringBuffer today = new StringBuffer();
		today.append(String.format("%04d", cal.get(cal.YEAR)));
		today.append("-");
		today.append(String.format("%02d", cal.get(cal.MONTH) + 1));
		today.append("-");
		today.append(String.format("%02d", cal.get(cal.DAY_OF_MONTH)));
		
		today.append(" ");
		
		today.append(String.format("%02d", cal.get(cal.HOUR_OF_DAY)));
		today.append(":");
		today.append(String.format("%02d", cal.get(cal.MINUTE)));
		today.append(":");
		today.append(String.format("%02d", cal.get(cal.SECOND)));
		return today.toString();
	}
	
	
	public static void writeFile(File file, StringBuffer strBuffer) {
		
		BufferedWriter writer = null;
		
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
			// (new OutputStreamWriter(new FileOutputStream(file),"MS949"));

			//for (String str : strList) {
				if (strBuffer != null) {
					writer.write(strBuffer.toString(), 0, strBuffer.toString().length());
					writer.newLine();
				}
			//}

			// 객체 닫기
			writer.close();

		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (Exception inEx) {
				writer = null;
			}
		}
	}
	
	
	public void printErrLog(String str) {
		// this.printErrLog(str);
		mainCtrl.printErrLog(str);
	}
	
	
	public void printLog(String str) {
		// this.printErrLog(str);
		mainCtrl.printLog(str);
	}

	
	/**
	 * 파일 읽기
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws Exception
	 */
	public static ArrayList<String> readFile(File file) throws IOException, Exception {
		if (file == null || !file.exists()) {
			return null;
		}

		ArrayList<String> resultList = null;

		FileInputStream fileInputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;

		try {
			fileInputStream = new FileInputStream(file);
			inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
			bufferedReader = new BufferedReader(inputStreamReader);

			String oneLine = null;
			while ((oneLine = bufferedReader.readLine()) != null) {
				if (resultList == null) {
					resultList = new ArrayList<String>();
				}

				resultList.add(oneLine);
			}

		} catch (IOException e) {
			throw e;

		} catch (Exception e) {
			throw e;

		} finally {
			close(bufferedReader);
			close(inputStreamReader);
			close(fileInputStream);
		}

		return resultList;
	}
	
	
	private static void close(BufferedWriter bufferedWriter) {
		try {
			if (bufferedWriter != null) {
				bufferedWriter.close();
			}
		} catch (Exception e) {
			// 무시

		} finally {
			bufferedWriter = null;
		}
	}

	private static void close(OutputStreamWriter outputStreamWriter) {
		try {
			if (outputStreamWriter != null) {
				outputStreamWriter.close();
			}
		} catch (Exception e) {
			// 무시

		} finally {
			outputStreamWriter = null;
		}
	}

	private static void close(FileOutputStream fileOutputStream) {
		try {
			if (fileOutputStream != null) {
				fileOutputStream.close();
			}
		} catch (Exception e) {
			// 무시

		} finally {
			fileOutputStream = null;
		}
	}

	private static void close(FileInputStream fileInputStream) {
		try {
			if (fileInputStream != null) {
				fileInputStream.close();
			}
		} catch (Exception e) {
			// 무시

		} finally {
			fileInputStream = null;
		}
	}

	private static void close(InputStreamReader inputStreamReader) {
		try {
			if (inputStreamReader != null) {
				inputStreamReader.close();
			}
		} catch (Exception e) {
			// 무시

		} finally {
			inputStreamReader = null;
		}
	}

	private static void close(BufferedReader bufferedReader) {

		try {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
		} catch (Exception e) {
			// 무시

		} finally {
			bufferedReader = null;
		}
	}

}
