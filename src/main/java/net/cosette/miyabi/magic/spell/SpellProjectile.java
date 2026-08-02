package net.cosette.miyabi.magic.spell;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.cosette.miyabi.registry.ModEntities;

public class SpellProjectile extends AbstractHurtingProjectile {
    private String spellId = "fireball";
    private boolean silentCast = false;
    private float intensityPercent = 100f;
    public SpellProjectile(EntityType<? extends SpellProjectile> type, Level level) {
        super(type, level);
    }
    public SpellProjectile(Level level, LivingEntity shooter, double dx, double dy, double dz,
                           String spellId, boolean silentCast, float intensityPercent) {
        super(ModEntities.SPELL_PROJECTILE.get(),
                shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ(),
                new Vec3(dx, dy, dz), level);
        this.setOwner(shooter);
        this.spellId = spellId;
        this.silentCast = silentCast;
        this.intensityPercent = intensityPercent;
    }
    public String getSpellId() { return spellId; }
    private SpellDefinition definition() {
        return SpellRegistry.byId(spellId);
    }
    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide) return;
        SpellDefinition def = definition();
        if (def == null) return;
        LivingEntity caster = getOwner() instanceof LivingEntity le ? le : null;
        SpellCastContext context = new SpellCastContext(caster, this, silentCast, intensityPercent);
        def.effect().apply(result.getEntity(), def, context);
    }
    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }
    @Override
    public boolean displayFireAnimation() {
        return false;
    }
}