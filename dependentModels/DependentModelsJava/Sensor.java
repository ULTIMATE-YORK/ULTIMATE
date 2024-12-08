import java.util.Random;

public class Sensor {
	private boolean detected;
	private Random rnd;
	private double nLargeObjects;
	private double nLargeObjectsDetected;
	private double nDetected;
	private double nExecutions;
	
	public Sensor() {
		this.rnd = new Random();
		this.detected = false;
		this.nLargeObjects = 0;
		this.nLargeObjectsDetected = 0;
		this.nDetected = 0;
		this.nExecutions = 0;
	}
	
	public void detectObject(int lightLevel) {
		// 0. Count executions
		this.nExecutions++;
		
		// 1. Draw object type from required distribution
		int object;
		double x = rnd.nextDouble();
		if (x < 0.8) {
			object = 0;
		}
		else if (x < 0.95) {
			object = 1;
		}
		else {
			object = 2;
			this.nLargeObjects++;
		}
		
		// 2. Sensing step
		//
		//  situation       low_light    med_light    high_light 
		//  -------------------------------------------------------
		//  no object          0.1          0.02          0
		//  small object       0.25         0.1           0.05
		//  large object       0.3          0.7           0.98

		x = rnd.nextDouble();
		switch (object) {
			case 0:
				this.detected = (lightLevel == 0 && x<0.1) || (lightLevel == 1 && x<0.02);
				break;
			case 1:
				this.detected = (lightLevel == 0 && x<0.25) || (lightLevel == 1 && x<0.1) || (lightLevel == 2 && x<0.05);
				break;
			case 2: 
				this.detected = (lightLevel == 0 && x<0.3) || (lightLevel == 1 && x<0.7) || (lightLevel == 2 && x<0.98);
				if (this.detected) {
					this.nLargeObjectsDetected++;
				}
				break;		
		}
		
		// 3. Count detections
		if (this.detected) {
			this.nDetected++;
		}
	}
	
	public boolean getDetected() {
		return this.detected;
	}
	
	public double getProbLargeObjectDetected() {
		return this.nLargeObjectsDetected/this.nLargeObjects;
	}
	
	public double getProbDetected() {
		return this.nDetected/this.nExecutions;
	}
}
