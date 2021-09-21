package com.example.myavaudiorecorder.ViewCotroller;


import android.util.Log;

import com.example.myavaudiorecorder.MainActivity;
import com.example.myavaudiorecorder.Model.Info;
import com.example.myavaudiorecorder.Model.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpRequest {

    //拿到token
    public static String getToken(String url) throws IOException {
        //设置获取Token需要的参数
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("grant_type","client_credentials")
                .add("client_id", Constant.API_KEY)
                .add("client_secret", Constant.Secret_Key)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
    //处理JSON to get token
    public static String processJSONForToken(String json) {
        try {
                JSONObject jsonObject = new JSONObject(json);
                String token = jsonObject.getString("access_token");
                return token;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "得不到Token";
    }
    //拿到最终返回的数据
    public static String getResult(String url, Info requestInfo) throws IOException {
        String json = toGetJSON(requestInfo);

        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , json);
        Request request = new Request.Builder()
                .url(url)
                .header("Content-Type",Constant.header)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
    //拿到封装好的Info to json
    public static String toGetJSON(Info requestInfo){

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("token",requestInfo.token);
            jsonObject.put("channel",requestInfo.channel);
            jsonObject.put("speech",requestInfo.speech);
            jsonObject.put("len",requestInfo.len);
            jsonObject.put("format",requestInfo.format);
            jsonObject.put("rate",requestInfo.rate);
            jsonObject.put("cuid",requestInfo.cuid);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "Json失败";
    }
    //将resultInfo解析json
    public static String processJSONForResult(String resultJson){

        try {
            JSONObject jsonObject = new JSONObject(resultJson);
            String result = jsonObject.getString("result");
            result = result.replace("[","");
            result = result.replace("]","");
            result =result.replace("\"","");
            return result;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "语音识别失败,请重新录音";
    }
    //计算文件的大小
    public static long getFileLen(String filePath){
        try{
            File file = new File(filePath);
            return file.length();
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }
    //处理字符串
    public static String trimSpaceTag(String str) {
        String regEx_space = "\n";//定义空格回车换行符
        Pattern p_space = Pattern.compile(regEx_space, Pattern.CASE_INSENSITIVE);
        Matcher m_space = p_space.matcher(str);
        str = m_space.replaceAll(""); // 过滤空格回车标签
        return str.trim(); // 返回文本字符串
    }




}
