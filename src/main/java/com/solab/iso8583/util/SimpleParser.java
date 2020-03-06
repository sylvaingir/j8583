/*
 * j8583 A Java implementation of the ISO8583 protocol
 * Copyright (C) 2007 Enrique Zamudio Lopez
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
package com.solab.iso8583.util;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.parse.ConfigParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;

/** A simple command-line program that reads a configuration file to set up a MessageFactory
 * and parse messages read from STDIN.
 *
 * @author Enrique Zamudio
 *         Date: 20/06/12 02:11
 */
public class SimpleParser {

    private static BufferedReader reader;

    private static String getMessage() throws IOException {
        if (reader == null) {
            reader = new BufferedReader(new InputStreamReader(System.in));
        }
        System.out.println("Paste your ISO8583 message here (no ISO headers): ");
        return reader.readLine();
    }

    public static void main(String [] args) throws IOException, ParseException {
        final MessageFactory<IsoMessage> mf = new MessageFactory<IsoMessage>();
        if (args.length == 0) {
            ConfigParser.configureFromDefault(mf);
        } else {
            if (System.console() != null) {
                System.console().printf("Attempting to configure MessageFactory from %s...%n", args[0]);
            }
            String url = args[0];
            if (url.contains("://")) {
                ConfigParser.configureFromUrl(mf, new URL(args[0]));
            } else {
                ConfigParser.configureFromUrl(mf, new File(url).toURI().toURL());
            }
        }
        //Now read messages in a loop
        String line = getMessage();
        while (line != null && line.length() > 0) {
            IsoMessage m = mf.parseMessage(line.getBytes(), 0);
            if (m != null) {
                System.out.printf("Message type: %04x%n", m.getType());
                System.out.println("FIELD TYPE    VALUE");
                for (int i = 2; i <= 128; i++) {
                    IsoValue<?> f = m.getField(i);
                    if (f != null) {
                        System.out.printf("%5d %-7s (%4d) [", i, f.getType(), f.getLength());
                        System.out.print(f.toString());
                        System.out.println(']');
                    }
                }
            }
            line = getMessage();
        }
    }
}
