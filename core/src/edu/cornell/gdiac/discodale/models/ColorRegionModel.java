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

import javax.swing.plaf.synth.SynthTextAreaUI;
import javax.swing.text.Utilities;

public class ColorRegionModel implements Cloneable{
	/** An earclipping triangular to make sure we work with convex shapes */
	private static final EarClippingTriangulator TRIANGULATOR = new EarClippingTriangulator();

	/** Color of this color region */
	private DaleColor color;

	private DaleColor[] seq;
	private int seqIndex;
	/** Shape of the color region */
	public Polygon shape;
	/** Polygon Region used for drawing */
	private PolygonRegion polygonRegion;

	/** Polygon Region used for drawing */
	private PolygonRegion polygonRegionTexture;
	private static boolean useTexture;

	/** Array of color textures */
	private static TextureRegion[] colors = new TextureRegion[5];
	/** Texture */
	private Texture texture;

	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

	public ColorRegionModel(DaleColor color, float[] vertices, DaleColor[] seq) {
		this.color = color;
		this.shape = new Polygon(vertices);
		this.seq = seq;
		this.seqIndex = 0;
		Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		texture = new Texture(pixmap);
		this.polygonRegion = new PolygonRegion(new TextureRegion(texture), vertices,
				TRIANGULATOR.computeTriangles(vertices).toArray());
		polygonRegionTexture = polygonRegion;
		useTexture = false;
	}

	public float[] getVertices(){
		return polygonRegion.getVertices();
	}

	public DaleColor[] getSeq(){
		return seq;
	}

//	public void setSeq(DaleColor[] seq) {
//		this.seq = seq;
////		System.out.println("set");
////		for(int i =0;i<seq.length;i++){
////			System.out.println(seq[0].toColorTexture());
////		}
//
//	}

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
	public int testBound(float bound, int r){
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

	public void switchColor(){
		if(seq != null){
			this.seqIndex = (this.seqIndex+1)%seq.length;
			setColor(seq[this.seqIndex]);
		}
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
			polygonRegionTexture = new PolygonRegion(colors[color.ordinal()],
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
