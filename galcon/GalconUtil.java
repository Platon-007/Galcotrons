package galcon;

/**
 * Created by IntelliJ IDEA.
 * User: Денис
 * Date: 19.04.2013
 * Time: 19:07:04
 * To change this template use File | Settings | File Templates.
 */
public class GalconUtil {
	public static boolean pointInPlanet(Planet p, int x, int y) {
		return Math.pow(x - p.x, 2) + Math.pow(y - p.y, 2) < Math.pow(PartyEngine.getPlanetRadius(p.power), 2);
	}

	public static boolean isPointInCircle(int cx1, int cy1, int r, int x, int y) {
		return Math.pow(x - cx1, 2) + Math.pow(y - cy1, 2) < Math.pow(r, 2);
	}

	public static boolean isPlanetIntersectsRect(Planet p, double x, double y, double w, double h) {
		if (w <= 0.0 || h <= 0.0) {
			return false;
		}
		// Normalize the rectangular coordinates compared to the ellipse
		// having a center at 0,0 and a radius of 0.5.
		double radius = PartyEngine.getPlanetRadius(p.power);
		double ellw = radius*2;
		if (ellw <= 0.0) {
			return false;
		}

		double normx0 = (x - (p.x - radius)) / ellw - 0.5;
		double normx1 = normx0 + w / ellw;
		double ellh = radius*2;
		if (ellh <= 0.0) {
			return false;
		}
		double normy0 = (y - (p.y - radius)) / ellh - 0.5;
		double normy1 = normy0 + h / ellh;
		// find nearest x (left edge, right edge, 0.0)
		// find nearest y (top edge, bottom edge, 0.0)
		// if nearest x,y is inside circle of radius 0.5, then intersects
		double nearx, neary;
		if (normx0 > 0.0) {
			// center to left of X extents
			nearx = normx0;
		} else if (normx1 < 0.0) {
			// center to right of X extents
			nearx = normx1;
		} else {
			nearx = 0.0;
		}
		if (normy0 > 0.0) {
			// center above Y extents
			neary = normy0;
		} else if (normy1 < 0.0) {
			// center below Y extents
			neary = normy1;
		} else {
			neary = 0.0;
		}
		return (nearx * nearx + neary * neary) < 0.25;
	}
}
