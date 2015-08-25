package com.az.mediaplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Environment;

public class GetList{
	/*
	 * 获得所有歌曲名和绝对路径
	 */
	public static List<Map<String, Object>> getList(File root){
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		File[] files = root.listFiles();
		if(files !=null){
			for(File file:files){
				if(file.isDirectory()){
					list.addAll(getList(file));
				}else{
					String str = file.getName().substring(file.getName().lastIndexOf(".")+1);
					if(str.equals("mp3")|str.equals("wav")){
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("name", file.getName());
						map.put("path", file.getAbsolutePath());
						list.add(map);
					}
				}		
			}
		}

		return list;
	}
	/*
	 * 获取歌曲名称
	 */
	public static ArrayList<String> getName(){
		ArrayList<String> list = new ArrayList<String>();
		ArrayList<Map<String, Object>> map = (ArrayList<Map<String, Object>>) getList(Environment.getExternalStorageDirectory());
		System.out.println("edfedf");
		if(map.size()>0){
			for(int i = 0;i<map.size();i++){
				list.add(map.get(i).get("name").toString());
			}
		}
		return list;
	}
	/*
	 * 获取歌曲名称的绝对路径
	 */
	public static ArrayList<String> getPath(){
		ArrayList<String> list = new ArrayList<String>();
		ArrayList<Map<String, Object>> map = (ArrayList<Map<String, Object>>) getList(Environment.getExternalStorageDirectory());
		if(map.size()>0){
			for(int i = 0;i<map.size();i++){
				list.add(map.get(i).get("path").toString());
			}
		}
		return list;
	}

}

