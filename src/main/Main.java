package main;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;

import windows.AdminWindow;
import windows.LoginWindow;
import windows.StudentWindow;
import functions.Password;

public class Main {
	
	static LoginWindow loginWindow = new LoginWindow();
	
	
	public static void main(String[] args) {
		initializeFiles();
		
		loginWindow.setVisible(true);
		
		
		loginWindow.forgotPasswordLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
			}
			
		});
		
		loginWindow.adminLoginButton.addActionListener(e -> {
			String[] loginStatus = login("admin");
			if(loginStatus[2].equals("true")) {
				SwingUtilities.invokeLater(() ->
	            new AdminWindow(loginStatus[0], loginStatus[1], null).setVisible(true));
			}
		});
		
		loginWindow.studentLoginButton.addActionListener(e -> {
			String[] loginStatus = login("student");
			if(loginStatus[2].equals("true")) {
				System.out.println(loginStatus[0]);
				SwingUtilities.invokeLater(() ->
	            new StudentWindow(loginStatus[0], null).setVisible(true));
			}
		});
	}
	
	public static void initializeFiles() {
		try {
			File file = new File(ApplicationConfig.DIR);
			if(!file.isDirectory()) {
				file.mkdir();
			}
			
			file = new File(ApplicationConfig.studentRecords);
			if(!file.exists()) {
				file.createNewFile();
			}
			
			file = new File(ApplicationConfig.adminCreds);
			if(!file.exists()) {
				file.createNewFile();
				FileWriter writer = new FileWriter(file);
				writer.write("admin|Super Admin|IYGUJjiVnBUh3KLTJAFRqA==:ExQQ05noKGDM6d1cQsSNAyxIdGMMuwYnSoDDqmXgmYM=");
				writer.close();
			}
		
		}catch(IOException e) {
			System.out.println(e);
		}
	}
	
	public static String[] login(String loginMethod) {
		String result[] = new String[3];
		
		String username = loginWindow.usernameField.getText();
		String password = new String(loginWindow.passwordField.getPassword());
		
		String credentialFile = "";
		int usernamePosition = 0;
		int passwordPosition = 0;
		if(loginMethod.equals("student")) {
			credentialFile = ApplicationConfig.studentRecords;
			usernamePosition = 2;
			passwordPosition = 6;
		} else {
			credentialFile = ApplicationConfig.adminCreds;
			usernamePosition = 0;
			passwordPosition = 2;
		}
		
		boolean isAuthenticated = false;
		String[] lineSplit;
		try(BufferedReader reader = new BufferedReader(new FileReader(credentialFile))){
			String line;
			while((line = reader.readLine()) != null) {
				lineSplit = line.split("\\|");
				System.out.println(lineSplit[usernamePosition]);
				System.out.println(lineSplit[passwordPosition]);
				if(lineSplit[usernamePosition].equals(username)) {
					Password ops_password = new Password();
					isAuthenticated = ops_password.verifyPassword(password, lineSplit[passwordPosition]);
					if(isAuthenticated && loginMethod.equals("admin")) {
					    result[0] = lineSplit[1];
					    result[1] = lineSplit[0];
					    result[2] = "true";
					    break;
					} else {
						result[0] = lineSplit[2];
					    result[1] = null;
					    result[2] = "true";
					    break;
					}
				}
			}
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		if(isAuthenticated) {
			return result;
		} else {
			JOptionPane.showMessageDialog(null, "Incorrect username or password!");
		}
		
		return new String[] {"null", "null", "false"};
	}
}
