package gui;

import java.io.IOException;

public class MultipleThreadXml implements Runnable {
	
	String commandToExecute;
	
	public MultipleThreadXml(String commandToExecute){
		this.commandToExecute = commandToExecute;
	}
	
	@Override
	public void run() {
		try {
			Runtime.getRuntime().exec(this.commandToExecute);
			//Thread.sleep(10000);
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

}
