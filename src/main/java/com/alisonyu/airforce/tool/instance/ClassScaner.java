package com.alisonyu.airforce.tool.instance;

import com.alisonyu.airforce.constant.Protocols;
import com.alisonyu.airforce.constant.Strings;
import io.vertx.core.impl.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 用于扫描classpath的所有类
 * @author yuzhiyi
 * @date 2018/9/11 21:46
 */
public class ClassScaner {

	private static Logger logger = LoggerFactory.getLogger(ClassScaner.class);

	private static Set<Class<?>> classes = new ConcurrentHashSet<>();

	private static final String ENCODING = "UTF-8";

	private static final ConcurrentHashSet<String> FORBIDDEN = new ConcurrentHashSet<String>() {
		{
			this.add("java");
			this.add("javax");
			this.add("jdk");
			// Sax & Yaml
			this.add("org.xml");
			this.add("org.yaml");
			// Idea
			this.add("com.intellij");
			// Sun
			this.add("sun");
			this.add("com.sun");
			// Netty
			this.add("io.netty");
			// Rxjava
			this.add("io.reactivex");
			// Jackson
			this.add("com.fasterxml");
			// Logback
			this.add("ch.qos");
			this.add("org.slf4j");
			this.add("org.apache");
			// Vert.x
			this.add("io.vertx.core");
			this.add("io.vertx.spi");
			this.add("io.vertx.ext.web");
			// Asm
			this.add("org.ow2");
			this.add("org.objectweb");
			this.add("com.esotericsoftware");
			// Hazelcast
			this.add("com.hazelcast");
			// Glassfish
			this.add("org.glassfish");
			// Junit
			this.add("org.junit");
			this.add("junit");
			// Hamcrest
			this.add("org.hamcrest");
			this.add("com.alisonyu.airforce");
		}
	};


	 public static Set<Class<?>> getClasses(){
		Set<String> packageDirs = getPackages();
		//并行流扫描
		packageDirs.forEach(ClassScaner :: getClassesInPackage);
		classes.stream().map(Class::getName).forEach(logger::debug);
		return classes;
	}

	/**
	 * TODO 多线程包扫描
	 */
	private static Set<Class<?>> getClassesInPackage(final String pkgName){
		final String pkgPath = getPackagePath(pkgName);
		try {
			Enumeration<URL> urls = getClassLoader().getResources(pkgPath);
			while(urls.hasMoreElements()){
				URL url = urls.nextElement();
				String protocol = url.getProtocol();
				if (Protocols.FILE.equals(protocol)){
					// Get path of this package
					final String path = URLDecoder.decode(url.getFile(),ENCODING);
					addClassesOfPath(pkgName,path);
				}else if (Protocols.JAR.equals(protocol)){
					addClassesFromJar(pkgName,pkgPath,url);
				}
			}
		} catch (IOException e) {
			//TODO 统一错误logger管理
			e.printStackTrace();
		}
		return null;
	}

	private static final String CLASS_SUFFIX = ".class";
	private static void addClassesOfPath(final String packageName, final String packagePath){
		final File file = new File(packagePath);
		if (!file.exists() || !file.isDirectory()){
			return;
		}
		String procssedPackName = packageName.startsWith(Strings.DOT) ? packageName.substring(1,packageName.length()) : packageName;
		//list all dir or file end with .class
		final File[] dirFiles = file.listFiles(f-> f.isDirectory() || f.getName().endsWith(CLASS_SUFFIX));
		if (dirFiles == null){
			return;
		}
		for (final File f : dirFiles){
			if (f.isDirectory()){
				final String dirName = f.getName();
				addClassesOfPath(procssedPackName+Strings.DOT+dirName,packagePath+Strings.SLASH+dirName);
			}else{
				//remove .class from the name
				final String className = f.getName().substring(0,f.getName().length()-6);
				try {
					classes.add(getClassLoader().loadClass(procssedPackName+Strings.DOT+className));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void addClassesFromJar(final String packName,final String packagePath,URL url){
		String packageName;
		// Get jar file
		final JarFile jar;
		try {
			jar = ((JarURLConnection) url.openConnection()).getJarFile();
			// List all entries of this jar
			final Enumeration<JarEntry> entries = jar.entries();
			// Loop for jar entry.
			while(entries.hasMoreElements()){
				final JarEntry entry = entries.nextElement();
				String name = entry.getName();
				if (name.charAt(0) == '/'){
					name = name.substring(1);
				}
				if (name.startsWith(packagePath)){
					final int idx = name.lastIndexOf('/');
					if (idx != 1){
						packageName = name.substring(0,idx).replace('/','.');
						if (name.endsWith(CLASS_SUFFIX) && !entry.isDirectory()){
							final String className = name.substring(packageName.length()+1,name.length() - 6);
							try{
								String fullClassName = packageName+Strings.DOT+className;
								classes.add(getClassLoader().loadClass(fullClassName));
							}catch (Exception e){
								e.printStackTrace();
							}

						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static ClassLoader getClassLoader(){
		return Thread.currentThread().getContextClassLoader();
	}


	/**
	 * xxx.yyy.zzz => xxx/yyy/zzz
	 */
	private static String getPackagePath(final String pkgName){
		if (pkgName==null) {
			return Strings.EMPTY;
		}
		return pkgName.equals(Strings.DOT) ?
				Strings.EMPTY :
				pkgName.replace(Strings.DOT,Strings.SLASH);
	}

	/**
	 * get packageNames like xxx.yyy.zzz
	 */
	private static Set<String> getPackages(){
		final Package[] packages = Package.getPackages();
		final Set<String> packageDirs = new HashSet<>();
		for (Package pkg : packages){
			String pkgName = pkg.getName();
			//对于FORBIDDEN里面的包不进行扫描
			boolean forbid = FORBIDDEN.stream().anyMatch(pkgName::startsWith);
			if (!forbid){
				packageDirs.add(pkgName);
			}
		}
		//add current classpath
		packageDirs.add(Strings.DOT);
		return packageDirs;
	}

	public static void main(String[] args) {
		getPackages().forEach(ClassScaner::getClassesInPackage);
		classes.stream().map(Class::getName).forEach(logger::info);
	}



}
