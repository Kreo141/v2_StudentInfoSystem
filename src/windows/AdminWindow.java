package windows;

import java.awt.CardLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import functions.Password;
import main.ApplicationConfig;

public class AdminWindow extends JFrame {

    // session info
    private final String privilege;
    private final String signedInAs;
    private final Runnable onLogout;

    // shared utilities
    private final Password passwordUtil = new Password();
    private static final DateTimeFormatter LOG_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // file paths
    private static final String STUDENT_FILE = ApplicationConfig.studentRecords;
    private static final String ADMIN_FILE   = ApplicationConfig.adminCreds;
    private static final String LOG_FILE     = ApplicationConfig.LOG_FILE;


    private static final String[] COLLEGES = {
        "College of Arts and Sciences Education",
        "College of Architecture and Fine Arts Education",
        "College of Computing Education",
        "College of Business Administration Education",
        "College of Accounting Education",
        "College of Criminal Justice Education",
        "College of Engineering",
        "Professional Schools"
    };

    private static final String[][] PROGRAMS = {
        { "BA Communication","BA English Language","BA Political Science","BS Agroforestry",
          "BS Biology","BS Environmental Science","BS Forestry","BS Psychology","BS Social Work" },
        { "BS Architecture","Bachelor of Fine Arts and Design Major in Painting","BS Interior Design" },
        { "BS Computer Science","BS Information Technology",
          "BS Entertainment and Multimedia Computing - Game Development",
          "BS Entertainment and Multimedia Computing - Digital Animation Technology",
          "Bachelor of Multimedia Arts","Bachelor of Library and Information Science" },
        { "BS Entrepreneurship","BS Business Administration - Financial Management",
          "BS Business Administration - Marketing Management" },
        { "BS Accountancy","BS Accounting Information System","BS Management Accounting" },
        { "BS Criminology" },
        { "BS Chemical Engineering","BS Electronics Engineering","BS Mechanical Engineering",
          "BS Electrical Engineering","BS Computer Engineering","BS Civil Engineering" },
        { "Master in Information Technology","Master in Information Systems","Master in Criminal Justice" }
    };

    private static final String[] YEAR_LEVELS = {
        "1st Year","2nd Year","3rd Year","4th Year","5th Year","Irregular"
    };


    // top nav
    private JPanel cardButtonContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        private JButton manageStudentBtn = new JButton("Manage Student");
        private JButton manageAdminBtn   = new JButton("Manage Admin");
        private JLabel  signedInLabel    = new JLabel();
        private JButton logoutBtn        = new JButton("Logout");

    private CardLayout cardLayout = new CardLayout();
    private JPanel container = new JPanel(cardLayout);

    // manage student card
    private JPanel msPanel  = new JPanel(new BorderLayout());
        private JPanel msTop = new JPanel(new GridLayout(1, 2));
            private JTextField searchField = new JTextField();
        private JPanel msCenter = new JPanel(new GridLayout(1, 2));
            JScrollPane  studentTableScroll;
            DefaultTableModel studentModel;
            JTable        studentTable;
            TableRowSorter<DefaultTableModel> studentSorter;
            JPanel msCenterRight = new JPanel(new GridLayout(2, 1));
                JPanel msCenterRightTop = new JPanel(new GridLayout(6, 2));
                    JLabel idPlaceholder      = new JLabel("—");
                    JLabel namePlaceholder    = new JLabel("—");
                    JLabel emailPlaceholder   = new JLabel("—");
                    JLabel programPlaceholder = new JLabel("—");
                    JLabel collegePlaceholder = new JLabel("—");
                    JLabel yearLevelPlaceholder = new JLabel("—");
        private JPanel msBot = new JPanel(new FlowLayout());
            private JButton addStudentBtn    = new JButton("Add Student");
            private JButton editStudentBtn   = new JButton("Edit");
            private JButton deleteStudentBtn = new JButton("Delete");

    // 0=id,1=name,2=email,3=program,4=college,5=yearLevel,6=password
    String[] selectedStudentData = new String[7];

