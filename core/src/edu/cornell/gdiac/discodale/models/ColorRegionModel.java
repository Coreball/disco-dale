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
	private PolygonRegion polygonRegion;

	/** Polygon Region used for drawing */
	private PolygonRegion polygonRegionTexture;
	private static boolean useTexture;

	/** Array of color textures */
	private static TextureRegion[] colors = new TextureRegion[5];
	/** Texture */
	private Texture texture;

	public ColorRegionModel(DaleColor color, float[] vertices) {
		this.color = color;
		this.shape = new Polygon(vertices);
		Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		texture = new Texture(pixmap);
		this.polygonRegion = new PolygonRegion(new TextureRegion(texture), vertices,
				TRIANGULATOR.computeTriangles(vertices).toArray());
		polygonRegionTexture = polygonRegion;
		useTexture = false;
	}

	public DaleColor getColor() {
		return color;
	}

	public void setColor(DaleColor c) {
		color = c;
	}

	public static void setColorTexture(Texture[] c){
		for (int i = 0; i < c.length; i++){
			c[i].setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
			colors[i] = new TextureRegion(c[i]);
		}
	}

	public void draw(GameCanvas canvas) {
//		for (PolygonShape tri : shapes) {
//			canvas.drawFilledTri(tri, color.toGdxColor(),0f,0f,getAngle(), drawScale.x,drawScale.y);
//		}
		if (useTexture){
			polygonRegionTexture = new PolygonRegion(colors[color.toColorTexture()],
					polygonRegion.getVertices(), polygonRegion.getTriangles());
			canvas.draw(polygonRegionTexture, 0, 0);
		} else {
			canvas.draw(polygonRegion, color.toGdxColor(), 0, 0);
		}
	}

	public static void switchDisplay(){
		useTexture = !useTexture;
	}


}
