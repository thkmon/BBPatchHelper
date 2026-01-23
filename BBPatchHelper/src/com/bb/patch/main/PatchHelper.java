package com.bb.patch.main;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.bb.patch.form.PatchForm;
import com.formdev.flatlaf.FlatLightLaf;


public class PatchHelper {

	public static void main(String[] args) {

		System.out.println("실행");
		
		// FlatLaf Look and Feel 설정
		try {
			// 모든 컴포넌트를 각지게 만들기 (arc = 0)
			UIManager.put("Button.arc", 0);
			UIManager.put("Component.arc", 0);
			UIManager.put("CheckBox.arc", 0);
			UIManager.put("RadioButton.arc", 0);
			UIManager.put("ComboBox.arc", 0);
			UIManager.put("ProgressBar.arc", 0);
			UIManager.put("TextComponent.arc", 0);
			UIManager.put("ScrollBar.arc", 0);
			UIManager.put("TabbedPane.tabArc", 0);
			
			FlatLightLaf.setup();
		} catch (Exception e) {
			System.err.println("FlatLaf 초기화 실패: " + e.getMessage());
			e.printStackTrace();
		}
		
		// 초기화
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				PatchForm patchForm = new PatchForm();
			}
		});
	}
}
