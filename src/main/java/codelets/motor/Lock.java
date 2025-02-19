package codelets.motor;

public class Lock {
	private static final String lockString = "lock";
	private static boolean canRun = true;
	public static boolean canRun() {
		synchronized(lockString.intern()){
			return canRun;
		}
	}
	protected static void setCanRun(boolean bool) {
		synchronized(lockString.intern()){
			canRun = bool;
		}
	}

}
