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

package de.tud.kom.srr.eval;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

public class EvalCollector {

	private static JSONObject db;

	static
	{
		try
		{
			db = new JSONObject();
			db.put("local", new JSONObject());
			db.put("srr", new JSONObject());
			db.put("exceptions", 0);

			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

				@Override
				public void run() {
					try
					{
						System.out.println("WRITE OUT DUMP...");
						FileWriter fw = new FileWriter("dumps/dump_controller_" + System.currentTimeMillis() + "_" + Math.abs(new Random().nextInt()) + ".json");
						fw.write(db.toString(1));
						fw.close();
					} catch (IOException e)
					{
						e.printStackTrace();
					} catch (JSONException e)
					{
						e.printStackTrace();
					}
				}
			}));
		} catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	public static void localBandwidthMeasurement(long timeMillis, double bandwidth) {
		try
		{
			db.getJSONObject("local").put(String.valueOf(timeMillis), bandwidth);
		} catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	public static void srrBandwidthMeasurement(long timeMillis, double bandwidth) {
		try
		{
			db.getJSONObject("srr").put(String.valueOf(timeMillis), bandwidth);
		} catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	public static void incrementExceptionCounter() {
		try
		{
			db.put("exceptions", db.getInt("exceptions") + 1);
		} catch (JSONException e)
		{
			e.printStackTrace();
		}
	}
}
