package net.cosette.miyabi.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.cosette.miyabi.magic.spell.SpellProjectile;

public class SpellProjectileRenderer extends EntityRenderer<SpellProjectile> {
    public SpellProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
    @Override
    public ResourceLocation getTextureLocation(SpellProjectile entity) {
        return null;
    }
}