package org.cheplay.model.playlist;

/**
 * Encapsulates the high-level constraints for playlist generation.
 */
public class PlaylistConstraints {
    private final int targetSize;
    private final boolean uniqueArtist;
    private final Integer minTotalDuration;
    private final Integer maxTotalDuration;

    private PlaylistConstraints(Builder builder) {
        this.targetSize = builder.targetSize;
        this.uniqueArtist = builder.uniqueArtist;
        this.minTotalDuration = builder.minTotalDuration;
        this.maxTotalDuration = builder.maxTotalDuration;
    }

    public int getTargetSize() {
        return targetSize;
    }

    public boolean isUniqueArtist() {
        return uniqueArtist;
    }

    public Integer getMinTotalDuration() {
        return minTotalDuration;
    }

    public Integer getMaxTotalDuration() {
        return maxTotalDuration;
    }

    public static Builder builder(int targetSize) {
        return new Builder(targetSize);
    }

    public static class Builder {
        private final int targetSize;
        private boolean uniqueArtist = true;
        private Integer minTotalDuration;
        private Integer maxTotalDuration;

        public Builder(int targetSize) {
            if (targetSize <= 0) {
                throw new IllegalArgumentException("targetSize must be positive");
            }
            this.targetSize = targetSize;
        }

        public Builder uniqueArtist(boolean value) {
            this.uniqueArtist = value;
            return this;
        }

        public Builder minTotalDuration(Integer seconds) {
            this.minTotalDuration = seconds != null && seconds <= 0 ? null : seconds;
            return this;
        }

        public Builder maxTotalDuration(Integer seconds) {
            this.maxTotalDuration = seconds != null && seconds <= 0 ? null : seconds;
            return this;
        }

        public PlaylistConstraints build() {
            if (minTotalDuration != null && maxTotalDuration != null && minTotalDuration > maxTotalDuration) {
                throw new IllegalArgumentException("minTotalDuration cannot exceed maxTotalDuration");
            }
            return new PlaylistConstraints(this);
        }
    }

    public PlaylistConstraints withTargetSize(int newSize) {
        return builder(newSize)
            .uniqueArtist(uniqueArtist)
            .minTotalDuration(minTotalDuration)
            .maxTotalDuration(maxTotalDuration)
            .build();
    }

    public PlaylistConstraints withUniqueArtist(boolean value) {
        return builder(targetSize)
            .uniqueArtist(value)
            .minTotalDuration(minTotalDuration)
            .maxTotalDuration(maxTotalDuration)
            .build();
    }
}
