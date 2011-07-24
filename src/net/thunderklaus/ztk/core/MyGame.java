package net.thunderklaus.ztk.core;

import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.World;

import forplay.core.CanvasLayer;
import forplay.core.Color;
import forplay.core.DebugDrawBox2D;
import forplay.core.Game;
import forplay.core.Pointer;

import static forplay.core.ForPlay.*;

public class MyGame implements Game {

	private static final int screenWidth = 1024;
	private static final int screenHeight = 768;
	private static final float worldWidth = 64.0f;
	private static final float worldScale = worldWidth / screenWidth;
	private static final float worldHeight = screenHeight * worldScale;

	private CanvasLayer canvasLayer;

	private World world;
	private Body body;
	private DebugDrawBox2D debugDraw;

	private static void createWalls(World world) {
		Vec2 bottomLeft = new Vec2(0f, 0f);
		Vec2 topLeft = new Vec2(0f, worldHeight);
		Vec2 topRight = new Vec2(worldWidth, worldHeight);
		Vec2 bottomRight = new Vec2(worldWidth, 0f);

		createWall(world, bottomLeft, topLeft);
		createWall(world, topLeft, topRight);
		createWall(world, topRight, bottomRight);
		createWall(world, bottomRight, bottomLeft);
	}

	private static void createWall(World world, Vec2 v1, Vec2 v2) {
		Body body = world.createBody(new BodyDef());
		PolygonShape shape = new PolygonShape();
		shape.setAsEdge(v1, v2);
		body.createFixture(shape, 0f);
	}

	@Override
	public void init() {
		log().info("init");
		graphics().setSize(screenWidth, screenHeight);

		canvasLayer = graphics().createCanvasLayer(screenWidth, screenHeight);
		graphics().rootLayer().add(canvasLayer);

		Vec2 gravity = new Vec2(0.0f, -10.0f);
		world = new World(gravity, true);

		createWalls(world);

		debugDraw = new DebugDrawBox2D();
		debugDraw.setCanvas(canvasLayer);
		debugDraw.setStrokeAlpha(150);
		debugDraw.setFillAlpha(75);
		debugDraw.setStrokeWidth(2.0f);
		debugDraw.setFlags(DebugDraw.e_shapeBit | DebugDraw.e_jointBit
				| DebugDraw.e_aabbBit | DebugDraw.e_centerOfMassBit);
		debugDraw.setCamera(0f, 48.0f, 16.0f);
		world.setDebugDraw(debugDraw);

		body = createUnit(world, new Vec2(21.0f, 30.0f));
		createUnit(world, new Vec2(20.0f, 28.0f));

		pointer().setListener(new Pointer.Listener() {

			@Override
			public void onPointerStart(float x, float y) {
				body.applyLinearImpulse(new Vec2(1.0f, 10000.0f),
						body.getWorldCenter());
			}

			@Override
			public void onPointerEnd(float x, float y) {
				log().info("click: " + x + "; " + y);
			}

			@Override
			public void onPointerDrag(float x, float y) {
			}
		});
	}

	private static Body createUnit(World world, Vec2 pos) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DYNAMIC;
		bodyDef.position = pos;
		Body body = world.createBody(bodyDef);
		CircleShape shape = new CircleShape();
		shape.m_radius = 1.0f;
		body.createFixture(shape, 1.0f);
		return body;
	}

	@Override
	public void update(float delta) {
		// log().info("update(" + delta + ")");
		world.step(1.0f / 20.0f, 10, 10);
	}

	@Override
	public void paint(float alpha) {
		debugDraw.getCanvas().canvas().setFillColor(Color.rgb(255, 255, 255));
		debugDraw.getCanvas().canvas()
				.fillRect(0f, 0f, screenWidth, screenHeight);

		world.drawDebugData();
		// log().info("paint(" + alpha + ")");
	}

	@Override
	public int updateRate() {
		return 1000 / 20;
	}
}