package com.powersurgepub.psfiles;

import com.powersurgepub.psfiles.FileSpec;

/**
  Interface for a program capable of opening a file from a file spec.
 
 */
public interface FileSpecOpener {
  
  /**      
    Standard way to respond to a document being passed to this application on a Mac.
   
    @param fileSpec File to be processed by this application, generally
                    as a result of a file or directory being dragged
                    onto the application icon.
   */
  public void handleOpenFile (FileSpec fileSpec);
  
}
