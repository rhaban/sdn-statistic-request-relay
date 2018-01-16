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

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;
import org.projectfloodlight.openflow.protocol.OFPortStatsReply;
import org.projectfloodlight.openflow.types.OFPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;

import de.tud.kom.srr.RelayModule;
import de.tud.kom.srr.SRRServiceInterface;
import net.floodlightcontroller.core.IOFSwitch;

public class BandwidthTask implements SRRTask {

	private TaskDescription description;
	private Logger logger;
	private long prevTxBytes = -1L;
	private long prevTime = -1L;
	private double lastTxBw = -1D;

	private BandwidthTask(TaskDescription task) throws JSONException {
		this.description = task;
		logger = LoggerFactory.getLogger(BandwidthTask.class);
	}

	@Override
	public void start() {
		try
		{
			NetworkEntity entity = description.getEntity();
			final IOFSwitch sw = RelayModule.getSwitch(entity.getDpid());
			final OFPort p = OFPort.ofInt(entity.getPort());
			long period = description.getPeriod();

			description.setActive(true);
			System.out.println("set active " + description.getTaskId()  + "(" + description.hashCode() + ")");

			do {
				System.out.println("is active - start measurement for " + description.getTaskId()  + "(" + description.hashCode() + ")");
				new Thread(new Runnable() {

					@Override
					public void run() {
						ListenableFuture<List<OFPortStatsReply>> replies = sw
								.writeStatsRequest(sw.getOFFactory().buildPortStatsRequest().setPortNo(p).build());
						try
						{
							List<OFPortStatsReply> values = (List<OFPortStatsReply>) replies.get(period * 3 / 4, TimeUnit.MILLISECONDS);
							handleReply(values, sw);
						} catch (Exception e)
						{
							logger.error("Failure retrieving statistics from switch {} port {}. Exception: {}", sw, e.getMessage());
							e.printStackTrace();
						}

					}
				}).start();
				Thread.sleep(description.getPeriod());
			} while (description.isPeriodic() && description.isActive());

		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	private void handleReply(List<OFPortStatsReply> values, IOFSwitch sw) throws Exception {
		long txBytes = values.iterator().next().getEntries().iterator().next().getTxBytes().getValue();
		long currentTime = System.currentTimeMillis();
		if (prevTime > -1 || prevTxBytes > -1)
		{
			long timeMillis = currentTime - prevTime;
			long bytes = txBytes - prevTxBytes;
			double bandwidth = (double) bytes / (((double) timeMillis) / 1000D);  //bytes per second

			if ((description.getThresholdType().equalsIgnoreCase("delta") && Math.abs(bandwidth - lastTxBw) >= description.getThreshold())
					|| (description.getThresholdType().equalsIgnoreCase("delta_percentage")
							&& (Math.abs((bandwidth - lastTxBw) / lastTxBw) * 100 >= description.getThreshold() || (lastTxBw == 0 && bandwidth != 0)))
					|| (description.getThresholdType().equalsIgnoreCase("absolute") && bandwidth >= description.getThreshold())
					|| (description.getThresholdType().equalsIgnoreCase("none"))
					|| lastTxBw == -1D)
			{
				for(String controller : description.getController())
				{
					SRRServiceInterface.sendMessage(controller, description.getControllerPort(),
							new JSONObject().put("type", "measurement").put("task_id", description.getTaskId()).put("timestamp", currentTime)
							.put("bandwidth", bandwidth).put("unit", "byte per second"));					
				}
				lastTxBw = bandwidth;
			} else
			{
				logger.warn("Not reached threshold (type: {}, theshold: {}, value: {}, previous: {}, percentage: {}",
						new Object[] { description.getThresholdType(), description.getThreshold(), bandwidth, lastTxBw,
								Math.abs((bandwidth - lastTxBw) / lastTxBw) * 100 });
			}
		}
		prevTime = currentTime;
		prevTxBytes = txBytes;
	}

	@Override
	public void stop() {
		description.setActive(false);
	}

	public static BandwidthTask createFromDescription(TaskDescription task) throws JSONException {
		return new BandwidthTask(task);
	}
}
