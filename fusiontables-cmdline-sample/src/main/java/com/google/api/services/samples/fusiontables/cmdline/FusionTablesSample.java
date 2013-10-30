/*
 * Copyright (c) 2012 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.services.samples.fusiontables.cmdline;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.FileCredentialStore;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.fusiontables.Fusiontables;
import com.google.api.services.fusiontables.Fusiontables.Query.Sql;
import com.google.api.services.fusiontables.Fusiontables.Table.Delete;
import com.google.api.services.fusiontables.FusiontablesScopes;
import com.google.api.services.fusiontables.model.Column;
import com.google.api.services.fusiontables.model.Sqlresponse;
import com.google.api.services.fusiontables.model.Table;
import com.google.api.services.fusiontables.model.TableList;

import it.uniurb.disbef.virtualsense.basestation.BaseStationLogger;
import it.uniurb.disbef.virtualsense.basestation.FusionTableGlobalPeopleRecord;
import it.uniurb.disbef.virtualsense.basestation.FusionTableNodeRecord;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * @author Christian Junk
 * 
 */
public class FusionTablesSample {
  
  /**
   * Be sure to specify the name of your application. If the application name is {@code null} or
   * blank, the application will log a warning. Suggested format is "MyCompany-ProductName/1.0".
   */
  private static final String APPLICATION_NAME = "VirtualSenseBaseStation";
  
  /** Global instance of the HTTP transport. */
  private static HttpTransport HTTP_TRANSPORT;

  /** Global instance of the JSON factory. */
  private static final JsonFactory JSON_FACTORY = new JacksonFactory();

  private static Fusiontables fusiontables;

