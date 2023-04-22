package polis.ok.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public final class AnimationMedia extends Media implements Serializable {
    @JsonProperty("list")
    public final Collection<Animation> animations;

    public AnimationMedia(Collection<Animation> animations) {
        super("animation");
        this.animations = animations;
    }

    public AnimationMedia(int animationCount) {
        this(new ArrayList<>(animationCount));
    }

    public void addAnimation(Animation animation) {
        animations.add(animation);
    }
}
