package com.caurix.distributorauto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TimerTask;

public class SchedulerTask extends TimerTask {
	private Set<AbstractTask> taskSet = new HashSet<AbstractTask>();
	private ArrayList<AbstractTask> toDelete = new ArrayList<AbstractTask>();
	private boolean purge = false;

	public void registerTask(AbstractTask abstractTask) {
		taskSet.add(abstractTask);
	}

	public void clearTasks() {
		purge = true;
	}
	
	public void deleteTask(AbstractTask abstractTask){
		toDelete.add(abstractTask);
	}
	

	@Override
	public void run() {
		if (!purge) {
			for (AbstractTask task : taskSet) {
				if (task.enabled()) {
					task.run();
				} else {
//					toDelete.add(task);
				}
			}

			for (AbstractTask task : toDelete) {
				taskSet.remove(task);
			}

			toDelete.clear();
		}else{
			taskSet.clear();
			purge = false;
		}

	}

}
