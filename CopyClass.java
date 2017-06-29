package xmlTools;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

public abstract class CopyClass implements Copyable{
	/**
	 * create a "map" inner class that has an immutable key list but a mutable value list
	 *
	 */
	private class ArgsMap<K, V>{
		private HashMap<K, V> args = new HashMap<K, V>();
		
		/**
		 * private no-arg constructor
		 */
		@SuppressWarnings("unused")
		private ArgsMap(){
			
		}
		
		/**
		 * constructor that can be used by subclasses
		 * initiates argsMap with a list of keys that will be immutable once initialized
		 */
		public ArgsMap(List<K> initList){
			for (K key: initList){
				args.put(key, null);
			}
		}
		
		private HashMap<K,V> getArgsMap(){
			return (HashMap<K, V>) Collections.unmodifiableMap(args);
		}
		
		/**
		 * allows setting a key only if key already exists in the ArgsMap
		 * @param key
		 * @param value
		 */
		public void put(K key, V value){
			if (args.containsKey(key)) {
				args.put(key, value); 
			}			
			else {
				throw new UnsupportedOperationException("Cannot insert new keys into CopyClass args.") ;
			}			
		}
		
		/**
		 * allows getting value of key
		 * @param key
		 */
		public V get(K key){
			return args.get(key);
		}
	}
	
	//allows CopyClass objects to initialize the ArgMaps object but not change it
	private ArgsMap<String, String> argsMap;
	
	public void initArgs(List<String> keyList){
		if (argsMap == null) {
			argsMap = new ArgsMap<>(keyList);
		}
		else {
			throw new UnsupportedOperationException("Cannot change args of CopyClass.") ;
		}
			
	}
	
	public void setArgs(String key, String value){
		argsMap.put(key, value);
	}
	
	public String getArgs(String key){
		return argsMap.get(key);
	}
	
	public HashMap<String, String> getArgsMap(){
		return argsMap.getArgsMap();
	}
	
	public abstract void abortCopy();
	public abstract int startCopying() throws IOException, URISyntaxException, XPathExpressionException, ParserConfigurationException, SAXException, TransformerException;
	/**
	 * Set arguments for the CopyAndModifyXML class, e.g. instructions for modification
	 */	
}