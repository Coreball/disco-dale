package edu.cornell.gdiac.physics.platform;

import com.badlogic.gdx.physics.box2d.PolygonShape;
import edu.cornell.gdiac.physics.GameCanvas;
import edu.cornell.gdiac.physics.obstacle.PolygonObstacle;

public class ColorRegionModel extends PolygonObstacle {
	private DaleColor color;

	public ColorRegionModel(DaleColor color, float[] vertices) {
		super(vertices);
		this.color = color;
	}

	public DaleColor getColor() {
		return color;
	}

	public void setColor(DaleColor c) {
		color = c;
	}

	public void draw(GameCanvas canvas) {
		for (PolygonShape tri : shapes) {
			canvas.drawFilledTri(tri, color.toGdxColor(),0f,0f,getAngle(), drawScale.x,drawScale.y);
		}
	}
}
