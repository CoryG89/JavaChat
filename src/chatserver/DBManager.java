package chatserver;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


/** DBManager.java
 * 
 * This class manages the connection and interaction with the database.
 * This is based on the implementation at OWASP here:
 * 
 *   https://www.owasp.org/index.php/Hashing_Java
 * 
 * @author Cory Gross (CoryG89@gmail.com)
 * @version October 25, 2012
 *
 */
public class DBManager {
	private Connection db;
	private String url;
	private String username;
	private String password;
	
	private final static int ITERATION_NUMBER = 1000;
	
	public DBManager(String host, String user, String pass) {
		url = host;
	    username = user;
	    password = pass;

        try {
            db = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
        	System.err.println(e);
            e.printStackTrace();
        }
        
        System.out.println("Log: DBManager -- Connection to database established");
	}

   /**
    * Authenticates the user with a given login and password
    * If password and/or login is null then always returns false.
    * If the user does not exist in the database returns false.
    *
    * @param username String The login of the user
    * @param password String The password of the user
    * @return boolean Returns true if the user is authenticated, false otherwise
    * @throws SQLException If the database is inconsistent or unavailable (
    *           (Two users with the same login, salt or digested password altered etc.)
    * @throws NoSuchAlgorithmException If the algorithm SHA-1 is not supported by the JVM
    */
   public boolean authenticate(String username, String password) throws SQLException, NoSuchAlgorithmException
   {
       PreparedStatement ps = null;
       ResultSet rs = null;
       try {
           boolean userExist = true;
           // INPUT VALIDATION
           if (username == null || password == null) {
               // TIME RESISTANT ATTACK
               // Computation time is equal to the time needed by a legitimate user
               userExist = false;
               username = "";
               password = "";
           }
 
           ps = db.prepareStatement("SELECT PASSWORD, SALT FROM CREDENTIAL WHERE LOGIN = ?");
           ps.setString(1, username);
           rs = ps.executeQuery();
           String digest, salt;
           if (rs.next()) {
               digest = rs.getString("PASSWORD");
               salt = rs.getString("SALT");
               // DATABASE VALIDATION
               if (digest == null || salt == null) {
                   throw new SQLException("Database inconsistant Salt or Digested Password altered");
               }
               if (rs.next()) { // Should not append, because login is the primary key
                   throw new SQLException("Database inconsistent two CREDENTIALS with the same LOGIN");
               }
           } else {
        	   // TIME RESISTANT (even if the user does not exist the
               // computation time is equal to the time needed for a legitimate user)
        	   System.out.println("Log: Attempted login -> Username (" + username);
               digest = "000000000000000000000000000=";
               salt = "00000000000=";
               userExist = false;
           }
 
           byte[] bDigest = base64ToByte(digest);
           byte[] bSalt = base64ToByte(salt);
 
           // Compute the new DIGEST
           byte[] proposedDigest = getHash(ITERATION_NUMBER, password, bSalt);
           
           return Arrays.equals(proposedDigest, bDigest) && userExist;
       } catch (IOException ex){
           throw new SQLException("Database inconsistant Salt or Digested Password altered");
       }
       finally{
           close(rs);
           close(ps);
       }
   }
 
   /**
    * Determines if a given username exists in the database
    * 
    * @param username
    * @return boolean Returns true if the username exists in the database.
    */
   public boolean userExists(String username) {
	   boolean exists = false;
	   
	   PreparedStatement ps = null;
	   ResultSet rs = null;
	   
	   try {
	       ps = db.prepareStatement("SELECT 1 FROM CREDENTIAL WHERE LOGIN='" + username + "' LIMIT 1");
	       rs = ps.executeQuery();
	       if (rs.next()) exists = true;
	   } catch (SQLException e) {
		   System.err.println(e);
		   e.printStackTrace();
	   }
	   
	   return exists;
   }
   
 
   /**
    * Inserts a new user in the database
    * 
    * @param username String The login of the user
    * @param password String The password of the user
    * @return boolean Returns true if the login and password are ok (not null and length(login)<=100
    * @throws SQLException If the database is unavailable
    * @throws NoSuchAlgorithmException If the algorithm SHA-1 or the SecureRandom is not supported by the JVM
    */
   public boolean createUser(String username, String password) throws SQLException, NoSuchAlgorithmException
   {	   
       PreparedStatement ps = null;
       try {
           if (username != null && password != null && username.length() <= 100) {
        	   
               // Uses a secure Random not a simple Random
               SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
               
               // Salt generation 64 bits long
               byte[] bSalt = new byte[8];
               random.nextBytes(bSalt);
               
               // Digest computation
               byte[] bDigest = getHash(ITERATION_NUMBER, password, bSalt);
               String sDigest = byteToBase64(bDigest);
               String sSalt = byteToBase64(bSalt);
               
               // Insert string data into the credentials table
               ps = db.prepareStatement("INSERT INTO CREDENTIAL (LOGIN, PASSWORD, SALT) VALUES (?,?,?)");
               ps.setString(1,username);
               ps.setString(2,sDigest);
               ps.setString(3,sSalt);
               ps.executeUpdate();
               return true;
           } else {
               return false;
           }
       } finally {
           close(ps);
       }
   }



/**
    * From a password, a number of iterations and a salt,
    * returns the corresponding digest
    * 
    * @param iterationNb int The number of iterations of the algorithm
    * @param password String The password to encrypt
    * @param salt byte[] The salt
    * @return byte[] The digested password
    * @throws NoSuchAlgorithmException If the algorithm doesn't exist
    */
   public byte[] getHash(int iterationNb, String password, byte[] salt) throws NoSuchAlgorithmException {
       MessageDigest digest = MessageDigest.getInstance("SHA-1");
       digest.reset();
       digest.update(salt);
       byte[] input = null;
       
       try {
           input = digest.digest(password.getBytes("UTF-8"));
       } catch (UnsupportedEncodingException e) {
    	   System.err.println(e);
    	   e.printStackTrace();
       }
		
       for (int i = 0; i < iterationNb; i++) {
           digest.reset();
           input = digest.digest(input);
       }
       return input;
   }
 
   /**
    * Closes the current statement
    * 
    * @param ps Statement
    */
   public void close(Statement ps) {
       if (ps!=null){
           try {
               ps.close();
           } catch (SQLException ignore) {
           }
       }
   }
 
   /**
    * Closes the current result set
    * 
    * @param ps Statement
    */
   public void close(ResultSet rs) {
       if (rs!=null){
           try {
               rs.close();
           } catch (SQLException ignore) {
           }
       }
   }
 
   /**
    * From a base 64 representation, returns the corresponding byte[] 
    * 
    * @param data String The base64 representation
    * @return byte[]
    * @throws IOException
    */
   public static byte[] base64ToByte(String data) throws IOException {
       BASE64Decoder decoder = new BASE64Decoder();
       return decoder.decodeBuffer(data);
   }
 
   /**
    * From a byte[] returns a base 64 representation
    * 
    * @param data byte[]
    * @return String
    * @throws IOException
    */
   public static String byteToBase64(byte[] data){
       BASE64Encoder endecoder = new BASE64Encoder();
       return endecoder.encode(data);
   }
}

