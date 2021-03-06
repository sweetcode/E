package de.SweetCode.e.rendering.particle.areas;

import de.SweetCode.e.E;
import de.SweetCode.e.Renderable;
import de.SweetCode.e.input.InputEntry;
import de.SweetCode.e.math.BoundingBox;
import de.SweetCode.e.math.Location;
import de.SweetCode.e.math.Vector2D;
import de.SweetCode.e.rendering.layers.Layer;
import de.SweetCode.e.rendering.layers.Layers;
import de.SweetCode.e.rendering.particle.Particle;
import de.SweetCode.e.rendering.particle.ParticleTypes;
import de.SweetCode.e.utils.Assert;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ParticleArea implements Renderable {

    private final List<Particle> particles = new ArrayList<>();

    /**
     * @param layer The layer the area should be rendered on.
     * @param boundingBox The bounding box to defeine the size of the area.
     * @param vector2D The vector for all particles.
     * @param particleType The particle type.
     * @param color The color of all particles.
     * @param mixType If it is set to true than it iterates through all different particle types.
     * @param lifeSpan The amount of time the particle should exist.
     * @param width The width of a particle
     * @param amount The amount of particles spawned in the area.
     */
    public ParticleArea(Layer layer, BoundingBox boundingBox, Vector2D vector2D, ParticleTypes particleType, Color color, boolean mixType, long lifeSpan, int width, int amount) {

        Assert.assertNotNull(boundingBox);
        Assert.assertNotNull(particleType);
        Assert.assertTrue("The width of a particle cannot be less than 1.", width > 0);
        Assert.assertTrue("The width of a particle cannot be less than 2 if you want to spawn a particle just use the Particle object.", amount > 0);

        for(int i = 0; i < amount; i++) {

            Particle particle = Particle.Builder.create()
                                    .layer(layer)
                                    .location(ParticleArea.getRandomLocation(boundingBox))
                                    .vector2D(vector2D)
                                    .particleType(particleType)
                                    .color(color == null ? new Color(E.getE().getRandom(false).nextInt(255), E.getE().getRandom(false).nextInt(255), E.getE().getRandom(false).nextInt(255)) : color)
                                    .destroyItself(false)
                                    .fadeInAndOut(false)
                                    .mixType(mixType)
                                    .lifeSpan(lifeSpan)
                                    .width(width)
                                .build();
            this.particles.add(particle);
            E.getE().addComponent(particle);

        }

    }

    /**
     * @param layer The layer the area should be rendered on.
     * @param boundingBox The bounding box to defeine the size of the area.
     * @param vector2D The vector for all particles.
     * @param particleType The particle type.
     * @param color The color of all particles.
     * @param width The width of a particle
     * @param amount The amount of particles spawned in the area.
     */
    public ParticleArea(Layer layer, BoundingBox boundingBox, Vector2D vector2D, ParticleTypes particleType, Color color, int width, int amount) {
        this(layer, boundingBox, vector2D, particleType, color, false, -1, width, amount);
    }

    /**
     * @param layer The layer the area should be rendered on.
     * @param boundingBox The bounding box to defeine the size of the area.
     * @param vector2D The vector for all particles.
     * @param particleType The particle type.
     * @param width The width of a particle
     * @param amount The amount of particles spawned in the area.
     */
    public ParticleArea(Layer layer, BoundingBox boundingBox, Vector2D vector2D, ParticleTypes particleType, int width, int amount) {
        this(layer, boundingBox, vector2D, particleType, null, false, -1, width, amount);
    }

    /**
     * @param layer The layer the area should be rendered on.
     * @param boundingBox The bounding box to defeine the size of the area.
     * @param vector2D The vector for all particles.
     * @param width The width of a particle
     * @param amount The amount of particles spawned in the area.
     * @param mixType If it is set to true than it iterates through all different particle types.
     * @param color The color of all particles.
     */
    public ParticleArea(Layer layer, BoundingBox boundingBox, Vector2D vector2D, Color color, boolean mixType, int width, int amount) {
        this(layer, boundingBox, vector2D, ParticleTypes.RANDOM, color, mixType, -1, width, amount);
    }

    /**
     * @param layer The layer the area should be rendered on.
     * @param boundingBox The bounding box to defeine the size of the area.
     * @param vector2D The vector for all particles.
     * @param width The width of a particle
     * @param amount The amount of particles spawned in the area.
     * @param mixType If it is set to true than it iterates through all different particle types.
     */
    public ParticleArea(Layer layer, BoundingBox boundingBox, Vector2D vector2D, boolean mixType, int width, int amount) {
        this(layer, boundingBox, vector2D, ParticleTypes.RANDOM, null, mixType, -1, width, amount);
    }


    /**
     * Desroys all particles
     * @param destroy Should the particle be destroyed?
     */
    public void setDestroy(boolean destroy) {
        this.particles.forEach(e -> e.setDestroy(destroy));
    }

    @Override
    public void render(Layers value) {}

    @Override
    public void update(InputEntry input, long delta) {

        if(!(this.particles.get(0).isActive())) {
            this.particles.clear();
        }

    }

    @Override
    public boolean isActive() {
        return (this.particles.size() > 0);
    }

    private static Location getRandomLocation(BoundingBox boundingBox) {

        Location min = boundingBox.getMin();
        Location max = boundingBox.getMax();

        Location relative = new Location(max.getX() - min.getX(), max.getY() - min.getY());

        double x = E.getE().getRandom(false).nextDouble() * relative.getX() + min.getX();
        double y = E.getE().getRandom(false).nextDouble() * relative.getY() + min.getY();

        return new Location(x, y);

    }

}
