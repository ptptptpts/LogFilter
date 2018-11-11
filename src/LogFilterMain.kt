import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.*
import java.awt.event.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class LogFilterMain : JFrame(), INotiEvent {
    internal val COMBO_ANDROID = "Android          "
    internal val COMBO_IOS = "ios"
    internal val COMBO_CUSTOM_COMMAND = "custom command"
    internal val IOS_DEFAULT_CMD = "adb logcat -v time "
    internal val IOS_SELECTED_CMD_FIRST = "adb -s "
    internal val IOS_SELECTED_CMD_LAST = " logcat -v time "
    //    final String              ANDROID_DEFAULT_CMD        = "logcat -v time ";
    //    final String              ANDROID_THREAD_CMD         = "logcat -v threadtime ";
    //    final String              ANDROID_EVENT_CMD          = "logcat -b events -v time ";
    //    final String              ANDROID_RADIO_CMD          = "logcat -b radio -v time          ";
    //    final String              ANDROID_CUSTOM_CMD         = "logcat ";
    internal val ANDROID_DEFAULT_CMD_FIRST = "adb "
    internal val ANDROID_SELECTED_CMD_FIRST = "adb -s "
    //    final String              ANDROID_SELECTED_CMD_LAST  = " logcat -v time ";
    internal val DEVICES_CMD = arrayOf("adb devices", "", "")

    internal val L = SwingConstants.LEFT
    internal val C = SwingConstants.CENTER
    internal val R = SwingConstants.RIGHT

    internal lateinit var m_tpTab: JTabbedPane
    internal lateinit var m_tfStatus: JTextField
    internal lateinit var m_ipIndicator: IndicatorPanel
    internal lateinit var m_arTagInfo: ArrayList<TagInfo>
    internal lateinit var m_arLogInfoAll: ArrayList<LogInfo>
    internal lateinit var m_arLogInfoFiltered: ArrayList<LogInfo>
    internal lateinit var m_hmBookmarkAll: HashMap<Int, Int>
    internal lateinit var m_hmBookmarkFiltered: HashMap<Int, Int>
    internal lateinit var m_hmErrorAll: HashMap<Int, Int>
    internal lateinit var m_hmErrorFiltered: HashMap<Int, Int>
    internal lateinit var m_iLogParser: ILogParser
    internal lateinit var m_tbLogTable: LogTable
    //    TagTable                    m_tbTagTable;
    internal lateinit var m_scrollVBar: JScrollPane
    //    JScrollPane                 m_scrollVTagBar;
    internal lateinit var m_tmLogTableModel: LogFilterTableModel
    //    TagFilterTableModel         m_tmTagTableModel;
    internal var m_bUserFilter: Boolean = false

    //Word Filter, tag filter
    internal lateinit var m_tfHighlight: JTextField
    internal lateinit var m_tfFindWord: JTextField
    internal lateinit var m_tfRemoveWord: JTextField
    internal lateinit var m_tfShowTag: JTextField
    internal lateinit var m_tfRemoveTag: JTextField
    internal lateinit var m_tfShowPid: JTextField
    internal lateinit var m_tfShowTid: JTextField

    //Device
    internal lateinit var m_btnDevice: JButton
    internal lateinit var m_lDeviceList: JList<*>
    internal lateinit var m_comboDeviceCmd: JComboBox<String>
    internal lateinit var m_comboCmd: JComboBox<String>
    internal lateinit var m_btnSetFont: JButton

    //Log filter enable/disable
    internal lateinit var m_chkEnableFind: JCheckBox
    internal lateinit var m_chkEnableRemove: JCheckBox
    internal lateinit var m_chkEnableShowTag: JCheckBox
    internal lateinit var m_chkEnableRemoveTag: JCheckBox
    internal lateinit var m_chkEnableShowPid: JCheckBox
    internal lateinit var m_chkEnableShowTid: JCheckBox
    internal lateinit var m_chkEnableHighlight: JCheckBox

    //Log filter
    internal lateinit var m_chkVerbose: JCheckBox
    internal lateinit var m_chkDebug: JCheckBox
    internal lateinit var m_chkInfo: JCheckBox
    internal lateinit var m_chkWarn: JCheckBox
    internal lateinit var m_chkError: JCheckBox
    internal lateinit var m_chkFatal: JCheckBox

    //Show column
    internal lateinit var m_chkClmBookmark: JCheckBox
    internal lateinit var m_chkClmLine: JCheckBox
    internal lateinit var m_chkClmDate: JCheckBox
    internal lateinit var m_chkClmTime: JCheckBox
    internal lateinit var m_chkClmLogLV: JCheckBox
    internal lateinit var m_chkClmPid: JCheckBox
    internal lateinit var m_chkClmThread: JCheckBox
    internal lateinit var m_chkClmTag: JCheckBox
    internal lateinit var m_chkClmMessage: JCheckBox

    internal lateinit var m_tfFontSize: JTextField
    //    JTextField                  m_tfProcessCmd;
    internal lateinit var m_comboEncode: JComboBox<String>
    internal lateinit var m_jcFontType: JComboBox<String>
    internal lateinit var m_btnRun: JButton
    internal lateinit var m_btnClear: JButton
    internal lateinit var m_tbtnPause: JToggleButton
    internal lateinit var m_btnStop: JButton

    internal lateinit var m_strLogFileName: String
    internal lateinit var m_strSelectedDevice: String
    //    String                      m_strProcessCmd;
    internal var m_Process: Process? = null
    internal var m_thProcess: Thread? = null
    internal var m_thWatchFile: Thread? = null
    internal var m_thFilterParse: Thread? = null
    internal var m_bPauseADB: Boolean = false

    internal lateinit var FILE_LOCK: Any
    internal lateinit var FILTER_LOCK: Any
    @Volatile
    internal var m_nChangedFilter: Int = 0
    internal var m_nFilterLogLV: Int = 0
    internal var m_nWinWidth = DEFAULT_WIDTH
    internal var m_nWinHeight = DEFAULT_HEIGHT
    internal var m_nLastWidth: Int = 0
    internal var m_nLastHeight: Int = 0
    internal var m_nWindState: Int = 0
    //    String                    m_strLastDir;

    internal var m_alButtonListener: ActionListener = ActionListener { e ->
        if (e.source == m_btnDevice)
            setDeviceList()
        else if (e.source == m_btnSetFont) {
            m_tbLogTable.setFontSize(Integer.parseInt(m_tfFontSize.text))
            updateTable(-1, false)
        } else if (e.source == m_btnRun) {
            startProcess()
        } else if (e.source == m_btnStop) {
            stopProcess()
        } else if (e.source == m_btnClear) {
            val bBackup = m_bPauseADB
            m_bPauseADB = true
            clearData()
            updateTable(-1, false)
            m_bPauseADB = bBackup
        } else if (e.source == m_tbtnPause)
            pauseProcess()
        else if (e.source == m_jcFontType) {
            T.d("font = " + m_tbLogTable.font)

            m_tbLogTable.font = Font(m_jcFontType.selectedItem as String, Font.PLAIN, 12)
            m_tbLogTable.setFontSize(Integer.parseInt(m_tfFontSize.text))
        }
    }

    internal val INI_FILE = "LogFilter.ini"
    internal val INI_FILE_CMD = "LogFilterCmd.ini"
    internal val INI_FILE_COLOR = "LogFilterColor.ini"
    internal val INI_LAST_DIR = "LAST_DIR"
    internal val INI_CMD_COUNT = "CMD_COUNT"
    internal val INI_CMD = "CMD_"
    internal val INI_FONT_TYPE = "FONT_TYPE"
    internal val INI_WORD_FIND = "WORD_FIND"
    internal val INI_WORD_REMOVE = "WORD_REMOVE"
    internal val INI_TAG_SHOW = "TAG_SHOW"
    internal val INI_TAG_REMOVE = "TAG_REMOVE"
    internal val INI_HIGHLIGHT = "HIGHLIGHT"
    internal val INI_PID_SHOW = "PID_SHOW"
    internal val INI_TID_SHOW = "TID_SHOW"
    internal val INI_COLOR_0 = "INI_COLOR_0"
    internal val INI_COLOR_1 = "INI_COLOR_1"
    internal val INI_COLOR_2 = "INI_COLOR_2"
    internal val INI_COLOR_3 = "INI_COLOR_3(E)"
    internal val INI_COLOR_4 = "INI_COLOR_4(W)"
    internal val INI_COLOR_5 = "INI_COLOR_5"
    internal val INI_COLOR_6 = "INI_COLOR_6(I)"
    internal val INI_COLOR_7 = "INI_COLOR_7(D)"
    internal val INI_COLOR_8 = "INI_COLOR_8(F)"
    internal val INI_HIGILIGHT_COUNT = "INI_HIGILIGHT_COUNT"
    internal val INI_HIGILIGHT_ = "INI_HIGILIGHT_"
    internal val INI_WIDTH = "INI_WIDTH"
    internal val INI_HEIGHT = "INI_HEIGHT"
    internal val INI_WINDOW_STATE = "INI_WINDOW_STATE"

    internal val INI_COMUMN = "INI_COMUMN_"

    internal//        //iookill
    //        m_tmTagTableModel = new TagFilterTableModel();
    //        m_tmTagTableModel.setData(m_arTagInfo);
    //        m_tbTagTable = new TagTable(m_tmTagTableModel, this);
    //
    //        m_scrollVTagBar = new JScrollPane(m_tbTagTable);
    //        m_scrollVTagBar.setPreferredSize(new Dimension(182,50));
    //        // show list
    //        jp.add(m_scrollVTagBar, BorderLayout.WEST);
    val bookmarkPanel: Component
        get() {
            val jp = JPanel()
            jp.layout = BorderLayout()

            m_ipIndicator = IndicatorPanel(this)
            m_ipIndicator.setData(m_arLogInfoAll, m_hmBookmarkAll, m_hmErrorAll)
            jp.add(m_ipIndicator, BorderLayout.CENTER)
            return jp
        }

    internal val filterPanel: Component
        get() {
            m_chkEnableFind = JCheckBox()
            m_chkEnableRemove = JCheckBox()
            m_chkEnableShowTag = JCheckBox()
            m_chkEnableRemoveTag = JCheckBox()
            m_chkEnableShowPid = JCheckBox()
            m_chkEnableShowTid = JCheckBox()
            m_chkEnableFind.isSelected = true
            m_chkEnableRemove.isSelected = true
            m_chkEnableShowTag.isSelected = true
            m_chkEnableRemoveTag.isSelected = true
            m_chkEnableShowPid.isSelected = true
            m_chkEnableShowTid.isSelected = true

            m_tfFindWord = JTextField()
            m_tfRemoveWord = JTextField()
            m_tfShowTag = JTextField()
            m_tfRemoveTag = JTextField()
            m_tfShowPid = JTextField()
            m_tfShowTid = JTextField()

            val jpMain = JPanel(BorderLayout())

            val jpWordFilter = JPanel(BorderLayout())
            jpWordFilter.border = BorderFactory.createTitledBorder("Word filter")

            val jpFind = JPanel(BorderLayout())
            val find = JLabel()
            find.text = "        Find : "
            jpFind.add(find, BorderLayout.WEST)
            jpFind.add(m_tfFindWord, BorderLayout.CENTER)
            jpFind.add(m_chkEnableFind, BorderLayout.EAST)

            val jpRemove = JPanel(BorderLayout())
            val remove = JLabel()
            remove.text = "Remove : "
            jpRemove.add(remove, BorderLayout.WEST)
            jpRemove.add(m_tfRemoveWord, BorderLayout.CENTER)
            jpRemove.add(m_chkEnableRemove, BorderLayout.EAST)

            jpWordFilter.add(jpFind, BorderLayout.NORTH)
            jpWordFilter.add(jpRemove)

            jpMain.add(jpWordFilter, BorderLayout.NORTH)

            val jpTagFilter = JPanel(GridLayout(4, 1))
            jpTagFilter.border = BorderFactory.createTitledBorder("Tag filter")

            val jpPid = JPanel(BorderLayout())
            val pid = JLabel()
            pid.text = "         Pid : "
            jpPid.add(pid, BorderLayout.WEST)
            jpPid.add(m_tfShowPid, BorderLayout.CENTER)
            jpPid.add(m_chkEnableShowPid, BorderLayout.EAST)

            val jpTid = JPanel(BorderLayout())
            val tid = JLabel()
            tid.text = "         Tid : "
            jpTid.add(tid, BorderLayout.WEST)
            jpTid.add(m_tfShowTid, BorderLayout.CENTER)
            jpTid.add(m_chkEnableShowTid, BorderLayout.EAST)

            val jpShow = JPanel(BorderLayout())
            val show = JLabel()
            show.text = "     Show : "
            jpShow.add(show, BorderLayout.WEST)
            jpShow.add(m_tfShowTag, BorderLayout.CENTER)
            jpShow.add(m_chkEnableShowTag, BorderLayout.EAST)

            val jpRemoveTag = JPanel(BorderLayout())
            val removeTag = JLabel()
            removeTag.text = "Remove : "
            jpRemoveTag.add(removeTag, BorderLayout.WEST)
            jpRemoveTag.add(m_tfRemoveTag, BorderLayout.CENTER)
            jpRemoveTag.add(m_chkEnableRemoveTag, BorderLayout.EAST)

            jpTagFilter.add(jpPid)
            jpTagFilter.add(jpTid)
            jpTagFilter.add(jpShow)
            jpTagFilter.add(jpRemoveTag)

            jpMain.add(jpTagFilter, BorderLayout.CENTER)

            return jpMain
        }

    internal val highlightPanel: Component
        get() {
            m_chkEnableHighlight = JCheckBox()
            m_chkEnableHighlight.isSelected = true

            m_tfHighlight = JTextField()

            val jpMain = JPanel(BorderLayout())
            jpMain.border = BorderFactory.createTitledBorder("Highlight")

            val jlHighlight = JLabel()
            jlHighlight.text = "Highlight : "
            jpMain.add(jlHighlight, BorderLayout.WEST)
            jpMain.add(m_tfHighlight)
            jpMain.add(m_chkEnableHighlight, BorderLayout.EAST)

            return jpMain
        }

    internal val checkPanel: Component
        get() {
            m_chkVerbose = JCheckBox()
            m_chkDebug = JCheckBox()
            m_chkInfo = JCheckBox()
            m_chkWarn = JCheckBox()
            m_chkError = JCheckBox()
            m_chkFatal = JCheckBox()

            m_chkClmBookmark = JCheckBox()
            m_chkClmLine = JCheckBox()
            m_chkClmDate = JCheckBox()
            m_chkClmTime = JCheckBox()
            m_chkClmLogLV = JCheckBox()
            m_chkClmPid = JCheckBox()
            m_chkClmThread = JCheckBox()
            m_chkClmTag = JCheckBox()
            m_chkClmMessage = JCheckBox()

            val jpMain = JPanel(BorderLayout())

            val jpLogFilter = JPanel()
            jpLogFilter.layout = FlowLayout(FlowLayout.CENTER, 0, 0)
            jpLogFilter.border = BorderFactory.createTitledBorder("Log filter")
            m_chkVerbose.text = "Verbose"
            m_chkVerbose.isSelected = true
            m_chkDebug.text = "Debug"
            m_chkDebug.isSelected = true
            m_chkInfo.text = "Info"
            m_chkInfo.isSelected = true
            m_chkWarn.text = "Warn"
            m_chkWarn.isSelected = true
            m_chkError.text = "Error"
            m_chkError.isSelected = true
            m_chkFatal.text = "Fatal"
            m_chkFatal.isSelected = true
            jpLogFilter.add(m_chkVerbose)
            jpLogFilter.add(m_chkDebug)
            jpLogFilter.add(m_chkInfo)
            jpLogFilter.add(m_chkWarn)
            jpLogFilter.add(m_chkError)
            jpLogFilter.add(m_chkFatal)

            jpMain.add(jpLogFilter, BorderLayout.NORTH)

            val jpShowColumn = JPanel()
            jpShowColumn.layout = FlowLayout(FlowLayout.CENTER, 0, 0)
            jpShowColumn.border = BorderFactory.createTitledBorder("Show column")
            m_chkClmBookmark.text = "Mark"
            m_chkClmBookmark.toolTipText = "Bookmark"
            m_chkClmLine.text = "Line"
            m_chkClmLine.isSelected = true
            m_chkClmDate.text = "Date"
            m_chkClmDate.isSelected = true
            m_chkClmTime.text = "Time"
            m_chkClmTime.isSelected = true
            m_chkClmLogLV.text = "LogLV"
            m_chkClmLogLV.isSelected = true
            m_chkClmPid.text = "Pid"
            m_chkClmPid.isSelected = true
            m_chkClmThread.text = "Thread"
            m_chkClmThread.isSelected = true
            m_chkClmTag.text = "Tag"
            m_chkClmTag.isSelected = true
            m_chkClmMessage.text = "Msg"
            m_chkClmMessage.isSelected = true
            jpShowColumn.add(m_chkClmBookmark)
            jpShowColumn.add(m_chkClmLine)
            jpShowColumn.add(m_chkClmDate)
            jpShowColumn.add(m_chkClmTime)
            jpShowColumn.add(m_chkClmLogLV)
            jpShowColumn.add(m_chkClmPid)
            jpShowColumn.add(m_chkClmThread)
            jpShowColumn.add(m_chkClmTag)
            jpShowColumn.add(m_chkClmMessage)

            jpMain.add(jpShowColumn, BorderLayout.CENTER)
            jpMain.add(highlightPanel, BorderLayout.SOUTH)
            return jpMain
        }

    internal val optionFilter: Component
        get() {
            val optionFilter = JPanel(BorderLayout())

            optionFilter.add(cmdPanel, BorderLayout.WEST)
            optionFilter.add(checkPanel, BorderLayout.EAST)
            optionFilter.add(filterPanel, BorderLayout.CENTER)

            return optionFilter
        }

    internal//        m_comboCmd.setMaximumSize( m_comboCmd.getPreferredSize()  );
    //        m_comboCmd.setSize( 20000, m_comboCmd.getHeight() );
    //        m_comboCmd.addItem(ANDROID_THREAD_CMD);
    //        m_comboCmd.addItem(ANDROID_DEFAULT_CMD);
    //        m_comboCmd.addItem(ANDROID_RADIO_CMD);
    //        m_comboCmd.addItem(ANDROID_EVENT_CMD);
    //        m_comboCmd.addItem(ANDROID_CUSTOM_CMD);
    //        m_comboCmd.addItemListener(new ItemListener()
    //        {
    //            public void itemStateChanged(ItemEvent e)
    //            {
    //                if(e.getStateChange() != ItemEvent.SELECTED) return;
    //
    //                if (e.getItem().equals(ANDROID_CUSTOM_CMD)) {
    //                    m_comboCmd.setEditable(true);
    //                } else {
    //                    m_comboCmd.setEditable(false);
    //                }
    ////                setProcessCmd(m_comboDeviceCmd.getSelectedIndex(), m_strSelectedDevice);
    //            }
    //        });
    val optionMenu: Component
        get() {
            val optionMenu = JPanel(BorderLayout())
            val optionWest = JPanel()

            val jlFontType = JLabel("Font Type : ")
            m_jcFontType = JComboBox()
            val fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames
            m_jcFontType.addItem("Dialog")
            for (i in fonts.indices) {
                m_jcFontType.addItem(fonts[i])
            }
            m_jcFontType.addActionListener(m_alButtonListener)


            val jlFont = JLabel("Font Size : ")
            m_tfFontSize = JTextField(2)
            m_tfFontSize.horizontalAlignment = SwingConstants.RIGHT
            m_tfFontSize.text = "12"

            m_btnSetFont = JButton("OK")
            m_btnSetFont.margin = Insets(0, 0, 0, 0)
            m_btnSetFont.addActionListener(m_alButtonListener)

            val jlEncode = JLabel("Text Encode : ")
            m_comboEncode = JComboBox()
            m_comboEncode.addItem("UTF-8")
            m_comboEncode.addItem("Local")

            val jlGoto = JLabel("Goto : ")
            val tfGoto = JTextField(6)
            tfGoto.horizontalAlignment = SwingConstants.RIGHT
            tfGoto.addCaretListener {
                try {
                    val nIndex = Integer.parseInt(tfGoto.text) - 1
                    m_tbLogTable.showRow(nIndex, false)
                } catch (err: Exception) {
                }
            }

            val jlProcessCmd = JLabel("Cmd : ")
            m_comboCmd = JComboBox()
            m_comboCmd.preferredSize = Dimension(180, 25)

            m_btnClear = JButton("Clear")
            m_btnClear.margin = Insets(0, 0, 0, 0)
            m_btnClear.isEnabled = false
            m_btnRun = JButton("Run")
            m_btnRun.margin = Insets(0, 0, 0, 0)

            m_tbtnPause = JToggleButton("Pause")
            m_tbtnPause.margin = Insets(0, 0, 0, 0)
            m_tbtnPause.isEnabled = false
            m_btnStop = JButton("Stop")
            m_btnStop.margin = Insets(0, 0, 0, 0)
            m_btnStop.isEnabled = false
            m_btnRun.addActionListener(m_alButtonListener)
            m_btnStop.addActionListener(m_alButtonListener)
            m_btnClear.addActionListener(m_alButtonListener)
            m_tbtnPause.addActionListener(m_alButtonListener)

            optionWest.add(jlFontType)
            optionWest.add(m_jcFontType)
            optionWest.add(jlFont)
            optionWest.add(m_tfFontSize)
            optionWest.add(m_btnSetFont)
            optionWest.add(jlEncode)
            optionWest.add(m_comboEncode)
            optionWest.add(jlGoto)
            optionWest.add(tfGoto)
            optionWest.add(jlProcessCmd)
            optionWest.add(m_comboCmd)
            optionWest.add(m_btnClear)
            optionWest.add(m_btnRun)
            optionWest.add(m_tbtnPause)
            optionWest.add(m_btnStop)

            optionMenu.add(optionWest, BorderLayout.WEST)
            return optionMenu
        }

    internal val optionPanel: Component
        get() {
            val optionMain = JPanel(BorderLayout())

            optionMain.add(optionFilter, BorderLayout.CENTER)
            optionMain.add(optionMenu, BorderLayout.SOUTH)

            return optionMain
        }

    internal val statusPanel: Component
        get() {
            m_tfStatus = JTextField("ready")
            m_tfStatus.isEditable = false
            return m_tfStatus
        }

    internal val tabPanel: Component
        get() {
            m_tpTab = JTabbedPane()
            m_tmLogTableModel = LogFilterTableModel()
            m_tmLogTableModel.setData(m_arLogInfoAll)
            m_tbLogTable = LogTable(m_tmLogTableModel, this)
            m_iLogParser = LogCatParser()
            m_tbLogTable.setLogParser(m_iLogParser)

            m_scrollVBar = JScrollPane(m_tbLogTable)

            m_tpTab.addTab("Log", m_scrollVBar)

            return m_scrollVBar
        }

    internal//            return ANDROID_DEFAULT_CMD_FIRST + m_comboCmd.getSelectedItem() + makeFilename();
    val processCmd: String
        get() = if (m_lDeviceList.selectedIndex < 0)
            ANDROID_DEFAULT_CMD_FIRST + m_comboCmd.selectedItem!!
        else
            ANDROID_SELECTED_CMD_FIRST + m_strSelectedDevice + m_comboCmd.selectedItem

    internal//        jpOptionDevice.setPreferredSize(new Dimension(200, 100));
    //        m_comboDeviceCmd.addItem(COMBO_IOS);
    //        m_comboDeviceCmd.addItem(CUSTOM_COMMAND);
    val cmdPanel: Component
        get() {
            val jpOptionDevice = JPanel()
            jpOptionDevice.border = BorderFactory.createTitledBorder("Device select")
            jpOptionDevice.layout = BorderLayout()

            val jpCmd = JPanel()
            m_comboDeviceCmd = JComboBox()
            m_comboDeviceCmd.addItem(COMBO_ANDROID)
            m_comboDeviceCmd.addItemListener(ItemListener { e ->
                if (e.stateChange != ItemEvent.SELECTED) return@ItemListener

                val listModel = m_lDeviceList.model as DefaultListModel<*>
                listModel.clear()
                if (e.item == COMBO_CUSTOM_COMMAND) {
                    m_comboDeviceCmd.setEditable(true)
                } else {
                    m_comboDeviceCmd.setEditable(false)
                }
                setProcessCmd(m_comboDeviceCmd.selectedIndex, m_strSelectedDevice)
            })

            val listModel = DefaultListModel<String>()
            m_btnDevice = JButton("OK")
            m_btnDevice.margin = Insets(0, 0, 0, 0)
            m_btnDevice.addActionListener(m_alButtonListener)

            jpCmd.add(m_comboDeviceCmd)
            jpCmd.add(m_btnDevice)

            jpOptionDevice.add(jpCmd, BorderLayout.NORTH)

            m_lDeviceList = JList(listModel)
            val vbar = JScrollPane(m_lDeviceList)
            vbar.preferredSize = Dimension(100, 50)
            m_lDeviceList.selectionMode = ListSelectionModel.SINGLE_SELECTION
            m_lDeviceList.addListSelectionListener { e ->
                val deviceList = e.source as JList<*>
                val selectedItem = deviceList.selectedValue
                m_strSelectedDevice = ""
                if (selectedItem != null) {
                    m_strSelectedDevice = selectedItem.toString()
                    m_strSelectedDevice = m_strSelectedDevice.replace("\t", " ").replace("device", "").replace("offline", "")
                    setProcessCmd(m_comboDeviceCmd.selectedIndex, m_strSelectedDevice)
                }
            }
            jpOptionDevice.add(vbar)

            return jpOptionDevice
        }

    internal var m_dlFilterListener: DocumentListener = object : DocumentListener {
        override fun changedUpdate(arg0: DocumentEvent) {
            try {
                if (arg0.document == m_tfFindWord.document && m_chkEnableFind.isSelected)
                    m_tbLogTable.setFilterFind(arg0.document.getText(0, arg0.document.length))
                else if (arg0.document == m_tfRemoveWord.document && m_chkEnableRemove.isSelected)
                    m_tbLogTable.SetFilterRemove(arg0.document.getText(0, arg0.document.length))
                else if (arg0.document == m_tfShowPid.document && m_chkEnableShowPid.isSelected)
                    m_tbLogTable.SetFilterShowPid(arg0.document.getText(0, arg0.document.length))
                else if (arg0.document == m_tfShowTid.document && m_chkEnableShowTid.isSelected)
                    m_tbLogTable.SetFilterShowTid(arg0.document.getText(0, arg0.document.length))
                else if (arg0.document == m_tfShowTag.document && m_chkEnableShowTag.isSelected)
                    m_tbLogTable.SetFilterShowTag(arg0.document.getText(0, arg0.document.length))
                else if (arg0.document == m_tfRemoveTag.document && m_chkEnableRemoveTag.isSelected)
                    m_tbLogTable.SetFilterRemoveTag(arg0.document.getText(0, arg0.document.length))
                else if (arg0.document == m_tfHighlight.document && m_chkEnableHighlight.isSelected)
                    m_tbLogTable.SetHighlight(arg0.document.getText(0, arg0.document.length))
                m_nChangedFilter = STATUS_CHANGE
                runFilter()
            } catch (e: Exception) {
                T.e(e)
            }

        }

        override fun insertUpdate(arg0: DocumentEvent) {
            try {
                if (arg0.document == m_tfFindWord.document && m_chkEnableFind.isSelected)
                    m_tbLogTable.setFilterFind(arg0.document.getText(0, arg0.document.length))
                else if (arg0.document == m_tfRemoveWord.document && m_chkEnableRemove.isSelected)
                    m_tbLogTable.SetFilterRemove(arg0.document.getText(0, arg0.document.length))
                else if (arg0.document == m_tfShowPid.document && m_chkEnableShowPid.isSelected)
                    m_tbLogTable.SetFilterShowPid(arg0.document.getText(0, arg0.document.length))
                else if (arg0.document == m_tfShowTid.document && m_chkEnableShowTid.isSelected)
                    m_tbLogTable.SetFilterShowTid(arg0.document.getText(0, arg0.document.length))
                else if (arg0.document == m_tfShowTag.document && m_chkEnableShowTag.isSelected)
                    m_tbLogTable.SetFilterShowTag(arg0.document.getText(0, arg0.document.length))
                else if (arg0.document == m_tfRemoveTag.document && m_chkEnableRemoveTag.isSelected)
                    m_tbLogTable.SetFilterRemoveTag(arg0.document.getText(0, arg0.document.length))
                else if (arg0.document == m_tfHighlight.document && m_chkEnableHighlight.isSelected)
                    m_tbLogTable.SetHighlight(arg0.document.getText(0, arg0.document.length))
                m_nChangedFilter = STATUS_CHANGE
                runFilter()
            } catch (e: Exception) {
                T.e(e)
            }

        }

        override fun removeUpdate(arg0: DocumentEvent) {
            try {
                if (arg0.document == m_tfFindWord.document && m_chkEnableFind.isSelected)
                    m_tbLogTable.setFilterFind(arg0.document.getText(0, arg0.document.length))
                else if (arg0.document == m_tfRemoveWord.document && m_chkEnableRemove.isSelected)
                    m_tbLogTable.SetFilterRemove(arg0.document.getText(0, arg0.document.length))
                else if (arg0.document == m_tfShowPid.document && m_chkEnableShowPid.isSelected)
                    m_tbLogTable.SetFilterShowPid(arg0.document.getText(0, arg0.document.length))
                else if (arg0.document == m_tfShowTid.document && m_chkEnableShowTid.isSelected)
                    m_tbLogTable.SetFilterShowTid(arg0.document.getText(0, arg0.document.length))
                else if (arg0.document == m_tfShowTag.document && m_chkEnableShowTag.isSelected)
                    m_tbLogTable.SetFilterShowTag(arg0.document.getText(0, arg0.document.length))
                else if (arg0.document == m_tfRemoveTag.document && m_chkEnableRemoveTag.isSelected)
                    m_tbLogTable.SetFilterRemoveTag(arg0.document.getText(0, arg0.document.length))
                else if (arg0.document == m_tfHighlight.document && m_chkEnableHighlight.isSelected)
                    m_tbLogTable.SetHighlight(arg0.document.getText(0, arg0.document.length))
                m_nChangedFilter = STATUS_CHANGE
                runFilter()
            } catch (e: Exception) {
                T.e(e)
            }

        }
    }

    internal var m_itemListener: ItemListener = ItemListener { itemEvent ->
        val check = itemEvent.source as JCheckBox

        if (check == m_chkVerbose)
            setLogLV(LogInfo.LOG_LV_VERBOSE, check.isSelected)
        else if (check == m_chkDebug)
            setLogLV(LogInfo.LOG_LV_DEBUG, check.isSelected)
        else if (check == m_chkInfo)
            setLogLV(LogInfo.LOG_LV_INFO, check.isSelected)
        else if (check == m_chkWarn)
            setLogLV(LogInfo.LOG_LV_WARN, check.isSelected)
        else if (check == m_chkError)
            setLogLV(LogInfo.LOG_LV_ERROR, check.isSelected)
        else if (check == m_chkFatal)
            setLogLV(LogInfo.LOG_LV_FATAL, check.isSelected)
        else if (check == m_chkClmBookmark)
            m_tbLogTable.showColumn(LogFilterTableModel.COMUMN_BOOKMARK, check.isSelected)
        else if (check == m_chkClmLine)
            m_tbLogTable.showColumn(LogFilterTableModel.COMUMN_LINE, check.isSelected)
        else if (check == m_chkClmDate)
            m_tbLogTable.showColumn(LogFilterTableModel.COMUMN_DATE, check.isSelected)
        else if (check == m_chkClmTime)
            m_tbLogTable.showColumn(LogFilterTableModel.COMUMN_TIME, check.isSelected)
        else if (check == m_chkClmLogLV)
            m_tbLogTable.showColumn(LogFilterTableModel.COMUMN_LOGLV, check.isSelected)
        else if (check == m_chkClmPid)
            m_tbLogTable.showColumn(LogFilterTableModel.COMUMN_PID, check.isSelected)
        else if (check == m_chkClmThread)
            m_tbLogTable.showColumn(LogFilterTableModel.COMUMN_THREAD, check.isSelected)
        else if (check == m_chkClmTag)
            m_tbLogTable.showColumn(LogFilterTableModel.COMUMN_TAG, check.isSelected)
        else if (check == m_chkClmMessage)
            m_tbLogTable.showColumn(LogFilterTableModel.COMUMN_MESSAGE, check.isSelected)
        else if (check == m_chkEnableFind
                || check == m_chkEnableRemove
                || check == m_chkEnableShowPid
                || check == m_chkEnableShowTid
                || check == m_chkEnableShowTag
                || check == m_chkEnableRemoveTag
                || check == m_chkEnableHighlight)
            useFilter(check)
    }

    internal fun makeFilename(): String {
        val now = Date()
        val format = SimpleDateFormat("yyyyMMdd_HHmmss")
        return "LogFilter_" + format.format(now) + ".txt"
    }

    internal fun exit() {
        if (m_Process != null) m_Process!!.destroy()
        if (m_thProcess != null) m_thProcess!!.interrupt()
        if (m_thWatchFile != null) m_thWatchFile!!.interrupt()
        if (m_thFilterParse != null) m_thFilterParse!!.interrupt()

        saveFilter()
        saveColor()
        System.exit(0)
    }

    /**
     * @throws HeadlessException
     */
    init {
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                exit()
            }
        })
        initValue()
        createComponent()

        val pane = contentPane
        pane.layout = BorderLayout()

        pane.add(optionPanel, BorderLayout.NORTH)
        pane.add(bookmarkPanel, BorderLayout.WEST)
        pane.add(statusPanel, BorderLayout.SOUTH)
        pane.add(tabPanel, BorderLayout.CENTER)

        setDnDListener()
        addChangeListener()
        startFilterParse()

        isVisible = true
        addDesc()
        loadFilter()
        loadColor()
        loadCmd()
        m_tbLogTable.setColumnWidth()

        //        if(m_nWindState == JFrame.MAXIMIZED_BOTH)
        //        else
        setSize(m_nWinWidth, m_nWinHeight)
        extendedState = m_nWindState
        minimumSize = Dimension(MIN_WIDTH, MIN_HEIGHT)
    }

    internal fun loadCmd() {
        try {
            val p = Properties()

            // ini ���� �б�
            p.load(FileInputStream(INI_FILE_CMD))

            T.d("p.getProperty(INI_CMD_COUNT) = " + p.getProperty(INI_CMD_COUNT))
            val nCount = Integer.parseInt(p.getProperty(INI_CMD_COUNT))
            T.d("nCount = $nCount")
            for (nIndex in 0 until nCount) {
                T.d("CMD = $INI_CMD$nIndex")
                m_comboCmd.addItem(p.getProperty(INI_CMD + nIndex))
            }
        } catch (e: Exception) {
            println(e)
        }

    }

    internal fun saveColor() {
        try {
            val p = Properties()

            p.setProperty(INI_COLOR_0, "0x" + Integer.toHexString(LogColor.COLOR_0).toUpperCase())
            p.setProperty(INI_COLOR_1, "0x" + Integer.toHexString(LogColor.COLOR_1).toUpperCase())
            p.setProperty(INI_COLOR_2, "0x" + Integer.toHexString(LogColor.COLOR_2).toUpperCase())
            p.setProperty(INI_COLOR_3, "0x" + Integer.toHexString(LogColor.COLOR_3).toUpperCase())
            p.setProperty(INI_COLOR_4, "0x" + Integer.toHexString(LogColor.COLOR_4).toUpperCase())
            p.setProperty(INI_COLOR_5, "0x" + Integer.toHexString(LogColor.COLOR_5).toUpperCase())
            p.setProperty(INI_COLOR_6, "0x" + Integer.toHexString(LogColor.COLOR_6).toUpperCase())
            p.setProperty(INI_COLOR_7, "0x" + Integer.toHexString(LogColor.COLOR_7).toUpperCase())
            p.setProperty(INI_COLOR_8, "0x" + Integer.toHexString(LogColor.COLOR_8).toUpperCase())

            if (LogColor.COLOR_HIGHLIGHT != null) {
                p.setProperty(INI_HIGILIGHT_COUNT, "" + LogColor.COLOR_HIGHLIGHT!!.size)
                for (nIndex in LogColor.COLOR_HIGHLIGHT!!.indices)
                    p.setProperty(INI_HIGILIGHT_ + nIndex, "0x" + LogColor.COLOR_HIGHLIGHT!![nIndex]!!.toUpperCase())
            }

            p.store(FileOutputStream(INI_FILE_COLOR), "done.")
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    internal fun loadColor() {
        try {
            val p = Properties()

            p.load(FileInputStream(INI_FILE_COLOR))

            LogColor.COLOR_0 = Integer.parseInt(p.getProperty(INI_COLOR_0).replace("0x", ""), 16)
            LogColor.COLOR_1 = Integer.parseInt(p.getProperty(INI_COLOR_1).replace("0x", ""), 16)
            LogColor.COLOR_2 = Integer.parseInt(p.getProperty(INI_COLOR_2).replace("0x", ""), 16)
            LogColor.COLOR_3 = Integer.parseInt(p.getProperty(INI_COLOR_3).replace("0x", ""), 16)
            LogColor.COLOR_ERROR = LogColor.COLOR_3
            LogColor.COLOR_4 = Integer.parseInt(p.getProperty(INI_COLOR_4).replace("0x", ""), 16)
            LogColor.COLOR_WARN = LogColor.COLOR_4
            LogColor.COLOR_5 = Integer.parseInt(p.getProperty(INI_COLOR_5).replace("0x", ""), 16)
            LogColor.COLOR_6 = Integer.parseInt(p.getProperty(INI_COLOR_6).replace("0x", ""), 16)
            LogColor.COLOR_INFO = LogColor.COLOR_6
            LogColor.COLOR_7 = Integer.parseInt(p.getProperty(INI_COLOR_7).replace("0x", ""), 16)
            LogColor.COLOR_DEBUG = LogColor.COLOR_7
            LogColor.COLOR_8 = Integer.parseInt(p.getProperty(INI_COLOR_8).replace("0x", ""), 16)
            LogColor.COLOR_FATAL = LogColor.COLOR_8

            val nCount = Integer.parseInt(p.getProperty(INI_HIGILIGHT_COUNT, "0"))
            if (nCount > 0) {
                LogColor.COLOR_HIGHLIGHT = arrayOfNulls(nCount)
                for (nIndex in 0 until nCount)
                    LogColor.COLOR_HIGHLIGHT!![nIndex] = p.getProperty(INI_HIGILIGHT_ + nIndex).replace("0x", "")
            } else {
                LogColor.COLOR_HIGHLIGHT = arrayOfNulls(1)
                LogColor.COLOR_HIGHLIGHT!![0] = "ffff"
            }
        } catch (e: Exception) {
            println(e)
        }

    }

    internal fun loadFilter() {
        try {
            val p = Properties()

            // ini ���� �б�
            p.load(FileInputStream(INI_FILE))

            // Key �� �б�
            val strFontType = p.getProperty(INI_FONT_TYPE)
            if (strFontType != null && strFontType.length > 0)
                m_jcFontType.selectedItem = p.getProperty(INI_FONT_TYPE)
            m_tfFindWord.text = p.getProperty(INI_WORD_FIND)
            m_tfRemoveWord.text = p.getProperty(INI_WORD_REMOVE)
            m_tfShowTag.text = p.getProperty(INI_TAG_SHOW)
            m_tfRemoveTag.text = p.getProperty(INI_TAG_REMOVE)
            m_tfShowPid.text = p.getProperty(INI_PID_SHOW)
            m_tfShowTid.text = p.getProperty(INI_TID_SHOW)
            m_tfHighlight.text = p.getProperty(INI_HIGHLIGHT)
            m_nWinWidth = Integer.parseInt(p.getProperty(INI_WIDTH))
            m_nWinHeight = Integer.parseInt(p.getProperty(INI_HEIGHT))
            m_nWindState = Integer.parseInt(p.getProperty(INI_WINDOW_STATE))

            for (nIndex in 0 until LogFilterTableModel.COMUMN_MAX) {
                LogFilterTableModel.setColumnWidth(nIndex, Integer.parseInt(p.getProperty(INI_COMUMN + nIndex)))
            }
        } catch (e: Exception) {
            println(e)
        }

    }

    internal fun addDesc(strMessage: String) {
        val logInfo = LogInfo()
        logInfo.m_strLine = "" + (m_arLogInfoAll.size + 1)
        logInfo.m_strMessage = strMessage
        m_arLogInfoAll.add(logInfo)
    }

    internal fun addDesc() {
        addDesc(VERSION)
        addDesc("")
        addDesc("Version 1.8 : java -jar LogFilter_xx.jar [filename] �߰�")
        addDesc("Version 1.7 : copy�� ���̴� column�� clipboard�� ����(Line ����)")
        addDesc("Version 1.6 : cmd�޺��ڽ� ���� ����")
        addDesc("Version 1.5 : Highlight color list�߰�()")
        addDesc("   - LogFilterColor.ini �� ī��Ʈ�� �� �־� �ֽø� �˴ϴ�.")
        addDesc("   - ex)INI_HIGILIGHT_COUNT=2")
        addDesc("   -    INI_COLOR_HIGILIGHT_0=0xFFFF")
        addDesc("   -    INI_COLOR_HIGILIGHT_1=0x00FF")
        addDesc("Version 1.4 : âũ�� ����")
        addDesc("Version 1.3 : recent file �� open�޴��߰�")
        addDesc("Version 1.2 : Tid ���� �߰�")
        addDesc("Version 1.1 : Level F �߰�")
        addDesc("Version 1.0 : Pid filter �߰�")
        addDesc("Version 0.9 : Font type �߰�")
        addDesc("Version 0.8 : ����üũ �ڽ� �߰�")
        addDesc("Version 0.7 : Ŀ�ηα� �Ľ�/LogFilter.ini�� �÷�����(0~7)")
        addDesc("Version 0.6 : ���� ��ҹ� ����")
        addDesc("Version 0.5 : ��ɾ� ini���Ϸ� ����")
        addDesc("Version 0.4 : add thread option, filter ����")
        addDesc("Version 0.3 : �ܸ� ���� �ȵǴ� ���� ����")
        addDesc("")
        addDesc("[Tag]")
        addDesc("Alt+L/R Click : Show/Remove tag")
        addDesc("")
        addDesc("[Bookmark]")
        addDesc("Ctrl+F2/double click: bookmark toggle")
        addDesc("F2 : pre bookmark")
        addDesc("F3 : next bookmark")
        addDesc("")
        addDesc("[Copy]")
        addDesc("Ctrl+c : row copy")
        addDesc("right click : cloumn copy")
        addDesc("")
        addDesc("[New version]")
        addDesc("http://blog.naver.com/iookill/140135139931")
    }

    /**
     * @param nIndex    ���� ����Ʈ�� �ε���
     * @param nLine     m_strLine
     * @param bBookmark
     */
    internal fun bookmarkItem(nIndex: Int, nLine: Int, bBookmark: Boolean) {
        synchronized(FILTER_LOCK) {
            val logInfo = m_arLogInfoAll[nLine]
            logInfo.m_bMarked = bBookmark
            m_arLogInfoAll[nLine] = logInfo

            if (logInfo.m_bMarked) {
                m_hmBookmarkAll[nLine] = nLine
                if (m_bUserFilter)
                    m_hmBookmarkFiltered[nLine] = nIndex
            } else {
                m_hmBookmarkAll.remove(nLine)
                if (m_bUserFilter)
                    m_hmBookmarkFiltered.remove(nLine)
            }
        }
        m_ipIndicator.repaint()
    }

    internal fun clearData() {
        m_arTagInfo.clear()
        m_arLogInfoAll.clear()
        m_arLogInfoFiltered.clear()
        m_hmBookmarkAll.clear()
        m_hmBookmarkFiltered.clear()
        m_hmErrorAll.clear()
        m_hmErrorFiltered.clear()
    }

    internal fun createComponent() {}

    internal fun saveFilter() {
        try {
            m_nWinWidth = m_nLastWidth
            m_nWinHeight = m_nLastHeight
            m_nWindState = extendedState
            T.d("m_nWindState = $m_nWindState")

            val p = Properties()
            //            p.setProperty( INI_LAST_DIR, m_strLastDir );
            p.setProperty(INI_FONT_TYPE, m_jcFontType.selectedItem as String)
            p.setProperty(INI_WORD_FIND, m_tfFindWord.text)
            p.setProperty(INI_WORD_REMOVE, m_tfRemoveWord.text)
            p.setProperty(INI_TAG_SHOW, m_tfShowTag.text)
            p.setProperty(INI_TAG_REMOVE, m_tfRemoveTag.text)
            p.setProperty(INI_PID_SHOW, m_tfShowPid.text)
            p.setProperty(INI_TID_SHOW, m_tfShowTid.text)
            p.setProperty(INI_HIGHLIGHT, m_tfHighlight.text)
            p.setProperty(INI_WIDTH, "" + m_nWinWidth)
            p.setProperty(INI_HEIGHT, "" + m_nWinHeight)
            p.setProperty(INI_WINDOW_STATE, "" + m_nWindState)

            for (nIndex in 0 until LogFilterTableModel.COMUMN_MAX) {
                p.setProperty(INI_COMUMN + nIndex, "" + m_tbLogTable.getColumnWidth(nIndex))
            }
            p.store(FileOutputStream(INI_FILE), "done.")
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    internal fun addTagList(strTag: String) {
        //        for(TagInfo tagInfo : m_arTagInfo)
        //            if(tagInfo.m_strTag.equals(strTag))
        //                return;
        //        String strRemoveFilter = m_tbLogTable.GetFilterRemoveTag();
        //        String strShowFilter = m_tbLogTable.GetFilterShowTag();
        //        TagInfo tagInfo = new TagInfo();
        //        tagInfo.m_strTag = strTag;
        //        if(strRemoveFilter.contains(strTag))
        //            tagInfo.m_bRemove = true;
        //        if(strShowFilter.contains(strTag))
        //            tagInfo.m_bShow = true;
        //        m_arTagInfo.add(tagInfo);
        //        m_tmTagTableModel.setData(m_arTagInfo);
        //
        //        m_tmTagTableModel.fireTableRowsUpdated(0, m_tmTagTableModel.getRowCount() - 1);
        //        m_scrollVTagBar.validate();
        //        m_tbTagTable.invalidate();
        //        m_tbTagTable.repaint();
        //            m_tbTagTable.changeSelection(0, 0, false, false);
    }

    internal fun addLogInfo(logInfo: LogInfo) {
        synchronized(FILTER_LOCK) {
            m_tbLogTable.setTagLength(logInfo.m_strTag.length)
            m_arLogInfoAll.add(logInfo)
            //            addTagList(logInfo.m_strTag);
            if (logInfo.m_strLogLV == "E" || logInfo.m_strLogLV == "ERROR")
                m_hmErrorAll[Integer.parseInt(logInfo.m_strLine) - 1] = Integer.parseInt(logInfo.m_strLine) - 1

            if (m_bUserFilter) {
                if (m_ipIndicator.m_chBookmark.isSelected || m_ipIndicator.m_chError.isSelected) {
                    var bAddFilteredArray = false
                    if (logInfo.m_bMarked && m_ipIndicator.m_chBookmark.isSelected) {
                        bAddFilteredArray = true
                        m_hmBookmarkFiltered[Integer.parseInt(logInfo.m_strLine) - 1] = m_arLogInfoFiltered.size
                        if (logInfo.m_strLogLV == "E" || logInfo.m_strLogLV == "ERROR")
                            m_hmErrorFiltered[Integer.parseInt(logInfo.m_strLine) - 1] = m_arLogInfoFiltered.size
                    }
                    if ((logInfo.m_strLogLV == "E" || logInfo.m_strLogLV == "ERROR") && m_ipIndicator.m_chError.isSelected) {
                        bAddFilteredArray = true
                        m_hmErrorFiltered[Integer.parseInt(logInfo.m_strLine) - 1] = m_arLogInfoFiltered.size
                        if (logInfo.m_bMarked)
                            m_hmBookmarkFiltered[Integer.parseInt(logInfo.m_strLine) - 1] = m_arLogInfoFiltered.size
                    }

                    if (bAddFilteredArray) m_arLogInfoFiltered.add(logInfo)
                } else if (checkLogLVFilter(logInfo)
                        && checkPidFilter(logInfo)
                        && checkTidFilter(logInfo)
                        && checkShowTagFilter(logInfo)
                        && checkRemoveTagFilter(logInfo)
                        && checkFindFilter(logInfo)
                        && checkRemoveFilter(logInfo)) {
                    m_arLogInfoFiltered.add(logInfo)
                    if (logInfo.m_bMarked)
                        m_hmBookmarkFiltered[Integer.parseInt(logInfo.m_strLine) - 1] = m_arLogInfoFiltered.size
                    if (logInfo.m_strLogLV === "E" || logInfo.m_strLogLV === "ERROR")
                        if (logInfo.m_strLogLV == "E" || logInfo.m_strLogLV == "ERROR")
                            m_hmErrorFiltered[Integer.parseInt(logInfo.m_strLine) - 1] = m_arLogInfoFiltered.size
                }
            }
        }
    }

    internal fun addChangeListener() {
        m_tfHighlight.document.addDocumentListener(m_dlFilterListener)
        m_tfFindWord.document.addDocumentListener(m_dlFilterListener)
        m_tfRemoveWord.document.addDocumentListener(m_dlFilterListener)
        m_tfShowTag.document.addDocumentListener(m_dlFilterListener)
        m_tfRemoveTag.document.addDocumentListener(m_dlFilterListener)
        m_tfShowPid.document.addDocumentListener(m_dlFilterListener)
        m_tfShowTid.document.addDocumentListener(m_dlFilterListener)

        m_chkEnableFind.addItemListener(m_itemListener)
        m_chkEnableRemove.addItemListener(m_itemListener)
        m_chkEnableShowPid.addItemListener(m_itemListener)
        m_chkEnableShowTid.addItemListener(m_itemListener)
        m_chkEnableShowTag.addItemListener(m_itemListener)
        m_chkEnableRemoveTag.addItemListener(m_itemListener)
        m_chkEnableHighlight.addItemListener(m_itemListener)

        m_chkVerbose.addItemListener(m_itemListener)
        m_chkDebug.addItemListener(m_itemListener)
        m_chkInfo.addItemListener(m_itemListener)
        m_chkWarn.addItemListener(m_itemListener)
        m_chkError.addItemListener(m_itemListener)
        m_chkFatal.addItemListener(m_itemListener)
        m_chkClmBookmark.addItemListener(m_itemListener)
        m_chkClmLine.addItemListener(m_itemListener)
        m_chkClmDate.addItemListener(m_itemListener)
        m_chkClmTime.addItemListener(m_itemListener)
        m_chkClmLogLV.addItemListener(m_itemListener)
        m_chkClmPid.addItemListener(m_itemListener)
        m_chkClmThread.addItemListener(m_itemListener)
        m_chkClmTag.addItemListener(m_itemListener)
        m_chkClmMessage.addItemListener(m_itemListener)


        m_scrollVBar.viewport.addChangeListener {
            //                m_ipIndicator.m_bDrawFull = false;
            if (extendedState != JFrame.MAXIMIZED_BOTH) {
                m_nLastWidth = width
                m_nLastHeight = height
            }
            m_ipIndicator.repaint()
        }
    }

    internal fun initValue() {
        m_bPauseADB = false
        FILE_LOCK = Any()
        FILTER_LOCK = Any()
        m_nChangedFilter = STATUS_READY
        m_nFilterLogLV = LogInfo.LOG_LV_ALL

        m_arTagInfo = ArrayList()
        m_arLogInfoAll = ArrayList()
        m_arLogInfoFiltered = ArrayList()
        m_hmBookmarkAll = HashMap()
        m_hmBookmarkFiltered = HashMap()
        m_hmErrorAll = HashMap()
        m_hmErrorFiltered = HashMap()

        m_strLogFileName = makeFilename()
        //        m_strProcessCmd     = ANDROID_DEFAULT_CMD + m_strLogFileName;
    }

    internal fun parseFile(file: File?) {
        if (file == null) {
            T.e("file == null")
            return
        }

        title = file.path
        Thread(Runnable {
            var fstream: FileInputStream? = null
            var `in`: DataInputStream? = null
            var br: BufferedReader? = null
            var nIndex = 1

            try {
                fstream = FileInputStream(file)
                `in` = DataInputStream(fstream)
                if (m_comboEncode.selectedItem == "UTF-8")
                    br = BufferedReader(InputStreamReader(`in`, "UTF-8"))
                else
                    br = BufferedReader(InputStreamReader(`in`))

                var strLine: String?

                setStatus("Parsing")
                clearData()
                m_tbLogTable.clearSelection()
                do {
                    strLine = br.readLine()
                    if (strLine == null) {
                        break
                    } else if ("" != strLine.trim { it <= ' ' }) {
                        val logInfo = m_iLogParser.parseLog(strLine)
                        logInfo.m_strLine = "" + nIndex++
                        addLogInfo(logInfo)
                    }
                } while (true)

                runFilter()
                setStatus("Parse complete")
            } catch (ioe: Exception) {
                T.e(ioe)
            }

            try {
                br?.close()
                `in`?.close()
                fstream?.close()
            } catch (e: Exception) {
                T.e(e)
            }
        }).start()
    }

    internal fun pauseProcess() {
        if (m_tbtnPause.isSelected) {
            m_bPauseADB = true
            m_tbtnPause.text = "Resume"
        } else {
            m_bPauseADB = false
            m_tbtnPause.text = "Pause"
        }
    }

    internal fun setBookmark(nLine: Int, strBookmark: String) {
        val logInfo = m_arLogInfoAll[nLine]
        logInfo.m_strBookmark = strBookmark
        m_arLogInfoAll[nLine] = logInfo
    }

    internal fun setDeviceList() {
        m_strSelectedDevice = ""

        val listModel = m_lDeviceList.model as DefaultListModel<String>
        try {
            listModel.clear()
            var s: String
            var strCommand = DEVICES_CMD[m_comboDeviceCmd.selectedIndex]
            if (m_comboDeviceCmd.selectedIndex == DEVICES_CUSTOM)
                strCommand = m_comboDeviceCmd.selectedItem as String
            val oProcess = Runtime.getRuntime().exec(strCommand)

            // �ܺ� ���α׷� ��� �б�
            val stdOut = BufferedReader(InputStreamReader(oProcess.inputStream))
            val stdError = BufferedReader(InputStreamReader(oProcess.errorStream))

            // "ǥ�� ���"�� "ǥ�� ���� ���"�� ���
            do {
                s = stdOut.readLine()
                if (s == null) {
                    break
                } else {
                    if (s != "List of devices attached ") {
                        s = s.replace("\t", " ")
                        s = s.replace("device", "")
                        listModel.addElement(s)
                    }
                }
            } while (true)

            do {
                s = stdError.readLine()
                if (s == null) {
                    break
                } else {
                    listModel.addElement(s)
                }
            } while (true)

            // �ܺ� ���α׷� ��ȯ�� ��� (�� �κ��� �ʼ��� �ƴ�)
            println("Exit Code: " + oProcess.exitValue())
        } catch (e: Exception) {
            T.e("e = $e")
            listModel.addElement(e.toString())
        }

    }

    fun setFindFocus() {
        m_tfFindWord.requestFocus()
    }

    internal fun setDnDListener() {

        DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, object : DropTargetListener {
            override fun dropActionChanged(dtde: DropTargetDragEvent) {}
            override fun dragOver(dtde: DropTargetDragEvent) {}
            override fun dragExit(dte: DropTargetEvent) {}
            override fun dragEnter(event: DropTargetDragEvent) {}

            override fun drop(event: DropTargetDropEvent) {
                try {
                    event.acceptDrop(DnDConstants.ACTION_COPY)
                    val t = event.transferable
                    val list = t.getTransferData(DataFlavor.javaFileListFlavor) as List<*>
                    val i = list.iterator()
                    if (i.hasNext()) {
                        val file = i.next() as File
                        title = file.path

                        stopProcess()
                        parseFile(file)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        })
    }

    internal fun setLogLV(nLogLV: Int, bChecked: Boolean) {
        if (bChecked)
            m_nFilterLogLV = m_nFilterLogLV or nLogLV
        else
            m_nFilterLogLV = m_nFilterLogLV and nLogLV.inv()
        m_nChangedFilter = STATUS_CHANGE
        runFilter()
    }

    internal fun useFilter(checkBox: JCheckBox) {
        if (checkBox == m_chkEnableFind)
            m_tbLogTable.setFilterFind(if (checkBox.isSelected) m_tfFindWord.text else "")
        if (checkBox == m_chkEnableRemove)
            m_tbLogTable.SetFilterRemove(if (checkBox.isSelected) m_tfRemoveWord.text else "")
        if (checkBox == m_chkEnableShowPid)
            m_tbLogTable.SetFilterShowPid(if (checkBox.isSelected) m_tfShowPid.text else "")
        if (checkBox == m_chkEnableShowTid)
            m_tbLogTable.SetFilterShowTid(if (checkBox.isSelected) m_tfShowTid.text else "")
        if (checkBox == m_chkEnableShowTag)
            m_tbLogTable.SetFilterShowTag(if (checkBox.isSelected) m_tfShowTag.text else "")
        if (checkBox == m_chkEnableRemoveTag)
            m_tbLogTable.SetFilterRemoveTag(if (checkBox.isSelected) m_tfRemoveTag.text else "")
        if (checkBox == m_chkEnableHighlight)
            m_tbLogTable.SetHighlight(if (checkBox.isSelected) m_tfHighlight.text else "")
        m_nChangedFilter = STATUS_CHANGE
        runFilter()
    }

    internal fun setProcessBtn(bStart: Boolean) {
        if (bStart) {
            m_btnRun.isEnabled = false
            m_btnStop.isEnabled = true
            m_btnClear.isEnabled = true
            m_tbtnPause.isEnabled = true
        } else {
            m_btnRun.isEnabled = true
            m_btnStop.isEnabled = false
            m_btnClear.isEnabled = false
            m_tbtnPause.isEnabled = false
            m_tbtnPause.isSelected = false
            m_tbtnPause.text = "Pause"
        }
    }

    internal fun setProcessCmd(nType: Int, strSelectedDevice: String?) {
        //        m_comboCmd.removeAllItems();

        m_strLogFileName = makeFilename()
        //        if(strSelectedDevice != null)
        //        {
        //            strSelectedDevice = strSelectedDevice.replace("\t", " ").replace("device", "").replace("offline", "");
        //            T.d("strSelectedDevice = " + strSelectedDevice);
        //        }

        if (nType == DEVICES_ANDROID) {
            if (strSelectedDevice != null && strSelectedDevice.length > 0) {
                //                m_comboCmd.addItem(ANDROID_SELECTED_CMD_FIRST + strSelectedDevice + ANDROID_SELECTED_CMD_LAST);
                //                m_strProcessCmd = ANDROID_SELECTED_CMD_FIRST + strSelectedDevice + ANDROID_SELECTED_CMD_LAST;
            } else {
                //                m_comboCmd.addItem(ANDROID_DEFAULT_CMD);
                //                m_strProcessCmd = ANDROID_DEFAULT_CMD;
            }
        } else if (nType == DEVICES_IOS) {
            if (strSelectedDevice != null && strSelectedDevice.length > 0) {
                //                m_comboCmd.addItem(ANDROID_SELECTED_CMD_FIRST + strSelectedDevice + ANDROID_SELECTED_CMD_LAST);
                //                m_strProcessCmd = IOS_SELECTED_CMD_FIRST + strSelectedDevice + IOS_SELECTED_CMD_LAST;
            } else {
                //              m_comboCmd.addItem(IOS_DEFAULT_CMD);
                //              m_strProcessCmd = IOS_DEFAULT_CMD;
            }
        } else {
            //            m_comboCmd.addItem(ANDROID_DEFAULT_CMD);
        }
    }

    internal fun setStatus(strText: String) {
        m_tfStatus.text = strText
    }

    internal fun stopProcess() {
        setProcessBtn(false)
        if (m_Process != null) m_Process!!.destroy()
        if (m_thProcess != null) m_thProcess!!.interrupt()
        if (m_thWatchFile != null) m_thWatchFile!!.interrupt()
        m_Process = null
        m_thProcess = null
        m_thWatchFile = null
        m_bPauseADB = false
    }

    internal fun startFileParse() {
        m_thWatchFile = Thread(Runnable {
            var fstream: FileInputStream? = null
            var `in`: DataInputStream? = null
            var br: BufferedReader? = null

            try {
                fstream = FileInputStream(m_strLogFileName)
                `in` = DataInputStream(fstream)
                if (m_comboEncode.selectedItem == "UTF-8")
                    br = BufferedReader(InputStreamReader(`in`, "UTF-8"))
                else
                    br = BufferedReader(InputStreamReader(`in`))

                title = m_strLogFileName

                m_arLogInfoAll.clear()
                m_arTagInfo.clear()

                var bEndLine: Boolean
                var nSelectedIndex: Int
                var nAddCount: Int
                var nPreRowCount = 0
                var nEndLine: Int

                while (true) {
                    Thread.sleep(50)

                    if (m_nChangedFilter == STATUS_CHANGE || m_nChangedFilter == STATUS_PARSING)
                        continue
                    if (m_bPauseADB) continue

                    bEndLine = false
                    nSelectedIndex = m_tbLogTable.selectedRow
                    nPreRowCount = m_tbLogTable.rowCount
                    nAddCount = 0

                    if (nSelectedIndex == -1 || nSelectedIndex == m_tbLogTable.rowCount - 1)
                        bEndLine = true

                    synchronized(FILE_LOCK) {
                        var nLine = m_arLogInfoAll.size + 1
                        var strLine = br.readLine()
                        while (!m_bPauseADB && (strLine) != null) {
                            if (strLine != null && "" != strLine.trim { it <= ' ' }) {
                                val logInfo = m_iLogParser.parseLog(strLine)
                                logInfo.m_strLine = "" + nLine++
                                addLogInfo(logInfo)
                                nAddCount++
                            }
                        }
                    }
                    if (nAddCount == 0) continue

                    synchronized(FILTER_LOCK) {
                        if (m_bUserFilter == false) {
                            m_tmLogTableModel.setData(m_arLogInfoAll)
                            m_ipIndicator.setData(m_arLogInfoAll, m_hmBookmarkAll, m_hmErrorAll)
                        } else {
                            m_tmLogTableModel.setData(m_arLogInfoFiltered)
                            m_ipIndicator.setData(m_arLogInfoFiltered, m_hmBookmarkFiltered, m_hmErrorFiltered)
                        }

                        nEndLine = m_tmLogTableModel.rowCount
                        if (nPreRowCount != nEndLine) {
                            if (bEndLine)
                                updateTable(nEndLine - 1, true)
                            else
                                updateTable(nSelectedIndex, false)
                        }
                    }
                }
            } catch (e: Exception) {
                T.e(e)
                e.printStackTrace()
            }

            try {
                br?.close()
                `in`?.close()
                fstream?.close()
            } catch (e: Exception) {
                T.e(e)
            }

            println("End m_thWatchFile thread")
            //                setTitle(LOGFILTER + " " + VERSION);
        })
        m_thWatchFile!!.start()
    }

    internal fun runFilter() {
        checkUseFilter()
        while (m_nChangedFilter == STATUS_PARSING)
            try {
                Thread.sleep(100)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        synchronized(FILTER_LOCK) {
            //FILTER_LOCK.notify()
        }
    }

    internal fun startFilterParse() {
        m_thFilterParse = Thread(Runnable {
            try {
                while (true) {
                    synchronized(FILTER_LOCK) {
                        m_nChangedFilter = STATUS_READY
                        // FILTER_LOCK.wait()

                        m_nChangedFilter = STATUS_PARSING

                        m_arLogInfoFiltered.clear()
                        m_hmBookmarkFiltered.clear()
                        m_hmErrorFiltered.clear()
                        m_tbLogTable.clearSelection()

                        if (m_bUserFilter == false) {
                            m_tmLogTableModel.setData(m_arLogInfoAll)
                            m_ipIndicator.setData(m_arLogInfoAll, m_hmBookmarkAll, m_hmErrorAll)
                            updateTable(m_arLogInfoAll.size - 1, true)
                            m_nChangedFilter = STATUS_READY
                            //continue
                        }
                        m_tmLogTableModel.setData(m_arLogInfoFiltered)
                        m_ipIndicator.setData(m_arLogInfoFiltered, m_hmBookmarkFiltered, m_hmErrorFiltered)
                        //                        updateTable(-1);
                        setStatus("Parsing")

                        val nRowCount = m_arLogInfoAll.size
                        var logInfo: LogInfo
                        var bAddFilteredArray: Boolean

                        for (nIndex in 0 until nRowCount) {
                            if (nIndex % 10000 == 0)
                                Thread.sleep(1)
                            if (m_nChangedFilter == STATUS_CHANGE) {
                                //                                    T.d("m_nChangedFilter == STATUS_CHANGE");
                                break
                            }
                            logInfo = m_arLogInfoAll[nIndex]

                            if (m_ipIndicator.m_chBookmark.isSelected || m_ipIndicator.m_chError.isSelected) {
                                bAddFilteredArray = false
                                if (logInfo.m_bMarked && m_ipIndicator.m_chBookmark.isSelected) {
                                    bAddFilteredArray = true
                                    m_hmBookmarkFiltered[Integer.parseInt(logInfo.m_strLine) - 1] = m_arLogInfoFiltered.size
                                    if (logInfo.m_strLogLV == "E" || logInfo.m_strLogLV == "ERROR")
                                        m_hmErrorFiltered[Integer.parseInt(logInfo.m_strLine) - 1] = m_arLogInfoFiltered.size
                                }
                                if ((logInfo.m_strLogLV == "E" || logInfo.m_strLogLV == "ERROR") && m_ipIndicator.m_chError.isSelected) {
                                    bAddFilteredArray = true
                                    m_hmErrorFiltered[Integer.parseInt(logInfo.m_strLine) - 1] = m_arLogInfoFiltered.size
                                    if (logInfo.m_bMarked)
                                        m_hmBookmarkFiltered[Integer.parseInt(logInfo.m_strLine) - 1] = m_arLogInfoFiltered.size
                                }

                                if (bAddFilteredArray) m_arLogInfoFiltered.add(logInfo)
                            } else if (checkLogLVFilter(logInfo)
                                    && checkPidFilter(logInfo)
                                    && checkTidFilter(logInfo)
                                    && checkShowTagFilter(logInfo)
                                    && checkRemoveTagFilter(logInfo)
                                    && checkFindFilter(logInfo)
                                    && checkRemoveFilter(logInfo)) {
                                m_arLogInfoFiltered.add(logInfo)
                                if (logInfo.m_bMarked)
                                    m_hmBookmarkFiltered[Integer.parseInt(logInfo.m_strLine) - 1] = m_arLogInfoFiltered.size
                                if (logInfo.m_strLogLV == "E" || logInfo.m_strLogLV == "ERROR")
                                    m_hmErrorFiltered[Integer.parseInt(logInfo.m_strLine) - 1] = m_arLogInfoFiltered.size
                            }
                        }
                        if (m_nChangedFilter == STATUS_PARSING) {
                            m_nChangedFilter = STATUS_READY
                            m_tmLogTableModel.setData(m_arLogInfoFiltered)
                            m_ipIndicator.setData(m_arLogInfoFiltered, m_hmBookmarkFiltered, m_hmErrorFiltered)
                            updateTable(m_arLogInfoFiltered.size - 1, true)
                            setStatus("Complete")
                        }
                    }
                }
            } catch (e: Exception) {
                T.e(e)
                e.printStackTrace()
            }

            println("End m_thFilterParse thread")
        })
        m_thFilterParse!!.start()
    }

    internal fun startProcess() {
        clearData()
        m_tbLogTable.clearSelection()
        m_thProcess = Thread(Runnable {
            try {
                var s: String?
                m_Process = null
                setProcessCmd(m_comboDeviceCmd.selectedIndex, m_strSelectedDevice)

                T.d("getProcessCmd() = $processCmd")
                m_Process = Runtime.getRuntime().exec(processCmd)
                val stdOut = BufferedReader(InputStreamReader(m_Process!!.inputStream, "UTF-8"))

                //                    BufferedWriter fileOut = new BufferedWriter(new FileWriter(m_strLogFileName));
                val fileOut = BufferedWriter(OutputStreamWriter(FileOutputStream(m_strLogFileName), "UTF-8"))

                startFileParse()

                do {
                    s = stdOut.readLine()
                    if (s == null) {
                        break
                    } else if ("" != s.trim { it <= ' ' }) {
                        synchronized(FILE_LOCK) {
                            fileOut.write(s)
                            fileOut.write("\r\n")
                            //                                fileOut.newLine();
                            fileOut.flush()
                        }
                    }
                } while (true)
                fileOut.close()
                //                    T.d("Exit Code: " + m_Process.exitValue());
            } catch (e: Exception) {
                T.e("e = $e")
            }

            stopProcess()
        })
        m_thProcess!!.start()
        setProcessBtn(true)
    }

    internal fun checkPidFilter(logInfo: LogInfo): Boolean {
        if (m_tbLogTable.GetFilterShowPid().length <= 0) return true

        val stk = StringTokenizer(m_tbLogTable.GetFilterShowPid(), "|", false)

        while (stk.hasMoreElements()) {
            if (logInfo.m_strPid.toLowerCase().contains(stk.nextToken().toLowerCase()))
                return true
        }

        return false
    }

    internal fun checkTidFilter(logInfo: LogInfo): Boolean {
        if (m_tbLogTable.GetFilterShowTid().length <= 0) return true

        val stk = StringTokenizer(m_tbLogTable.GetFilterShowTid(), "|", false)

        while (stk.hasMoreElements()) {
            if (logInfo.m_strThread.toLowerCase().contains(stk.nextToken().toLowerCase()))
                return true
        }

        return false
    }

    internal fun checkFindFilter(logInfo: LogInfo): Boolean {
        if (m_tbLogTable.GetFilterFind().length <= 0) return true

        val stk = StringTokenizer(m_tbLogTable.GetFilterFind(), "|", false)

        while (stk.hasMoreElements()) {
            if (logInfo.m_strMessage.toLowerCase().contains(stk.nextToken().toLowerCase()))
                return true
        }

        return false
    }

    internal fun checkRemoveFilter(logInfo: LogInfo): Boolean {
        if (m_tbLogTable.GetFilterRemove().length <= 0) return true

        val stk = StringTokenizer(m_tbLogTable.GetFilterRemove(), "|", false)

        while (stk.hasMoreElements()) {
            if (logInfo.m_strMessage.toLowerCase().contains(stk.nextToken().toLowerCase()))
                return false
        }

        return true
    }

    internal fun checkShowTagFilter(logInfo: LogInfo): Boolean {
        if (m_tbLogTable.GetFilterShowTag().length <= 0) return true

        val stk = StringTokenizer(m_tbLogTable.GetFilterShowTag(), "|", false)

        while (stk.hasMoreElements()) {
            if (logInfo.m_strTag.toLowerCase().contains(stk.nextToken().toLowerCase()))
                return true
        }

        return false
    }

    internal fun checkRemoveTagFilter(logInfo: LogInfo): Boolean {
        if (m_tbLogTable.GetFilterRemoveTag().length <= 0) return true

        val stk = StringTokenizer(m_tbLogTable.GetFilterRemoveTag(), "|", false)

        while (stk.hasMoreElements()) {
            if (logInfo.m_strTag.toLowerCase().contains(stk.nextToken().toLowerCase()))
                return false
        }

        return true
    }

    internal fun checkLogLVFilter(logInfo: LogInfo): Boolean {
        if (m_nFilterLogLV == LogInfo.LOG_LV_ALL)
            return true
        if (m_nFilterLogLV and LogInfo.LOG_LV_VERBOSE != 0 && (logInfo.m_strLogLV == "V" || logInfo.m_strLogLV == "VERBOSE"))
            return true
        if (m_nFilterLogLV and LogInfo.LOG_LV_DEBUG != 0 && (logInfo.m_strLogLV == "D" || logInfo.m_strLogLV == "DEBUG"))
            return true
        if (m_nFilterLogLV and LogInfo.LOG_LV_INFO != 0 && (logInfo.m_strLogLV == "I" || logInfo.m_strLogLV == "INFO"))
            return true
        if (m_nFilterLogLV and LogInfo.LOG_LV_WARN != 0 && (logInfo.m_strLogLV == "W" || logInfo.m_strLogLV == "WARN"))
            return true
        return if (m_nFilterLogLV and LogInfo.LOG_LV_ERROR != 0 && (logInfo.m_strLogLV == "E" || logInfo.m_strLogLV == "ERROR")) true else m_nFilterLogLV and LogInfo.LOG_LV_FATAL != 0 && (logInfo.m_strLogLV == "F" || logInfo.m_strLogLV == "FATAL")

    }

    internal fun checkUseFilter(): Boolean {
        m_bUserFilter = (m_ipIndicator.m_chBookmark.isSelected
                || m_ipIndicator.m_chError.isSelected
                || !checkLogLVFilter(LogInfo())
                || m_tbLogTable.GetFilterShowPid().length != 0 && m_chkEnableShowPid.isSelected
                || m_tbLogTable.GetFilterShowTid().length != 0 && m_chkEnableShowTid.isSelected
                || m_tbLogTable.GetFilterShowTag().length != 0 && m_chkEnableShowTag.isSelected
                || m_tbLogTable.GetFilterRemoveTag().length != 0 && m_chkEnableRemoveTag.isSelected
                || m_tbLogTable.GetFilterFind().length != 0 && m_chkEnableFind.isSelected
                || m_tbLogTable.GetFilterRemove().length != 0 && m_chkEnableRemove.isSelected)
        return m_bUserFilter
    }

    override fun notiEvent(param: INotiEvent.EventParam) {
        when (param.nEventId) {
            INotiEvent.EVENT_CLICK_BOOKMARK, INotiEvent.EVENT_CLICK_ERROR -> {
                m_nChangedFilter = STATUS_CHANGE
                runFilter()
            }
            INotiEvent.EVENT_CHANGE_FILTER_SHOW_TAG -> m_tfShowTag.text = m_tbLogTable.GetFilterShowTag()
            INotiEvent.EVENT_CHANGE_FILTER_REMOVE_TAG -> m_tfRemoveTag.text = m_tbLogTable.GetFilterRemoveTag()
        }
    }

    internal fun updateTable(nRow: Int, bMove: Boolean) {
        m_tmLogTableModel.fireTableRowsUpdated(0, m_tmLogTableModel.rowCount - 1)
        m_scrollVBar.validate()
        //        if(nRow >= 0)
        //            m_tbLogTable.changeSelection(nRow, 0, false, false);
        m_tbLogTable.invalidate()
        m_tbLogTable.repaint()
        if (nRow >= 0)
            m_tbLogTable.changeSelection(nRow, 0, false, false, bMove)
    }

    fun openFileBrowser() {
        val fd = FileDialog(this, "File open", FileDialog.LOAD)
        //        fd.setDirectory( m_strLastDir );
        fd.isVisible = true
        if (fd.file != null) {
            parseFile(File(fd.directory + fd.file))
            m_recentMenu.addEntry(fd.directory + fd.file)
        }

        //In response to a button click:
        //        final JFileChooser fc = new JFileChooser(m_strLastDir);
        //        int returnVal = fc.showOpenDialog(this);
        //        if (returnVal == JFileChooser.APPROVE_OPTION)
        //        {
        //            File file = fc.getSelectedFile();
        //            m_strLastDir = fc.getCurrentDirectory().getAbsolutePath();
        //            T.d("file = " + file.getAbsolutePath());
        //            parseFile(file);
        //            m_recentMenu.addEntry( file.getAbsolutePath() );
        //        }
    }

    companion object {
        private val serialVersionUID = 1L

        internal val LOGFILTER = "LogFilter"
        internal val VERSION = "Version 1.8"

        internal val DEFAULT_WIDTH = 1200
        internal val DEFAULT_HEIGHT = 720
        internal val MIN_WIDTH = 1100
        internal val MIN_HEIGHT = 500

        internal val DEVICES_ANDROID = 0
        internal val DEVICES_IOS = 1
        internal val DEVICES_CUSTOM = 2

        internal val STATUS_CHANGE = 1
        internal val STATUS_PARSING = 2
        internal val STATUS_READY = 4
        internal lateinit var m_recentMenu: RecentFileMenu

        @JvmStatic
        fun main(args: Array<String>) {
            //        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            val mainFrame = LogFilterMain()
            mainFrame.title = "$LOGFILTER $VERSION"
            //        mainFrame.addWindowListener(new WindowEventHandler());

            val menubar = JMenuBar()
            val file = JMenu("File")
            file.mnemonic = KeyEvent.VK_F

            val fileOpen = JMenuItem("Open")
            fileOpen.mnemonic = KeyEvent.VK_O
            fileOpen.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_O,
                    ActionEvent.ALT_MASK)
            fileOpen.toolTipText = "Open log file"
            fileOpen.addActionListener { mainFrame.openFileBrowser() }

            m_recentMenu = object : RecentFileMenu("RecentFile", 10) {
                override fun onSelectFile(filePath: String) {
                    mainFrame.parseFile(File(filePath))
                }
            }

            file.add(fileOpen)
            file.add(m_recentMenu)

            menubar.add(file)
            mainFrame.jMenuBar = menubar

            if (args != null && args.size > 0) {
                EventQueue.invokeLater { mainFrame.parseFile(File(args[0])) }
            }
        }
    }
}

