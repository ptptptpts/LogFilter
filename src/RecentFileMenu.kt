/*
RecentFileMenu.java - menu to store and display recently used files.
 
 Copyright  (C) 2005 Hugues Johnson
 
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
the GNU General Public License for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.LineNumberReader
import javax.swing.JMenu
import javax.swing.JMenuItem

/**
 * A menu used to store and display recently used files.
 * Saves entries in a file called "[user.dir]/[name passed to constructor].recent".
 * @author Hugues Johnson
 */
abstract class RecentFileMenu
/**
 * Create a new instance of RecentFileMenu.
 * @param name The name of this menu, not displayed but used to store the list of recently used file names.
 * @param count The number of recent files to store.
 */
(name: String, itemCount: Int //how many items in the menu
) : JMenu() {
    private var pathToSavedFile: String? = null //where to save the items in this menu
    private val recentEntries: Array<String?> //the recent file entries

    init {
        this.text = "Recent"
        this.setMnemonic('R')
        //initialize default entries
        this.recentEntries = arrayOfNulls(itemCount)
        for (index in 0 until this.itemCount) {
            this.recentEntries[index] = defaultText
        }
        //figure out the name of the recent file
        this.pathToSavedFile = System.getProperty("user.dir")
        if (this.pathToSavedFile == null || this.pathToSavedFile!!.length <= 0) {
            this.pathToSavedFile = "$name.ini" //probably unreachable
        } else if (this.pathToSavedFile!!.endsWith(File.separator)) {
            this.pathToSavedFile = this.pathToSavedFile + name + ".ini"
        } else {
            this.pathToSavedFile = this.pathToSavedFile + File.separator + name + ".ini"
        }
        //load the recent entries if they exist
        val recentFile = File(this.pathToSavedFile!!)
        if (recentFile.exists()) {
            try {
                val reader = LineNumberReader(FileReader(this.pathToSavedFile!!))
                while (reader.ready()) {
                    this.addEntry(reader.readLine(), false)
                }
            } catch (x: Exception) {
                x.printStackTrace()
            }

        } else { //disable
            this.isEnabled = false
        }
    }

    /**
     * Adds a new entry to the menu. Moves everything "down" one row.
     * @param filePath The new path to add.
     */
    fun addEntry(filePath: String) {
        this.addEntry(filePath, true)
    }

    /**
     * Adds a new entry to the menu. Moves everything "down" one row.
     * @param filePath The new path to add.
     * @param updateFile Whether to update the saved file, only false when called from constructor.
     */
    private fun addEntry(filePath: String, updateFile: Boolean) {
        //check if this is disabled
        if (!this.isEnabled) {
            this.isEnabled = true
        }
        //clear the existing items
        this.removeAll()
        //move everything down one slot
        val count = this.itemCount - 1
        for (index in count downTo 1) {
            //check for duplicate entry
            if (!this.recentEntries[index - 1].equals(filePath, ignoreCase = true)) {
                this.recentEntries[index] = this.recentEntries[index - 1]
            }
        }
        //add the new item, check if it's not alredy the first item
        if (!this.recentEntries[0].equals(filePath, ignoreCase = true)) {
            this.recentEntries[0] = filePath
        }
        //add items back to the menu
        for (index in 0 until this.itemCount) {
            val menuItem = JMenuItem()
            menuItem.text = this.recentEntries[index]
            if (this.recentEntries[index] == defaultText) {
                menuItem.isVisible = false
            } else {
                menuItem.isVisible = true
                menuItem.toolTipText = this.recentEntries[index]
                menuItem.actionCommand = this.recentEntries[index]
                menuItem.addActionListener { actionEvent -> onSelectFile(actionEvent.actionCommand) }
            }
            this.add(menuItem)
        }
        //update the file
        if (updateFile) {
            try {
                val writer = FileWriter(File(this.pathToSavedFile!!))
                val topIndex = this.itemCount - 1
                for (index in topIndex downTo 0) {
                    if (this.recentEntries[index] != defaultText) {
                        writer.write(this.recentEntries[index])
                        writer.write("\n")
                    }
                }
                writer.flush()
                writer.close()
            } catch (x: Exception) {
                x.printStackTrace()
            }

        }
    }

    /**
     * Event that fires when a recent file is selected from the menu. Override this when implementing.
     * @param filePath The file that was selected.
     */
    abstract fun onSelectFile(filePath: String)

    companion object {
        private val defaultText = "__R_e_C_e_N_t__:_?" //colon & question mark are not allowed as a file name in any OS that I'm aware of
    }
}