package com.tpp.persistence.util;

import java.io.*;
import java.util.*;

public class ByteArrayUtils {
	public static final byte TypeNull = 0;
	public static final byte TypeString = 1;
	public static final byte TypeIntegral = 2;
	public static final byte TypeFloating = 3;
	public static final byte TypeDate = 4;
	public static final byte TypeList = 5;
	public static final byte TypeMap = 6;
	public static final byte TypeBinary = 7;

	public static String toString(byte[] data) {
		try {
			return new String(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("this cannot happen", e);
		}
	}

	public static int toInt(byte[] data) {
		if (data == null || data.length != 4) return 0x0;
		// ----------
		return (0xff & data[0]) << 24 | (0xff & data[1]) << 16 | (0xff & data[2]) << 8 | (0xff & data[3]) << 0;
	}

	public static long toLong(byte[] data) {
		if (data == null || data.length != 8) return 0x0;
		// ----------
		return // (Below) convert to longs before shift because digits
				//         are lost with ints beyond the 32-bit limit
				(long) (0xff & data[0]) << 56 | (long) (0xff & data[1]) << 48 | (long) (0xff & data[2]) << 40
						| (long) (0xff & data[3]) << 32 | (long) (0xff & data[4]) << 24 | (long) (0xff & data[5]) << 16
						| (long) (0xff & data[6]) << 8 | (long) (0xff & data[7]) << 0;
	}

	public static Date toDate(byte[] data) {
		return new Date(toLong(data));
	}

	public static float toFloat(byte[] data) {
		if (data == null || data.length != 4) return 0x0;
		// ---------- simple:
		return Float.intBitsToFloat(toInt(data));
	}

	public static double toDouble(byte[] data) {
		if (data == null || data.length != 8) return 0x0;
		// ---------- simple:
		return Double.longBitsToDouble(toLong(data));
	}

	public static byte[] toByta(int data) {
		return new byte[] { (byte) ((data >> 24) & 0xff), (byte) ((data >> 16) & 0xff), (byte) ((data >> 8) & 0xff),
				(byte) ((data >> 0) & 0xff), };
	}

	public static byte[] toByta(String data) {
		try {
			return data.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("this cannot happen", e);
		}
	}

	public static byte[] toByta(long data) {
		return new byte[] { (byte) ((data >> 56) & 0xff), (byte) ((data >> 48) & 0xff), (byte) ((data >> 40) & 0xff),
				(byte) ((data >> 32) & 0xff), (byte) ((data >> 24) & 0xff), (byte) ((data >> 16) & 0xff),
				(byte) ((data >> 8) & 0xff), (byte) ((data >> 0) & 0xff), };
	}

	public static byte[] toByta(Date data) {
		return toByta(data.getTime());
	}

	public static byte[] toByta(float data) {
		return toByta(Float.floatToRawIntBits(data));
	}

	public static byte[] toByta(double data) {
		return toByta(Double.doubleToRawLongBits(data));
	}

	public static byte[] toByta(Map<String, Object> map) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			write(map, baos);
		} catch (IOException e) {
			throw new RuntimeException("this cannot happen", e);
		}
		return baos.toByteArray();
	}

	public static byte[] toByta(List<Object> list) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			write(list, baos);
		} catch (IOException e) {
			throw new RuntimeException("this cannot happen", e);
		}
		return baos.toByteArray();
	}

	/**
	 * write object with first by as type flag
	 *
	 * @param data
	 * @param os
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private static void write(Object data, OutputStream os) throws IOException {
		if (data == null) {
			os.write(TypeNull);
		} else if (data instanceof String) {
			os.write(TypeString);
			byte[] bytes = toByta((String) data);
			os.write(toByta(bytes.length));
			os.write(bytes);
		} else if (data instanceof Integer || data instanceof Long) {
			os.write(TypeIntegral);
			os.write(toByta(((Number) data).longValue()));
		} else if (data instanceof Float || data instanceof Double) {
			os.write(TypeFloating);
			os.write(toByta(((Number) data).doubleValue()));
		} else if (data instanceof Date) {
			os.write(TypeDate);
			os.write(toByta(((Date) data).getTime()));
		} else if (data instanceof List) {
			os.write(TypeList);
			write((List<Object>) data, os);
		} else if (data instanceof Map) {
			os.write(TypeMap);
			write((Map<String, Object>) data, os);
		} else if (data instanceof byte[]) {
			os.write(TypeBinary);
			os.write(toByta(((byte[]) data).length));
			os.write((byte[]) data);
		} else {
			throw new RuntimeException("Unknown object type " + data.getClass().getCanonicalName());
		}
	}

	private static void write(Map<String, Object> map, OutputStream os) throws IOException {
		os.write(toByta(map.size()));
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			// write length key
			byte[] baKey = entry.getKey().getBytes("UTF-8");
			os.write((byte) baKey.length);
			os.write(baKey);
			write(entry.getValue(), os);
		}
	}

	private static void write(List<Object> list, OutputStream os) throws IOException {
		os.write(toByta(list.size()));
		for (Object data : list) {
			// write length key
			write(data, os);
		}
	}

	public static List<Object> toList(byte[] data) {
		InputStream is = new ByteArrayInputStream(data);
		try {
			return readList(is);
		} catch (IOException ex) {
			throw new RuntimeException("this cannot happen", ex);
		}
	}

	public static Map<String, Object> toMap(byte[] data) {
		InputStream is = new ByteArrayInputStream(data);
		try {
			return readMap(is);
		} catch (IOException ex) {
			throw new RuntimeException("this cannot happen", ex);
		}
	}

	private static int readInt(InputStream is) throws IOException {
		return (0xff & is.read()) << 24 | (0xff & is.read()) << 16 | (0xff & is.read()) << 8 | (0xff & is.read()) << 0;
	}

	public static long readLong(InputStream is) throws IOException {
		return // (Below) convert to longs before shift because digits
				//         are lost with ints beyond the 32-bit limit
				(long) (0xff & is.read()) << 56 | (long) (0xff & is.read()) << 48 | (long) (0xff & is.read()) << 40
						| (long) (0xff & is.read()) << 32 | (long) (0xff & is.read()) << 24
						| (long) (0xff & is.read()) << 16 | (long) (0xff & is.read()) << 8
						| (long) (0xff & is.read()) << 0;
	}

	public static double readDouble(InputStream is) throws IOException {
		return Double.longBitsToDouble(readLong(is));
	}

	/**
	 * read list
	 *
	 * @param is
	 * @return
	 * @throws IOException
	 */
	private static List<Object> readList(InputStream is) throws IOException {
		int size = readInt(is);
		ArrayList<Object> list = new ArrayList<Object>(size);
		for (int i = 0; i < size; i++) {
			list.add(readObject(is));
		}

		return list;
	}

	/**
	 * read object
	 *
	 * @param is
	 * @return
	 * @throws IOException
	 */
	private static Map<String, Object> readMap(InputStream is) throws IOException {
		int size = readInt(is);
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>(size);
		for (int i = 0; i < size; i++) {
			int keylen = is.read();
			byte[] key = new byte[keylen];
			is.read(key);
			map.put(toString(key), readObject(is));
		}

		return map;
	}

	/**
	 * read object with first by is type flag
	 *
	 * @param is
	 * @return
	 * @throws IOException
	 */
	private static Object readObject(InputStream is) throws IOException {
		Object value;
		int type = is.read();
		switch (type) {
			case ByteArrayUtils.TypeNull:
				value = null;
				break;
			case ByteArrayUtils.TypeString:
				int length = readInt(is);
				byte[] string = new byte[length];
				is.read(string);
				value = toString(string);
				break;
			case ByteArrayUtils.TypeIntegral:
				value = readLong(is);
				break;
			case ByteArrayUtils.TypeFloating:
				value = readDouble(is);
				break;
			case ByteArrayUtils.TypeDate:
				value = new Date(readLong(is));
				break;
			case ByteArrayUtils.TypeList:
				value = readList(is);
				break;
			case ByteArrayUtils.TypeMap:
				value = readMap(is);
				break;
			case ByteArrayUtils.TypeBinary:
				int len = readInt(is);
				byte[] dat = new byte[len];
				if (is.read(dat) < len) throw new IOException("failed to read binary");
				value = dat;
				break;
			default:
				throw new RuntimeException("Unknown persistence type flag " + type);
		}
		return value;
	}

	/**
	 * copy a byte array from src to dest
	 *
	 * @param src
	 * @param dest
	 * @param dIndex index of dest array to start writing
	 */
	public static void copy(byte[] src, byte[] dest, int dIndex) {
		for (int i = 0; i + dIndex < dest.length; i++)
			dest[i + dIndex] = src[i];
	}

	/**
	 * compare two byte array
	 *
	 * @param data1
	 * @param data2
	 * @return
	 */
	public static int compare(byte[] data1, byte[] data2) {
		int minLen = Math.min(data1.length, data2.length);
		for (int i = 0; i < minLen; i++) {
			if (data1[i] == data2[i]) continue;
			if (data1[i] < data2[i]) return -1;
			return 1;
		}
		if (data1.length < data2.length) return -1;
		if (data1.length > data2.length) return 1;
		return 0;
	}
}
