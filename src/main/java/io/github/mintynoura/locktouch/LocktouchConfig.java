package io.github.mintynoura.locktouch;

import folk.sisby.kaleido.api.ReflectiveConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.FloatRange;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.SerializedName;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;

public class LocktouchConfig extends ReflectiveConfig {
    @Comment("The default chance for a Trial Key to be recycled.")
    @SerializedName("recycle_key_chance")
    @FloatRange(min = 0.0f, max = 1.0f)
    public final TrackedValue<Float> recycleKeyChance = this.value(0.0f);
    @Comment("The default chance for a Lockpick to be recycled.")
    @SerializedName("recycle_lockpick_chance")
    @FloatRange(min = 0.0f, max = 1.0f)
    public final TrackedValue<Float> recycleLockpickChance = this.value(0.0f);
    @Comment("The default chance for a Lockpick to successfully pick a Vault.")
    @SerializedName("lockpick_success_chance")
    @FloatRange(min = 0.0f, max = 1.0f)
    public final TrackedValue<Float> lockpickSuccessChance = this.value(0.3f);

    @SerializedName("lockpick_charm_section")
    public final LockpickCharmSection lockpickCharmSection = new LockpickCharmSection();
    public static final class LockpickCharmSection extends Section {
        @Comment("The modifier used for the Lockpicking Charm's key recycling.")
        @SerializedName("recycle_key_modifier")
        @FloatRange(min = 0.0f, max = 1.0f)
        public final TrackedValue<Float> recycleKeyModifier = this.value(0.4f);
        @Comment("The modifier used for the Lockpicking Charm's Lockpick recycling.")
        @SerializedName("recycle_lockpick_modifier")
        @FloatRange(min = 0.0f, max = 1.0f)
        public final TrackedValue<Float> recycleLockpickModifier = this.value(0.4f);
    }
}
