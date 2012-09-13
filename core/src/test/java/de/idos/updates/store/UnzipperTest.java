package de.idos.updates.store;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class UnzipperTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Installation wrapped = mock(Installation.class);
    private Unzipper unzipper = new Unzipper(wrapped);

    @Test
    public void unzipsArchivesAndForwardsEntriesToWrapped() throws Exception {
        final File content = createContentFileForZip();
        File sourceFolder = folder.newFolder("source");
        File targetFolder = folder.newFolder("target");
        createZipFileInTemporaryFolder(sourceFolder, "iAmA.zip", content);
        unzipper.unzipAllArchivesInDirectory(sourceFolder, targetFolder);
        ArgumentCaptor<DataInVersion> captor = ArgumentCaptor.forClass(DataInVersion.class);
        verify(wrapped).addContent(captor.capture());
        DataInVersion installedData = captor.getValue();
        File checkoutFolder = folder.newFolder();
        installedData.storeIn(checkoutFolder);
        assertThatFilesAreSimilar(content, checkoutFolder.listFiles()[0]);
    }

    private void assertThatFilesAreSimilar(File content, File file) throws IOException {
        assertThat(FileUtils.contentEquals(content, file), is(true));
        assertThat(content.getName(), is(file.getName()));
    }

    private File createContentFileForZip() throws IOException {
        File stagingFolder = this.folder.newFolder();
        File example = new File(stagingFolder, "example");
        FileUtils.writeStringToFile(example, "FILECONTENT");
        return example;
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
}