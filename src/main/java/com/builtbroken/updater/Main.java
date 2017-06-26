package com.builtbroken.updater;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 6/18/2017.
 */
public class Main
{
    public static int maxSearchDepth = 10;
    public static List<String> foldersToIgnore = new ArrayList();

    public static void main(String... args)
    {
        log("Starting");

        HashMap<String, String> launchSettings = loadArgs(args);
        if (launchSettings.containsKey("foldersToSearch"))
        {
            if (launchSettings.containsKey("lineToReplace"))
            {
                if (launchSettings.containsKey("lineToInsert"))
                {
                    if (launchSettings.containsKey("fileToEdit"))
                    {
                        String[] folderPaths = launchSettings.get("foldersToSearch").split(",");
                        String lineToReplace = launchSettings.get("lineToReplace");
                        String lineToInsert = launchSettings.get("lineToInsert");
                        String fileToEdit = launchSettings.get("fileToEdit");

                        log("-----------------------------------");
                        log("Args: ");
                        log("\tFolders: " + folderPaths.length);
                        log("\tFile: " + fileToEdit);
                        log("\tReplace: " + lineToReplace);
                        log("\tInsert: " + lineToInsert);
                        log("-----------------------------------");

                        //TODO add args to add more folders to list
                        foldersToIgnore.add("build");
                        foldersToIgnore.add(".git");
                        foldersToIgnore.add(".gradle");
                        foldersToIgnore.add("src");
                        foldersToIgnore.add("output");
                        foldersToIgnore.add("out");

                        List<File> files = new ArrayList();

                        log("Starting search");
                        for (String folder : folderPaths)
                        {
                            List<File> foundFiles = findFiles(folder, fileToEdit);
                            log("\tFound " + foundFiles.size() + " files in " + folder);
                            files.addAll(foundFiles);
                        }
                        log("-----------------------------------");

                        log("Done: found " + files.size() + " potential files to edit");

                        for (File file : files)
                        {
                            log("\t " + file);
                        }
                        log("-----------------------------------");

                        log("Starting edits");
                        List<File> editedFiles = new ArrayList();
                        for (File file : files)
                        {
                            if (editFile(file, lineToReplace, lineToInsert))
                            {
                                editedFiles.add(file);
                            }
                        }
                        log("Done: modified " + editedFiles.size() + " files.");
                        log("-----------------------------------");

                        if (launchSettings.containsKey("doGitCommit"))
                        {
                            boolean push = launchSettings.containsKey("doGitPush");
                            String message = launchSettings.get("gitCommitMessage");
                            if (message == null)
                            {
                                message = "Updated file: %fileName%"; //TODO add line change notes
                            }

                            for (File file : editedFiles)
                            {
                                File gitFolder = new File(file.getParentFile(), ".git");
                                if (gitFolder.exists())
                                {
                                    //https://git-scm.com/book/be/v2/Embedding-Git-in-your-Applications-JGit

                                    //git status
                                    // parse output to see if our file exists and is marked as changed

                                    //git add fileName
                                    //git commit -m "Automation: Updated VoltzEngine version # to 1.7.0"
                                    //git push
                                    if (runProcess(file.getParentFile(), "git add " + file.getName()))
                                    {
                                        String m = message.replace("%fileName%", file.getName());
                                        if (runProcess(file.getParentFile(), "git commit -m \"" + m + "\""))
                                        {
                                            if (push)
                                            {
                                                if (!runProcess(file.getParentFile(), "git push"))
                                                {
                                                    //TODO reset git branch to prevent issues
                                                }
                                            }
                                        }
                                        else
                                        {
                                            //TODO reset git branch to prevent issues
                                        }
                                    }
                                }
                                else
                                {
                                    log("Error: File '" + file + "' is not contained in a folder with a .git to allow for syncing.");
                                }
                            }
                        }
                    }
                    else
                    {
                        throw new RuntimeException("Missing file to edit. Add 'fileToEdit=fileName.txt' to your launch arguments, where fileName.txt is the desired file to edit.");
                    }
                }
                else
                {
                    throw new RuntimeException("Missing line to insert. Add 'lineToInsert=someLineInAFile' to your launch arguments, where someLineInAFile is the line to use during replacement.");
                }
            }
            else
            {
                throw new RuntimeException("Missing line to replace. Add 'lineToReplace=someLineInAFile' to your launch arguments, where someLineInAFile is the desired search key.");
            }
        }
        else
        {
            throw new RuntimeException("Missing Folders to search. Add 'foldersToSearch=file,file2,file3' to your launch arguments, where file is your desired search folder.");
        }

        log("Exiting");
    }

    public static boolean runProcess(File run, String process)
    {
        try
        {
            log("Running command: " + process);
            Process p = Runtime.getRuntime().exec(process, null, run);

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));

            String s = null;

            // read the output from the command
            log("-----------------------------------");
            log("Output:\n");
            while ((s = stdInput.readLine()) != null)
            {
                log(s);
            }
            log("-----------------------------------");

