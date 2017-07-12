package com.gupte.prabhas.plugins.apicaller;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Created by pgupte on 7/6/2017.
 */
public class APIClient {
    private String server;
    private String user;
    private String pass;
    private String userAgent = "Mozilla/5.0";
    private String xRequestedWith = "Jenkins Plugin";
    private final static Logger logger = Logger.getLogger(APIClient.class.getName());

    public APIClient(String server, String user, String pass) {
        this.server = server;
        this.user = user;
        this.pass = pass;
    }

    private String getBasicAuthHeader() {
        String userPass = user + ":" + pass;
        String basicAuthHeader = DatatypeConverter.printBase64Binary(userPass.getBytes());

        return basicAuthHeader;
    }
    public boolean testConnection() {
        try {
            URL url = new URL(server + "/user/test");
            HttpURLConnection conn = getConnection(url);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", userAgent);
            conn.setRequestProperty("X-Requested-With", xRequestedWith);
            String encoded = getBasicAuthHeader();
            conn.setRequestProperty("Authorization", "Basic " + encoded);
            int responseCode = conn.getResponseCode();

            if (responseCode != 200) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            logger.warning(e.getMessage());
            return false;
        }
    }

    private HttpURLConnection getConnection(URL url) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", userAgent);
            conn.setRequestProperty("X-Requested-With", xRequestedWith);
            String encoded = getBasicAuthHeader();
            conn.setRequestProperty("Authorization", "Basic " + encoded);
        } catch (Exception e) {
            logger.warning(e.getMessage());
        } finally {
            return conn;
        }
    }

    public String getImageScanResult(String imageId) {
        try {
            URL url = new URL(server + "/data/list");
            // HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            HttpURLConnection conn = getConnection(url);
            conn.setRequestMethod("GET");
            // int responseCode = conn.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            return response.toString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
