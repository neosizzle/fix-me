package com.jng.callables;

import java.util.concurrent.Callable;

public class CallableFactory {
	// public <T> Callable<T> createCallable()
	// {

	// }

	public Callable<Integer> createDefaultCallable()
	{
		return new Callable<Integer>() {
			public Integer call() throws Exception
			{
				System.out.println("Default callable.");
				return 0;
			}
		};
	}
}
