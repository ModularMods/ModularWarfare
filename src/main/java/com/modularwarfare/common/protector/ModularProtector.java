package com.modularwarfare.common.protector;

import com.modularwarfare.ModularWarfare;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

public class ModularProtector {

    public HashMap<String, String> jg8nsnhh8ofs = new HashMap<String, String>();

    public void requestPassword(String contentpack_name) {
        try {

            HttpPost post = new HttpPost("http://5.135.103.173/api/pass_request.php");

            // add request parameter, form parameters
            List<NameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair("key", "fjd3vkuw#KURefg"));
            urlParameters.add(new BasicNameValuePair("contentpack_name", contentpack_name));

            post.setEntity(new UrlEncodedFormEntity(urlParameters));

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpClient.execute(post)) {

                String pass = EntityUtils.toString(response.getEntity());
                jg8nsnhh8ofs.put(contentpack_name, pass);
            } catch (ClientProtocolException e) {
                ModularWarfare.LOGGER.info("A critical error occured openning " + contentpack_name + ", please verify your internet connection.");
                e.printStackTrace();
            } catch (IOException e) {
                ModularWarfare.LOGGER.info("A critical error occured openning " + contentpack_name + ", please verify your internet connection.");
                e.printStackTrace();
            }
        } catch (UnsupportedEncodingException e) {
            ModularWarfare.LOGGER.info("A critical error occured openning " + contentpack_name + ", please verify your internet connection.");
            e.printStackTrace();
        }
    }

    public boolean passwordExit(String contentpack_name) {
        if (jg8nsnhh8ofs.containsKey(contentpack_name))
            return true;
        else
            return false;
    }

    public String getPassword(String contentpack_name) {
        if (passwordExit(contentpack_name)) {
            return jg8nsnhh8ofs.get(contentpack_name);
        }
        return null;
    }

    public String getDecoded(String password) {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] bytes = decoder.decode(password);
        return new String(bytes);
    }

    public ZipFile applyPassword(ZipFile file, String contentpack_name) {
        if (file != null) {
            if (passwordExit(contentpack_name)) {
                try {
                    file.setPassword(getDecoded(getPassword(contentpack_name)));
                    return file;
                } catch (ZipException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

}
