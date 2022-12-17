import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.*;
import javax.swing.event.*;
import java.util.ArrayList;
import java.util.Arrays;

public class ProgramManager extends JFrame {

    private JList<String> programList;
    private DefaultListModel<String> programModel;
    private JButton removeButton;

    public ProgramManager() {
        // Set up the window
        setTitle("Program Manager");
        setSize(800, 1000);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Create the program list and model
        programModel = new DefaultListModel<>();
        programList = new JList<>(programModel);
        programList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        programList.setLayoutOrientation(JList.VERTICAL);
        programList.setVisibleRowCount(-1);

        // Add a scroll pane to the program list
        JScrollPane scrollPane = new JScrollPane(programList);
        scrollPane.setPreferredSize(new Dimension(250, 80));

        // Create the remove button and add an action listener
        removeButton = new JButton("Remove");
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeProgram();
            }
        });

        // Add the program list and remove button to the window
        add(scrollPane, BorderLayout.CENTER);
        add(removeButton, BorderLayout.SOUTH);

        // Populate the program list
        System.out.println("Populating program list...");
        populateProgramList();
    }

    private void populateProgramList() {
        // Clear the current list
        programModel.clear();
    
        // Run the "dpkg" command to list all installed packages
        ArrayList<String> output = runCommand("sudo dpkg -l");
    
        // Skip the first 2 lines of the output
        for (int i = 0; i < 3; i++) {
            output.remove(0);
        }
    
        // Iterate through the output and extract the package information
        for (String line : output) {
            String[] parts = line.split("\\s+");
            if (parts.length >= 4) {
                String packageName = parts[1];
                String packageVersion = parts[2];
                String packageArchitecture = parts[3];
                String packageDescription = String.join(" ", Arrays.copyOfRange(parts, 4, parts.length));
                if (packageDescription.length() > 15) {
                    packageDescription = packageDescription.substring(0, 12) + "...";
                }
                String formattedString = String.format("%-30.30s %-30.30s %-30.30s %-30.30s", packageName, packageVersion, packageArchitecture, packageDescription);
                programModel.addElement(formattedString);
            }
        }
    }

    private void removeProgram() {
        // Get the selected program
        String program = programList.getSelectedValue();
        if (program == null) {
            // No program selected, do nothing
            return;
        }

        // Prompt the user to confirm the removal
        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove \n" + program,
                "Confirm Removal", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            // Run the "apt-get remove" command to remove the program
            runCommand("sudo apt-get remove -y " + program);

            // Update the program list
            populateProgramList();
        }
    }

    private ArrayList<String> runCommand(String command) {
        // run the command from linux in sudo mode
        ArrayList<String> output = new ArrayList<>();
        try {
            
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.add(line);
            }
            reader.close();
        } catch (IOException e) {
            
            e.printStackTrace();
        }
        return output;

        }
    

    public static void main(String[] args) {
        ProgramManager window = new ProgramManager();
        window.setVisible(true);
    }
}