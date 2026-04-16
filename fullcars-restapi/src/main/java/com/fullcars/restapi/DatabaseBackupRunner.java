package com.fullcars.restapi;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.oauth.DbxCredential;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;

import okhttp3.OkHttpClient;

@Component
public class DatabaseBackupRunner implements ApplicationRunner {

    private final Environment env;
    private static OkHttpClient httpClient;

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
        String pgDumpPath = env.getProperty("spring.datasource.pgdump-path");
        String url = env.getProperty("spring.datasource.url"); 
        String dbName = url.substring(url.lastIndexOf("/") + 1);

        //String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        //String backupFileName = "fullCarsBackup_" + timestamp + ".sql";
        //String localBackupPath = "C:\\SoftwareFullCars\\" + backupFileName;
        String baseBackupPath = "C:\\SoftwareFullCars\\BACKUPfullCarsSoftwareDataBase.sql";

        String command = String.format("\"%s\" -U %s -F c -b -v -f \"%s\" %s", pgDumpPath, user, baseBackupPath, dbName);

        ProcessBuilder pb = new ProcessBuilder("cmd", "/c", command);
        pb.environment().put("PGPASSWORD", password);

        Process process = pb.start();

        try (BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = stdError.readLine()) != null) 
                System.err.println(line);
        }

        int exitCode = process.waitFor();
        if (exitCode == 0) {
            System.out.println("Backup creado en: " + baseBackupPath);
            
            //Files.copy(Paths.get(baseBackupPath), Paths.get(localBackupPath));
            uploadToDropbox(baseBackupPath, "BACKUPfullCarsSoftwareDataBase.sql");
            
        } else {
            System.err.println("Error creando backup, código de salida: " + exitCode);
        }
    }

	 private void uploadToDropbox(String filePath, String fileName) {
		 //System.setProperty("https.protocols", "TLSv1.2,TLSv1.3");
		 //System.setProperty("jdk.tls.client.protocols", "TLSv1.2,TLSv1.3");
		    
		 String refreshToken = env.getProperty("dropbox.refresh-token");
	     String appKey = env.getProperty("dropbox.app-key");
	     String appSecret = env.getProperty("dropbox.app-secret");
	
	     if (refreshToken == null || appKey == null || appSecret == null) {
	         System.err.println("Faltan credenciales de Dropbox");
	         return;
	     }
	
	  DbxRequestConfig config = DbxRequestConfig.newBuilder("fullcars-backup-app")
	      .withHttpRequestor(new OkHttp3Requestor(new okhttp3.OkHttpClient()))
	      .build();

	  DbxCredential credential = new DbxCredential("", 0L, refreshToken, appKey, appSecret);
	  DbxClientV2 client = new DbxClientV2(config, credential);
	  
	     try (InputStream in = new FileInputStream(filePath)) {
	         System.out.println("Iniciando subida con Refresh Token...");
	         
	         FileMetadata metadata = client.files().uploadBuilder("/" + fileName)
	                 .withMode(WriteMode.OVERWRITE)
	                 .uploadAndFinish(in);
	         
	         System.out.println("Backup subido con éxito: " + metadata.getPathDisplay());
	
	     } catch (Exception e) {
	         System.err.println("Error al subir: " + e.getMessage());
	         e.printStackTrace();
	     }
	 }
}