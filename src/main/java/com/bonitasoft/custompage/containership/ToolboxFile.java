package com.bonitasoft.custompage.containership;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.log.event.BEvent.Level;

public class ToolboxFile {

    static BEvent DuplicationSuccess = new BEvent(ToolboxFile.class.getName(), 1, Level.INFO,
            "Duplication correct", "");
    static BEvent AlreadyExist = new BEvent(ToolboxFile.class.getName(), 2, Level.INFO,
            "Already Exist", "");

    static BEvent CopyError = new BEvent(ToolboxFile.class.getName(), 3, Level.APPLICATIONERROR,
            "Error during copyFolder", "An error arrive during the copy of one folder/file", "Check the exception error");

    static BEvent BadFolder = new BEvent(ToolboxFile.class.getName(), 4, Level.APPLICATIONERROR,
            "Bad Folder", "The folder is expected to exist, and this is not the case", "Check the path");

    static BEvent EventErrorDeletionDirectory = new BEvent(ToolboxFile.class.getName(), 5, Level.ERROR,
            "Directory deletion", "Error while delete the directory", "Check the path");

    public static class CopyFolderParameter {

        public boolean overwrite = true;
        public boolean reportOnlyError = false;
        public boolean destinationFolderMustExist = false;
    }

    /**
     * copy a folder recursively to an another folder. All subfolder are copyed. If overwrite is true, then the existing file is replace in the dest folder
     * Use like File srcFolder = new File("c:\\mkyong"); File destFolder = new File("c:\\mkyong-new");copyFolder(srcFolder, destFolder)
     *
     * @param src
     * @param dest
     * @param overwrite if a file with same name is found in the dest folder, it is override
     * @return
     */
    public static List<BEvent> copyFolder(final String src, final String dest, final CopyFolderParameter copyFolderParameter)
    {
        List<BEvent> listEvents = new ArrayList<BEvent>();
        Toolbox.logger.info("Copy filter [" + src + "] to [" + dest + "]");

        try
        {
            final File fSrc = new File(src);
            if (!fSrc.exists())
            {
                listEvents.add(new BEvent(BadFolder, "Source Path [" + src + "]"));
                return listEvents;
            }
            final File fDest = new File(dest);
            if (!fDest.exists() && copyFolderParameter.destinationFolderMustExist)
            {
                listEvents.add(new BEvent(BadFolder, "Destination Path [" + src + "]"));
                return listEvents;

            }
            listEvents = copyFolder(fSrc, fDest, copyFolderParameter);
        } catch (final NullPointerException e) {
            listEvents.add(new BEvent(CopyError, e, "Bad parameters"));
        }
        return listEvents;
    }

    public static List<BEvent> copyFolder(final File src, final File dest, final CopyFolderParameter copyFolderParameter)
    {
        final List<BEvent> listEvents = new ArrayList<BEvent>();
        try
        {
            if (src.isDirectory()) {

                //if directory not exists, create it
                if (!dest.exists()) {
                    dest.mkdir();
                    // logger.info("Directory created from " + src + "  to " + dest);
                }

                //list all the directory contents
                final String files[] = src.list();
                if (files != null) {
                    for (final String file : files) {
                        //construct the src and dest file structure
                        final File srcFile = new File(src, file);
                        final File destFile = new File(dest, file);
                        //recursive copy
                        listEvents.addAll(copyFolder(srcFile, destFile, copyFolderParameter));
                    }
                }

            } else {
                if (dest.exists() && !copyFolderParameter.overwrite) {
                    if (!copyFolderParameter.reportOnlyError) {
                        listEvents.add(new BEvent(AlreadyExist, dest.getAbsolutePath()));
                    }
                }
                //if file, then copy it
                //Use bytes stream to support all file types
                final InputStream in = new FileInputStream(src);
                final OutputStream out = new FileOutputStream(dest);

                final byte[] buffer = new byte[1024];
                int length;
                //copy the file content in bytes
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }

                in.close();
                out.close();
                if (!copyFolderParameter.reportOnlyError) {
                    listEvents.add(new BEvent(DuplicationSuccess, dest.getAbsolutePath()));
                }

                // logger.info("File copied from " + src + " to " + dest);
            }
        } catch (final Exception e)
        {
            Toolbox.logger.severe("Error during copy " + e.toString());
            listEvents.add(new BEvent(CopyError, e, "from " + src + " to " + dest));
        }
        return listEvents;
    }

    public static List<BEvent> deleteFolder(final String dir)
            throws IOException {
        final File directory = new File(dir);
        final List<BEvent> listEvents = new ArrayList<BEvent>();

        //make sure directory exists
        if (!directory.exists()) {
            return listEvents;
        }
        return deleteFolder(directory);
    }

    public static List<BEvent> deleteFolder(final File directory)
            throws IOException {
        final List<BEvent> listEvents = new ArrayList<BEvent>();

        if (directory.isDirectory()) {

            //directory is empty, then delete it
            if (directory.list().length == 0) {

                directory.delete();
                // System.out.println("Directory is deleted : "+ directory.getAbsolutePath());

            } else {

                //list all the directory contents
                final String files[] = directory.list();

                for (final String temp : files) {
                    //construct the file structure
                    final File fileDelete = new File(directory, temp);

                    //recursive delete
                    listEvents.addAll(deleteFolder(fileDelete));
                }

                //check the directory again, if empty then delete it
                if (directory.list().length == 0) {
                    directory.delete();
                    // System.out.println("Directory is deleted : " + directory.getAbsolutePath());
                }
            }

        } else {
            //if file, then delete it
            if (!directory.delete())
            {
                listEvents.add(new BEvent(EventErrorDeletionDirectory, directory.getAbsolutePath()));
                // System.out.println("File is deleted : " + directory.getAbsolutePath());
            }
        }
        return listEvents;
    }

}
