package windows;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

public class LoginWindow extends JFrame{
	
	JPanel loginFormPanel = new JPanel();
		JLabel modeLoginLabel = new JLabel("<html><h1 style='font-size: 30px; font-family: Courier New; color: white;'>Log in</h1><html>");
		public JTextField usernameField = new JTextField(15);
		public JPasswordField passwordField = new JPasswordField(15);
		
		public JLabel forgotPasswordLabel = new JLabel("<html><u style='color: white;'>Forgot Password?</u><html>");
		JPanel btnPanel = new JPanel(new GridLayout(1,2));
		public JButton adminLoginButton = new JButton("Admin");
		public JButton studentLoginButton = new JButton("Student");
	
	GridBagConstraints gbc = new GridBagConstraints();
	
	
	public LoginWindow(){
		setLoginWindowProperties();
		
		add(loginFormPanel, BorderLayout.CENTER);
		
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5, 5, 5, 5);
		
		loginFormPanel.setLayout(new GridBagLayout());
		gbc.gridy = 0;
		loginFormPanel.add(modeLoginLabel, gbc);
		gbc.gridy = 1;
		loginFormPanel.add(usernameField, gbc);
		gbc.gridy = 2;
		loginFormPanel.add(passwordField, gbc);
		gbc.gridy = 3;
		loginFormPanel.add(forgotPasswordLabel, gbc);
		
		gbc.gridy = 4;
		loginFormPanel.add(btnPanel, gbc);
		
		btnPanel.add(adminLoginButton);
		btnPanel.add(studentLoginButton);
		
		
	/* 
		STYLES 
	*/
		// Login Form Panel
		loginFormPanel.setOpaque(false);
		
		// Mode Login Label
		modeLoginLabel.setHorizontalAlignment(SwingConstants.CENTER);
		
		// Fields
		usernameField.setFont(new Font("SansSerif", Font.PLAIN, 25));
		passwordField.setFont(new Font("SansSerif", Font.PLAIN, 25));
		
		// Button
		adminLoginButton.setPreferredSize(new Dimension(0,40));
		
		
	/*
	 	Event Listeners
	*/	
		// Forgot Password
		adminLoginButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
			}
		});
		
		
		adminLoginButton.addActionListener(e -> {
			
		});
		
		studentLoginButton.addActionListener(e -> {
			
		});
	}
	
	
	LoginWindowVariables my = new LoginWindowVariables();
	public void setLoginWindowProperties() {
		setTitle(my.TITLE);
		setPreferredSize(my.WINDOW_SIZE); setSize(my.WINDOW_SIZE);
		setLocationRelativeTo(null);
		setContentPane(new GradientPanel());
		setLayout(my.LAYOUT_MANAGER);
	}
}

class GradientPanel extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        GradientPaint gp = new GradientPaint(
                0, 0, Color.decode("#45ffca"),        // start color
                getWidth(), getHeight(), Color.decode("#c445ff") // end color
        );

        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }
}
