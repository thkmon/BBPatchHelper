package com.bb.patch.main;

import javax.swing.SwingUtilities;

import com.bb.patch.form.PatchForm;
import com.formdev.flatlaf.FlatLightLaf;


public class PatchHelper {

	public static void main(String[] args) {

		System.out.println("실행");
		
		// FlatLaf Look and Feel 설정
		try {
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
