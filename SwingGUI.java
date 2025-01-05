import jxl.read.biff.BiffException;
import org.apache.pdfbox.pdmodel.font.*;
import org.jfree.chart.ChartFactory;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

import javax.swing.*;
import java.io.FileWriter;
import javax.swing.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import java.util.List;
import java.io.IOException;
import java.util.List;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SwingGUI extends JFrame {
    private PopulatieInteract interact;
    private String currentUser ;
    private String currentEthnicityFilter = null;
    private String currentCityFilter = null;
    private Integer currentMinAgeFilter = null;
    private Integer currentMaxAgeFilter = null;

    public SwingGUI(String currentUser ) throws IOException {
        this.currentUser  = currentUser ;
        this.interact = new PopulatieInteract(currentUser );

        setTitle("Population Management");
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        // Automatically show persons when the GUI is opened
        try {
            showPersons(); // Call to show persons
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        JMenu importExportMenu = new JMenu("Import/Export");

// Export to CSV
        JMenuItem exportCSVItem = new JMenuItem("Export to CSV");
        exportCSVItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportToCSV();
            }
        });

// Add items to the menu
        importExportMenu.add(exportCSVItem);


// Add the menu to the menu bar

        JMenuBar menuBar = new JMenuBar();

        JMenu editMenu = new JMenu("Edit");
        JMenu searchMenu = new JMenu("Search");
        JMenuItem viewPersons = new JMenuItem("Reset filters");
        viewPersons.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentEthnicityFilter = null; // Reset ethnicity filter
                currentCityFilter = null; // Reset city filter
                currentMinAgeFilter = null; // Reset minimum age filter
                currentMaxAgeFilter = null; // Reset maximum age filter
                try {
                    showPersons(); // Show all persons
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        if ("admin".equals(currentUser )) {
        JMenuItem istoricMenuItem = new JMenuItem("Istoric Utilizare");
        istoricMenuItem.addActionListener(e -> {
            try {
                List<UsageHistory> istoric = PopulatieInteract.obtineIstoricUtilizare();
                // Create a new JFrame or JDialog to display the history
                JFrame istoricFrame = new JFrame("Istoric Utilizare");
                String[] columnNames = {"ID", "Data/Ora", "Utilizator", "Operatie", "Comanda SQL"};

                // Prepare the data for the table
                Object[][] data = new Object[istoric.size()][5];
                for (int i = 0; i < istoric.size(); i++) {
                    UsageHistory entry = istoric.get(i);
                    data[i][0] = entry.getId();
                    data[i][1] = entry.getDataOra();
                    data[i][2] = entry.getUtilizator();
                    data[i][3] = entry.getOperatie();
                    data[i][4] = entry.getComandaSQL();
                }

                // Create a non-editable table model
                DefaultTableModel model = new DefaultTableModel(data, columnNames) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false; // Prevent editing
                    }
                };

                // Create the JTable with the non-editable model
                JTable table = new JTable(model);
                istoricFrame.add(new JScrollPane(table));
                istoricFrame.setSize(800, 400);
                istoricFrame.setVisible(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Eroare la obtinerea istoricului: " + ex.getMessage());
            }
        });
        editMenu.add(istoricMenuItem);}
        searchMenu.add(viewPersons);
        JMenuItem searchByIdItem = new JMenuItem("Search by ID");
        searchByIdItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String idStr = JOptionPane.showInputDialog("Enter ID of the person to search:");
                if (idStr != null) {
                    try {
                        int id = Integer.parseInt(idStr);
                        Persoana persoana = interact.obtinePersoana(id);
                        if (persoana != null) {
                            Adresa adresa = interact.obtineAdresa(persoana.getAdresaId()); // Store the address in a variable
                            displayPersonInTable(persoana, adresa); // Call a method to display the person
                            PopulatieInteract.logUsage(currentUser, "Search by ID", "N/A");
                        } else {
                            JOptionPane.showMessageDialog(null, "Person not found.");
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Invalid ID format.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        JMenuItem searchByAgeRangeItem = new JMenuItem("Search by Age Range");
        searchByAgeRangeItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String minAgeStr = JOptionPane.showInputDialog("Enter minimum age:");
                String maxAgeStr = JOptionPane.showInputDialog("Enter maximum age:");

                try {
                    int minAge = Integer.parseInt(minAgeStr);
                    int maxAge = Integer.parseInt(maxAgeStr);

                    if (minAge <= maxAge) {
                        currentMinAgeFilter = minAge; // Set the current minimum age filter
                        currentMaxAgeFilter = maxAge; // Set the current maximum age filter
                        PopulatieInteract.logUsage(currentUser, "Search by Age Range", "N/A");
                        showPersons(); // Call to show persons with the new filter
                    } else {
                        JOptionPane.showMessageDialog(null, "Minimum age must be less than or equal to maximum age.");
                    }
                } catch (NumberFormatException | SQLException ex) {
                    JOptionPane.showMessageDialog(null, "Please enter valid age values.");
                }
            }
        });

        searchMenu.add(searchByAgeRangeItem);

        JMenuItem searchByEthnicityItem = new JMenuItem("Search by Ethnicity");
        searchByEthnicityItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String ethnicity = JOptionPane.showInputDialog("Enter ethnicity to search:");
                if (ethnicity != null && !ethnicity.trim().isEmpty()) {
                    currentEthnicityFilter = ethnicity; // Set the current ethnicity filter
                    try {
                        PopulatieInteract.logUsage(currentUser, "Search by Ethnicity", "N/A");
                        showPersons(); // Call to show persons with the new filter
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Please enter an ethnicity.");
                }
            }
        });
        searchMenu.add(searchByEthnicityItem);
        JMenuItem searchByCityItem = new JMenuItem("Search by City");
        // Export to PDF
        JMenuItem exportPDFItem = new JMenuItem("Export to PDF");
        exportPDFItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportToPDF();
            }
        });
        importExportMenu.add(exportPDFItem);
        searchByCityItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String city = JOptionPane.showInputDialog("Enter city to search:");
                if (city != null && !city.trim().isEmpty()) {
                    currentCityFilter = city; // Set the current city filter
                    try {
                        PopulatieInteract.logUsage(currentUser, "Search by City", "N/A");
                        showPersons(); // Call to show persons with the new filter
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Please enter a city.");
                }
            }
        });
        searchMenu.add(searchByCityItem);


        searchMenu.add(searchByIdItem);

        if ("admin".equals(currentUser )) {
            JMenuItem addPerson = new JMenuItem("Add Person");
            addPerson.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showAddPersonForm();
                }
            });
            editMenu.add(addPerson);

            JMenuItem updatePerson = new JMenuItem("Update Person");
            updatePerson.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showUpdatePersonForm();
                }
            });
            editMenu.add(updatePerson);

            JMenuItem deletePerson = new JMenuItem("Delete Person");
            deletePerson.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showDeletePersonForm();
                }
            });
            editMenu.add(deletePerson);
        }
        JMenu chartMenu = new JMenu("Charts");


        JMenuItem pieChartItem = new JMenuItem("Generate Pie Chart by Ethnicity");
        chartMenu.add(pieChartItem);

        JMenuItem histogramItem = new JMenuItem("Generate Age Histogram");
        chartMenu.add(histogramItem);
        pieChartItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generatePieChart();
            }
        });

        histogramItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateAgeHistogram();
            }
        });



        // Add Logout Menu Item
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Dispose of the current frame
                dispose();
                // Show the login screen
                try {
                    PopulatieInteract.logUsage(currentUser, "Logout", "N/A");
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                LoginScreen loginScreen = new LoginScreen();
                loginScreen.setVisible(true);
            }
        });
        editMenu.add(logoutItem);
        menuBar.add(editMenu);
        menuBar.add(searchMenu);
        menuBar.add(chartMenu);
        if ("admin".equals(currentUser )) {
            JMenuItem importCSVItem = new JMenuItem("Import from CSV");
            importCSVItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("Select CSV File");
                    int userSelection = fileChooser.showOpenDialog(null);

                    if (userSelection == JFileChooser.APPROVE_OPTION) {
                        File fileToImport = fileChooser.getSelectedFile();
                        int choice = JOptionPane.showOptionDialog(null,
                                "What would you like to do with the imported data?",
                                "Import Options",
                                JOptionPane.YES_NO_CANCEL_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                new Object[]{"Overwrite", "Merge", "Cancel"},
                                null);

                        ImportUtil importUtil = new ImportUtil();
                        try {
                            if (choice == JOptionPane.YES_OPTION) {
                                // Overwrite existing data
                                importUtil.importFromCSV(fileToImport, true); // true for overwrite
                            } else if (choice == JOptionPane.NO_OPTION) {
                                // Merge with existing data
                                importUtil.importFromCSV(fileToImport, false); // false for merge
                            }
                            JOptionPane.showMessageDialog(null, "Import successful!");
                            PopulatieInteract.logUsage(currentUser, "Import from CSV", "N/A");
                            showPersons(); // Refresh the displayed persons after import

                        } catch (IOException | SQLException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(null, "Error importing data: " + ex.getMessage());
                        }
                    }
                }
            });
        importExportMenu.add(importCSVItem);}
        menuBar.add(importExportMenu);
        setJMenuBar(menuBar);
    }
    private void generatePieChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();

        try {
            String query = "SELECT cetatenie, COUNT(*) as numar FROM persoane GROUP BY cetatenie";

            try (Connection conn = ConexiuneBazaDeDate.obtineConexiune();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String ethnicity = rs.getString("cetatenie");
                    int count = rs.getInt("numar");
                    dataset.setValue(ethnicity, count);
                }
            }

            JFreeChart pieChart = ChartFactory.createPieChart(
                    "Distribution of Ethnicities", // Title of the chart
                    dataset, // Data
                    true, // Include legend
                    true, // Include tooltips
                    false // Include URLs
            );

            ChartPanel chartPanel = new ChartPanel(pieChart);
            chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));

            JFrame frame = new JFrame("Pie Chart");
            frame.setContentPane(chartPanel);
            frame.pack();
            frame.setVisible(true);
            PopulatieInteract.logUsage(currentUser, "Generated Pie Chart", "N/A");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error generating pie chart: " + ex.getMessage());
        }
    }
    private void generateAgeHistogram() {
        // Use a TreeMap to store age counts in sorted order
        Map<Integer, Integer> ageCounts = new TreeMap<>();

        try {
            // SQL query to get the count of individuals by their age
            String query = "SELECT varsta AS age, COUNT(*) AS numar FROM persoane GROUP BY varsta";

            try (Connection conn = ConexiuneBazaDeDate.obtineConexiune();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    // Get the individual age
                    int age = rs.getInt("age");
                    // Get the count of individuals with that age
                    int count = rs.getInt("numar");

                    // Store the count in the TreeMap
                    ageCounts.put(age, count);
                }
            }

            // Create the dataset for the histogram
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (Map.Entry<Integer, Integer> entry : ageCounts.entrySet()) {
                dataset.addValue(entry.getValue(), "Număr", entry.getKey());
            }

            // Create the histogram chart
            JFreeChart histogram = ChartFactory.createBarChart(
                    "Age Distribution", // Title of the chart
                    "Age", // X-axis label
                    "Number", // Y-axis label
                    dataset // Data
            );

            ChartPanel chartPanel = new ChartPanel(histogram);
            chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));

            JFrame frame = new JFrame("Age Histogram");
            frame.setContentPane(chartPanel);
            frame.pack();
            frame.setVisible(true);
            PopulatieInteract.logUsage(currentUser, "Generate Age Histogram", "N/A");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error generating age histogram: " + ex.getMessage());
        }
    }
    private void exportToPDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");
        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();

            // Ensure the file has a .pdf extension
            if (!filePath.endsWith(".pdf")) {
                filePath += ".pdf";
            }

            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    // Set font for title
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(25, 750);
                    contentStream.showText("Population Data Export");
                    contentStream.endText();

                    // Set font for table content
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                    contentStream.beginText(); // Start a new text block for the table
                    contentStream.newLineAtOffset(25, 730); // Move down for table (adjust as needed)

                    // Add table headers
                    String[] headers = {"ID", "Name", "Age", "Street", "City", "Country", "Postal Code", "Birthplace", "CNP", "Ethnicity"};
                    for (String header : headers) {
                        contentStream.showText(header + "   "); // Use spaces for better alignment
                    }
                    contentStream.newLineAtOffset(0, -15); // Move down for next row

                    // Get the list of persons
                    String criteriuFiltrare = "1=1"; // This will select all records; adjust as needed
                    String ordineSortare = "nume"; // Sort by name; adjust as needed
                    List<Persoana> persoane = interact.filtreazaSiOrdoneazaPersoane(criteriuFiltrare, ordineSortare);

                    // Add data to the PDF
                    for (Persoana persoana : persoane) {
                        Adresa adresa = interact.obtineAdresa(persoana.getAdresaId());
                        contentStream.showText(persoana.getId() + "   " +
                                persoana.getNume() + "   " +
                                persoana.getVarsta() + "   " +
                                (adresa != null ? adresa.getStrada() : "N/A") + "   " +
                                (adresa != null ? adresa.getOras() : "N/A") + "   " +
                                (adresa != null ? adresa.getStat() : "N/A") + "   " +
                                (adresa != null ? adresa.getCodPostal() : "N/A") + "   " +
                                persoana.getLocDeNastere() + "   " +
                                persoana.getCnp() + "   " +
                                persoana.getCetatenie());
                        contentStream.newLineAtOffset(0, -15); // Move down for next row
                    }
                    contentStream.endText(); // End the text block
                }

                document.save(filePath);
                PopulatieInteract.logUsage(currentUser, "Export to PDF", "N/A");
                JOptionPane.showMessageDialog(null, "PDF saved successfully at: " + filePath);
            } catch (IOException | SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error saving PDF: " + e.getMessage());
            }
        }
    }
    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");
        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();

            // Ensure the file has a .csv extension
            if (!filePath.endsWith(".csv")) {
                filePath += ".csv";
            }

            try (FileWriter writer = new FileWriter(filePath)) {
                // Write header
                writer.append("ID,Nume,Varsta,Strada,Oras,Stat,CodPostal,LocDeNastere,CNP,Cetatenie\n");

                // Get the list of persons (you may need to adjust this based on your data source)
                String criteriuFiltrare = "1=1"; // This will select all records; adjust as needed
                String ordineSortare = "nume"; // Sort by name; adjust as needed

                // Get the list of persons with filtering and sorting
                List<Persoana> persoane = interact.filtreazaSiOrdoneazaPersoane(criteriuFiltrare, ordineSortare); // Adjust this method as needed

                // Write data
                for (Persoana persoana : persoane) {
                    Adresa adresa = interact.obtineAdresa(persoana.getAdresaId());
                    writer.append(String.valueOf(persoana.getId())).append(",")
                            .append(persoana.getNume()).append(",")
                            .append(String.valueOf(persoana.getVarsta())).append(",")
                            .append(adresa != null ? adresa.getStrada() : "N/A").append(",")
                            .append(adresa != null ? adresa.getOras() : "N/A").append(",")
                            .append(adresa != null ? adresa.getStat() : "N/A").append(",")
                            .append(adresa != null ? adresa.getCodPostal() : "N/A").append(",")
                            .append(persoana.getLocDeNastere()).append(",")
                            .append(persoana.getCnp()).append(",")
                            .append(persoana.getCetatenie()).append("\n");
                }
                PopulatieInteract.logUsage(currentUser, "Export to CSV", "N/A");
                JOptionPane.showMessageDialog(null, "Export successful to " + filePath);
            } catch (IOException | SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error exporting to CSV: " + e.getMessage());
            }
        }
    }
    private void displayResults(List<Persoana> results) throws SQLException {
        String[] columnNames = {"ID", "Name", "Age", "Street", "City", "Country", "Postal Code", "Birthplace", "CNP", "Ethnicity"};
        Object[][] data = new Object[results.size()][10];

        for (int i = 0; i < results.size(); i++) {
            Persoana persoana = results.get(i);
            Adresa adresa = interact.obtineAdresa(persoana.getAdresaId());
            data[i][0] = persoana.getId();
            data[i][1] = persoana.getNume();
            data[i][2] = persoana.getVarsta();
            data[i][3] = (adresa != null) ? adresa.getStrada() : "N/A";
            data[i][4] = (adresa != null) ? adresa.getOras() : "N/A";
            data[i][5] = (adresa != null) ? adresa.getStat() : "N/A";
            data[i][6] = (adresa != null) ? adresa.getCodPostal() : "N/A";
            data[i][7] = persoana.getLocDeNastere();
            data[i][8] = persoana.getCnp();
            data[i][9] = persoana.getCetatenie();
        }

        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Prevent editing
            }
        };

        JTable table = new JTable(model);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);
        getContentPane().removeAll(); // Clear previous content
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
    private void displayPersonInTable(Persoana persoana, Adresa adresa) {
        String[] columnNames = {"ID", "Name", "Age", "Street", "City", "Country", "Postal Code", "Birthplace", "CNP", "Ethnicity"};
        Object[][] data = new Object[1][10];

        data[0][0] = persoana.getId();
        data[0][1] = persoana.getNume();
        data[0][2] = persoana.getVarsta();
        data[0][3] = (adresa != null) ? adresa.getStrada() : "N/A";
        data[0][4] = (adresa != null) ? adresa.getOras() : "N/A";
        data[0][5] = (adresa != null) ? adresa.getStat() : "N/A";
        data[0][6] = (adresa != null) ? adresa.getCodPostal() : "N/A";
        data[0][7] = persoana.getLocDeNastere();
        data[0][8] = persoana.getCnp();
        data[0][9] = persoana.getCetatenie();

        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Prevent editing
            }
        };

        JTable table = new JTable(model);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);
        getContentPane().removeAll(); // Clear previous content
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
    private void showPersons() throws SQLException {
        String filterCriteria = "1=1"; // Default to show all

        if (currentEthnicityFilter != null) {
            filterCriteria += " AND cetatenie = '" + currentEthnicityFilter + "'";
        }
        if (currentCityFilter != null) {
            filterCriteria += " AND EXISTS (SELECT 1 FROM adrese WHERE adrese.id = persoane.adresa_id AND adrese.oras = '" + currentCityFilter + "')";
        }
        if (currentMinAgeFilter != null && currentMaxAgeFilter != null) {
            filterCriteria += " AND varsta >= " + currentMinAgeFilter + " AND varsta <= " + currentMaxAgeFilter;
        }

        List<Persoana> persoane = interact.filtreazaSiOrdoneazaPersoane(filterCriteria, "nume ASC");
        String[] columnNames = {"ID", "Name", "Age", "Street", "City", "Country", "Postal Code", "Birthplace", "CNP", "Ethnicity"};
        Object[][] data = new Object[persoane.size()][10];

        for (int i = 0; i < persoane.size(); i++) {
            Persoana p = persoane.get(i);
            Adresa adresa = interact.obtineAdresa(p.getAdresaId());

            data[i][0] = p.getId();
            data[i][1] = p.getNume();
            data[i][2] = p.getVarsta();
            data[i][3] = (adresa != null) ? adresa.getStrada() : "N/A";
            data[i][4] = (adresa != null) ? adresa.getOras() : "N/A";
            data[i][5] = (adresa != null) ? adresa.getStat() : "N/A";
            data[i][6] = (adresa != null) ? adresa.getCodPostal() : "N/A";
            data[i][7] = p.getLocDeNastere();
            data[i][8] = p.getCnp();
            data[i][9] = p.getCetatenie();
        }

        // Create a non-editable table model using an anonymous subclass
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Prevent editing
            }
        };

        JTable table = new JTable(model);
        // Create a TableRowSorter and set it on the table
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);
        getContentPane().removeAll();
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void showAddPersonForm() {
        JPanel panel = new JPanel(new GridLayout(9, 2));
        panel.add(new JLabel("Name:"));
        JTextField nameField = new JTextField();
        panel.add(nameField);
        panel.add(new JLabel("Age:"));
        JTextField ageField = new JTextField();
        panel.add(ageField);
        panel.add(new JLabel("Street:"));
        JTextField streetField = new JTextField();
        panel.add(streetField);
        panel.add(new JLabel("City:"));
        JTextField cityField = new JTextField();
        panel.add(cityField);
        panel.add(new JLabel("Country:"));
        JTextField countryField = new JTextField();
        panel.add(countryField);
        panel.add(new JLabel("Postal Code:"));
        JTextField zipField = new JTextField();
        panel.add(zipField);
        panel.add(new JLabel("Ethnicity:"));
        JTextField ethnicityField = new JTextField();
        panel.add(ethnicityField);
        panel.add(new JLabel("Birthplace:"));
        JTextField birthplaceField = new JTextField();
        panel.add(birthplaceField);
        panel.add(new JLabel("CNP:"));
        JTextField cnpField = new JTextField();
        panel.add(cnpField);


        int result = JOptionPane.showConfirmDialog(this, panel, "Add Person", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                if (cnpField.getText().length() != 13 || !cnpField.getText().matches("\\d{13}")) { // Check if CNP is 13 digits
                    JOptionPane.showMessageDialog(this, "CNP must be exactly 13 digits.", "Error", JOptionPane.ERROR_MESSAGE);
                    return; // Exit the method if CNP is invalid
                }
                Adresa adresa = new Adresa(0, streetField.getText(), cityField.getText(), countryField.getText(), zipField.getText());
                Persoana persoana = new Persoana(0, nameField.getText(), Integer.parseInt(ageField.getText()), 0, birthplaceField.getText(), cnpField.getText(), ethnicityField.getText());
                interact.adaugaPersoana(persoana, adresa); // Pass the address to the method
                showPersons();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void showUpdatePersonForm() {
        String idStr = JOptionPane.showInputDialog(this, "Enter ID of the person to update:");
        if (idStr != null) {
            try {
                int id = Integer.parseInt(idStr);
                Persoana persoana = interact.obtinePersoana(id);
                if (persoana != null) {
                    // Retrieve the address using the adresaId
                    Adresa adresa = interact.obtineAdresa(persoana.getAdresaId());
                    JPanel panel = new JPanel(new GridLayout(9, 2));
                    panel.add(new JLabel("Name:"));
                    JTextField nameField = new JTextField(persoana.getNume());
                    panel.add(nameField);
                    panel.add(new JLabel("Age:"));
                    JTextField ageField = new JTextField(String.valueOf(persoana.getVarsta()));
                    panel.add(ageField);
                    panel.add(new JLabel("Street:"));
                    JTextField streetField = new JTextField(adresa.getStrada());
                    panel.add(streetField);
                    panel.add(new JLabel("City:"));
                    JTextField cityField = new JTextField(adresa.getOras());
                    panel.add(cityField);
                    panel.add(new JLabel("Country:"));
                    JTextField countryField = new JTextField(adresa.getStat());
                    panel.add(countryField);
                    panel.add(new JLabel("Postal Code:"));
                    JTextField zipField = new JTextField(adresa.getCodPostal());
                    panel.add(zipField);
                    panel.add(new JLabel("Birthplace:"));
                    JTextField birthplaceField = new JTextField(persoana.getLocDeNastere());
                    panel.add(birthplaceField);
                    panel.add(new JLabel("CNP:"));
                    JTextField cnpField = new JTextField(persoana.getCnp());
                    panel.add(cnpField);
                    panel.add(new JLabel("Ethnicity:"));
                    JTextField ethnicityField = new JTextField(persoana.getCetatenie());
                    panel.add(ethnicityField);

                    int result = JOptionPane.showConfirmDialog(this, panel, "Update Person", JOptionPane.OK_CANCEL_OPTION);
                    if (result == JOptionPane.OK_OPTION) {
                        // Create a new address object with updated details
                        if (cnpField.getText().length() != 13 || !cnpField.getText().matches("\\d{13}")) { // Check if CNP is 13 digits
                            JOptionPane.showMessageDialog(this, "CNP must be exactly 13 digits.", "Error", JOptionPane.ERROR_MESSAGE);
                            return; // Exit the method if CNP is invalid
                        }
                        Adresa updatedAdresa = new Adresa(0, streetField.getText(), cityField.getText(), countryField.getText(), zipField.getText());
                        persoana.setNume(nameField.getText());
                        persoana.setVarsta(Integer.parseInt(ageField.getText()));
                        persoana.setLocDeNastere(birthplaceField.getText());
                        persoana.setCnp(cnpField.getText());

                        persoana.setCetatenie(ethnicityField.getText());
                        interact.actualizeazaPersoana(persoana, updatedAdresa); // Update the person and address
                        showPersons();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Person not found.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void showDeletePersonForm() {
        String idStr = JOptionPane.showInputDialog(this, "Enter ID of the person to delete:");
        if (idStr != null) {
            try {
                int id = Integer.parseInt(idStr);
                // First, retrieve the person to confirm they exist
                Persoana persoana = interact.obtinePersoana(id);
                if (persoana != null) {
                    // Show confirmation dialog
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Are you sure you want to delete " + persoana.getNume() + "?",
                            "Confirm Deletion",
                            JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        interact.stergePersoana(id);
                        showPersons(); // Refresh the list of persons
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Person not found.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid ID format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                SwingGUI window = new SwingGUI("admin ");
                window.setVisible(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}