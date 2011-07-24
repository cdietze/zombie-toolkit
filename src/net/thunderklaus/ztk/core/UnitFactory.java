package net.thunderklaus.ztk.core;

import java.util.Random;

import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;

import forplay.core.DebugDrawBox2D;
import forplay.core.ForPlay;

public class UnitFactory {

	private static final float unitRadius = 0.4f;
	private static final Random random = new Random();

	public static Body createUnit(World world, Vec2 pos) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DYNAMIC;
		bodyDef.position = pos;
		Body body = world.createBody(bodyDef);
		ForPlay.log().debug("created body: " + body);
		CircleShape shape = new CircleShape();
		shape.m_radius = unitRadius;
		body.createFixture(shape, 1.0f);
		body.applyLinearImpulse(randomVec2().mulLocal(4.0f),
				body.getWorldCenter());
		return body;
	}

	public static void createUnitCrowd(World world, int amount, Vec2 center) {
		float radius = (float) Math.sqrt(amount);
		for (int i = 0; i < amount; ++i) {
			Vec2 pos = randomVec2().mulLocal(radius).addLocal(center);
			createUnit(world, pos);
		}
	}

	private static Vec2 randomVec2() {
		return new Vec2(random.nextFloat() * 2.0f - 1.0f,
				random.nextFloat() * 2.0f - 1.0f);
	}

	public static void applySwarmAi(final Body body, DebugDrawBox2D debugDraw) {
		World world = body.getWorld();
		final Vec2 pos = body.getPosition();
		float boxSize = 4f;
		final AABB aabb = new AABB(pos.add(new Vec2(-boxSize, -boxSize)),
				pos.add(new Vec2(boxSize, boxSize)));
		debugDraw.drawSegment(aabb.lowerBound, aabb.upperBound, Color3f.BLUE);
		world.queryAABB(new QueryCallback() {

			@Override
			public boolean reportFixture(Fixture fixture) {
				if (fixture.getBody() == body) {
					return true;
				}
				if (fixture.getBody().getType() != BodyType.DYNAMIC) {
					return true;
				}
				if (!AABB.testOverlap(aabb, fixture.getAABB())) {
					return true;
				}
//				ForPlay.log().debug(
//						"applying attraction: " + body + " -> "
//								+ fixture.getBody());
				Vec2 dir = fixture.getBody().getPosition().sub(pos);
				float power = 1.0f / dir.lengthSquared();
				dir.normalize();
				dir.mulLocal(power);
				body.applyLinearImpulse(dir, body.getWorldCenter());
				return true;
			}
		}, aabb);
	}
}
