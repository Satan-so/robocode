/**
 * 
 */
package satan;

import robocode.AdvancedRobot;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

/**
 * @author Satan
 * 
 */
public final class R0 extends AdvancedRobot {
	
	static double direction = 1;
	static double enemyEnergy = 100;
	
	final static double ROLL_FACTOR = 8;
	final static int P_OFFS = 8;
	final static int P_BINS = 1 + 2 * P_OFFS;
	final static double BULLET_SPEED = 14.5;
	static double rAvgCurrent;
	static int lvIndexPrevious;
	static double[] rAvgLateralVelocity = new double[P_BINS];
	
	public void run() {
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
//		setAllColors(Color.DARK_GRAY);

		while (true) {
			turnRadarRightRadians(Double.POSITIVE_INFINITY);
		}
	}
	
	public void onScannedRobot(ScannedRobotEvent e) {
		double absBearing = e.getBearingRadians() + getHeadingRadians();
		
		double distance = e.getDistance();
		double offset = enemyEnergy - (enemyEnergy = e.getEnergy());
		
		if (distance < 150 || (offset > 0 && offset <= 3)) {
			setAhead(direction * 128 * Math.random());
		}
		
		setTurnRight(e.getBearing() + 90 - direction * 15);
	    
		rAvgCurrent = (rAvgCurrent * ROLL_FACTOR + e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing)) / (ROLL_FACTOR + 1.0);
	    int lvIndexCurrent = (int) rAvgCurrent + P_OFFS;
	    
	    setTurnGunRightRadians(Utils.normalRelativeAngle(
	    		absBearing - getGunHeadingRadians() + rAvgLateralVelocity[lvIndexCurrent] / BULLET_SPEED));
	   
	    if (setFireBullet(Math.min(1.99, getEnergy() / 10)) != null) {
	    	rAvgLateralVelocity[lvIndexPrevious] = rAvgCurrent;
	    	lvIndexPrevious = lvIndexCurrent;
	    }
	    
	    setTurnRadarLeftRadians(getRadarTurnRemaining());
	}
	
	public void onHitWall(HitWallEvent e) {
		direction = -direction;
	}

}
