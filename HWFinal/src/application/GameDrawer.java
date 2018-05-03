package application;



import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class GameDrawer {
	int width;
	int height;
	int door;
	int diameter;
	int scoreWidth;
	int scoreHeight;
	int selfScore = 0;
	int othersScore = 0;
	
	protected Canvas canvas = new Canvas();
	
	public GameDrawer (int w, int h) {
		canvas.setWidth(w);
		canvas.setHeight(h);
		width = w;
		height = h;
		door = width / 2;
		diameter = door / 4;
		scoreWidth = door / 5;
		scoreHeight = (int)(scoreWidth * 1.5);
	}
	
	public void drawBackground () {
		GraphicsContext g = canvas.getGraphicsContext2D();
		g.setFill(Color.WHITE);
		g.setStroke(Color.WHITE);
		//g.rect(0, 0, width, height);
		g.fillRect(0, 0, width - 1, height - 1);
		
		g.setStroke(Color.BLACK);
		g.strokeRect(0, 0, width - 1, height - 1);
		g.strokeLine(0, height / 2, width - 1, height / 2);
		g.strokeOval((width - door) / 2, (height - door) / 2, door, door);
		
		g.setStroke(Color.WHITE);
		g.strokeLine((width - door) / 2, 0, (width + door) / 2, 0);
		g.strokeLine((width - door) / 2, height - 1, (width + door) / 2, height - 1);
		
		g.setStroke(Color.BLACK);
		g.setFont(new Font(60));
		g.strokeText(String.valueOf(othersScore), (width - scoreWidth) / 2, (height - scoreHeight) / 2 + 10, 40); // why?
		g.strokeText(String.valueOf(selfScore), (width - scoreWidth) / 2, (height / 2) + 60, 40); // why?
	}
	
	public void draw (Point2D self, Point2D other, Point2D ball) {
		drawBackground();
		GraphicsContext g = canvas.getGraphicsContext2D();
		g.setFill(Color.BLUE);
		g.setStroke(Color.BLUE);
		g.fillOval(self.getX() - diameter / 2, self.getY() - diameter / 2, diameter, diameter);
		g.fillOval(other.getX() - diameter / 2, other.getY() - diameter / 2, diameter, diameter);
		g.setFill(Color.GREEN);
		g.setStroke(Color.GREEN);
		g.fillOval(ball.getX() - diameter / 2, ball.getY() - diameter / 2, diameter, diameter);
	}
	
}
