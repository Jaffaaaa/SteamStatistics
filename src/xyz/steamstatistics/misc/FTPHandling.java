package xyz.steamstatistics.misc;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import xyz.steamstatistics.Core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FTPHandling {

    private Core core;
    private String ftpUser;
    private String ftpPass;
    private String server = "jaffaaaa.xyz";

    public FTPHandling(Core core, String ftpUser, String ftpPass) {
        this.core = core;
        this.ftpUser = ftpUser;
        this.ftpPass = ftpPass;
    }

    public boolean upload(File file, String name) {
        FTPClient ftpClient = new FTPClient();
        String firstRemoteFile = name;
        try {
            ftpClient.connect(server, 21);
            ftpClient.login(ftpUser, ftpPass);
            ftpClient.enterLocalPassiveMode();

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            InputStream is = new FileInputStream(file);
            boolean done = ftpClient.storeFile("steamstats/" + firstRemoteFile, is);
            if (done) {
                Logger.log("File '" + firstRemoteFile + "' uploaded successfully.", this.getClass());
            }
            return done;
        } catch (IOException e) {
            Logger.error("Could not save file '" + firstRemoteFile + "'.", this.getClass());
            return false;
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void deletePast() {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(server, 21);
            ftpClient.login(ftpUser, ftpPass);
            ftpClient.enterLocalPassiveMode();

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            for (FTPFile file : ftpClient.listFiles("steamstats/")) {
                 ftpClient.deleteFile("steamstats/" + file.getName());
            }

            Logger.log("Cleaned screenshots!", this.getClass());
        } catch (IOException ignored) {
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
