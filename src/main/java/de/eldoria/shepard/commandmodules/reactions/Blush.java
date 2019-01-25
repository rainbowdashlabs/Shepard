package de.eldoria.shepard.commandmodules.reactions;

public class Blush extends Reaction {

    public Blush() {
        super("blush",
                null);
    }

    @Override
    protected String[] getImages() {
        return getREACTIONS().getBlush();
    }
}
