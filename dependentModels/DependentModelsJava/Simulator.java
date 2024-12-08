import java.util.Random;

public class Simulator {

	public static void main(String[] args) {
		LightingSystem light = new LightingSystem();
		Sensor sensor = new Sensor();

		Random rnd = new Random();
		double sensorPeriod = 3; // every 3s 
		double tSensor = rnd.nextDouble(sensorPeriod);
		double lightPeriod = 50; // every 50s
		double tLight = rnd.nextDouble(lightPeriod);
		
		double t = 0;
		
		while (t <10000000) {
			if (tSensor < tLight) {
				t = tSensor;
				tSensor += sensorPeriod;
				sensor.detectObject(light.getLightLevel());
			}
			else {
				t = tLight;
				tLight += lightPeriod;
				light.adjutstLight(sensor.getDetected());
			}
		}
		
		System.out.println("d=" + sensor.getProbDetected());
		System.out.println("l=" + light.getProbLow());
		System.out.println("m=" + light.getProbMed());
		System.out.println("R=" + sensor.getProbLargeObjectDetected());   
	}
}
