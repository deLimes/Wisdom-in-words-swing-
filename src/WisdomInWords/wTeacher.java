package WisdomInWords;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 * Created by User on 06.12.2017.
 */
public class wTeacher extends JFrame {

    private JPanel panel;
    private JPanel topPanel;
    private JFrame frame;
    private Point compCoords;
    private DefaultTableModel dtm;
    private TableColumnModel columnModel;
    private Boolean englishLeft = true;
    JTable table;
    boolean showAnswers, hideAnswers;
    boolean answersAreHidden;
    boolean tableChanged, rowChanged, columnChanged, filterChanged;
    String original, answer;
    int keyPreviousRow, previousRow, previousColumn;
    JScrollPane scrollPane;
    JPanel contentPane = new JPanel();
    JButton btnrBack;
    boolean flagEnSwitch, flagRuSwitch, stringSwitch;
    int numberOfBlocks = 1, numberOfCollocationsInABlock = 1, fontSize = 12, portNumber = 7373;
    int rowBeginIndexOfLearnedWords = 0, rowBeginIndexOfWellLearnedWords = 0, rowBeginIndexOfNativeWords = 0;
    int countOfLearnedWords = 0;
    int countOfDifficultWords = 0;
    Integer sendersCreated = 0;
    List<Collocation> listDictionary = new ArrayList<Collocation>();
    JTextField jtfFilterValue;
    JProgressBar progressBar = new JProgressBar();
    String MASSAGE_WRONG_FORMAT = "Use format: [ENword][~][RUword]";
    String title = "Wisdom in words";
    JLabel labelTitle = new JLabel(title);
    JLabel labelNumberOfLearnedWords = new JLabel("");
    JLabel labelNumberOfDifficultWords = new JLabel("");
    JLabel labelNumberOfWordsLeft = new JLabel("");
    JLabel labelNumberOfWordsTotal = new JLabel("");
    JSpinner spinnerNumberOfBlocks, spinnerNumberOfCollocationsInABlock, spinnerFontSize, spinnerPortNumber;
    JButton btnStartStop, btnSwap;
    TimerLabel timerLabel;
    TimerLabel differenceTimeLabel;
    long timeOfLastMeasurement = 0;
    boolean swap = false;
    final int NUMBER_OF_ATTEMPTS_TO_CREATE_SERVER = 5;
    int countOfAttemptsToCreateServer = 0;
    ServerSocket serverSocket;
    int selectedRow = 0;
    int selectedRowForTimer = -1;//set value only if(!timerLabel.isTimerRunning)
    int indexOfThePreviousSelectedRow = -1, indexOfTheTempPreviousSelectedRow = -1;
    String storedTextOfFilter = "";
    boolean storedValueHotStartStop;
    boolean movingColumns = false;
    boolean ignoreTableChange = false;
    boolean playedNextPoint = false;


    private boolean EnglishTextLayout = false;
    char[] ArrayEnglishCharacters = {'h', 'j', 'k', 'l', 'y', 'u', 'i', 'o',
            'p', '[', ']', 'n', 'm',
            'g', 'f', 'd', 's', 'a', 'b', 'v', 'c', 'x', 'z', 't', 'r', 'e', 'w', 'q', '`'};

    char[] ArrayRussianCharacters = {'р', 'о', 'л', 'д', 'ж', 'э', 'н', 'г',
            'ш', 'щ', 'з', 'х', 'ъ', 'т', 'ь', 'б', 'ю',
            'п', 'а', 'в', 'ы', 'ф', 'и', 'м', 'с', 'ч', 'я', 'е', 'к', 'у', 'ц', 'й', 'ё'};



    public static void main(String[] args) {

        new wTeacher();

    }

    private wTeacher() {
        initPanel();
        initFrame();
    }

