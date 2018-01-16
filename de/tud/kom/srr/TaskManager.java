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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import de.tud.kom.srr.tasks.BandwidthTask;
import de.tud.kom.srr.tasks.NetworkEntity;
import de.tud.kom.srr.tasks.SRRTask;
import de.tud.kom.srr.tasks.TaskDescription;

public class TaskManager {

	private Map<Integer, TaskDescription> taskDescriptionStore;
	private Map<Integer, SRRTask> taskStore;

	public TaskManager() {
		taskDescriptionStore = new HashMap<>();
		taskStore = new HashMap<>();
	}

	public JSONObject registerTask(JSONObject msg) throws JSONException {
		System.out.println("register task " + System.currentTimeMillis());
		String controllerIp = msg.getString("controller");
		int controllerPort = msg.getInt("controller_port");
		JSONObject task = msg.getJSONObject("task");
		String metric = task.getString("metric");
		String thresholdType = task.getString("threshold_type"); // none, delta, absolute, delta_percentage
		double threshold = task.getDouble("threshold");
		boolean periodic = task.getBoolean("periodic");
		long period = -1L;
		if(periodic)
		{
			period = task.getLong("period");			
		}
		NetworkEntity entity = NetworkEntity.parse(task.getJSONObject("entity"));

		Integer taskId = insertNewTask(controllerIp, controllerPort, metric, thresholdType, threshold, periodic, period, entity);
		taskId = checkForSimilarTask(taskId);
		System.out.println("START: " + taskId);
		startTask(taskId);
		return new JSONObject().put("type","confirm_activate").put("task_id", taskId);
	}

	private synchronized int insertNewTask(String controllerIp, int controllerPort, String metric, String thresholdType, double threshold, boolean periodic,
			long period, NetworkEntity entity) {
		int taskId = generateTaskId();
		System.out.println("Generate id:" + taskId);
		taskDescriptionStore.put(taskId, new TaskDescription(taskId, controllerIp, controllerPort, metric, periodic, period, threshold, thresholdType, entity, false));
		return taskId;
	}

	private int checkForSimilarTask(Integer taskId) {
		TaskDescription newTask = taskDescriptionStore.get(taskId);
		for(TaskDescription alreadyExistingTask : taskDescriptionStore.values()){
			if(newTask != alreadyExistingTask && // not the same
					(newTask.getMetric().equals(alreadyExistingTask.getMetric())) &&
					newTask.getEntity().equals(alreadyExistingTask.getEntity())
					){
				System.out.println("Found matching task: " + alreadyExistingTask.getTaskId());
				// activate one, deactivate the other, inform twice
				TaskDescription active = newTask.getPeriod() > alreadyExistingTask.getPeriod() ? alreadyExistingTask : newTask;
				TaskDescription inactive = active == newTask ? alreadyExistingTask : newTask;
				taskDescriptionStore.get(inactive.getTaskId()).setActive(false);
				System.out.println("Set inactive: " + inactive.getTaskId() + "(" + inactive.hashCode() + ")");
				active.addControllers(inactive.getController());
				return active.getTaskId();
			}
		}
		System.out.println("Found no matching task");
		return taskId;
	}

	private void startTask(Integer taskId) throws JSONException {
		TaskDescription taskDescription = taskDescriptionStore.get(taskId);
		SRRTask task = null;
		if(taskDescription.getMetric().equalsIgnoreCase("bandwidth"))
		{
			task = BandwidthTask.createFromDescription(taskDescription);
		}
		task.start();
		taskStore.put(taskId, task);
	}

	private synchronized int generateTaskId() {
		int id = -1;
		for (Iterator<Integer> it = taskDescriptionStore.keySet().iterator(); it.hasNext();)
		{
			id = Math.max(id, it.next());
		}
		return id + 1;
	}

	public JSONObject unregisterTask(JSONObject msg) throws JSONException {
		int taskId = msg.getInt("taskId");
		
		taskDescriptionStore.get(taskId).setActive(false);
		return new JSONObject().put("type","confirm_deactivate").put("task_id", taskId);
	}

}
