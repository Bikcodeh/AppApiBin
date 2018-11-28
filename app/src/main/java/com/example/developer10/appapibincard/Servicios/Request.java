package com.example.developer10.appapibincard.Servicios;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class Request {

    public static String method = "POST";
    public static URL urlServer;
    public static String urlAddress;
    private static JSONObject params;


    public static String makeRequest(String method, String url, JSONObject patametros, int opcion) {

        urlAddress = url;
        params = patametros;

        if (opcion == 1)
            return getData();
        else
            return postData();
    }


    private static String getData() {

        JSONObject responseParameters = new JSONObject();
        String error = null;
        try {

            urlServer = new URL(urlAddress);
            HttpURLConnection conectionServer = (HttpURLConnection) urlServer.openConnection();
            conectionServer.setRequestMethod(method);
            conectionServer.setRequestProperty("content-type", "application/json");


            // Se establece el tiempo maximo de espera para realizar la conexion.
            //conectionServer.setConnectTimeout(timeOut);
            // Se establece el tiempo maximo para hacer la lectura.
            //conectionServer.setReadTimeout(timeOut);
            conectionServer.connect();
            InputStream inputStream = conectionServer.getInputStream();
            StringBuffer stringBuffer = new StringBuffer();

            if (inputStream == null) {
                return null;
            }

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line + "\n");
            }

            if (stringBuffer.length() == 0) {
                return null;
            }

            String forecastJsonStr = stringBuffer.toString();
            // Se cierra la conexion.
            conectionServer.disconnect();
            return forecastJsonStr;

        } catch (MalformedURLException e) {

            try {
                responseParameters.put("STATUS", "0");
                responseParameters.put("MSG", e.getMessage());
                error = responseParameters.toString();
            } catch (JSONException el) {

            }


        } catch (IOException e) {

            try {
                responseParameters.put("STATUS", "0");
                responseParameters.put("MSG", e.getMessage());
                error = responseParameters.toString();
            } catch (JSONException e1) {
                e1.printStackTrace();
            }

        }
        return error;
    }// fin get


    private static String postData() {

        String response = "";
        int responseCode;

        try {


            urlServer = new URL(urlAddress);
            // Se abre la conexion con el servidor.
            HttpURLConnection conectionServer = (HttpURLConnection) urlServer.openConnection();
            conectionServer.setRequestProperty("content-type", "application/json");
            // Se establece el tiempo maximo de espera para realizar la conexion.
            conectionServer.setConnectTimeout(4000);
            // Se establece el tiempo maximo para hacer la lectura.
            conectionServer.setReadTimeout(4000);
            // Activa el metodo post
            conectionServer.setDoOutput(true);
            // Se establece el metodo a usar.
            conectionServer.setRequestMethod(method);


            conectionServer.connect();
            String da = params.toString();
            Log.d("Daata: ", da);

            OutputStream outputStream = conectionServer.getOutputStream();

            outputStream.write(da.getBytes("UTF-8"));
            outputStream.flush();
            outputStream.close();

            responseCode = conectionServer.getResponseCode();

            Log.d("RESPONSE CODE: ", ""+responseCode);

            String line;
            if (responseCode == HttpsURLConnection.HTTP_OK) {

                BufferedReader br = new BufferedReader(new InputStreamReader(conectionServer.getInputStream())); // gets response String
                while ((line = br.readLine()) != null) {
                    response += line;
                }

            } else {
                BufferedReader brError = new BufferedReader(new InputStreamReader(conectionServer.getErrorStream())); //gets error response if different from 200
                while ((line = brError.readLine()) != null) {
                    response += line;
                }
            }// Fin responseCode

            if (responseCode == 503) {

                response = "";
            }// Fin if == 503

        }
        //Excepcion cuando el tiempo de espera de conexion caduca
        catch (java.net.SocketTimeoutException e) {
            JSONObject responseParameters = new JSONObject();
            try {

                responseParameters.put("MSG", e.getMessage());
                responseParameters.put("STATUS", 1); // Valor de 1, para saber que fue por tiempo de espera
                response = responseParameters.toString();

            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        catch (IOException e) {

            JSONObject responseParameters = new JSONObject();
            try {

                responseParameters.put("MSG", e.getMessage());
                responseParameters.put("STATUS", 0);
                response = responseParameters.toString();

            } catch (JSONException e1) {
                e1.printStackTrace();
            }

        }// Fin try/catch
        return response;

    }// Fin postData
}