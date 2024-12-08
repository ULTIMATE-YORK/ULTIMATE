
public class LightingSystem {
	private int lightLevel;
	private double nExecutions;
	private double nLow;
	private double nMed;
	
	public LightingSystem() {
		this.lightLevel = 0;
		this.nExecutions = 0;
		this.nLow = 0;
		this.nMed = 0;
	}
	
	public void adjutstLight(boolean detected) {
		this.nExecutions++;
		
		switch (this.lightLevel) {
		case 0:
		case 1:
			this.lightLevel = detected?2:0;
			break;
		case 2:
			this.lightLevel = detected?2:1;
			break;
		}
		
		if (this.lightLevel == 0) {
			this.nLow++;
		}
		else if (this.lightLevel == 1) {
			this.nMed++;
		}
	}
	
	public int getLightLevel() {
		return this.lightLevel;
	}
	
	public double getProbLow() {
		return this.nLow / this.nExecutions;
	}

	public double getProbMed() {
		return this.nMed / this.nExecutions;
	}
}
