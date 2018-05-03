package application;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerThread implements Runnable {
	Socket homeSocket;
	DataInputStream inputFromHome;
	DataOutputStream outputToHome;
	
	Socket visitSocket;
	DataInputStream inputFromVisit;
	DataOutputStream outputToVisit;
	
	public ServerThread (Socket s) {
		homeSocket = s;
		try {
			inputFromHome = new DataInputStream(homeSocket.getInputStream());
			outputToHome = new DataOutputStream(homeSocket.getOutputStream());
			
			outputToHome.writeUTF("Waiting");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	protected int join (Socket s) {
		visitSocket = s;
		try {
			inputFromVisit = new DataInputStream(visitSocket.getInputStream());
			outputToVisit = new DataOutputStream(visitSocket.getOutputStream());
			
			outputToVisit.writeUTF("Ready");
			outputToHome.writeUTF("Start");
			return 1;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		new Thread(() -> {
			try {
				String visitString;
				while (true) {
					visitString = inputFromVisit.readUTF(); 
					if (visitString.equals("End")) {
						outputToHome.writeUTF(visitString);
						outputToVisit.writeUTF(visitString);
						visitSocket.close();
						break;
					}
					outputToHome.writeUTF(visitString);
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Disconnected");
				//e.printStackTrace();
			}
		}).start();
		
		new Thread(() -> {
			String homeString;
			try {
				while (true) {
					homeString = inputFromHome.readUTF(); 
					if (homeString.equals("End")) {
						outputToHome.writeUTF(homeString);
						outputToVisit.writeUTF(homeString);
						homeSocket.close();
						break;
					}
					outputToVisit.writeUTF(homeString);
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Disconnected");
				//e.printStackTrace();
			}
		}).start();
		
		
	}

}
