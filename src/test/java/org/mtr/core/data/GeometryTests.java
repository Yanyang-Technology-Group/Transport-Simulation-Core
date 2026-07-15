package org.mtr.core.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mtr.core.tool.Angle;
import org.mtr.core.tool.Utilities;
import org.mtr.core.tool.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for core geometry and utility classes
 */
public final class GeometryTests {

	private static final double TOLERANCE = 1E-9;
	private static final Rail.Shape[] SHAPES = {Rail.Shape.QUADRATIC, Rail.Shape.TWO_RADII, Rail.Shape.CABLE};
	private static final int[] TILT_CONFIGS = {2, 3, 4, 5, 6, 7};

	private record RenderSample(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, double tilt) {}

	private static RailMath createRailMath(long x1, long z1, Angle a1, long x2, long z2, Angle a2, long y1, long y2, Rail.Shape shape, int tiltPoints) {
		return new RailMath(
			new Position(x1, y1, z1), a1,
			new Position(x2, y2, z2), a2,
			shape, 10, tiltPoints,
			0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
		);
	}

	private static List<RenderSample> collectVector(RailMath railMath, double interval, float r1, float r2) {
		final List<RenderSample> samples = new ArrayList<>();
		railMath.render((x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, tilt) ->
			samples.add(new RenderSample(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, tilt)),
			interval, r1, r2
		);
		return samples;
	}

	private static List<RenderSample> collectScalar(RailMath railMath, double interval, float r1, float r2) {
		final List<RenderSample> samples = new ArrayList<>();
		railMath.renderScalar((x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, tilt) ->
			samples.add(new RenderSample(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, tilt)),
			interval, r1, r2
		);
		return samples;
	}

	private static void assertSampleEquals(RenderSample expected, RenderSample actual, double tol) {
		assertEquals(expected.x1, actual.x1, tol, "x1 mismatch");
		assertEquals(expected.y1, actual.y1, tol, "y1 mismatch");
		assertEquals(expected.z1, actual.z1, tol, "z1 mismatch");
		assertEquals(expected.x2, actual.x2, tol, "x2 mismatch");
		assertEquals(expected.y2, actual.y2, tol, "y2 mismatch");
		assertEquals(expected.z2, actual.z2, tol, "z2 mismatch");
		assertEquals(expected.x3, actual.x3, tol, "x3 mismatch");
		assertEquals(expected.y3, actual.y3, tol, "y3 mismatch");
		assertEquals(expected.z3, actual.z3, tol, "z3 mismatch");
		assertEquals(expected.x4, actual.x4, tol, "x4 mismatch");
		assertEquals(expected.y4, actual.y4, tol, "y4 mismatch");
		assertEquals(expected.z4, actual.z4, tol, "z4 mismatch");
		assertEquals(expected.tilt, actual.tilt, tol, "tilt mismatch");
	}

	/**
	 * Provides parameterized test cases covering various rail configurations.
	 * Each entry is: {x1, z1, a1, x2, z2, a2, y1, y2, shape, tiltPoints, interval, r1, r2, name}
	 */
	static Stream<org.junit.jupiter.params.provider.Arguments> railConfigProvider() {
		final List<org.junit.jupiter.params.provider.Arguments> cases = new ArrayList<>();

		// Straight horizontal
		cases.add(org.junit.jupiter.params.provider.Arguments.of(0L, 0L, Angle.E, 10L, 0L, Angle.E, 60L, 60L, Rail.Shape.QUADRATIC, 2, 0.5, 0f, 0f));
		// Straight with slope
		cases.add(org.junit.jupiter.params.provider.Arguments.of(0L, 0L, Angle.E, 10L, 0L, Angle.E, 60L, 65L, Rail.Shape.QUADRATIC, 3, 0.5, 0f, 0f));
		// 90-degree curve
		cases.add(org.junit.jupiter.params.provider.Arguments.of(0L, 0L, Angle.E, 10L, 10L, Angle.S, 60L, 60L, Rail.Shape.QUADRATIC, 2, 0.5, 0f, 0f));
		// Curve with slope
		cases.add(org.junit.jupiter.params.provider.Arguments.of(0L, 0L, Angle.E, 10L, 10L, Angle.S, 60L, 65L, Rail.Shape.TWO_RADII, 4, 0.5, 0f, 0f));
		// Double curve (S-bend)
		cases.add(org.junit.jupiter.params.provider.Arguments.of(0L, 0L, Angle.E, 20L, 5L, Angle.W, 60L, 60L, Rail.Shape.QUADRATIC, 5, 0.5, 0f, 0f));
		// Straight with wide offset
		cases.add(org.junit.jupiter.params.provider.Arguments.of(0L, 0L, Angle.E, 10L, 0L, Angle.E, 60L, 60L, Rail.Shape.QUADRATIC, 2, 0.5, 0.25f, 0.25f));
		// Curve with offset and 7 tilt points
		cases.add(org.junit.jupiter.params.provider.Arguments.of(0L, 0L, Angle.E, 10L, 10L, Angle.S, 60L, 62L, Rail.Shape.QUADRATIC, 7, 0.5, 0.125f, 0.25f));
		// Short rail (under 0.5 block)
		cases.add(org.junit.jupiter.params.provider.Arguments.of(0L, 0L, Angle.E, 3L, 0L, Angle.E, 60L, 60L, Rail.Shape.QUADRATIC, 2, 0.5, 0f, 0f));
		// Cable shape
		cases.add(org.junit.jupiter.params.provider.Arguments.of(0L, 0L, Angle.E, 10L, 0L, Angle.E, 60L, 65L, Rail.Shape.CABLE, 2, 0.5, 0f, 0f));
		// Reverse direction curve
		cases.add(org.junit.jupiter.params.provider.Arguments.of(10L, 10L, Angle.W, 0L, 0L, Angle.N, 60L, 60L, Rail.Shape.QUADRATIC, 6, 0.5, 0f, 0f));
		// Only offset1 nonzero
		cases.add(org.junit.jupiter.params.provider.Arguments.of(0L, 0L, Angle.E, 10L, 0L, Angle.E, 60L, 60L, Rail.Shape.QUADRATIC, 2, 0.5, 0.25f, 0f));
		// Only offset2 nonzero
		cases.add(org.junit.jupiter.params.provider.Arguments.of(0L, 0L, Angle.E, 10L, 0L, Angle.E, 60L, 60L, Rail.Shape.QUADRATIC, 2, 0.5, 0f, 0.25f));

		return cases.stream();
	}

