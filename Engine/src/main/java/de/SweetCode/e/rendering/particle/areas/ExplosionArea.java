package de.SweetCode.e.rendering.particle.areas;

import de.SweetCode.e.E;
import de.SweetCode.e.Renderable;
import de.SweetCode.e.input.InputEntry;
import de.SweetCode.e.math.CircleBox;
import de.SweetCode.e.math.Location;
import de.SweetCode.e.math.Vector2D;
import de.SweetCode.e.rendering.layers.Layer;
import de.SweetCode.e.rendering.layers.Layers;
import de.SweetCode.e.rendering.particle.Particle;
import de.SweetCode.e.rendering.particle.ParticleTypes;
import de.SweetCode.e.utils.Assert;
import de.SweetCode.e.utils.ToString.ToStringBuilder;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExplosionArea implements Renderable {

    private final List<Particle> particles = new ArrayList<>();

    private Layer layer;
    private CircleBox circleBox;
    private final boolean evenDistribution;
    private final int stepDelay;
    private final int lifetime;

    private int amount;
    private int step;
    private int currentStep = 0;

    private long timePassed;

    /**
     * The constructor to create a new explosion area.
     *
     * @param layer The layer to setScene the explosion on.
     * @param circleBox The area of the explosion.
     * @param amount The amount of particles used in the explosion.
     * @param steps The amount of steps to spawn all particles.
     * @param stepDelay The delay between each step.
     * @param evenDistribution Should the particle be evenly distributed in the area.
     */
    public ExplosionArea(Layer layer, CircleBox circleBox, boolean evenDistribution, int amount, int steps, int stepDelay) {

        Assert.assertNotNull("The layer cannot be null.", layer);
        Assert.assertNotNull("The circle box cannot be null.", circleBox);
        Assert.assertTrue("The amount of particles cannot be lass than 2.", amount > 1);
        Assert.assertTrue("The amount of steps cannot be less than 1.", amount > 0);
        Assert.assertTrue("The step delay cannot be less than 1.", stepDelay > 0);

        this.layer = layer;
        this.circleBox = circleBox;
        this.evenDistribution = evenDistribution;
        this.amount = amount;
        this.step = amount / steps;
        this.stepDelay = stepDelay;
        this.lifetime = steps * stepDelay;

        this.setup();
    }

    private void setup() {

        for(int i = 0; i < this.amount; i++) {

            this.particles.add(
                    Particle.Builder.create()
                            .layer(this.layer)
                            .location(ExplosionArea.getRandomLocation(this.circleBox, this.evenDistribution))
                            .vector2D(new Vector2D(0, 0))
                            .particleType(ParticleTypes.RANDOM)
                            .color(Color.ORANGE)
                            .fadeInAndOut(true)
                            .destroyItself(true)
                            .mixType(false)
                            .lifeSpan(this.lifetime)
                            .width(10)
                    .build()
            );

        }

        this.particles.sort((o1, o2) -> {

            double o1D = o1.getLocation().distanceTo(this.circleBox.getCenter());
            double o2D = o2.getLocation().distanceTo(this.circleBox.getCenter());

            if (o1D == o2D) {
                return 0;
            }

            return (o1D > o2D ? 1 : -1);

        });

    }

    @Override
    public void render(Layers value) {}

    @Override
    public void update(InputEntry input, long delta) {

        this.timePassed += delta;


        if(this.stepDelay <= this.timePassed && this.currentStep < this.particles.size()) {

            long deltaStep = Math.min((currentStep + (this.timePassed / this.stepDelay * this.step)), this.particles.size() - 1);

            for(int i = this.currentStep; i < deltaStep; i++) {
                E.getE().addComponent(this.particles.get(i));
            }

            this.currentStep += step;
            this.timePassed = 0;

        }

    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public String toString() {
        return ToStringBuilder.create(this)
                .append("amount", this.amount)
                .append("step", this.step)
                .append("currentStep", this.currentStep)
                .append("evenDistribution", this.evenDistribution)
                .append("stepDelay", this.stepDelay)
                .append("lifetime", this.lifetime)
                .append("circleBox", this.circleBox)
                .append("particles", this.particles)
            .build();
    }

    private static Location getRandomLocation(CircleBox circleBox, boolean evenDistribution) {
        double angle = E.getE().getRandom(false).nextDouble() * Math.PI * 2;
        double radius = (evenDistribution ? Math.sqrt(Math.random()) : Math.random()) * circleBox.getRadius();

        return new Location(
                circleBox.getCenter().getX() + radius * Math.cos(angle),
                circleBox.getCenter().getY() + radius * Math.sin(angle)
        );
    }

}
