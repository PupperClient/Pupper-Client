package cn.pupperclient.mixin.mixins.minecraft.client.input;

import cn.pupperclient.event.EventBus;
import cn.pupperclient.event.client.EventMoveInput;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({KeyboardInput.class})
public class MixinKeyboardInput extends Input {
    @Inject(
        at = {@At("TAIL")},
        method = {"tick"}
    )
    private void onTickTail(CallbackInfo ci) {
        EventMoveInput eventMoveInput = new EventMoveInput(this.movementForward, this.movementSideways, this.playerInput.jump(), this.playerInput.sneak(), 0.3);
        EventBus.getInstance().post(eventMoveInput);
        if (!eventMoveInput.isCancelled()) {
            this.movementForward = eventMoveInput.getForward();
            this.movementSideways = eventMoveInput.getStrafe();
            this.playerInput.jump = eventMoveInput.isJump();
            this.playerInput.sneak = eventMoveInput.isSneak();
        }
    }
}