	@ParameterizedTest(name = "[{index}] {0},{1},{2} -> {3},{4},{5} shape={8} tilt={9} interval={10} r1={11} r2={12}")
	@MethodSource("railConfigProvider")
	public void testRenderScalarMatchesVector(long x1, long z1, Angle a1, long x2, long z2, Angle a2, long y1, long y2, Rail.Shape shape, int tiltPoints, double interval, float r1, float r2) {
		final RailMath railMath = createRailMath(x1, z1, a1, x2, z2, a2, y1, y2, shape, tiltPoints);
		final List<RenderSample> vectorSamples = collectVector(railMath, interval, r1, r2);
		final List<RenderSample> scalarSamples = collectScalar(railMath, interval, r1, r2);

		assertEquals(vectorSamples.size(), scalarSamples.size(), "Sample count mismatch");

		for (int i = 0; i < vectorSamples.size(); i++) {
			assertSampleEquals(vectorSamples.get(i), scalarSamples.get(i), TOLERANCE);
		}
	}

	@Test
	public void testGetTiltAngleScalar() {
		final RailMath railMath = createRailMath(0, 0, Angle.E, 10, 0, Angle.E, 60, 65, Rail.Shape.QUADRATIC, 7);
		final double length = railMath.getLength();

		// Sample tilt at several points along the rail
		for (double pos = 0; pos <= length; pos += 0.5) {
			final double expected = railMath.getTiltAngle(pos, false);
			final double actual = railMath.getTiltAngleScalar(pos);
			assertEquals(expected, actual, TOLERANCE, "Tilt mismatch at position " + pos);
		}
	}

	@Test
	public void testPositionConstruction() {
		final Position position = new Position(10, 20, 30);
		assertEquals(10, position.getX(), "X coordinate should be set correctly");
		assertEquals(20, position.getY(), "Y coordinate should be set correctly");
		assertEquals(30, position.getZ(), "Z coordinate should be set correctly");
	}

	@Test
	public void testPositionEquality() {
		final Position position1 = new Position(10, 20, 30);
		final Position position2 = new Position(10, 20, 30);
		final Position position3 = new Position(10, 20, 31);

		assertEquals(position1, position2, "Positions with same coordinates should be equal");
		assertNotEquals(position1, position3, "Positions with different coordinates should not be equal");
	}

	@Test
	public void testPositionHashConsistency() {
		final Position position1 = new Position(10, 20, 30);
		final Position position2 = new Position(10, 20, 30);

		assertEquals(position1.hashCode(), position2.hashCode(),
			"Equal positions should have equal hash codes");
	}

	@Test
	public void testPositionOrdering() {
		final Position position1 = new Position(5, 20, 30);
		final Position position2 = new Position(10, 20, 30);
		final Position position3 = new Position(10, 15, 30);
		final Position position4 = new Position(10, 20, 25);

		assertTrue(position1.compareTo(position2) < 0, "Position with smaller X should be less");
		assertTrue(position2.compareTo(position1) > 0, "Position with larger X should be greater");
		assertTrue(position3.compareTo(position2) < 0, "Position with same X but smaller Y should be less");
		assertTrue(position4.compareTo(position2) < 0, "Position with same X,Y but smaller Z should be less");
		assertEquals(0, position1.compareTo(new Position(5, 20, 30)), "Equal positions should compare as 0");
	}

