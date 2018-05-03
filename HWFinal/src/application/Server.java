package application;
	
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;


public class Server extends Application {
	ScrollPane scrollPane = new ScrollPane();
	TextField text = new TextField();
	int waiting = 0;
	int stopper = 0;
	int portNumber = 12345;
	
	@Override
	public void start(Stage primaryStage) {
		text.setEditable(false);
		BorderPane root = new BorderPane(text);
		Scene scene = new Scene(root, 400, 200);
		MenuBar menuBar = new MenuBar();
		Menu menuOpt = new Menu("Stoper");
		MenuItem add = new MenuItem("Stop the server");
		add.setOnAction((ActionEvent t) -> {
            stopper = 1;
            try {
            	Socket s = new Socket("localhost", portNumber); 
            	System.exit(0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        });
		
		menuOpt.getItems().addAll(add);
		menuBar.getMenus().addAll(menuOpt);
		root.setTop(menuBar);
		primaryStage.setScene(scene);
		primaryStage.show();
		text.setText("Waiting");
		
		primaryStage.setOnCloseRequest((WindowEvent event1) -> {
		    try {
		        System.out.println("close");
		        Socket s = new Socket("localhost", portNumber); 
		        System.exit(0);
		    }catch (Exception ex) {
		        
		    }
		});
		
		Thread thread = new Thread(() -> {
			try {
				ServerSocket serverSocket = new ServerSocket(portNumber);
				Socket socket;
				ServerThread newConnection = null;
				while(true){
					
					socket = serverSocket.accept(); 
					if (stopper == 1) {
						break;
					}
					if (waiting == 0) {
						newConnection = new ServerThread(socket);
						text.setText("New waiting");
						waiting += 1;
					} else {
						waiting -= newConnection.join(socket);
						if (waiting == 0) {
							text.setText("Connected");
							newConnection.run();
						} else {
							text.setText("Failure");
						}
					}
					
				}
			} catch (IOException e) {
				System.out.println("error:" + e.getMessage());
				e.printStackTrace();
			}
		}); 
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
