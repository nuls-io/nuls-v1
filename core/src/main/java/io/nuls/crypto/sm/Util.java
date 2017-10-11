package io.nuls.crypto.sm;

import java.math.BigInteger;

public class Util 
{
	/**
	 * ����ת�������紫����ֽ������ֽ����飩������
	 * 
	 * @param num һ����������
	 * @return 4���ֽڵ��Լ�����
	 */
	public static byte[] intToBytes(int num)
	{
		byte[] bytes = new byte[4];
		bytes[0] = (byte) (0xff & (num >> 0));
		bytes[1] = (byte) (0xff & (num >> 8));
		bytes[2] = (byte) (0xff & (num >> 16));
		bytes[3] = (byte) (0xff & (num >> 24));
		return bytes;
	}

	/**
	 * �ĸ��ֽڵ��ֽ�����ת����һ����������
	 * 
	 * @param bytes 4���ֽڵ��ֽ�����
	 * @return һ����������
	 */
	public static int byteToInt(byte[] bytes) 
	{
		int num = 0;
		int temp;
		temp = (0x000000ff & (bytes[0])) << 0;
		num = num | temp;
		temp = (0x000000ff & (bytes[1])) << 8;
		num = num | temp;
		temp = (0x000000ff & (bytes[2])) << 16;
		num = num | temp;
		temp = (0x000000ff & (bytes[3])) << 24;
		num = num | temp;
		return num;
	}

	/**
	 * ������ת�������紫����ֽ������ֽ����飩������
	 * 
	 * @param num һ������������
	 * @return 4���ֽڵ��Լ�����
	 */
	public static byte[] longToBytes(long num) 
	{
		byte[] bytes = new byte[8];
		for (int i = 0; i < 8; i++) 
		{
			bytes[i] = (byte) (0xff & (num >> (i * 8)));
		}

		return bytes;
	}

	/**
	 * ������ת���ֽ������ֽ����飩������
	 * 
	 * @param n
	 * @return
	 */
	public static byte[] byteConvert32Bytes(BigInteger n) 
	{
		byte tmpd[] = (byte[])null;
        if(n == null)
        {
        	return null;
        }
        
        if(n.toByteArray().length == 33)
        {
            tmpd = new byte[32];
            System.arraycopy(n.toByteArray(), 1, tmpd, 0, 32);
        } 
        else if(n.toByteArray().length == 32)
        {
            tmpd = n.toByteArray();
        } 
        else
        {
            tmpd = new byte[32];
            for(int i = 0; i < 32 - n.toByteArray().length; i++)
            {
            	tmpd[i] = 0;
            }
            System.arraycopy(n.toByteArray(), 0, tmpd, 32 - n.toByteArray().length, n.toByteArray().length);
        }
        return tmpd;
	}
	
	/**
	 * ���ֽ������ֽ����飩������ת������
	 * 
	 * @param b
	 * @return
	 */
	public static BigInteger byteConvertInteger(byte[] b)
	{
	    if (b[0] < 0)
	    {
	    	byte[] temp = new byte[b.length + 1];
	    	temp[0] = 0;
	    	System.arraycopy(b, 0, temp, 1, b.length);
	    	return new BigInteger(temp);
	    }
	    return new BigInteger(b);
	}
	
	/**
	 * �����ֽ�������ֵ(ʮ����������)
	 * 
	 * @param bytes
	 * @return
	 */
	public static String getHexString(byte[] bytes) 
	{
		return getHexString(bytes, true);
	}
	
