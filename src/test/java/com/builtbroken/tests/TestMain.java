package com.builtbroken.tests;

import com.builtbroken.updater.Main;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
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
        List<File> files = Main.findFiles(new File(".", "src/test/resources/test_files/search/"), "file1.txt");
        assertEquals(4, files.size());
    }
}
