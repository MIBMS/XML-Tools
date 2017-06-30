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

/**
 * provides more implementation details of the Copyable interface
 * contains data members for the extensions and the arguments that are applicable for the subclasses
 * ArgsMap is an inner type that allows the user to initialize the keys of a HashMap but not change them by replacement - values can be replaced and are mutable
 */
public abstract class CopyClass implements Copyable{
	//list of extensions supported by Copy Object
	private List<String> extensions;
	
	/**
	 * create a "map" inner class that has an immutable key list but a mutable value list
	 * the underlying real map which is inaccessible elsewhere is a HashMap
	 * but the returned public "fake" map is just an unmodifiable view of the real map containing the arguments
	 * it is used to store argument labels (keys) and values to be used by a calling class to instruct a subclass of Copyable during a copy process
	 * this forces the developer to set the argument keys that can be used by a subclass - or else we have a mess of arguments for the Copyable subclass
	 */
	private class ArgsMap<K, V>{
		private HashMap<K, V> args = new HashMap<K, V>();
		
		/**
		 * private no-arg constructor - you can only instantiate this ArgMaps object with a list
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
		
		/**
		 * @return an unmodifiable Map view of the underlying real map
		 */
		private Map<K,V> getArgsMap(){
			return Collections.unmodifiableMap(args);
		}
		
		/**
		 * allows setting a key only if key already exists in the ArgsMap
		 * @param key key to be set
		 * @param value value to set it to if key is already initialized
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
		 * @param key key to get value from
		 */
		public V get(K key){
			return args.get(key);
		}
	}
	
	//allows CopyClass objects to initialize the ArgMaps object but not change it
	private ArgsMap<String, String> argsMap;
	
	/**
	 * allows calling classes to initialize the arguments of the Copyable subclass
	 * @param keyList list of keys (argument labels) that will be used by this Copyable subclass
	 */
	public void initArgs(List<String> keyList){
		if (argsMap == null) {
			argsMap = new ArgsMap<>(keyList);
		}
		else {
			throw new UnsupportedOperationException("Cannot change args of CopyClass.") ;
		}
			
	}
	
	/**
	 * allows calling classes to set values for initialized keys of the Copyable subclass
	 * @param key
	 * @param value
	 */
	public void setArgs(String key, String value){
		argsMap.put(key, value);
	}
	
	/**
	 * allows calling classes to get values from keys of the Copyable subclass
	 * @param key
	 * @return the value of the key
	 */
	public String getArgs(String key){
		return argsMap.get(key);
	}
	
	/**
	 * allows calling classes to get an unmodifiable view of the arguments of this Copyable subclass
	 * @return a Map that cannot be modified directly
	 */
	public Map<String, String> getArgsMap(){
		return argsMap.getArgsMap();
	}
	
	/**
	 * sets the permitted extensions of files that are supported by this Copyable subclass
	 * @param extensions a list of the extensions
	 */
	protected void setExtensions(List<String> extensions){
		this.extensions = extensions;
	}
	
	/**
	 * allows calling classes to get the list of extensions supported by this Copyable subclass
	 */
	public List<String> getExtensions(){
		return extensions;
	}

}