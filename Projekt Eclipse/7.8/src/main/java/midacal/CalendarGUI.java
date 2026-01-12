package midacal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.time.LocalDate;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

public class CalendarGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private final Main.MemoryContainer appMemory;
    private final DBManager dbManager;
    
    // Mechanizm auto-zapisu
    private int changeCounter = 0;
    private static final int CHANGE_INTERVAL = 10; // Co 10 zmian -> auto-zapis do bazy
    
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
    private Color eventDayColor = new Color(0, 102, 0);
    
    private JMenuBar menuBar;
    private JMenu fileMenu, editMenu, viewMenu;
    
    private JButton addKontaktBtn, editKontaktBtn, deleteKontaktBtn;
    private JButton addZdarzenieBtn, editZdarzenieBtn, deleteZdarzenieBtn;
    private JButton refreshBtn, saveBtn;
    
    private javax.swing.JTextArea calendarDayDetailsArea; // Panel szczegółów dnia w kalendarzu
    
    // Szczegóły paneli dla Kontaktów i Zdarzeń - podzielone na sekcje
    private javax.swing.JTextArea kontaktyDetailsArea;
    private javax.swing.JTextArea kontaktyEventsArea;
    private javax.swing.JTextArea zdarzeniaDetailsArea;
    private javax.swing.JTextArea zdarzeniaParticipantsArea;
    
    public CalendarGUI(Main.MemoryContainer memory, DBManager dbMgr) {
        this.appMemory = memory;
        this.dbManager = dbMgr;
        
        initializeGUI();
    }
    
    private java.awt.event.ActionListener onAction(Runnable r) { return e -> { if (e != null) r.run(); }; }

    private void initializeGUI() {
        setTitle("KALENDARZ MIDACAL - Tryb Graficzny");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
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
        
        tabbedPane.addTab("Kalendarz", kalendarzPanel);
        tabbedPane.addTab("Kontakty", kontaktyPanel);
        tabbedPane.addTab("Zdarzenia", zdarzeniaPanel);
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Panel dolny - przyciski akcji
        JPanel actionPanel = createActionPanel();
        mainPanel.add(actionPanel, BorderLayout.SOUTH);
        
        getContentPane().add(mainPanel);
        
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

        // Pasek nawigacji - wszystko w jednej linii
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        navPanel.setBackground(new Color(245, 245, 250));
        navPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 210)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Nagłówek z aktualnym miesiącem i rokiem
        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(monthLabel.getFont().deriveFont(Font.BOLD, 20f));
        monthLabel.setForeground(new Color(40, 40, 50));
        monthLabel.setPreferredSize(new Dimension(200, 30));
        
        // Przyciski nawigacji
        JButton prevMonthBtn = new JButton("◀ Poprzedni");
        JButton nextMonthBtn = new JButton("Następny ▶");
        prevMonthBtn.setFont(prevMonthBtn.getFont().deriveFont(Font.BOLD, 12f));
        nextMonthBtn.setFont(nextMonthBtn.getFont().deriveFont(Font.BOLD, 12f));
        prevMonthBtn.setFocusPainted(false);
        nextMonthBtn.setFocusPainted(false);
        
        // DatePicker w środku
        com.github.lgooddatepicker.components.DatePickerSettings dateSettings = new com.github.lgooddatepicker.components.DatePickerSettings();
        dateSettings.setFormatForDatesCommonEra("yyyy-MM-dd");
        com.github.lgooddatepicker.components.DatePicker monthDatePicker = new com.github.lgooddatepicker.components.DatePicker(dateSettings);
        monthDatePicker.setDate(LocalDate.now());
        monthDatePicker.addPropertyChangeListener("date", evt -> {
            LocalDate pickedDate = monthDatePicker.getDate();
            if (pickedDate != null) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(pickedDate.getYear(), pickedDate.getMonthValue() - 1, pickedDate.getDayOfMonth());
                currentMonth = cal;
                updateMonthDisplay();
                fillCalendarGrid();
            }
        });
        
        // Wszystko w jednej linii: Poprzedni | Nagłówek | DatePicker | Następny
        navPanel.add(prevMonthBtn);
        navPanel.add(monthLabel);
        navPanel.add(monthDatePicker);
        navPanel.add(nextMonthBtn);
        panel.add(navPanel, BorderLayout.NORTH);

        // Panel informacji o dniu (poniżej kalendarza)
        JPanel dayDetailsPanel = new JPanel(new BorderLayout(10, 10));
        dayDetailsPanel.setBackground(new Color(250, 250, 255));
        dayDetailsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 220), 2),
            "Szczegóły wybranego dnia",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font(Font.SANS_SERIF, Font.BOLD, 12),
            new Color(50, 50, 70)
        ));
        dayDetailsPanel.setPreferredSize(new Dimension(0, 140));
        calendarDayDetailsArea = new javax.swing.JTextArea();
        calendarDayDetailsArea.setEditable(false);
        calendarDayDetailsArea.setLineWrap(true);
        calendarDayDetailsArea.setWrapStyleWord(true);
        JScrollPane detailsScroll = new JScrollPane(calendarDayDetailsArea);
        dayDetailsPanel.add(detailsScroll, BorderLayout.CENTER);

        // Siatka dni 7x7 (nagłówki dni tygodnia + maks. 6 tygodni)
        calendarGrid = new JPanel(new GridLayout(7, 7, 4, 4));
        calendarGrid.setBackground(new Color(255, 255, 255));
        calendarGrid.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Górna część: opakowanie siatki
        JPanel topCalendarWrapper = new JPanel(new BorderLayout(10, 10));
        topCalendarWrapper.add(calendarGrid, BorderLayout.CENTER);

        // JSplitPane dzielący kalendarz i szczegóły dnia - większy kalendarz
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topCalendarWrapper, dayDetailsPanel);
        splitPane.setResizeWeight(0.85); // Większa proporcja na kalendarz
        splitPane.setOneTouchExpandable(true);
        
        panel.add(splitPane, BorderLayout.CENTER);

        // Inicjalizacja bieżącego miesiąca
        currentMonth = java.util.Calendar.getInstance();
        currentMonth.set(java.util.Calendar.DAY_OF_MONTH, 1);

        // Zdarzenia przycisków nawigacji miesiąca
        prevMonthBtn.addActionListener(onAction(() -> { currentMonth.add(java.util.Calendar.MONTH, -1); updateMonthDisplay(); fillCalendarGrid(); }));
        nextMonthBtn.addActionListener(onAction(() -> { currentMonth.add(java.util.Calendar.MONTH, 1); updateMonthDisplay(); fillCalendarGrid(); }));

        // Wypełnij kafelki po raz pierwszy
        updateMonthDisplay();
        fillCalendarGrid();
        return panel;
    }

    private void fillCalendarGrid() {
        calendarGrid.removeAll();

        // Nagłówki dni tygodnia (Pn..Nd) - profesjonalny wygląd
        String[] dni = {"Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd"};
        for (int i = 0; i < dni.length; i++) {
            JLabel hdr = new JLabel(dni[i], SwingConstants.CENTER);
            hdr.setFont(hdr.getFont().deriveFont(Font.BOLD, 13f));
            hdr.setOpaque(true);
            // Weekend - inny kolor
            if (i >= 5) {
                hdr.setBackground(new Color(80, 120, 180)); // Weekend - ciemniejszy niebieski
            } else {
                hdr.setBackground(new Color(70, 130, 200)); // Dzień roboczy - jasny niebieski
            }
            hdr.setForeground(Color.WHITE);
            hdr.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 90, 150), 1),
                BorderFactory.createEmptyBorder(8, 2, 8, 2)
            ));
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

        // Dni miesiąca - profesjonalny wygląd kafelków
        java.time.LocalDate today = java.time.LocalDate.now();
        for (int day = 1; day <= daysInMonth; day++) {
            JPanel dayPanel = new JPanel(new BorderLayout(2, 2));
            dayPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 230), 1),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
            ));
            dayPanel.setOpaque(true);
            dayPanel.setBackground(new Color(252, 252, 255)); // Subtelny odcień białego

            JLabel dayLabel = new JLabel(String.valueOf(day), SwingConstants.CENTER);
            dayLabel.setFont(dayLabel.getFont().deriveFont(Font.BOLD, 13f));
            dayLabel.setForeground(new Color(40, 40, 50));
            dayPanel.add(dayLabel, BorderLayout.NORTH);

            // Podświetl dzisiejszy dzień - elegancki design
            if (today.getYear() == y && today.getMonthValue() - 1 == m && today.getDayOfMonth() == day) {
                dayPanel.setBackground(new Color(255, 250, 205)); // Delikatny żółty
                dayPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
                    BorderFactory.createEmptyBorder(3, 3, 3, 3)
                ));
                dayLabel.setFont(dayLabel.getFont().deriveFont(Font.BOLD, 14f));
                dayLabel.setForeground(new Color(180, 100, 0)); // Ciemniejszy tekst
            }

            // Zbierz zdarzenia w tym dniu i wyświetl tytuły
            java.util.List<Zdarzenie> eventsForDay = new java.util.ArrayList<>();
            for (Zdarzenie z : appMemory.zdarzenia) {
                java.time.LocalDate d = z.getData();
                if (d != null && d.getYear() == y && d.getMonthValue() - 1 == m && d.getDayOfMonth() == day) {
                    eventsForDay.add(z);
                }
            }

            // Przygotuj wspólny klik dla całego kafelka
            final int dayOfMonth = day;
            final int yearVal = y;
            final int monthZeroBased = m;
            java.awt.event.MouseAdapter dayClick = new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    java.time.LocalDate date = java.time.LocalDate.of(yearVal, monthZeroBased + 1, dayOfMonth);
                    displayDayDetails(date);
                }
            };

            if (!eventsForDay.isEmpty()) {
                // Całe tło zielone, jeśli są zdarzenia
                dayPanel.setBackground(new Color(144, 238, 144)); // Jasna zieleń na cały kafelek
                dayPanel.setBorder(BorderFactory.createLineBorder(new Color(34, 139, 34), 1)); // Ciemnozielony border

                StringBuilder sb = new StringBuilder();
                int maxShow = 2;
                for (int i = 0; i < eventsForDay.size() && i < maxShow; i++) {
                    Zdarzenie z = eventsForDay.get(i);
                    String title = z.getTytul() != null ? z.getTytul() : "(bez tytułu)";
                    sb.append("• ").append(title);
                    if (i < Math.min(eventsForDay.size(), maxShow) - 1) sb.append('\n');
                }
                if (eventsForDay.size() > maxShow) {
                    sb.append('\n').append("…(+").append(eventsForDay.size() - maxShow).append(")");
                }

                javax.swing.JTextArea area = new javax.swing.JTextArea(sb.toString());
                area.setEditable(false);
                area.setLineWrap(true);
                area.setWrapStyleWord(true);
                area.setOpaque(false); // Przezroczyste - pokaż zielone tło panelu
                area.setForeground(new Color(0, 100, 0)); // Ciemna zieleń tekstu
                area.setFont(area.getFont().deriveFont(Font.BOLD, 8f));

                // Ustaw tooltip z pełną listą zdarzeń
                StringBuilder tooltip = new StringBuilder("Zdarzenia:\n");
                for (Zdarzenie z : eventsForDay) {
                    String title = z.getTytul() != null ? z.getTytul() : "(bez tytułu)";
                    tooltip.append("- ").append(title);
                    if (z.getOpis() != null && !z.getOpis().isBlank()) tooltip.append(" — ").append(z.getOpis());
                    tooltip.append('\n');
                }
                dayPanel.setToolTipText(tooltip.toString());
                dayPanel.add(area, BorderLayout.CENTER);
                // Klikalność również na tekście zdarzeń
                area.addMouseListener(dayClick);
                area.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            }

            dayPanel.addMouseListener(dayClick);
            dayLabel.addMouseListener(dayClick);
            dayPanel.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            dayLabel.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

            calendarGrid.add(dayPanel);
        }

        // Uzupełnij siatkę do 7x7
        int cellsFilled = 7 + dowIndex + daysInMonth; // 7 nagłówków + offset + dni
        for (int i = cellsFilled; i < 49; i++) {
            calendarGrid.add(new JLabel(""));
        }

        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    private void updateMonthDisplay() {
        String[] miesiace = {"Styczeń","Luty","Marzec","Kwiecień","Maj","Czerwiec","Lipiec","Sierpień","Wrzesień","Październik","Listopad","Grudzień"};
        int currentMonthIndex = currentMonth.get(java.util.Calendar.MONTH);
        int currentYear = currentMonth.get(java.util.Calendar.YEAR);
        monthLabel.setText(miesiace[currentMonthIndex] + " " + currentYear);
    }

    
    private void createMenuBar() {
        menuBar = new JMenuBar();
        
        // File menu
        fileMenu = new JMenu("Plik");
        JMenuItem openFileItem = new JMenuItem("Otwórz plik XML...");
        openFileItem.addActionListener(onAction(() -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("XML Files (*.xml)", "xml"));
            int res = fc.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fc.getSelectedFile();
                try {
                    com.fasterxml.jackson.dataformat.xml.XmlMapper mapper = new com.fasterxml.jackson.dataformat.xml.XmlMapper();
                    mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                    Main.MemoryContainer loadedData = mapper.readValue(selectedFile, Main.MemoryContainer.class);
                    appMemory.kontakty = loadedData.kontakty;
                    appMemory.zdarzenia = loadedData.zdarzenia;
                    refreshData();
                    JOptionPane.showMessageDialog(null, "Plik XML wczytany pomyślnie!", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Błąd wczytywania pliku: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
                }
            }
        }));
        JMenuItem exportItem = new JMenuItem("Eksport do pliku XML...");
        exportItem.addActionListener(onAction(() -> {
            if (appMemory.kontakty.isEmpty() && appMemory.zdarzenia.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Pamięć RAM jest pusta. Nie ma danych do wyeksportowania.", "Brak danych", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("XML Files (*.xml)", "xml"));
            fc.setSelectedFile(new File("export.xml"));
            int res = fc.showSaveDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fc.getSelectedFile();
                try {
                    com.fasterxml.jackson.dataformat.xml.XmlMapper mapper = new com.fasterxml.jackson.dataformat.xml.XmlMapper();
                    mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                    mapper.writerWithDefaultPrettyPrinter().writeValue(selectedFile, appMemory);
                    JOptionPane.showMessageDialog(null, "Dane wyeksportowane do XML pomyślnie!", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Błąd eksportu: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
                }
            }
        }));
        JMenuItem exitItem = new JMenuItem("Wyjście");
        exitItem.addActionListener(onAction(this::saveAndExit));
        fileMenu.add(openFileItem);
        fileMenu.add(exportItem);
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
        JScrollPane tableScroll = new JScrollPane(kontaktyTable);

        // Panel szczegółów kontaktu (góra) - prosty tekst
        kontaktyDetailsArea = new javax.swing.JTextArea();
        kontaktyDetailsArea.setEditable(false);
        kontaktyDetailsArea.setLineWrap(true);
        kontaktyDetailsArea.setWrapStyleWord(true);
        kontaktyDetailsArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane detailsScroll = new JScrollPane(kontaktyDetailsArea);
        detailsScroll.setBorder(BorderFactory.createTitledBorder("Szczegóły kontaktu"));
        
        // Panel przypisanych zdarzeń (dół) - prosty tekst
        kontaktyEventsArea = new javax.swing.JTextArea();
        kontaktyEventsArea.setEditable(false);
        kontaktyEventsArea.setLineWrap(true);
        kontaktyEventsArea.setWrapStyleWord(true);
        kontaktyEventsArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane eventsScroll = new JScrollPane(kontaktyEventsArea);
        eventsScroll.setBorder(BorderFactory.createTitledBorder("Przypisane zdarzenia"));
        
        // JSplitPane pionowy dzielący szczegóły kontaktu i zdarzenia
        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, detailsScroll, eventsScroll);
        rightSplitPane.setResizeWeight(0.4);
        rightSplitPane.setOneTouchExpandable(true);
        
        // JSplitPane poziomy dzielący tabelę i panel szczegółów
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScroll, rightSplitPane);
        splitPane.setResizeWeight(0.45);
        splitPane.setOneTouchExpandable(true);
        
        // Panel przycisków
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
        buttonPanel.add(javax.swing.Box.createHorizontalStrut(20));
        
        // Sortowanie
        JButton sortByNameBtn = new JButton("Sortuj (Nazwisko)");
        sortByNameBtn.addActionListener(onAction(() -> { Collections.sort(appMemory.kontakty); refreshData(); }));
        JButton sortByImieBtn = new JButton("Sortuj (Imię)");
        sortByImieBtn.addActionListener(onAction(() -> { appMemory.kontakty.sort(new Kontakt.ImieComparator()); refreshData(); }));
        JButton sortByPhoneBtn = new JButton("Sortuj (Telefon)");
        sortByPhoneBtn.addActionListener(onAction(() -> { appMemory.kontakty.sort(new Kontakt.TelComparator()); refreshData(); }));
        JButton sortByEmailBtn = new JButton("Sortuj (Email)");
        sortByEmailBtn.addActionListener(onAction(() -> { appMemory.kontakty.sort(new Kontakt.EmailComparator()); refreshData(); }));
        
        buttonPanel.add(sortByNameBtn);
        buttonPanel.add(sortByImieBtn);
        buttonPanel.add(sortByPhoneBtn);
        buttonPanel.add(sortByEmailBtn);

        panel.add(splitPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Nasłuch zaznaczenia
        kontaktyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = kontaktyTable.getSelectedRow();
                if (row >= 0 && row < appMemory.kontakty.size()) {
                    showKontaktDetailsInPanel(appMemory.kontakty.get(row));
                }
            }
        });
        
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
        JScrollPane tableScroll = new JScrollPane(zdarzeniaTable);

        // Panel szczegółów zdarzenia (góra) - prosty tekst
        zdarzeniaDetailsArea = new javax.swing.JTextArea();
        zdarzeniaDetailsArea.setEditable(false);
        zdarzeniaDetailsArea.setLineWrap(true);
        zdarzeniaDetailsArea.setWrapStyleWord(true);
        zdarzeniaDetailsArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane detailsScroll = new JScrollPane(zdarzeniaDetailsArea);
        detailsScroll.setBorder(BorderFactory.createTitledBorder("Szczegóły zdarzenia"));
        
        // Panel uczestników (dół) - prosty tekst
        zdarzeniaParticipantsArea = new javax.swing.JTextArea();
        zdarzeniaParticipantsArea.setEditable(false);
        zdarzeniaParticipantsArea.setLineWrap(true);
        zdarzeniaParticipantsArea.setWrapStyleWord(true);
        zdarzeniaParticipantsArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane participantsScroll = new JScrollPane(zdarzeniaParticipantsArea);
        participantsScroll.setBorder(BorderFactory.createTitledBorder("Uczestnicy"));
        
        // JSplitPane pionowy dzielący szczegóły zdarzenia i uczestników
        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, detailsScroll, participantsScroll);
        rightSplitPane.setResizeWeight(0.5);
        rightSplitPane.setOneTouchExpandable(true);
        
        // JSplitPane poziomy dzielący tabelę i panel szczegółów
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScroll, rightSplitPane);
        splitPane.setResizeWeight(0.45);
        splitPane.setOneTouchExpandable(true);
        
        // Panel przycisków
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
        buttonPanel.add(javax.swing.Box.createHorizontalStrut(20));
        
        // Sortowanie
        JButton sortByDateBtn = new JButton("Sortuj (Data)");
        sortByDateBtn.addActionListener(onAction(() -> { Collections.sort(appMemory.zdarzenia); refreshData(); }));
        JButton sortByTitleBtn = new JButton("Sortuj (Tytuł)");
        sortByTitleBtn.addActionListener(onAction(() -> { appMemory.zdarzenia.sort(new Zdarzenie.TytulComparator()); refreshData(); }));
        JButton sortByDescBtn = new JButton("Sortuj (Opis)");
        sortByDescBtn.addActionListener(onAction(() -> { appMemory.zdarzenia.sort(new Zdarzenie.OpisComparator()); refreshData(); }));
        JButton sortByLinkBtn = new JButton("Sortuj (Link)");
        sortByLinkBtn.addActionListener(onAction(() -> { appMemory.zdarzenia.sort(new Zdarzenie.LinkComparator()); refreshData(); }));
        
        buttonPanel.add(sortByDateBtn);
        buttonPanel.add(sortByTitleBtn);
        buttonPanel.add(sortByDescBtn);
        buttonPanel.add(sortByLinkBtn);

        panel.add(splitPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Nasłuch zaznaczenia
        zdarzeniaTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = zdarzeniaTable.getSelectedRow();
                if (row >= 0 && row < appMemory.zdarzenia.size()) {
                    showZdarzenieDetailsInPanel(appMemory.zdarzenia.get(row));
                }
            }
        });
        
        return panel;
    }
    
    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        refreshBtn = new JButton("Odśwież");
        refreshBtn.addActionListener(onAction(this::refreshData));
        
        JButton loadXmlBtn = new JButton("Wczytaj XML");
        loadXmlBtn.addActionListener(onAction(this::loadDataFromXml));
        
        JButton saveXmlBtn = new JButton("Zapisz XML");
        saveXmlBtn.addActionListener(onAction(this::saveDataToXml));
        
        saveBtn = new JButton("Zapisz do bazy");
        saveBtn.addActionListener(onAction(this::saveData));

        // Pasek postępu (pokazywany przy zapisie)
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
        panel.add(progressBar);
        
        panel.add(refreshBtn);
        panel.add(loadXmlBtn);
        panel.add(saveXmlBtn);
        panel.add(saveBtn);
        
        return panel;
    }

    private void loadDataFromXml() {
        try {
            System.out.println("[GUI] Wczytywanie danych z XML...");
            Main.loadFromXml();
            appMemory.kontakty = Main.appMemory.kontakty;
            appMemory.zdarzenia = Main.appMemory.zdarzenia;
            System.out.println("[GUI] Wczytano z XML pomyślnie!");
            refreshData();
        } catch (Exception e) {
            System.err.println("[GUI ERROR] Błąd wczytywania XML: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Błąd wczytywania XML:\n" + e.getMessage(), 
                "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveDataToXml() {
        try {
            System.out.println("[GUI] Zapisywanie danych do XML...");
            Main.saveToXml();
            System.out.println("[GUI] Zapisano do XML pomyślnie!");
            JOptionPane.showMessageDialog(this, "Dane zapisane do XML pomyślnie!", 
                "Sukces", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            System.err.println("[GUI ERROR] Błąd zapisywania XML: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Błąd zapisywania XML:\n" + e.getMessage(), 
                "Błąd", JOptionPane.ERROR_MESSAGE);
        }
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

        // Odśwież widok kalendarza oraz wyczyść szczegóły dnia
        if (calendarDayDetailsArea != null) {
            calendarDayDetailsArea.setText("");
        }
        if (calendarGrid != null) {
            fillCalendarGrid();
        }
    }
    
    // Mechanizm auto-zapisu co 10 zmian
    private void markChanged() {
        changeCounter++;
        if (changeCounter >= CHANGE_INTERVAL) {
            System.out.println("[AUTO] Backup do bazy po " + changeCounter + " zmianach w RAM...");
            dbManager.saveToDatabase(appMemory);
            // Auto-save do XML co 10 zmian (dla dodatkowego backup'u)
            try {
                System.out.println("[AUTO] Auto-backup do XML...");
                Main.saveToXml();
            } catch (Exception e) {
                System.err.println("[AUTO ERROR] Nie udało się auto-zapisać do XML: " + e.getMessage());
            }
            changeCounter = 0;
        }
    }
    
    private void addKontakt() {
        JDialog dialog = new JDialog((java.awt.Frame) null, "Dodaj Kontakt", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel z podstawowymi danymi
        JPanel dataPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        dataPanel.setBorder(BorderFactory.createTitledBorder("Dane kontaktu"));
        
        JTextField imieField = new JTextField();
        JTextField nazwiskoField = new JTextField();
        JTextField telField = new JTextField();
        JTextField emailField = new JTextField();
        
        dataPanel.add(new JLabel("Imię:"));
        dataPanel.add(imieField);
        dataPanel.add(new JLabel("Nazwisko:"));
        dataPanel.add(nazwiskoField);
        dataPanel.add(new JLabel("Telefon:"));
        dataPanel.add(telField);
        dataPanel.add(new JLabel("Email:"));
        dataPanel.add(emailField);
        
        mainPanel.add(dataPanel, BorderLayout.NORTH);
        
        // Panel zdarzeń
        JPanel eventsPanel = new JPanel(new BorderLayout(10, 10));
        eventsPanel.setBorder(BorderFactory.createTitledBorder("Przypisz do zdarzeń"));
        
        DefaultListModel<String> eventsModel = new DefaultListModel<>();
        JList<String> eventsList = new JList<>(eventsModel);
        eventsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        // Wypełnij listę zdarzeniami
        for (Zdarzenie z : appMemory.zdarzenia) {
            eventsModel.addElement(z.getTytul() + " (" + z.getData() + ")");
        }
        
        JScrollPane eventsScroll = new JScrollPane(eventsList);
        eventsPanel.add(new JLabel("Zaznacz zdarzenia (można wybrać wiele):"), BorderLayout.NORTH);
        eventsPanel.add(eventsScroll, BorderLayout.CENTER);
        
        mainPanel.add(eventsPanel, BorderLayout.CENTER);
        
        // Przyciski
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
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
                
                // Dodaj kontakt do wybranych zdarzeń
                int[] selectedIndices = eventsList.getSelectedIndices();
                for (int idx : selectedIndices) {
                    Zdarzenie z = appMemory.zdarzenia.get(idx);
                    if (z.getKontakty() == null) {
                        z.setKontakty(new java.util.ArrayList<>());
                    }
                    z.getKontakty().add(k);
                    
                    if (k.getZdarzenia() == null) {
                        k.setZdarzenia(new java.util.ArrayList<>());
                    }
                    k.getZdarzenia().add(z);
                }
                
                appMemory.kontakty.add(k);
                markChanged();
                refreshData();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Kontakt dodany pomyślnie!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Błąd: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }));
        
        cancelBtn.addActionListener(onAction(dialog::dispose));
        
        buttonPanel.add(okBtn);
        buttonPanel.add(cancelBtn);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.getContentPane().add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void editKontakt() {
        int row = kontaktyTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(null, "Wybierz kontakt do edycji!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        Kontakt k = appMemory.kontakty.get(row);
        JDialog dialog = new JDialog((java.awt.Frame) null, "Edytuj Kontakt", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel z podstawowymi danymi
        JPanel dataPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        dataPanel.setBorder(BorderFactory.createTitledBorder("Dane kontaktu"));
        
        JTextField imieField = new JTextField(k.getImie());
        JTextField nazwiskoField = new JTextField(k.getNazwisko());
        JTextField telField = new JTextField(k.getTelStr());
        JTextField emailField = new JTextField(k.getEmailStr());
        
        dataPanel.add(new JLabel("Imię:"));
        dataPanel.add(imieField);
        dataPanel.add(new JLabel("Nazwisko:"));
        dataPanel.add(nazwiskoField);
        dataPanel.add(new JLabel("Telefon:"));
        dataPanel.add(telField);
        dataPanel.add(new JLabel("Email:"));
        dataPanel.add(emailField);
        
        mainPanel.add(dataPanel, BorderLayout.NORTH);
        
        // Panel zdarzeń
        JPanel eventsPanel = new JPanel(new BorderLayout(10, 10));
        eventsPanel.setBorder(BorderFactory.createTitledBorder("Przypisane zdarzenia"));
        
        DefaultListModel<String> eventsModel = new DefaultListModel<>();
        JList<String> eventsList = new JList<>(eventsModel);
        eventsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        // Zaznacz aktualnie przypisane zdarzenia - porównuj po tytule i dacie
        java.util.List<Integer> selectedIndices = new java.util.ArrayList<>();
        for (int i = 0; i < appMemory.zdarzenia.size(); i++) {
            Zdarzenie z = appMemory.zdarzenia.get(i);
            eventsModel.addElement(z.getTytul() + " (" + z.getData() + ")");
            
            // Sprawdź czy to zdarzenie jest przypisane do kontaktu
            if (k.getZdarzenia() != null) {
                for (Zdarzenie kz : k.getZdarzenia()) {
                    if (kz.getTytul().equals(z.getTytul()) && 
                        kz.getData().equals(z.getData())) {
                        selectedIndices.add(i);
                        break;
                    }
                }
            }
        }
        
        // Ustaw zaznaczenia
        int[] indices = selectedIndices.stream().mapToInt(Integer::intValue).toArray();
        eventsList.setSelectedIndices(indices);
        eventsList.ensureIndexIsVisible(0); // Wymuszenie odświeżenia UI
        
        JScrollPane eventsScroll = new JScrollPane(eventsList);
        eventsPanel.add(new JLabel("Zaznacz zdarzenia (można wybrać wiele):"), BorderLayout.NORTH);
        eventsPanel.add(eventsScroll, BorderLayout.CENTER);
        
        mainPanel.add(eventsPanel, BorderLayout.CENTER);
        
        // Przyciski
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Anuluj");
        
        okBtn.addActionListener(onAction(() -> {
            k.setImie(imieField.getText());
            k.setNazwisko(nazwiskoField.getText());
            k.setTelStr(telField.getText());
            k.setEmailStr(emailField.getText());
            
            // Usuń kontakt ze wszystkich zdarzeń
            for (Zdarzenie z : appMemory.zdarzenia) {
                if (z.getKontakty() != null) {
                    z.getKontakty().remove(k);
                }
            }
            
            // Wyczyść listę zdarzeń w kontakcie
            if (k.getZdarzenia() == null) {
                k.setZdarzenia(new java.util.ArrayList<>());
            }
            k.getZdarzenia().clear();
            
            // Dodaj do wybranych zdarzeń
            int[] selectedIdx = eventsList.getSelectedIndices();
            for (int idx : selectedIdx) {
                Zdarzenie z = appMemory.zdarzenia.get(idx);
                if (z.getKontakty() == null) {
                    z.setKontakty(new java.util.ArrayList<>());
                }
                z.getKontakty().add(k);
                k.getZdarzenia().add(z);
            }
            
            markChanged();
            refreshData();
            dialog.dispose();
            JOptionPane.showMessageDialog(this, "Kontakt zaktualizowany!");
        }));
        
        cancelBtn.addActionListener(onAction(dialog::dispose));
        
        buttonPanel.add(okBtn);
        buttonPanel.add(cancelBtn);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.getContentPane().add(mainPanel);
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
            markChanged();
            refreshData();
            JOptionPane.showMessageDialog(this, "Kontakt usunięty!");
        }
    }
    
    private void addZdarzenie() {
        JDialog dialog = new JDialog((java.awt.Frame) null, "Dodaj Zdarzenie", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel z polami edycji
        JPanel editPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        editPanel.setBorder(BorderFactory.createTitledBorder("Szczegóły zdarzenia"));
        
        JTextField tytulField = new JTextField();
        JTextField opisField = new JTextField();
        
        // DatePickerPanel - gotowy calendar picker
        com.github.lgooddatepicker.components.DatePickerSettings settings = new com.github.lgooddatepicker.components.DatePickerSettings();
        settings.setFormatForDatesCommonEra("yyyy-MM-dd");
        com.github.lgooddatepicker.components.DatePicker datePicker = new com.github.lgooddatepicker.components.DatePicker(settings);
        datePicker.setDate(LocalDate.now());
        
        JTextField linkField = new JTextField();
        
        editPanel.add(new JLabel("Tytuł:"));
        editPanel.add(tytulField);
        editPanel.add(new JLabel("Opis:"));
        editPanel.add(opisField);
        editPanel.add(new JLabel("Data:"));
        editPanel.add(datePicker);
        editPanel.add(new JLabel("Link:"));
        editPanel.add(linkField);
        
        mainPanel.add(editPanel, BorderLayout.NORTH);
        
        // Panel z uczestnikami
        JPanel participantsPanel = new JPanel(new BorderLayout(10, 10));
        participantsPanel.setBorder(BorderFactory.createTitledBorder("Uczestnicy"));
        
        DefaultListModel<String> participantsModel = new DefaultListModel<>();
        JList<String> participantsList = new JList<>(participantsModel);
        participantsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane participantsScroll = new JScrollPane(participantsList);
        participantsPanel.add(participantsScroll, BorderLayout.CENTER);
        
        // Przyciski do zarządzania uczestnikami
        JPanel participantsButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addParticipantBtn = new JButton("Dodaj uczestnika");
        JButton removeParticipantBtn = new JButton("Usuń uczestnika");
        
        java.util.List<Kontakt> tempParticipants = new java.util.ArrayList<>();
        
        addParticipantBtn.addActionListener(onAction(() -> {
            // Filtruj kontakty - pokaż tylko te, którzy jeszcze nie są uczestnikami
            java.util.List<Kontakt> availableContacts = new java.util.ArrayList<>();
            for (Kontakt k : appMemory.kontakty) {
                if (!tempParticipants.contains(k)) {
                    availableContacts.add(k);
                }
            }
            
            if (availableContacts.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Wszystkie kontakty zostały już dodane!", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            String[] contactNames = new String[availableContacts.size()];
            for (int i = 0; i < availableContacts.size(); i++) {
                Kontakt k = availableContacts.get(i);
                contactNames[i] = k.getNazwisko() + " " + k.getImie();
            }
            
            String selected = (String) JOptionPane.showInputDialog(
                dialog,
                "Wybierz kontakt do dodania:",
                "Dodaj uczestnika",
                JOptionPane.QUESTION_MESSAGE,
                null,
                contactNames,
                contactNames[0]
            );
            
            if (selected != null) {
                for (Kontakt k : availableContacts) {
                    if ((k.getNazwisko() + " " + k.getImie()).equals(selected)) {
                        tempParticipants.add(k);
                        participantsModel.addElement(selected);
                        break;
                    }
                }
            }
        }));
        
        removeParticipantBtn.addActionListener(onAction(() -> {
            int idx = participantsList.getSelectedIndex();
            if (idx < 0) {
                JOptionPane.showMessageDialog(null, "Wybierz uczestnika do usunięcia!", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            tempParticipants.remove(idx);
            participantsModel.remove(idx);
        }));
        
        participantsButtonPanel.add(addParticipantBtn);
        participantsButtonPanel.add(removeParticipantBtn);
        participantsPanel.add(participantsButtonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(participantsPanel, BorderLayout.CENTER);
        
        // Przyciski OK/Anuluj na dole
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Anuluj");
        
        okBtn.addActionListener(onAction(() -> {
            try {
                LocalDate selectedDate = datePicker.getDate();
                if (selectedDate == null) selectedDate = LocalDate.now();
                Zdarzenie z = new Zdarzenie(
                    tytulField.getText(),
                    opisField.getText(),
                    selectedDate,
                    java.net.URI.create(linkField.getText()).toURL()
                );
                for (Kontakt k : tempParticipants) {
                    z.dodajKontakt(k);
                }
                appMemory.zdarzenia.add(z);
                markChanged();
                refreshData();
                dialog.dispose();
                JOptionPane.showMessageDialog(null, "Zdarzenie dodane pomyślnie!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Błąd: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }));
        
        cancelBtn.addActionListener(onAction(dialog::dispose));
        
        buttonPanel.add(okBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.getContentPane().add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void editZdarzenie() {
        int row = zdarzeniaTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Wybierz zdarzenie do edycji!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        Zdarzenie z = appMemory.zdarzenia.get(row);
        JDialog dialog = new JDialog((java.awt.Frame) null, "Edytuj Zdarzenie", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel z polami edycji
        JPanel editPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        editPanel.setBorder(BorderFactory.createTitledBorder("Szczegóły zdarzenia"));
        
        JTextField tytulField = new JTextField(z.getTytul());
        JTextField opisField = new JTextField(z.getOpis());
        
        // DatePickerPanel - gotowy calendar picker
        com.github.lgooddatepicker.components.DatePickerSettings settings = new com.github.lgooddatepicker.components.DatePickerSettings();
        settings.setFormatForDatesCommonEra("yyyy-MM-dd");
        com.github.lgooddatepicker.components.DatePicker datePicker = new com.github.lgooddatepicker.components.DatePicker(settings);
        datePicker.setDate(z.getData());
        
        JTextField linkField = new JTextField(z.getMiejsce() != null ? z.getMiejsce().toString() : "");
        
        editPanel.add(new JLabel("Tytuł:"));
        editPanel.add(tytulField);
        editPanel.add(new JLabel("Opis:"));
        editPanel.add(opisField);
        editPanel.add(new JLabel("Data:"));
        editPanel.add(datePicker);
        editPanel.add(new JLabel("Link:"));
        editPanel.add(linkField);
        
        mainPanel.add(editPanel, BorderLayout.NORTH);
        
        // Panel z uczestnikami
        JPanel participantsPanel = new JPanel(new BorderLayout(10, 10));
        participantsPanel.setBorder(BorderFactory.createTitledBorder("Uczestnicy"));
        
        DefaultListModel<String> participantsModel = new DefaultListModel<>();
        for (Kontakt k : z.getKontakty()) {
            participantsModel.addElement(k.getNazwisko() + " " + k.getImie());
        }
        JList<String> participantsList = new JList<>(participantsModel);
        participantsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane participantsScroll = new JScrollPane(participantsList);
        participantsPanel.add(participantsScroll, BorderLayout.CENTER);
        
        // Przyciski do zarządzania uczestnikami
        JPanel participantsButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addParticipantBtn = new JButton("Dodaj uczestnika");
        JButton removeParticipantBtn = new JButton("Usuń uczestnika");
        
        addParticipantBtn.addActionListener(onAction(() -> {
            // Filtruj kontakty - pokaż tylko te, którzy jeszcze nie są uczestnikami
            java.util.List<Kontakt> availableContacts = new java.util.ArrayList<>();
            for (Kontakt k : appMemory.kontakty) {
                if (z.getKontakty() == null || !z.getKontakty().contains(k)) {
                    availableContacts.add(k);
                }
            }
            
            if (availableContacts.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Wszystkie kontakty są już uczestnikami tego zdarzenia!", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            String[] contactNames = new String[availableContacts.size()];
            for (int i = 0; i < availableContacts.size(); i++) {
                Kontakt k = availableContacts.get(i);
                contactNames[i] = k.getNazwisko() + " " + k.getImie();
            }
            
            String selected = (String) JOptionPane.showInputDialog(
                dialog,
                "Wybierz kontakt do dodania:",
                "Dodaj uczestnika",
                JOptionPane.QUESTION_MESSAGE,
                null,
                contactNames,
                contactNames[0]
            );
            
            if (selected != null) {
                for (Kontakt k : availableContacts) {
                    if ((k.getNazwisko() + " " + k.getImie()).equals(selected)) {
                        z.dodajKontakt(k);
                        participantsModel.clear();
                        for (Kontakt participant : z.getKontakty()) {
                            participantsModel.addElement(participant.getNazwisko() + " " + participant.getImie());
                        }
                        break;
                    }
                }
            }
        }));
        
        removeParticipantBtn.addActionListener(onAction(() -> {
            int idx = participantsList.getSelectedIndex();
            if (idx < 0) {
                JOptionPane.showMessageDialog(null, "Wybierz uczestnika do usunięcia!", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            Kontakt k = z.getKontakty().get(idx);
            z.usunKontakt(k);
            participantsModel.remove(idx);
        }));
        
        participantsButtonPanel.add(addParticipantBtn);
        participantsButtonPanel.add(removeParticipantBtn);
        participantsPanel.add(participantsButtonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(participantsPanel, BorderLayout.CENTER);
        
        // Przyciski OK/Anuluj na dole
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Anuluj");
        
        okBtn.addActionListener(onAction(() -> {
            try {
                z.setTytul(tytulField.getText());
                z.setOpis(opisField.getText());
                LocalDate pickedDate = datePicker.getDate();
                if (pickedDate == null) pickedDate = LocalDate.now();
                z.setData(pickedDate);
                z.setMiejsce(java.net.URI.create(linkField.getText()).toURL());
                markChanged();
                refreshData();
                dialog.dispose();
                JOptionPane.showMessageDialog(null, "Zdarzenie zaktualizowane!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Błąd: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }));
        
        cancelBtn.addActionListener(onAction(dialog::dispose));
        
        buttonPanel.add(okBtn);
        buttonPanel.add(cancelBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.getContentPane().add(mainPanel);
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
            markChanged();
            refreshData();
            JOptionPane.showMessageDialog(this, "Zdarzenie usunięte!");
        }
    }
    
    private void saveData() {
        try {
            // Pokazuj pasek postępu podczas zapisu
            JDialog dlg = new JDialog(this, "Zapisywanie...", true); // modal = true
            JProgressBar pb = new JProgressBar();
            pb.setIndeterminate(true);
            dlg.getContentPane().add(pb);
            dlg.setSize(300, 80);
            dlg.setLocationRelativeTo(this);
            
            // Zapis w wątku aby nie zablokować EDT
            Thread saveThread = new Thread(() -> {
                System.out.println("[GUI] Rozpoczynam zapis do bazy danych...");
                dbManager.saveToDatabase(appMemory);
                System.out.println("[GUI] Zapis do bazy danych zakończony!");
                dlg.dispose();
            });
            saveThread.start();
            dlg.setVisible(true);
            
            // Czekaj aż wątek się skończy
            saveThread.join();
            
            JOptionPane.showMessageDialog(this, "Dane zapisane do bazy danych!", "Sukces", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Błąd zapisu: " + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    private void clearRAM() {
        int confirm = JOptionPane.showConfirmDialog(this, "Wyczyścić RAM?", "Potwierdzenie", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            appMemory.kontakty.clear();
            appMemory.zdarzenia.clear();
            markChanged();
            refreshData();
            JOptionPane.showMessageDialog(this, "RAM wyczyszczony!");
        }
    }
    
    private void saveAndExit() {
        String[] options = {"Zapisz i wyjdź", "Nie zapisuj i wyjdź", "Anuluj"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Zapisać zmiany przed wyjściem?",
                "Potwierdzenie",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) { // Zapisz i wyjdź
            saveData();
            System.exit(0);
        } else if (choice == 1) { // Nie zapisuj i wyjdź
            System.exit(0);
        } else {
            // Anuluj — nic nie rób
        }
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
                java.util.List<Zdarzenie> zd = k.getZdarzenia();
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
                java.util.List<Kontakt> uczestnicy = z.getKontakty();
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
            JOptionPane.showMessageDialog(null, "Wybierz kontakt w zakładce Kontakty.", "Info", JOptionPane.INFORMATION_MESSAGE);
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
        java.util.List<Zdarzenie> zd = k.getZdarzenia();
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

        JDialog dialog = new JDialog((java.awt.Frame) null, "Relacje: Kontakt ↔ Zdarzenie", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().add(panel);

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
            JOptionPane.showMessageDialog(null, "Wybierz zdarzenie w zakładce Zdarzenia.", "Info", JOptionPane.INFORMATION_MESSAGE);
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
        java.util.List<Kontakt> uc = z.getKontakty();
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

        JDialog dialog = new JDialog((java.awt.Frame) null, "Relacje: Zdarzenie ↔ Kontakty", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().add(panel);

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
        JOptionPane.showMessageDialog(null, scroll, title, JOptionPane.INFORMATION_MESSAGE);
    }

    // === Wyświetlanie szczegółów dnia w panelu (poniżej kalendarza) ===
    private void displayDayDetails(java.time.LocalDate date) {
        java.util.List<Zdarzenie> eventsForDay = new java.util.ArrayList<>();
        for (Zdarzenie z : appMemory.zdarzenia) {
            java.time.LocalDate d = z.getData();
            if (d != null && d.equals(date)) {
                eventsForDay.add(z);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(date).append(" ===\n\n");

        if (eventsForDay.isEmpty()) {
            sb.append("Brak zdarzeń w tym dniu.");
        } else {
            sb.append("Zdarzenia:\n");
            for (int i = 0; i < eventsForDay.size(); i++) {
                Zdarzenie z = eventsForDay.get(i);
                sb.append("\n[").append(i + 1).append("] ").append(z.getTytul() != null ? z.getTytul() : "(bez tytułu)").append("\n");
                if (z.getOpis() != null && !z.getOpis().isBlank()) {
                    sb.append("    Opis: ").append(z.getOpis()).append("\n");
                }
                if (z.getMiejsce() != null) {
                    sb.append("    Link: ").append(z.getMiejsce()).append("\n");
                }
            }
        }

        if (calendarDayDetailsArea != null) {
            calendarDayDetailsArea.setText(sb.toString());
            calendarDayDetailsArea.setCaretPosition(0);
        }
    }

    // Wyświetlanie szczegółów kontaktu w podzielonych panelach
    private void showKontaktDetailsInPanel(Kontakt k) {
        // Panel szczegółów kontaktu (góra)
        StringBuilder details = new StringBuilder();
        details.append("SZCZEGÓŁY KONTAKTU\n\n");
        details.append("Imię: ").append(k.getImie()).append("\n");
        details.append("Nazwisko: ").append(k.getNazwisko()).append("\n");
        if (k.getTelStr() != null && !k.getTelStr().isBlank())
            details.append("Telefon: ").append(k.getTelStr()).append("\n");
        if (k.getEmailStr() != null && !k.getEmailStr().isBlank())
            details.append("Email: ").append(k.getEmailStr()).append("\n");
        
        if (kontaktyDetailsArea != null) {
            kontaktyDetailsArea.setText(details.toString());
            kontaktyDetailsArea.setCaretPosition(0);
        }
        
        // Panel przypisanych zdarzeń (dół)
        java.util.List<Zdarzenie> events = k.getZdarzenia();
        StringBuilder eventsText = new StringBuilder();
        eventsText.append("PRZYPISANE ZDARZENIA (").append(events != null ? events.size() : 0).append(")\n\n");
        
        if (events == null || events.isEmpty()) {
            eventsText.append("Brak przypisanych zdarzeń");
        } else {
            for (int i = 0; i < events.size(); i++) {
                Zdarzenie z = events.get(i);
                eventsText.append(i + 1).append(". ").append(z.getTytul() != null ? z.getTytul() : "(bez tytułu)").append("\n");
                eventsText.append("   Data: ").append(z.getData()).append("\n");
                if (z.getOpis() != null && !z.getOpis().isBlank())
                    eventsText.append("   Opis: ").append(z.getOpis()).append("\n");
                if (i < events.size() - 1)
                    eventsText.append("\n");
            }
        }
        
        if (kontaktyEventsArea != null) {
            kontaktyEventsArea.setText(eventsText.toString());
            kontaktyEventsArea.setCaretPosition(0);
        }
    }

    private void showZdarzenieDetailsInPanel(Zdarzenie z) {
        // Panel szczegółów zdarzenia (góra)
        StringBuilder details = new StringBuilder();
        details.append("SZCZEGÓŁY ZDARZENIA\n\n");
        details.append("Tytuł: ").append(z.getTytul() != null ? z.getTytul() : "(bez tytułu)").append("\n");
        if (z.getData() != null)
            details.append("Data: ").append(z.getData()).append("\n");
        if (z.getOpis() != null && !z.getOpis().isBlank())
            details.append("Opis: ").append(z.getOpis()).append("\n");
        if (z.getMiejsce() != null)
            details.append("Link: ").append(z.getMiejsce()).append("\n");
        
        if (zdarzeniaDetailsArea != null) {
            zdarzeniaDetailsArea.setText(details.toString());
            zdarzeniaDetailsArea.setCaretPosition(0);
        }
        
        // Panel uczestników (dół)
        java.util.List<Kontakt> kontakty = z.getKontakty();
        StringBuilder participantsText = new StringBuilder();
        participantsText.append("UCZESTNICY (").append(kontakty != null ? kontakty.size() : 0).append(")\n\n");
        
        if (kontakty == null || kontakty.isEmpty()) {
            participantsText.append("Brak przypisanych uczestników");
        } else {
            for (int i = 0; i < kontakty.size(); i++) {
                Kontakt k = kontakty.get(i);
                participantsText.append(i + 1).append(". ").append(k.getNazwisko()).append(" ").append(k.getImie()).append("\n");
                if (k.getTelStr() != null && !k.getTelStr().isBlank())
                    participantsText.append("   Tel: ").append(k.getTelStr()).append("\n");
                if (k.getEmailStr() != null && !k.getEmailStr().isBlank())
                    participantsText.append("   Email: ").append(k.getEmailStr()).append("\n");
                if (i < kontakty.size() - 1)
                    participantsText.append("\n");
            }
        }
        
        if (zdarzeniaParticipantsArea != null) {
            zdarzeniaParticipantsArea.setText(participantsText.toString());
            zdarzeniaParticipantsArea.setCaretPosition(0);
        }
    }
}
