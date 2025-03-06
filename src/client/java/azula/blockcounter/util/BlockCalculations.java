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
        return firstTotal + secondTotal - 1;
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