  /** Authorizes the installed application to access user's protected data. */
  private static Credential authorize() throws Exception {
    // load client secrets
    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
        JSON_FACTORY, new InputStreamReader(
            FusionTablesSample.class.getResourceAsStream("/client_secrets.json")));
    if (clientSecrets.getDetails().getClientId().startsWith("Enter")
        || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
      System.out.println(
          "Enter Client ID and Secret from https://code.google.com/apis/console/?api=fusiontables "
          + "into fusiontables-cmdline-sample/src/main/resources/client_secrets.json");
      System.exit(1);
    }
    // set up file credential store
    FileCredentialStore credentialStore = new FileCredentialStore(
        new File(System.getProperty("user.home"), ".credentials/fusiontables.json"), JSON_FACTORY);
    // set up authorization code flow
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets,
        Collections.singleton(FusiontablesScopes.FUSIONTABLES)).setCredentialStore(credentialStore)
        .build();
    // authorize
    return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
  }

  public static void connect() {
    try {
      try {
        HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        // authorization
        Credential credential = authorize();
        // set up global FusionTables instance
        fusiontables = new Fusiontables.Builder(
            HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
        // run commands
        listTables();
        //String tableId = createTable();
        //insertData(tableId);
        //showRows(tableId);
        //deleteTable(tableId);
        // success!
        return;
      } catch (IOException e) {
        System.err.println(e.getMessage());
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
    System.exit(1);
  }

  /**
   * @param tableId
   * @throws IOException
   */
  private static void showRows(String tableId) throws IOException {
    View.header("Showing Rows From Table");

    Sql sql = fusiontables.query().sql("SELECT Text,Number,Location,Date FROM " + tableId);

    try {
      sql.execute();
    } catch (IllegalArgumentException e) {
      // For google-api-services-fusiontables-v1-rev1-1.7.2-beta this exception will always
      // been thrown.
      // Please see issue 545: JSON response could not be deserialized to Sqlresponse.class
      // http://code.google.com/p/google-api-java-client/issues/detail?id=545
    }
  }

  /** List tables for the authenticated user. */
  public static TableList listTables() throws IOException {
    //View.header("Listing My Tables");

    // Fetch the table list
    Fusiontables.Table.List listTables = fusiontables.table().list();
    TableList tableList = listTables.execute();

    if (tableList.getItems() == null || tableList.getItems().isEmpty()) {
      System.out.println("No tables found!");
      return null;
    }

    /*for (Table table : tableList.getItems()) {
      View.show(table);
      View.separator();
    }*/
    return tableList;
  }

  /** Create a table for the authenticated user. */
  private static String createTable() throws IOException {
    View.header("Create Sample Table");

    // Create a new table
    Table table = new Table();
    table.setName(UUID.randomUUID().toString());
    table.setIsExportable(false);
    table.setDescription("Sample Table");

    // Set columns for new table
    table.setColumns(Arrays.asList(new Column().setName("Text").setType("STRING"),
        new Column().setName("Number").setType("NUMBER"),
        new Column().setName("Location").setType("LOCATION"),
        new Column().setName("Date").setType("DATETIME")));

    // Adds a new column to the table.
    Fusiontables.Table.Insert t = fusiontables.table().insert(table);
    Table r = t.execute();

    View.show(r);

    return r.getTableId();
  }

  /** Inserts a row in the newly created table for the authenticated user. */
  public static void insertData(String tableId, long time, 
                                 String counter, String noise, 
                                 String co2, String in, String out, 
                                 String pressure, String temp, String lum) throws IOException {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Sql sql = fusiontables.query().sql("INSERT INTO " + tableId + " (Noise,CO2,Temperature,Pressure,Light, PeopleOut, PeopleIn, Counter,Date)"
        + " VALUES (" + "'"+noise+"', '"+co2+"','"+temp+"', '"+pressure+"','"+lum+"','"+out+"','"+in+"','"+counter+"'"
        + ",'" + format.format(new Date(time)) + "')");
    
    System.out.println(sql.toString());

    try {
      sql.execute();
    } catch (IllegalArgumentException e) {
      // For google-api-services-fusiontables-v1-rev1-1.7.2-beta this exception will always
      // been thrown.
      // Please see issue 545: JSON response could not be deserialized to Sqlresponse.class
      // http://code.google.com/p/google-api-java-client/issues/detail?id=545
    }
  }
  
  /** Inserts a row in the newly created table for the authenticated user. */
  public static void insertDataToGlobalCounter(long time, 
                                 String peopleIn, String peopleOut, 
                                 String peopleInside) throws IOException {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Sql sql = fusiontables.query().sql("INSERT INTO 1bEiHoWTB5Eo5iCMwNJCgf5g7swL4RFGENAt2bR0 (PeopleIn,PeopleOut,PeopleInside,Date)"
        + " VALUES (" + "'"+peopleIn+"', '"+peopleOut+"','"+peopleInside+"', '" + format.format(new Date(time)) + "')");
    
    System.out.println(sql.toString());

    try {
      sql.execute();
    } catch (IllegalArgumentException e) {
      // For google-api-services-fusiontables-v1-rev1-1.7.2-beta this exception will always
      // been thrown.
      // Please see issue 545: JSON response could not be deserialized to Sqlresponse.class
      // http://code.google.com/p/google-api-java-client/issues/detail?id=545
    }
  }

  /** Deletes a table for the authenticated user. */
  private static void deleteTable(String tableId) throws IOException {
    View.header("Delete Sample Table");
    // Deletes a table
    Delete delete = fusiontables.table().delete(tableId);
    delete.execute();
  }

/**
 * @param nr
 */
public static void insertData(FusionTableNodeRecord nr) throws IOException{
	java.text.DecimalFormat format = new java.text.DecimalFormat("0.00");
	
    Table toUpdate = BaseStationLogger.findTableByNodeID(nr.nodeID);    
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Sql sql = fusiontables.query().sql("INSERT INTO " + toUpdate.getTableId() + 
    		" (Noise,CO2,Temperature,Pressure,Light, PeopleOut, PeopleIn, Counter,Date)"
        + " VALUES (" + "'"+format.format(nr.noise)+"', '"+format.format(nr.co2)+"','"+
    		format.format(nr.temperature)+"', '"+format.format(nr.pressure)+"','"+
            format.format(nr.luminosity)+"','"+format.format(nr.out)+"','"+
            format.format(nr.in)+"','"+format.format(nr.counter)+"'"
        + ",'" + dateFormat.format(new Date(System.currentTimeMillis())) + "')");
    
    System.out.println(sql.toString());

    
    sql.execute();
}

public static FusionTableGlobalPeopleRecord initGlobalRecord(FusionTableGlobalPeopleRecord r) throws IOException{
	// read values from global table counter 
    Date now = new Date();
    now.setHours(0);
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Sql sql = fusiontables.query().sql("SELECT Date, PeopleIn, PeopleOut, PeopleInside FROM "
    		+ "1bEiHoWTB5Eo5iCMwNJCgf5g7swL4RFGENAt2bR0 WHERE Date > '"+format.format(now)+"' ORDER BY Date");

    try {
      Sqlresponse rep = sql.execute();
      if(rep != null){
          List<List<Object>> l = rep.getRows();
          if(l!= null){
        	  //get the last element
	          Iterator it = l.get(l.size()-1).iterator();
	          it.next(); // is the date
	          int ii = Integer.parseInt(""+it.next()); // is PeopleIn 
	          int oo = Integer.parseInt(""+it.next()); // is PeopleOut
	          int inside = Integer.parseInt(""+it.next()); // is PeopleInside
	          r.in = (short)ii;
	          r.out = (short) oo;
	          r.inside = inside;
	          System.out.println(" Initializing global object from table - in - "+ii+" -- out -- "+oo+" -- inside -- "+inside);
          }
      }
      
      
    } catch (IllegalArgumentException e) {
      // For google-api-services-fusiontables-v1-rev1-1.7.2-beta this exception will always
      // been thrown.
      // Please see issue 545: JSON response could not be deserialized to Sqlresponse.class
      // http://code.google.com/p/google-api-java-client/issues/detail?id=545
    }
    return r;
}
}
