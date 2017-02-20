package lab3EV3Navigation;

public interface UltrasonicController {
	
	public void processUSData(int distance);
	
	public int readUSDistance();
}
//Ready for Open Source