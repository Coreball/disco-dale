package edu.cornell.gdiac.discodale.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import edu.cornell.gdiac.discodale.GameCanvas;
import edu.cornell.gdiac.discodale.models.DaleColor;

import javax.swing.text.Utilities;

public class ColorRegionModel {
	/** An earclipping triangular to make sure we work with convex shapes */
	private static final EarClippingTriangulator TRIANGULATOR = new EarClippingTriangulator();

	/** Color of this color region */
	private DaleColor color;
	/** Shape of the color region */
	public Polygon shape;
	/** Polygon Region used for drawing */
	private PolygonRegion polygonRegion;
	/** Texture */
	private final Texture texture;

	public ColorRegionModel(DaleColor color, float[] vertices) {
		this.color = color;
		this.shape = new Polygon(vertices);
		Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		this.texture = new Texture(pixmap);
		this.polygonRegion = new PolygonRegion(new TextureRegion(this.texture), vertices, TRIANGULATOR.computeTriangles(vertices).toArray());
	}

	public float[] getVertices(){
		return polygonRegion.getVertices();
	}

	public void move(float dx, float dy){
		float[] vertices = getVertices().clone();
		for(int i =0;i<vertices.length;i++){
			if(i%2==0){
				vertices[i]+=dx;
			}else{
				vertices[i]+=dy;
			}
		}
		this.shape.setVertices(vertices);
		this.polygonRegion = new PolygonRegion(new TextureRegion(texture), vertices, TRIANGULATOR.computeTriangles(vertices).toArray());
	}

	public void rotateAround(float x2, float y2, float d){
		float[] vertices = getVertices().clone();
		for(int i =0;i<vertices.length;i=i+2){
			float x1 = vertices[i];
			float y1 = vertices[i+1];
			float x = (x1-x2)* MathUtils.cosDeg(d) - (y1-y2)*MathUtils.sinDeg(d)+x2;
			float y = (x1-x2)* MathUtils.sinDeg(d) + (y1-y2)*MathUtils.cosDeg(d)+y2;
			vertices[i]=x;
			vertices[i+1]=y;
		}
		this.shape.setVertices(vertices);
		this.polygonRegion = new PolygonRegion(new TextureRegion(texture), vertices, TRIANGULATOR.computeTriangles(vertices).toArray());
	}

	/**
	 * Test if all the vertices are out of bounds
	 *
	 * @param bound The bound to test
	 * @param r Either x or y coordinates to test. 0 means x coordinate, 1 means y coordinate
	 *
	 * @return 0 means not or only partially out of bounds, 1 means to the left or bottom, -1 means to the right or top
	 */
	public int testBound(int bound, int r){
		boolean toLeftOrBottom = true;
		boolean toRightOrTop = true;
		float[] vertices = getVertices().clone();
		for(int i=0;i<vertices.length;i++){
			if(i%2==r){
				if(vertices[i]>0){
					toLeftOrBottom = false;
				}
				if(vertices[i]<=bound){
					toRightOrTop = false;
				}
			}
		}
		if(toLeftOrBottom){
			return 1;
		}
		if(toRightOrTop){
			return -1;
		}
		return 0;
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
//		for(int i=-1;i<=1;i++){
//			for(int j=-1;j<=1;j++){
//				canvas.draw(polygonRegion, color.toGdxColor(), i*windowWidth, j*windowHeight);
//			}
//		}
		canvas.draw(polygonRegion, color.toGdxColor(), 0, 0);

	}
}
