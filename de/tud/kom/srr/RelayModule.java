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

package de.tud.kom.srr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.projectfloodlight.openflow.types.DatapathId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;

public class RelayModule  implements IFloodlightModule, SRRListener {

	private Logger logger;
	private static IOFSwitchService switchService;
	private IFloodlightProviderService floodlightProvider;
	private TaskManager taskManager; 

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		return null;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		l.add(IOFSwitchService.class);
		return l;
	}

	@SuppressWarnings("resource")
	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		switchService = context.getServiceImpl(IOFSwitchService.class);
		logger = LoggerFactory.getLogger(RelayModule.class);
		try
		{
			taskManager = new TaskManager();
			new SRRServiceInterface();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		logger.error("STARTED RELAY MODULE LISTENING TO JSONS ON PORT 7777");
		SRRServiceInterface.registerListener(this);
	}

	@Override
	public void receive(JSONObject msg) {
		logger.error("Received msg from RSS" + msg.toString() + " - ACK!");
		try
		{
			String type = msg.getString("type");
			JSONObject confirmation = null;
			if(type.equalsIgnoreCase("register_task"))
			{
				confirmation = taskManager.registerTask(msg);
			}
			else if (type.equalsIgnoreCase("unregister_task"))
			{
				confirmation = taskManager.unregisterTask(msg);
			}
			SRRServiceInterface.sendMessage(msg.getString("controller"), msg.getInt("controller_port"), confirmation);
		} catch (JSONException e)
		{
			e.printStackTrace();
		}
		
	}
	
	public static IOFSwitch getSwitch(long dpid){
		return switchService.getSwitch(DatapathId.of(dpid));
	}

}
