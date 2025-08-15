package org.rkanaje.expense.ai.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Slf4j
@Configuration
public class GoogleSheetsConfig {

    private static final String APPLICATION_NAME = "Expense Tracker";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Value("${google.sheets.credentials.json}")
    private String credentialsJsonFile;

    @Value("${google.sheets.spreadsheet.id}")
    private String spreadsheetId;

    @Bean
    public NetHttpTransport httpTransport() throws GeneralSecurityException, IOException {
        return GoogleNetHttpTransport.newTrustedTransport();
    }

    @Bean
    public Credential credential() throws IOException, GeneralSecurityException {

        // read the credentials.json file
        final String credentialsJson = new String(Files.readAllBytes(Paths.get(credentialsJsonFile)));
        try (InputStream credentialsStream = new ByteArrayInputStream(credentialsJson.getBytes())) {
            return GoogleCredential.fromStream(credentialsStream)
                    .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));
        }
    }

    @Bean
    public Sheets sheetsService(NetHttpTransport httpTransport, Credential credential) {
        return new Sheets.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    @Bean
    public String spreadsheetId() {
        return spreadsheetId;
    }
}
