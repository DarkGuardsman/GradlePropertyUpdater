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

    public static List<File> findFiles(String path, String name)
    {
        return findFiles(new File(path), name);
    }

    public static List<File> findFiles(File folder, String name)
    {
        List<File> files = new ArrayList();
        findFiles(folder, files, name, 0);
        return files;
    }

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

    public static void editFile(File file, String replace, String insert)
    {
        List<String> lines = new ArrayList<String>();
        String line = null;
        try
        {
            //Load file
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            while ((line = br.readLine()) != null)
            {
                //Check if should replace
                if (line.contains(replace)) //TODO add regex support
                {
                    //Replace
                    line = insert;
                }
                lines.add(line);
            }
            fr.close();
            br.close();

            //Write file
            FileWriter fw = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(fw);
            for (String s : lines)
            {
                out.write(s);
            }
            out.flush();
            out.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
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
