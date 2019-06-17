package com.nomad.app.client;

import com.nomad.app.repository.OracleTriggerImpl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Md Shariful Islam
 */
public class SyncClient {

    //args:: [url, user-name, password, table-name]
    public static void main(String[] args) {
        if(args.length != 4) {
            System.out.println("Incorrect argument count. Expected 4 found " + args.length);
            return;
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("triggers_" + args[1] + "_.sql"));
            OracleTriggerImpl oracleTrigger = new OracleTriggerImpl();
            oracleTrigger.writeTrigger(args[0], args[1], args[2], args[3], writer);
            oracleTrigger.process();
            writer.close();
        } catch (IOException iex) {
            iex.printStackTrace();
        }

    }
}
