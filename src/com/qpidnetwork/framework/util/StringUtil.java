package com.qpidnetwork.framework.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.util.CharArrayBuffer;

@SuppressWarnings("deprecation")
public class StringUtil {

	/**
	 * 合并多个字符串
	 * 
	 * @param params
	 * @return
	 */
	public static String mergeMultiString(Object... params) {
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < params.length; i++) {
			buff.append(params[i]);
		}
		return buff.toString();
	}

	/**
	 * 
	 * @param is
	 * @return
	 */
	public static String convertStreamToString(InputStream is) {
		String line = null;
		StringBuilder sb = new StringBuilder(8192 * 20);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(is), 8192 * 20);

			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (is != null)
					is.close();

				reader = null;
				is = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	/**
	 * 读取输入流，返回字符串
	 * 
	 * @param instream
	 * @param charset
	 * @return
	 */
	public static String readInputStream(InputStream instream, int contentLength, Charset charset) throws IOException {
		Reader reader = new InputStreamReader(new BufferedInputStream(instream), charset);
		CharArrayBuffer buffer = new CharArrayBuffer(contentLength);
		try {
			char[] tmp = new char[1024];
			int l;
			while ((l = reader.read(tmp)) != -1) {
				buffer.append(tmp, 0, l);
			}
		} finally {
			reader.close();
		}
		return buffer.toString();
	}

	/**
	 * 合成URI所需要的参数形式
	 * 
	 * @param keyValues
	 * @return
	 */
	public static String formateUriFromKeyValues(Object... keyValues) {
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < keyValues.length; i += 2) {
			buff.append(keyValues[i]);
			buff.append("=");
			buff.append(keyValues[i + 1] == null ? "" : keyValues[i + 1]);
			buff.append("&");
		}
		if (buff.length() > 0) {
			buff.deleteCharAt(buff.length() - 1);
		}
		return buff.toString();
	}

	// /**
	// * 合成URI所需要的参数形式
	// *
	// * @param bean
	// * @return
	// */
	// public static String formateUriFromBeanFields(Object bean) {
	// StringBuffer buff = new StringBuffer();
	// Class clazz = bean.getClass();
	// Field[] fields = bean.getClass().getDeclaredFields();
	// while (!BaseBean.class.equals(clazz = clazz.getSuperclass())) {
	// Field[] superFields = bean.getClass().getDeclaredFields();
	// if (superFields.length > 0) {
	// Field[] newFields = new Field[fields.length + superFields.length];
	// System.arraycopy(fields, 0, newFields, 0, fields.length);
	// System.arraycopy(superFields, 0, newFields, fields.length,
	// superFields.length);
	// fields = newFields;
	// }
	// }
	// Field.setAccessible(fields, true);
	// for (int i = 0; i < fields.length; i++) {
	// Field field = fields[i];
	// if (Modifier.isStatic(field.getModifiers())) {
	// continue;
	// }
	// String name = field.getName();
	// SerializedName serializedName =
	// field.getAnnotation(SerializedName.class);
	// if (serializedName != null) {
	// name = serializedName.value();
	// }
	// buff.append(name);
	// buff.append("=");
	// Object val = null;
	// try {
	// val = field.get(bean);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// if (val != null) {
	// buff.append(val.toString());
	// }
	// buff.append("&");
	// }
	// if (buff.length() > 0) {
	// buff.deleteCharAt(buff.length() - 1);
	// }
	// return buff.toString();
	// }

	// public static String formateUriFromBeanArrayFields(Object... beans) {
	// StringBuffer sb = new StringBuffer();
	// for (Object bean : beans) {
	// String text = formateUriFromBeanFields(bean);
	// if (text != null && text.length() > 0) {
	// sb.append(text).append("&");
	// }
	// }
	// if (sb.length() > 0) {
	// sb.deleteCharAt(sb.length() - 1);
	// }
	// return sb.toString();
	// }

	/**
	 * 合成URI所需要的参数形式
	 * 
	 * @param map
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String formateUriFromMapFields(Map<String, ?> map) {
		StringBuffer buff = new StringBuffer();
		Set<?> entrySet = map.entrySet();
		Iterator<?> it = entrySet.iterator();
		while (it.hasNext()) {
			Map.Entry<String, ?> entry = (Entry<String, ?>) it.next();
			buff.append(entry.getKey());
			buff.append("=");
			Object val = entry.getValue();
			if (val != null) {
				buff.append(val.toString());
			}
			buff.append("&");
		}
		if (buff.length() > 0) {
			buff.deleteCharAt(buff.length() - 1);
		}
		return buff.toString();

	}

	/**
	 * 将map中的数据转换成JSON格式 的字符串
	 * 
	 * @param map
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String formateMapToJson(Map map) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		Set<?> entrySet = map.entrySet();
		Iterator<?> it = entrySet.iterator();
		while (it.hasNext()) {
			Map.Entry<String, ?> entry = (Entry<String, ?>) it.next();
			buffer.append("\"" + entry.getKey() + "\"");
			buffer.append(":");
			Object val = entry.getValue();
			if (val != null) {
				buffer.append("\"" + val.toString() + "\"");
			}
			buffer.append(",");
		}
		if (buffer.length() > 1) {
			buffer.deleteCharAt(buffer.length() - 1);
			buffer.append("}");
		} else if (buffer.length() == 1) {
			buffer.deleteCharAt(buffer.length() - 1);
		}
		return buffer.toString();
	}

	/**
	 * 转换成JSON格式的字符串(其中不含{})
	 * 
	 * @param keyValues
	 * @return
	 */
	public static String formateStringToJson(Object... keyValues) {
		StringBuffer buffer = new StringBuffer();
		if (keyValues.length > 0) {
			buffer.append("{");
		}
		for (int i = 0; i < keyValues.length; i += 2) {
			buffer.append("\"" + keyValues[i] + "\"");
			buffer.append(":\"");
			buffer.append(keyValues[i + 1] == null ? "" : keyValues[i + 1]);
			buffer.append("\",");
		}
		if (buffer.length() > 1) {
			buffer.deleteCharAt(buffer.length() - 1);
			buffer.append("}");
		} else if (buffer.length() == 1) {
			buffer.deleteCharAt(buffer.length() - 1);
		}

		return buffer.toString();
	}

	public static boolean isNotEmpty(String string) {
		return string != null && string.length() > 0;
	}

	public static boolean isEmpty(String string) {
		return string == null || string.length() == 0;
	}

	public static boolean isEqual(String o, String c) {
		if (StringUtil.isEmpty(o)) {
			o = "";
		}
		if (StringUtil.isEmpty(c)) {
			c = "";
		}
		return o.equals(c);
	}

	public static String[] split(String texts, String seperator) {
		String[] results = new String[0];
		if (texts == null) {
			return results;
		}
		return texts.split(seperator);
	}

	public static String compose(Object[] texts, String seperator) {
		if (texts == null) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for (Object text : texts) {
			sb.append((String) text);
			sb.append(",");
		}
		if (texts.length > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	/**
	 * 
	 * @param s
	 * @return
	 */
	public static String UnicodeToGBK2(String s) {
		String[] k = s.split(";");
		String rs = "";
		for (int i = 0; i < k.length; i++) {
			int strIndex = k[i].indexOf("&#");
			String newstr = k[i];
			if (strIndex > -1) {
				String kstr = "";
				if (strIndex > 0) {
					kstr = newstr.substring(0, strIndex);
					rs += kstr;
					newstr = newstr.substring(strIndex);
				}
				int m = Integer.parseInt(newstr.replace("&#", ""));
				char c = (char) m;
				rs += c;
			} else {
				rs += k[i];
			}
		}
		return rs;
	}

	/**
	 * A hashing method that changes a string (like a URL) into a hash suitable
	 * for using as a disk filename.
	 */
	public static String hashKeyForDisk(String key) {
		String cacheKey;
		try {
			final MessageDigest mDigest = MessageDigest.getInstance("MD5");
			mDigest.update(key.getBytes());
			cacheKey = bytesToHexString(mDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			cacheKey = String.valueOf(key.hashCode());
		}
		return cacheKey;
	}

	private static String bytesToHexString(byte[] bytes) {
		// http://stackoverflow.com/questions/332079
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				sb.append('0');
			}
			sb.append(hex);
		}
		return sb.toString();
	}

}
