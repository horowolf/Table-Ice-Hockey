package application;
	
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;


public class Client extends Application {
	int width = 400;
	int height = 600;
	int door = width / 2;
	int leftDoor = (width - door) / 2;
	int rightDoor = (width + door) / 2;
	int diameter = width / 8; // door = width / 2; diameter = door / 4;
	ServerSocket listen;
	Socket connection;
	DataOutputStream send = null;
	DataInputStream recv = null;
	Point2D selfP = new Point2D(width / 2, height * 3 / 4);
	Point2D otherP = new Point2D(width / 4, height / 4);
	Point2D ballP = new Point2D(width / 2, height / 4);
	int ballDirectionX = 0;
	int ballDirectionY = 0;
	Stage newHostStage = new Stage();
	Stage newConnectStage = new Stage();
	FlowPane newHostPane = new FlowPane();
	FlowPane newConnectPane = new FlowPane();
	Scene newHostScene, newConnectScene;
	TextField addressTextC, portTextC, portTextH;
	Button newHostButton = new Button("Listen");
	Button newConnectButton = new Button("Connect");
	Thread thread;
	int drawing = 0;
	
	GameDrawer drawer = new GameDrawer(width, height);
	Canvas canvas = drawer.canvas;
	
	protected MouseEvent getMouse;
	
