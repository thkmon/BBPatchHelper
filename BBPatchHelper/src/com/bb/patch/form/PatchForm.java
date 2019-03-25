package com.bb.patch.form;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.bb.patch.common.CConst;
import com.bb.patch.datetime.DatetimeUtil;
import com.bb.patch.main.MainController;
import com.bb.patch.string.StringUtil;

public class PatchForm {
	
	public static JTextField targetFolderText = null;
	public static JTextField classFolderText = null;
	public static JCheckBox javaToClassCheckBox = null;
	
	private int top = 0;
	
	private void plusTop(int num) {
		top = top + (25 * num);
	}
	private void plusTopLittle(int num) {
		top = top + (10 * num);
	}
	
	public static JTextArea targetPathList = null;
	public static JScrollPane targetPathScrollPane = null;
	
	public static JLabel lineLabel = null;
	
	public static JLabel forbiddenFileLabel = null;
	public static JTextField forbiddenFileText = null;
	
	public static JLabel destDirLabel = null;
	public static JTextField destDirText = null;
	
	public static JButton copyButton = null;
	
	
	public PatchForm() {
		
		String title = "BBPatchHelper";
		if (CConst.version != null && CConst.version.length() > 0) {
			title = title + "_" + CConst.version;
		}
		
		final BasicForm bForm = new BasicForm(CConst.winWidth, CConst.winHeight, title);
		
		bForm.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.out.println("사용자 명령으로 종료합니다.");
				System.exit(0);
			}
		});
		
		int width = 560;
		int left = 10;
		
		// 대상폴더
		bForm.addLabel(left, top, width, 30, "대상 폴더 (비워도 됨)");
		plusTop(1);
		targetFolderText = bForm.addTextInput(left, top, 560, 25);
		targetFolderText.setText(CConst.targetDir);
		
		plusTop(1);
		
		// 클래스폴더
		bForm.addLabel(left, top, width, 30, "클래스 폴더");
		plusTop(1);
		classFolderText = bForm.addTextInput(left, top, 560, 25);
		classFolderText.setText(CConst.classDir);
		
		plusTop(1);
		
		// 대상파일
		bForm.addLabel(left, top, width, 30, "대상 파일");
		
		JButton reviseButton = bForm.addButton(80, top + 4, 79, 20, "REVISE");
		reviseButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				MainController mainCtrl = new MainController();
				mainCtrl.reviseButtonClicked();
			}
		});
		
		lineLabel = bForm.addLabel(width - 235, top, 200, 30, "");
		// 우측정렬
		lineLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		
		// 대상파일 TextArea
		plusTop(1);
		targetPathList = bForm.addTextArea(left, top, width, 170);
		targetPathScrollPane = bForm.addScrollPane(targetPathList, left, top, width, 170);
		
		targetPathList.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				// 라인현황 업데이트
				updateLineCountLabel();
				
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				// 라인현황 업데이트
				updateLineCountLabel();
				
			}
		});
		
		targetPathList.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				// 라인현황 업데이트
				updateLineCountLabel();
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// 라인현황 업데이트
				updateLineCountLabel();
			}
		});
		
		plusTop(6);
		plusTopLittle(1);
		plusTopLittle(1);
	
		javaToClassCheckBox = bForm.addCheckBox(left, top, width, 30, ".java 대신 .class 가져오기");
		javaToClassCheckBox.setSelected(CConst.bJavaToClass);
		
		plusTop(1);
		
		// 복사금지 패턴
		forbiddenFileLabel = bForm.addLabel(left, top, width, 30, "복사금지 패턴");
		plusTop(1);
		forbiddenFileText = bForm.addTextInput(left, top, width, 25);
		forbiddenFileText.setText(CConst.forbiddenFile);
		
		plusTop(1);
		// 결과폴더
		destDirLabel = bForm.addLabel(left, top, width, 30, "결과 폴더");
		plusTop(1);
		destDirText = bForm.addTextInput(left, top, width, 25);
		destDirText.setText(getNotExistingDestDirPath());
		
		plusTop(1);
		plusTopLittle(1);
		copyButton = bForm.addButton(left, top, width, 30, "COPY");
		copyButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				MainController mainCtrl = new MainController();
				mainCtrl.executePatch();
			}
		});
		
		bForm.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent evt) {
				resizeForm(bForm.getWidth(), bForm.getHeight());
	        }
		});
		
		bForm.open();
	}
	
	
	/**
	 * 라인현황 업데이트
	 */
	public static void updateLineCountLabel() {
		
		try {
			// 현재 커서가 위치한 곳이 몇 번째 줄인지 알아내기
		    int currentLineIndex = targetPathList.getLineOfOffset(targetPathList.getCaretPosition()) + 1;
		    
		    // 전체 몇 줄인지 알아내기
			int totalLineCount = targetPathList.getLineCount();
			
			// 라인 현황 표시
			lineLabel.setText(currentLineIndex + " / " + totalLineCount);
		
		} catch (Exception e) {}
	}
	
	
	private static void resizeForm(int formWidth, int formHeight) {
		int topToAdd = formHeight - CConst.winHeight; // 520
		if (topToAdd < 0) {
			topToAdd = 0;
		}
		
		int newWidth = formWidth - 40;
		
		// 대상폴더 인풋박스
		targetFolderText.setSize(newWidth, targetFolderText.getHeight());
		
		// 클래스폴더 인풋박스
		classFolderText.setSize(newWidth, classFolderText.getHeight());
		
		// 라인현황 레이블
		lineLabel.setBounds(formWidth - 235, lineLabel.getY(), lineLabel.getWidth(), lineLabel.getHeight());
		
		// 대상파일 텍스트영역 2
		try {
			int newHeight = 155;
			newHeight = newHeight + topToAdd;
			
			if (newHeight < 155) {
				newHeight = 155;
			}
						
			// 대상파일 텍스트영역 1
			targetPathList.setSize(newWidth, newHeight);
			targetPathScrollPane.setSize(newWidth, newHeight);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 자바대신클래스 가져오기
		javaToClassCheckBox.setBounds(javaToClassCheckBox.getX(), 280 + topToAdd, javaToClassCheckBox.getWidth(), javaToClassCheckBox.getHeight());
		
		// 복사금지 라벨
		forbiddenFileLabel.setBounds(forbiddenFileLabel.getX(), 305 + topToAdd, forbiddenFileLabel.getWidth(), forbiddenFileLabel.getHeight());
		
		// 복사금지 인풋박스
		forbiddenFileText.setBounds(forbiddenFileText.getX(), 330 + topToAdd, newWidth, forbiddenFileText.getHeight());
		
		// 결과폴더 라벨
		destDirLabel.setBounds(destDirLabel.getX(), 355 + topToAdd, destDirLabel.getWidth(), destDirLabel.getHeight());
		
		// 결과폴더 인풋박스
		destDirText.setBounds(destDirText.getX(), 380 + topToAdd, newWidth, destDirText.getHeight());
		
		// 카피 버튼
		copyButton.setBounds(copyButton.getX(), 420 + topToAdd, newWidth, copyButton.getHeight());
	}
	
	
	/**
	 * 아직 디스크에 존재하지 않는 목적폴더의 경로를 생성한다.
	 * @return
	 */
	private String getNotExistingDestDirPath() {
		
		String prefix = CConst.destDir + "/" + DatetimeUtil.getTodayDate().substring(2);
		prefix = StringUtil.revisePath(prefix);
		
		int num = 0;
		String numStr = "";
		
		File dir = null;
		boolean inLoop = true;
		
		while(inLoop) {
			if (num == 0) {
				numStr = "";
			} else if (num < 10) {
				numStr = "_0" + num;
			} else {
				numStr = "_" + num;
			}
			
			dir = new File(prefix + numStr);
			if (dir.exists()) {
				num++;
				continue;
			}
			
			// 존재하지 않을 경우
			inLoop = false;
			break;
		}
	
		return StringUtil.revisePath(dir.getAbsolutePath());
	}
}
