package midacal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

public class CalendarGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private Main.MemoryContainer appMemory;
    private DBManager dbManager;
    
    // Komponenty
    private JTabbedPane tabbedPane;
    private JTable kontaktyTable;
    private JTable zdarzeniaTable;
    private DefaultTableModel kontaktyModel;
    private DefaultTableModel zdarzeniaModel;
    
    // Kalendarz (gotowe komponenty Swing)
    private JPanel calendarGrid;
    private JLabel monthLabel;
    private java.util.Calendar currentMonth;
    private JComboBox<String> monthCombo;
    private JComboBox<Integer> yearCombo;
    private JSlider calendarFontSlider;
    private float calendarFontSize = 12f;
    private Color eventDayColor = new Color(0, 102, 0);
    
    private JMenuBar menuBar;
    private JMenu fileMenu, editMenu, viewMenu, sortMenu;
    
    private JButton addKontaktBtn, editKontaktBtn, deleteKontaktBtn;
    private JButton addZdarzenieBtn, editZdarzenieBtn, deleteZdarzenieBtn;
    private JButton refreshBtn, saveBtn;
    
    // Dodatkowe panele
    private JPanel overviewPanel; // JSplitPane: Kontakty | Zdarzenia
    private JDesktopPane desktopPane; // InternalFrames
    
    public CalendarGUI(Main.MemoryContainer memory, DBManager dbMgr) {
        this.appMemory = memory;
        this.dbManager = dbMgr;
        
        initializeGUI();
    }
    
    private java.awt.event.ActionListener onAction(Runnable r) { return e -> { if (e != null) r.run(); }; }
    private javax.swing.event.ChangeListener onChange(Runnable r) { return e -> { if (e != null) r.run(); }; }
    
    // Przegląd: SplitPane z dwoma tabelami
    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTable kontaktyView = new JTable(kontaktyModel);
        JTable zdarzeniaView = new JTable(zdarzeniaModel);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(kontaktyView), new JScrollPane(zdarzeniaView));
        split.setResizeWeight(0.5);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    // Okna: DesktopPane + InternalFrames z tabelami
    private JPanel createWindowsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        desktopPane = new JDesktopPane();
        panel.add(desktopPane, BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton openKontakty = new JButton("Nowe okno: Kontakty");
        JButton openZdarzenia = new JButton("Nowe okno: Zdarzenia");
        controls.add(openKontakty);
        controls.add(openZdarzenia);
        panel.add(controls, BorderLayout.NORTH);

        openKontakty.addActionListener(onAction(() -> {
            JInternalFrame f = new JInternalFrame("Kontakty", true, true, true, true);
            f.add(new JScrollPane(new JTable(kontaktyModel)));
            f.pack();
            f.setVisible(true);
            desktopPane.add(f);
            f.setLocation(10, 10);
        }));
        openZdarzenia.addActionListener(onAction(() -> {
            JInternalFrame f = new JInternalFrame("Zdarzenia", true, true, true, true);
            f.add(new JScrollPane(new JTable(zdarzeniaModel)));
            f.pack();
            f.setVisible(true);
            desktopPane.add(f);
            f.setLocation(30, 30);
        }));

        return panel;
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
        JPanel kalendarzPanel = createCalendarPanel();
        overviewPanel = createOverviewPanel();
        JPanel oknaPanel = createWindowsPanel();
        
        tabbedPane.addTab("Kontakty", kontaktyPanel);
        tabbedPane.addTab("Zdarzenia", zdarzeniaPanel);
        tabbedPane.addTab("Kalendarz", kalendarzPanel);
        tabbedPane.addTab("Przegląd", overviewPanel);
        tabbedPane.addTab("Okna", oknaPanel);
        
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

    // === KALENDARZ: gotowe komponenty (JPanel, JLabel, GridLayout, BorderLayout) ===
    private JPanel createCalendarPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Pasek nawigacji miesiąca z gotowymi komponentami
        JPanel navPanel = new JPanel(new BorderLayout(10, 10));
        JPanel leftNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JButton prevBtn = new JButton("◀ Poprzedni");
        JButton nextBtn = new JButton("Następny ▶");
        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(monthLabel.getFont().deriveFont(Font.BOLD, 16f));

        // ComboBox miesiąc/rok
        String[] miesiace = {"Styczeń","Luty","Marzec","Kwiecień","Maj","Czerwiec","Lipiec","Sierpień","Wrzesień","Październik","Listopad","Grudzień"};
        monthCombo = new JComboBox<>(miesiace);
        Integer[] lata = new Integer[21];
        int baseYear = java.time.LocalDate.now().getYear();
        for (int i = 0; i <= 20; i++) lata[i] = baseYear - 10 + i;
        yearCombo = new JComboBox<>(lata);

        // Slider rozmiaru czcionki siatki
        calendarFontSlider = new JSlider(10, 20, (int) calendarFontSize);
        calendarFontSlider.setMajorTickSpacing(5);
        calendarFontSlider.setMinorTickSpacing(1);
        calendarFontSlider.setPaintTicks(true);
        calendarFontSlider.setToolTipText("Rozmiar czcionki kalendarza");

        leftNav.add(prevBtn);
        leftNav.add(nextBtn);
        leftNav.add(new JLabel("Miesiąc:"));
        leftNav.add(monthCombo);
        leftNav.add(new JLabel("Rok:"));
        leftNav.add(yearCombo);
        leftNav.add(new JLabel("Czcionka:"));
        leftNav.add(calendarFontSlider);

        navPanel.add(leftNav, BorderLayout.WEST);
        navPanel.add(monthLabel, BorderLayout.CENTER);
        panel.add(navPanel, BorderLayout.NORTH);

        // Siatka dni 7x7 (nagłówki dni tygodnia + maks. 6 tygodni)
        calendarGrid = new JPanel(new GridLayout(7, 7, 5, 5));
        panel.add(calendarGrid, BorderLayout.CENTER);

        // Inicjalizacja bieżącego miesiąca
        currentMonth = java.util.Calendar.getInstance();
        currentMonth.set(java.util.Calendar.DAY_OF_MONTH, 1);

        // Zdarzenia przycisków i kontrolek
        prevBtn.addActionListener(onAction(() -> { currentMonth.add(java.util.Calendar.MONTH, -1); syncCombosFromCalendar(); fillCalendarGrid(); }));
        nextBtn.addActionListener(onAction(() -> { currentMonth.add(java.util.Calendar.MONTH, 1); syncCombosFromCalendar(); fillCalendarGrid(); }));
        monthCombo.addActionListener(onAction(() -> { currentMonth.set(java.util.Calendar.MONTH, monthCombo.getSelectedIndex()); fillCalendarGrid(); }));
        yearCombo.addActionListener(onAction(() -> { currentMonth.set(java.util.Calendar.YEAR, (Integer) yearCombo.getSelectedItem()); fillCalendarGrid(); }));
        calendarFontSlider.addChangeListener(onChange(() -> { calendarFontSize = calendarFontSlider.getValue(); fillCalendarGrid(); }));

        // Wypełnij siatkę po raz pierwszy
        syncCombosFromCalendar();
        fillCalendarGrid();
        return panel;
    }

    private void fillCalendarGrid() {
        calendarGrid.removeAll();

        // Nagłówki dni tygodnia (Pn..Nd)
        String[] dni = {"Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd"};
        for (String d : dni) {
            JLabel hdr = new JLabel(d, SwingConstants.CENTER);
            hdr.setFont(hdr.getFont().deriveFont(Font.BOLD, calendarFontSize));
            calendarGrid.add(hdr);
        }

        // Ustaw label miesiąca
        String[] miesiace = {"Styczeń","Luty","Marzec","Kwiecień","Maj","Czerwiec","Lipiec","Sierpień","Wrzesień","Październik","Listopad","Grudzień"};
        int m = currentMonth.get(java.util.Calendar.MONTH);
        int y = currentMonth.get(java.util.Calendar.YEAR);
        monthLabel.setText(miesiace[m] + " " + y);

        // Oblicz offset (pierwszy dzień tygodnia: poniedziałek)
        java.util.Calendar cal = (java.util.Calendar) currentMonth.clone();
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        int firstDayDow = cal.get(java.util.Calendar.DAY_OF_WEEK); // 1=Nd..7=So w Java
        // Konwersja na indeks poniedziałek=0..niedziela=6
        int dowIndex;
        switch (firstDayDow) {
            case java.util.Calendar.MONDAY -> dowIndex = 0;
            case java.util.Calendar.TUESDAY -> dowIndex = 1;
            case java.util.Calendar.WEDNESDAY -> dowIndex = 2;
            case java.util.Calendar.THURSDAY -> dowIndex = 3;
            case java.util.Calendar.FRIDAY -> dowIndex = 4;
            case java.util.Calendar.SATURDAY -> dowIndex = 5;
            default -> dowIndex = 6; // SUNDAY
        }

        int daysInMonth = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);

        // Puste pola przed pierwszym dniem
        for (int i = 0; i < dowIndex; i++) {
            calendarGrid.add(new JLabel(""));
        }

        // Dni miesiąca
        java.time.LocalDate today = java.time.LocalDate.now();
        for (int day = 1; day <= daysInMonth; day++) {
            JLabel cell = new JLabel(String.valueOf(day), SwingConstants.CENTER);
            cell.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            cell.setFont(cell.getFont().deriveFont(calendarFontSize));

            // Podświetl dzisiejszy dzień
            if (today.getYear() == y && today.getMonthValue() - 1 == m && today.getDayOfMonth() == day) {
                cell.setOpaque(true);
                cell.setBackground(new Color(220, 240, 255));
                cell.setFont(cell.getFont().deriveFont(Font.BOLD));
            }

            // Oznacz dni z wydarzeniami (jeśli istnieją w pamięci)
            boolean hasEvent = false;
            for (Zdarzenie z : appMemory.zdarzenia) {
                java.time.LocalDate d = z.getData();
                if (d != null && d.getYear() == y && d.getMonthValue() - 1 == m && d.getDayOfMonth() == day) {
                    hasEvent = true; break;
                }
            }
            if (hasEvent) {
                cell.setForeground(eventDayColor);
                cell.setToolTipText("Zdarzenia w tym dniu");
            }

            calendarGrid.add(cell);
        }

        // Uzupełnij siatkę do 7x7
        int cellsFilled = 7 + dowIndex + daysInMonth; // 7 nagłówków + offset + dni
        for (int i = cellsFilled; i < 49; i++) {
            calendarGrid.add(new JLabel(""));
        }

        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    private void syncCombosFromCalendar() {
        if (monthCombo != null) monthCombo.setSelectedIndex(currentMonth.get(java.util.Calendar.MONTH));
        if (yearCombo != null) yearCombo.setSelectedItem(currentMonth.get(java.util.Calendar.YEAR));
    }
    
    private void createMenuBar() {
        menuBar = new JMenuBar();
        
        // File menu
        fileMenu = new JMenu("Plik");
        JMenuItem openFileItem = new JMenuItem("Otwórz plik...");
        openFileItem.addActionListener(onAction(() -> {
            JFileChooser fc = new JFileChooser();
            int res = fc.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                JOptionPane.showMessageDialog(this, "Wybrano: " + fc.getSelectedFile().getAbsolutePath());
            }
        }));
        JMenuItem saveItem = new JMenuItem("Zapisz");
        saveItem.addActionListener(onAction(this::saveData));
        JMenuItem exitItem = new JMenuItem("Wyjście");
        exitItem.addActionListener(onAction(this::saveAndExit));
        fileMenu.add(openFileItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Edit menu
        editMenu = new JMenu("Edycja");
        JMenuItem clearAllItem = new JMenuItem("Wyczyść RAM");
        clearAllItem.addActionListener(onAction(this::clearRAM));
        JMenuItem relKontaktItem = new JMenuItem("Relacje: Kontakt ↔ Zdarzenie");
        relKontaktItem.addActionListener(onAction(this::manageKontaktZdarzeniaDialog));
        JMenuItem relZdarzenieItem = new JMenuItem("Relacje: Zdarzenie ↔ Kontakty");
        relZdarzenieItem.addActionListener(onAction(this::manageZdarzenieUczestnicyDialog));
        editMenu.add(clearAllItem);
        editMenu.addSeparator();
        editMenu.add(relKontaktItem);
        editMenu.add(relZdarzenieItem);
        
        // View menu
        viewMenu = new JMenu("Widok");
        JMenuItem refreshItem = new JMenuItem("Odśwież");
        refreshItem.addActionListener(onAction(this::refreshData));
        JMenuItem showKontaktyItem = new JMenuItem("Pokaż Kontakty (przejdź)");
        showKontaktyItem.addActionListener(onAction(() -> tabbedPane.setSelectedIndex(0)));
        JMenuItem showZdarzeniaItem = new JMenuItem("Pokaż Zdarzenia (przejdź)");
        showZdarzeniaItem.addActionListener(onAction(() -> tabbedPane.setSelectedIndex(1)));
        JMenuItem showAllItem = new JMenuItem("Pokaż wszystko");
        showAllItem.addActionListener(onAction(this::showAllDialog));
        JMenuItem showKontaktyZdarzeniaItem = new JMenuItem("Kontakty > Zdarzenia");
        showKontaktyZdarzeniaItem.addActionListener(onAction(this::showKontaktyWithZdarzeniaDialog));
        JMenuItem showZdarzeniaKontaktyItem = new JMenuItem("Zdarzenia > Kontakty");
        showZdarzeniaKontaktyItem.addActionListener(onAction(this::showZdarzeniaWithKontaktyDialog));
        JMenuItem colorItem = new JMenuItem("Kolor zdarzeń...");
        colorItem.addActionListener(onAction(() -> {
            Color c = JColorChooser.showDialog(this, "Wybierz kolor zdarzeń", eventDayColor);
            if (c != null) { eventDayColor = c; fillCalendarGrid(); }
        }));
        viewMenu.add(refreshItem);
        viewMenu.addSeparator();
        viewMenu.add(showKontaktyItem);
        viewMenu.add(showZdarzeniaItem);
        viewMenu.add(showAllItem);
        viewMenu.addSeparator();
        viewMenu.add(showKontaktyZdarzeniaItem);
        viewMenu.add(showZdarzeniaKontaktyItem);
        viewMenu.addSeparator();
        viewMenu.add(colorItem);
        
        // Sort menu
        sortMenu = new JMenu("Sortuj");
        JMenuItem sortKontaktyComparable = new JMenuItem("Kontakty (Nazwisko)");
        sortKontaktyComparable.addActionListener(onAction(() -> { Collections.sort(appMemory.kontakty); refreshData(); }));
        JMenuItem sortZdarzeniaComparable = new JMenuItem("Zdarzenia (Data)");
        sortZdarzeniaComparable.addActionListener(onAction(() -> { Collections.sort(appMemory.zdarzenia); refreshData(); }));
        
        JMenu kontaktyComparatorSub = new JMenu("Kontakty przez Comparator");
        JMenuItem sortImie = new JMenuItem("Imię");
        sortImie.addActionListener(onAction(() -> { appMemory.kontakty.sort(new Kontakt.ImieComparator()); refreshData(); }));
        JMenuItem sortTel = new JMenuItem("Numer");
        sortTel.addActionListener(onAction(() -> { appMemory.kontakty.sort(new Kontakt.TelComparator()); refreshData(); }));
        JMenuItem sortEmail = new JMenuItem("E-mail");
        sortEmail.addActionListener(onAction(() -> { appMemory.kontakty.sort(new Kontakt.EmailComparator()); refreshData(); }));
        kontaktyComparatorSub.add(sortImie);
        kontaktyComparatorSub.add(sortTel);
        kontaktyComparatorSub.add(sortEmail);
        
        JMenu zdarzeniaComparatorSub = new JMenu("Zdarzenia przez Comparator");
        JMenuItem sortTytul = new JMenuItem("Tytuł");
        sortTytul.addActionListener(onAction(() -> { appMemory.zdarzenia.sort(new Zdarzenie.TytulComparator()); refreshData(); }));
        JMenuItem sortOpis = new JMenuItem("Opis");
        sortOpis.addActionListener(onAction(() -> { appMemory.zdarzenia.sort(new Zdarzenie.OpisComparator()); refreshData(); }));
        JMenuItem sortLink = new JMenuItem("Link");
        sortLink.addActionListener(onAction(() -> { appMemory.zdarzenia.sort(new Zdarzenie.LinkComparator()); refreshData(); }));
        zdarzeniaComparatorSub.add(sortTytul);
        zdarzeniaComparatorSub.add(sortOpis);
        zdarzeniaComparatorSub.add(sortLink);
        
        sortMenu.add(sortKontaktyComparable);
        sortMenu.add(kontaktyComparatorSub);
        sortMenu.addSeparator();
        sortMenu.add(sortZdarzeniaComparable);
        sortMenu.add(zdarzeniaComparatorSub);
        
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(sortMenu);
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
        addKontaktBtn.addActionListener(onAction(this::addKontakt));
        editKontaktBtn = new JButton("Edytuj");
        editKontaktBtn.addActionListener(onAction(this::editKontakt));
        deleteKontaktBtn = new JButton("Usuń");
        deleteKontaktBtn.addActionListener(onAction(this::deleteKontakt));
        
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
        addZdarzenieBtn.addActionListener(onAction(this::addZdarzenie));
        editZdarzenieBtn = new JButton("Edytuj");
        editZdarzenieBtn.addActionListener(onAction(this::editZdarzenie));
        deleteZdarzenieBtn = new JButton("Usuń");
        deleteZdarzenieBtn.addActionListener(onAction(this::deleteZdarzenie));
        
        buttonPanel.add(addZdarzenieBtn);
        buttonPanel.add(editZdarzenieBtn);
        buttonPanel.add(deleteZdarzenieBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        refreshBtn = new JButton("Odśwież");
        refreshBtn.addActionListener(onAction(this::refreshData));
        
        saveBtn = new JButton("Zapisz do bazy");
        saveBtn.addActionListener(onAction(this::saveData));

        // Pasek postępu (pokazywany przy zapisie)
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
        panel.add(progressBar);
        
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
        
        okBtn.addActionListener(onAction(() -> {
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
        }));
        
        cancelBtn.addActionListener(onAction(dialog::dispose));
        
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
        
        okBtn.addActionListener(onAction(() -> {
            k.setImie(imieField.getText());
            k.setNazwisko(nazwiskoField.getText());
            k.setTelStr(telField.getText());
            k.setEmailStr(emailField.getText());
            refreshData();
            dialog.dispose();
            JOptionPane.showMessageDialog(this, "Kontakt zaktualizowany!");
        }));
        
        cancelBtn.addActionListener(onAction(dialog::dispose));
        
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
        
        okBtn.addActionListener(onAction(() -> {
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
        }));
        
        cancelBtn.addActionListener(onAction(dialog::dispose));
        
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
        
        okBtn.addActionListener(onAction(() -> {
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
        }));
        
        cancelBtn.addActionListener(onAction(dialog::dispose));
        
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
        // Pokazuj pasek postępu podczas zapisu (bez blokowania EDT)
        JDialog dlg = new JDialog(this, "Zapisywanie...", false);
        JProgressBar pb = new JProgressBar();
        pb.setIndeterminate(true);
        dlg.add(pb);
        dlg.setSize(300, 80);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        dbManager.saveToDatabase(appMemory);
        dlg.dispose();
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

    // === Funkcje konsolowe odwzorowane w GUI ===
    private void showAllDialog() {
        StringBuilder sb = new StringBuilder();
        sb.append("[KONTAKTY]\n");
        if (appMemory.kontakty.isEmpty()) sb.append("  — brak\n");
        else {
            for (int i = 0; i < appMemory.kontakty.size(); i++) {
                Kontakt k = appMemory.kontakty.get(i);
                sb.append("  [").append(i).append("] ")
                  .append(k.getNazwisko()).append(" ").append(k.getImie())
                  .append(" | ").append(k.getTelStr())
                  .append(" | ").append(k.getEmailStr()).append("\n");
            }
        }
        sb.append("\n[ZDARZENIA]\n");
        if (appMemory.zdarzenia.isEmpty()) sb.append("  — brak\n");
        else {
            for (int i = 0; i < appMemory.zdarzenia.size(); i++) {
                Zdarzenie z = appMemory.zdarzenia.get(i);
                sb.append("  [").append(i).append("] [").append(z.getData()).append("] ")
                  .append(z.getTytul()).append(" | ").append(z.getOpis()).append("\n");
            }
        }
        showTextDialog("Wszystko", sb.toString());
    }

    private void showKontaktyWithZdarzeniaDialog() {
        StringBuilder sb = new StringBuilder();
        if (appMemory.kontakty.isEmpty()) sb.append("[!] Brak kontaktów.");
        else {
            for (Kontakt k : appMemory.kontakty) {
                sb.append(k.getNazwisko()).append(" ").append(k.getImie()).append("\n");
                List<Zdarzenie> zd = k.getZdarzenia();
                if (zd == null || zd.isEmpty()) sb.append("   — brak zdarzeń\n");
                else {
                    for (Zdarzenie z : zd) {
                        sb.append("   [").append(z.getData()).append("] ").append(z.getTytul()).append("\n");
                    }
                }
            }
        }
        showTextDialog("Kontakty > Zdarzenia", sb.toString());
    }

    private void showZdarzeniaWithKontaktyDialog() {
        StringBuilder sb = new StringBuilder();
        if (appMemory.zdarzenia.isEmpty()) sb.append("[!] Brak zdarzeń.");
        else {
            for (Zdarzenie z : appMemory.zdarzenia) {
                sb.append("[").append(z.getData()).append("] ").append(z.getTytul()).append("\n");
                List<Kontakt> uczestnicy = z.getKontakty();
                if (uczestnicy == null || uczestnicy.isEmpty()) sb.append("   — brak uczestników\n");
                else {
                    for (Kontakt k : uczestnicy) {
                        sb.append("   ").append(k.getNazwisko()).append(" ").append(k.getImie()).append("\n");
                    }
                }
            }
        }
        showTextDialog("Zdarzenia > Kontakty", sb.toString());
    }

    private void manageKontaktZdarzeniaDialog() {
        int row = kontaktyTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Wybierz kontakt w zakładce Kontakty.", "Info", JOptionPane.INFORMATION_MESSAGE);
            tabbedPane.setSelectedIndex(0);
            return;
        }
        Kontakt k = appMemory.kontakty.get(row);

        // Lista istniejących zdarzeń
        DefaultListModel<String> allEventsModel = new DefaultListModel<>();
        for (int i = 0; i < appMemory.zdarzenia.size(); i++) {
            Zdarzenie z = appMemory.zdarzenia.get(i);
            allEventsModel.addElement("[" + i + "] " + z.getData() + " | " + z.getTytul());
        }

        JList<String> allEventsList = new JList<>(allEventsModel);
        allEventsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultListModel<String> kontaktEventsModel = new DefaultListModel<>();
        List<Zdarzenie> zd = k.getZdarzenia();
        if (zd != null) {
            for (Zdarzenie z : zd) {
                kontaktEventsModel.addElement(z.getData() + " | " + z.getTytul());
            }
        }
        JList<String> kontaktEventsList = new JList<>(kontaktEventsModel);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("Kontakt: " + k.getNazwisko() + " " + k.getImie()), BorderLayout.NORTH);

        JPanel listsPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        listsPanel.add(new JScrollPane(allEventsList));
        listsPanel.add(new JScrollPane(kontaktEventsList));
        panel.add(listsPanel, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("Dodaj");
        JButton removeBtn = new JButton("Usuń");
        JButton closeBtn = new JButton("Zamknij");
        btns.add(addBtn); btns.add(removeBtn); btns.add(closeBtn);
        panel.add(btns, BorderLayout.SOUTH);

        JDialog dialog = new JDialog(this, "Relacje: Kontakt ↔ Zdarzenie", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.add(panel);

        addBtn.addActionListener(onAction(() -> {
            int idx = allEventsList.getSelectedIndex();
            if (idx < 0) return;
            Zdarzenie z = appMemory.zdarzenia.get(idx);
            k.dodajZdarzenie(z);
            z.dodajKontakt(k);
            kontaktEventsModel.addElement(z.getData() + " | " + z.getTytul());
            refreshData();
        }));
        removeBtn.addActionListener(onAction(() -> {
            int idx = kontaktEventsList.getSelectedIndex();
            if (idx < 0) return;
            Zdarzenie z = k.getZdarzenia().get(idx);
            k.usunZdarzenie(z);
            z.usunKontakt(k);
            kontaktEventsModel.remove(idx);
            refreshData();
        }));
        closeBtn.addActionListener(onAction(dialog::dispose));

        dialog.setVisible(true);
    }

    private void manageZdarzenieUczestnicyDialog() {
        int row = zdarzeniaTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Wybierz zdarzenie w zakładce Zdarzenia.", "Info", JOptionPane.INFORMATION_MESSAGE);
            tabbedPane.setSelectedIndex(1);
            return;
        }
        Zdarzenie z = appMemory.zdarzenia.get(row);

        DefaultListModel<String> allKontaktyModel = new DefaultListModel<>();
        for (int i = 0; i < appMemory.kontakty.size(); i++) {
            Kontakt k = appMemory.kontakty.get(i);
            allKontaktyModel.addElement("[" + i + "] " + k.getNazwisko() + " " + k.getImie());
        }
        JList<String> allKontaktyList = new JList<>(allKontaktyModel);
        allKontaktyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultListModel<String> uczestnicyModel = new DefaultListModel<>();
        List<Kontakt> uc = z.getKontakty();
        if (uc != null) {
            for (Kontakt k : uc) {
                uczestnicyModel.addElement(k.getNazwisko() + " " + k.getImie());
            }
        }
        JList<String> uczestnicyList = new JList<>(uczestnicyModel);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("Zdarzenie: " + z.getTytul() + " [" + z.getData() + "]"), BorderLayout.NORTH);

        JPanel listsPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        listsPanel.add(new JScrollPane(allKontaktyList));
        listsPanel.add(new JScrollPane(uczestnicyList));
        panel.add(listsPanel, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("Dodaj");
        JButton removeBtn = new JButton("Usuń");
        JButton closeBtn = new JButton("Zamknij");
        btns.add(addBtn); btns.add(removeBtn); btns.add(closeBtn);
        panel.add(btns, BorderLayout.SOUTH);

        JDialog dialog = new JDialog(this, "Relacje: Zdarzenie ↔ Kontakty", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.add(panel);

        addBtn.addActionListener(onAction(() -> {
            int idx = allKontaktyList.getSelectedIndex();
            if (idx < 0) return;
            Kontakt k = appMemory.kontakty.get(idx);
            z.dodajKontakt(k);
            k.dodajZdarzenie(z);
            uczestnicyModel.addElement(k.getNazwisko() + " " + k.getImie());
            refreshData();
        }));
        removeBtn.addActionListener(onAction(() -> {
            int idx = uczestnicyList.getSelectedIndex();
            if (idx < 0) return;
            Kontakt k = z.getKontakty().get(idx);
            z.usunKontakt(k);
            k.usunZdarzenie(z);
            uczestnicyModel.remove(idx);
            refreshData();
        }));
        closeBtn.addActionListener(onAction(dialog::dispose));

        dialog.setVisible(true);
    }

    private void showTextDialog(String title, String content) {
        javax.swing.JTextArea area = new javax.swing.JTextArea(content);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(700, 450));
        JOptionPane.showMessageDialog(this, scroll, title, JOptionPane.INFORMATION_MESSAGE);
    }
}
