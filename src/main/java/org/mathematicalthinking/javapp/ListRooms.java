package org.mathematicalthinking.javapp;

import de.fhg.ipsi.concertchat.servlets.applications.*;
import org.apache.commons.lang3.builder.*;
import java.io.*;
import java.sql.*;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//import java.sql.Statement;

/**
 * List the VMT rooms from the channelTable
 */
public class ListRooms
{
  public static void main( String[] args )
  {
    System.out.println( "Hello Java!" );
    // load and register JDBC driver for MySQL
    //Class.forName("com.mysql.jdbc.Driver");
    Connection conn = null;
    try {
      // Get a database 'Connection' object
      conn = DriverManager.getConnection(
        "jdbc:mysql://localhost:3306/concertchat", "root", "");
      System.out.println( "Got connection" );
      // Allocate a 'Statement' object in the Connection
      Statement stmt = conn.createStatement();
      System.out.println( "Got statement" );
      String strSelect = "select * from channelTable";
      System.out.println("The SQL query is: " + strSelect);
      // get the ResultSet
      ResultSet rset = stmt.executeQuery(strSelect);
      // Process the ResultSet
      System.out.println("The records selected are:");
      int rowCount = 0;
      while(rset.next()) {   // Move the cursor to the next row, return false if no more row
        String channelID = rset.getString("channelID");
        String className = rset.getString("applicationClassName");
        System.out.println(channelID + ", " + className);
        // convert description blob from ResultSet to DefaultDescription object
        Blob desc = rset.getBlob("description");
        ObjectInputStream objectInputStream = new ObjectInputStream(desc.getBinaryStream());
        DefaultDescription descObj = (DefaultDescription) objectInputStream.readObject();
        // print out the short description if it is not blank
        String descString = descObj.getShortDescription();
        if (descString != "") {
          System.out.println(descString);
        }
        ++rowCount;
      }
      System.out.println("Total number of records = " + rowCount);
    } catch (SQLException ex) {
      // handle any SQL errors
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
    } catch (Exception ex){
      // handle any other errors
      System.out.println("Exception: " + ex);
    }
  }
}
