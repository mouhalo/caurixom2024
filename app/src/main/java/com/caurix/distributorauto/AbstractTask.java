package com.caurix.distributorauto;

import android.content.Context;

public abstract class AbstractTask {
	protected boolean enabled;
	protected Context ctx = null;
	protected int executeAfterSecs;
	private int timeElapsed = 1;

	public AbstractTask(Context ctx, int executeAfterSecs) {
		super();
		this.ctx = ctx;
		this.executeAfterSecs = executeAfterSecs;
		enabled = true;
	}

	public final void run() {
		if (timeElapsed == executeAfterSecs) {
			execute();
			timeElapsed = 0;
		}
		timeElapsed++;
	}

	protected abstract void execute();

	public boolean enabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (enabled) {
			timeElapsed = 0;
		}
	}

	public int getExecuteAfterSecs() {
		return executeAfterSecs;
	}

	public void setExecuteAfterSecs(int executeAfterSecs) {
		this.executeAfterSecs = executeAfterSecs;
	}
}
