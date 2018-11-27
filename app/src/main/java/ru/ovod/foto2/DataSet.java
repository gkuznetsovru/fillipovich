package ru.ovod.foto2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class DataSet {

    // объявим адрес доступа к PHP json, который обрабатывае запросы
    String url_for_web = "https://smit.ovod.ru/upload/json.php";

    // результат выборки с сервера в формате JSONArray
    JSONArray data = null;

    // Текущая строка. Использует для функции FieldByName
    JSONObject row = null;

    // конструктор класса
    public DataSet () {

    }

    // возвращает количество записей
    public Integer RecordCount()
    {
        try {
            return data.length();
        }
         catch (Exception e) {
        return 0;
        }
    }


    // функция получает строку массива JSON по номеру
    public Boolean GetRowByNumber(Integer i)
    {
        Boolean res=Boolean.FALSE;

        try {
            row = data.getJSONObject(i);
            if (row.length()>0)
               {
                res = Boolean.TRUE;
               }
        } catch (JSONException e)
        {
            res = Boolean.FALSE;
        }
        return res;
    }

    // получени значие из текущей строки по имени
    public String FieldByName_AsString(String nm)
    {
      String res=null;
      if (row.length()>0)
       {

        try {
            res = row.getString(nm);
            }
         catch (JSONException e)
            {
            res = null;
            }
       }
       return res;
    }

    // получени значие из текущей строки по имени
    public Integer FieldByName_AsInteger(String nm)
    {
        Integer res = null;
        if (row.length()>0)
        {

            try {
                res = row.getInt(nm);
            }
            catch (JSONException e)
            {
                res = null;
            }
        }
        return res;
    }


    // функция обрабатывает на сервере POST-запросы
    public  String postRequest( String mainUrl,HashMap<String,String> parameterList)
    {
        String response="";
        try {
            URL url = new URL(mainUrl);

            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, String> param : parameterList.entrySet())
            {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }

            byte[] postDataBytes = postData.toString().getBytes("UTF-8");




            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);

            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0; )
                sb.append((char) c);
            response = sb.toString();


            return  response;
        }catch (Exception excep){
            excep.printStackTrace();}
        return response;
    }


    // функция возвращаю JSON результат по SQL-запросу
    public void GetJSONFromWEB(String sql) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("SQL", sql);
        String s = postRequest(url_for_web, hashMap);

        // распарсим в массив
        try {
            data = new JSONArray(s);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
