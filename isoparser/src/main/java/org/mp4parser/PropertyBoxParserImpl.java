/*
 * Copyright 2012 Sebastian Annies, Hamburg
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mp4parser;

import org.mp4parser.tools.Hex;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Property file based BoxFactory
 */
public class PropertyBoxParserImpl extends AbstractBoxParser {
    public static Properties BOX_MAP_CACHE = null;
    public Properties mapping;

    static String[] EMPTY_STRING_ARRAY = new String[0];
    Pattern constuctorPattern = Pattern.compile("(.*)\\((.*?)\\)");
    StringBuilder buildLookupStrings = new StringBuilder();
    ThreadLocal<String> clazzName = new ThreadLocal<>();
    ThreadLocal<String[]> param = new ThreadLocal<>();

    public PropertyBoxParserImpl(String... customProperties) {

        if (BOX_MAP_CACHE != null) {
            mapping = new Properties(BOX_MAP_CACHE);
        } else {
            InputStream is = ClassLoader.getSystemResourceAsStream("isoparser2-default.properties");
            try {
                mapping = new Properties();
                try {
                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    if (cl == null) {
                        cl = ClassLoader.getSystemClassLoader();
                    }
                    if (is == null) {
                        is = cl.getResourceAsStream("isoparser2-default.properties");
                    }
                    mapping.load(is);

                    Enumeration<URL> enumeration = cl.getResources("isoparser-custom.properties");

                    while (enumeration.hasMoreElements()) {
                        URL url = enumeration.nextElement();
                        try (InputStream customIS = url.openStream()) {
                            mapping.load(customIS);
                        }
                    }
                    for (String customProperty : customProperties) {
                        mapping.load(getClass().getResourceAsStream(customProperty));
                    }
                    BOX_MAP_CACHE = mapping;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    // ignore - I can't help
                }
            }
        }
    }

    public PropertyBoxParserImpl(Properties mapping) {
        this.mapping = mapping;
    }

    @Override
    public ParsableBox createBox(String type, byte[] userType, String parent) {

        invoke(type, userType, parent);
        String[] param = this.param.get();
        try {
            Class<ParsableBox> clazz = (Class<ParsableBox>) Class.forName(clazzName.get());
            if (param.length > 0) {
                Class[] constructorArgsClazz = new Class[param.length];
                Object[] constructorArgs = new Object[param.length];
                for (int i = 0; i < param.length; i++) {
                    if ("userType".equals(param[i])) {
                        constructorArgs[i] = userType;
                        constructorArgsClazz[i] = byte[].class;
                    } else if ("type".equals(param[i])) {
                        constructorArgs[i] = type;
                        constructorArgsClazz[i] = String.class;
                    } else if ("parent".equals(param[i])) {
                        constructorArgs[i] = parent;
                        constructorArgsClazz[i] = String.class;
                    } else {
                        throw new InternalError("No such param: " + param[i]);
                    }
                }

                Constructor<ParsableBox> constructorObject = clazz.getConstructor(constructorArgsClazz);
                return constructorObject.newInstance(constructorArgs);
            } else {
                return clazz.getDeclaredConstructor().newInstance();
            }

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public void invoke(String type, byte[] userType, String parent) {
        String constructor;
        if (userType != null) {
            if (!"uuid".equals((type))) {
                throw new RuntimeException("we have a userType but no uuid box type. Something's wrong");
            }
            constructor = mapping.getProperty("uuid[" + Hex.encodeHex(userType).toUpperCase() + "]");
            if (constructor == null) {
                constructor = mapping.getProperty((parent) + "-uuid[" + Hex.encodeHex(userType).toUpperCase() + "]");
            }
            if (constructor == null) {
                constructor = mapping.getProperty("uuid");
            }
        } else {
            constructor = mapping.getProperty((type));
            if (constructor == null) {
                String lookup = buildLookupStrings.append(parent).append('-').append(type).toString();
                buildLookupStrings.setLength(0);
                constructor = mapping.getProperty(lookup);

            }
        }
        if (constructor == null) {
            constructor = mapping.getProperty("default");
        }
        if (constructor == null) {
            throw new RuntimeException("No box object found for " + type);
        }
        if (!constructor.endsWith(")")) {
            param.set(EMPTY_STRING_ARRAY);
            clazzName.set(constructor);
        } else {
            Matcher m = constuctorPattern.matcher(constructor);
            boolean matches = m.matches();
            if (!matches) {
                throw new RuntimeException("Cannot work with that constructor: " + constructor);
            }
            clazzName.set(m.group(1));
            if (m.group(2).length() == 0) {
                param.set(EMPTY_STRING_ARRAY);
            } else {
                param.set(m.group(2).length() > 0 ? m.group(2).split(",") : new String[]{});
            }
        }

    }
}
