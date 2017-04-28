package test.com.ckjava.io.command;

public class Test {

	public static void main(String[] args) throws Exception {
	    System.out.print("Progress:");
	    for (int i = 1; i <= 100; i++) {
	        System.out.print(i + "%");
	        Thread.sleep(100);
	         
	        for (int j = 0; j <= String.valueOf(i).length(); j++) {
	          //  System.out.print("\b");
	        }
	    }
	    System.out.println();
	}

}
