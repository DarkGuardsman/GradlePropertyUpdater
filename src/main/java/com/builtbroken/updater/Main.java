package main.java.com.builtbroken.updater;

import java.io.File;
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

    public static void main(String... args)
    {
        log("Starting");

        HashMap<String, String> launchSettings = loadArgs(args);
        if (launchSettings.containsKey("foldersToSearch"))
        {
            String[] folderPaths = launchSettings.get("foldersToSearch").split(",");
            log("Args: " + folderPaths.length + " folders to search");

            if (launchSettings.containsKey("lineToReplace"))
            {
                if (launchSettings.containsKey("lineToInsert"))
                {
                    if (launchSettings.containsKey("fileToEdit"))
                    {
                        String lineToReplace = launchSettings.get("lineToReplace");
                        String lineToInsert = launchSettings.get("lineToInsert");
                        String fileToEdit = launchSettings.get("fileToEdit");

                        List<File> files = new ArrayList();

                        log("Starting search");
                        for (String folder : folderPaths)
                        {
                            List<File> foundFiles = findFiles(folder, fileToEdit);
                            log("\tFound " + foundFiles.size() + " files in " + folder);
                            files.addAll(foundFiles);
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
            throw new RuntimeException("Missing Folders to search. Add 'foldersToSearch=file,file2,file3' to your launch arguments, where file is your designed search folder.");
        }

        log("Exiting");
    }

    public static List<File> findFiles(String path, String name)
    {
        List<File> files = new ArrayList();

        File folder = new File(path);
        findFiles(folder, files, name, 0);

        return files;
    }

    public static void findFiles(File folder, List<File> files, String name, int depth)
    {
        if (folder.exists() && folder.isDirectory())
        {
            for (File file : folder.listFiles())
            {
                if (file.isDirectory() && depth < maxSearchDepth)
                {
                    findFiles(folder, files, name, depth + 1);
                }
                else if (file.getName().equalsIgnoreCase(name))
                {
                    files.add(file);
                }
            }
        }
    }

    public static void log(String msg)
    {
        System.out.println("[GradlePropertyUpdater] " + msg);
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
