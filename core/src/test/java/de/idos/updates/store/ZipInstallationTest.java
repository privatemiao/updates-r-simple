package de.idos.updates.store;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ZipInstallationTest {
    @Rule
    public TemporaryFolder stagingFolder = new TemporaryFolder();

    private Installation wrapped = mock(Installation.class);
    private ZipInstallation installation = new ZipInstallation(wrapped);
    private DataInVersion dataInVersion = mock(DataInVersion.class);

    @Test
    public void unzipsArchivesAndForwardsEntriesToWrapped() throws Exception {
        installFromZip();
        verify(wrapped).addContent(Matchers.isA(DataInVersion.class));
    }

    @Test
    public void deletesAllTemporaryFiles() throws Exception {
        installFromZip();
        String[] temporaryDirectories = stagingFolder.getRoot().getParentFile().list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("updates-r-us");
            }
        });
        assertThat(temporaryDirectories.length, is(0));
    }

    private File createContentFileForZip() throws IOException {
        File file = stagingFolder.newFile();
        FileUtils.writeStringToFile(file, "FILECONTENT");
        return file;
    }

    private File createZipFileInTemporaryFolder(File targetFolder, String name, File content) throws IOException {
        File file = new File(targetFolder, name);
        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(file));
        zipOutputStream.putNextEntry(new ZipEntry(content.getName()));
        zipOutputStream.write(FileUtils.readFileToByteArray(content));
        zipOutputStream.closeEntry();
        zipOutputStream.close();
        file.deleteOnExit();
        return file;
    }

    private void installFromZip() throws IOException {
        final File content = createContentFileForZip();
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                File targetFolder = (File) invocation.getArguments()[0];
                createZipFileInTemporaryFolder(targetFolder, "iAmA.zip", content);
                return null;
            }
        }).when(dataInVersion).storeIn(Matchers.isA(File.class));
        installation.addContent(dataInVersion);
    }
}