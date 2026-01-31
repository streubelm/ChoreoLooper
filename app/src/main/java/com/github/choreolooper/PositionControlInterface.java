package com.github.choreolooper;

/**
 * Interface for controlling the active scene and mark externally.
 */
public interface PositionControlInterface {

    /**
     * Select a specific scene from an external source.
     *
     * @param scene scene to be activated
     */
    void ext_selectScene(Scene scene);

    /**
     * Select a specific mark from an external source.
     *
     * @param mark mark to be selected
     */
    void ext_selectMark(Mark mark);
}
