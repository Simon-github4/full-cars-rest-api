package com.fullcars.restapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class DatabaseBackupRunner implements ApplicationRunner {

    private final Environment env;

    public DatabaseBackupRunner(Environment env) {
        this.env = env;
    }
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        backupDatabase();
    }

    private void backupDatabase() throws IOException, InterruptedException {
        String user = env.getProperty("spring.datasource.username");
        String password = env.getProperty("spring.datasource.password");
        // Ajusta la ruta de pg_dump si no está en el PATH
        String pgDumpPath = env.getProperty("spring.datasource.pgdump-path");
        String url = env.getProperty("spring.datasource.url"); 
        String dbName = url.substring(url.lastIndexOf("/") + 1);

        String backupPath = System.getProperty("user.home") + "\\BACKUPfullCarsSoftwareDataBase.sql";

        String command = String.format("\"%s\" -U %s -F c -b -v -f \"%s\" %s", pgDumpPath, user, backupPath, dbName);

        ProcessBuilder pb = new ProcessBuilder("cmd", "/c", command);
        pb.environment().put("PGPASSWORD", password);

        Process process = pb.start();

        try (BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = stdError.readLine()) != null) 
                System.err.println(line);
        }

        int exitCode = process.waitFor();
        if (exitCode == 0) 
            System.out.println("Backup creado en: " + backupPath);
        else 
            System.err.println("Error creando backup, código de salida: " + exitCode);
    }
	
}

