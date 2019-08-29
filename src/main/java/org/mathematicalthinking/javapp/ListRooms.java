package org.mathematicalthinking.javapp;

import java.lang.reflect.*;

import de.fhg.ipsi.concertchat.servlets.applications.*;
import de.fhg.ipsi.concertchat.persistency.MessageContainer;
import de.fhg.ipsi.concertchat.applications.tabbedChat.whiteboard.TabbedWhiteboardDescriptor;
import de.fhg.ipsi.concertchat.framework.agilo.channel.ChannelMessage;
import de.fhg.ipsi.concertchat.applications.tabbedChat.geogebraBoard.TabbedGeogebraBoardDescriptor;
import de.fhg.ipsi.concertchat.applications.tabbedChat.geogebraBoard.TabbedGeogebraBoard;
import de.fhg.ipsi.concertchat.applications.tabbedChat.ITabbedApplication;

//import org.apache.commons.lang3.builder.*;
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
  public static final String TAB_GEO_BOARD_DESC = "de.fhg.ipsi.concertchat.applications.tabbedChat.geogebraBoard.TabbedGeogebraBoardDescriptor";
  public static void main( String[] args )
  {
    System.out.println( "Hello Java!" );
    // load and register JDBC driver for MySQL
    //Class.forName("com.mysql.jdbc.Driver");
    Connection ccConn = null;
    Connection vmtConn = null;
    try {
      System.out.println( "Try to get connection" );
      // Get a database 'Connection' object
      ccConn = DriverManager.getConnection(
        "jdbc:mysql://localhost:3306/concertchat?serverTimezone=UTC", "root", "");
      vmtConn = DriverManager.getConnection(
          "jdbc:mysql://localhost:3306/vmt?serverTimezone=UTC", "root", "");
      System.out.println( "Got connections" );
      Statement stmtCh = ccConn.createStatement();
      System.out.println( "Got stmtCh" );
      // String strSelectCh = "select * from r4349443A31333438303433393839323931 order by PRIMKEY";
      String strSelectCh = "select * from r4349443A31353236393535323430383835 order by PRIMKEY";
      // String strSelectCh = "select * from r4349443A31353336323831393331343838 order by PRIMKEY";
      System.out.println("The SQL query is: " + strSelectCh);
      // get the ResultSet
      ResultSet rsetCh = stmtCh.executeQuery(strSelectCh);
      // Process the ResultSet
      System.out.println("The records selected are:");
      int rowCountCh = 0;
      while(rsetCh.next()) {   // Move the cursor to the next row, return false if no more row
        try {
          // Integer timeInt = rsetCh.getInt("TIME");
          // String timeStamp = (new SimpleDateFormat("mm:ss:SSS")).format(new Date(timeInt));
          Blob descCh = rsetCh.getBlob("MESSAGE");
          // Print out byte array (from stream) out as HEX
          // byte[] blobBytes = descCh.getBytes(1, (int)descCh.length());
          // StringBuilder sb = new StringBuilder(blobBytes.length * 2);
          // for(byte b: blobBytes)
          //   sb.append(String.format("%02x", b));
          // System.out.println( "Message in Hex: " + sb);

          ObjectInputStream objectInputStream = new ObjectInputStream(descCh.getBinaryStream());
          // channelMessageContainer containes the message in
          MessageContainer channelMessageContainer = (MessageContainer) objectInputStream.readObject();
          //MessageContainer channelMessage = (ChannelMessage) objectInputStream.readObject();
          objectInputStream.close();

          objectInputStream = new ObjectInputStream(descCh.getBinaryStream());
          Object o = objectInputStream.readObject();
          System.out.println("      desCh message class name is: " + o.getClass().getName());
          objectInputStream.close();

          String channelID = channelMessageContainer.getChannelID();
          if (rowCountCh == 0) {
            // get room information from the channel id from the first record
            Statement stmt = vmtConn.createStatement();
            String strSelect = "select * from aroom r "
            + "left join community c on r.CommunityID = c.CommunityID "
            + "left join roomtype rt on r.RoomTypeID = rt.RoomTypeID "
            + "left join atopic t on r.TopicID = t.TopicID "
            + "left join subject s on t.SubjectID = s.SubjectID "
            + "where r.channelID = '" + channelID + "'";
            ResultSet rset = stmt.executeQuery(strSelect);
            int rowCount = 0;
            if (rset.next()) {
              printRoomRec(rset);
            } else {
              System.out.println("missing aroom record");
            }
          }
          System.out.println("   messageID: " + channelMessageContainer.getMessageID());
          System.out.println("      Type: " + channelMessageContainer.getType());
          System.out.println("      TYPE: " + rsetCh.getString("TYPE"));
          System.out.println("      globalIndex: " + channelMessageContainer.getGlobalIndex());
          System.out.println("      TypeIndex: " + channelMessageContainer.getTypeIndex());
          System.out.println("      timestamp: " + channelMessageContainer.getTimeStamp());
          String type = rsetCh.getString("TYPE");
          System.out.println("      message type " + type);
          System.out.println("      ChannelID: " + channelID);
          switch(type) {
            case "TabbedChat_Message" :
              System.out.println("      TabbedChat_Message to be processed.");
              // String message = rsetCh.getBytes("message").toString();
              // System.out.println("      " + message);
              Object o2 = channelMessageContainer.getMessage();
              System.out.println("      o2 message class name is: " + o2.getClass().getName());
              if (o2.getClass().getName() == TAB_GEO_BOARD_DESC) {
                TabbedGeogebraBoardDescriptor tgbd = (TabbedGeogebraBoardDescriptor)channelMessageContainer.getMessage();
                System.out.println("        Descriptor Name: " + tgbd.getName());
                System.out.println("        Descriptor ID: " + tgbd.getID());
                Field initFile = TabbedGeogebraBoardDescriptor.class.getDeclaredField("initFile");
                initFile.setAccessible(true);
                Object initValue = initFile.get(tgbd);
                System.out.println("        initFile: " + initValue);
              }
              // MessageContainer msgContainer = (MessageContainer) o2;
              // System.out.println ("      msgContainer.getMessageID: " + msgContainer.getMessageID());
              // Object o = channelMessageContainer.getMessage();
              // System.out.println("      class name is: " + o.getClass().getName());
              // System.out.println("      messageBytes: " + channelMessageContainer.getMessage());
              //System.out.println("      persistent: " + channelMessageContainer.getPersistent());
              break;
            default :
              System.out.println("      Missing object for this message type.");
          }
        } catch (ClassNotFoundException e) {
          throw new IOException("Class Not Found reading message");
        } catch (IOException e) {
          throw new IOException("IO Exception reading message");
        }
        ++rowCountCh;
      }

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
  private static void printRoomRec(ResultSet rset) {
    try {
      String roomID = rset.getString("roomID");
      String roomName = rset.getString("roomName");
      String communityName = rset.getString("c.CommunityName");
      String roomTypeName = rset.getString("rt.RoomTypeName");
      String topicName = rset.getString("t.TopicName"); //corresponds with vmt.mathforum.org/vmtwiki/index.php/VMTTopics/<TopicName>
      String subjectName = rset.getString("s.SubjectName");
      int subjectOrder = rset.getInt("s.SubjectOrder");
      System.out.println("Room: " + roomID + " - " + roomName);
      System.out.println("      community: " + communityName);
      System.out.println("      room type: " + roomTypeName);
      System.out.println("      topic: " + topicName);
      System.out.println("      subject: " + subjectName);
      System.out.println("      subject order: " + subjectOrder);
    } catch (SQLException ex) {
      // handle any SQL errors
      System.out.println("printRoomRec SQLException: " + ex.getMessage());
      System.out.println("printRoomRec SQLState: " + ex.getSQLState());
      System.out.println("printRoomRec VendorError: " + ex.getErrorCode());
    } catch (Exception ex){
      // handle any other errors
      System.out.println("printRoomRec Exception: " + ex);
    }
  }
}
