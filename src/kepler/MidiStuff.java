/***************************************************
 * Copyright 2007 by Simran Gleason                *
 * This program is distributed under the terms     *
 * of the GNU General Public License.              *
 * See kepler.Kepler.LICENSE_TEXT or               *
 * http://www.gnu.org/licenses/gpl.txt             *
 ***************************************************/

package kepler;


import javax.sound.midi.*;

/*
  Interesting instruments
  channel 0  piano1        0 0 
  channel 0  honkytonkpiano        0 3
  channel 0  electricpiano2        0 5
  channel 0  celesta         0 8
  channel 1  NylonGuitar       0 24
  channel 1  OrchHit       0 55
  channel 1  SynStrings    0 50
  channel 1  oboe          0 68
  channel 1  french_horn   0 69
  channel 1  clarinet   0 71
  channel 1  piccolo   0 72
  channel 1  panflute    0 75
  channel 1  bottle    0 76
  channel 1  SynStrings    0 50
  channel 4  vibes         0 11
  channel 5  organ         0 19
  channel 11 slowstrings  0 49
  channel 12 pizzicato    0 45
  channel 13 choir_aahs   0 52
  channel 14 choir_oohs   0 53
  channel 15 tuba         0 58
  channel 1  IceRain         0 96  // synthy
  channel 1  Soundtrack         0 97  // synthy
  channel 1  Crystal         0 98  // short sharp
  channel 1  goblins         0 101  
  channel 1  SciFiHorns         0 103  
  channel 1  Kalimba         0 108  // plinky
  channel 1  IceRain         0 96  
  channel 1  Fantasia         0 88  
  channel 1  BowedGlass         0 92
  channel 1  HaloPad         0 94

  channel 1  AltoSax         0 94 good lows. 
  channel 1 MetalPad         0 93 good lows
  channel 1 SweepPad         0 95 highs
  channel 1 EchoDrops        0 102 ?maybe?
  channel 1 koto             0 107 shorts
  channel 1 bagpipe          0 109
  channel 1 Fiddle           0 110  good strings
  channel 1 Agogo            0 113  short klink
  channel 1 Taiko            0 116  Boom!

  channel 1 ReverseCymbal 0 119
  channel 1 FretNoise     0 120  Chirps
  channel 1 SeaShore      0 122  FX
  channel 1 bird          0 123 spacey chirps
  channel 1 Telephone     0 124
  channel 1 Helicopter    0 125
  channel 1 Applause      0 126 good lows
  channel 1 ReedOrgan     1 4
  channel 1 Accord        1 5  sad lows
  


*/


public class MidiStuff {
    public static final long NOW = -1;

    private Orrery orrery;
    private Receiver rcvr = null;

    Sequencer seq;
    Transmitter seqTrans;
    Synthesizer synth;
    Patch [] currentPatchSet;
    Instrument[] loadedInstruments;
    Soundbank soundbank;
    Receiver synthRcvr;


    public MidiStuff(Orrery u) {
        this.orrery = u;
        init();
        if (rcvr != null) {
            printStuff();
        } else {
            System.out.println("Midi not set up. ");
        }
    }

    public void init() {
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        System.out.println("MidiInfo: ------------");
        for(int i=0; i < infos.length; i++) {
            printMidiInfo(infos[i]);
        }
        System.out.println("-------- ------------");
        try {
            //rcvr = MidiSystem.getReceiver();
            System.out.println("Default Receiver: " + rcvr);
            seq = MidiSystem.getSequencer();
            seqTrans = seq.getTransmitter();
            synth = MidiSystem.getSynthesizer();
            System.out.println("Synth: " + synth);

            if (!(synth.isOpen())) {
                synth.open();
            }
            soundbank = synth.getDefaultSoundbank();


            currentPatchSet = makePatches(defaultPatches);
            System.out.println("Soundbank: " + soundbank);
            synth.loadAllInstruments(soundbank);
            //synth.loadInstruments(soundbank, patches);
            loadedInstruments = synth.getLoadedInstruments();

            loadPatchSet(currentPatchSet);

            synthRcvr = synth.getReceiver();
            rcvr = synthRcvr;
            seqTrans.setReceiver(synthRcvr);

        } catch (Exception ex) {
            System.out.println("Caught exception setting up midi devices. " + ex);
            ex.printStackTrace();
        }
    }

