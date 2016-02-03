/*
 * Copyright 2016 Fumiharu Kinoshita
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.bunji.jdbc.util;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import info.bunji.jdbc.logger.JdbcLogger;
import info.bunji.jdbc.logger.JdbcLoggerFactory;

/**
 *
 * @author f.kinoshita
 */
public class ClassScanUtil {

	private static JdbcLogger logger = JdbcLoggerFactory.getLogger();

	/**
	 *
	 * @param packageName
	 * @return
	 * @throws Exception
	 */
	public static List<Class<?>> findClassesFromPackage(String packageName) throws Exception {
		return findClassesFromPackage(Thread.currentThread().getContextClassLoader(), packageName);
	}

	/*
	 *
	 */
	private static List<Class<?>> findClassesFromPackage(ClassLoader classLoader, String packageName) throws Exception {
		List<Class<?>> classList = new ArrayList<Class<?>>();

		String pathName = packageName.replace(".", "/");
		Enumeration<URL> pathList = classLoader.getResources(pathName);

		while (pathList.hasMoreElements()) {
			URL path = pathList.nextElement();
			if (path.getProtocol().equals("jar")){
				// build jar file name, then loop through zipped entries
				String jarFileName = URLDecoder.decode(path.getFile(), "UTF-8");
				jarFileName = jarFileName.substring(5,jarFileName.indexOf("!"));

				JarFile jarFile = new JarFile(jarFileName);
				try {
					Enumeration<JarEntry> entries = jarFile.entries();
					while (entries.hasMoreElements()) {
						String name = entries.nextElement().getName();
						if(name.startsWith(pathName) && name.endsWith(".class")) {
							name = name.substring(pathName.length() + 1);
							name = name.substring(0, name.length() - 6).replace("/", ".");
							if (name.indexOf(".") == -1) {
								try {
									name = packageName + "."+ name;
logger.trace(name + "(in jar)");
									classList.add(Class.forName(name));
								} catch (Throwable t) {}
							}
			            }
			        }
				} finally {
					jarFile.close();
				}
		    } else {
			    URI uri = new URI(path.toString());
		        File[] files = new File(uri.getPath()).listFiles();
		        for (File file: files) {
					String name = file.getName();
					if (name.endsWith(".class")) {
						name = name.substring(0, name.length() - 6);
						try {
							name = packageName + "." + name;
logger.trace(name + "(in path)");
							classList.add(Class.forName(name));
						} catch (Throwable t) {}
					}
		        }
		    }
		}

		// remove package-info
		Iterator<Class<?>> it = classList.iterator();
		while (it.hasNext()) {
			Class<?> clazz = it.next();
			if (clazz.getSimpleName().equals("package-info")) {
				it.remove();
			}
		}
		return classList;
	}
}
