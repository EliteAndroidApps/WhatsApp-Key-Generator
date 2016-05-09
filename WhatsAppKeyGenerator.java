import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class WhatsAppKeyGenerator {

	  final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	  
	  private static String bytesToHex(byte[] bytes) {
			char[] hexChars = new char[bytes.length * 2];
		    for ( int j = 0; j < bytes.length; j++ ) {
		        int v = bytes[j] & 0xFF;
		        hexChars[j * 2] = hexArray[v >>> 4];
		        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		    }
		    return new String(hexChars);
		}

	  private static byte[] hexToBytes(String s) {
			int l = s.length();
			byte[] b = new byte[l / 2];
		    for (int i = 0; i < l; i += 2)
		      b[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		    return b;
		}

	  private static byte[] getRandom(int i) throws NoSuchAlgorithmException {
			byte[] b = new byte[i];
			SecureRandom.getInstance("SHA1PRNG").nextBytes(b);
			return b;
		}

	  private static String generateRandomIV() throws NoSuchAlgorithmException {
		  return bytesToHex(getRandom(16));
		}

	  private static String generateRandomKey() throws NoSuchAlgorithmException {
		  return bytesToHex(getRandom(32));
		}
	  
	  private static String getStaticHex(String seed, String hex) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		  return bytesToHex(staticHex(seed, hex));
		}
	  
	  private static byte[] staticHex(String seed, String hex) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		  byte[] challenge = hexToBytes(hex);
	      MessageDigest locMessageDigest = MessageDigest.getInstance("SHA-256");
	      byte[] challengeBytes = seed.getBytes("UTF-8");
	      int i = challengeBytes.length;
	      byte[] challengeFinalBytes = new byte[i + challenge.length];
	      System.arraycopy(challengeBytes, 0, challengeFinalBytes, 0, challengeBytes.length);
	      System.arraycopy(challenge, 0, challengeFinalBytes, i, challenge.length);
	      locMessageDigest.reset();
	      locMessageDigest.update(challengeFinalBytes);
	      return locMessageDigest.digest();
		}
	  
	  private static String generateKey(String seed, String imei) throws NoSuchAlgorithmException, UnsupportedEncodingException {

		  if (seed.length() == 0) {
			  return ("\nMissing Account Seed\n");
		  }
		  else if (!imei.matches("^[0-9]{15}$")) {
			  return ("\nInvalid IMEI Number (Expecting 15 Digits)\n");
		  }
		  else {

			  String randomIV = generateRandomIV();
			  String staticIV = generateRandomIV();
			  String randomKey = generateRandomKey();
			  String staticKey = getStaticHex(seed,randomKey);
			  String pairedKey = getStaticHex(imei,randomKey);

			  return ("\nMode: WhatsApp Crypt Key Generation"+
					  "\n\nAccount Seed: "+seed+
					  "\nDevice IMEI: "+imei+
					  "\nPassword: See \"/data/data/com.whatsapp/files/rc\" or \"/sdcard/WhatsApp/Profile Pictures/.nomedia\" (0-26 of key)"+
					  "\nPadding: Multiple variations \"0x0,0x0,0x1 / 0x0,0x1,0x1 / 0x0,0x1,0x2 / Etc\" (27-30 of key or 0-3 of enc db)"+
					  "\nRandom Key: "+randomKey+" (31-62 of key or 4-35 of enc db)"+
					  "\nRandom IV: "+randomIV+" (unused since WhatsApp v2.12.38) (63-78 of key or 36-51 of enc db)"+
					  "\nPaired Key: "+pairedKey+" (79-110 of key / internal marriage)"+
					  "\nStatic IV: "+staticIV+" (key zeroed WhatsApp >= 2.12.38) (111-126 of key or 52-67 of enc db)"+
					  "\nStatic Key: "+staticKey+" (127-158 of key) **DECRYPTION KEY**"+
					  "\n\nAuthor: TripCode\n");
		  	}
		}

	  private static String retrieveKey(String seed, String imei, String file) throws NoSuchAlgorithmException, IOException {

		  File dbFile = new File(file);

		  if (seed.length() == 0) {
			  return ("\nMissing Account Seed\n");
		  }
		  else if (!imei.matches("^[0-9]{15}$")) {
			  return ("\nInvalid IMEI Number (Expecting 15 Digits)\n");
		  }
		  else if (file.length() == 0) {
			  return ("\nFile Unspecified\n");
		  }
		  else if (!dbFile.exists()) {
			  return ("\nFile Not Found\n");
		  }
		  else {
			  
			  InputStream DB = new BufferedInputStream(new FileInputStream(dbFile));
			  byte[] Data = new byte[67];
			  DB.read(Data);
			  byte[] rKey = new byte[32];
			  System.arraycopy(Data, 3, rKey, 0, 32);
			  byte[] rIV = new byte[16];
			  System.arraycopy(Data, 35, rIV, 0, 16);
			  byte[] sIV = new byte[16];
			  System.arraycopy(Data, 51, sIV, 0, 16);
			  DB.close();
			  
			  String randomIV = bytesToHex(rIV);
			  String staticIV = bytesToHex(sIV);
			  String randomKey = bytesToHex(rKey);
			  String staticKey = getStaticHex(seed,randomKey);
			  String pairedKey = getStaticHex(imei,randomKey);

			  return ("\nMode: WhatsApp Crypt Key Recovery"+
					  "\n\nAccount Seed: "+seed+
					  "\nDevice IMEI: "+imei+
					  "\nPassword: See \"/data/data/com.whatsapp/files/rc\" or \"/sdcard/WhatsApp/Profile Pictures/.nomedia\" (0-26 of key)"+
					  "\nPadding: Multiple variations \"0x0,0x0,0x1 / 0x0,0x1,0x1 / 0x0,0x1,0x2 / Etc\" (27-30 of key or 0-3 of enc db)"+
					  "\nRandom Key: "+randomKey+" (31-62 of key or 4-35 of enc db)"+
					  "\nRandom IV: "+randomIV+" (unused since WhatsApp v2.12.38) (63-78 of key or 36-51 of enc db)"+
					  "\nPaired Key: "+pairedKey+" (79-110 of key / internal marriage)"+
					  "\nStatic IV: "+staticIV+" (key zeroed WhatsApp >= 2.12.38) (111-126 of key or 52-67 of enc db)"+
					  "\nStatic Key: "+staticKey+" (127-158 of key) **DECRYPTION KEY**"+
					  "\n\nAuthor: TripCode\n");
		  	}
		}

	  public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
		  int c = args.length;
		  if (c == 2)
		  {
			  System.out.print(generateKey(args[0],args[1]));
		  }
		  else if (c == 3)
		  {
			  System.out.print(retrieveKey(args[0],args[1],args[2]));
		  }
		  else
		  {
			  System.out.print("\nUsage:\n\tjava -jar WhatsAppKeyGenerator.jar <Account Seed> <IMEI Number>\n\tjava -jar WhatsAppKeyGenerator.jar <Account Seed> <IMEI Number> <Encrypted Database Path>\n");
		  }
		  
		}
}
