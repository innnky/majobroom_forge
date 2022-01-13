package com.innky.majobroom.utills;

import com.google.gson.Gson;
import com.innky.majobroom.MajoBroom;
import com.innky.majobroom.jsonbean.GeomtryBean;
import com.innky.majobroom.jsonbean.ModelBean;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;

public class ModelJsonReader {
    public static GeomtryBean readJson(String path){
        ResourceManager resourceManager = Minecraft.getInstance()
                .getResourceManager();
//            URL a  = MajoBroom.class.getClassLoader().getResource("/assets/majobroom/textures/entity");
//
//            File file = new File(String.valueOf(a));
//            String[] asa = file.list();

        try {
//            InputStreamReader io = new InputStreamReader( Minecraft.getInstance()
//                    .getResourceManager()
//                    .getResource( new ResourceLocation( "majobroom", path ) )
//                    .getInputStream());
            InputStream in = MajoBroom.class.getClassLoader().getResourceAsStream("/assets/majobroom/"+path+".json");


            if (in!=null){
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                StringBuffer stringBuffer = new StringBuffer();
                String temp = "";

                while ((temp = bufferedReader.readLine()) != null) {
                    stringBuffer.append(temp);
                }
                String presonsString = stringBuffer.toString();
                Gson gson = new Gson();
                ModelBean fromJson = gson.fromJson(presonsString,
                        ModelBean.class);

                return fromJson.getModel();
            }

//        InputStream in = MajoBroom.class.getClassLoader().getResourceAsStream("/assets/majobroom/textures/entity/a.json");
//        InputStreamReader io = new InputStreamReader(in);
//        BufferedReader bufferedReader = new BufferedReader(io);
//        StringBuffer stringBuffer = new StringBuffer();
//        String temp = "";
//
//            while ((temp = bufferedReader.readLine()) != null) {
//                stringBuffer.append(temp);
//            }
//            String presonsString = stringBuffer.toString();
//            Gson gson = new Gson();
//            ModelBean fromJson = gson.fromJson(presonsString,
//                    ModelBean.class);
//
//            return fromJson.getModel();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
