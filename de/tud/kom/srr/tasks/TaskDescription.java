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

import java.util.ArrayList;
import java.util.List;

public class TaskDescription {

	private int taskId;
	private int referenceTaskId;
	private List<String> controller;
	private int controllerPort;
	private String metric;
	private boolean periodic;
	private long period;
	private double threshold;
	private String thresholdType;
	private NetworkEntity entity;
	private boolean active;

	public TaskDescription(int taskId, String controller, int controllerPort, String metric, boolean periodic, long period, double threshold,
			String thresholdType, NetworkEntity entity, boolean active) {
		super();
		this.taskId = taskId;
		this.controller = new ArrayList<>();
		this.controller.add(controller);
		this.controllerPort = controllerPort;
		this.metric = metric;
		this.periodic = periodic;
		this.period = period;
		this.threshold = threshold;
		this.thresholdType = thresholdType;
		this.entity = entity;
		this.active = active;
		this.referenceTaskId = -1;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public int getReferenceTaskId() {
		return referenceTaskId;
	}

	public void setReferenceTaskId(int referenceTaskId) {
		this.referenceTaskId = referenceTaskId;
	}

	public List<String> getController() {
		return controller;
	}

	public void setController(List<String> controller) {
		this.controller = controller;
	}
	
	public void addController(String controller) {
		this.controller.add(controller);
	}

	public void addControllers(List<String> controller) {
		this.controller.addAll(controller);
	}
	
	public int getControllerPort() {
		return controllerPort;
	}

	public void setControllerPort(int controllerPort) {
		this.controllerPort = controllerPort;
	}

	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
	}

	public boolean isPeriodic() {
		return periodic;
	}

	public void setPeriodic(boolean periodic) {
		this.periodic = periodic;
	}

	public long getPeriod() {
		return period;
	}

	public void setPeriod(long period) {
		this.period = period;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public String getThresholdType() {
		return thresholdType;
	}

	public void setThresholdType(String thresholdType) {
		this.thresholdType = thresholdType;
	}

	public NetworkEntity getEntity() {
		return entity;
	}

	public void setEntity(NetworkEntity entity) {
		this.entity = entity;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