    // manage admin card — adminRecords.txt format: username|privilege|hashedPassword
    private JPanel maPanel  = new JPanel(new BorderLayout());
            JScrollPane  adminTableScroll;
            DefaultTableModel adminModel;
            JTable        adminTable;
            TableRowSorter<DefaultTableModel> adminSorter;
        private JPanel maBot = new JPanel(new FlowLayout());
            private JButton addAdminBtn    = new JButton("Add Admin");
            private JButton editAdminBtn   = new JButton("Edit Admin");
            private JButton deleteAdminBtn = new JButton("Delete Admin");

    // 0=username, 1=privilege, 2=hashedPassword
    String[] selectedAdminData = new String[3];

    // constructor
    public AdminWindow(String privilege, String signedInAs, Runnable onLogout) {
        this.privilege  = privilege;
        this.signedInAs = signedInAs;
        this.onLogout   = onLogout;

        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setTitle("Admin Panel — " + signedInAs + " (" + privilege + ")");

        buildStudentCard();
        buildAdminCard();
        buildNav();

        add(cardButtonContainer, BorderLayout.NORTH);
        add(container, BorderLayout.CENTER);

        container.add(msPanel, "Manage Student");
        container.add(maPanel, "Manage Admin");

        cardLayout.show(container, "Manage Student");
    }