	@Override
	public void start(Stage primaryStage) {
		addressTextC = new TextField();
		portTextC = new TextField();
		portTextH = new TextField();
		newHostPane.getChildren().addAll(portTextH, newHostButton);
		newConnectPane.getChildren().addAll(addressTextC, portTextC, newConnectButton);
		newHostScene = new Scene(newHostPane, 200, 200);
		newConnectScene = new Scene(newConnectPane, 200, 200);
		newHostStage.setScene(newHostScene);
		newConnectStage.setScene(newConnectScene);
		newHostButton.setOnAction((ActionEvent t) -> {
			try {
				int port = Integer.parseInt(portTextH.getText());
				drawer.selfScore = 0;
	            drawer.othersScore = 0;
				homing(port);
			} catch (NumberFormatException e) {
				System.out.println("Fail to get port number");
			}
            newHostStage.close();
        });
		newConnectButton.setOnAction((ActionEvent t) -> {
			try {
				String addr = addressTextC.getText();
				int port = Integer.parseInt(portTextC.getText());
				drawer.selfScore = 0;
	            drawer.othersScore = 0;
				visiting(addr, port);
			}  catch (NumberFormatException e) {
				System.out.println("Fail to get port number");
			}
            
            newConnectStage.close();
        });
		
		try {
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root, width + 10, height + 40);
			drawer.drawBackground();
			//drawer.draw(new Point2D(30.0, 70.0), new Point2D(280.0, 450.0), new Point2D(230.0, 303.0));
			MenuBar menuBar = new MenuBar();
			Menu menuOpt = new Menu("Connections");
			MenuItem add = new MenuItem("Connect to server");
			add.setOnAction((ActionEvent t) -> {
	            connectToServer();
	            drawer.selfScore = 0;
	            drawer.othersScore = 0;
	        });
			menuOpt.getItems().addAll(add);
			add = new MenuItem("Be a Host");
			add.setOnAction((ActionEvent t) -> {
				newHostStage.showAndWait();
	        });
			menuOpt.getItems().addAll(add);
			add = new MenuItem("Connect to host");
			add.setOnAction((ActionEvent t) -> {
				newConnectStage.showAndWait();
	        });
			menuOpt.getItems().addAll(add);
			
			menuBar.getMenus().addAll(menuOpt);
			root.setTop(menuBar);
			root.setCenter(canvas);
			canvas.setOnMouseDragged(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					// TODO Auto-generated method stub
					double x = event.getX();
					double y = event.getY();
					if (x < diameter / 2) x = diameter / 2;
					if (x > width - (diameter / 2) - 1) x = width - (diameter / 2) - 1;
					if (y < (height + diameter) / 2) y = (height + diameter) / 2;
					if (y > height - (diameter / 2) - 1) y = height - (diameter / 2) - 1;
					
					selfP = new Point2D(x, y);
					callDrawing();
					//drawer.draw(selfP, otherP, ballP);
					
				}
				
			});
			canvas.setOnMouseMoved(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					// TODO Auto-generated method stub
					double x = event.getX();
					double y = event.getY();
					if (x < diameter / 2) x = diameter / 2;
					if (x > width - (diameter / 2) - 1) x = width - (diameter / 2) - 1;
					if (y < (height + diameter) / 2) y = (height + diameter) / 2;
					if (y > height - (diameter / 2) - 1) y = height - (diameter / 2) - 1;
					
					selfP = new Point2D(x, y);
					callDrawing();
					//drawer.draw(selfP, otherP, ballP);
				}
				
			});
			
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		primaryStage.setOnCloseRequest((WindowEvent event1) -> {
		    try {
		        System.out.println("close");
		        send.writeUTF("End");
		    }catch (Exception ex) {
		        
		    }
		});
		
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	public void homing (int port) {
		try {
			listen = new ServerSocket(port);
			connection = listen.accept();
			listen.close();
			recv = new DataInputStream(connection.getInputStream());
        	send = new DataOutputStream(connection.getOutputStream());
        	
        	send.writeUTF("Ready");
        	gamingAsHome();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void visiting (String address, int port) {
		try {
			connection = new Socket(address, port);
			recv = new DataInputStream(connection.getInputStream());
        	send = new DataOutputStream(connection.getOutputStream());
        	String get = recv.readUTF();
        	if (get.equals("Ready")) {
        		gamingAsVisit();
        	} else {
        		//failure
        		System.out.println("visit fail");
        	}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void connectToServer () {
		// decide homing or visiting
		try {
			connection = new Socket("horowolf.ddns.net", 12345);
			recv = new DataInputStream(connection.getInputStream());
        	send = new DataOutputStream(connection.getOutputStream());
        	
        	String get = recv.readUTF();
        	//System.out.println("" + get);
        	if (get.equals("Waiting")) {
        		// waiting for start
        		//System.out.println("Waiting from server");
        		while (true) {
        			get = recv.readUTF();
        			//System.out.println("" + get);
        			if (get.equals("Start")) {
        				gamingAsHome();
        				//System.out.println("Get start from server");
        				break;
        			}
        			
        			
        		}
        	} else if (get.equals("Ready")) {
        		gamingAsVisit();
        	} else {
        		//failure
        	}
        	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void gamingAsHome () {
		int readyToEnd = 0;
		ballP = new Point2D(width / 2, height / 4);
		ballDirectionX = 0;
		ballDirectionY = 0;
		thread = new Thread(() -> {
			try {
				//connection.setSoTimeout(10);
				send.writeUTF("Start");
				System.out.println("Home start");
				String get = "";
				while (true) {
					System.out.println("Home loop");
					try {
						get = recv.readUTF();
						System.out.println("Home get");
					} catch (SocketException e) {
						System.out.println("Home t-out");
						thread.sleep(10); // timeout
						continue;
					}
					if (get.equals("End")) {
						break;
					}
					String[] token = get.split(" ");
					otherP = new Point2D(Double.parseDouble(token[1]), Double.parseDouble(token[2]));
					System.out.println(get);
					if (drawer.selfScore >= 7 || drawer.othersScore >= 7) {
						send.writeUTF("End");
						break;
					}
					//ball animations  or another thread?
					ballMotion();
					
					String message = "Score " + drawer.selfScore + " " + drawer.othersScore + " point " + (width - selfP.getX()) + " " + (height - selfP.getY()) + " ball " + (width - ballP.getX()) + " " + (height - ballP.getY());
					while (true) {
						try {
							send.writeUTF(message);
							thread.sleep(100);
							break;
						} catch (IOException e) { //do again
						}
					}
					
					System.out.println("Home send");
					callDrawing();
					//drawer.draw(selfP, otherP, ballP);
					//thread.sleep(10);
				}
				
				recv.close();
				send.close();
				connection.close();
			} catch (IOException e) {
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}); 
		thread.start();
		
		
		
		
	}
	
	public void gamingAsVisit () {
		thread = new Thread(() -> {
			try {
				System.out.println("visit start");
				//connection.setSoTimeout(10);
				String get = recv.readUTF();
				
				if (get.equals("Start")) {
					while (true) {
						System.out.println("visit loop");
						while (true) {
							try {
								send.writeUTF("point " + (width - selfP.getX()) + " " + (height - selfP.getY()));
								break;
							} catch (IOException e) { //do again
							}
						}
						
						System.out.println("visit send");
						try {
							get = recv.readUTF();
							System.out.println("visit get");
						} catch (SocketException e) {
							System.out.println("visit t-out");
							thread.sleep(10); // timeout
							continue;
						} 
						if (get.equals("End")) {
							send.writeUTF(get);
							break;
						}
						String[] token = get.split(" ");
						System.out.println(get);
						otherP = new Point2D(Double.parseDouble(token[4]), Double.parseDouble(token[5]));
						ballP = new Point2D(Double.parseDouble(token[7]), Double.parseDouble(token[8]));
						drawer.selfScore = Integer.parseInt(token[2]);
						drawer.othersScore = Integer.parseInt(token[1]);
						callDrawing();
						//drawer.draw(selfP, otherP, ballP);
						
					}
				} else {
					System.out.println("visitor didn't get start");
				}
				recv.close();
				send.close();
				connection.close();
			} catch (IOException e) {
				
			}  catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}); 
		thread.start();
	}
	
	public void callDrawing () {
		if (drawing == 0) {
			drawing++;
			drawer.draw(selfP, otherP, ballP);
			drawing--;
		}
	}
	
	public int ballMotion () {
		int ballX = (int)ballP.getX();
		int ballY = (int)ballP.getY();
		int radius = diameter / 2;
		
		if (ballP.distance(selfP) < diameter) {
			double dx = ballP.getX() - selfP.getX();
			double dy = ballP.getY() - selfP.getY();
			double dr = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
			double cos = ((dy / dr) * (dy / dr)) - ((dx / dr) * (dx / dr));
			double sin = ((dy / dr) * (dx / dr)) *2;
			double speed = (diameter - ballP.distance(selfP)) * 2 + radius * 0.2;
			
			double dirXBuf = cos * (ballDirectionX + selfP.getX() - ballP.getX()) + sin * (ballDirectionY + selfP.getY() - ballP.getY());
			double dirYBuf = sin * (ballDirectionX + selfP.getX() - ballP.getX()) - cos * (ballDirectionY + selfP.getY() - ballP.getY());
			double dirRBuf = Math.sqrt(Math.pow(dirXBuf, 2) + Math.pow(dirYBuf, 2));
			
			
			ballDirectionX = (int) ((dirXBuf * speed/ dirRBuf));
			ballDirectionY = (int) ((dirYBuf * speed/ dirRBuf));
			
		} else if (ballP.distance(otherP) < diameter) {
			double dx = ballP.getX() - otherP.getX();
			double dy = ballP.getY() - otherP.getY();
			double dr = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
			double cos = ((dy / dr) * (dy / dr)) - ((dx / dr) * (dx / dr));
			double sin = ((dy / dr) * (dx / dr)) *2;
			double speed = (diameter - ballP.distance(otherP)) * 2 + radius * 0.2;
			
			double dirXBuf = cos * (ballDirectionX + otherP.getX() - ballP.getX()) + sin * (ballDirectionY + otherP.getY() - ballP.getY());
			double dirYBuf = sin * (ballDirectionX + otherP.getX() - ballP.getX()) - cos * (ballDirectionY + otherP.getY() - ballP.getY());
			double dirRBuf = Math.sqrt(Math.pow(dirXBuf, 2) + Math.pow(dirYBuf, 2));
			
			
			ballDirectionX = (int) ((dirXBuf / dirRBuf) * speed);
			ballDirectionY = (int) ((dirYBuf / dirRBuf) * speed);
		}
		
		if ((ballX + ballDirectionX ) < radius) {
			ballX = diameter - (int)(ballX + ballDirectionX );
			ballDirectionX *= -1;
		} else if ((ballX + ballDirectionX ) > width - radius) {
			ballX = (width * 2) - diameter - (int)(ballX + ballDirectionX );
			ballDirectionX *= -1;
		} else {
			ballX = (int)(ballX + ballDirectionX );
		}
		
		if ((ballY + ballDirectionY ) < radius) {
			int ballD = ballX * ballDirectionY - ballY * ballDirectionX;
			int leftD = leftDoor * ballDirectionY - 0 * ballDirectionX;
			int rightD = rightDoor * ballDirectionY - 0 * ballDirectionX;
			double unitD = Math.sqrt((Math.pow(ballDirectionX, 2) + Math.pow(ballDirectionY, 2)));
			System.out.println("b:" + ballD + " L:" + leftD + " R:" + rightD + " U:" + unitD);
			if ((ballD - leftD) / unitD < -25 && (rightD - ballD) / unitD < -25) {
				if ((ballY + ballDirectionY ) < 0) {
					ballX = width / 2;
					ballY = height / 4;
					ballDirectionX = 0;
					ballDirectionY = 0;
					//score
					drawer.selfScore++;
					if (drawer.selfScore >= 7) {
						//stop and win
						return 1;
					}
				} else {
					ballY = (int)(ballY + ballDirectionY );
				}
			} else {
				ballY = diameter - (int)(ballY + ballDirectionY );
				ballDirectionY *= -1;
			}
			
			
			
		} else if ((ballY + ballDirectionY ) > height - radius) {
			int ballD = ballX * ballDirectionY - ballY * ballDirectionX;
			int leftD = leftDoor * ballDirectionY - (height - 1) * ballDirectionX;
			int rightD = rightDoor * ballDirectionY - (height - 1) * ballDirectionX;
			double unitD = Math.sqrt((Math.pow(ballDirectionX, 2) + Math.pow(ballDirectionY, 2)));
			System.out.println("b:" + ballD + " L:" + leftD + " R:" + rightD + " U:" + unitD);
			if ((ballD - leftD) / unitD > 25 && (rightD - ballD) / unitD > 25) {
				if ((ballY + ballDirectionY ) > height) {
					ballX = width / 2;
					ballY = 3 * height / 4;
					ballDirectionX = 0;
					ballDirectionY = 0;
					//score
					drawer.othersScore++;
					if (drawer.othersScore >= 7) {
						//stop and win
						return 1;
					}
				} else {
					ballY = (int)(ballY + ballDirectionY );
				}
			} else {
				ballY = (height * 2) - diameter - (int)(ballY + ballDirectionY );
				ballDirectionY *= -1;
			}
			
			
		} else {
			ballY = (int)(ballY + ballDirectionY );
		}
		
		
		System.out.println("Ball Point" + ballX + " " + ballY);
		ballP = new Point2D(ballX, ballY);
		return 0;
	}
}
