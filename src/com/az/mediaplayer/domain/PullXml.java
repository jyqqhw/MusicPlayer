package com.az.mediaplayer.domain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.os.Environment;
import android.util.Xml;

public class PullXml {
	/*
	 * 解析所有歌曲名称和绝对路径
	 */
	public static List<Map<String,Object>> PullList(){
		List<Map<String,Object>> list = null;
		Map<String, Object> map = null;
		XmlPullParser  xmlPullParser = Xml.newPullParser();
		File file = new File(Environment.getExternalStorageDirectory()+File.separator+ "MediaPlayer",
					"music_list.xml");
		try {
			FileInputStream fis = new FileInputStream(file);
			xmlPullParser.setInput(fis, "utf-8");
			int type = xmlPullParser.getEventType();
			while(type != XmlPullParser.END_DOCUMENT){
				switch (type) {
				case XmlPullParser.START_TAG:
					if("MusicInfos".equals(xmlPullParser.getName())){
						list = new ArrayList<Map<String,Object>>();
					}else if("MusicInfo".equals(xmlPullParser.getName())){
						map = new HashMap<String, Object>();
					}else if("name".equals(xmlPullParser.getName())){
						map.put("name", xmlPullParser.nextText());
					}else if("path".equals(xmlPullParser.getName())){
						map.put("path", xmlPullParser.nextText());
					}
					break;
				case XmlPullParser.END_TAG:
					if("MusicInfo".equals(xmlPullParser.getName())){
						list.add(map);
						map = null;
					}
					break;
				default:
					break;
				}
				type = xmlPullParser.next();
			}
			fis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}
	/*
	 * 解析所有歌曲的名称
	 */
	public static List<String> PullName(){
		List<String> list = new ArrayList<String>();
		XmlPullParser xmlPullParser = Xml.newPullParser();
		File file = new File(Environment.getExternalStorageDirectory()+File.separator+"MediaPlayer",
				"music_list.xml");
		try {
			FileInputStream fis = new FileInputStream(file);
			xmlPullParser.setInput(fis, "utf-8");
			int type = xmlPullParser.getEventType();
			while(type != XmlPullParser.END_DOCUMENT){
					if(type == XmlPullParser.START_TAG&&"name".equals(xmlPullParser.getName())){
						list.add(xmlPullParser.nextText());
					}
				type = xmlPullParser.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	/*
	 * 解析所有歌曲的绝对路径
	 */
	public static List<String> PullPath(){
		List<String> list = new ArrayList<String>();
		XmlPullParser xmlPullParser = Xml.newPullParser();
		File file = new File(Environment.getExternalStorageDirectory()+File.separator+"MediaPlayer",
				"music_list.xml");
		try {
			FileInputStream fis = new FileInputStream(file);
			xmlPullParser.setInput(fis, "utf-8");
			int type = xmlPullParser.getEventType();
			while(type != XmlPullParser.END_DOCUMENT){
					if(type == XmlPullParser.START_TAG&&"path".equals(xmlPullParser.getName())){
						list.add(xmlPullParser.nextText());
					}
				type = xmlPullParser.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
}
