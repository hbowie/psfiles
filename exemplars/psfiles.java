/*
 The psfiles package is designed to keep track of a number of files or folders that have been opened
 by an application in the past, and that presumably the user may wish to open again at some point. 
 */

/*
 The application, or its proxy, should implement the following code. 
 */
 
    implements AppToBackup, FileSpecOpener;
 
  private             PrefsWindow         prefsWindow;
  private             RecentFiles         recentFiles;
  private             FilePrefs           filePrefs;
  
  /*
   Initialization code. An openRecentMenu menu must have been added to the File menu. 
   */
   
    prefsWindow = new PrefsWindow (this);
    
    filePrefs = new FilePrefs(this);
    filePrefs.loadFromPrefs();
    prefsWindow.setFilePrefs(filePrefs);
    
    recentFiles = new RecentFiles();
    // or the following, if the app requires multiple file types, each having its own
    // list of recent files...
    recentFiles = new RecentFiles(prefsQualifier);
    
    filePrefs.setRecentFiles(recentFiles);
    recentFiles.registerMenu(openRecentMenu, this);
    recentFiles.loadFromPrefs();
    if (filePrefs.purgeRecentFilesAtStartup()) {
      recentFiles.purgeInaccessibleFiles();
    }
    
    /*
     Following initialization, to get user's preferred file or folder to open.
     */
    String lastFileString = filePrefs.getStartupFilePath();
    if (lastFileString != null
        && lastFileString.length() > 0) {
      File lastFile = new File (lastFileString);
      if (lastFile.exists()
          && lastFile.isFile()
          && lastFile.canRead()) {
        openFile (lastFile);
      }
    }
    
    /*
     Whenever doing a general save of preferences, as at shutdown.
     */
		recentFiles.savePrefs();
		filePrefs.savePrefs();
		
    
    
  /**      
    Standard way to respond to a document being passed to this application on a Mac.
   
    @param fileSpec File to be processed by this application, generally
                    as a result of a file or directory being dragged
                    onto the application icon.
   */
  public void handleOpenFile (FileSpec fileSpec) {
  
  }
  
  /*
   Handle a major event, before which one might wish to backup one's data. 
   */
   filePrefs.handleMajorEvent(fileSpec, prefsQualifier, recentFileNumber);
  
  /*
   When opening a new file...
   */
		recentFiles.addRecentFile (file);
		
	/*
	 When closing a file...
	 */
    if (currentFileModified) {
      filePrefs.handleClose();
    }
 
  /**
   Prompt the user for a backup location. 
  
   @return True if backup was successful.
  */
  public boolean promptForBackup() {
  
  }
  /**
   Backup without prompting the user. 
  
   @return True if backup was successful. 
  */
  public boolean backupWithoutPrompt() {
  
  }