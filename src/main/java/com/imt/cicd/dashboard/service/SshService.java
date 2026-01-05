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
        session.setConfig("StrictHostKeyChecking", "no");   // Attention en prod, mais OK pour TP
        session.connect();

        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);

        // Récupération des logs distants
        InputStream in = channel.getInputStream();
        channel.connect();

        // Lire le flux 'in' et l'ajouter aux logs de 'execution' (similaire à CommandService)
        // ... implémentation lecture flux ...

        channel.disconnect();
        session.disconnect();
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