    private void initFrame() {

        frame = new JFrame(title);
        frame.setUndecorated(true); // Remove title bar

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        topPanel = new JPanel(new BorderLayout());
        JPanel a = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel b = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel c = new JPanel(new FlowLayout(FlowLayout.RIGHT));;

        final JButton btnHelp = new JButton("?");
        // Слушатель обработки события
        btnHelp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                frame.getContentPane().removeAll();
                frame.add(topPanel, BorderLayout.PAGE_START);
                frame.add(btnrBack, BorderLayout.PAGE_END);
                SwtBrowserCanvas.addBrowserToFrame(frame);

                String link = "https://www.youtube.com/watch?v=sFWIiEP4V9w";
                SwtBrowserCanvas.browserCanvasSetUrl(link);

                getContentPane().revalidate();
                getContentPane().repaint();

                frame.setSize(new Dimension(frame.getSize().width, frame.getSize().height + 1));

            }
        });
        final JButton btnMinimize = new JButton("-");
        btnMinimize.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.setState(JFrame.ICONIFIED);
            }
        });
        final JButton btnExit = new JButton("X");
        btnExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if (!swap) {
                    save();
                }
                System.exit(0);
            }
        });

        compCoords = null;
        topPanel.addMouseListener(new MouseListener() {
            public void mouseReleased(MouseEvent e) {
                compCoords = null;
            }

            public void mousePressed(MouseEvent e) {
                compCoords = e.getPoint();
            }

            public void mouseExited(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseClicked(MouseEvent e) {
            }
        });
        topPanel.addMouseMotionListener(new MouseMotionListener() {
            public void mouseMoved(MouseEvent e) {
            }

            public void mouseDragged(MouseEvent e) {
                Point currCoords = e.getLocationOnScreen();
                frame.setLocation(currCoords.x - compCoords.x, currCoords.y - compCoords.y);
            }
        });

        BufferedImage wPic = null;
        try {
            wPic = ImageIO.read(this.getClass().getResource("/ic_launcher.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        JLabel wIcon = new JLabel(new ImageIcon(wPic));

        a.add(wIcon);
        a.add(labelTitle);
        //a.add(wIcon);

        c.add(btnHelp);
        c.add(btnMinimize);
        c.add(btnExit);

        topPanel.add(a, "West");
        topPanel.add(b, "Center");
        topPanel.add(c, "East");

        frame.add(BorderLayout.NORTH, topPanel);
        frame.setLocationRelativeTo(null);

        frame.add(panel);

        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setLayout(new BorderLayout());
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {

                if (!swap) {
                    save();
                }

            }
        });
    }

    private void initPanel() {

        panel = new JPanel();
        panel.setPreferredSize(new Dimension(900, 550));

        init();
    }

    private void init() {

        table = new JTable() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 1;
            }

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int colIndex) {

                TableCellRenderer myJTextPaneRenderer = new EditorPaneRenderer();
                TableCellRenderer myTextAreaRenderer = new TextAreaRenderer();

                Component rComp = super.prepareRenderer(renderer, rowIndex, colIndex);
                boolean rendererMyJTextPane = rowIndex == table.getSelectedRow()
                        && colIndex == table.getSelectedColumn()
                        && tableChanged;

                if (rendererMyJTextPane) {
                    rComp = super.prepareRenderer(myJTextPaneRenderer, rowIndex, colIndex);
                } else if (colIndex == 1) {
                    rComp = super.prepareRenderer(myTextAreaRenderer, rowIndex, colIndex);
                } else if (colIndex == 3) {
                    rComp.setBackground(new Color(0f, 0f, 0f, 0f));
                }

                if (rowIndex + 1 > rowBeginIndexOfLearnedWords && rowIndex + 1 <= rowBeginIndexOfWellLearnedWords) {
                    rComp.setBackground(new Color(0xF0FFFF));
                } else if (rowIndex + 1 > rowBeginIndexOfWellLearnedWords && rowIndex < rowBeginIndexOfNativeWords) {
                    rComp.setBackground(new Color(0xD3D3D3));//0xB0C4DE));
                } else if (rowIndex + 1 > rowBeginIndexOfNativeWords) {
                    rComp.setBackground(new Color(0xA0FAA0));
                } else if ((rowIndex + 1) % numberOfCollocationsInABlock == 0 && !rendererMyJTextPane) {
                    rComp.setBackground(new Color(0xF0F8FF));
                } else if (colIndex != 3) {
                    rComp.setBackground(getBackground());
                }

                int index = (Integer) table.getValueAt(rowIndex, 4);
                Collocation collocation = listDictionary.get(index);
                if (listDictionary.size() > 0 && collocation.isDifficult) {
                    rComp.setBackground(new Color(0xFFA07A));
                }
                if ((colIndex == 0 || colIndex == 2) && collocation.learnedEn != collocation.learnedRu) {
                    rComp.setBackground(new Color(0xADFF2F));
                }
                if ((colIndex == 0 || colIndex == 2) && rowIndex == indexOfThePreviousSelectedRow) {
                    rComp.setBackground(new Color(0xFFFF00));
                }
                if ((colIndex == 0 || colIndex == 2) && rowIndex == table.getSelectedRow()) {
                    rComp.setBackground(new Color(0x7B68EE));
                }

                return rComp;
            }

        };

        dtm = new DefaultTableModel() {

            public Class<?> getColumnClass(int column) {

                switch (column) {
                    case 0:
                        return Boolean.class;
                    case 1:
                        return String.class;
                    case 2:
                        return Boolean.class;
                    case 3:
                        return String.class;
                    case 4:
                        return Integer.class;
                    default:
                        return String.class;
                }

            }

        };

        table.setModel(dtm);
        columnModel = table.getColumnModel();
        //table.putClientProperty("JTable.autoStartsEdit", true);
        table.setSurrendersFocusOnKeystroke(true);

        restoreListDictionary();

        Preferences prefs = Preferences.userNodeForPackage(wTeacher.class);

        progressBar.setValue((int) ((double) countOfLearnedWords / listDictionary.size() * 100));

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                tableChanged = false;
                rowChanged = true;

                if (!filterChanged && previousRow != -1) {
                    dtm.fireTableCellUpdated(previousRow, previousColumn);
                }
                previousRow = table.getSelectedRow();
            }
        });

        columnModel.addColumnModelListener(new TableColumnModelListener() {
            @Override
            public void columnAdded(TableColumnModelEvent e) {

            }

            @Override
            public void columnRemoved(TableColumnModelEvent e) {

            }

            @Override
            public void columnMoved(TableColumnModelEvent e) {

            }

            @Override
            public void columnMarginChanged(ChangeEvent e) {

            }

            @Override
            public void columnSelectionChanged(ListSelectionEvent e) {

                tableChanged = false;//
                columnChanged = true;
                if (!filterChanged && previousRow != -1) {
                    dtm.fireTableCellUpdated(previousRow, previousColumn);
                }
                previousColumn = table.getSelectedColumn();
            }
        });

        dtm.addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent e) {

                int indexOfTheFilteredSelectedRow = table.getSelectedRow();

                int indexConvertOfTheSelectedColumn = table.convertColumnIndexToModel(table.getSelectedColumn());
                if (indexOfTheFilteredSelectedRow == -1 || indexConvertOfTheSelectedColumn == -1
                        || indexOfTheFilteredSelectedRow >= dtm.getDataVector().size()
                        || listDictionary.size() == 0
                        || movingColumns
                        || previousRow == -1
                        || ignoreTableChange
                        ) {

                    return;
                }
                int indexOfTheSelectedRow = (Integer) table.getValueAt(indexOfTheFilteredSelectedRow, 4);
                int indexOfTheSelectedColumn = table.getSelectedColumn();

                Object v = dtm.getValueAt(indexOfTheSelectedRow, indexConvertOfTheSelectedColumn);
                Collocation collocation = listDictionary.get(indexOfTheSelectedRow);
                boolean isDifficultTemp = collocation.isDifficult;
                boolean learnedEnTemp = collocation.learnedEn;
                boolean learnedRuTemp = collocation.learnedRu;

                if ((v instanceof Boolean)) {

                    if (indexOfTheSelectedColumn == 0) {
                        if (englishLeft) {
                            collocation.learnedEn = (Boolean) v;
                        } else {
                            collocation.learnedRu = (Boolean) v;
                        }

                        if (!flagEnSwitch && !(Boolean) v) {
                            flagEnSwitch = true;

                            collocation.learnedEn = collocation.learnedRu = false;
                            table.setValueAt(false, indexOfTheFilteredSelectedRow, 2);

                            if((learnedEnTemp && learnedRuTemp)) {
                                labelNumberOfLearnedWords.setText("learned: " + --countOfLearnedWords);
                                labelNumberOfWordsLeft.setText("left: " + (listDictionary.size() - countOfLearnedWords));
                                progressBar.setValue((int) ((double) countOfLearnedWords / listDictionary.size() * 100));
                                learnedEnTemp = false;
                            }
                        }
                        if(learnedEnTemp && learnedRuTemp && !(Boolean) v){
                            labelNumberOfLearnedWords.setText("learned: " + --countOfLearnedWords);
                            labelNumberOfWordsLeft.setText("left: " + (listDictionary.size() - countOfLearnedWords));
                            progressBar.setValue((int) ((double) countOfLearnedWords / listDictionary.size() * 100));
                        }
                    } else if (indexOfTheSelectedColumn == 2) {
                        if (englishLeft) {
                            collocation.learnedRu = (Boolean) v;
                        } else {
                            collocation.learnedEn = (Boolean) v;
                        }

                        if (!flagRuSwitch && (Boolean) v) {
                            flagRuSwitch = true;

                            collocation.learnedEn = collocation.learnedRu = true;
                            table.setValueAt(true, indexOfTheFilteredSelectedRow, 0);

                            if(!(learnedEnTemp && learnedRuTemp)) {
                                labelNumberOfLearnedWords.setText("learned: " + ++countOfLearnedWords);
                                labelNumberOfWordsLeft.setText("left: " + (listDictionary.size() - countOfLearnedWords));
                                progressBar.setValue((int) ((double) countOfLearnedWords / listDictionary.size() * 100));
                                if (countOfLearnedWords % 1000 == 0 && !playedNextPoint) {
                                //if (countOfLearnedWords % 3 == 0) {
                                    playNextPoint();
                                }
                                learnedEnTemp = false;
                            }
                        }
                        if(learnedEnTemp && learnedRuTemp && !(Boolean) v){
                            labelNumberOfLearnedWords.setText("learned: " + --countOfLearnedWords);
                            labelNumberOfWordsLeft.setText("left: " + (listDictionary.size() - countOfLearnedWords));
                            progressBar.setValue((int) ((double) countOfLearnedWords / listDictionary.size() * 100));
                        }
                    }

                    if (!stringSwitch) {
                        stringSwitch = true;

                        if (collocation.learnedEn && collocation.learnedRu) {

                            if (englishLeft) {
                                table.setValueAt(collocation.ru, indexOfTheFilteredSelectedRow, 3);
                            } else {
                                table.setValueAt(collocation.en, indexOfTheFilteredSelectedRow, 3);
                            }

                        } else if (answersAreHidden) {
                            table.setValueAt("", indexOfTheFilteredSelectedRow, 3);
                        }
                        flagEnSwitch = false;
                        flagRuSwitch = false;
                        stringSwitch = false;

                        int i = 0;
                        for (Collocation colloc : listDictionary) {
                            if (colloc.learnedEn != colloc.learnedRu) {
                                i++;
                            }
                        }
                        progressBar.setValue((int) (((double) i / numberOfCollocationsInABlock) * 100));

                        if (!timerLabel.isTimerRunning()) {
                            selectedRowForTimer = indexOfTheSelectedRow;
                        }

                    }


                } else if ((v instanceof String)) {

                    String content = v.toString();

                    if ((rowChanged && !filterChanged) || (columnChanged && !filterChanged)
                            || hideAnswers || showAnswers
                            || content.isEmpty() || content.contains("✓") || content.contains("⚓") ) {

                        rowChanged = columnChanged = false;
                        return;
                    }

                    //get the index editable word
                    if (englishLeft) {
                        original = collocation.ru;
                    } else {
                        original = collocation.en
                                .replace("✓", "")
                                .replace("⚓", "");
                    }

                    answer = content;
                    String resultText;
                    if (answer.equals(original)) {
                        resultText = original + "✓";
                        if (answersAreHidden) {
                            ignoreTableChange = true;
                            table.setValueAt("", indexOfTheFilteredSelectedRow, 3);

                            if (indexOfTheSelectedRow == selectedRowForTimer) {
                                startStopTimer(true, true);
                            }
                        } else {
                            ignoreTableChange = true;
                            table.setValueAt(resultText, indexOfTheFilteredSelectedRow, 3);

                            if (indexOfTheSelectedRow == selectedRowForTimer) {
                                startStopTimer(true, true);
                            }
                        }

                        if (!englishLeft) {
                            collocation.isDifficult = false;
                            if(isDifficultTemp != collocation.isDifficult){
                                labelNumberOfDifficultWords.setText("difficult: " + --countOfDifficultWords);
                            }
                        }
                    } else {
                        if (englishLeft) {
                            resultText = original;
                        } else {
                            resultText = original + "⚓";
                            collocation.isDifficult = true;
                            if(isDifficultTemp != collocation.isDifficult){
                                labelNumberOfDifficultWords.setText("difficult: " + ++countOfDifficultWords);
                            }

                            startStopTimer(false, true);
                        }

                        if (answersAreHidden) {
                            ignoreTableChange = true;
                            table.setValueAt("", indexOfTheFilteredSelectedRow, 3);
                        } else {
                            ignoreTableChange = true;
                            table.setValueAt(resultText, indexOfTheFilteredSelectedRow, 3);
                        }

                    }

                    if (indexConvertOfTheSelectedColumn == 1) {
                        collocation.en = resultText;
                    } else if (indexConvertOfTheSelectedColumn == 3) {
                        collocation.ru = resultText;
                    }

                    if(indexOfTheSelectedRow != indexOfTheTempPreviousSelectedRow) {
                        indexOfThePreviousSelectedRow = indexOfTheTempPreviousSelectedRow;
                    }
                    indexOfTheTempPreviousSelectedRow = indexOfTheSelectedRow;

                    ignoreTableChange = false;
                    tableChanged = true;
                }
            }

        });


        // Кнопка перемещения колонки
        JButton btnChange = new JButton("Change");
        // Слушатель обработки события
        btnChange.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                boolean answersWereHidden = answersAreHidden;

                showAnswers();

                changeColumns(false);

                if (answersWereHidden) {
                    hideAnswers();
                }


            }
        });


        // Кнопка Скрыть ответы
        final JButton btnHide = new JButton("Hide answers");
        // Слушатель обработки события
        btnHide.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                hideAnswers();

            }
        });


        // Кнопка Показать ответы
        JButton btnShow = new JButton("Show answers");
        // Слушатель обработки события
        btnShow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                showAnswers();

            }
        });

        // Кнопка Учить навые слова
        JButton btnLearn = new JButton("Learn new words");
        // Слушатель обработки события
        btnLearn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (swap) {

                    scrollToRow(0);

                    swap = false;
                    btnSwap.setBackground(btnHide.getBackground());
                } else {

                    int i = 0;
                    int j = 0;
                    for (Collocation colloc : listDictionary) {
                        if (colloc.learnedEn != colloc.learnedRu) {
                            j = i + 1;
                        }
                        i++;
                    }

                    scrollToRow(j);

                }
            }
        });

        // Кнопка Повторять выученные слова
        JButton btnrRepeat = new JButton("Repeat the learned words");
        // Слушатель обработки события
        btnrRepeat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (swap) {
                    int j = listDictionary.size() - 1;
                    scrollToRow(j);

                    swap = false;
                    btnSwap.setBackground(btnHide.getBackground());
                } else {
                    int j = 0;
                    for (Collocation i : listDictionary) {
                        if (i.learnedEn && i.learnedRu) {
                            break;
                        }
                        j++;
                    }
                    if (j == listDictionary.size()) j = 0;

                    scrollToRow(j);
                }
            }
        });

        //Create the scroll pane and add the table to it.
        scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(900, 450));

        // Кнопка Вернуться к изучению
        btnrBack = new JButton("Back to learn");
        // Слушатель обработки события
        btnrBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                frame.getContentPane().removeAll();
                frame.add(topPanel, BorderLayout.PAGE_START);
                frame.getContentPane().add(panel);
                frame.getContentPane().revalidate();
                frame.getContentPane().repaint();
                setSize(new Dimension(frame.getSize().width, frame.getSize().height - 1));

            }
        });

        // Кнопка Сортировать
        JButton btnSort = new JButton("Sort");
        // Слушатель обработки события
        btnSort.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if(filterChanged){
                    return;
                }

                Comparator enRuComparator = new Comparator<Collocation>() {
                    @Override
                    public int compare(Collocation o1, Collocation o2) {
                        if (englishLeft) {
                            return o1.en.compareTo(o2.en);
                        } else {
                            return o1.ru.compareTo(o2.ru);
                        }
                    }
                };

                List<Collocation> listOfStudiedWords = new ArrayList<Collocation>();
                List<Collocation> listOfFavoriteWords = new ArrayList<Collocation>();
                List<Collocation> listOfDifficultWords = new ArrayList<Collocation>();
                List<Collocation> listOfLearnedWords = new ArrayList<Collocation>();

                for (int i = 0; i < listDictionary.size(); i++) {

                    Collocation collocation = listDictionary.get(i);

                    if (collocation.learnedEn != collocation.learnedRu) {
                        if(collocation.isDifficult && swap){
                            listOfFavoriteWords.add(collocation);
                        }else {
                            listOfStudiedWords.add(collocation);
                        }
                        listDictionary.remove(i);
                        i--;
                        continue;
                    }
                    if (collocation.isDifficult) {
                        listOfDifficultWords.add(collocation);
                        listDictionary.remove(i);
                        i--;
                        continue;
                    }
                    if (collocation.learnedEn && collocation.learnedRu) {
                        listOfLearnedWords.add(collocation);
                        listDictionary.remove(i);
                        i--;
                        continue;
                    }
                }

                Collections.sort(listOfStudiedWords, enRuComparator);
                Collections.sort(listOfFavoriteWords, enRuComparator);
                Collections.sort(listOfDifficultWords, enRuComparator);
                Collections.sort(listDictionary, enRuComparator);

                int j = 0;
                for (Collocation collocation : listOfFavoriteWords) {
                    listDictionary.add(j, collocation);
                    j++;
                    rowBeginIndexOfLearnedWords = j;
                }
                for (Collocation collocation : listOfStudiedWords) {
                    listDictionary.add(j, collocation);
                    j++;
                    rowBeginIndexOfLearnedWords = j;
                }
                for (Collocation collocation : listDictionary) {
                    if (collocation.learnedEn == collocation.learnedRu) {
                        j++;
                    }
                    rowBeginIndexOfLearnedWords = j;
                }
                countOfDifficultWords = 0;
                for (Collocation collocation : listOfDifficultWords) {
                    countOfDifficultWords++;
                    listDictionary.add(collocation);
                    j++;
                    rowBeginIndexOfLearnedWords = j;
                }
                int countOfLearnedWords = 0;
                List<Collocation> listOfWellLearnedWords = new ArrayList<Collocation>();
                for (Collocation collocation : listOfLearnedWords) {
                    countOfLearnedWords++;

                    if (j >= rowBeginIndexOfLearnedWords + numberOfBlocks * numberOfCollocationsInABlock) {
                        listOfWellLearnedWords.add(collocation);
                    } else {
                        listDictionary.add(collocation);
                        j++;
                        rowBeginIndexOfWellLearnedWords = j;
                    }
                }
                rowBeginIndexOfNativeWords = rowBeginIndexOfWellLearnedWords + numberOfBlocks * numberOfCollocationsInABlock;
                progressBar.setValue((int) ((double) countOfLearnedWords / listDictionary.size() * 100));
                for (Collocation collocation : listOfWellLearnedWords) {
                    listDictionary.add(collocation);
                    j++;
                }

                labelNumberOfLearnedWords.setText("learned: " + Integer.toString(countOfLearnedWords));
                labelNumberOfDifficultWords.setText("difficult: " + Integer.toString(countOfDifficultWords));
                labelNumberOfWordsLeft.setText("left: " + Integer.toString(listDictionary.size() - countOfLearnedWords));
                labelNumberOfWordsTotal.setText("total: " + Integer.toString(listDictionary.size()));

                updateTable();

                swap = false;
                btnSwap.setBackground(btnHide.getBackground());

            }

        });

        // Кнопка Перемешать
        JButton btnShuffle = new JButton("Shuffle");
        // Слушатель обработки события
        btnShuffle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if(filterChanged){
                    return;
                }

                List<Collocation> listOfStudiedWords = new ArrayList<Collocation>();
                List<Collocation> listOfFavoriteWords = new ArrayList<Collocation>();
                List<Collocation> listOfDifficultWords = new ArrayList<Collocation>();
                List<Collocation> listOfLearnedWords = new ArrayList<Collocation>();

                for (int i = 0; i < listDictionary.size(); i++) {

                    Collocation collocation = listDictionary.get(i);

                    if (collocation.learnedEn != collocation.learnedRu) {
                        if(collocation.isDifficult && swap){
                            listOfFavoriteWords.add(collocation);
                        }else {
                            listOfStudiedWords.add(collocation);
                        }
                        listDictionary.remove(i);
                        i--;
                        continue;
                    }
                    if (collocation.isDifficult) {

                        listOfDifficultWords.add(collocation);
                        listDictionary.remove(i);
                        i--;
                        continue;
                    }
                    if (collocation.learnedEn && collocation.learnedRu) {
                        listOfLearnedWords.add(collocation);
                        listDictionary.remove(i);
                        i--;
                        continue;
                    }
                }

                Collections.shuffle(listOfStudiedWords);
                Collections.shuffle(listOfFavoriteWords);
                Collections.shuffle(listDictionary);

                int j = 0;
                for (Collocation collocation : listOfFavoriteWords) {
                    listDictionary.add(j, collocation);
                    j++;
                    rowBeginIndexOfLearnedWords = j;
                }

                for (Collocation collocation : listOfStudiedWords) {
                    listDictionary.add(j, collocation);
                    j++;
                    rowBeginIndexOfLearnedWords = j;
                }

                for (Collocation collocation : listDictionary) {
                    if (collocation.learnedEn == collocation.learnedRu) {
                        j++;
                    }
                    rowBeginIndexOfLearnedWords = j;
                }
                countOfDifficultWords = 0;
                for (Collocation collocation : listOfDifficultWords) {
                    countOfDifficultWords++;
                    listDictionary.add(collocation);
                    j++;
                    rowBeginIndexOfLearnedWords = j;
                }
                int countOfLearnedWords = 0;
                List<Collocation> listOfWellLearnedWords = new ArrayList<Collocation>();
                for (Collocation collocation : listOfLearnedWords) {
                    countOfLearnedWords++;

                    if (j >= rowBeginIndexOfLearnedWords + numberOfBlocks * numberOfCollocationsInABlock) {
                        listOfWellLearnedWords.add(collocation);
                    } else {
                        listDictionary.add(collocation);
                        j++;
                        rowBeginIndexOfWellLearnedWords = j;
                    }
                }
                rowBeginIndexOfNativeWords = rowBeginIndexOfWellLearnedWords + numberOfBlocks * numberOfCollocationsInABlock;
                progressBar.setValue((int) ((double) countOfLearnedWords / listDictionary.size() * 100));
                Collections.shuffle(listOfWellLearnedWords);
                for (Collocation collocation : listOfWellLearnedWords) {
                    listDictionary.add(collocation);
                    j++;
                }

                labelNumberOfLearnedWords.setText("learned: " + Integer.toString(countOfLearnedWords));
                labelNumberOfDifficultWords.setText("difficult: " + Integer.toString(countOfDifficultWords));
                labelNumberOfWordsLeft.setText("left: " + Integer.toString(listDictionary.size() - countOfLearnedWords));
                labelNumberOfWordsTotal.setText("total: " + Integer.toString(listDictionary.size()));

                updateTable();

                swap = false;
                btnSwap.setBackground(btnHide.getBackground());

            }

        });


        table.addMouseListener(new MouseAdapter() {
            private java.util.Timer t;

            public void mousePressed(MouseEvent e) {
                if (t == null) {
                    t = new java.util.Timer();
                }
                t.schedule(new TimerTask() {
                    public void run() {
                        int indexOfTheSelectedColumn = table.convertColumnIndexToModel(table.getSelectedColumn());

                        Object v = dtm.getValueAt(table.getSelectedRow(), indexOfTheSelectedColumn);
                        if (!(v instanceof String)) {
                            return;
                        }

                        if(storedTextOfFilter.isEmpty()){
                            selectedRow = table.getSelectedRow();
                        }

                        String content = v.toString();
                        if(content.isEmpty()){
                            return;
                        }
                        content = content.replace("✓", "").replace("⚓", "");

                        frame.getContentPane().removeAll();
                        frame.add(topPanel, BorderLayout.PAGE_START);
                        frame.add(btnrBack, BorderLayout.PAGE_END);
                        SwtBrowserCanvas.addBrowserToFrame(frame);

                        frame.getContentPane().revalidate();
                        frame.getContentPane().repaint();

                        frame.setSize(new Dimension(frame.getSize().width, frame.getSize().height + 1));


                        //translate the word
                        Character Symbol = content.charAt(0);
                        boolean EnglishLayout = false;//engList.indexOf(Symbol) != -1;
                        boolean RussianLayout = false;//rusList.indexOf(Symbol) != -1;

                        for (int i = 0; i < ArrayEnglishCharacters.length; i++) {
                            if (ArrayEnglishCharacters[i] == Symbol) {
                                EnglishLayout = true;
                            }
                        }

                        for (int i = 0; i < ArrayRussianCharacters.length; i++) {
                            if (ArrayRussianCharacters[i] == Symbol) {
                                RussianLayout = true;
                            }
                        }

                        if (EnglishLayout != RussianLayout) {
                            EnglishTextLayout = EnglishLayout;
                        }

                        String link = "https://translate.google.com/?hl=ru#en/ru/" + content;
                        if (!EnglishTextLayout) {
                            link = "https://translate.google.com/?hl=ru#ru/en/" + content;
                        }

                        jtfFilterValue.setText(content);
                        SwtBrowserCanvas.browserCanvasSetUrl(link);
                    }
                }, 1000);
            }

            public void mouseReleased(MouseEvent e) {
                if (t != null) {
                    t.cancel();
                    t = null;
                }
            }

        });

        JLabel labelFontSize = new JLabel("Font");
        fontSize = prefs.getInt("fontSize", fontSize);
        spinnerFontSize = new JSpinner(new SpinnerNumberModel(fontSize, 4, 100, 1));
        spinnerFontSize.setToolTipText("Font size");
        table.setFont(new Font(table.getFont().getName(), table.getFont().getStyle(), fontSize));

        JTextField textField = new JTextField();
        textField.setFont(new Font(table.getFont().getName(), table.getFont().getStyle(), fontSize));
        textField.setBorder(new LineBorder(Color.BLACK));
        DefaultCellEditor dce = new DefaultCellEditor(textField);
        table.getColumnModel().getColumn(1).setCellEditor(dce);
        table.getColumnModel().getColumn(3).setCellEditor(dce);

        spinnerFontSize.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {

                int value = (Integer) spinnerFontSize.getValue();
                if (value != fontSize) {
                    fontSize = value;
                    table.setFont(new Font(table.getFont().getName(), table.getFont().getStyle(), fontSize));

                    JTextField textField = new JTextField();
                    textField.setFont(new Font(table.getFont().getName(), table.getFont().getStyle(), fontSize));
                    textField.setBorder(new LineBorder(Color.BLACK));
                    DefaultCellEditor dce = new DefaultCellEditor(textField);
                    table.getColumnModel().getColumn(1).setCellEditor(dce);
                    table.getColumnModel().getColumn(3).setCellEditor(dce);
                }

            }
        });


        int sizeListDictionary = listDictionary.size() == 0 ? 1 : listDictionary.size();
        JLabel labelNumberOfBlocks = new JLabel("Blocks");
        spinnerNumberOfBlocks = new JSpinner(new SpinnerNumberModel(1, 1, sizeListDictionary, 1));
        spinnerNumberOfBlocks.setToolTipText("Number of blocks");
        spinnerNumberOfBlocks.setValue(numberOfBlocks);


        spinnerNumberOfBlocks.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {

                int value = (Integer) spinnerNumberOfBlocks.getValue();
                if (value != numberOfBlocks) {
                    numberOfBlocks = value;
                    defineIndexesOfWords();
                }

            }
        });

        JLabel labelNumberOfCollocationsInABlock = new JLabel("Collocations in a block");
        spinnerNumberOfCollocationsInABlock = new JSpinner(new SpinnerNumberModel(1, 1, sizeListDictionary, 1));
        spinnerNumberOfCollocationsInABlock.setToolTipText("Number of collocations in a block");
        spinnerNumberOfCollocationsInABlock.setValue(numberOfCollocationsInABlock);


        spinnerNumberOfCollocationsInABlock.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {

                int value = (Integer) spinnerNumberOfCollocationsInABlock.getValue();
                if (value != numberOfCollocationsInABlock) {
                    numberOfCollocationsInABlock = value;
                    defineIndexesOfWords();
                }

            }
        });

        portNumber = prefs.getInt("portNumber", portNumber);
        JLabel labelPortNumber = new JLabel("Port");
        spinnerPortNumber = new JSpinner(new SpinnerNumberModel(portNumber, 1, 65535, 1));
        spinnerPortNumber.setToolTipText("Server port number");

        spinnerPortNumber.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {

                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                        labelTitle.setText(title);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                int value = (Integer) spinnerPortNumber.getValue();
                if (value != portNumber) {
                    portNumber = value;
                    startServer(portNumber);
                }

            }
        });


        jtfFilterValue = new JTextField("", 25);

        jtfFilterValue.addKeyListener(new KeyAdapter() {
            /**
             * Invoked when a key has been released.
             *
             * @param e
             */

            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);

                filterChanged = true;
                if(storedTextOfFilter.isEmpty()){
                    selectedRow = table.getSelectedRow();
                }

                String text = jtfFilterValue.getText();
                setRowFilter(text);

                if (text.isEmpty()){
                    scrollToRow(selectedRow);
                    if (answersAreHidden) {
                        hideAnswers();
                    }
                }
                storedTextOfFilter = text;
            }
        });

        // Кнопка Добавить навое словосочетание
        JButton btnAdd = new JButton("add");
        // Слушатель обработки события
        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String strCollocation = jtfFilterValue.getText().toString();
                if (strCollocation.contains("~")) {
                    String[] collocationParts = strCollocation.split("~");
                    if (collocationParts.length != 2) {
                        showMessageDialog(MASSAGE_WRONG_FORMAT);
                        return;
                    }
                    Collocation collocation = new Collocation(false, collocationParts[0].trim(), false, collocationParts[1].trim(), false, 0);
                    Character Symbol = collocation.en.charAt(0);
                    boolean EnglishLayout = false;//engList.indexOf(Symbol) != -1;
                    boolean RussianLayout = false;//rusList.indexOf(Symbol) != -1;

                    for (int i = 0; i < ArrayEnglishCharacters.length; i++) {
                        if (ArrayEnglishCharacters[i] == Symbol) {
                            EnglishLayout = true;
                        }
                    }

                    for (int i = 0; i < ArrayRussianCharacters.length; i++) {
                        if (ArrayRussianCharacters[i] == Symbol) {
                            RussianLayout = true;
                        }
                    }

                    if (EnglishLayout != RussianLayout) {
                        EnglishTextLayout = EnglishLayout;
                    }

                    if (!EnglishTextLayout) {
                        showMessageDialog(MASSAGE_WRONG_FORMAT);
                        return;
                    }

                    listDictionary.add(0, collocation);

                    jtfFilterValue.setText("");
                    setRowFilter("");

                    updateTable();

                    labelNumberOfWordsLeft.setText("left: " + Integer.toString(listDictionary.size() - countOfLearnedWords));
                    labelNumberOfWordsTotal.setText("total: " + Integer.toString(listDictionary.size()));

                } else {
                    showMessageDialog(MASSAGE_WRONG_FORMAT);
                }


            }
        });

        // Кнопка Удалить словосочетание
        JButton btnDelete = new JButton("delete");
        // Слушатель обработки события
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if(swap){
                    int result = JOptionPane.showConfirmDialog(wTeacher.this,
                            "Clear progress?",
                            "Reset progress",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);

                    if (result == JOptionPane.YES_OPTION) {

                        for (int i = 0; i < listDictionary.size(); i++) {
                            Collocation collocation = listDictionary.get(i);

                            collocation.learnedEn = false;
                            collocation.learnedRu = false;
                            collocation.isDifficult = false;
                            collocation.en = collocation.en
                                    .replace("✓", "")
                                    .replace("⚓", "");
                            collocation.index = i;
                        }

                        updateTable();

                        progressBar.setValue((0));
                        labelNumberOfLearnedWords.setText("learned: 0");
                        labelNumberOfDifficultWords.setText("difficult: 0");
                        labelNumberOfWordsLeft.setText("left: " + Integer.toString(listDictionary.size()));
                        labelNumberOfWordsTotal.setText("total: " + Integer.toString(listDictionary.size()));
                    }

                    swap = false;
                    btnSwap.setBackground(btnHide.getBackground());

                    return;
                }

                int indexOfTheFilteredSelectedRow = table.getSelectedRow();
                int indexOfTheSelectedRow = (Integer) table.getValueAt(indexOfTheFilteredSelectedRow, 4);
                if (indexOfTheFilteredSelectedRow != -1) {
                    Collocation collocation = listDictionary.get(indexOfTheSelectedRow);
                    String strCollocation = collocation.en + "~" + collocation.ru;
                    int result = JOptionPane.showConfirmDialog(wTeacher.this,
                            strCollocation,
                            "Remove the words",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);

                    if (result == JOptionPane.YES_OPTION) {
                        previousRow = -1;
                        listDictionary.remove(indexOfTheSelectedRow);
                        dtm.removeRow(indexOfTheSelectedRow);

                        updateTable();

                        labelNumberOfLearnedWords.setText("learned: " + Integer.toString(countOfLearnedWords));
                        labelNumberOfWordsLeft.setText("left: " + Integer.toString(listDictionary.size() - countOfLearnedWords));
                        labelNumberOfWordsTotal.setText("total: " + Integer.toString(listDictionary.size()));

                    }
                }

            }

        });


        // Кнопка Вернуть словарь к эталону
        JButton btnReset = new JButton("Reset");
        // Слушатель обработки события
        btnReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int result = JOptionPane.showConfirmDialog(wTeacher.this,
                        "Back to the reference dictionary?",
                        "Reset progress",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (result == JOptionPane.YES_OPTION) {
                    resetListDictionary();
                }

            }
        });

        timerLabel = new TimerLabel();
        differenceTimeLabel = new TimerLabel();
        differenceTimeLabel.setTimeColor(new Color(0, 128, 0));

        btnStartStop = new JButton("Start");
        // Слушатель обработки события
        btnStartStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                selectedRowForTimer = -1;
                startStopTimer(true, false);

            }
        });

        selectedRow = prefs.getInt("selectedRow", 0);
        scrollToRow(selectedRow);


        btnSwap = new JButton("Swap");
        // Слушатель обработки события
        btnSwap.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                swap = !swap;
                if (swap) {
                    btnSwap.setBackground(new Color(0xFA8072));
                } else {
                    btnSwap.setBackground(btnHide.getBackground());
                }

            }
        });

        table.addKeyListener(new KeyAdapter() {

            /**
             * Invoked when a key has been pressed.
             *
             * @param e
             */
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);

                if (e.getKeyCode() == KeyEvent.VK_UP ||
                        e.getKeyCode() == KeyEvent.VK_DOWN) {

                    keyPreviousRow = table.getSelectedRow();
                }


            }

            /**
             * Invoked when a key has been released.
             *
             * @param e
             */
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);

                if (e.getKeyCode() == KeyEvent.VK_UP) {

                    if ( table.getSelectedRow() == 0 && keyPreviousRow == 0) {
                        int i = 0;
                        int j = listDictionary.size() - 1;
                        for (Collocation colloc : listDictionary) {
                            if (colloc.learnedEn != colloc.learnedRu) {
                                j = i;
                            }
                            i++;
                        }

                        scrollToRow(j);


                        if (!timerLabel.isTimerRunning()) {
                            selectedRowForTimer = j;
                        }

                    }

                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {

                    if (table.getSelectedRow() == listDictionary.size() - 1) {
                        scrollToRow(0);
                    }
                }



            }

    });


        JButton btnSave = new JButton("Save");
        // Слушатель обработки события
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                save();

            }
        });

        JButton btnClearFilter = new JButton("X");
        // Слушатель обработки события
        btnClearFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                jtfFilterValue.setText("");
                setRowFilter("");
                storedTextOfFilter = "";
                filterChanged = false;

                if (answersAreHidden) {
                    hideAnswers();
                }
                scrollToRow(selectedRow);

            }
        });

        JButton btnTest = new JButton("Test");
        // Слушатель обработки события
        btnTest.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                progressBar.setValue(95);

            }
        });

        //table.getCellEditor().setFont;

        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setStringPainted(true);
        //progressBar.setIndeterminate(true);

        labelNumberOfLearnedWords.setForeground(new Color(0, 128, 0));
        labelNumberOfDifficultWords.setForeground(new Color(255, 140, 0));
        labelNumberOfWordsLeft.setForeground(Color.RED);
        labelNumberOfWordsTotal.setForeground(Color.BLACK);

        panel.add(btnReset);
        panel.add(btnHide);
        panel.add(btnShow);
        panel.add(btnLearn);
        panel.add(btnrRepeat);
        panel.add(btnSort);
        panel.add(btnShuffle);
        panel.add(btnChange);

        //panel.add(btnTest);

        panel.add(btnSave);
        panel.add(labelPortNumber);
        panel.add(spinnerPortNumber);
        panel.add(progressBar);
        panel.add(labelNumberOfLearnedWords);
        panel.add(labelNumberOfDifficultWords);
        panel.add(labelNumberOfWordsLeft);
        panel.add(labelNumberOfWordsTotal);

        panel.add(btnStartStop);
        panel.add(timerLabel);
        panel.add(differenceTimeLabel);
        panel.add(btnSwap);

        panel.add(scrollPane);

        panel.add(jtfFilterValue);
        panel.add(btnClearFilter);
        panel.add(btnAdd);
        panel.add(btnDelete);
        panel.add(labelFontSize);
        panel.add(spinnerFontSize);
        panel.add(labelNumberOfBlocks);
        panel.add(spinnerNumberOfBlocks);
        panel.add(labelNumberOfCollocationsInABlock);
        panel.add(spinnerNumberOfCollocationsInABlock);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                startServer(portNumber);
            }
        });



    }

    private void startStopTimer(boolean showDifference, boolean hotStartStop){

        boolean timerStarted = timerLabel.isTimerRunning();

        if (timerStarted && hotStartStop && storedValueHotStartStop != hotStartStop){
            return;
        }

        //чтобы при ошибке ввода не запускался таймер
        if (!showDifference && hotStartStop && !timerStarted){
            return;
        }

        if (storedValueHotStartStop != hotStartStop){
            showDifference = false;
        }

        if (timerStarted) {

            timerLabel.stopTimer();
            btnStartStop.setText("Start");

            if (showDifference) {
                long differenceTime = timerLabel.getTime() - timeOfLastMeasurement;
                if (differenceTime < 0) {
                    differenceTimeLabel.setTimeColor(new Color(0, 128, 0));
                    differenceTime *= -1;
                } else {
                    differenceTimeLabel.setTimeColor(Color.RED);
                }

                timeOfLastMeasurement = timerLabel.getTime();
                differenceTimeLabel.setStringTime(StringUtils.timeToString(differenceTime));
            } else {
                differenceTimeLabel.setTimeColor(Color.RED);
                differenceTimeLabel.setStringTime(StringUtils.timeToString(timerLabel.getTime()));
            }

            int i = 0;
            int j = -1;
            for (Collocation colloc : listDictionary) {
                if (colloc.learnedEn != colloc.learnedRu) {
                    j = i;
                }
                i++;
            }

            if (!timerLabel.isTimerRunning()) {
                selectedRowForTimer = j;
            }
        } else {
            timerLabel.startTimer();
            btnStartStop.setText("Stop");
        }

        storedValueHotStartStop = hotStartStop;

    }


    private void hideAnswers() {

        hideAnswers = true;
        tableChanged = false;

        for (int i = 0; i < table.getRowCount(); i++) {
            table.setValueAt("", i, 3);
        }

        hideAnswers = false;
        answersAreHidden = true;
    }

    private void showAnswers() {

        showAnswers = true;
        tableChanged = false;
        answersAreHidden = false;

        if (englishLeft) {
            for (int row = 0; row < table.getRowCount(); row++) {
                int index = (Integer) table.getValueAt(row, 4);
                Collocation collocation = listDictionary.get(index);
                table.setValueAt(collocation.ru, row, 3);
            }
        } else {
            for (int row = 0; row < table.getRowCount(); row++) {
                int index = (Integer) table.getValueAt(row, 4);
                Collocation collocation = listDictionary.get(index);
                table.setValueAt(collocation.en, row, 3);
            }
        }
        showAnswers = false;
    }

    private void setColumnWidth() {

        columnModel.getColumn(0).setMaxWidth(25);
        columnModel.getColumn(1).setMaxWidth(425);
        columnModel.getColumn(2).setMaxWidth(25);
        columnModel.getColumn(3).setMaxWidth(425);

        columnModel.getColumn(4).setMinWidth(0);
        columnModel.getColumn(4).setMaxWidth(0);
        columnModel.getColumn(4).setPreferredWidth(0);

    }

    private void setRowFilter(String text) {

        if(text.isEmpty()){
            filterChanged = false;
        }

        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(dtm);

        RowFilter<TableModel, Object> firstFiler = null;
        RowFilter<TableModel, Object> secondFilter = null;
        List<RowFilter<TableModel, Object>> filters = new ArrayList<RowFilter<TableModel, Object>>();
        RowFilter<TableModel, Object> compoundRowFilter = null;
        try {
            firstFiler = RowFilter.regexFilter(text, 1);
            secondFilter = RowFilter.regexFilter(text, 3);
            filters.add(firstFiler);
            filters.add(secondFilter);
            compoundRowFilter = RowFilter.orFilter(filters);
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }
        sorter.setRowFilter(compoundRowFilter);
        table.setRowSorter(sorter);
    }

    private void showMessageDialog(String strMessage) {

        JOptionPane.showMessageDialog(wTeacher.this, strMessage);

    }

    private void saveWordsForKeyboardSimulator() {

        //final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
        //String currentRootDirectoryPath = getClass().getResource("").getPath();;
        String currentRootDirectoryPath = wTeacher.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPath = null;
        try {
            decodedPath = URLDecoder.decode(currentRootDirectoryPath, "UTF-8");
            File currentJavaJarFile = new File(currentRootDirectoryPath);
            String fileName = URLDecoder.decode(currentJavaJarFile.getName(), "UTF-8");
            decodedPath = decodedPath.replace(fileName, "");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //En
        File fileLearnedWordsEn = new File(decodedPath, "learnedWordsEn.txt");
        if (!fileLearnedWordsEn.exists()) {
            try {
                fileLearnedWordsEn.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            // открываем поток для записи
            Writer bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileLearnedWordsEn), "UTF-8"));//"UTF-8"));

            for (int i = 0; i < listDictionary.size(); i++) {
                Collocation collocation = listDictionary.get(i);
                if (collocation.learnedEn && collocation.learnedRu) {
                    // пишем данные
                    bw.write(collocation.en
                            .replace("✓", "")
                            .replace("⚓", "") + "\r\n");
                }
            }
            // закрываем поток
            bw.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        //Ru
        File fileLearnedWordsRu = new File(decodedPath, "learnedWordsRu.txt");
        if (!fileLearnedWordsRu.exists()) {
            try {
                fileLearnedWordsRu.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            // открываем поток для записи
            Writer bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileLearnedWordsRu), "UTF-8"));//"UTF-8"));

            for (int i = 0; i < listDictionary.size(); i++) {
                Collocation collocation = listDictionary.get(i);
                if (collocation.learnedEn && collocation.learnedRu) {
                    // пишем данные
                    bw.write(collocation.ru + "\r\n");
                }
            }
            // закрываем поток
            bw.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


    }
    private void saveListDictionary() {

        String jsonStr = new Gson().toJson(listDictionary);

        //final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
        //String currentRootDirectoryPath = getClass().getResource("").getPath();;
        String currentRootDirectoryPath = wTeacher.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPath = null;
        try {
            decodedPath = URLDecoder.decode(currentRootDirectoryPath, "UTF-8");
            File currentJavaJarFile = new File(currentRootDirectoryPath);
            String fileName = URLDecoder.decode(currentJavaJarFile.getName(), "UTF-8");
            decodedPath = decodedPath.replace(fileName, "");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        File file = new File(decodedPath, "savedListDictionary");

        try {
            // открываем поток для записи
            Writer bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));//"UTF-8"));
            // пишем данные
            bw.write(Integer.toString(numberOfBlocks) + ";" + Integer.toString(numberOfCollocationsInABlock) + "\r\n");
            bw.write(jsonStr);
            // закрываем поток
            bw.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    private void restoreListDictionary() {

        Path filePath;

        //final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
        //File file = new File(sysTempDir, "savedListDictionary");

        //String currentRootDirectoryPath = getClass().getResource("").getPath();
        String currentRootDirectoryPath = wTeacher.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPath = null;
        try {
            decodedPath = URLDecoder.decode(currentRootDirectoryPath, "UTF-8");
            File currentJavaJarFile = new File(currentRootDirectoryPath);
            String fileName = URLDecoder.decode(currentJavaJarFile.getName(), "UTF-8");
            decodedPath = decodedPath.replace(fileName, "");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        File file = new File(decodedPath, "savedListDictionary");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        filePath = Paths.get(file.toURI());

        List<String> lines = null;
        try {
            lines = Files.readAllLines(filePath, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (lines.size() > 0) {
            String str = lines.get(0);
            String[] arrayStr = str.split(";");
            numberOfBlocks = Integer.valueOf(arrayStr[0]);
            numberOfCollocationsInABlock = Integer.valueOf(arrayStr[1]);

            str = lines.get(1);
            JsonParser parser = new JsonParser();
            Gson gson = new Gson();
            JsonArray array = parser.parse(str).getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                Collocation collocation = (gson.fromJson(array.get(i), Collocation.class));
                listDictionary.add(collocation);
            }
        }

        if (listDictionary.size() == 0) {
            resetListDictionary();
        }else{
            Preferences prefs = Preferences.userNodeForPackage(wTeacher.class);
            englishLeft = prefs.getBoolean("englishLeft", true);
            answersAreHidden = prefs.getBoolean("answersWereHidden", false);
            countOfLearnedWords = prefs.getInt("countOfLearnedWords", 0);
            updateTable();

            labelNumberOfLearnedWords.setText("learned: " + Integer.toString(prefs.getInt("countOfLearnedWords", 0)));
            labelNumberOfDifficultWords.setText("difficult: " + Integer.toString(prefs.getInt("countOfDifficultWords", 0)));
            labelNumberOfWordsLeft.setText("left: " + Integer.toString(prefs.getInt("countOfLeftWords", 0)));
            labelNumberOfWordsTotal.setText("total: " + Integer.toString(prefs.getInt("countOfTotalWords", 0)));
        }

    }

    private void changeColumns(boolean softwareChange) {

        movingColumns = true;

        final TableColumnModel finalColumnModel = columnModel;

      //table.setColumnSelectionInterval(1, 1);

        if (!softwareChange) {
            englishLeft = !englishLeft;
        }

        // Индекс первой колоки
        int first = 0;
        // Индекс второй колонки
        int last = 3;
        // Перемещение столбцов
        finalColumnModel.moveColumn(first, last);
        finalColumnModel.moveColumn(first, last);

        movingColumns = false;

    }

    private void resetListDictionary() {

        Path filePath;
        URL resourceURL = getClass().getResource("/etalonDictionary");
        URI resourceURI = null;
        try {
            resourceURI = resourceURL.toURI();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        FileSystem fileSystem = null;
        if (resourceURI.getScheme().equals("jar")) {
            final Map<String, String> env = new HashMap();
            final String[] array = resourceURI.toString().split("!");
            try {
                fileSystem = FileSystems.newFileSystem(URI.create(array[0]), env);
            } catch (IOException e) {
                e.printStackTrace();
            }
            filePath = fileSystem.getPath(array[1]);
        } else {
            // Not running in a jar, so just use a regular filesystem path
            filePath = Paths.get(resourceURI);
        }

        List<String> lines = null;
        try {
            lines = Files.readAllLines(filePath, Charset.forName("UTF-8"));
            if (fileSystem != null) {
                fileSystem.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        listDictionary.clear();

        int index = 0;
        for (int i = 0; i < lines.size(); i += 2) {
            listDictionary.add(new Collocation(false, lines.get(i), false, lines.get(i + 1), false, index));
            index++;
        }

        setDataVector();
        setColumnWidth();
        defineIndexesOfWords();
        englishLeft = true;
        answersAreHidden = false;
        progressBar.setValue(0);

        labelNumberOfLearnedWords.setText("learned: 0");
        labelNumberOfDifficultWords.setText("difficult: 0");
        labelNumberOfWordsLeft.setText("left: " + Integer.toString(listDictionary.size()));
        labelNumberOfWordsTotal.setText("total: " + Integer.toString(listDictionary.size()));

    }

    private void defineIndexesOfWords() {

        List<Collocation> listDictionaryCopy = new ArrayList<Collocation>();

        int index = 0;
        for (Collocation collocation : listDictionary) {
            collocation.index = index;
            listDictionaryCopy.add(new Collocation(
                    collocation.learnedEn,
                    collocation.en,
                    collocation.learnedRu,
                    collocation.ru,
                    collocation.isDifficult,
                    collocation.index
            ));
            index++;
        }

        List<Collocation> listOfStudiedWords = new ArrayList<Collocation>();
        List<Collocation> listOfDifficultWords = new ArrayList<Collocation>();
        List<Collocation> listOfLearnedWords = new ArrayList<Collocation>();

        for (int i = 0; i < listDictionaryCopy.size(); i++) {

            Collocation collocation = listDictionaryCopy.get(i);

            if (collocation.learnedEn != collocation.learnedRu) {
                listOfStudiedWords.add(collocation);
                listDictionaryCopy.remove(i);
                i--;
                continue;
            }
            if (collocation.isDifficult) {

                listOfDifficultWords.add(collocation);
                listDictionaryCopy.remove(i);
                i--;
                continue;
            }
            if (collocation.learnedEn && collocation.learnedRu) {
                listOfLearnedWords.add(collocation);
                listDictionaryCopy.remove(i);
                i--;
                continue;
            }
        }


        int j = 0;
        for (Collocation collocation : listOfStudiedWords) {
            listDictionaryCopy.add(j, collocation);
            j++;
            rowBeginIndexOfLearnedWords = j;
        }

        for (Collocation collocation : listDictionaryCopy) {
            if (collocation.learnedEn == collocation.learnedRu) {
                j++;
            }
            rowBeginIndexOfLearnedWords = j;
        }

        countOfDifficultWords = 0;
        for (Collocation collocation : listOfDifficultWords) {
            countOfDifficultWords++;
            listDictionaryCopy.add(collocation);
            j++;
            rowBeginIndexOfLearnedWords = j;
        }
        countOfLearnedWords = 0;
        List<Collocation> listOfWellLearnedWords = new ArrayList<Collocation>();
        for (Collocation collocation : listOfLearnedWords) {
            countOfLearnedWords++;

            if (j >= rowBeginIndexOfLearnedWords + numberOfBlocks * numberOfCollocationsInABlock) {
                listOfWellLearnedWords.add(collocation);
            } else {
                listDictionaryCopy.add(collocation);
                j++;
                rowBeginIndexOfWellLearnedWords = j;
            }
        }
        rowBeginIndexOfNativeWords = rowBeginIndexOfWellLearnedWords + numberOfBlocks * numberOfCollocationsInABlock;

        if (rowBeginIndexOfWellLearnedWords == 0 || rowBeginIndexOfLearnedWords == listDictionaryCopy.size()) {
            rowBeginIndexOfWellLearnedWords = listDictionaryCopy.size();
            rowBeginIndexOfLearnedWords  = listDictionaryCopy.size();
            rowBeginIndexOfNativeWords = listDictionaryCopy.size();
        }

    }

    public void startServer(final int port) {

        //start server
        //Создание потока
        Thread serverSocketThread = new Thread(new Runnable() {
            public void run() //Этот метод будет выполняться в побочном потоке
            {
                List<Sender> senderList = new ArrayList<Sender>();

                String strLocalSocketAddress = "FAILED";

                try {
                    serverSocket = new ServerSocket(port);//7373);new ServerSocket(port, 1, InetAddress.getLocalHost());
                    portNumber = serverSocket.getLocalPort();
                    spinnerPortNumber.setValue(portNumber);

                    final DatagramSocket datagramSocket = new DatagramSocket();
                    try{
                        datagramSocket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                        String ip = datagramSocket.getLocalAddress().getHostAddress();
                        strLocalSocketAddress = ip +":"+ serverSocket.getLocalPort();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    final String finalStrLocalSocketAddress = strLocalSocketAddress;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            labelTitle.setText(title + " /socket server: " + finalStrLocalSocketAddress);//0.5Keyboard
                            System.out.println(finalStrLocalSocketAddress);
                        }
                    });


                } catch (IOException e) {
                    countOfAttemptsToCreateServer++;
                    if (countOfAttemptsToCreateServer <= NUMBER_OF_ATTEMPTS_TO_CREATE_SERVER) {
                        startServer(0);
                    }
                    e.printStackTrace();
                }
                try {

                    while (true) {
                        System.out.println("the server is start." + " /socket server: " + strLocalSocketAddress);

                        Socket s = serverSocket.accept();

                        String clientAddress = s.getInetAddress().getHostAddress();
                        System.out.println("\r\nNew connection from " + clientAddress);

                        Sender sndr = new Sender(s);
                        sndr.start();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(e);
                } finally {
            /*
            for (Sender sndr : senderList) {
                sndr.setFinishFlag();
            }
            */
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }
        });
        serverSocketThread.start();
    }

    public void scrollToRow(final int rowIndex){

        table.getSelectionModel().setSelectionInterval(rowIndex, rowIndex);
        table.scrollRectToVisible(new Rectangle(table.getCellRect(rowIndex, 0, true)));

        //не работает с первого разаа
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                table.scrollRectToVisible(new Rectangle(table.getCellRect(rowIndex, 0, true)));
            }
        });

    }

    public void save(){

        Preferences prefs = Preferences.userNodeForPackage(wTeacher.class);
        prefs.putInt("fontSize", fontSize);
        prefs.putInt("portNumber", portNumber);
        prefs.putInt("selectedRow", table.getSelectedRow());
        prefs.putInt("countOfLearnedWords", countOfLearnedWords);

        prefs.putInt("countOfLearnedWords", countOfLearnedWords);
        prefs.putInt("countOfDifficultWords", countOfDifficultWords);
        prefs.putInt("countOfLeftWords", listDictionary.size() - countOfLearnedWords);
        prefs.putInt("countOfTotalWords", listDictionary.size());

        prefs.putBoolean("englishLeft", englishLeft);
        prefs.putBoolean("answersWereHidden", answersAreHidden);

        saveListDictionary();
        saveWordsForKeyboardSimulator();
    }

    public void setDataVector(){

        String headers[] = { "", "En", "", "Ru", "index" };
        Object[][] rows = new Object[listDictionary.size()][headers.length];
        int index = 0;
        for (Collocation collocation : listDictionary) {
            collocation.index = index;

            Object[] data = new Object[headers.length];
            data[0] = collocation.learnedEn;
            data[1] = collocation.en;
            data[2] = collocation.learnedRu;
            data[3] = collocation.ru;
            data[4] = collocation.index;

            rows[index++] = data;
        }

        dtm.setDataVector(rows, headers);

    }

    public void updateTable(){

        defineIndexesOfWords();

        setDataVector();

        setColumnWidth();

        if (!englishLeft) {
            changeColumns(true);
        }

        if (answersAreHidden) {
            hideAnswers();
        }
    }

    public void playNextPoint(){

        playedNextPoint = true;

        try {
            URL soundURL = this.getClass().getResource("/nextPoint.wav"); //Звуковой файл

            //Получаем AudioInputStream
            //Вот тут могут полететь IOException и UnsupportedAudioFileException
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL);

            //Получаем реализацию интерфейса Clip
            //Может выкинуть LineUnavailableException
            Clip clip = AudioSystem.getClip();

            //Загружаем наш звуковой поток в Clip
            //Может выкинуть IOException и LineUnavailableException
            clip.open(ais);

            /*
            //Получаем контроллер громкости
            FloatControl vc = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            //Устанавливаем значение
            //Оно должно быть в пределах от vc.getMinimum() до vc.getMaximum()
            vc.setValue(5); //Громче обычного
            */

            clip.setFramePosition(0); //устанавливаем указатель на старт
            clip.start(); //Поехали!!!

        } catch (IOException exc) {
            exc.printStackTrace();
        } catch (UnsupportedAudioFileException e1) {
            e1.printStackTrace();
        } catch (LineUnavailableException e1) {
            e1.printStackTrace();
        }

    }

    public Character[] createArrayOfCharactersByLine(String line){

        Character[] arrayOfCharacters = new Character[line.length()];
        for (int i = 0; i < line.length(); i++) {
            arrayOfCharacters[i] = line.charAt(i);
        }

        return arrayOfCharacters;
    }

    public StateMap[] createStateMap(String original, String answer) {

        Character[] x = createArrayOfCharactersByLine(original);
        Character[] y = createArrayOfCharactersByLine(answer);

        int m = x.length;
        int n = y.length;
        int[][] len = new int[m + 1][n + 1];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (x[i] == y[j]) {
                    len[i + 1][j + 1] = len[i][j] + 1;
                } else {
                    len[i + 1][j + 1] = Math.max(len[i + 1][j], len[i][j + 1]);
                }
            }
        }
        int cnt = len[m][n];
        StateMap[] res = new StateMap[cnt];
        for (int i = m - 1, j = n - 1; i >= 0 && j >= 0;) {
            if (x[i] == y[j]) {
                res[--cnt] = new StateMap(x[i], i, j);
                --i;
                --j;
            } else if (len[i + 1][j] > len[i][j + 1]) {
                --j;
            } else {
                --i;
            }
        }

        return res;
    }

    class StateMap {

        Character unit;
        int indexX;
        int indexY;

        public StateMap(Character unit, int indexX, int indexY) {
            this.unit = unit;
            this.indexX = indexX;
            this.indexY = indexY;
        }
    }

    class Sender extends Thread {
        Socket socket;
        int senderNumber;

        public Sender(Socket s) {
            this.socket = s;
            System.out.print("Sender started: #");
            senderNumber = ++sendersCreated;
            System.out.println(senderNumber);
        }


        public void run() {

            defineIndexesOfWords();

            try {
                ///*
                InputStream sin = socket.getInputStream();
                OutputStream sout = socket.getOutputStream();

                DataInputStream in = new DataInputStream(sin);
                DataOutputStream out = new DataOutputStream(sout);

                ObjectOutputStream oos = new ObjectOutputStream(sout);
                ObjectInputStream ois = new ObjectInputStream(sin);

                boolean swapOnAndroid = in.readBoolean();
                int countOfLearnedWordsOnAndroid = in.readInt();
                if ((countOfLearnedWords < countOfLearnedWordsOnAndroid)
                        || (countOfLearnedWords == countOfLearnedWordsOnAndroid && swapOnAndroid)) {
                    out.writeUTF("unloading");//инструкция для Android

                    numberOfBlocks = in.readInt();
                    spinnerNumberOfBlocks.setValue(numberOfBlocks);

                    numberOfCollocationsInABlock = in.readInt();
                    spinnerNumberOfCollocationsInABlock.setValue(numberOfCollocationsInABlock);

                    String jsonStr = (String) ois.readObject();//listDictionary = (ArrayList<Collocation>) ois.readObject();

                    JsonParser parser = new JsonParser();
                    Gson gson = new Gson();
                    JsonArray array = parser.parse(jsonStr).getAsJsonArray();
                    listDictionary.clear();

                    for (int i = 0; i < array.size(); i++) {
                        Collocation collocation = (gson.fromJson(array.get(i), Collocation.class));
                        listDictionary.add(collocation);
                    }

                    updateTable();

                    progressBar.setValue((int) ((double) countOfLearnedWords / listDictionary.size() * 100));

                    labelNumberOfLearnedWords.setText("learned: " + Integer.toString(countOfLearnedWords));
                    labelNumberOfDifficultWords.setText("difficult: " + Integer.toString(countOfDifficultWords));
                    labelNumberOfWordsLeft.setText("left: " + Integer.toString(listDictionary.size() - countOfLearnedWords));
                    labelNumberOfWordsTotal.setText("total: " + Integer.toString(listDictionary.size()));

                } else {
                    out.writeUTF("loading");//инструкция для Android

                    out.writeInt(numberOfBlocks);
                    out.writeInt(numberOfCollocationsInABlock);

                    String jsonStr = new Gson().toJson(listDictionary);
                    oos.writeObject(jsonStr);
                    oos.flush();
                }


            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e);
            }
        }
    }



    public class EditorPaneRenderer extends JTextPane implements TableCellRenderer {

        private final DefaultTableCellRenderer adaptee = new DefaultTableCellRenderer();
        StyledDocument doc = new DefaultStyledDocument();
        Random random = new Random();
        int last_row = -1;
        SimpleAttributeSet set = new SimpleAttributeSet();

        public EditorPaneRenderer() {
            setStyledDocument(doc);
        }

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {

            setFont(new Font(adaptee.getFont().getName(), adaptee.getFont().getStyle(), fontSize));
            String comparison = original + "\n" + answer;
            setText(comparison);

            for (int i = 0; i < comparison.length(); i++) {
                if (i <= original.length()){
                    StyleConstants.setForeground(set, Color.BLUE);
                    doc.setCharacterAttributes(i, 1, set, true);
                }else{
                    StyleConstants.setForeground(set, Color.RED);
                    doc.setCharacterAttributes(i, 1, set, true);
                }

            }

            StateMap[] stateMap = createStateMap(original, answer);

            int s = 0;
            for (int i = 0; i < comparison.length(); i++) {
                if(i <= original.length()){
                    for (int j = 0; j < stateMap.length; j++) {
                        if(stateMap[j].unit.equals(comparison.charAt(i)) && stateMap[j].indexX == i){
                            StyleConstants.setForeground(set, Color.GRAY);
                            doc.setCharacterAttributes(i, 1, set, true);
                            break;
                        }
                    }
                }else{
                    for (int j = 0; j < stateMap.length; j++) {
                        if(stateMap[j].unit.equals(comparison.charAt(i)) && stateMap[j].indexY == s){
                            StyleConstants.setForeground(set, new Color(0, 200, 0));
                            doc.setCharacterAttributes(i, 1, set, true);
                            break;
                        }
                    }
                    s++;
                }

            }

            Rectangle rect = table.getCellRect(row, column, true);
            this.setSize(rect.getSize());//для установки ширины компоненты
            int height_wanted = (int) getPreferredSize().getHeight();
            if ((height_wanted > table.getRowHeight(row) | row != last_row) &  //если новая строчка и полученная высота больше чем установленна
                    height_wanted > table.getRowHeight()) //и вывсота больше чем дефолтная
                table.setRowHeight(row, height_wanted);

            last_row = row;

            return this;
        }

    }


    /**
     * *****************************************************************************
     * Класс рендерера ячейки таблицы, поддерживающий многострочный текст
     * и автоматически подбирающий высоту ячейки так, чтобы полностью показать
     * все содержимое ячейки (в т.ч. динамически)
     */
    public class TextAreaRenderer extends JTextArea implements TableCellRenderer {
        private final DefaultTableCellRenderer adaptee = new DefaultTableCellRenderer();

        public TextAreaRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
        }

        int last_row = -1;

        public Component getTableCellRendererComponent(JTable table, Object obj,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {

            adaptee.getTableCellRendererComponent(table, obj, isSelected, hasFocus, row, column);
            setForeground(adaptee.getForeground());
            setBackground(adaptee.getBackground());
            setBorder(adaptee.getBorder());
            setFont(adaptee.getFont());
            setText(adaptee.getText());

            setText(obj == null ? "" : obj.toString());

            Rectangle rect = table.getCellRect(row, column, true);
            this.setSize(rect.getSize());//для установки ширины компоненты
            int height_wanted = (int) getPreferredSize().getHeight();
            if ((height_wanted > table.getRowHeight(row) | row != last_row) &  //если новая строчка и полученная высота больше чем установленна
                    height_wanted > table.getRowHeight()) //и вывсота больше чем дефолтная
                table.setRowHeight(row, height_wanted);

            last_row = row;

            return this;
        }


    }


    public class TimerLabel extends JLabel {
        private Timer timer;
        private long startTime;
        private long time;

        /**
         * Constructor TimerLabel
         */
        public TimerLabel() {
            super();
            resetText();
        }

        /**
         * @return Returns the timer.
         */
        private final Timer getTimer() {
            if (timer == null) {
                int delay = 1; // milliseconds
                ActionListener taskPerformer = new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        taskPerformed();
                    }
                };
                //
                timer = new Timer(delay, taskPerformer);
                timer.setInitialDelay(0);
            }
            return timer;
        }

        /**
         * @return Returns the time.
         */
        private final long getTime() {
            return time;
        }

        /**
         * Method taskPerformed
         */
        private final void taskPerformed() {
            //long time = System.currentTimeMillis() - startTime - 1000*60*60*2; // milliseconds
            time = System.currentTimeMillis() - startTime; // milliseconds
            setText(StringUtils.timeToString(time));
        }

        /**
         * Method resetText
         */
        public final void resetText() {
            setText("00:00:00"); //$NON-NLS-1$
        }

        /**
         * Method setStringTime
         */
        public final void setStringTime(String text) {
            setText(text);
        }

        /**
         * Method setTimeColor
         */
        public final void setTimeColor(Color color) {
            setForeground(color);
        }

        /**
         * Method startTimer
         */
        public final synchronized void startTimer() {
            startTime = System.currentTimeMillis();
            getTimer().start();
        }

        /**
         * Method stopTimer
         */
        public final synchronized void stopTimer() {
            getTimer().stop();
        }

        /**
         * Method isTimerRunning
         *
         * @return boolean
         */
        public final boolean isTimerRunning() {
            return getTimer().isRunning();
        }

    }


}
