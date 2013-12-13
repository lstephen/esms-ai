package com.ljs.ifootballmanager.ai;

import com.ljs.ifootballmanager.ai.rating.Weighting;

/**
 *
 * @author lstephen
 */
public enum Tactic {
    NORMAL('N') {
        public Weighting getWeighting(Role r) {
            switch (r) {
                case GK:
                    return Weighting.builder().st(100).build();
                case DF:
                    return Weighting.builder().tk(100).ps(50).sh(30).build();
                case MF:
                    return Weighting.builder().tk(30).ps(100).sh(30).build();
                case FW:
                    return Weighting.builder().tk(30).ps(30).sh(100).build();
                case DM:
                    return Weighting.builder().tk(67).ps(87).sh(30).build();
                case AM:
                    return Weighting.builder().tk(30).ps(87).sh(67).build();
            }

            throw new IllegalStateException();
        }
    },
    DEFENSIVE('D') {
        public Weighting getWeighting(Role r) {
            switch (r) {
                case GK:
                    return Weighting.builder().st(100).build();
                case DF:
                    return Weighting.builder().tk(125).ps(25).sh(0).build();
                case MF:
                    return Weighting.builder().tk(100).ps(75).sh(25).build();
                case FW:
                    return Weighting.builder().tk(50).ps(25).sh(75).build();
                case DM:
                    return Weighting.builder().tk(110).ps(65).sh(5).build();
                case AM:
                    return Weighting.builder().tk(65).ps(65).sh(50).build();
            }

            throw new IllegalStateException();
        }
    },
    ATTACKING('A') {
        public Weighting getWeighting(Role r) {
            switch (r) {
                case GK:
                    return Weighting.builder().st(100).build();
                case DF:
                    return Weighting.builder().tk(100).ps(50).sh(50).build();
                case MF:
                    return Weighting.builder().tk(0).ps(100).sh(75).build();
                case FW:
                    return Weighting.builder().tk(0).ps(65).sh(150).build();
                case DM:
                    return Weighting.builder().tk(50).ps(90).sh(50).build();
                case AM:
                    return Weighting.builder().tk(0).ps(90).sh(115).build();
            }

            throw new IllegalStateException();
        }
    },
    COUNTER_ATTACK('C') {
        public Weighting getWeighting(Role r) {
            switch (r) {
                case GK:
                    return Weighting.builder().st(100).build();
                case DF:
                    return Weighting.builder().tk(100).ps(50).sh(25).build();
                case MF:
                    return Weighting.builder().tk(50).ps(100).sh(25).build();
                case FW:
                    return Weighting.builder().tk(50).ps(50).sh(100).build();
                case DM:
                    return Weighting.builder().tk(75).ps(85).sh(25).build();
                case AM:
                    return Weighting.builder().tk(60).ps(85).sh(60).build();
            }

            throw new IllegalStateException();
        }
    },
    LONG_BALL('L') {
        public Weighting getWeighting(Role r) {
            switch (r) {
                case GK:
                    return Weighting.builder().st(100).build();
                case DF:
                    return Weighting.builder().tk(110).ps(60).sh(10).build();
                case MF:
                    return Weighting.builder().tk(50).ps(70).sh(65).build();
                case FW:
                    return Weighting.builder().tk(10).ps(30).sh(130).build();
                case DM:
                    return Weighting.builder().tk(75).ps(60).sh(50).build();
                case AM:
                    return Weighting.builder().tk(30).ps(50).sh(100).build();
            }

            throw new IllegalStateException();
        }
    },
    PASSING('P') {
        public Weighting getWeighting(Role r) {
            switch (r) {
                case GK:
                    return Weighting.builder().st(100).build();
                case DF:
                    return Weighting.builder().tk(100).ps(75).sh(30).build();
                case MF:
                    return Weighting.builder().tk(25).ps(100).sh(25).build();
                case FW:
                    return Weighting.builder().tk(25).ps(75).sh(100).build();
                case DM:
                    return Weighting.builder().tk(65).ps(85).sh(20).build();
                case AM:
                    return Weighting.builder().tk(25).ps(85).sh(65).build();
            }

            throw new IllegalStateException();
        }
    },
    EUROPEAN('E') {
        public Weighting getWeighting(Role r) {
            switch (r) {
                case GK:
                    return Weighting.builder().st(100).build();
                case DF:
                    return Weighting.builder().tk(100).ps(30).sh(40).build();
                case MF:
                    return Weighting.builder().tk(25).ps(125).sh(40).build();
                case FW:
                    return Weighting.builder().tk(20).ps(40).sh(95).build();
                case DM:
                    return Weighting.builder().tk(75).ps(100).sh(15).build();
                case AM:
                    return Weighting.builder().tk(15).ps(100).sh(75).build();
            }

            throw new IllegalStateException();
        }
    };

    private final Character code;

    Tactic(Character code) {
        this.code = code;
    }

    public Character getCode() {
        return code;
    }

    public abstract Weighting getWeighting(Role r);


}
