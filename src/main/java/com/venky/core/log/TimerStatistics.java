package com.venky.core.log;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.venky.core.util.Bucket;


public class TimerStatistics {
	private final String context;
	private final Bucket callCount ;
	private final Bucket elapsedTime; 
	private TimerStatistics(String context){
		this.context = context;
		this.callCount = new Bucket();
		this.elapsedTime = new Bucket();
	}
	
	private static ThreadLocal<Map<String,TimerStatistics>> timerStatisticsInThread = new ThreadLocal<Map<String,TimerStatistics>>();
	static Map<String,TimerStatistics> getTimerStatistics(){
		Map<String,TimerStatistics> timerStatistics =  timerStatisticsInThread.get();
		if (timerStatistics == null){
			timerStatistics = new HashMap<String, TimerStatistics>();
			timerStatisticsInThread.set(timerStatistics);
		}
		return timerStatistics;
	}
	
	private static ThreadLocal<Stack<Timer>> timerStackInThread = new ThreadLocal<Stack<Timer>>();
	static Stack<Timer> getTimerStack(){
		Stack<Timer> timerStack = timerStackInThread.get();
		if (timerStack == null){
			timerStack = new Stack<TimerStatistics.Timer>();
			timerStackInThread.set(timerStack);
		}
		return timerStack;
	}

	
	
	private static final Logger cat = Logger.getLogger(TimerStatistics.class.getName()); 
	public String getStatistics(){
		if (!cat.isLoggable(ExtendedLevel.TIMER)){
			return "";
		}
		StringBuilder b = new StringBuilder();
		if (callCount.intValue() > 0){
			b.append(context).append("|").append(callCount.intValue()).append("|").append(elapsedTime.value()).append("|").append(elapsedTime.value()/callCount.intValue());
		}
		return b.toString();
	}
	
	
	public static void dumpStatistics(){
		if (!cat.isLoggable(ExtendedLevel.TIMER)){
			return;
		}
		Map<String,TimerStatistics> timers = getTimerStatistics();
		Iterator<String> i = timers.keySet().iterator();
		SortedSet<TimerStatistics> out = new TreeSet<TimerStatistics>(new Comparator<TimerStatistics>() {

			public int compare(TimerStatistics o1, TimerStatistics o2) {
				return o2.elapsedTime.intValue() - o1.elapsedTime.intValue();
			}
		});
		while (i.hasNext()){
			String e = i.next();
			TimerStatistics t = timers.get(e);
			if (t.callCount.intValue() > 0 ){
				out.add(t);
			}
			i.remove();
		}
		cat.log(ExtendedLevel.TIMER, "--------Timings Dumped-----------");
		for (TimerStatistics s:out){
			cat.log(ExtendedLevel.TIMER, s.getStatistics());
		}
		cat.log(ExtendedLevel.TIMER, "----------------------------------");
	}
	
	public static class Timer {
		public static final Timer DUMMY = new Timer("dummy") {
			@Override
			public void stop(){
				
			}
			@Override
			public void start(){
				
			}
		};
		
		static Timer startTimer(){
			return startTimer(null);
		}
		static Timer startTimer(String ctx){
			return startTimer(ctx,false);
		}
		static Timer startTimer(String ctx,boolean additive){
			if (!cat.isLoggable(ExtendedLevel.TIMER)){
				return DUMMY;
			}
			String context = (ctx == null ? getCaller().toString() : ctx );
			Map<String,TimerStatistics> timerStatistics = getTimerStatistics();
			
			TimerStatistics ts = timerStatistics.get(context);
			if (ts == null){
				ts = new TimerStatistics(context);
				timerStatistics.put(context, ts);
			}
			Timer timer = new Timer(context,additive);
			timer.start();
			return timer;
		}

		private static StackTraceElement getCaller(){
			StackTraceElement[] elements = Thread.currentThread().getStackTrace();
			for (StackTraceElement element: elements){
				if (!element.getClassName().startsWith("com.venky.core.log") && !element.getClassName().startsWith("java")){
					return element;
				}
			}
			throw new RuntimeException("Timer class cannot be timed!");
		}
		
		
		private final String key; 
		private final boolean additive;
		public Timer(String key){
			this(key,false);
		}
		public Timer(String key,boolean additive){
			this.key = key;
			this.additive = additive;
		}
		
		private Long start = null;
		protected void start(){
			Stack<Timer> timerStack = getTimerStack();
			if (!timerStack.isEmpty()){
				Timer previous = timerStack.peek();
				if (!previous.additive){
					previous.suspend();
				}
			}
			timerStack.push(this);
			resume();
		}
		protected void resume(){
			start = System.currentTimeMillis();
		}
		
		public void stop(){
			TimerStatistics statistics = suspend();
			statistics.callCount.increment();
			Stack<Timer> timerStack = getTimerStack();
			Timer last  = null;

			//Pop timer stack till this.
			//List<Timer> removed = new ArrayList<TimerStatistics.Timer>();
			while(last != this && !timerStack.isEmpty()){
				if (last != null){
					cat.warning("Timer " + last.key + " not stopped!");
				}
				last = timerStack.pop();
			}

			if (!timerStack.isEmpty()){
				Timer previous = timerStack.peek();
				if (!previous.additive){
					previous.resume();
				}
			}
		}
		private TimerStatistics suspend(){
			long now = System.currentTimeMillis();
			TimerStatistics statistics = getTimerStatistics().get(key);
			if (start != null){
				statistics.elapsedTime.increment(now - start);
				start = null;
			}
			return statistics;
		}

	}
}
