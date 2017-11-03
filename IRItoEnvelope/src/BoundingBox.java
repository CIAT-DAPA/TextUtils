
public class BoundingBox {
	private double topLat;
	private double leftLon;
	private double bottonLat;;
	private double rightLon;

	public BoundingBox(double topLat, double leftLon, double bottonLat, double rightLon) {
		super();
		this.topLat = topLat;
		this.leftLon = leftLon;
		this.bottonLat = bottonLat;
		this.rightLon = rightLon;
	}

	public double getTopLat() {
		return topLat;
	}

	public void setTopLat(double topLat) {
		this.topLat = topLat;
	}

	public double getLeftLon() {
		return leftLon;
	}

	public void setLeftLon(double leftLon) {
		this.leftLon = leftLon;
	}

	public double getBottonLat() {
		return bottonLat;
	}

	public void setBottonLat(double bottonLat) {
		this.bottonLat = bottonLat;
	}

	public double getRightLon() {
		return rightLon;
	}

	public void setRightLon(double rightLon) {
		this.rightLon = rightLon;
	}
	
	
}
