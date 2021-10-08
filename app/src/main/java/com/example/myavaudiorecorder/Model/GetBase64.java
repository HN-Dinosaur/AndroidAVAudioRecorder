package com.example.myavaudiorecorder.Model;

import android.os.FileUtils;
import android.util.Base64;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class GetBase64 {
        /**
         * encodeBase64File:(将文件转成base64 字符串).
         * @param path 文件路径
         * @return
         * @throws Exception

         */



        public static String encodeBase64File(String path) throws Exception {
            if (path == null) {
                return null;
            }
            try {
                byte[] b = Files.readAllBytes(Paths.get(path));
                return Base64.encodeToString(b, Base64.DEFAULT);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
        /**
         * decoderBase64File:(将base64字符解码保存文件).

         * @param base64Code 编码后的字串
         * @param savePath  文件保存路径
         * @throws Exception

         */
        public static void decoderBase64File(String base64Code,String savePath) throws Exception {

            byte[] buffer = Base64.decode(base64Code, Base64.DEFAULT);
            FileOutputStream out = new FileOutputStream(savePath);
            out.write(buffer);
            out.close();


        }




}
