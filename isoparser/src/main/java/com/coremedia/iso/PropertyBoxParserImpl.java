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
package com.coremedia.iso;

import com.coremedia.iso.boxes.Box;

import java.io.BufferedInputStream;
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
    Properties mapping;
    Pattern constuctorPattern = Pattern.compile("(.*)\\((.*?)\\)");

    public PropertyBoxParserImpl(String... customProperties) {
        InputStream is = getClass().getResourceAsStream("/isoparser-default.properties");
        try {
            mapping = new Properties();
            try {
                mapping.load(is);
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                if (cl == null) {
                    cl = ClassLoader.getSystemClassLoader();
                }
                Enumeration<URL> enumeration = cl.getResources("isoparser-custom.properties");

                while (enumeration.hasMoreElements()) {
                    URL url = enumeration.nextElement();
                    InputStream customIS = url.openStream();
                    try {
                        mapping.load(customIS);
                    } finally {
                        customIS.close();
                    }
                }
                for (String customProperty : customProperties) {
                    mapping.load(getClass().getResourceAsStream(customProperty));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                // ignore - I can't help
            }
        }
    }

    public PropertyBoxParserImpl(Properties mapping) {
        this.mapping = mapping;
    }


    @Override
    public Box createBox(String type, byte[] userType, String parent) {

        invoke(type, userType, parent);
        String[] param = this.param.get();
        try {
            Class<Box> clazz = (Class<Box>) Class.forName(clazzName.get());
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

                Constructor<Box> constructorObject = clazz.getConstructor(constructorArgsClazz);
                return constructorObject.newInstance(constructorArgs);
            } else {
                return clazz.newInstance();
            }

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


    StringBuilder buildLookupStrings = new StringBuilder();
    ThreadLocal<String> clazzName = new ThreadLocal<String>();
    ThreadLocal<String[]> param = new ThreadLocal<String[]>();
    static String[] EMPTY_STRING_ARRAY = new String[0];

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
            param.set( EMPTY_STRING_ARRAY);
            clazzName.set(constructor);
        } else {
            Matcher m = constuctorPattern.matcher(constructor);
            boolean matches = m.matches();
            if (!matches) {
                throw new RuntimeException("Cannot work with that constructor: " + constructor);
            }
            clazzName.set( m.group(1));
            if (m.group(2).length() == 0) {
                param.set(EMPTY_STRING_ARRAY);
            } else {
                param.set(m.group(2).length() > 0 ? m.group(2).split(",") : new String[]{});
            }
        }

    }
}
