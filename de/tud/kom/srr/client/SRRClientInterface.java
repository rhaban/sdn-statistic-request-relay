/**
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at
      http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
    
    
    @author Rhaban Hark
**/

package de.tud.kom.srr.client;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import de.tud.kom.srr.SRRListener;
import de.tud.kom.srr.SRRServiceInterface;

public class SRRClientInterface extends ServerSocket {
	
	private LinkedList<SRRListener> listeners;

	public SRRClientInterface(int port) throws IOException {
		super(port);
		init();
		new Thread(new Runnable() {
			@Override
			public void run() {
				listen();
			}
			@Override
			public void finalize() throws IOException {
				SRRClientInterface.super.close();
			}
		}).start();
	}
	
	private void init() {
		this.listeners = new LinkedList<SRRListener>();
	}

	public void registerListener(SRRListener l){
		this.listeners.add(l);
	}

	private void listen() {
		while(true)
		{
			try
			{
				Socket socket = accept();
				JSONTokener tokenizer = new JSONTokener(new InputStreamReader(socket.getInputStream()));
				JSONObject object = (JSONObject) tokenizer.nextValue();
				for (Iterator<SRRListener> iterator = listeners.iterator(); iterator.hasNext();)
				{
					SRRListener rssListener = (SRRListener) iterator.next();
					rssListener.receive(object);
				}
			} catch (IOException e)
			{
				e.printStackTrace();
			} catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void sendMessage(JSONObject msg) {
		try
		{
			Socket s = new Socket("127.0.0.1", SRRServiceInterface.DEFAULT_PORT);
			OutputStreamWriter writer = new OutputStreamWriter(s.getOutputStream());
			writer.write(msg.toString());
			writer.close(); // flushes first
			s.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
