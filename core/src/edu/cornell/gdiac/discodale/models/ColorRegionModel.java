package edu.cornell.gdiac.discodale.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Polygon;
import edu.cornell.gdiac.discodale.GameCanvas;
import edu.cornell.gdiac.discodale.models.DaleColor;

public class ColorRegionModel {
	/** An earclipping triangular to make sure we work with convex shapes */
	private static final EarClippingTriangulator TRIANGULATOR = new EarClippingTriangulator();

	/** Color of this color region */
	private DaleColor color;
	/** Shape of the color region */
	public final Polygon shape;
	/** Polygon Region used for drawing */
	private final PolygonRegion polygonRegion;

	public ColorRegionModel(DaleColor color, float[] vertices) {
		this.color = color;
		this.shape = new Polygon(vertices);
		Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		Texture texture = new Texture(pixmap);
		this.polygonRegion = new PolygonRegion(new TextureRegion(texture), vertices, TRIANGULATOR.computeTriangles(vertices).toArray());
	}

	public DaleColor getColor() {
		return color;
	}

	public void setColor(DaleColor c) {
		color = c;
	}

	public void draw(GameCanvas canvas) {
//		for (PolygonShape tri : shapes) {
//			canvas.drawFilledTri(tri, color.toGdxColor(),0f,0f,getAngle(), drawScale.x,drawScale.y);
//		}
		canvas.draw(polygonRegion, color.toGdxColor(), 0, 0);
	}
}