	/**
	 * �����ֽ�������ֵ(ʮ����������)
	 * 
	 * @param bytes
	 * @param upperCase
	 * @return
	 */
	public static String getHexString(byte[] bytes, boolean upperCase) 
	{
		String ret = "";
		for (int i = 0; i < bytes.length; i++) 
		{
			ret += Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1);
		}
		return upperCase ? ret.toUpperCase() : ret;
	}
	
	/**
	 * ��ӡʮ�������ַ���
	 * 
	 * @param bytes
	 */
	public static void printHexString(byte[] bytes) 
	{
		for (int i = 0; i < bytes.length; i++) 
		{
			String hex = Integer.toHexString(bytes[i] & 0xFF);
			if (hex.length() == 1) 
			{
				hex = '0' + hex;
			}
			System.out.print("0x" + hex.toUpperCase() + ",");
		}
		System.out.println("");
	}
	
	/**
	 * Convert hex string to byte[]
	 * 
	 * @param hexString
	 *            the hex string
	 * @return byte[]
	 */
	public static byte[] hexStringToBytes(String hexString) 
	{
		if (hexString == null || hexString.equals("")) 
		{
			return null;
		}
		
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) 
		{
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}
	
	/**
	 * Convert char to byte
	 * 
	 * @param c
	 *            char
	 * @return byte
	 */
	public static byte charToByte(char c) 
	{
		return (byte) "0123456789ABCDEF".indexOf(c);
	}
	
	/**
     * ���ڽ���ʮ�������ַ��������Сд�ַ�����
     */
    private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
 
    /**
     * ���ڽ���ʮ�������ַ�������Ĵ�д�ַ�����
     */
    private static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
 
    /**
     * ���ֽ�����ת��Ϊʮ�������ַ�����
     *
     * @param data byte[]
     * @return ʮ������char[]
     */
    public static char[] encodeHex(byte[] data) {
        return encodeHex(data, true);
    }
 
    /**
     * ���ֽ�����ת��Ϊʮ�������ַ�����
     *
     * @param data        byte[]
     * @param toLowerCase <code>true</code> ������Сд��ʽ �� <code>false</code> �����ɴ�д��ʽ
     * @return ʮ������char[]
     */
    public static char[] encodeHex(byte[] data, boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }
 
    /**
     * ���ֽ�����ת��Ϊʮ�������ַ�����
     *
     * @param data     byte[]
     * @param toDigits ���ڿ��������char[]
     * @return ʮ������char[]
     */
    protected static char[] encodeHex(byte[] data, char[] toDigits) {
        int l = data.length;
        char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }
 
    /**
     * ���ֽ�����ת��Ϊʮ�������ַ���
     *
     * @param data byte[]
     * @return ʮ������String
     */
    public static String encodeHexString(byte[] data) {
        return encodeHexString(data, true);
    }
 
    /**
     * ���ֽ�����ת��Ϊʮ�������ַ���
     *
     * @param data        byte[]
     * @param toLowerCase <code>true</code> ������Сд��ʽ �� <code>false</code> �����ɴ�д��ʽ
     * @return ʮ������String
     */
    public static String encodeHexString(byte[] data, boolean toLowerCase) {
        return encodeHexString(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }
 
    /**
     * ���ֽ�����ת��Ϊʮ�������ַ���
     *
     * @param data     byte[]
     * @param toDigits ���ڿ��������char[]
     * @return ʮ������String
     */
    protected static String encodeHexString(byte[] data, char[] toDigits) {
        return new String(encodeHex(data, toDigits));
    }
 
    /**
     * ��ʮ�������ַ�����ת��Ϊ�ֽ�����
     *
     * @param data ʮ������char[]
     * @return byte[]
     * @throws RuntimeException ���Դʮ�������ַ�������һ����ֵĳ��ȣ����׳�����ʱ�쳣
     */
    public static byte[] decodeHex(char[] data) {
        int len = data.length;
 
        if ((len & 0x01) != 0) {
            throw new RuntimeException("Odd number of characters.");
        }
 
        byte[] out = new byte[len >> 1];
 
        // two characters form the hex value.
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(data[j], j) << 4;
            j++;
            f = f | toDigit(data[j], j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }
 
        return out;
    }
 
    /**
     * ��ʮ�������ַ�ת����һ������
     *
     * @param ch    ʮ������char
     * @param index ʮ�������ַ����ַ������е�λ��
     * @return һ������
     * @throws RuntimeException ��ch����һ���Ϸ���ʮ�������ַ�ʱ���׳�����ʱ�쳣
     */
    protected static int toDigit(char ch, int index) {
        int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new RuntimeException("Illegal hexadecimal character " + ch
                    + " at index " + index);
        }
        return digit;
    }
 
    /**
     * �����ַ���תASCII���ַ���
     * 
     * @param String
     *            �ַ���
     * @return ASCII�ַ���
     */
    public static String StringToAsciiString(String content) {
        String result = "";
        int max = content.length();
        for (int i = 0; i < max; i++) {
            char c = content.charAt(i);
            String b = Integer.toHexString(c);
            result = result + b;
        }
        return result;
    }
    
    /**
     * ʮ������ת�ַ���
     * 
     * @param hexString
     *            ʮ�������ַ���
     * @param encodeType
     *            ��������4��Unicode��2����ͨ����
     * @return �ַ���
     */
    public static String hexStringToString(String hexString, int encodeType) {
        String result = "";
        int max = hexString.length() / encodeType;
        for (int i = 0; i < max; i++) {
            char c = (char) hexStringToAlgorism(hexString
                    .substring(i * encodeType, (i + 1) * encodeType));
            result += c;
        }
        return result;
    }
    
    /**
     * ʮ�������ַ���װʮ����
     * 
     * @param hex
     *            ʮ�������ַ���
     * @return ʮ������ֵ
     */
    public static int hexStringToAlgorism(String hex) {
        hex = hex.toUpperCase();
        int max = hex.length();
        int result = 0;
        for (int i = max; i > 0; i--) {
            char c = hex.charAt(i - 1);
            int algorism = 0;
            if (c >= '0' && c <= '9') {
                algorism = c - '0';
            } else {
                algorism = c - 55;
            }
            result += Math.pow(16, max - i) * algorism;
        }
        return result;
    }
    
    /**
     * ʮ��ת������
     * 
     * @param hex
     *            ʮ�������ַ���
     * @return �������ַ���
     */
    public static String hexStringToBinary(String hex) {
        hex = hex.toUpperCase();
        String result = "";
        int max = hex.length();
        for (int i = 0; i < max; i++) {
            char c = hex.charAt(i);
            switch (c) {
            case '0':
                result += "0000";
                break;
            case '1':
                result += "0001";
                break;
            case '2':
                result += "0010";
                break;
            case '3':
                result += "0011";
                break;
            case '4':
                result += "0100";
                break;
            case '5':
                result += "0101";
                break;
            case '6':
                result += "0110";
                break;
            case '7':
                result += "0111";
                break;
            case '8':
                result += "1000";
                break;
            case '9':
                result += "1001";
                break;
            case 'A':
                result += "1010";
                break;
            case 'B':
                result += "1011";
                break;
            case 'C':
                result += "1100";
                break;
            case 'D':
                result += "1101";
                break;
            case 'E':
                result += "1110";
                break;
            case 'F':
                result += "1111";
                break;
            }
        }
        return result;
    }
    
    /**
     * ASCII���ַ���ת�����ַ���
     * 
     * @param String
     *            ASCII�ַ���
     * @return �ַ���
     */
    public static String AsciiStringToString(String content) {
        String result = "";
        int length = content.length() / 2;
        for (int i = 0; i < length; i++) {
            String c = content.substring(i * 2, i * 2 + 2);
            int a = hexStringToAlgorism(c);
            char b = (char) a;
            String d = String.valueOf(b);
            result += d;
        }
        return result;
    }
    
    /**
     * ��ʮ����ת��Ϊָ�����ȵ�ʮ�������ַ���
     * 
     * @param algorism
     *            int ʮ��������
     * @param maxLength
     *            int ת�����ʮ�������ַ�������
     * @return String ת�����ʮ�������ַ���
     */
    public static String algorismToHexString(int algorism, int maxLength) {
        String result = "";
        result = Integer.toHexString(algorism);

        if (result.length() % 2 == 1) {
            result = "0" + result;
        }
        return patchHexString(result.toUpperCase(), maxLength);
    }
    
    /**
     * �ֽ�����תΪ��ͨ�ַ�����ASCII��Ӧ���ַ���
     * 
     * @param bytearray
     *            byte[]
     * @return String
     */
    public static String byteToString(byte[] bytearray) {
        String result = "";
        char temp;

        int length = bytearray.length;
        for (int i = 0; i < length; i++) {
            temp = (char) bytearray[i];
            result += temp;
        }
        return result;
    }
    
    /**
     * �������ַ���תʮ����
     * 
     * @param binary
     *            �������ַ���
     * @return ʮ������ֵ
     */
    public static int binaryToAlgorism(String binary) {
        int max = binary.length();
        int result = 0;
        for (int i = max; i > 0; i--) {
            char c = binary.charAt(i - 1);
            int algorism = c - '0';
            result += Math.pow(2, max - i) * algorism;
        }
        return result;
    }

    /**
     * ʮ����ת��Ϊʮ�������ַ���
     * 
     * @param algorism
     *            int ʮ���Ƶ�����
     * @return String ��Ӧ��ʮ�������ַ���
     */
    public static String algorismToHEXString(int algorism) {
        String result = "";
        result = Integer.toHexString(algorism);

        if (result.length() % 2 == 1) {
            result = "0" + result;

        }
        result = result.toUpperCase();

        return result;
    }
    
    /**
     * HEX�ַ���ǰ��0����Ҫ���ڳ���λ�����㡣
     * 
     * @param str
     *            String ��Ҫ���䳤�ȵ�ʮ�������ַ���
     * @param maxLength
     *            int �����ʮ�������ַ����ĳ���
     * @return ������
     */
    static public String patchHexString(String str, int maxLength) {
        String temp = "";
        for (int i = 0; i < maxLength - str.length(); i++) {
            temp = "0" + temp;
        }
        str = (temp + str).substring(0, maxLength);
        return str;
    }
    
    /**
     * ��һ���ַ���ת��Ϊint
     * 
     * @param s
     *            String Ҫת�����ַ���
     * @param defaultInt
     *            int ��������쳣,Ĭ�Ϸ��ص�����
     * @param radix
     *            int Ҫת�����ַ�����ʲô���Ƶ�,��16 8 10.
     * @return int ת���������
     */
    public static int parseToInt(String s, int defaultInt, int radix) {
        int i = 0;
        try {
            i = Integer.parseInt(s, radix);
        } catch (NumberFormatException ex) {
            i = defaultInt;
        }
        return i;
    }
    
    /**
     * ��һ��ʮ������ʽ�������ַ���ת��Ϊint
     * 
     * @param s
     *            String Ҫת�����ַ���
     * @param defaultInt
     *            int ��������쳣,Ĭ�Ϸ��ص�����
     * @return int ת���������
     */
    public static int parseToInt(String s, int defaultInt) {
        int i = 0;
        try {
            i = Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            i = defaultInt;
        }
        return i;
    }
    
    /**
     * ʮ�����ƴ�ת��Ϊbyte����
     * 
     * @return the array of byte
     */
    public static byte[] hexToByte(String hex)
            throws IllegalArgumentException {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException();
        }
        char[] arr = hex.toCharArray();
        byte[] b = new byte[hex.length() / 2];
        for (int i = 0, j = 0, l = hex.length(); i < l; i++, j++) {
            String swap = "" + arr[i++] + arr[i];
            int byteint = Integer.parseInt(swap, 16) & 0xFF;
            b[j] = new Integer(byteint).byteValue();
        }
        return b;
    }
    
    /**
     * �ֽ�����ת��Ϊʮ�������ַ���
     * 
     * @param b
     *            byte[] ��Ҫת�����ֽ�����
     * @return String ʮ�������ַ���
     */
    public static String byteToHex(byte b[]) {
        if (b == null) {
            throw new IllegalArgumentException(
                    "Argument b ( byte array ) is null! ");
        }
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xff);
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs.toUpperCase();
    }
    
    public static byte[] subByte(byte[] input, int startIndex, int length) {
		byte[] bt = new byte[length];
		for (int i = 0; i < length; i++) {
			bt[i] = input[i + startIndex];
		}
		return bt;
    }
}
