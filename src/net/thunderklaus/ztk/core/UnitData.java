package net.thunderklaus.ztk.core;

import java.util.ArrayList;
import java.util.List;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

public class UnitData {
	public Vec2 randomDir = new Vec2();
	public Vec2 alignmentDir = new Vec2();
	public Vec2 cohesionDir = new Vec2();
	public List<Body> friends = new ArrayList<Body>();
	public int nextFriendUpdate = -1;
	public int nextRandomUpdate = -1;
}
