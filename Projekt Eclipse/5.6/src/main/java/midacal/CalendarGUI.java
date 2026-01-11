package midacal;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.List;

public class CalendarGUI extends JFrame {
    private Main.MemoryContainer appMemory;
    private DBManager dbManager;
    
    // Komponenty
    private JTabbedPane tabbedPane;
    private JTable kontaktyTable;
    private JTable zdarzeniaTable;
    private DefaultTableModel kontaktyModel;
    private DefaultTableModel zdarzeniaModel;
    
    private JMenuBar menuBar;
    private JMenu fileMenu, editMenu, viewMenu;
    
    private JButton addKontaktBtn, editKontaktBtn, deleteKontaktBtn;
    private JButton addZdarzenieBtn, editZdarzenieBtn, deleteZdarzenieBtn;
    private JButton refreshBtn, saveBtn;
    
    public CalendarGUI(Main.MemoryContainer memory, DBManager dbMgr) {
        this.appMemory = memory;
        this.dbManager = dbMgr;
        
        initializeGUI();
    }
    
    private void initializeGUI() {
        setTitle("KALENDARZ MIDACAL - Tryb Graficzny");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(true);
        
        // Panel główny
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Menu
        createMenuBar();
        setJMenuBar(menuBar);
        
        // Tabbed pane - dla Kontaktów i Zdarzeń
        tabbedPane = new JTabbedPane();
        
        JPanel kontaktyPanel = createKontaktyPanel();
        JPanel zdarzeniaPanel = createZdarzeniaPanel();
        
        tabbedPane.addTab("Kontakty", kontaktyPanel);
        tabbedPane.addTab("Zdarzenia", zdarzeniaPanel);
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Panel dolny - przyciski akcji
        JPanel actionPanel = createActionPanel();
        mainPanel.add(actionPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Załaduj dane do tabel
        refreshData();
        
        // Window listener dla zapisania danych przy zamykaniu
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveAndExit();
            }
        });
    }
    
    private void createMenuBar() {
        menuBar = new JMenuBar();
        
        // File menu
        fileMenu = new JMenu("Plik");
        JMenuItem saveItem = new JMenuItem("Zapisz");
        saveItem.addActionListener(e -> saveData());
        JMenuItem exitItem = new JMenuItem("Wyjście");
        exitItem.addActionListener(e -> saveAndExit());
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Edit menu
        editMenu = new JMenu("Edycja");
        JMenuItem clearAllItem = new JMenuItem("Wyczyść RAM");
        clearAllItem.addActionListener(e -> clearRAM());
        editMenu.add(clearAllItem);
        
        // View menu
        viewMenu = new JMenu("Widok");
        JMenuItem refreshItem = new JMenuItem("Odśwież");
        refreshItem.addActionListener(e -> refreshData());
        viewMenu.add(refreshItem);
        
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
    }
    
    private JPanel createKontaktyPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Tabela kontaktów
        String[] columns = {"Imię", "Nazwisko", "Telefon", "Email"};
        kontaktyModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        kontaktyTable = new JTable(kontaktyModel);
        kontaktyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(kontaktyTable);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Przyciski dla kontaktów
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addKontaktBtn = new JButton("Dodaj");
        addKontaktBtn.addActionListener(e -> addKontakt());
        editKontaktBtn = new JButton("Edytuj");
        editKontaktBtn.addActionListener(e -> editKontakt());
        deleteKontaktBtn = new JButton("Usuń");
        deleteKontaktBtn.addActionListener(e -> deleteKontakt());
        
        buttonPanel.add(addKontaktBtn);
        buttonPanel.add(editKontaktBtn);
        buttonPanel.add(deleteKontaktBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createZdarzeniaPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Tabela zdarzeń
        String[] columns = {"Tytuł", "Opis", "Data", "Link"};
        zdarzeniaModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        zdarzeniaTable = new JTable(zdarzeniaModel);
        zdarzeniaTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(zdarzeniaTable);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Przyciski dla zdarzeń
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addZdarzenieBtn = new JButton("Dodaj");
        addZdarzenieBtn.addActionListener(e -> addZdarzenie());
        editZdarzenieBtn = new JButton("Edytuj");
        editZdarzenieBtn.addActionListener(e -> editZdarzenie());
        deleteZdarzenieBtn = new JButton("Usuń");
        deleteZdarzenieBtn.addActionListener(e -> deleteZdarzenie());
        
        buttonPanel.add(addZdarzenieBtn);
        buttonPanel.add(editZdarzenieBtn);
        buttonPanel.add(deleteZdarzenieBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        refreshBtn = new JButton("Odśwież");
        refreshBtn.addActionListener(e -> refreshData());
        
        saveBtn = new JButton("Zapisz do bazy");
        saveBtn.addActionListener(e -> saveData());
        
        panel.add(refreshBtn);
        panel.add(saveBtn);
        
        return panel;
    }
    
    private void refreshData() {
        // Wyczyść tabele
        kontaktyModel.setRowCount(0);
        zdarzeniaModel.setRowCount(0);
        
        // Wczytaj kontakty
        for (Kontakt k : appMemory.kontakty) {
            kontaktyModel.addRow(new Object[]{
                k.getImie(),
                k.getNazwisko(),
                k.getTelStr(),
                k.getEmailStr()
            });
        }
        
        // Wczytaj zdarzenia
        for (Zdarzenie z : appMemory.zdarzenia) {
            zdarzeniaModel.addRow(new Object[]{
                z.getTytul(),
                z.getOpis(),
                z.getData(),
                z.getMiejsce()
            });
        }
    }
    
    private void addKontakt() {
        JDialog dialog = new JDialog(this, "Dodaj Kontakt", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel imieLabel = new JLabel("Imię:");
        JTextField imieField = new JTextField();
        JLabel nazwiskoLabel = new JLabel("Nazwisko:");
        JTextField nazwiskoField = new JTextField();
        JLabel telLabel = new JLabel("Telefon:");
        JTextField telField = new JTextField();
        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField();
        
        panel.add(imieLabel);
        panel.add(imieField);
        panel.add(nazwiskoLabel);
        panel.add(nazwiskoField);
        panel.add(telLabel);
        panel.add(telField);
        panel.add(emailLabel);
        panel.add(emailField);
        
        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Anuluj");
        
        okBtn.addActionListener(e -> {
            try {
                Kontakt k = new Kontakt(
                    imieField.getText(),
                    nazwiskoField.getText(),
                    com.google.i18n.phonenumbers.PhoneNumberUtil.getInstance().parse(telField.getText(), "PL"),
                    new jakarta.mail.internet.InternetAddress(emailField.getText())
                );
                appMemory.kontakty.add(k);
                refreshData();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Kontakt dodany pomyślnie!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Błąd: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        panel.add(okBtn);
        panel.add(cancelBtn);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void editKontakt() {
        int row = kontaktyTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Wybierz kontakt do edycji!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        Kontakt k = appMemory.kontakty.get(row);
        JDialog dialog = new JDialog(this, "Edytuj Kontakt", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField imieField = new JTextField(k.getImie());
        JTextField nazwiskoField = new JTextField(k.getNazwisko());
        JTextField telField = new JTextField(k.getTelStr());
        JTextField emailField = new JTextField(k.getEmailStr());
        
        panel.add(new JLabel("Imię:"));
        panel.add(imieField);
        panel.add(new JLabel("Nazwisko:"));
        panel.add(nazwiskoField);
        panel.add(new JLabel("Telefon:"));
        panel.add(telField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        
        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Anuluj");
        
        okBtn.addActionListener(e -> {
            k.setImie(imieField.getText());
            k.setNazwisko(nazwiskoField.getText());
            k.setTelStr(telField.getText());
            k.setEmailStr(emailField.getText());
            refreshData();
            dialog.dispose();
            JOptionPane.showMessageDialog(this, "Kontakt zaktualizowany!");
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        panel.add(okBtn);
        panel.add(cancelBtn);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void deleteKontakt() {
        int row = kontaktyTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Wybierz kontakt do usunięcia!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Potwierdzasz usunięcie?", "Potwierdzenie", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            appMemory.kontakty.remove(row);
            refreshData();
            JOptionPane.showMessageDialog(this, "Kontakt usunięty!");
        }
    }
    
    private void addZdarzenie() {
        JDialog dialog = new JDialog(this, "Dodaj Zdarzenie", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField tytulField = new JTextField();
        JTextField opisField = new JTextField();
        JTextField dataField = new JTextField("YYYY-MM-DD");
        JTextField linkField = new JTextField();
        
        panel.add(new JLabel("Tytuł:"));
        panel.add(tytulField);
        panel.add(new JLabel("Opis:"));
        panel.add(opisField);
        panel.add(new JLabel("Data:"));
        panel.add(dataField);
        panel.add(new JLabel("Link:"));
        panel.add(linkField);
        
        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Anuluj");
        
        okBtn.addActionListener(e -> {
            try {
                Zdarzenie z = new Zdarzenie(
                    tytulField.getText(),
                    opisField.getText(),
                    LocalDate.parse(dataField.getText()),
                    java.net.URI.create(linkField.getText()).toURL()
                );
                appMemory.zdarzenia.add(z);
                refreshData();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Zdarzenie dodane pomyślnie!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Błąd: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        panel.add(okBtn);
        panel.add(cancelBtn);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void editZdarzenie() {
        int row = zdarzeniaTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Wybierz zdarzenie do edycji!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        Zdarzenie z = appMemory.zdarzenia.get(row);
        JDialog dialog = new JDialog(this, "Edytuj Zdarzenie", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField tytulField = new JTextField(z.getTytul());
        JTextField opisField = new JTextField(z.getOpis());
        JTextField dataField = new JTextField(z.getData().toString());
        JTextField linkField = new JTextField(z.getMiejsce() != null ? z.getMiejsce().toString() : "");
        
        panel.add(new JLabel("Tytuł:"));
        panel.add(tytulField);
        panel.add(new JLabel("Opis:"));
        panel.add(opisField);
        panel.add(new JLabel("Data:"));
        panel.add(dataField);
        panel.add(new JLabel("Link:"));
        panel.add(linkField);
        
        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Anuluj");
        
        okBtn.addActionListener(e -> {
            try {
                z.setTytul(tytulField.getText());
                z.setOpis(opisField.getText());
                z.setData(LocalDate.parse(dataField.getText()));
                z.setMiejsce(java.net.URI.create(linkField.getText()).toURL());
                refreshData();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Zdarzenie zaktualizowane!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Błąd: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        panel.add(okBtn);
        panel.add(cancelBtn);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void deleteZdarzenie() {
        int row = zdarzeniaTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Wybierz zdarzenie do usunięcia!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Potwierdzasz usunięcie?", "Potwierdzenie", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            appMemory.zdarzenia.remove(row);
            refreshData();
            JOptionPane.showMessageDialog(this, "Zdarzenie usunięte!");
        }
    }
    
    private void saveData() {
        dbManager.saveToDatabase(appMemory);
        JOptionPane.showMessageDialog(this, "Dane zapisane do bazy danych!", "Sukces", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void clearRAM() {
        int confirm = JOptionPane.showConfirmDialog(this, "Wyczyścić RAM?", "Potwierdzenie", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            appMemory.kontakty.clear();
            appMemory.zdarzenia.clear();
            refreshData();
            JOptionPane.showMessageDialog(this, "RAM wyczyszczony!");
        }
    }
    
    private void saveAndExit() {
        int confirm = JOptionPane.showConfirmDialog(this, "Zapisać zmiany przed wyjściem?", "Potwierdzenie", JOptionPane.YES_NO_CANCEL_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            saveData();
        }
        System.exit(0);
    }
}
