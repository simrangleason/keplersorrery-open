/***************************************************
 * Copyright 2007 by Simran Gleason                *
 * This program is distributed under the terms     *
 * of the GNU General Public License.              *
 * See kepler.Kepler.LICENSE_TEXT or               *
 * http://www.gnu.org/licenses/gpl.txt             *
 ***************************************************/

package kepler;

import java.util.HashMap;

public class Note extends Playable {

    public float pitch;
    public float velocity;
    public int duration;  // ms
    public static float DEFAULT_VELOCITY = 65.f;
    public static int DEFAULT_DURATION = 444; // ms
    public static Note EMPTY = new Note(-1);

    public static int REST = -1;
    public static int REST_STOP = -2;

    public Note(float pitch, float vel, int duration) {
        this.pitch = pitch;
        this.velocity = vel;
        this.duration = duration;
    }

    public Note(float pitch, float vel) {
        this.pitch = pitch;
        this.velocity = vel;
        this.duration = DEFAULT_DURATION;
    }
    
    public Note(float pitch) {
        this.pitch = pitch;
        this.velocity = DEFAULT_VELOCITY;
        this.duration = DEFAULT_DURATION;
    }

    public Note(String notestr) {
        this.pitch = parseNote(notestr);
        this.velocity = DEFAULT_VELOCITY;
    }

    public Note(String notestr, float vel) {
        this.pitch = parseNote(notestr);
        this.velocity = vel;
    }

    ///////////////////////
    /// Playabe api     ///
    ///////////////////////

    public String getName() {
        return noteString((int)pitch);
    }

    public String getType() {
        return "note";
    }
    
    // Playable api for getting the next note.
    public Note nextNote(Body b, Orrery orrery) {
        return this;
    }
    public boolean atEnd()  {
        return true;
    }

    public Playable duplicate() {
        return new Note(this.pitch, this.velocity);
    }

    public void reset() {
    }

    public void writeBody(StringBuffer buf) {
        buf.append(getName());
    }

    public void writeCurrentState(StringBuffer buf) {
        writeBody(buf);
    }
        
    ///////////////////////////////////////

    public float getPitch() {
        return this.pitch;
    }
    public void setPitch(float val) {
        this.pitch = val;
    }

    public boolean isRest() {
        return pitch < 0;
    }

    public boolean isRestStop() {
        return pitch == REST_STOP;
    }
    
    public float getVelocity() {
        return this.velocity;
    }
    public void setVelocity(float val) {
        this.velocity = val;
    }


    public static float midiToFreq(float midi) {
        return 0.0f;
    }

    public static float freqToMidi(float freq) {
        return 0.0f;
    }

    // Notes:  <degree>[<inflection>]<octave>
    //   e.g. C3, c3 = middle C = midi pitch 60
    //        Bb4 = B flat an octave above middle C 
    //        D#2 = D sharp an octave below middle C
    //      c:0 c#d:1 ... g:4 a:5
    
    public static HashMap notesHash = new HashMap();
    static {
        notesHash.put("C", new Integer(0));
        notesHash.put("C#", new Integer(1));
        notesHash.put("DB", new Integer(1));
        notesHash.put("D", new Integer(2));
        notesHash.put("D#", new Integer(3));
        notesHash.put("EB", new Integer(3));
        notesHash.put("E", new Integer(4));
        notesHash.put("FB", new Integer(4));
        notesHash.put("E#", new Integer(5));
        notesHash.put("F", new Integer(5));
        notesHash.put("F#", new Integer(6));
        notesHash.put("GB", new Integer(6));
        notesHash.put("G", new Integer(7));
        notesHash.put("G#", new Integer(8));
        notesHash.put("AB", new Integer(8));
        notesHash.put("A", new Integer(9));
        notesHash.put("A#", new Integer(10));
        notesHash.put("BB", new Integer(10));
        notesHash.put("B", new Integer(11));
        notesHash.put("CB", new Integer(11));
        notesHash.put("B#", new Integer(0));
    }
    public static String[] notesArray = new String[] {
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    
    public static float parseNote(String notestr) {
        String noteLetters;
        //System.out.print(" " + notestr + " ");
        if ("_".equals(notestr) ||
            "rest".equalsIgnoreCase(notestr) 
            ) {
            return REST;
        }
        if (".".equals(notestr) ||
            "*".equals(notestr) ||
            "rest_stop".equalsIgnoreCase(notestr) 
            ) {
            return REST_STOP;
        }
        char maybeInflection = notestr.charAt(1);
        int nllen;
        if (maybeInflection == 'b' || maybeInflection == '#') {
            noteLetters = notestr.substring(0, 2).toUpperCase();
            nllen = 2;
        } else {
            noteLetters = notestr.substring(0, 1).toUpperCase();
            nllen = 1;
        }
        int octave = Integer.parseInt(notestr.substring(nllen));

        int base = 60 + (octave - 3) * 12;
        return (float)(base + ((Integer)notesHash.get(noteLetters)).intValue());
    }

    public static String noteString(int pitch) {
        if (pitch == REST) {
            return "_";
        }
        if (pitch == REST_STOP) {
            return ".";
        }

        int octave = (int)(pitch / 12) - 2;  // Middle C = Midi 60 = C3 
        int remainder = pitch % 12;
        return notesArray[remainder] + octave;
    }

    public void toString(StringBuffer buf) {
        buf.append(noteString((int)pitch));
    }

    public String toString() {
        return noteString((int)pitch);
    }
}