            // read any errors from the attempted command
            log("Error:\n");
            while ((s = stdError.readLine()) != null)
            {
                log(s);
            }
            log("-----------------------------------");
        }
        catch (Exception e)
        {
            log("Failed to run command");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Called to locate all files with the provided name
     *
     * @param path
     * @param name
     */
    public static List<File> findFiles(String path, String name)
    {
        return findFiles(new File(path), name);
    }

    /**
     * Called to locate all files with the provided name
     *
     * @param folder
     * @param name
     */
    public static List<File> findFiles(File folder, String name)
    {
        List<File> files = new ArrayList();
        findFiles(folder, files, name, 0);
        return files;
    }

    /**
     * Called to locate all files with the provided name
     *
     * @param folder
     * @param files
     * @param name
     * @param depth
     */
    public static void findFiles(File folder, List<File> files, String name, int depth)
    {
        if (folder.exists() && folder.isDirectory())
        {
            String spacer = "";
            for (int i = 0; i <= depth; i++)
            {
                spacer += "\t";
            }
            log(spacer + "Looking at folder: " + folder);
            for (File file : folder.listFiles())
            {
                if (file.isDirectory() && depth < maxSearchDepth && !foldersToIgnore.contains(file.getName()))
                {
                    findFiles(file, files, name, depth + 1);
                }
                else if (file.getName().equalsIgnoreCase(name))
                {
                    files.add(file);
                }
            }
        }
        else
        {
            log("Error: folder '" + folder + "' does not exist or is not a directory valid for searching.");
        }
    }

    public static void log(String msg)
    {
        System.out.println("[GradlePropertyUpdater] " + msg);
    }

    /**
     * Called to edit a file to replace the given line
     *
     * @param file
     * @param replace
     * @param insert
     */
    public static boolean editFile(File file, String replace, String insert)
    {
        boolean edited = false;
        try
        {
            //Load file
            List<String> lines = readFile(file);

            //Replace lines
            for (int i = 0; i < lines.size(); i++)
            {
                final String line = lines.get(i);
                if (line.contains(replace))
                {
                    lines.set(i, insert);
                    log("Replacing line in file: " + file);
                    edited = true;
                }
            }

            //Write file
            writeFile(file, lines);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return edited;
    }

    /**
     * Loads the file as a list of strings
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static List<String> readFile(File file) throws IOException
    {
        final List<String> lines = new ArrayList<String>();


        final FileReader fr = new FileReader(file);
        final BufferedReader br = new BufferedReader(fr);

        String line;
        while ((line = br.readLine()) != null)
        {
            lines.add(line);
        }

        fr.close();
        br.close();

        return lines;
    }

    /**
     * Writes a series of lines to a file
     *
     * @param file
     * @param lines
     * @throws IOException
     */
    public static void writeFile(File file, List<String> lines)
    {
        //Backup file in case of errors
        File bak = new File(file.getPath(), file.getName() + "_bak");
        if (file.exists())
        {
            file.renameTo(bak);
        }

        String newLine = System.getProperty("line.separator");
        //Write to file
        try
        {
            //Write file
            FileWriter fw = new FileWriter(file);
            for (int i = 0; i < lines.size(); i++)
            {
                String s = lines.get(i);
                fw.write(s + ((i - 1) < lines.size() ? newLine : ""));
            }
            fw.flush();
            fw.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        //Remove temporary backup file
        bak.delete();
    }

    /**
     * Converts arguments into a hashmap for usage
     *
     * @param args
     * @return
     */
    public static HashMap<String, String> loadArgs(String... args)
    {
        final HashMap<String, String> map = new HashMap();
        if (args != null)
        {
            String currentArg = null;
            String currentValue = "";
            for (int i = 0; i < args.length; i++)
            {
                String next = args[i].trim();
                if (next == null)
                {
                    throw new IllegalArgumentException("Null argument detected in launch arguments");
                }
                else if (next.startsWith("-"))
                {
                    if (currentArg != null)
                    {
                        map.put(currentArg, currentValue);
                        currentValue = "";
                    }

                    if (next.contains("="))
                    {
                        String[] split = next.split("=");
                        currentArg = split[0].substring(1).trim();
                        currentValue = split[1].trim();
                        if (split.length > 2)
                        {
                            for (int l = 2; l < split.length; l++)
                            {
                                currentValue += "=" + split[l];
                            }
                        }
                    }
                    else
                    {
                        currentArg = next.substring(1).trim();
                    }
                }
                else if (currentArg != null)
                {
                    if (!currentValue.isEmpty())
                    {
                        currentValue += ",";
                    }
                    currentValue += next.replace("\"", "").replace("'", "").trim();
                }
                else
                {
                    throw new IllegalArgumentException("Value has no argument associated with it [" + next + "]");
                }
            }
            //Add the last loaded value to the map
            if (currentArg != null)
            {
                map.put(currentArg, currentValue);
            }
        }
        return map;
    }
}
