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

package de.tud.kom.srr.tasks;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

public class NetworkEntity implements JSONString {
	
	private long dpid;
	private int port;

	public NetworkEntity(long dpid, int port) {
		this.dpid = dpid;
		this.port = port;
	}
	

	public long getDpid() {
		return dpid;
	}

	public void setDpid(long dpid) {
		this.dpid = dpid;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public static NetworkEntity parse(JSONObject json) {
		try
		{
			return new NetworkEntity(json.getLong("dpid"), json.getInt("port"));
		} catch (JSONException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String toJSONString() {
		try
		{
			return new JSONObject().put("dpid", dpid).put("port", port).toString();
		} catch (JSONException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public boolean equals(Object o) {
		NetworkEntity e = (NetworkEntity)o;
		return e.port == this.port && e.dpid == this.dpid;
	}
}
