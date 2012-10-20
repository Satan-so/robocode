/**
 * 
 */
package satan;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

/**
 * @author Satan
 *
 */
public final class White extends AdvancedRobot {
	
	static final Rectangle2D.Double MOVEAREA = new Rectangle2D.Double(18, 18, 764, 564);
	static double enemyEnergy = 100;
	static double direction = 1;
	
	static final int GUN_BINS = 230;
	static final int MAX_MATCHES = 50;
	static StringBuilder data = new StringBuilder();
	
	public void run() {
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAllColors(Color.WHITE);

		while (true) {
			turnRadarRightRadians(Double.POSITIVE_INFINITY);
		}
	}
	
	public void onScannedRobot(ScannedRobotEvent e) {
//		{{{{ MOVE
		double headingRadians = getHeadingRadians();
		double absBearing = e.getBearingRadians() + headingRadians;
		
		double distance = e.getDistance();
		boolean rammer = distance < 150;
		double offset = enemyEnergy - (enemyEnergy = e.getEnergy());
		
		if (rammer || (offset > 0 && offset <= 3)) {
			if (Math.random() > 0.8) {
				direction = -direction;
			}
			
			offset = direction * distance / 4 * Math.random() + 32;
			
			if (!MOVEAREA.contains(getX() + offset * Math.sin(headingRadians), getY() + offset * Math.cos(headingRadians))) {
				direction = -direction;
				offset = -offset;
			}
			
			setAhead(offset);
		}
		
		setTurnRight(e.getBearing() + 90 + direction * (rammer ? 15 : -15));
//		MOVE }}}}
		
//		{{{{ TURN GUN
		double bulletPower = rammer ? 2.99 : Math.min(2.49, Math.min(enemyEnergy / 4, getEnergy() / 10));
		setFire(bulletPower);
		
		data.insert(0,
				(char) (((int) Math.round(e.getVelocity() * Math.sin(headingRadians)) << 8) |
						(0XFF & ((byte) (e.getVelocity() * Math.cos(headingRadians))))));
		
		int keyLength = Math.min(66, data.length());
		int indexSize = 0;
		int tempIndex = 0;
		int[] index = new int[MAX_MATCHES];
		int[] bins = new int[GUN_BINS];
		
		do {
			do {
				if (tempIndex < 0) {
					keyLength = keyLength * 3 / 4;
				}

				tempIndex = data.indexOf(data.substring(0, keyLength), tempIndex + 2);

			} while ((tempIndex < 0 || Arrays.binarySearch(index, tempIndex) >= 0)
					&& keyLength > 0);
			
			if (keyLength == 0) {
				break;
			}
			
			int iterateIndex = index[0] = tempIndex;
			Arrays.sort(index);

			double tempDist = distance;
			double tmpBearing = 0;
			double db = 0;
			char comboChar;
			
			do {
				tmpBearing +=
						(byte) ((comboChar = data.charAt(iterateIndex--)) >> 8) / tempDist;
				tempDist += (byte) (comboChar & 0XFF);
				db += Rules.getBulletSpeed(bulletPower);
				
			} while (db < tempDist && iterateIndex > 0);

			bins[(int) (Utils.normalAbsoluteAngle(tmpBearing) * ((GUN_BINS - 1) / (2 * Math.PI)))]
			     += keyLength;
			indexSize++;

		} while (indexSize < MAX_MATCHES);

		keyLength = GUN_BINS - 1;
		tempIndex = 0;
		
		do {
			if (bins[keyLength] > bins[tempIndex]) {
				tempIndex = keyLength;
			}
			
		} while (keyLength-- > 0);
		
		double gunAngle = tempIndex * (2 * Math.PI / (GUN_BINS - 1));
		setTurnGunRightRadians(Utils.normalRelativeAngle(
				absBearing + 0.005 + gunAngle - getGunHeadingRadians()));
//		TURN GUN }}}}
		
		setTurnRadarRightRadians(Utils.normalRelativeAngle(
				absBearing - getRadarHeadingRadians()) * 2);
	}
	
	public void onHitByBullet(HitByBulletEvent e) {
		enemyEnergy += 20 - e.getVelocity();
	}

	public void onBulletHit(BulletHitEvent e) {
		enemyEnergy -= Rules.getBulletDamage(e.getBullet().getPower());
	}

}
