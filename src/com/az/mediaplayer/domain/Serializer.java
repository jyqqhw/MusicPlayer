package com.az.mediaplayer.domain;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlSerializer;

import android.os.Environment;
import android.util.Xml;

public class Serializer {
	/*
	 * 第一种方式序列化
	 */
	public static void Serialize(List<Map<String, Object>> list){
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		sb.append("<MusicInfos>");
		for(int i = 0;i < list.size();i++){
			Map<String, Object> map = list.get(i);
			sb.append("<MusicInfo>");

			sb.append("<name>");
			sb.append(map.get("name").toString());
			sb.append("</name>");

			sb.append("<path>");
			sb.append(map.get("path").toString());
			sb.append("</path>");

			sb.append("</MusicInfo>");
		}
		sb.append("</MusicInfos>");
		File file = new File(Environment.getExternalStorageDirectory()+File.separator+ "MediaPlayer","music_list.xml");
		try {
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(sb.toString().getBytes("utf-8"));
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	/*
	 * 第二种方法序列化
	 */
	public static void XmlSerialize(List<Map<String, Object>> list){
		XmlSerializer xmlSerializer = Xml.newSerializer();
		File file = new File(Environment.getExternalStorageDirectory()+File.separator+ "MediaPlayer","music_list.xml");
		try {
			FileOutputStream fos = new FileOutputStream(file);
			xmlSerializer.setOutput(fos, "utf-8");
			xmlSerializer.startDocument("utf-8", true);
			xmlSerializer.startTag(null, "MusicInfos");
			for(int i = 0; i < list.size(); i++){
				Map<String, Object> map = list.get(i);
				xmlSerializer.startTag(null, "MusicInfo");
				xmlSerializer.attribute(null, "id", i+1+"");

				xmlSerializer.startTag(null, "name");
				xmlSerializer.text(map.get("name").toString());
				xmlSerializer.endTag(null, "name");

				xmlSerializer.startTag(null, "path");
				xmlSerializer.text(map.get("path").toString());
				xmlSerializer.endTag(null, "path");

				xmlSerializer.endTag(null, "MusicInfo");
			}
			xmlSerializer.endTag(null, "MusicInfos");
			xmlSerializer.endDocument();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}


	}


}
