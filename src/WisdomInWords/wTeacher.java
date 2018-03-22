package WisdomInWords;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.LineBorder;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;


/**
 * Created by User on 06.12.2017.
 */
public class wTeacher extends JFrame {

    private JPanel panel;
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
    String title = "ILEW - I Lern English Words";
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


    private boolean EnglishTextLayout = false;
    char[] ArrayEnglishCharacters = {'h', 'j', 'k', 'l', 'y', 'u', 'i', 'o',
            'p', '[', ']', 'n', 'm',
            'g', 'f', 'd', 's', 'a', 'b', 'v', 'c', 'x', 'z', 't', 'r', 'e', 'w', 'q', '`'};

    char[] ArrayRussianCharacters = {'р', 'о', 'л', 'д', 'ж', 'э', 'н', 'г',
            'ш', 'щ', 'з', 'х', 'ъ', 'т', 'ь', 'б', 'ю',
            'п', 'а', 'в', 'ы', 'ф', 'и', 'м', 'с', 'ч', 'я', 'е', 'к', 'у', 'ц', 'й', 'ё'};


    /////////////
    HttpsURLConnection yc;
    ///////////

    public static void main(String[] args) {

        new wTeacher();

    }

    private wTeacher() {
        initPanel();
        initFrame();
    }

