package com.example.net1cloud.utils;


import com.example.net1cloud.data.Music;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;


public class SerializeAndDeserializeUtil {

    //序列化音乐
    public static String serializeMusic(Music music) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(music);
        String serializedStr = byteArrayOutputStream.toString("ISO-8859-1");
        serializedStr = java.net.URLEncoder.encode(serializedStr, "UTF-8");
        objectOutputStream.close();
        byteArrayOutputStream.close();
        return serializedStr;
    }

    //反序列化音乐
    public static Music deSerializeMusic(String str) throws IOException, ClassNotFoundException {
        String readStr = java.net.URLDecoder.decode(str, "UTF-8");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                readStr.getBytes("ISO-8859-1"));
        ObjectInputStream objectInputStream = new ObjectInputStream(
                byteArrayInputStream);
        Music music = (Music) objectInputStream.readObject();
        objectInputStream.close();
        byteArrayInputStream.close();
        return music;
    }

    //序列化音乐列表
    public static String serializeMusicPathList(List<String> musicPathList) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(musicPathList);
        String serializedStr = byteArrayOutputStream.toString("ISO-8859-1");
        serializedStr = java.net.URLEncoder.encode(serializedStr, "UTF-8");
        objectOutputStream.close();
        byteArrayOutputStream.close();
        return serializedStr;
    }

    //反序列化音乐列表
    public static List<String> deSerializeMusicPathList(String str) throws IOException, ClassNotFoundException {
        String readStr = java.net.URLDecoder.decode(str, "UTF-8");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                readStr.getBytes("ISO-8859-1"));
        ObjectInputStream objectInputStream = new ObjectInputStream(
                byteArrayInputStream);
        List<String> musicPathList = (List<String>) objectInputStream.readObject();
        objectInputStream.close();
        byteArrayInputStream.close();
        return musicPathList;
    }

}
