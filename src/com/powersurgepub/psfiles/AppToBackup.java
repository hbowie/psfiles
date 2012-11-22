package com.powersurgepub.psfiles;

/**
 The required interface for the application needing backup. 

 @author Herb Bowie
 */
public interface AppToBackup {
  
  /**
   Prompt the user for a backup location. 
  
   @return True if backup was successful.
  */
  public boolean promptForBackup();
  
  /**
   Backup without prompting the user. 
  
   @return True if backup was successful. 
  */
  public boolean backupWithoutPrompt();
  
}
