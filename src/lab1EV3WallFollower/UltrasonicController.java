package lab1EV3WallFollower;

public interface UltrasonicController {
	
	public void processUSData(int distance);
	
	public int readUSDistance();
}
