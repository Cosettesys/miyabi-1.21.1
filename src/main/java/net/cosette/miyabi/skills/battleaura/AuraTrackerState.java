package net.cosette.miyabi.skills.battleaura;

/** Dernière position connue du joueur, pour mesurer la distance parcourue (état de session, non sauvegardé). */
public final class AuraTrackerState {
    private boolean hasLast = false;
    private double lastX;
    private double lastZ;
    public boolean hasLast() { return hasLast; }
    public double getLastX() { return lastX; }
    public double getLastZ() { return lastZ; }
    public void setLast(double x, double z) {
        this.lastX = x;
        this.lastZ = z;
        this.hasLast = true;
    }
}