	@Test
	public void testPositionOffset() {
		final Position position = new Position(10, 20, 30);
		final Position offset1 = position.offset(5, 10, 15);

		assertEquals(15, offset1.getX(), "X should be incremented");
		assertEquals(30, offset1.getY(), "Y should be incremented");
		assertEquals(45, offset1.getZ(), "Z should be incremented");

		// Test zero offset optimization
		final Position offset2 = position.offset(0, 0, 0);
		assertSame(position, offset2, "Zero offset should return same object (optimization)");
	}

	@Test
	public void testPositionOffsetWithPosition() {
		final Position position1 = new Position(10, 20, 30);
		final Position position2 = new Position(5, 10, 15);
		final Position result = position1.offset(position2);

		assertEquals(15, result.getX(), "X should be sum of both positions");
		assertEquals(30, result.getY(), "Y should be sum of both positions");
		assertEquals(45, result.getZ(), "Z should be sum of both positions");
	}

	@Test
	public void testManhattanDistance() {
		final Position position1 = new Position(0, 0, 0);
		final Position position2 = new Position(3, 4, 5);

		long distance = position1.manhattanDistance(position2);
		assertEquals(12, distance, "Manhattan distance should be |x| + |y| + |z|");
	}

	@Test
	public void testManhattanDistanceNegative() {
		final Position position1 = new Position(10, 20, 30);
		final Position position2 = new Position(5, 15, 25);

		long distance = position1.manhattanDistance(position2);
		assertEquals(15, distance, "Manhattan distance should handle negative offsets");
	}

	@Test
	public void testPositionGetMin() {
		final Position position1 = new Position(10, 20, 30);
		final Position position2 = new Position(5, 25, 15);
		final Position min = Position.getMin(position1, position2);

		assertEquals(5, min.getX(), "Min X should be 5");
		assertEquals(20, min.getY(), "Min Y should be 20");
		assertEquals(15, min.getZ(), "Min Z should be 15");
	}

	@Test
	public void testPositionGetMinWithNull() {
		final Position position = new Position(10, 20, 30);

		assertSame(position, Position.getMin(position, null), "Min with null should return non-null");
		assertSame(position, Position.getMin(null, position), "Min with null should return non-null");
		assertNull(Position.getMin(null, null), "Min of two nulls should be null");
	}

	@Test
	public void testPositionGetMax() {
		final Position position1 = new Position(10, 20, 30);
		final Position position2 = new Position(5, 25, 15);
		final Position max = Position.getMax(position1, position2);

		assertEquals(10, max.getX(), "Max X should be 10");
		assertEquals(25, max.getY(), "Max Y should be 25");
		assertEquals(30, max.getZ(), "Max Z should be 30");
	}

	@Test
	public void testPositionGetMaxWithNull() {
		final Position position = new Position(10, 20, 30);

		assertSame(position, Position.getMax(position, null), "Max with null should return non-null");
		assertSame(position, Position.getMax(null, position), "Max with null should return non-null");
		assertNull(Position.getMax(null, null), "Max of two nulls should be null");
	}

	@Test
	public void testPositionFromVector() {
		final Vector vector = new Vector(10.7, 20.3, 30.9);
		final Position position = new Position(vector);

		assertEquals(10, position.getX(), "X coordinate should be floored");
		assertEquals(20, position.getY(), "Y coordinate should be floored");
		assertEquals(30, position.getZ(), "Z coordinate should be floored");
	}

	@Test
	public void testPositionFromVectorNegative() {
		final Vector vector = new Vector(-10.7, -20.3, -30.9);
		final Position position = new Position(vector);

		assertEquals(-11, position.getX(), "Negative X coordinate should be floored correctly");
		assertEquals(-21, position.getY(), "Negative Y coordinate should be floored correctly");
		assertEquals(-31, position.getZ(), "Negative Z coordinate should be floored correctly");
	}

	@Test
	public void testPositionInArea() {
		final Position position = new Position(5, 0, 5);
		final Position area1 = new Position(0, 0, 0);
		final Position area2 = new Position(10, 10, 10);
		assertTrue(Utilities.isBetween(position, area1, area2, 0));
	}

	@Test
	public void testPositionOutOfArea() {
		final Position position = new Position(15, 0, 5);
		final Position area1 = new Position(0, 0, 0);
		final Position area2 = new Position(10, 10, 10);
		assertFalse(Utilities.isBetween(position, area1, area2, 0));
	}

	@Test
	public void testVectorConstruction() {
		final Vector vector = new Vector(1.5, 2.5, 3.5);
		assertEquals(1.5, vector.x(), 1e-10);
		assertEquals(2.5, vector.y(), 1e-10);
		assertEquals(3.5, vector.z(), 1e-10);
	}
}
