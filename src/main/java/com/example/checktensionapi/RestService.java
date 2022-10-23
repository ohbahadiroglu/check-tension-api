package com.example.checktensionapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;


@Service
public class RestService {
    private final RestTemplate restTemplate;
    private boolean mailAlreadySent;
    @Autowired
    private Environment env;

    public RestService(RestTemplateBuilder rstb,@Value("${customprop.timeout}")
        final int timeout) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException{
        // set connection and read timeouts

        this.restTemplate = rstb.build();
        this.restTemplate.setRequestFactory(getRequestFactory(timeout));

    }
    public HttpComponentsClientHttpRequestFactory getRequestFactory(int timeout) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        TrustStrategy acceptingTrustStrategy = (x509Certificates, s) -> true;
        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);
        return requestFactory;
    }

    private void sendmail() throws MessagingException, IOException, InterruptedException {
        // mail içeriğine eklenecek bilgilerin derlenmesi
        Runtime run = Runtime.getRuntime();
        String[] cmd = {"/bin/sh",
                        "-c",
                        "ps -aux | grep 'jar tension.1.0.0.jar' | grep -v grep"} ;
        String cmd2 = "free -m";
        Process pr = run.exec(cmd);
        pr.waitFor();
        Process pr2 = run.exec(cmd2);
        pr2.waitFor();
        BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        BufferedReader buf2 = new BufferedReader(new InputStreamReader(pr2.getInputStream()));
        StringBuilder htmlMailContent = new StringBuilder();

        String tensionProcess = "";
        if (buf.readLine() != null){
            tensionProcess = "processing";
            htmlMailContent.append(String.format("<h2>Mail sent due to %s ms timeout. (Tension is proccessing but not responding)</h2>",env.getProperty("customprop.timeout")));
        }else{
            tensionProcess = "notProcessing";
            htmlMailContent.append("<h2>Mail sent due to Tension is not processing</h2>");
        }

        String line;
        String[] tokens = new String[0];
        while ((line=buf2.readLine())!=null) {
            if (line.toLowerCase().startsWith("mem")){
                line = line.trim().replaceAll(" +", " ");
                tokens = line.split(" ");
            }
        }

        htmlMailContent.append(
                "<html>\n" +
                "<head><html>\n" +
                "<style>\n" +
                "table {\n" +
                "  font-family: arial, sans-serif;\n" +
                "  border-collapse: collapse;\n" +
                "  width: 100%;\n" +
                "}\n" +
                "\n" +
                "td, th {\n" +
                "  border: 1px solid #dddddd;\n" +
                "  text-align: left;\n" +
                "  padding: 8px;\n" +
                "}\n" +
                "\n" +
                "tr:nth-child(even) {\n" +
                "  background-color: #dddddd;\n" +
                "}\n" +
                "</style>\n" +
                "</head>\n" +
                "<body><html>\n" +
                "<h2>MEVCUT SUNUCU DURUMU (RAM)</h2>\n" +
                "\n" +
                "<table>\n" +
                "  <tr>\n" +
                "    <th>Total (mb)</th>\n" +
                "    <th>Used (mb)</th>\n" +
                "    <th>Free (mb)</th>\n" +
                "    <th>Shared (mb)</th>\n" +
                "    <th>Buff/Cache (mb)</th>\n" +
                "    <th>Available (mb)</th>\n" +
                "  </tr>\n" +
                "  <tr>\n" +
                "    <td>"+tokens[1]+"</td>\n" +
                "    <td>"+tokens[2]+"</td>\n" +
                "    <td>"+tokens[3]+"</td>\n" +
                "    <td>"+tokens[4]+"</td>\n" +
                "    <td>"+tokens[5]+"</td>\n" +
                "    <td>"+tokens[6]+"</td>\n" +
                "  </tr>\n" +
                "</table>\n" +
                "\n" +
                "</body>\n" +
                "</html>\n" +
                "\n");


        Properties props = new Properties();
        props.put("mail.smtp.auth", env.getProperty("customprop.mail-smpt-auth"));
        props.put("mail.smtp.starttls.enable", env.getProperty("customprop.mail-smpt-starttls-enable"));
        props.put("mail.smtp.host", env.getProperty("customprop.mail-smpt-host"));
        props.put("mail.smtp.port", env.getProperty("customprop.mail-smpt-port"));

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(env.getProperty("customprop.mail-from"), env.getProperty("customprop.session-password"));
            }
        });
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(env.getProperty("customprop.mail-from"), false));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(env.getProperty("customprop.mail-to")));
        msg.setSubject("TENSION ALARM");
        //System.out.println(output);
        msg.setContent(htmlMailContent.toString(), "text/html");
        msg.setSentDate(new Date());
        Transport.send(msg);
        System.out.println("mail sent successfully");
    }

    public TensItem createPost() throws MessagingException, IOException, InterruptedException {
        //
        String url = env.getProperty("customprop.url");

        // create headers
        HttpHeaders headers = new HttpHeaders();
        // set `content-type` header
        headers.setContentType(MediaType.APPLICATION_JSON);
        // create a map for post parameters
        Map<String, Object> map = new HashMap<>();
        map.put("id", "1234");
        map.put("date", "210620221222");
        map.put("content", "Aldığım hizmeti hiç beğenmedim");
        map.put("from", "ohbahadiroglu@gmail.com");
        map.put("subject", "Berbat hizmet");
        // build the request
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);

        // send POST request
        try {
            ResponseEntity<TensItem> response = this.restTemplate.postForEntity(url, entity, TensItem.class);
            this.mailAlreadySent = false;
            return response.getBody();
        } catch (Exception e) {
            //System.out.println(e.getMessage());
            if ( !(this.mailAlreadySent) ) {
                sendmail();
                this.mailAlreadySent = true;
            }
            return null;
        }
    }
}