    private void initFrame() {
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle(title);//0.5Keyboard
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());
        setVisible(true);

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {

                if(!swap){
                    save();
                }

            }
        });
    }

    private void initPanel() {


        panel = new JPanel();
        panel.setPreferredSize(new Dimension(900, 550));
        getContentPane().add(panel);

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
                Collocation collocation = listDictionary.get(rowIndex);
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
                    default:
                        return String.class;
                }

            }

        };

        table.setModel(dtm);
        //table.putClientProperty("JTable.autoStartsEdit", true);
        table.setSurrendersFocusOnKeystroke(true);

        dtm.addColumn("");
        dtm.addColumn("En");
        dtm.addColumn("");
        dtm.addColumn("Ru");

        columnModel = table.getColumnModel();
        restoreListDictionary();
        defineIndexesOfWords();

        Preferences prefs = Preferences.userNodeForPackage(wTeacher.class);
        labelNumberOfLearnedWords.setText("learned: " + Integer.toString(prefs.getInt("countOfLearnedWords", 0)));
        labelNumberOfDifficultWords.setText("difficult: " + Integer.toString(prefs.getInt("countOfDifficultWords", 0)));
        labelNumberOfWordsLeft.setText("left: " + Integer.toString(prefs.getInt("countOfLeftWords", 0)));
        labelNumberOfWordsTotal.setText("total: " + Integer.toString(prefs.getInt("countOfTotalWords", 0)));

        countOfLearnedWords = prefs.getInt("countOfLearnedWords", 0);
        progressBar.setValue((int) ((double) countOfLearnedWords / listDictionary.size() * 100));

        int j = 0;
        for (Collocation i : listDictionary) {
            dtm.addRow(new Object[0]);
            dtm.setValueAt(i.learnedEn, j, 0);
            dtm.setValueAt(i.en, j, 1);
            dtm.setValueAt(i.learnedRu, j, 2);
            dtm.setValueAt(i.ru, j, 3);
            j++;
        }

        setColumnWidth();

        englishLeft = prefs.getBoolean("englishLeft", true);
        if (!englishLeft) {
            changeColumns(true);
        }

        final boolean answersWereHidden = prefs.getBoolean("answersWereHidden", false);
        if (answersWereHidden) {
            hideAnswers();
        }

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

                int indexOfTheSelectedRow = table.getSelectedRow();
                int indexConvertOfTheSelectedColumn = table.convertColumnIndexToModel(table.getSelectedColumn());
                if (indexOfTheSelectedRow == -1 || indexConvertOfTheSelectedColumn == -1
                        || indexOfTheSelectedRow >= dtm.getDataVector().size()
                        || listDictionary.size() == 0
                        || filterChanged
                        || indexOfTheSelectedRow == -1
                        || previousRow == -1) {

                    if (jtfFilterValue.getText().isEmpty()) {
                        filterChanged = false;
                    }
                    return;
                }
                int indexOfTheSelectedColumn = table.getSelectedColumn();
                Object v = dtm.getValueAt(indexOfTheSelectedRow, indexConvertOfTheSelectedColumn);
                Collocation collocation = listDictionary.get(indexOfTheSelectedRow);

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
                            if (englishLeft) {
                                dtm.setValueAt(false, table.getSelectedRow(), 2);
                            } else {
                                dtm.setValueAt(false, table.getSelectedRow(), 0);
                            }
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
                            if (englishLeft) {
                                dtm.setValueAt(true, table.getSelectedRow(), 0);
                            } else {
                                dtm.setValueAt(true, table.getSelectedRow(), 2);
                            }

                        }
                    }

                    if (!stringSwitch) {
                        stringSwitch = true;

                        if (collocation.learnedEn && collocation.learnedRu) {
                            if (englishLeft) {
                                dtm.setValueAt(collocation.ru, indexOfTheSelectedRow, 3);
                            } else {
                                dtm.setValueAt(collocation.en, indexOfTheSelectedRow, 1);
                            }
                        } else if (collocation.learnedEn != collocation.learnedRu && answersAreHidden) {
                            if (englishLeft) {
                                dtm.setValueAt("", table.getSelectedRow(), 3);
                            } else {
                                dtm.setValueAt("", table.getSelectedRow(), 1);
                            }
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
                    if (rowChanged || columnChanged || hideAnswers || showAnswers
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
                            dtm.setValueAt("",
                                    table.getSelectedRow(),
                                    indexConvertOfTheSelectedColumn);


                            if (indexOfTheSelectedRow == selectedRowForTimer) {
                                startStopTimer(true, true);
                            }
                        } else {
                            dtm.setValueAt(resultText,
                                    table.getSelectedRow(),
                                    indexConvertOfTheSelectedColumn);

                            if (indexOfTheSelectedRow == selectedRowForTimer) {
                                startStopTimer(true, true);
                            }
                        }

                        if (!englishLeft) {
                            listDictionary.get(indexOfTheSelectedRow).isDifficult = false;
                        }
                    } else {
                        if (englishLeft) {
                            resultText = original;
                        } else {
                            resultText = original + "⚓";
                            collocation.isDifficult = true;

                            startStopTimer(false, true);
                        }
                        if (answersAreHidden) {
                            dtm.setValueAt("",
                                    table.getSelectedRow(),
                                    indexConvertOfTheSelectedColumn);
                        } else {
                            dtm.setValueAt(resultText,
                                    table.getSelectedRow(),
                                    indexConvertOfTheSelectedColumn);
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

                changeColumns(false);

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

                int k = getContentPane().getComponentCount();
                for (int i = 0; i < k; i++) {
                    getContentPane().remove(0);
                }
                getContentPane().add(panel);
                getContentPane().revalidate();
                getContentPane().repaint();
                setSize(new Dimension(getSize().width, getSize().height - 1));

            }
        });

        // Кнопка Сортировать
        JButton btnSort = new JButton("Sort");
        // Слушатель обработки события
        btnSort.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

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

                table.clearSelection();
                dtm.getDataVector().clear();

                int j = 0;
                for (Collocation collocation : listOfFavoriteWords) {
                    listDictionary.add(j, collocation);
                    dtm.addRow(new Object[0]);
                    dtm.setValueAt(collocation.learnedEn, j, 0);
                    dtm.setValueAt(collocation.en, j, 1);
                    dtm.setValueAt(collocation.learnedRu, j, 2);
                    dtm.setValueAt(collocation.ru, j, 3);
                    j++;
                    rowBeginIndexOfLearnedWords = j;
                }
                for (Collocation collocation : listOfStudiedWords) {
                    listDictionary.add(j, collocation);
                    dtm.addRow(new Object[0]);
                    dtm.setValueAt(collocation.learnedEn, j, 0);
                    dtm.setValueAt(collocation.en, j, 1);
                    dtm.setValueAt(collocation.learnedRu, j, 2);
                    dtm.setValueAt(collocation.ru, j, 3);
                    j++;
                    rowBeginIndexOfLearnedWords = j;
                }
                for (Collocation collocation : listDictionary) {
                    if (collocation.learnedEn == collocation.learnedRu) {
                        dtm.addRow(new Object[0]);
                        dtm.setValueAt(collocation.learnedEn, j, 0);
                        dtm.setValueAt(collocation.en, j, 1);
                        dtm.setValueAt(collocation.learnedRu, j, 2);
                        dtm.setValueAt(collocation.ru, j, 3);
                        j++;
                    }
                    rowBeginIndexOfLearnedWords = j;
                }
                countOfDifficultWords = 0;
                for (Collocation collocation : listOfDifficultWords) {
                    countOfDifficultWords++;

                    listDictionary.add(collocation);
                    dtm.addRow(new Object[0]);
                    dtm.setValueAt(collocation.learnedEn, j, 0);
                    dtm.setValueAt(collocation.en, j, 1);
                    dtm.setValueAt(collocation.learnedRu, j, 2);
                    dtm.setValueAt(collocation.ru, j, 3);
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
                        dtm.addRow(new Object[0]);
                        dtm.setValueAt(collocation.learnedEn, j, 0);
                        dtm.setValueAt(collocation.en, j, 1);
                        dtm.setValueAt(collocation.learnedRu, j, 2);
                        dtm.setValueAt(collocation.ru, j, 3);
                        j++;
                        rowBeginIndexOfWellLearnedWords = j;
                    }
                }
                rowBeginIndexOfNativeWords = rowBeginIndexOfWellLearnedWords + numberOfBlocks * numberOfCollocationsInABlock;
                progressBar.setValue((int) ((double) countOfLearnedWords / listDictionary.size() * 100));
                for (Collocation collocation : listOfWellLearnedWords) {
                    listDictionary.add(collocation);
                    dtm.addRow(new Object[0]);
                    dtm.setValueAt(collocation.learnedEn, j, 0);
                    dtm.setValueAt(collocation.en, j, 1);
                    dtm.setValueAt(collocation.learnedRu, j, 2);
                    dtm.setValueAt(collocation.ru, j, 3);
                    j++;
                }

                labelNumberOfLearnedWords.setText("learned: " + Integer.toString(countOfLearnedWords));
                labelNumberOfDifficultWords.setText("difficult: " + Integer.toString(countOfDifficultWords));
                labelNumberOfWordsLeft.setText("left: " + Integer.toString(listDictionary.size() - countOfLearnedWords));
                labelNumberOfWordsTotal.setText("total: " + Integer.toString(listDictionary.size()));

                Preferences prefs = Preferences.userNodeForPackage(wTeacher.class);
                prefs.putInt("countOfLearnedWords", countOfLearnedWords);
                prefs.putInt("countOfDifficultWords", countOfDifficultWords);
                prefs.putInt("countOfLeftWords", listDictionary.size() - countOfLearnedWords);
                prefs.putInt("countOfTotalWords", listDictionary.size());


                boolean answersWereHidden = prefs.getBoolean("answersWereHidden", false);
                if (answersWereHidden) {
                    hideAnswers();
                }

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

                table.clearSelection();
                dtm.getDataVector().clear();

                int j = 0;
                for (Collocation collocation : listOfFavoriteWords) {
                    listDictionary.add(j, collocation);
                    dtm.addRow(new Object[0]);
                    dtm.setValueAt(collocation.learnedEn, j, 0);
                    dtm.setValueAt(collocation.en, j, 1);
                    dtm.setValueAt(collocation.learnedRu, j, 2);
                    dtm.setValueAt(collocation.ru, j, 3);
                    j++;
                    rowBeginIndexOfLearnedWords = j;
                }

                for (Collocation collocation : listOfStudiedWords) {
                    listDictionary.add(j, collocation);
                    dtm.addRow(new Object[0]);
                    dtm.setValueAt(collocation.learnedEn, j, 0);
                    dtm.setValueAt(collocation.en, j, 1);
                    dtm.setValueAt(collocation.learnedRu, j, 2);
                    dtm.setValueAt(collocation.ru, j, 3);
                    j++;
                    rowBeginIndexOfLearnedWords = j;
                }

                for (Collocation collocation : listDictionary) {
                    if (collocation.learnedEn == collocation.learnedRu) {
                        dtm.addRow(new Object[0]);
                        dtm.setValueAt(collocation.learnedEn, j, 0);
                        dtm.setValueAt(collocation.en, j, 1);
                        dtm.setValueAt(collocation.learnedRu, j, 2);
                        dtm.setValueAt(collocation.ru, j, 3);
                        j++;
                    }
                    rowBeginIndexOfLearnedWords = j;
                }
                countOfDifficultWords = 0;
                for (Collocation collocation : listOfDifficultWords) {
                    countOfDifficultWords++;
                    listDictionary.add(collocation);
                    dtm.addRow(new Object[0]);
                    dtm.setValueAt(collocation.learnedEn, j, 0);
                    dtm.setValueAt(collocation.en, j, 1);
                    dtm.setValueAt(collocation.learnedRu, j, 2);
                    dtm.setValueAt(collocation.ru, j, 3);
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
                        dtm.addRow(new Object[0]);
                        dtm.setValueAt(collocation.learnedEn, j, 0);
                        dtm.setValueAt(collocation.en, j, 1);
                        dtm.setValueAt(collocation.learnedRu, j, 2);
                        dtm.setValueAt(collocation.ru, j, 3);
                        j++;
                        rowBeginIndexOfWellLearnedWords = j;
                    }
                }
                rowBeginIndexOfNativeWords = rowBeginIndexOfWellLearnedWords + numberOfBlocks * numberOfCollocationsInABlock;
                progressBar.setValue((int) ((double) countOfLearnedWords / listDictionary.size() * 100));
                Collections.shuffle(listOfWellLearnedWords);
                for (Collocation collocation : listOfWellLearnedWords) {
                    listDictionary.add(collocation);
                    dtm.addRow(new Object[0]);
                    dtm.setValueAt(collocation.learnedEn, j, 0);
                    dtm.setValueAt(collocation.en, j, 1);
                    dtm.setValueAt(collocation.learnedRu, j, 2);
                    dtm.setValueAt(collocation.ru, j, 3);
                    j++;
                }

                labelNumberOfLearnedWords.setText("learned: " + Integer.toString(countOfLearnedWords));
                labelNumberOfDifficultWords.setText("difficult: " + Integer.toString(countOfDifficultWords));
                labelNumberOfWordsLeft.setText("left: " + Integer.toString(listDictionary.size() - countOfLearnedWords));
                labelNumberOfWordsTotal.setText("total: " + Integer.toString(listDictionary.size()));

                Preferences prefs = Preferences.userNodeForPackage(wTeacher.class);
                prefs.putInt("countOfLearnedWords", countOfLearnedWords);
                prefs.putInt("countOfDifficultWords", countOfDifficultWords);
                prefs.putInt("countOfLeftWords", listDictionary.size() - countOfLearnedWords);
                prefs.putInt("countOfTotalWords", listDictionary.size());


                boolean answersWereHidden = prefs.getBoolean("answersWereHidden", false);
                if (answersWereHidden) {
                    hideAnswers();
                }

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

                        String content = v.toString();
                        content = content.replace("✓", "").replace("⚓", "");

                        getContentPane().remove(0);
                        getContentPane().add(btnrBack, BorderLayout.PAGE_START);
                        SwtBrowserCanvas.addBrowserToFrame(wTeacher.this);

                        getContentPane().revalidate();
                        getContentPane().repaint();

                        setSize(new Dimension(getSize().width, getSize().height + 1));


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

                int value = (Integer) spinnerPortNumber.getValue();
                if (value != portNumber) {
                    portNumber = value;
                    /*
                    if(serverSocket != null){
                        try {
                            serverSocket.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                    */
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
                    Collocation collocation = new Collocation(false, collocationParts[0].trim(), false, collocationParts[1].trim(), false);
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
                    dtm.insertRow(0, new Object[0]);
                    dtm.setValueAt(collocation.learnedEn, 0, 0);
                    dtm.setValueAt(collocation.en, 0, 1);
                    dtm.setValueAt(collocation.learnedRu, 0, 2);
                    dtm.setValueAt(collocation.ru, 0, 3);

                    jtfFilterValue.setText("");
                    setRowFilter("");

                    Preferences prefs = Preferences.userNodeForPackage(wTeacher.class);
                    int countOfLeftWords = prefs.getInt("countOfLeftWords", 0);

                    countOfLeftWords++;
                    labelNumberOfWordsLeft.setText("left: " + Integer.toString(countOfLeftWords));
                    labelNumberOfWordsTotal.setText("total: " + Integer.toString(listDictionary.size()));

                    prefs.putInt("countOfLeftWords", countOfLeftWords);
                    prefs.putInt("countOfTotalWords", listDictionary.size());

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

                int indexOfTheSelectedRow = table.getSelectedRow();
                if (indexOfTheSelectedRow != -1) {
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

                        Preferences prefs = Preferences.userNodeForPackage(wTeacher.class);
                        int countOfLearnedWords = prefs.getInt("countOfLearnedWords", 0);
                        int countOfLeftWords = prefs.getInt("countOfLeftWords", 0);
                        labelNumberOfWordsTotal.setText("total: " + Integer.toString(listDictionary.size()));
                        if (collocation.learnedEn && collocation.learnedRu) {
                            countOfLearnedWords--;
                            labelNumberOfLearnedWords.setText("lerned: " + Integer.toString(countOfLearnedWords));
                        } else {
                            countOfLeftWords--;
                            labelNumberOfWordsLeft.setText("left: " + Integer.toString(countOfLeftWords));
                        }
                        prefs.putInt("countOfLeftWords", countOfLeftWords);
                        prefs.putInt("countOfTotalWords", listDictionary.size());


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

        startServer(portNumber);

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
        panel.add(btnAdd);
        panel.add(btnDelete);
        panel.add(labelFontSize);
        panel.add(spinnerFontSize);
        panel.add(labelNumberOfBlocks);
        panel.add(spinnerNumberOfBlocks);
        panel.add(labelNumberOfCollocationsInABlock);
        panel.add(spinnerNumberOfCollocationsInABlock);

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

        if (englishLeft) {
            for (int i = 0; i < listDictionary.size(); i++) {
                dtm.setValueAt("", i, 3);
            }
        } else {
            for (int i = 0; i < listDictionary.size(); i++) {
                dtm.setValueAt("", i, 1);
            }
        }

        hideAnswers = false;
        answersAreHidden = true;
        Preferences prefs = Preferences.userNodeForPackage(wTeacher.class);
        prefs.putBoolean("answersWereHidden", answersAreHidden);

    }

    private void showAnswers() {

        showAnswers = true;
        tableChanged = false;
        answersAreHidden = false;

        int j = 0;
        if (englishLeft) {
            for (Collocation collocation : listDictionary) {
                dtm.setValueAt(collocation.ru, j, 3);
                j++;
            }
        } else {
            for (Collocation collocation : listDictionary) {
                dtm.setValueAt(collocation.en, j, 1);
                j++;
            }
        }
        showAnswers = false;

        Preferences prefs = Preferences.userNodeForPackage(wTeacher.class);
        prefs.putBoolean("answersWereHidden", answersAreHidden);
    }

    private void setColumnWidth() {

        columnModel.getColumn(0).setMaxWidth(25);
        columnModel.getColumn(1).setMaxWidth(425);
        columnModel.getColumn(2).setMaxWidth(25);
        columnModel.getColumn(3).setMaxWidth(425);

    }

    private void setRowFilter(String text) {

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
            lines = Files.readAllLines(filePath, Charset.forName("UTF-8"));//"UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //for (String str : lines){

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

        //}

        if (listDictionary.size() == 0) {
            resetListDictionary();
        }

    }

    private void changeColumns(boolean softwareChange) {

        final TableColumnModel finalColumnModel = columnModel;

        table.setColumnSelectionInterval(1, 1);

        Preferences prefs = Preferences.userNodeForPackage(wTeacher.class);
        boolean answersWereHidden = prefs.getBoolean("answersWereHidden", false);

        if (answersAreHidden) {
            answersWereHidden = true;
        }
        showAnswers();

        if (softwareChange) {
            englishLeft = prefs.getBoolean("englishLeft", englishLeft);
        } else {
            englishLeft = !englishLeft;
            prefs.putBoolean("englishLeft", englishLeft);
        }

        // Индекс первой колоки
        int first = 0;
        // Индекс второй колонки
        int last = 3;
        // Перемещение столбцов
        finalColumnModel.moveColumn(first, last);
        finalColumnModel.moveColumn(first, last);

        if (answersWereHidden) {
            hideAnswers();
        }


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
        for (int i = 0; i < lines.size(); i += 2) {
            listDictionary.add(new Collocation(false, lines.get(i), false, lines.get(i + 1), false));
        }

        table.clearSelection();
        dtm.getDataVector().clear();
        int j = 0;
        for (Collocation i : listDictionary) {
            dtm.addRow(new Object[0]);
            dtm.setValueAt(i.learnedEn, j, 0);
            dtm.setValueAt(i.en, j, 1);
            dtm.setValueAt(i.learnedRu, j, 2);
            dtm.setValueAt(i.ru, j, 3);
            j++;
        }

        labelNumberOfLearnedWords.setText("learned: 0");
        labelNumberOfDifficultWords.setText("difficult: 0");
        labelNumberOfWordsLeft.setText("left: " + Integer.toString(listDictionary.size()));
        labelNumberOfWordsTotal.setText("total: " + Integer.toString(listDictionary.size()));

        Preferences prefs = Preferences.userNodeForPackage(wTeacher.class);

        prefs.putInt("countOfLearnedWords", 0);
        prefs.putInt("countOfDifficultWords", 0);
        prefs.putInt("countOfLeftWords", listDictionary.size());
        prefs.putInt("countOfTotalWords", listDictionary.size());

    }

    private void defineIndexesOfWords() {

        List<Collocation> listDictionaryCopy = new ArrayList<Collocation>();

        for (Collocation collocation : listDictionary) {
            listDictionaryCopy.add(new Collocation(
                    collocation.learnedEn,
                    collocation.en,
                    collocation.learnedRu,
                    collocation.ru,
                    collocation.isDifficult
            ));
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

        if (rowBeginIndexOfWellLearnedWords == 0) {
            rowBeginIndexOfWellLearnedWords = listDictionaryCopy.size();
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
                            wTeacher.this.setTitle(title + " /socket server: " + finalStrLocalSocketAddress);//0.5Keyboard
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

        saveListDictionary();
        saveWordsForKeyboardSimulator();
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
                    table.clearSelection();
                    dtm.getDataVector().clear();
                    for (int i = 0; i < array.size(); i++) {
                        Collocation collocation = (gson.fromJson(array.get(i), Collocation.class));
                        listDictionary.add(collocation);

                        dtm.addRow(new Object[0]);
                        dtm.setValueAt(collocation.learnedEn, i, 0);
                        dtm.setValueAt(collocation.en, i, 1);
                        dtm.setValueAt(collocation.learnedRu, i, 2);
                        dtm.setValueAt(collocation.ru, i, 3);
                    }

                    defineIndexesOfWords();
                    progressBar.setValue((int) ((double) countOfLearnedWords / listDictionary.size() * 100));

                    labelNumberOfLearnedWords.setText("learned: " + Integer.toString(countOfLearnedWords));
                    labelNumberOfDifficultWords.setText("difficult: " + Integer.toString(countOfDifficultWords));
                    labelNumberOfWordsLeft.setText("left: " + Integer.toString(listDictionary.size() - countOfLearnedWords));
                    labelNumberOfWordsTotal.setText("total: " + Integer.toString(listDictionary.size()));

                    Preferences prefs = Preferences.userNodeForPackage(wTeacher.class);
                    prefs.putInt("countOfLearnedWords", countOfLearnedWords);
                    prefs.putInt("countOfDifficultWords", countOfDifficultWords);
                    prefs.putInt("countOfLeftWords", listDictionary.size() - countOfLearnedWords);
                    prefs.putInt("countOfTotalWords", listDictionary.size());


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
            setText(original + "\r\n" + answer);

            for (int i = 0; i < original.length(); i++) {
                StyleConstants.setForeground(set, Color.BLUE);
                doc.setCharacterAttributes(i, 1, set, true);
            }

            int j = 0;
            for (int i = original.length() + 1; i < getStyledDocument().getLength(); i++) {
                if(j < original.length()) {
                    StyleConstants.setForeground(set, Color.GRAY);
                    doc.setCharacterAttributes(j, 1, set, true);
                }

                if (j >= original.length() || original.charAt(j) != answer.charAt(j)) {
                    StyleConstants.setForeground(set, Color.RED);
                    doc.setCharacterAttributes(i, 1, set, true);


                    if (j <= original.length()) {
                        StyleConstants.setForeground(set, Color.BLUE);
                        doc.setCharacterAttributes(j, 1, set, true);
                    }

                } else {
                    StyleConstants.setForeground(set, new Color(0, 200, 0));
                    doc.setCharacterAttributes(i, 1, set, true);
                }
                j++;
            }

            j = answer.length() - 1;
            for (int i = getStyledDocument().getLength(); i >= 0; i--) {

                boolean charactersEqual;

                if (j >= answer.length() - original.length() && answer.length() >= original.length()) {
                    charactersEqual = original.charAt(j - (answer.length() - original.length())) == answer.charAt(j);

                    int pos = original.length() + 1 + j;
                    Element el = doc.getCharacterElement(pos);
                    AttributeSet attr = el.getAttributes();
                    Color color = StyleConstants.getForeground(attr);
                    if (color.equals(Color.RED) && charactersEqual) {
                        StyleConstants.setForeground(set, new Color(0, 200, 0));
                        doc.setCharacterAttributes(pos, 1, set, true);
                    }
                } else if (i < original.length() && answer.length() < original.length() && i >= original.length() - answer.length()) {
                    charactersEqual = original.charAt(i) == answer.charAt(i - (original.length() - answer.length()));

                    int pos = original.length() + 1 + (i - (original.length() - answer.length()));
                    Element el = doc.getCharacterElement(pos);
                    AttributeSet attr = el.getAttributes();
                    Color color = StyleConstants.getForeground(attr);
                    if (color.equals(Color.RED) && charactersEqual) {
                        StyleConstants.setForeground(set, new Color(0, 200, 0));
                        doc.setCharacterAttributes(pos, 1, set, true);
                    }
                }


                if (i < original.length() && answer.length() >= original.length()) {
                    charactersEqual = original.charAt(i) == answer.charAt(i + answer.length() - original.length());

                    if (charactersEqual) {
                        StyleConstants.setForeground(set, Color.GRAY);
                        doc.setCharacterAttributes(i, 1, set, true);
                    }
                } else if (i < original.length() && answer.length() < original.length() && i >= original.length() - answer.length()) {
                    charactersEqual = original.charAt(i) == answer.charAt(i - (original.length() - answer.length()));

                    if (charactersEqual) {
                        StyleConstants.setForeground(set, Color.GRAY);
                        doc.setCharacterAttributes(i, 1, set, true);
                    }
                }

                j--;
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