    // nav bar
    private void buildNav() {
        signedInLabel.setText("  Signed in as: " + signedInAs + "  |  " + privilege);
        signedInLabel.setFont(new Font("Arial", Font.ITALIC, 13));

        manageStudentBtn.addActionListener(e -> cardLayout.show(container, "Manage Student"));
        manageAdminBtn.addActionListener(e   -> cardLayout.show(container, "Manage Admin"));

        // non-super admins cannot access manage admin
        if (!"Super Admin".equals(privilege)) {
            manageAdminBtn.setEnabled(false);
            manageAdminBtn.setToolTipText("Only Super Admins can manage admin accounts.");
        }

        logoutBtn.setForeground(Color.RED);
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this, "Are you sure you want to logout?",
                "Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                if (onLogout != null) onLogout.run();
            }
        });

        cardButtonContainer.add(manageStudentBtn);
        cardButtonContainer.add(manageAdminBtn);
        cardButtonContainer.add(signedInLabel);
        cardButtonContainer.add(logoutBtn);
    }

    // build manage student card
    private void buildStudentCard() {
        searchField.setFont(new Font("Arial", Font.BOLD, 18));

        // model: ID | NAME | EMAIL | Program | College | PASSWORD
        studentModel = new DefaultTableModel(
                new String[]{"ID", "NAME", "EMAIL", "Program", "College", "YEAR_LEVEL", "PASSWORD"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        studentTable  = new JTable(studentModel);
        studentSorter = new TableRowSorter<>(studentModel);
        studentTable.setRowSorter(studentSorter);
        studentTableScroll = new JScrollPane(studentTable);

        hideCol(studentTable, 3);
        hideCol(studentTable, 4);
        hideCol(studentTable, 5);
        hideCol(studentTable, 6);

        loadStudentsFromFile();

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void search() {
                String t = searchField.getText();
                studentSorter.setRowFilter(t.trim().isEmpty()
                        ? null : RowFilter.regexFilter("(?i)" + t));
            }
            public void insertUpdate(DocumentEvent e)  { search(); }
            public void removeUpdate(DocumentEvent e)  { search(); }
            public void changedUpdate(DocumentEvent e) { search(); }
        });

        studentTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int vr = studentTable.getSelectedRow();
                if (vr != -1) {
                    int mr = studentTable.convertRowIndexToModel(vr);
                    for (int i = 0; i < 7; i++)
                        selectedStudentData[i] = studentModel.getValueAt(mr, i).toString();
                    loadStudentPlaceholder();
                }
            }
        });

        addStudentBtn.addActionListener(e    -> addStudent());
        editStudentBtn.addActionListener(e   -> editStudent());
        deleteStudentBtn.addActionListener(e -> deleteStudent());

        // layout
        msPanel.add(msTop,    BorderLayout.NORTH);
        msPanel.add(msCenter, BorderLayout.CENTER);
        msPanel.add(msBot,    BorderLayout.SOUTH);

        msTop.add(searchField);
        msTop.add(new JPanel());

        msCenter.add(studentTableScroll);
        msCenter.add(msCenterRight);

        msCenterRight.add(msCenterRightTop);
        msCenterRight.add(new JPanel());

        msCenterRightTop.add(new JLabel("ID:"));      msCenterRightTop.add(idPlaceholder);
        msCenterRightTop.add(new JLabel("Name:"));    msCenterRightTop.add(namePlaceholder);
        msCenterRightTop.add(new JLabel("Email:"));   msCenterRightTop.add(emailPlaceholder);
        msCenterRightTop.add(new JLabel("Program:")); msCenterRightTop.add(programPlaceholder);
        msCenterRightTop.add(new JLabel("College:")); msCenterRightTop.add(collegePlaceholder);
        msCenterRightTop.add(new JLabel("Year Level:")); msCenterRightTop.add(yearLevelPlaceholder);

        msBot.add(addStudentBtn);
        msBot.add(editStudentBtn);
        msBot.add(deleteStudentBtn);
    }

    // build manage admin card
    private void buildAdminCard() {
        // model: USERNAME | PRIVILEGE | PASSWORD(hidden)
        adminModel = new DefaultTableModel(
                new String[]{"USERNAME", "PRIVILEGE", "PASSWORD"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        adminTable  = new JTable(adminModel);
        adminSorter = new TableRowSorter<>(adminModel);
        adminTable.setRowSorter(adminSorter);
        adminTableScroll = new JScrollPane(adminTable);

        hideCol(adminTable, 2); // hide password column

        loadAdminsFromFile();

        adminTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int vr = adminTable.getSelectedRow();
                if (vr != -1) {
                    int mr = adminTable.convertRowIndexToModel(vr);
                    for (int i = 0; i < 3; i++)
                        selectedAdminData[i] = adminModel.getValueAt(mr, i).toString();
                }
            }
        });

        addAdminBtn.addActionListener(e    -> addAdmin());
        editAdminBtn.addActionListener(e   -> editAdmin());
        deleteAdminBtn.addActionListener(e -> deleteAdmin());

        // layout
        maPanel.add(adminTableScroll, BorderLayout.CENTER);
        maPanel.add(maBot,            BorderLayout.SOUTH);

        maBot.add(addAdminBtn);
        maBot.add(editAdminBtn);
        maBot.add(deleteAdminBtn);
    }

    // student crud

    private void addStudent() {
        JTextField idField    = new JTextField();
        JTextField nameField  = new JTextField();
        JTextField emailField = new JTextField();

        JComboBox<String> collegeBox  = new JComboBox<>(COLLEGES);
        JComboBox<String> programBox  = new JComboBox<>(PROGRAMS[0]);
        JComboBox<String> yearBox     = new JComboBox<>(YEAR_LEVELS);

        // update program list when college changes
        collegeBox.addActionListener(e -> {
            programBox.removeAllItems();
            int idx = collegeBox.getSelectedIndex();
            for (String p : PROGRAMS[idx]) programBox.addItem(p);
        });

        Object[] fields = {
            "Student ID:",           idField,
            "Name (Last, First M):", nameField,
            "Email:",                emailField,
            "College:",              collegeBox,
            "Program:",              programBox,
            "Year Level:",           yearBox
        };

        int res = JOptionPane.showConfirmDialog(this, fields, "Add New Student",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        String id        = idField.getText().trim();
        String name      = nameField.getText().trim();
        String email     = emailField.getText().trim();
        String college   = collegeBox.getSelectedItem().toString();
        String program   = programBox.getSelectedItem().toString();
        String yearLevel = yearBox.getSelectedItem().toString();

        if (id.isEmpty() || name.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID, Name, and Email are required.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        for (int i = 0; i < studentModel.getRowCount(); i++) {
            if (studentModel.getValueAt(i, 0).toString().equals(id)) {
                JOptionPane.showMessageDialog(this, "Student ID already exists.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String hashed = passwordUtil.hashPassword("123");
        studentModel.addRow(new String[]{id, name, email, program, college, yearLevel, hashed});
        saveStudentsToFile();
        writeLog("ADD STUDENT", "ID=" + id + " | Name=" + name + " | College=" + college + " | Program=" + program + " | Year=" + yearLevel);
        JOptionPane.showMessageDialog(this,
                "Student added successfully!\nDefault password: 123",
                "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void editStudent() {
        int vr = studentTable.getSelectedRow();
        if (vr == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to edit.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int mr = studentTable.convertRowIndexToModel(vr);

        String curCollege   = studentModel.getValueAt(mr, 4).toString();
        String curProgram   = studentModel.getValueAt(mr, 3).toString();
        String curYearLevel = studentModel.getValueAt(mr, 5).toString();

        JTextField idField    = new JTextField(studentModel.getValueAt(mr, 0).toString());
        JTextField nameField  = new JTextField(studentModel.getValueAt(mr, 1).toString());
        JTextField emailField = new JTextField(studentModel.getValueAt(mr, 2).toString());
        idField.setEditable(false);

        JComboBox<String> collegeBox = new JComboBox<>(COLLEGES);
        JComboBox<String> programBox = new JComboBox<>();
        JComboBox<String> yearBox    = new JComboBox<>(YEAR_LEVELS);

        // pre-select current college
        int collegeIdx = 0;
        for (int i = 0; i < COLLEGES.length; i++) {
            if (COLLEGES[i].equals(curCollege)) { collegeIdx = i; break; }
        }
        collegeBox.setSelectedIndex(collegeIdx);
        for (String p : PROGRAMS[collegeIdx]) programBox.addItem(p);
        programBox.setSelectedItem(curProgram);
        yearBox.setSelectedItem(curYearLevel);

        collegeBox.addActionListener(e -> {
            programBox.removeAllItems();
            int idx = collegeBox.getSelectedIndex();
            for (String p : PROGRAMS[idx]) programBox.addItem(p);
        });

        JPasswordField newPassField = new JPasswordField();
        JPasswordField conPassField = new JPasswordField();

        Object[] fields = {
            "Student ID (read-only):",      idField,
            "Name:",                        nameField,
            "Email:",                       emailField,
            "College:",                     collegeBox,
            "Program:",                     programBox,
            "Year Level:",                  yearBox,
            "New Password (blank = keep):", newPassField,
            "Confirm New Password:",        conPassField
        };

        int res = JOptionPane.showConfirmDialog(this, fields, "Edit Student",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        String name      = nameField.getText().trim();
        String email     = emailField.getText().trim();
        String college   = collegeBox.getSelectedItem().toString();
        String program   = programBox.getSelectedItem().toString();
        String yearLevel = yearBox.getSelectedItem().toString();
        String newPass   = new String(newPassField.getPassword()).trim();
        String conPass   = new String(conPassField.getPassword()).trim();

        if (name.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and Email are required.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String finalHash;
        boolean passChanged = false;
        if (newPass.isEmpty()) {
            finalHash = studentModel.getValueAt(mr, 6).toString();
        } else {
            if (!newPass.equals(conPass)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            finalHash   = passwordUtil.hashPassword(newPass);
            passChanged = true;
        }

        String oldName = studentModel.getValueAt(mr, 1).toString();
        studentModel.setValueAt(name,      mr, 1);
        studentModel.setValueAt(email,     mr, 2);
        studentModel.setValueAt(program,   mr, 3);
        studentModel.setValueAt(college,   mr, 4);
        studentModel.setValueAt(yearLevel, mr, 5);
        studentModel.setValueAt(finalHash, mr, 6);

        selectedStudentData[1] = name;
        selectedStudentData[2] = email;
        selectedStudentData[3] = program;
        selectedStudentData[4] = college;
        selectedStudentData[5] = yearLevel;
        selectedStudentData[6] = finalHash;
        loadStudentPlaceholder();

        saveStudentsToFile();
        writeLog("EDIT STUDENT",
                "ID=" + studentModel.getValueAt(mr, 0)
                + " | OldName=" + oldName + " | NewName=" + name
                + " | College=" + college + " | Program=" + program + " | Year=" + yearLevel
                + (passChanged ? " | PasswordChanged=YES" : ""));
        JOptionPane.showMessageDialog(this, "Student updated successfully.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteStudent() {
        int vr = studentTable.getSelectedRow();
        if (vr == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to delete.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int mr = studentTable.convertRowIndexToModel(vr);
        String id   = studentModel.getValueAt(mr, 0).toString();
        String name = studentModel.getValueAt(mr, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete:\n" + name + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        studentModel.removeRow(mr);
        saveStudentsToFile();
        writeLog("DELETE STUDENT", "ID=" + id + " | Name=" + name);

        for (int i = 0; i < selectedStudentData.length; i++) selectedStudentData[i] = "";
        idPlaceholder.setText("—");
        namePlaceholder.setText("—");
        emailPlaceholder.setText("—");
        programPlaceholder.setText("—");
        collegePlaceholder.setText("—");
        yearLevelPlaceholder.setText("—");

        JOptionPane.showMessageDialog(this, "Student deleted successfully.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    // admin crud

    private void addAdmin() {
        JTextField     usernameField = new JTextField();
        JPasswordField passField     = new JPasswordField();
        JPasswordField conField      = new JPasswordField();
        JComboBox<String> privBox    = new JComboBox<>(new String[]{"Admin", "Super Admin"});

        Object[] fields = {
            "Username:",         usernameField,
            "Password:",         passField,
            "Confirm Password:", conField,
            "Privilege:",        privBox
        };

        int res = JOptionPane.showConfirmDialog(this, fields, "Add New Admin",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        String username = usernameField.getText().trim();
        String pass     = new String(passField.getPassword()).trim();
        String con      = new String(conField.getPassword()).trim();
        String priv     = privBox.getSelectedItem().toString();

        if (username.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password are required.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!pass.equals(con)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        for (int i = 0; i < adminModel.getRowCount(); i++) {
            if (adminModel.getValueAt(i, 0).toString().equalsIgnoreCase(username)) {
                JOptionPane.showMessageDialog(this, "Username already exists.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String hashed = passwordUtil.hashPassword(pass);
        adminModel.addRow(new String[]{username, priv, hashed});
        saveAdminsToFile();
        writeLog("ADD ADMIN", "Username=" + username + " | Privilege=" + priv);
        JOptionPane.showMessageDialog(this, "Admin added successfully.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void editAdmin() {
        int vr = adminTable.getSelectedRow();
        if (vr == -1) {
            JOptionPane.showMessageDialog(this, "Please select an admin to edit.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int mr = adminTable.convertRowIndexToModel(vr);

        String currentUsername = adminModel.getValueAt(mr, 0).toString();
        String currentPriv     = adminModel.getValueAt(mr, 1).toString();

        JTextField     usernameField = new JTextField(currentUsername);
        JPasswordField newPassField  = new JPasswordField();
        JPasswordField conPassField  = new JPasswordField();
        JComboBox<String> privBox    = new JComboBox<>(new String[]{"Admin", "Super Admin"});
        privBox.setSelectedItem(currentPriv);

        Object[] fields = {
            "Username:",                    usernameField,
            "New Password (blank = keep):", newPassField,
            "Confirm New Password:",        conPassField,
            "Privilege:",                   privBox
        };

        int res = JOptionPane.showConfirmDialog(this, fields, "Edit Admin",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        String newUsername = usernameField.getText().trim();
        String newPass     = new String(newPassField.getPassword()).trim();
        String conPass     = new String(conPassField.getPassword()).trim();
        String newPriv     = privBox.getSelectedItem().toString();

        if (newUsername.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username cannot be empty.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        for (int i = 0; i < adminModel.getRowCount(); i++) {
            if (i == mr) continue;
            if (adminModel.getValueAt(i, 0).toString().equalsIgnoreCase(newUsername)) {
                JOptionPane.showMessageDialog(this, "Username already taken.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String finalHash;
        boolean passChanged = false;
        if (newPass.isEmpty()) {
            finalHash = adminModel.getValueAt(mr, 2).toString();
        } else {
            if (!newPass.equals(conPass)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            finalHash   = passwordUtil.hashPassword(newPass);
            passChanged = true;
        }

        adminModel.setValueAt(newUsername, mr, 0);
        adminModel.setValueAt(newPriv,     mr, 1);
        adminModel.setValueAt(finalHash,   mr, 2);

        saveAdminsToFile();
        writeLog("EDIT ADMIN",
                "OldUsername=" + currentUsername + " | NewUsername=" + newUsername
                + " | Privilege=" + newPriv
                + (passChanged ? " | PasswordChanged=YES" : ""));
        JOptionPane.showMessageDialog(this, "Admin updated successfully.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteAdmin() {
        int vr = adminTable.getSelectedRow();
        if (vr == -1) {
            JOptionPane.showMessageDialog(this, "Please select an admin to delete.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int mr = adminTable.convertRowIndexToModel(vr);
        String targetUser = adminModel.getValueAt(mr, 0).toString();

        if (targetUser.equalsIgnoreCase(signedInAs)) {
            JOptionPane.showMessageDialog(this, "You cannot delete your own account.",
                    "Not Allowed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete admin:\n" + targetUser + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        adminModel.removeRow(mr);
        saveAdminsToFile();
        writeLog("DELETE ADMIN", "Username=" + targetUser);

        JOptionPane.showMessageDialog(this, "Admin deleted successfully.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    // helpers

    private void loadStudentPlaceholder() {
        idPlaceholder.setText(selectedStudentData[0]);
        namePlaceholder.setText(selectedStudentData[1]);
        emailPlaceholder.setText(selectedStudentData[2]);
        programPlaceholder.setText(selectedStudentData[3]);
        collegePlaceholder.setText(selectedStudentData[4]);
        yearLevelPlaceholder.setText(selectedStudentData[5]);
    }

    // zero-width hide for any JTable column
    private void hideCol(JTable t, int col) {
        t.getColumnModel().getColumn(col).setMinWidth(0);
        t.getColumnModel().getColumn(col).setMaxWidth(0);
        t.getColumnModel().getColumn(col).setWidth(0);
    }

    // file i/o — students — format: id|name|email|program|college|hashedPassword

    private void loadStudentsFromFile() {
        try (BufferedReader r = new BufferedReader(new FileReader(STUDENT_FILE))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length >= 7)
                    studentModel.addRow(new String[]{p[0], p[1], p[2], p[3], p[4], p[5], p[6]});
            }
        } catch (IOException e) {
            System.out.println("Note (students): " + e.getMessage());
        }
    }

    private void saveStudentsToFile() {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(STUDENT_FILE))) {
            for (int i = 0; i < studentModel.getRowCount(); i++) {
                w.write(studentModel.getValueAt(i, 0) + "|"
                      + studentModel.getValueAt(i, 1) + "|"
                      + studentModel.getValueAt(i, 2) + "|"
                      + studentModel.getValueAt(i, 3) + "|"
                      + studentModel.getValueAt(i, 4) + "|"
                      + studentModel.getValueAt(i, 5) + "|"
                      + studentModel.getValueAt(i, 6));
                w.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving students: " + e.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // file i/o — admins — format: username|privilege|hashedPassword
    private void loadAdminsFromFile() {
        try (BufferedReader r = new BufferedReader(new FileReader(ADMIN_FILE))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length >= 3)
                    adminModel.addRow(new String[]{p[0], p[1], p[2]});
            }
        } catch (IOException e) {
            System.out.println("Note (admins): " + e.getMessage());
        }
    }

    private void saveAdminsToFile() {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(ADMIN_FILE))) {
            for (int i = 0; i < adminModel.getRowCount(); i++) {
                w.write(adminModel.getValueAt(i, 0) + "|"
                      + adminModel.getValueAt(i, 1) + "|"
                      + adminModel.getValueAt(i, 2));
                w.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving admins: " + e.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // activity log — format: [YYYY-MM-DD HH:mm:ss] [adminUsername] ACTION — details

    private void writeLog(String action, String details) {
        String timestamp = LocalDateTime.now().format(LOG_FMT);
        String entry = "[" + timestamp + "] [" + signedInAs + "] " + action + " — " + details;
        try (BufferedWriter w = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            w.write(entry);
            w.newLine();
        } catch (IOException e) {
            System.err.println("Log write failed: " + e.getMessage());
        }
        System.out.println("LOG: " + entry);
    }

    // entry point (for testing)
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() ->
//            new AdminWindow("Super Admin", "admin1", null).setVisible(true));
//    }
}