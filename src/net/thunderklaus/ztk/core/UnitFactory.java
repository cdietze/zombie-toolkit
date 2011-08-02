package net.thunderklaus.ztk.core;

import java.util.ArrayList;
import java.util.List;
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
import static net.thunderklaus.ztk.core.Constants.UPDATE_RATE_IN_MS;

public class UnitFactory {

	public static final float FRIEND_RADIUS = 10f;
	public static final int FRIEND_UPDATE_INTERVAL_IN_MS = 1000;
	public static final float COHESION_POWER = 0.05f;
	public static final float ALIGNMENT_POWER = 0.05f;
	public static final float RANDOM_POWER = 0.05f;
	public static final int RANDOM_UPDATE_INTERVAL_IN_MS = 1000;

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
		updateFriends(body);
		updateRandom(body);
		applyRandom(body);
		applyCohesion(body);
		applyAlignment(body);
	}

	private static void updateRandom(final Body body) {
		UnitData data = (UnitData) body.getUserData();
		data.nextRandomUpdate -= UPDATE_RATE_IN_MS;
		if (data.nextRandomUpdate > 0) {
			return;
		}
		data.nextRandomUpdate = random.nextInt(RANDOM_UPDATE_INTERVAL_IN_MS)
				+ RANDOM_UPDATE_INTERVAL_IN_MS / 2;
		data.randomDir = randomVec2Normalized().mulLocal(RANDOM_POWER);
	}

	private static void applyRandom(final Body body) {
		UnitData data = (UnitData) body.getUserData();
		body.applyLinearImpulse(data.randomDir, body.getWorldCenter());
	}

	private static void updateFriends(final Body body) {
		final UnitData data = (UnitData) body.getUserData();
		data.nextFriendUpdate -= UPDATE_RATE_IN_MS;
		if (data.nextFriendUpdate > 0) {
			return;
		}
		data.nextFriendUpdate = random.nextInt(FRIEND_UPDATE_INTERVAL_IN_MS)
				+ FRIEND_UPDATE_INTERVAL_IN_MS / 2;
		if (!data.friends.isEmpty()) {
			if (random.nextInt(100) <= 50)
				data.friends.remove(0);
		}
		if (random.nextInt(100) <= 50)
			return;
		final List<Body> candidates = new ArrayList<Body>();
		query(body, FRIEND_RADIUS, new MyQueryCallback() {
			@Override
			public void onMatch(Body otherBody) {
				if (!data.friends.contains(otherBody))
					candidates.add(otherBody);
			}
		});
		if (!candidates.isEmpty()) {
			Body newFriend = candidates.get(random.nextInt(candidates.size()));
			data.friends.add(newFriend);
		}
	}

	private static void applyAlignment(final Body body) {
		final UnitData data = (UnitData) body.getUserData();
		if (data.friends.isEmpty())
			return;
		Vec2 tmp = new Vec2();
		for (Body friend : data.friends) {
			Vec2 norm = friend.getLinearVelocity().clone();
			norm.normalize();
			tmp.addLocal(norm);
		}
		tmp.mulLocal(1f / data.friends.size());
		tmp.mulLocal(ALIGNMENT_POWER);
		data.alignmentDir = tmp;
		body.applyLinearImpulse(tmp, body.getWorldCenter());
	}

	private static void applyCohesion(final Body body) {
		final UnitData data = (UnitData) body.getUserData();
		if (data.friends.isEmpty())
			return;
		Vec2 tmp = new Vec2();
		for (Body friend : data.friends) {
			Vec2 dir = friend.getPosition().sub(body.getPosition());
			dir.normalize();
			tmp.addLocal(dir);
		}
		tmp.mulLocal(1f / data.friends.size());
		tmp.mulLocal(COHESION_POWER);
		data.cohesionDir = tmp;
		body.applyLinearImpulse(tmp, body.getWorldCenter());
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
