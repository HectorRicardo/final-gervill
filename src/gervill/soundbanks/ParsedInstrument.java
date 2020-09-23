package gervill.soundbanks;

import gervill.com.sun.media.sound.ModelInstrument;
import gervill.com.sun.media.sound.ModelPerformer;
import gervill.javax.sound.midi.Patch;
import own.main.ImmutableList;

abstract class ParsedInstrument extends ModelInstrument  {

    ParsedInstrument(Patch patch, String name) {
        super(patch, name);
    }

    private ImmutableList<ModelPerformer> performers;

    public ImmutableList<ModelPerformer> getPerformers() {
        if (performers == null) {
            performers = ImmutableList.create(buildPerformers());
        }
        return performers;
    }

    abstract ModelPerformer[] buildPerformers();
}
