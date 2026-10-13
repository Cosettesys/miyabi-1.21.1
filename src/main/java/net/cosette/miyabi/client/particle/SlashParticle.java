package net.cosette.miyabi.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class SlashParticle extends TextureSheetParticle {
    private static final float ROLL_DEGREES = -50f;
    private static final float BASE_SIZE = 3.4f;
    private final SpriteSet sprites;
    protected SlashParticle(ClientLevel level, double x, double y, double z, double extraSize, SpriteSet sprites) {
        super(level, x, y, z, 0.0, 0.0, 0.0);
        this.sprites = sprites;
        this.lifetime = 5;
        this.quadSize = BASE_SIZE + (float) extraSize;
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        this.roll = (float) Math.toRadians(ROLL_DEGREES);
        this.oRoll = this.roll;
        this.setSpriteFromAge(sprites);
    }
    @Override
    public int getLightColor(float partialTick) {
        return 15728880; // pleine luminosité, comme le sweep vanilla
    }
    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.setSpriteFromAge(this.sprites);
        }
    }
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            // xSpeed transporte une taille supplémentaire (0 par défaut).
            return new SlashParticle(level, x, y, z, xSpeed, this.sprites);
        }
    }
}