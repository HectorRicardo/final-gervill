package own.main;

import gervill.javax.sound.midi.Instrument;
import gervill.soundbanks.DLSSoundbankParser;
import gervill.soundbanks.SF2SoundbankParser;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class Main {

    public static void main(String[] args) throws IOException {
        // Step 1. Set up synthesizer
        final SynthesizerPlayer player = new SynthesizerPlayer();

        // Step 2. Repeat operations
        openPlayAndClose(player, SF2SoundbankParser.parseSoundbank(new File("assets/gm.sf2")));
        openPlayAndClose(player, DLSSoundbankParser.parseSoundbank(new File("assets/gm.dls")));
        openPlayAndClose(player, null);
    }

    public static void openPlayAndClose(SynthesizerPlayer player, ImmutableList<Instrument> soundbankInstruments) {
        // Open synthesizer
        player.open(soundbankInstruments);

        // Play something
        if (player.readyToStartPlaying()) { // will always be true
            final String[] instrumentInfos = player.sample1();

            // Print details
            System.out.print("Instruments chosen:  ");
            for (int i = 0; i < instrumentInfos.length; i++) {
                String info = instrumentInfos[i];
                System.out.print(info);
                if (i + 1 < instrumentInfos.length) {
                    System.out.print(String.join("", Collections.nCopies(26 - info.length(), " ")));
                }
            }
            System.out.println();

            player.sample2();
        }

        // Close synthesizer
        player.close();
    }
}
