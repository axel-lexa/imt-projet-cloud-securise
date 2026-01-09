package com.imt.cicd.dashboard.service;

import com.imt.cicd.dashboard.model.PipelineExecution;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;

@Service
public class SshService {

    @Value("${vm.host}")
    private String host;

    @Value("${vm.user}")
    private String user;

    @Value("${vm.private-key-path}")
    private String privateKeyPath;

    public void executeRemoteCommand(String command, PipelineExecution execution) throws Exception {
        JSch jsch = new JSch();
        jsch.addIdentity(privateKeyPath);

        Session session = jsch.getSession(user, host, 22);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);

        InputStream in = channel.getInputStream();
        InputStream err = channel.getErrStream(); // Important pour voir les erreurs Docker !

        channel.connect();

        // Lecture des logs en boucle tant que la commande tourne
        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) break;
                execution.appendLog(new String(tmp, 0, i));
            }
            while (err.available() > 0) {
                int i = err.read(tmp, 0, 1024);
                if (i < 0) break;
                execution.appendLog("ERREUR: " + new String(tmp, 0, i));
            }
            if (channel.isClosed()) {
                if (in.available() > 0) continue;
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception ee) {
            }
        }

        int exitCode = channel.getExitStatus();
        channel.disconnect();
        session.disconnect();

        if (exitCode != 0) {
            throw new RuntimeException("La commande SSH a échoué avec le code : " + exitCode);
        }
    }

    // Méthode pour transférer le docker-compose.yml via SFTP (ChannelSftp)
    public void transferFile(File localFile, String remotePath) throws Exception {
        JSch jsch = new JSch();
        jsch.addIdentity(privateKeyPath);
        Session session = jsch.getSession(user, host, 22);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();

        // Transfert du fichier
        channelSftp.put(localFile.getAbsolutePath(), remotePath);

        channelSftp.disconnect();
        session.disconnect();
    }

    public String getUser() {
        return this.user;
    }
}