    public long getMicrosecondPosition() {
        return seq.getMicrosecondPosition();
    }
    
    public void reloadPatchSet() {
        loadPatchSet(currentPatchSet);
    }
    
    public void loadPatchSet(Patch []patchSet) {
        if (rcvr == null) {
            return;
        }
        MidiChannel[] channels = synth.getChannels();
        for(int ch=0; ch < channels.length; ch++) {
            MidiChannel channel = channels[ch];
            Patch p = patchSet[ch];
            channel.programChange(p.getBank(), p.getProgram());
            if (orrery.debugLevel > 0) {
                System.out.println("CH" + ch + " bank: " + p.getBank() + " prog: " + channel.getProgram());
            }
        }
    }
    
    public int[][] defaultPatches = new int[][] {
        {0, 0},  // 0 piano1
        {0, 0},  // 1 piano2
        {0, 0},  // 2 piano3
        {0, 0},  // 3 piano4
        {0, 11}, // 4 vibes
        {0, 19},  // 5 church organ

        {0, 21},  // 6 accordian
        {0, 34},  // 7 picked bass
        {0, 35},  // 8 fretless bass
        {0, 40},  // 9 violin

        {0, 41},  // 10 viola
        {0, 49},  // 11 slow strings
        {0, 45},  // 12 pizzicato

        {0, 52},  // 13 choir aahs
        {0, 53},  // 14 choir oohs
        {0, 58},  // 15 tuba
    };
    
    public void setPatch(int nth, int bank, int program) {
        if (currentPatchSet != null) {
            currentPatchSet[nth] = new Patch(bank, program);
            //System.out.println(" REsetting patch " + nth + " : " + bank + " " + program);
        }
    }

    public Patch getPatch(int nth) {
        return currentPatchSet[nth];
    }


    public int getPatchBank(int nth) {
        Patch np = getPatch(nth);
        if (np != null) {
            return np.getBank();
        }
        return -1;
    }

    public int getPatchProgram(int nth) {
        Patch np = getPatch(nth);
        if (np != null) {
            return np.getProgram();
        }
        return -1;
    }
    
    public Patch[] makePatches(int[][] ps) {
        System.out.println("Making Patches. ps.len: " + ps.length);
        Patch[] patches = new Patch[ps.length];
        for(int i=0; i < ps.length; i++) {
            int[] datum = ps[i];
            patches[i] = new Patch(datum[0], datum[1]);
        }
        return patches;
    }
    
    public void printStuff() {
        printSynthStuff(synth);
    }

    public void printSynthStuff(Synthesizer synth) {
        
        System.out.println("Loaded Instruments: " + loadedInstruments.length);
        if (orrery.debugLevel > 0) {
            for(int i=0; i < loadedInstruments.length; i++) {
                System.out.println("LInstrument{" + i + "}: " + loadedInstruments[i]);
                System.out.println("     patch: " + patchString(loadedInstruments[i].getPatch()));
            }
        }
        /*
        Instrument[] ainstruments = synth.getAvailableInstruments();
        System.out.println("Available Instruments: " + ainstruments.length);
        for(int i=0; i < ainstruments.length; i++) {
            System.out.println("AInstrument{" + i + "}: " + ainstruments[i]);
        }
        */

        System.out.println(" CHANNEL INFO =======");
        MidiChannel[] channels = synth.getChannels();
        for(int ch=0; ch < channels.length; ch++) {
            MidiChannel channel = channels[ch];
            //channel.programChange(0, ch);
            System.out.println("CH" + ch + " prog: " + channel.getProgram());
        }
    }

    public String patchString(Patch patch) {
        return "Patch{bank: " + patch.getBank() + " prog: " + patch.getProgram() + "}";
    }

