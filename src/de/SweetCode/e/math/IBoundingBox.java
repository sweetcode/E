package de.SweetCode.e.math;

import de.SweetCode.e.utils.ToString.ToStringBuilder;

public class IBoundingBox {

    private final ILocation min;
    private final ILocation max;

    public IBoundingBox(Location a, Location b) {
        this(
                Math.min((int) a.getX(), (int) b.getX()),
                Math.min((int) a.getY(), (int) b.getY()),
                Math.max((int) a.getX(), (int) b.getX()),
                Math.max((int) a.getY(), (int) b.getY())
        );
    }

    public IBoundingBox(ILocation a, ILocation b) {
        this(new Location(a), new Location(b));
    }

    public IBoundingBox(int minX, int minY, int maxX, int maxY) {
        this.min = new ILocation(minX, minY);
        this.max = new ILocation(maxX, maxY);
    }

    public void move(int x, int y) {
        this.getMax().add(x, y);
        this.getCenter().add(x, y);
        this.getMin().add(x, y);
    }

    public ILocation getMax() {
        return this.max;
    }

    public Location getCenter() {
        return new Location(((this.getMin().getX() + this.getMax().getX()) / 2.0D), ((this.getMin().getY() + this.getMax().getY()) / 2.0D));
    }

    public ILocation getMin() {
        return this.min;
    }

    public int getHeight() {
        return Math.abs(this.getMax().getY() - this.getMin().getY());
    }

    public int getWidth() {
        return Math.abs(this.getMax().getX() - this.getMin().getX());
    }

    public boolean intersects(double minX, double minY, double maxX, double maxY) {
        return this.intersects(new BoundingBox(minX, minY, maxX, maxY));
    }

    public boolean intersects(BoundingBox other) {
        return (
                this.getMin().getX() < other.getMax().getX() &&
                        this.getMax().getX() > other.getMin().getX() &&
                        this.getMin().getY() < other.getMax().getY() &&
                        this.getMax().getY() > other.getMin().getY()
        );
    }

    public boolean contains(BoundingBox other) {
        return (
                this.getMin().getX() <= other.getMin().getX() &&
                        this.getMax().getX() <= other.getMax().getX() &&
                        this.getMin().getY() <= other.getMin().getY() &&
                        this.getMax().getY() <= other.getMax().getY()
        );
    }

    public boolean contains(Location location) {
        return (
                location.getX() >= this.min.getX() &&
                        location.getX() <= this.max.getX() &&
                        location.getY() >= this.min.getY() &&
                        location.getY() <= this.max.getY()
        );
    }

    public boolean contains(ILocation location) {
        return this.contains(new Location(location));
    }

    @Override
    public String toString() {
        return ToStringBuilder.create(this)
                .append("min", this.min)
                .append("max", this.max)
                .build();
    }

}
