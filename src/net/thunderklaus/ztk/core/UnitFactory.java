package net.thunderklaus.ztk.core;

import java.util.Random;

import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;

import forplay.core.DebugDrawBox2D;

public class UnitFactory {

	public static final float COHESION_RADIUS = 15f;
	public static final float COHESION_POWER = 0.01f;
	public static final float ALIGNMENT_RADIUS = 10f;
	public static final float ALIGNMENT_POWER = 0.05f;
	public static final float RANDOM_POWER = 0.1f;
	public static final float RANDOM_JITTER = 0.05f;

	private static final float unitRadius = 0.4f;
	private static final Random random = new Random();

	public static Body createUnit(World world, Vec2 pos) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DYNAMIC;
		bodyDef.position = pos;
		bodyDef.linearDamping = 0.5f;
		Body body = world.createBody(bodyDef);
		body.setUserData(new UnitData());
		CircleShape shape = new CircleShape();
		shape.m_radius = unitRadius;
		body.createFixture(shape, 1.0f);
		initRandom(body);
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

	public static void applySwarmAi(final Body body, DebugDrawBox2D debugDraw) {
		applyRandom(body);
		applyCohesion(body);
		applyAlignment(body);
	}

	private static void initRandom(final Body body) {
		UnitData data = (UnitData) body.getUserData();
		data.currentRandomDir = randomVec2().mulLocal(RANDOM_POWER);
	}

	private static void applyRandom(final Body body) {
		UnitData data = (UnitData) body.getUserData();
		Vec2 temp = randomVec2Normalized().mulLocal(
				RANDOM_POWER * RANDOM_JITTER);
		data.currentRandomDir.mulLocal(1.0f - RANDOM_JITTER).addLocal(temp);
		Vec2 vec = data.currentRandomDir;
		float length = vec.length();
		if (length > RANDOM_POWER) {
			vec.mulLocal(RANDOM_POWER / length);
		}
		body.applyLinearImpulse(vec, body.getWorldCenter());
	}
	
	private static void applyAlignment(final Body body) {
		final Vec2 alignmentVec = new Vec2();
		query(body, ALIGNMENT_RADIUS, new MyQueryCallback() {
			@Override
			public void onMatch(Body otherBody) {
				Vec2 dir = otherBody.getPosition().sub(body.getPosition());
				float power = 0.5f * ALIGNMENT_POWER
						* (ALIGNMENT_RADIUS - dir.length()) / ALIGNMENT_RADIUS;
				// ForPlay.log().debug("align power: " + power);
				Vec2 vec = otherBody.getLinearVelocity().mul(power);
				alignmentVec.addLocal(vec);
				// dir.normalize();
				// dir.mulLocal(power);
			}
		});
		float length = alignmentVec.length();
		if (length > ALIGNMENT_POWER) {
			alignmentVec.mulLocal(ALIGNMENT_POWER / length);
		}
		// ForPlay.log().debug("align vec: " + alignmentVec);
		body.applyLinearImpulse(alignmentVec, body.getWorldCenter());
	}

	private static void applyCohesion(final Body body) {
		final Vec2 vec = new Vec2();
		query(body, COHESION_RADIUS, new MyQueryCallback() {
			@Override
			public void onMatch(Body otherBody) {
				Vec2 dir = otherBody.getPosition().sub(body.getPosition());
				float power = 0.5f * COHESION_POWER
						* (COHESION_RADIUS - dir.length()) / COHESION_RADIUS;
				dir.normalize();
				dir.mulLocal(power);
				vec.addLocal(dir);
			}
		});
		float length = vec.length();
		if (length > COHESION_POWER) {
			vec.mulLocal(COHESION_POWER / length);
		}
		body.applyLinearImpulse(vec, body.getWorldCenter());
	}

	private static void query(final Body body, final float radius,
			final MyQueryCallback callback) {
		World world = body.getWorld();
		float boxSize = radius;
		final Vec2 pos = body.getPosition();
		final AABB aabb = new AABB(pos.add(new Vec2(-boxSize, -boxSize)),
				pos.add(new Vec2(boxSize, boxSize)));
		world.queryAABB(new QueryCallback() {

			@Override
			public boolean reportFixture(Fixture fixture) {
				Body otherBody = fixture.getBody();
				if (otherBody == body) {
					return true;
				}
				if (otherBody.getType() != BodyType.DYNAMIC) {
					return true;
				}
				if (!AABB.testOverlap(aabb, fixture.getAABB())) {
					return true;
				}
				// continue, if the other body is in the square but not in the
				// circle
				if (radius * radius < otherBody.getPosition()
						.sub(body.getPosition()).lengthSquared()) {
					return true;
				}
				callback.onMatch(fixture.getBody());
				return true;
			}
		}, aabb);
	}

	private interface MyQueryCallback {
		void onMatch(Body otherBody);
	}

	private static Vec2 randomVec2() {
		return new Vec2(random.nextFloat() * 2.0f - 1.0f,
				random.nextFloat() * 2.0f - 1.0f);
	}

	private static Vec2 randomVec2Normalized() {
		Vec2 vec = randomVec2();
		vec.normalize();
		return vec;
	}
}
