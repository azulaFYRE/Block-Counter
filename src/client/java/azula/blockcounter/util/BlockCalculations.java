package azula.blockcounter.util;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlockCalculations {

    public static int calculateBlocksOne(Vec3d firstPos, Vec3d secondPos, boolean isClick) {
        Direction.Axis largestDiff = findLargestAxisDiff(firstPos, secondPos);

        Vec3d newFirst, newSecond;

        if (largestDiff.equals(Direction.Axis.X)) {
            Vec3d x = new Vec3d(1, 0, 0);
            newFirst = firstPos.projectOnto(x);
            newSecond = secondPos.projectOnto(x);
        } else if (largestDiff.equals(Direction.Axis.Y)) {
            Vec3d y = new Vec3d(0, 1, 0);
            newFirst = firstPos.projectOnto(y);
            newSecond = secondPos.projectOnto(y);
        } else {
            Vec3d z = new Vec3d(0, 0, 1);
            newFirst = firstPos.projectOnto(z);
            newSecond = secondPos.projectOnto(z);
        }

        return (int) Math.ceil(newFirst.distanceTo(newSecond))
                + (!isClick && largestDiff.equals(Direction.Axis.Y) ? 0 : 1);
    }

    public static int calculateBlocksTwo(Vec3d firstPos, Vec3d secondPos, boolean isClick) {
        List<Direction.Axis> diffs = findTwoLargestAxisDiff(firstPos, secondPos);
        Direction.Axis first = diffs.getFirst();
        Direction.Axis second = diffs.get(1);

        Vec3d firstStart, firstEnd, secondStart, secondEnd;

        Vec3d x = new Vec3d(1, 0, 0);
        Vec3d y = new Vec3d(0, 1, 0);
        Vec3d z = new Vec3d(0, 0, 1);

        if (first.equals(Direction.Axis.X)) {
            firstStart = firstPos.projectOnto(x);
            firstEnd = secondPos.projectOnto(x);
        } else if (first.equals(Direction.Axis.Y)) {
            firstStart = firstPos.projectOnto(y);
            firstEnd = secondPos.projectOnto(y);
        } else {
            firstStart = firstPos.projectOnto(z);
            firstEnd = secondPos.projectOnto(z);
        }

        if (second.equals(Direction.Axis.X)) {
            secondStart = firstPos.projectOnto(x);
            secondEnd = secondPos.projectOnto(x);
        } else if (second.equals(Direction.Axis.Y)) {
            secondStart = firstPos.projectOnto(y);
            secondEnd = secondPos.projectOnto(y);
        } else {
            secondStart = firstPos.projectOnto(z);
            secondEnd = secondPos.projectOnto(z);
        }

        int firstTotal = (int) Math.ceil(firstStart.distanceTo(firstEnd))
                + (!isClick && first.equals(Direction.Axis.Y) ? 0 : 1);

        int secondTotal = (int) Math.ceil(secondStart.distanceTo(secondEnd))
                + (!isClick && second.equals(Direction.Axis.Y) ? 0 : 1);

        // - 1 for the intersection between the two
        return firstTotal + secondTotal - ((firstTotal != 0 && secondTotal != 0) ? 1 : 0);
    }

    public static int calculateBlocksFree(Vec3d firstPos, Vec3d secondPos, boolean isClick) {
        Vec3d firstPosInt = Random.toIntVec(firstPos);
        Vec3d secondPosInt = Random.toIntVec(secondPos);

        Vec3d startPos = new Vec3d(firstPosInt.x, firstPosInt.y - (isClick ? 0 : 1), firstPosInt.z);
        Vec3d endPos = new Vec3d(secondPosInt.x, secondPosInt.y, secondPosInt.z);

        int x = (int) startPos.x;
        int y = (int) startPos.y;
        int z = (int) startPos.z;

        int dy = Math.abs(((int) endPos.y) - y);
        int dx = Math.abs(((int) endPos.x) - x);
        int dz = Math.abs(((int) endPos.z) - z);

        int totalBlocks = 1;

        Vec3d xs, ys, zs;

        xs = new Vec3d(endPos.x > startPos.x ? 1 : -1, 0, 0);
        ys = new Vec3d(0, endPos.y > startPos.y ? 1 : -1, 0);
        zs = new Vec3d(0, 0, endPos.z > startPos.z ? 1 : -1);

        Direction.Axis largestDiff = BlockCalculations.findLargestAxisDiff(startPos, endPos);

        if (largestDiff.equals(Direction.Axis.X)) {
            int p1 = 2 * dy - dx;
            int p2 = 2 * dz - dx;

            while (startPos.x != endPos.x) {
                startPos = startPos.add(xs);
                if (p1 >= 0) {
                    startPos = startPos.add(ys);
                    p1 -= 2 * dx;
                }
                if (p2 >= 0) {
                    startPos = startPos.add(zs);
                    p2 -= 2 * dx;
                }
                p1 += 2 * dy;
                p2 += 2 * dz;
                totalBlocks++;
            }
        } else if (largestDiff.equals(Direction.Axis.Y)) {
            int p1 = 2 * dx - dy;
            int p2 = 2 * dz - dy;

            while (startPos.y != endPos.y) {
                startPos = startPos.add(ys);
                if (p1 >= 0) {
                    startPos = startPos.add(xs);
                    p1 -= 2 * dy;
                }
                if (p2 >= 0) {
                    startPos = startPos.add(zs);
                    p2 -= 2 * dy;
                }
                p1 += 2 * dx;
                p2 += 2 * dz;
                totalBlocks++;
            }
        } else {
            int p1 = 2 * dy - dz;
            int p2 = 2 * dx - dz;

            while (startPos.z != endPos.z) {
                startPos = startPos.add(zs);
                if (p1 >= 0) {
                    startPos = startPos.add(ys);
                    p1 -= 2 * dz;
                }
                if (p2 >= 0) {
                    startPos = startPos.add(xs);
                    p2 -= 2 * dz;
                }
                p1 += 2 * dy;
                p2 += 2 * dx;
                totalBlocks++;
            }
        }

        return totalBlocks;
    }

    public static Direction.Axis findLargestAxisDiff(Vec3d firstPos, Vec3d secondPos) {

        Vec3d diff = secondPos.subtract(firstPos);

        double x = Math.abs(diff.x);
        double y = Math.abs(diff.y);
        double z = Math.abs(diff.z);

        double maxDiff = Math.max(x, Math.max(y, z));

        if (maxDiff == x) {
            return Direction.Axis.X;
        } else if (maxDiff == y) {
            return Direction.Axis.Y;
        } else {
            return Direction.Axis.Z;
        }

    }

    public static List<Direction.Axis> findTwoLargestAxisDiff(Vec3d firstPos, Vec3d secondPos) {
        List<Direction.Axis> axes = new ArrayList<>(Arrays.asList(Direction.Axis.X, Direction.Axis.Y, Direction.Axis.Z));

        Vec3d diff = secondPos.subtract(firstPos);

        double x = Math.abs(diff.x);
        double y = Math.abs(diff.y);
        double z = Math.abs(diff.z);

        double minDiff = Math.min(x, Math.min(y, z));

        if (minDiff == x) {
            axes.removeFirst();
        } else if (minDiff == y) {
            axes.remove(1);
        } else {
            axes.remove(2);
        }

        return axes;
    }

    public static Direction.Axis findLargestAxisDiff(Vec3d pos) {
        double maxDiff = Math.max(pos.x, Math.max(pos.y, pos.z));

        if (maxDiff == pos.x) {
            return Direction.Axis.X;
        } else if (maxDiff == pos.y) {
            return Direction.Axis.Y;
        } else {
            return Direction.Axis.Z;
        }
    }

}
