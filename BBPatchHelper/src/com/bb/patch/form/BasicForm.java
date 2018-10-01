package com.bb.patch.form;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.bb.patch.common.CConst;

public class BasicForm extends JFrame {
	
	
	private ArrayList<Component> componentList = null;
	private Container container = null;
	private Font font = null;
	
	
	public BasicForm(int width, int height, String title) {
		componentList = new ArrayList<Component>();
		container = getContentPane();
		container.setLayout(null);
		setSize(width, height);
		setBounds(200, 200, width, height);
		
		setBackground(CConst.formBackgroundColor);
		container.setBackground(CConst.formBackgroundColor);
		
		setTitle(title);		
		font = new Font("굴림", 13, 13);
		
//		this.addWindowListener(new WindowAdapter() {
//			public void windowClosing(WindowEvent e) {
//				System.out.println("사용자 명령으로 종료합니다.");
//				System.exit(0);
//			}
//		});
		
//		this.addMouseMotionListener(new MouseMotionListener() {
//			@Override
//			public void mouseMoved(MouseEvent e) {
//				setCursor(Cursor.DEFAULT_CURSOR);
//			}
//			
//			@Override
//			public void mouseDragged(MouseEvent e) {
//			}
//		});
	}
	
	
	public void open() {
		setVisible(true);
	}
	
	
	public void close() {
		setVisible(false);
	}
	
	
	public JTextArea addTextArea(int left, int top, int width, int height) {

		JTextArea obj = new JTextArea();
		obj.setBackground(Color.white);
		obj.setBounds(left, top, width, height);
		obj.setFont(font);

		JScrollPane scrollPane = new JScrollPane(obj);
		scrollPane.setBackground(Color.white);
		scrollPane.setBounds(left, top, width, height);
		
//		obj.addMouseMotionListener(new MouseMotionListener() {
//			@Override
//			public void mouseMoved(MouseEvent e) {
//				setCursor(Cursor.TEXT_CURSOR);
//			}
//			
//			@Override
//			public void mouseDragged(MouseEvent e) {
//			}
//		});
		
		addComponent(scrollPane);
		return obj;
	}
	
	
	public JTextField addTextInput(int left, int top, int width, int height) {
		JTextField obj = new JTextField();
		obj.setBackground(Color.white);
		obj.setBounds(left, top, width, height);
		obj.setFont(font);
		
		addComponent(obj);
		return obj;
	}
	
	
	public JLabel addLabel(int left, int top, int width, int height, String value) {
		JLabel obj = new JLabel();
		obj.setBackground(Color.white);
		obj.setBounds(left, top, width, height);
		obj.setText(value);
		obj.setFont(font);
		
//		obj.addMouseMotionListener(new MouseMotionListener() {
//			@Override
//			public void mouseMoved(MouseEvent e) {
//				setCursor(Cursor.DEFAULT_CURSOR);
//			}
//			
//			@Override
//			public void mouseDragged(MouseEvent e) {
//			}
//		});
		
		addComponent(obj);
		return obj;
	}
	
	
	public JButton addButton(int left, int top, int width, int height, String value) {
		JButton obj = new JButton();
		obj.setBackground(CConst.buttonColor);
		obj.setBounds(left, top, width, height);
		obj.setText(value);
		obj.setFont(font);
		obj.setForeground(CConst.buttonTextColor);
		
//		obj.addMouseMotionListener(new MouseMotionListener() {
//			@Override
//			public void mouseMoved(MouseEvent e) {
//				setCursor(Cursor.HAND_CURSOR);
//			}
//			
//			@Override
//			public void mouseDragged(MouseEvent e) {
//			}
//		});
		
		addComponent(obj);
		return obj;
	}
	
	
	public JCheckBox addCheckBox(int left, int top, int width, int height, String value) {
		JCheckBox checkBox = new JCheckBox();
		checkBox.setBackground(CConst.formBackgroundColor);
		checkBox.setBounds(left, top, width, height);
		checkBox.setText(value);
		checkBox.setFont(font);
		
//		checkBox.addMouseMotionListener(new MouseMotionListener() {
//			@Override
//			public void mouseMoved(MouseEvent e) {
//				setCursor(Cursor.HAND_CURSOR);
//			}
//			
//			@Override
//			public void mouseDragged(MouseEvent e) {
//			}
//		});
		
		addComponent(checkBox);
		return checkBox;
	}
	
	
	private void addComponent(Component comp) {
		container.add(comp);
		componentList.add(comp);
	}
	
	
	public Component getComponent(int index) {
		if (componentList == null || componentList.size() == 0) {
			return null;
		}
		
		if (index < componentList.size() - 1) {
			return null;
		}
		
		return componentList.get(index);
	}

}
