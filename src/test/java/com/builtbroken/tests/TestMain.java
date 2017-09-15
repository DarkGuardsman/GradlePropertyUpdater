package com.builtbroken.tests;

import com.builtbroken.updater.Main;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.*;
import java.util.List;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 6/26/2017.
 */
public class TestMain extends TestCase
{
    @Test
    public void testSearch()
    {
        final File testFiles = new File(".", "src/test/resources/test_files/search/"); //TODO fix to get from resources
        if(testFiles.exists())
        {
            List<File> files = Main.findFiles(testFiles, "file1.txt");
            assertEquals(4, files.size());
            for (File file : files)
            {
                assertEquals("file1.txt", file.getName());
            }
        }
    }

    @Test
    public void testEdit() throws Exception
    {
        //Create folder
        File workFolder = new File(".", "run");
        workFolder.mkdirs();

        //Create file
        File file = new File(".", "run/file.txt");
        if (file.exists())
        {
            file.delete();
        }

        FileWriter fw = new FileWriter(file);
        String newLine = System.getProperty("line.separator");
        fw.write("line 1" + newLine);
        fw.write("line 2" + newLine);
        fw.write("line 3" + newLine);
        fw.write("line 4" + newLine);
        fw.write("line edit = edit" + newLine);
        fw.write("line 5" + newLine);
        fw.write("line 6" + newLine);
        fw.flush();
        fw.close();

        Main.editFile(file, "line edit =", "line edit = cheese");


        ///Read file and test each line
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        assertEquals("line 1", br.readLine());
        assertEquals("line 2", br.readLine());
        assertEquals("line 3", br.readLine());
        assertEquals("line 4", br.readLine());
        assertEquals("line edit = cheese", br.readLine());
        assertEquals("line 5", br.readLine());
        assertEquals("line 6", br.readLine());
        fr.close();
        br.close();

        //Cleanup
        file.delete();
        workFolder.delete();
    }
}
