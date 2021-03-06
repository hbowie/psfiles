/*
 * Copyright 1999 - 2017 Herb Bowie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.powersurgepub.psfiles;

  import com.powersurgepub.psutils.*;
  import com.powersurgepub.xos2.*;
  import java.io.*;
  import java.text.*;
  import java.util.*;
  import javax.swing.*;

/**
 Allow the user to express their preferences in terms of backups.

 @author Herb Bowie
 */
public class FilePrefs 
  extends javax.swing.JPanel {
  
  public static final String BACKUP_FREQUENCY             = "backup-frequency";
  public static final String OCCASIONAL_BACKUPS           = "occasional-backups";
  public static final String MANUAL_BACKUPS               = "manual-backups";
  public static final String AUTOMATIC_BACKUPS            = "automatic-backups";
  public static final String LAST_BACKUP_DATE             = "last-backup-date";
  public static final String NO_DATE                      = "no-date";
  
  public static final String BACKUPS_TO_KEEP              = "backups-to-keep";
  
  public static final String RECENT_FILES_MAX             = "recent-files-max";
  
  public static final String LAUNCH_AT_STARTUP            = "launch-at-startup";
  public static final String NO_FILE                      = "no-file";
  public static final int    NO_FILE_INDEX                = 0;
  public static final String LAST_FILE_OPENED             = "last-file-opened";
  public static final int    LAST_FILE_OPENED_INDEX       = 1;
  public static final int    STARTUP_COMBO_BOX_LITERALS   = 2;
  
  public static final int    RECENT_FILES_MAX_DEFAULT     = 5;
  
  public static final String PURGE_INACCESSIBLE_FILES     = "purge-inaccessible-files";
  public static final String NEVER                        = "never";
  public static final int    NEVER_INDEX                  = 0;
  public static final String AT_STARTUP                   = "at-startup";
  public static final int    AT_STARTUP_INDEX             = 2;
  public static final String NOW                          = "now";
  public static final int    NOW_INDEX                    = 1;
  
  public static final DateFormat  BACKUP_DATE_FORMATTER 
      = new SimpleDateFormat ("yyyy-MM-dd-HH-mm");
  
  private             int    purgeInaccessiblePref        = NEVER_INDEX;
  
  private             long   daysBetweenBackups           = 7;
  
  private             boolean recentFilesMaxUpdateInProgress = false;
  private             boolean backupsToKeepUpdateInProgress  = false;
  
  private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
  
  /** Single shared occurrence of FilePrefs. */
  private static  FilePrefs         filePrefs = null;
  
  private         AppToBackup       appToBackup;
  
  private         RecentFiles       recentFiles = null;
  
  private         String            specificFileAtStartup = "";
  
  public static final String        ESSENTIAL_PATH        = "essential-path";
  public static final int           ESSENTIAL_COMBO_BOX_LITERALS = 1;
  private             String        essentialPath         = "";
  private             boolean       essentialUserSelection = true;
  
  /**
   Returns a single instance of FilePrefs that can be shared by many classes.
   This is the only way to obtain an instance of FilePrefs, since the
   constructor is private.

  @return A single, shared instance of FilePrefs.
 */
  public static FilePrefs getShared(AppToBackup appToBackup) {
    if (filePrefs == null) {
      filePrefs = new FilePrefs(appToBackup);
    }
    return filePrefs;
  }
  
  /**
   Returns a single instance of FilePrefs that can be shared by many classes.
   This is the only way to obtain an instance of FilePrefs, since the
   constructor is private.

  @return A single, shared instance of FilePrefs.
 */
  public static FilePrefs getShared() {

    return filePrefs;
  }
  
  /** Creates new form FilePrefs */
  public FilePrefs(AppToBackup appToBackup) {
    
    super();
    
    this.appToBackup = appToBackup;
    
    initComponents();
    
  } // end constructor
  
  /**
   Load preference fields from stored user preferences. 
   */
  public void loadFromPrefs () {
    
    // Load frequency of backups
    String freq = UserPrefs.getShared().getPref 
        (BACKUP_FREQUENCY, OCCASIONAL_BACKUPS);
    if (freq.equalsIgnoreCase(MANUAL_BACKUPS)) {
      manualBackupsButton.setSelected(true);
    }
    else
    if (freq.equalsIgnoreCase(AUTOMATIC_BACKUPS)) {
      automaticBackupsButton.setSelected(true);
    } else {
      occasionalBackupsButton.setSelected(true);
    }
    
    // Load number of backups to keep
    int backupsToKeep = UserPrefs.getShared().getPrefAsInt 
        (BACKUPS_TO_KEEP, 0);
    backupsToKeepTextField.setText(String.valueOf(backupsToKeep));
    backupsToKeepSlider.setValue(backupsToKeep);
    
    // Load number of recent files
    int recentMax = UserPrefs.getShared().getPrefAsInt
        (RECENT_FILES_MAX, RECENT_FILES_MAX_DEFAULT);
    recentFilesMaxTextField.setText
        (String.valueOf(recentMax));
    recentFilesMaxSlider.setValue(recentMax);
    if (recentFiles != null) {
      recentFiles.setRecentFilesMax(recentMax);
    }
    
    // Load launch at startup preferences
    String launchAtStartup = UserPrefs.getShared().getPref 
        (LAUNCH_AT_STARTUP, LAST_FILE_OPENED);
    if (launchAtStartup.equalsIgnoreCase(NO_FILE)) {
      startupComboBox.setSelectedIndex(NO_FILE_INDEX);
    }
    else
    if (launchAtStartup.equalsIgnoreCase(LAST_FILE_OPENED)) {
      startupComboBox.setSelectedIndex(LAST_FILE_OPENED_INDEX);
    } else {
      int i = STARTUP_COMBO_BOX_LITERALS;
      boolean found = false;
      while (i < startupComboBox.getItemCount() && (! found)) {
        FileSpec comboBoxFileSpec = getStartupFileSpec (i);
        if (launchAtStartup.equalsIgnoreCase(comboBoxFileSpec.getPath())) {
          startupComboBox.setSelectedIndex(i);
          found = true;
        } else {
          i++;
        }
      } // while looking for a match
      if (! found) {
        specificFileAtStartup = launchAtStartup;
      }
    } // end if startup value is not a literal
    
    // Load essential path preferences
    essentialPath = UserPrefs.getShared().getPref
        (ESSENTIAL_PATH, "");
    setEssentialSelection();
    
    // Load purge inaccessible preferences
    String purgeInaccessible = UserPrefs.getShared().getPref 
        (PURGE_INACCESSIBLE_FILES, NEVER);
    if (purgeInaccessible.equalsIgnoreCase(AT_STARTUP)) {
      purgeWhenComboBox.setSelectedIndex(AT_STARTUP_INDEX);
    } else {
      purgeWhenComboBox.setSelectedIndex(NEVER_INDEX);
    }
    purgeInaccessiblePref = purgeWhenComboBox.getSelectedIndex();
  }
  
  /**
   Set the essential combo box selection to reflect the current value of 
   the essential file path. 
  */
  private void setEssentialSelection() {

    essentialUserSelection = false;
    if (essentialPath.length() == 0) {
      essentialComboBox.setSelectedIndex(NO_FILE_INDEX);
    } else {
      int i = ESSENTIAL_COMBO_BOX_LITERALS;
      boolean found = false;
      while (i < essentialComboBox.getItemCount() && (! found)) {
        FileSpec comboBoxFileSpec = getEssentialFileSpec (i);
        if (essentialPath.equalsIgnoreCase(comboBoxFileSpec.getPath())) {
          essentialComboBox.setSelectedIndex(i);
          found = true;
        } else {
          i++;
        }
      } // while looking for a match
    } // end if essential value is not a literal
    essentialUserSelection = true;
  }
  
  /**
   Provide access to the list of recent files.
  
   @param recentFiles A list of recent files. 
  */
  public void setRecentFiles (RecentFiles recentFiles) {
    this.recentFiles = recentFiles;
    if (recentFiles != null) {
      recentFiles.setFilePrefs(this);
      recentFiles.setRecentFilesMax(recentFilesMaxSlider.getValue());
    }
  }
  
  public void addRecentFileAtEnd (FileSpec recentFile) {

    essentialUserSelection = false;
    startupComboBox.addItem(recentFile.getBriefDisplayName());
    if (recentFile.getPath().equalsIgnoreCase(specificFileAtStartup)) {
      startupComboBox.setSelectedIndex(startupComboBox.getItemCount() - 1);
    }
    
    essentialComboBox.addItem(recentFile.getBriefDisplayName());
    if (recentFile.getPath().equalsIgnoreCase(essentialPath)) {
      essentialComboBox.setSelectedIndex(essentialComboBox.getItemCount() - 1);
    }
    essentialUserSelection = true;

  }
  
  public void addRecentFileAtTop (FileSpec recentFile) {

    essentialUserSelection = false;
    startupComboBox.insertItemAt
        (recentFile.getBriefDisplayName(), STARTUP_COMBO_BOX_LITERALS);
    if (recentFile.getPath().equalsIgnoreCase(specificFileAtStartup)) {
      startupComboBox.setSelectedIndex(STARTUP_COMBO_BOX_LITERALS);
    }
    
    essentialComboBox.insertItemAt
        (recentFile.getBriefDisplayName(), ESSENTIAL_COMBO_BOX_LITERALS);
    if (recentFile.getPath().equalsIgnoreCase(essentialPath)) {
      essentialComboBox.setSelectedIndex(ESSENTIAL_COMBO_BOX_LITERALS);
    } else {
      setEssentialSelection();
    }
    essentialUserSelection = true;

  }
  
  public void addNotSoRecentFile (FileSpec notSoRecentFile) {
    
    startupComboBox.insertItemAt
        (notSoRecentFile.getBriefDisplayName(), STARTUP_COMBO_BOX_LITERALS + 1);
    if (notSoRecentFile.getPath().equalsIgnoreCase(specificFileAtStartup)) {
      startupComboBox.setSelectedIndex(STARTUP_COMBO_BOX_LITERALS + 1);
    }
    
    essentialUserSelection = false;
    essentialComboBox.insertItemAt
        (notSoRecentFile.getBriefDisplayName(), ESSENTIAL_COMBO_BOX_LITERALS + 1);
    if (notSoRecentFile.getPath().equalsIgnoreCase(essentialPath)) {
      essentialComboBox.setSelectedIndex(ESSENTIAL_COMBO_BOX_LITERALS + 1);
    } else {
      setEssentialSelection();
    }
    essentialUserSelection = true;
    
  }
  
  public void removeRecentFile (int i) {

    if (startupComboBox.getItemCount() > (i + STARTUP_COMBO_BOX_LITERALS)) {
      startupComboBox.removeItemAt (i + STARTUP_COMBO_BOX_LITERALS);
    }
    
    essentialUserSelection = false;
    if (essentialComboBox.getItemCount() > (i + ESSENTIAL_COMBO_BOX_LITERALS)) {
      essentialComboBox.removeItemAt (i + ESSENTIAL_COMBO_BOX_LITERALS);
    }
    setEssentialSelection();
    essentialUserSelection = true;
  }
  
  /**
   Remove the oldest files, leaving only the latest. 
  */
  public void clearHistory () {

    while (startupComboBox.getItemCount() > (STARTUP_COMBO_BOX_LITERALS + 1)) {
      startupComboBox.remove(STARTUP_COMBO_BOX_LITERALS + 1);
    }
    
    essentialUserSelection = false;
    while (essentialComboBox.getItemCount() > (ESSENTIAL_COMBO_BOX_LITERALS + 1)) {
      essentialComboBox.remove(ESSENTIAL_COMBO_BOX_LITERALS + 1);
    }
    essentialUserSelection = true;
  }
  
  /**
   Set everything to the given value, if they're not already equal,
   and if the input is in an acceptable range. 
  
   @param recentFilesMax The new value to be used. 
  
   @return The resulting value after the update (if any). 
  */
  private void setRecentFilesMax (int recentFilesMax) {
    if (recentFilesMax >= 1
        && recentFilesMax <= recentFilesMaxSlider.getMaximum()) {
      if (recentFiles != null) {
        recentFiles.setRecentFilesMax(recentFilesMax);
      }
      if (recentFilesMax != getRecentFilesMaxFromText()) {
        recentFilesMaxTextField.setText(String.valueOf(recentFilesMax));
      }
      if (recentFilesMax != recentFilesMaxSlider.getValue()) {
        recentFilesMaxSlider.setValue(recentFilesMax);
      }
    }
  }
  
  /**
   Return an integer value extracted from the text field.
  
   @return The equivalent integer, or -1 if the text field cannot be 
           parsed into an integer. 
  */
  private int getRecentFilesMaxFromText() {
    try {
      return (Integer.parseInt(recentFilesMaxTextField.getText()));
    } catch (NumberFormatException e) {
      return -1;
    }
  }
  
  /**
   Set everything to the given value, if they're not already equal,
   and if the input is in an acceptable range. 
  
   @param recentFilesMax The new value to be used. 
  
   @return The resulting value after the update (if any). 
  */
  private void setBackupsToKeep (int backupsToKeep) {
    if (backupsToKeep >= 0
        && backupsToKeep <= backupsToKeepSlider.getMaximum()) {
      if (backupsToKeep != getBackupsToKeepFromText()) {
        backupsToKeepTextField.setText(String.valueOf(backupsToKeep));
      }
      if (backupsToKeep != backupsToKeepSlider.getValue()) {
        backupsToKeepSlider.setValue(backupsToKeep);
      }
    }
  }
  
  /**
   Return an integer value extracted from the text field.
  
   @return The equivalent integer, or -1 if the text field cannot be 
           parsed into an integer. 
  */
  private int getBackupsToKeepFromText() {
    try {
      return (Integer.parseInt(backupsToKeepTextField.getText()));
    } catch (NumberFormatException e) {
      return -1;
    }
  }
  
  /**
   Return the user's preferred file to launch automatically at startup.
  
   @return The complete path to the file.  
   */
  public String getStartupFilePath () {
    // Return startup file launch prefs
    int i = startupComboBox.getSelectedIndex();
    if (specificFileAtStartup != null
        && specificFileAtStartup.length() > 0) {
      return specificFileAtStartup;
    }
    else
    if (i == 0) {
      return "";
    }
    else
    if (i == 1) {
      if (recentFiles != null
          && recentFiles.size() > 0) {
        return recentFiles.get(0).getPath();
      } else {
        return "";
      }
    } else {
      if (recentFiles != null
          && recentFiles.size() > (i - STARTUP_COMBO_BOX_LITERALS)) {
        return recentFiles.get(i - STARTUP_COMBO_BOX_LITERALS).getPath();
      } else {
        return specificFileAtStartup;
      }
    }
  }
  
  public boolean hasEssentialFilePath() {
    // refreshEssentialPath();
    return (essentialPath != null && essentialPath.length() > 0);
  }
  
  public String getEssentialFilePath() {
    
    // refreshEssentialPath();
    return essentialPath;
  }
  
  /**
   Return a File Spec identifying the user's preferred file to launch
   automatically at startup.
  
   @return The File Spec identifying the preferred file.  
   */
  public FileSpec getStartupFileSpec () {
    return getStartupFileSpec (startupComboBox.getSelectedIndex());
  }
  
  /**
   Return the file spec for the file identified by the passed index.
  
   @param i Index to the desired startup combo box value. 
  
   @return A file spec for the file implied by the passed index.  
  */
  public FileSpec getStartupFileSpec (int i) {

    if (startupComboBox.getItemCount() <= STARTUP_COMBO_BOX_LITERALS
        && specificFileAtStartup != null
        && specificFileAtStartup.length() > 0) {
      return new FileSpec (specificFileAtStartup);
    }
    else
    if (i == 0) {
      return null;
    }
    else
    if (i == 1) {
      if (recentFiles != null
          && recentFiles.size() > 0) {
        return recentFiles.get(0);
      } else {
        return null;
      }
    } else {
      if (recentFiles != null
          && recentFiles.size() > (i - STARTUP_COMBO_BOX_LITERALS)) {
        return recentFiles.get(i - STARTUP_COMBO_BOX_LITERALS);
      } else {
        return null;
      }
    }
  }
  
  /**
   Return a File Spec identifying the user's preferred file to launch
   with the Essential shortcut.
  
   @return The File Spec identifying the essential file.  
   */
  public FileSpec getEssentialFileSpec () {
    return getEssentialFileSpec (essentialComboBox.getSelectedIndex());
  }
  
  /**
   Return the file spec for the file identified by the passed index.
  
   @param i Index to the desired essential combo box value. 
  
   @return A file spec for the file implied by the passed index.  
  */
  public FileSpec getEssentialFileSpec (int i) {

    if (essentialComboBox.getItemCount() <= ESSENTIAL_COMBO_BOX_LITERALS) {
      return null;
    }
    else
    if (i == 0) {
      return null;
    } else {
      if (recentFiles != null
          && recentFiles.size() > (i - ESSENTIAL_COMBO_BOX_LITERALS)) {
        return recentFiles.get(i - ESSENTIAL_COMBO_BOX_LITERALS);
      } else {
        return null;
      }
    }
  }
  
  /**
   Set combo box index programmatically. 
  
   @param specified index
  */
  private void setEssentialIndex(int i) {
    essentialUserSelection = false;
    essentialComboBox.setSelectedIndex(i);
    essentialUserSelection = true;
  }
  
  public boolean purgeRecentFilesAtStartup () {
    return (purgeWhenComboBox.getSelectedIndex() == AT_STARTUP_INDEX);
  }
  
  /**
   Save the preferences that were last specified by the user. 
   */
  public void savePrefs() {
    
    // Save backup prefs
    if (manualBackupsButton.isSelected()) {
      UserPrefs.getShared().setPref(BACKUP_FREQUENCY, MANUAL_BACKUPS);
    }
    else
    if (automaticBackupsButton.isSelected()) {
      UserPrefs.getShared().setPref(BACKUP_FREQUENCY, AUTOMATIC_BACKUPS);
    } else {
      UserPrefs.getShared().setPref(BACKUP_FREQUENCY, OCCASIONAL_BACKUPS);
    }
    
    // Save backups to keep
    UserPrefs.getShared().setPref
        (BACKUPS_TO_KEEP, backupsToKeepSlider.getValue());
    
    // Save recent files max
    UserPrefs.getShared().setPref
        (RECENT_FILES_MAX, recentFilesMaxSlider.getValue());
    
    // Save startup file launch prefs
    if (startupComboBox.getSelectedIndex() == NO_FILE_INDEX) {
      UserPrefs.getShared().setPref(LAUNCH_AT_STARTUP, NO_FILE);
    }
    else
    if (startupComboBox.getSelectedIndex() == LAST_FILE_OPENED_INDEX) {
      UserPrefs.getShared().setPref(LAUNCH_AT_STARTUP, LAST_FILE_OPENED);
    } else {
      FileSpec selectedFileSpec = getStartupFileSpec();
      UserPrefs.getShared().setPref
          (LAUNCH_AT_STARTUP, selectedFileSpec.getPath());
    }
    
    // Save Essential file prefs
    UserPrefs.getShared().setPref(ESSENTIAL_PATH, essentialPath);
    
    // Save purge inaccessible files prefs
    if (purgeWhenComboBox.getSelectedIndex() == AT_STARTUP_INDEX) {
      UserPrefs.getShared().setPref(PURGE_INACCESSIBLE_FILES, AT_STARTUP);
    } else {
      UserPrefs.getShared().setPref(PURGE_INACCESSIBLE_FILES, NEVER);
    }
  }
  
  /**
   Handle a major event that could threaten data integrity (and thus
   should prompt a backup).
  
   @return True if backup occurred. 
  */
  public boolean handleMajorEvent(
      FileSpec fileSpec, 
      String prefsQualifier, 
      int recentFileNumber) {
    
    boolean backedUp = false;
    if (fileSpec != null
        && fileSpec.hasPath()) {
    
      if (automaticBackupsButton.isSelected()) {
        backedUp = appToBackup.backupWithoutPrompt();
      }
      else
      if (occasionalBackupsButton.isSelected()) {
        backedUp = promptForBackup();
      } 
      if (backedUp) {
        saveLastBackupDate(fileSpec, prefsQualifier, recentFileNumber);
      }
    }
    return backedUp;
  }
  
  public boolean handleClose() {
    if (recentFiles != null
        && recentFiles.size() > 0) {
      return handleClose
          (recentFiles.get(0), recentFiles.getPrefsQualifier(), 0);
    } else {
      return false;
    }
  }
  
  /**
   Handle the close operation for a recent file. 
  
   @return True if backup occurred. 
  */
  public boolean handleClose(
      FileSpec fileSpec, 
      String prefsQualifier, 
      int recentFileNumber) {
    
    boolean backedUp = false;
    
    if (fileSpec != null
        && fileSpec.hasPath()) {
    
      // For automatic backups, backup with every quit
      if (automaticBackupsButton.isSelected()) {
        backedUp = appToBackup.backupWithoutPrompt();
      }
      else

      // For occasional backups, offer to backup every 7 days
      if (occasionalBackupsButton.isSelected()) {
        long daysBetween = daysBetweenBackups; 
        Calendar today =  Calendar.getInstance();
        today.setTime(new Date());
        String lastBackupDateString = fileSpec.getLastBackupDateAsString();
        if (lastBackupDateString.equals(NO_DATE)
            || lastBackupDateString.length() == 0) {
          daysBetween = daysBetweenBackups;
        } else {
          try {
            DateFormat formatter = DateFormat.getDateTimeInstance();
            Date lastBackupDate = formatter.parse(lastBackupDateString);
            Calendar last = Calendar.getInstance();
            last.setTime(lastBackupDate);
            daysBetween = 0;
            while (last.before(today)) {  
              last.add(Calendar.DAY_OF_MONTH, 1);  
              daysBetween++;  
            } 
          } catch (ParseException e) {
            System.out.println ("  Parse Exception " + e.toString());
            daysBetween = daysBetweenBackups;
          } 
        }
        if (daysBetween >= daysBetweenBackups) {
          backedUp = promptForBackup();
        }
      } 

      if (backedUp) {
        saveLastBackupDate(fileSpec, prefsQualifier, recentFileNumber);
      }
    }
    return backedUp;
  }
  
  /**
   See if the user wants to do a backup now.
  
   @return Yes if the backup ended up getting done, false otherwise.
  */
  private boolean promptForBackup() {
    Object[] options = {"Yes, please",
                    "No, thanks"};
    int option = JOptionPane.showOptionDialog(
        XOS.getShared().getMainWindow(),
        "May we suggest a backup?", 
        "Backup Suggestion", 
        JOptionPane.YES_NO_OPTION, 
        JOptionPane.QUESTION_MESSAGE, 
        Home.getShared().getIcon(), 
        options, 
        options[0]);
    
    if (option == 0) {
      return appToBackup.promptForBackup();
    } else {
      return false;
    }
  }
  
  /**
   Get the default file name to be used for backups. 
  
   @param primaryFile The file or folder to be backed up.
  
   @param ext The intended extension for the backup file. 
  
   @return THe suggested name for the backup file. 
  */
  public String getBackupFileName(File primaryFile, String ext) {
    StringBuilder backupFileName = new StringBuilder ();
    FileName name = new FileName (primaryFile);
    int numberOfFolders = name.getNumberOfFolders();
    int i = numberOfFolders - 1;
    if (i < 0) {
      i = 0;
    }
    while (i <= numberOfFolders) {
      if (backupFileName.length() > 0) {
        backupFileName.append (' ');
      }
      backupFileName.append (name.getFolder (i));
      i++;
    }
    backupFileName.append (" backup ");
    backupFileName.append (getBackupDate());
    if (ext.length() == 0) {
      // no extension
    }
    else
    if (ext.charAt(0) == '.') {
      backupFileName.append(ext);
    } else {
      backupFileName.append (".");
      backupFileName.append(ext);
    }
    return backupFileName.toString();
  }
  
  /**
   Remove older backup files or folders. 
  
   @param backupFolder The folder containing all the backups.
   @param fileNameWithoutDate The file name, without any date. 
  
   @return The number of backups pruned. 
  */
  public int pruneBackups(File backupFolder, String fileNameWithoutDate) {
    int pruned = 0;
    int backupsToKeep = backupsToKeepSlider.getValue();
    if (backupsToKeep > 0) {
      ArrayList<String> backups = new ArrayList<String>();
      String[] dirEntries = backupFolder.list();
      for (int i = 0; i < dirEntries.length; i++) {
        String dirEntryName = dirEntries[i];
        if (dirEntryName.startsWith(fileNameWithoutDate)) {
          boolean added = false;
          int j = 0;
          while ((! added) && (j < backups.size())) {
            if (dirEntryName.compareTo(backups.get(j)) > 0) {
              backups.add(j, dirEntryName);
              added = true;
            } else {
              j++;
            }
          } // end while looking for insertion point
          if (! added) {
            backups.add(dirEntryName);
          }
        } // end if file/folder name matches prefix
      } // end of directory entries
      while (backups.size() > backupsToKeep) {
        String toDelete = backups.get(backups.size() - 1);
        File toDeleteFile = new File (backupFolder, toDelete);
        if (toDeleteFile.isDirectory()) {
          FileUtils.deleteFolderContents(toDeleteFile);
        }
        toDeleteFile.delete(); 
        Logger.getShared().recordEvent(LogEvent.NORMAL, 
            "Pruning older backup: " + toDeleteFile.toString(), 
            false);
        pruned++;
        backups.remove(backups.size() - 1);
      }
    } // if we have a backups to keep number
    return pruned;
  }
  
  /**
   Return the current date and time formatted in a way that can be 
   easily appended to a file or folder name. 
  
   @return Current date and time. 
  */
  public static String getBackupDate() {
    return BACKUP_DATE_FORMATTER.format (new Date());
  }
  
  public void saveLastBackupDate(
      FileSpec fileSpec, 
      String prefsQualifier, 
      int recentFileNumber) {
    
    fileSpec.setLastBackupDateToNow();
    fileSpec.saveToRecentPrefs(prefsQualifier, recentFileNumber);
  }
  
  private void updateRecentFilesMaxTextField() {
    if (! recentFilesMaxUpdateInProgress) {
      recentFilesMaxUpdateInProgress = true;
      msgToUser.setText(" ");    
      int recentFilesMaxText = getRecentFilesMaxFromText();
      if (recentFilesMaxText >= 0) {
        if (recentFilesMaxText >= 1
            && recentFilesMaxText <= recentFilesMaxSlider.getMaximum()) {
          setRecentFilesMax(recentFilesMaxText);
        } else {
          recentFilesMaxTextField.setText
              (String.valueOf(recentFilesMaxSlider.getValue()));
          msgToUser.setText("Number of Recent Files cannot be outside of slider range");
        }
      } else {
        recentFilesMaxTextField.setText
            (String.valueOf(recentFilesMaxSlider.getValue()));
        msgToUser.setText("Number of Recent Files smust be numeric");
      }
      recentFilesMaxUpdateInProgress = false;
    }
  }
  
  private void updateRecentFilesMax(int recentFilesMax) {
    if (recentFiles != null) {
      recentFiles.setRecentFilesMax(recentFilesMax);
    }
  }
  
  private void updateBackupsToKeepTextField() {
    if (! backupsToKeepUpdateInProgress) {
      backupsToKeepUpdateInProgress = true;
      msgToUser.setText(" ");    
      int backupsToKeepText = getBackupsToKeepFromText();
      if (backupsToKeepText >= 0) {
        if (backupsToKeepText >= 1
            && backupsToKeepText <= backupsToKeepSlider.getMaximum()) {
          setBackupsToKeep(backupsToKeepText);
        } else {
          backupsToKeepTextField.setText
              (String.valueOf(backupsToKeepSlider.getValue()));
          msgToUser.setText("Number of Backups to Keep cannot be outside of slider range");
        }
      } else {
        backupsToKeepTextField.setText
            (String.valueOf(backupsToKeepSlider.getValue()));
        msgToUser.setText("Number of Backups to Keep smust be numeric");
      }
      backupsToKeepUpdateInProgress = false;
    }
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    frequencyButtonGroup = new javax.swing.ButtonGroup();
    filePrefsForLabel = new javax.swing.JLabel();
    backupFrequencyLabel = new javax.swing.JLabel();
    manualBackupsButton = new javax.swing.JRadioButton();
    occasionalBackupsButton = new javax.swing.JRadioButton();
    automaticBackupsButton = new javax.swing.JRadioButton();
    backupsToKeepLabel = new javax.swing.JLabel();
    backupsToKeepTextField = new javax.swing.JTextField();
    backupsToKeepSlider = new javax.swing.JSlider();
    startupLabel = new javax.swing.JLabel();
    startupComboBox = new javax.swing.JComboBox();
    essentialLabel = new javax.swing.JLabel();
    essentialComboBox = new javax.swing.JComboBox();
    purgeWhenLabel = new javax.swing.JLabel();
    purgeWhenComboBox = new javax.swing.JComboBox();
    bottomSpacer = new javax.swing.JLabel();
    msgToUser = new javax.swing.JLabel();
    recentFilesMaxLabel = new javax.swing.JLabel();
    recentFilesMaxTextField = new javax.swing.JTextField();
    recentFilesMaxSlider = new javax.swing.JSlider();

    setLayout(new java.awt.GridBagLayout());

    filePrefsForLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    filePrefsForLabel.setText(" ");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    add(filePrefsForLabel, gridBagConstraints);

    backupFrequencyLabel.setText("Backup Frequency:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    add(backupFrequencyLabel, gridBagConstraints);

    frequencyButtonGroup.add(manualBackupsButton);
    manualBackupsButton.setText("Manual Only");
    manualBackupsButton.setToolTipText("Backups will only be performed when you manually select Backup from the File menu.");
    manualBackupsButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        manualBackupsButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    add(manualBackupsButton, gridBagConstraints);

    frequencyButtonGroup.add(occasionalBackupsButton);
    occasionalBackupsButton.setSelected(true);
    occasionalBackupsButton.setText("Occasional Suggestions");
    occasionalBackupsButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        occasionalBackupsButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    add(occasionalBackupsButton, gridBagConstraints);

    frequencyButtonGroup.add(automaticBackupsButton);
    automaticBackupsButton.setText("Automatic Backups");
    automaticBackupsButton.setToolTipText("Backups will be performed automatically whenever you close the program, and at major program events. ");
    automaticBackupsButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        automaticBackupsButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    add(automaticBackupsButton, gridBagConstraints);

    backupsToKeepLabel.setText("Backups to Keep:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    add(backupsToKeepLabel, gridBagConstraints);

    backupsToKeepTextField.setColumns(4);
    backupsToKeepTextField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    backupsToKeepTextField.setText("5");
    backupsToKeepTextField.setToolTipText("The number of backups to be retained");
    backupsToKeepTextField.setMinimumSize(new java.awt.Dimension(62, 28));
    backupsToKeepTextField.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        backupsToKeepTextFieldActionPerformed(evt);
      }
    });
    backupsToKeepTextField.addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusLost(java.awt.event.FocusEvent evt) {
        backupsToKeepTextFieldFocusLost(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    add(backupsToKeepTextField, gridBagConstraints);

    backupsToKeepSlider.setMajorTickSpacing(5);
    backupsToKeepSlider.setMaximum(25);
    backupsToKeepSlider.setMinorTickSpacing(1);
    backupsToKeepSlider.setPaintTicks(true);
    backupsToKeepSlider.setValue(5);
    backupsToKeepSlider.setFocusable(false);
    backupsToKeepSlider.setMinimumSize(new java.awt.Dimension(100, 38));
    backupsToKeepSlider.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        backupsToKeepSliderStateChanged(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    add(backupsToKeepSlider, gridBagConstraints);

    startupLabel.setText("At startup, open:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    add(startupLabel, gridBagConstraints);

    startupComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Nothing", "Last File Opened" }));
    startupComboBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        startupComboBoxActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    add(startupComboBox, gridBagConstraints);

    essentialLabel.setText("Assign Essential Shortcut to:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    add(essentialLabel, gridBagConstraints);

    essentialComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Nothing" }));
    essentialComboBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        essentialComboBoxActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    add(essentialComboBox, gridBagConstraints);

    purgeWhenLabel.setText("Purge inaccessible files:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    add(purgeWhenLabel, gridBagConstraints);

    purgeWhenComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Never", "Now", "At startup" }));
    purgeWhenComboBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        purgeWhenComboBoxActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    add(purgeWhenComboBox, gridBagConstraints);

    bottomSpacer.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    bottomSpacer.setText(" ");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    add(bottomSpacer, gridBagConstraints);

    msgToUser.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    msgToUser.setText(" ");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    add(msgToUser, gridBagConstraints);

    recentFilesMaxLabel.setText("Number of Recent Files:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    add(recentFilesMaxLabel, gridBagConstraints);

    recentFilesMaxTextField.setColumns(4);
    recentFilesMaxTextField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    recentFilesMaxTextField.setText("5");
    recentFilesMaxTextField.setToolTipText("The maximum number of recent files to be retained");
    recentFilesMaxTextField.setMinimumSize(new java.awt.Dimension(62, 28));
    recentFilesMaxTextField.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        recentFilesMaxTextFieldActionPerformed(evt);
      }
    });
    recentFilesMaxTextField.addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusLost(java.awt.event.FocusEvent evt) {
        recentFilesMaxTextFieldFocusLost(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    add(recentFilesMaxTextField, gridBagConstraints);

    recentFilesMaxSlider.setMajorTickSpacing(5);
    recentFilesMaxSlider.setMaximum(50);
    recentFilesMaxSlider.setMinorTickSpacing(1);
    recentFilesMaxSlider.setPaintTicks(true);
    recentFilesMaxSlider.setValue(5);
    recentFilesMaxSlider.setFocusable(false);
    recentFilesMaxSlider.setMinimumSize(new java.awt.Dimension(100, 38));
    recentFilesMaxSlider.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        recentFilesMaxSliderStateChanged(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    add(recentFilesMaxSlider, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents

private void manualBackupsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manualBackupsButtonActionPerformed
  msgToUser.setText(" ");
  savePrefs();
}//GEN-LAST:event_manualBackupsButtonActionPerformed

private void occasionalBackupsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_occasionalBackupsButtonActionPerformed
  msgToUser.setText(" ");
  savePrefs();
}//GEN-LAST:event_occasionalBackupsButtonActionPerformed

private void automaticBackupsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_automaticBackupsButtonActionPerformed
  msgToUser.setText(" ");
  savePrefs();
}//GEN-LAST:event_automaticBackupsButtonActionPerformed

  private void recentFilesMaxSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_recentFilesMaxSliderStateChanged

    if (! recentFilesMaxUpdateInProgress) {
      recentFilesMaxUpdateInProgress = true;
      msgToUser.setText(" ");
      int recentFilesMaxSliderValue = recentFilesMaxSlider.getValue();
      if (recentFilesMaxSliderValue < 1) {
        recentFilesMaxSliderValue = 1;
      }
      recentFilesMaxTextField.setText(String.valueOf(recentFilesMaxSliderValue));
      if (! recentFilesMaxSlider.getValueIsAdjusting()) {
        updateRecentFilesMax(recentFilesMaxSliderValue);
      }
      recentFilesMaxUpdateInProgress = false;
    }
  }//GEN-LAST:event_recentFilesMaxSliderStateChanged

  private void recentFilesMaxTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recentFilesMaxTextFieldActionPerformed
    updateRecentFilesMaxTextField();
  }//GEN-LAST:event_recentFilesMaxTextFieldActionPerformed

  private void recentFilesMaxTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_recentFilesMaxTextFieldFocusLost
    updateRecentFilesMaxTextField();
  }//GEN-LAST:event_recentFilesMaxTextFieldFocusLost

  private void purgeWhenComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_purgeWhenComboBoxActionPerformed
    if (purgeWhenComboBox.getSelectedIndex() == NOW_INDEX) {
      if (recentFiles != null) {
        recentFiles.purgeInaccessibleFiles();
      }
      purgeWhenComboBox.setSelectedIndex(purgeInaccessiblePref);
    } else {
      purgeInaccessiblePref = purgeWhenComboBox.getSelectedIndex();
    }
  }//GEN-LAST:event_purgeWhenComboBoxActionPerformed

  private void backupsToKeepTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backupsToKeepTextFieldActionPerformed
    updateBackupsToKeepTextField();
  }//GEN-LAST:event_backupsToKeepTextFieldActionPerformed

  private void backupsToKeepTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_backupsToKeepTextFieldFocusLost
    updateBackupsToKeepTextField();
  }//GEN-LAST:event_backupsToKeepTextFieldFocusLost

  private void backupsToKeepSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_backupsToKeepSliderStateChanged
    if (! backupsToKeepUpdateInProgress) {
      backupsToKeepUpdateInProgress = true;
      msgToUser.setText(" ");
      int backupsToKeepSliderValue = backupsToKeepSlider.getValue();
      if (backupsToKeepSliderValue < 0) {
        backupsToKeepSliderValue = 0;
      }
      backupsToKeepTextField.setText(String.valueOf(backupsToKeepSliderValue));
      backupsToKeepUpdateInProgress = false;
    }
  }//GEN-LAST:event_backupsToKeepSliderStateChanged

  private void startupComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startupComboBoxActionPerformed
    // ???
  }//GEN-LAST:event_startupComboBoxActionPerformed

  private void essentialComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_essentialComboBoxActionPerformed

    int i = essentialComboBox.getSelectedIndex();
    if (essentialUserSelection) {
      if (i <= 0) {
        essentialPath = "";
      } else {
        if (recentFiles != null
            && recentFiles.size() > (i - ESSENTIAL_COMBO_BOX_LITERALS)) {
          essentialPath = recentFiles.get(i - ESSENTIAL_COMBO_BOX_LITERALS).getPath();
        } 
      }
    }
  }//GEN-LAST:event_essentialComboBoxActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JRadioButton automaticBackupsButton;
  private javax.swing.JLabel backupFrequencyLabel;
  private javax.swing.JLabel backupsToKeepLabel;
  private javax.swing.JSlider backupsToKeepSlider;
  private javax.swing.JTextField backupsToKeepTextField;
  private javax.swing.JLabel bottomSpacer;
  private javax.swing.JComboBox essentialComboBox;
  private javax.swing.JLabel essentialLabel;
  private javax.swing.JLabel filePrefsForLabel;
  private javax.swing.ButtonGroup frequencyButtonGroup;
  private javax.swing.JRadioButton manualBackupsButton;
  private javax.swing.JLabel msgToUser;
  private javax.swing.JRadioButton occasionalBackupsButton;
  private javax.swing.JComboBox purgeWhenComboBox;
  private javax.swing.JLabel purgeWhenLabel;
  private javax.swing.JLabel recentFilesMaxLabel;
  private javax.swing.JSlider recentFilesMaxSlider;
  private javax.swing.JTextField recentFilesMaxTextField;
  private javax.swing.JComboBox startupComboBox;
  private javax.swing.JLabel startupLabel;
  // End of variables declaration//GEN-END:variables
}
