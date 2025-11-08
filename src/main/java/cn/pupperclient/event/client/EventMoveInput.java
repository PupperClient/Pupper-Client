package cn.pupperclient.event.client;

import cn.pupperclient.event.Event;
import net.minecraft.entity.Entity;

import static cn.pupperclient.utils.IMinecraft.mc;

public class EventMoveInput extends Event {
    public static Object r;
    public static Object s;
    private float forward;
    private float strafe;
    private boolean jump;
    private boolean sneak;
    private double sneakSlowDownMultiplier;

    public float getForward() {
        return this.forward;
    }

    public float getStrafe() {
        return this.strafe;
    }

    public boolean isJump() {
        return this.jump;
    }

    public boolean isSneak() {
        return this.sneak;
    }

    public double getSneakSlowDownMultiplier() {
        return this.sneakSlowDownMultiplier;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof EventMoveInput other)) {
            return false;
        } else if (!other.canEqual(this)) {
            return false;
        } else if (Float.compare(this.getForward(), other.getForward()) != 0) {
            return false;
        } else if (Float.compare(this.getStrafe(), other.getStrafe()) != 0) {
            return false;
        } else if (this.isJump() != other.isJump()) {
            return false;
        } else {
            return this.isSneak() == other.isSneak() && Double.compare(this.getSneakSlowDownMultiplier(), other.getSneakSlowDownMultiplier()) == 0;
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof EventMoveInput;
    }

    @Override
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + Float.floatToIntBits(this.getForward());
        result = result * 59 + Float.floatToIntBits(this.getStrafe());
        result = result * 59 + (this.isJump() ? 79 : 97);
        result = result * 59 + (this.isSneak() ? 79 : 97);
        return result * 59 + Double.hashCode(this.getSneakSlowDownMultiplier());
    }

    @Override
    public String toString() {
        return "EventMoveInput(forward="
            + this.getForward()
            + ", strafe="
            + this.getStrafe()
            + ", jump="
            + this.isJump()
            + ", sneak="
            + this.isSneak()
            + ", sneakSlowDownMultiplier="
            + this.getSneakSlowDownMultiplier()
            + ")";
    }

    public EventMoveInput(float forward, float strafe, boolean jump, boolean sneak, double sneakSlowDownMultiplier) {
        this.forward = forward;
        this.strafe = strafe;
        this.jump = jump;
        this.sneak = sneak;
        this.sneakSlowDownMultiplier = sneakSlowDownMultiplier;
    }

    public void setForward(float closestForward) {
        this.forward = closestForward;
    }

    public void setStrafe(float closestStrafe) {
        this.strafe = closestStrafe;
    }

    public Entity getEntity() {
        return mc.player;
    }
}
