package azula.blockcounter.util;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

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

}