    public void printMidiInfo(MidiDevice.Info info) {
        System.out.println(" Name: " + info.getName());
        System.out.println(" Desc: " + info.getDescription());
        System.out.println(" Vend: " + info.getVendor());
        System.out.println(" Vers: " + info.getVersion());
        System.out.println();
    }

    public void playNote(Note note, int channel, long beatStartTime) {
        playNote(note, note.duration, channel, beatStartTime);
    }
            
    public void playNote(Note note, int duration, int channel, long beatStartTime) {
        if (note.pitch <= 0 || rcvr == null) {
            return;
        }
        try {
            ShortMessage myMsg = new ShortMessage();
            // Start playing the note Middle C (60),
            // moderately loud (velocity = 93).
            myMsg.setMessage(ShortMessage.NOTE_ON, channel, (int)note.pitch, (int)note.velocity);
            long timeStamp = beatStartTime;
            rcvr.send(myMsg, timeStamp);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void noteOff(Note note, int channel) {
        noteOff((int)note.pitch, channel);
    }
    
    public void noteOff(int pitch, int channel) {
        if (pitch <= 0) {
            return;
        }
        try {
            ShortMessage myMsg = new ShortMessage();
            // Start playing the note Middle C (60),
            // moderately loud (velocity = 93).
            myMsg.setMessage(ShortMessage.NOTE_OFF, channel, pitch, 0);
            long timeStamp = -1;
            rcvr.send(myMsg, timeStamp);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void playBank(int bank, int offset) {
        for(int i=0; i < 16; i++) {
            currentPatchSet[i] = new Patch(bank, offset + i);
        }
        reloadPatchSet();
        playSamples(300, 600);
    }

    public void playInst(int bank, int prog) {
        currentPatchSet[0] = new Patch(bank, prog);
        reloadPatchSet();
        playSampleInst(0, 300);
    }

    public void performInst(int bank, int prog, int from, int to, int dur, int vel) {
        currentPatchSet[0] = new Patch(bank, prog);
        reloadPatchSet();
        performInst(0, from, to, dur, vel);
    }

    public void performInst(int channel, int from, int to, int dur, int vel) {
        int between = Math.max(4000, dur);
        for(int pitch=from; pitch <= to; pitch++) {
            Note note = new Note(pitch, vel);
            playNote(note, dur, channel, NOW);
            System.out.println("    Note: " + note + "[ch: " + channel + " p:" + (int)note.getPitch() + ", v:" + (int)note.getVelocity() + "] ");
            
            try { Thread.sleep(dur);
            } catch (InterruptedException e) { System.out.println("Error sleeping"); }
            noteOff(note, channel);
            try { Thread.sleep(between);
            } catch (InterruptedException e) { System.out.println("Error sleeping"); }

        }

    }

    public void playSampleInst(int ch, int dur) {
        Note [] notes1 = new Note[] {
            new Note("A0"),
            new Note("D0"),
            new Note("A1"),
            new Note("D1"),
            new Note("A2"),
            new Note("D2"),
            new Note("A3"),
            new Note("D3"),
            new Note("A4"),
            new Note("D4")
        };

        Note[] notes2 = new Note[] {
            new Note("D0"),
            new Note("E0"),
            new Note("F0"),
            new Note("G0"),
            new Note("A1"),
            new Note("B1"),
            new Note("C1"),
            new Note("D1"),
            new Note("E1")
        };

        Note[] notes3 = new Note[] {
            new Note("D2"),
            new Note("E2"),
            new Note("F2"),
            new Note("G2"),
            new Note("A3"),
            new Note("B3"),
            new Note("C3"),
            new Note("D3"),
            new Note("E3")
        };

        dur=800;
        for(int n=0; n < notes1.length; n++) {
            Note note = notes1[n];
            note.setVelocity(50);
            playNote(note, dur, ch, NOW);
            System.out.println("    Note: " + note + "[ch: " + ch + " p:" + note.getPitch() + ", v:" + note.getVelocity() + "] ");
            
            try { Thread.sleep(dur);
            } catch (InterruptedException e) { System.out.println("Error sleeping"); }
            noteOff(note, ch);
        }
        

        dur = 900;
        for(int n=0; n < notes2.length; n++) {
            Note note = notes2[n];
            note.setVelocity(45);
            playNote(note, dur, ch, NOW);
            System.out.println("    Note: " + note + "[ch: " + ch + " p:" + note.getPitch() + ", v:" + note.getVelocity() + "] ");
            
            try { Thread.sleep(dur);
            } catch (InterruptedException e) { System.out.println("Error sleeping"); }
            noteOff(note, ch);
        }

        dur = 500;
        for(int n=0; n < notes3.length; n++) {
            Note note = notes3[n];
            note.setVelocity(45);
            playNote(note, dur, ch, NOW);
            System.out.println("    Note: " + note + "[ch: " + ch + " p:" + note.getPitch() + ", v:" + note.getVelocity() + "] ");
            
            try { Thread.sleep(dur);
            } catch (InterruptedException e) { System.out.println("Error sleeping"); }
            noteOff(note, ch);
        }
    }
        
    public void playSamples(int dur, int pause) {
        Note[] notes1 = new Note[] {
            new Note("C2"),
            new Note("D2"),
            new Note("E2")//,
            //new Note("G2"),
            //new Note("A2")
        };

        Note[] notes2 = new Note[] {
            new Note("A0"),
            new Note("B0"),
            new Note("E0"),
            new Note("_"),
            new Note("_"),
            new Note("."),
            new Note("."),
            new Note("."),
            new Note("G0"),
            new Note("A1"),
            new Note("B1"),
            new Note("E1"),
            new Note("G2"),
            new Note("A3"),
            new Note("E3"),
            new Note("A2")
        };
        for(int ch = 0; ch < 16; ch++) {
            Patch p = currentPatchSet[ch];
            int inst = p.getBank() * 16 + p.getProgram();
            System.out.println(" Ch: " + ch + " {" + p.getBank() + ", " + p.getProgram() + "] " + loadedInstruments[inst]);
            for(int n=0; n < notes1.length; n++) {
                Note note = notes1[n];
                note.setVelocity(20 * (n + 1));
                playNote(note, 400, ch, NOW);
                System.out.println("    Note: " + note + "[ch: " + ch + " p:" + note.getPitch() + ", v:" + note.getVelocity() + "] ");

                try { Thread.sleep(dur);
                } catch (InterruptedException e) { System.out.println("Error sleeping"); }
                noteOff(note, ch);

            }
            for(int n=0; n < 2 * notes2.length ; n++) {
                if (n < notes2.length) {
                    Note note = notes2[n];
                    note.setVelocity(Math.min(72, 12 * (n+1)));
                    playNote(note, 400, ch, NOW);
                    System.out.println(" Note: " + note);
                } else {
                    Note note = notes2[n - notes2.length];
                    noteOff(note, ch);
                }
                try { Thread.sleep(dur);
                } catch (InterruptedException e) { System.out.println("Error sleeping"); }
            }
            System.out.println();
            try { Thread.sleep(pause);
            } catch (InterruptedException e) { System.out.println("Error sleeping"); }
        }
    }

    public static void main(String[] args) {
        MidiStuff midiStuff = new MidiStuff(new Orrery());

        if (args.length > 0) {
            if (args[0].equals("bank")) {
                int bank = Integer.parseInt(args[1]);
                int offset = Integer.parseInt(args[2]);
                midiStuff.playBank(bank, offset);
            } else if (args[0].equals("inst")) {
                int bank = Integer.parseInt(args[1]);
                int offset = Integer.parseInt(args[2]);
                midiStuff.playInst(bank, offset);
            } else if (args[0].equals("performinst")) {
                int bank = Integer.parseInt(args[1]);
                int offset = Integer.parseInt(args[2]);
                int from = (int)Note.parseNote(args[3]);
                int to = (int)Note.parseNote(args[4]);
                int dur  = Integer.parseInt(args[5]);
                int vel = 75;
                if (args.length > 6) {
                    vel = Integer.parseInt(args[6]);
                }
                midiStuff.performInst(bank, offset, from, to, dur, vel);
            } else {
                midiStuff.playSamples(300, 800);
            }
        }
        System.exit(0);
    }
}
