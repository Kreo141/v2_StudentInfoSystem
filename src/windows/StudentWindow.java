package windows;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import functions.Password;
import main.ApplicationConfig;

public class StudentWindow extends JFrame {

    private static final String STUDENT_FILE = ApplicationConfig.studentRecords;

    private final Password passwordUtil = new Password();
    private final String email;
    private final Runnable onLogout;

    // student data loaded from file
    private String studentId;
    private String studentName;
    private String studentEmail;
    private String studentProgram;
    private String studentCollege;
    private String studentYearLevel;
    private String studentHashedPassword;

    // info display labels
    private JLabel idValueLabel      = new JLabel("—");
    private JLabel nameValueLabel    = new JLabel("—");
    private JLabel emailValueLabel   = new JLabel("—");
    private JLabel programValueLabel = new JLabel("—");
    private JLabel collegeValueLabel   = new JLabel("—");
    private JLabel yearLevelValueLabel = new JLabel("—");

    public StudentWindow(String email, Runnable onLogout) {
        this.email    = email;
        this.onLogout = onLogout;

        setTitle("Student Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        if (!loadStudentByEmail(email)) {
            JOptionPane.showMessageDialog(null,
                "No student found with email: " + email,
                "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        buildUI();
    }

    // load student from file

    private boolean loadStudentByEmail(String targetEmail) {
        try (BufferedReader r = new BufferedReader(new FileReader(STUDENT_FILE))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length >= 7 && p[2].trim().equalsIgnoreCase(targetEmail.trim())) {
                    studentId             = p[0];
                    studentName           = p[1];
                    studentEmail          = p[2];
                    studentProgram        = p[3];
                    studentCollege        = p[4];
                    studentYearLevel      = p[5];
                    studentHashedPassword = p[6];
                    return true;
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                "Error reading student records: " + e.getMessage(),
                "File Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    // build ui

    private void buildUI() {
        // top bar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setForeground(Color.RED);
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                if (onLogout != null) onLogout.run();
            }
        });
        topBar.add(logoutBtn);

        // center card
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(40, 120, 40, 120),
            BorderFactory.createTitledBorder("Student Information")
        ));

        // title
        JLabel titleLabel = new JLabel("Welcome, " + studentName);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(20));

        // info grid
        JPanel infoGrid = new JPanel(new GridLayout(6, 2, 10, 12));
        infoGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        Font labelFont = new Font("Arial", Font.BOLD, 14);
        Font valueFont = new Font("Arial", Font.PLAIN, 14);

        idValueLabel.setText(studentId);
        nameValueLabel.setText(studentName);
        emailValueLabel.setText(studentEmail);
        programValueLabel.setText(studentProgram);
        collegeValueLabel.setText(studentCollege);
        yearLevelValueLabel.setText(studentYearLevel);

        for (JLabel lbl : new JLabel[]{idValueLabel, nameValueLabel,
                emailValueLabel, programValueLabel, collegeValueLabel, yearLevelValueLabel}) {
            lbl.setFont(valueFont);
        }

        addInfoRow(infoGrid, "Student ID:",  idValueLabel,      labelFont);
        addInfoRow(infoGrid, "Full Name:",   nameValueLabel,    labelFont);
        addInfoRow(infoGrid, "Email:",       emailValueLabel,   labelFont);
        addInfoRow(infoGrid, "Program:",     programValueLabel, labelFont);
        addInfoRow(infoGrid, "College:",     collegeValueLabel,   labelFont);
        addInfoRow(infoGrid, "Year Level:", yearLevelValueLabel, labelFont);

        card.add(infoGrid);
        card.add(Box.createVerticalStrut(30));

        // change password button
        JButton changePassBtn = new JButton("Change Password");
        changePassBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        changePassBtn.setFont(new Font("Arial", Font.BOLD, 13));
        changePassBtn.addActionListener(e -> changePassword());
        card.add(changePassBtn);

        // wrap card in scroll pane
        JScrollPane scroll = new JScrollPane(card);
        scroll.setBorder(null);

        add(topBar,  BorderLayout.NORTH);
        add(scroll,  BorderLayout.CENTER);
    }

    private void addInfoRow(JPanel grid, String labelText, JLabel valueLabel, Font labelFont) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(labelFont);
        grid.add(lbl);
        grid.add(valueLabel);
    }

    // change password

    private void changePassword() {
        JPasswordField oldPassField     = new JPasswordField();
        JPasswordField newPassField     = new JPasswordField();
        JPasswordField confirmPassField = new JPasswordField();

        Object[] fields = {
            "Current Password:",      oldPassField,
            "New Password:",          newPassField,
            "Confirm New Password:",  confirmPassField
        };

        int result = JOptionPane.showConfirmDialog(
            this, fields, "Change Password",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        String oldPass  = new String(oldPassField.getPassword()).trim();
        String newPass  = new String(newPassField.getPassword()).trim();
        String conPass  = new String(confirmPassField.getPassword()).trim();

        if (oldPass.isEmpty() || newPass.isEmpty() || conPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All password fields are required.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!passwordUtil.verifyPassword(oldPass, studentHashedPassword)) {
            JOptionPane.showMessageDialog(this, "Current password is incorrect.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!newPass.equals(conPass)) {
            JOptionPane.showMessageDialog(this, "New passwords do not match.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (newPass.equals(oldPass)) {
            JOptionPane.showMessageDialog(this,
                "New password must be different from the current password.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String newHash = passwordUtil.hashPassword(newPass);
        studentHashedPassword = newHash;
        updatePasswordInFile(newHash);

        JOptionPane.showMessageDialog(this, "Password changed successfully.",
            "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    // update password in file

    private void updatePasswordInFile(String newHash) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new FileReader(STUDENT_FILE))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length >= 7 && p[2].trim().equalsIgnoreCase(studentEmail.trim())) {
                    line = p[0] + "|" + p[1] + "|" + p[2] + "|"
                         + p[3] + "|" + p[4] + "|" + p[5] + "|" + newHash;
                }
                lines.add(line);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Error reading file: " + e.getMessage(),
                "File Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (BufferedWriter w = new BufferedWriter(new FileWriter(STUDENT_FILE))) {
            for (String line : lines) {
                w.write(line);
                w.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Error saving file: " + e.getMessage(),
                "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // entry point (for testing)
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() ->
//            new StudentWindow("brian.peralta75@email.com", null).setVisible(true));
//    }
}