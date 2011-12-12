/***************************************************
 * Copyright 2007 by Simran Gleason                *
 * This program is distributed under the terms     *
 * of the GNU General Public License.              *
 * See kepler.Kepler.LICENSE_TEXT or               *
 * http://www.gnu.org/licenses/gpl.txt             *
 ***************************************************/

package kepler;

import java.util.ArrayList;
import java.util.Iterator;

public class Sequence extends Playable {
    public static final int TIME_TYPE_CYCLES = 0;
    public static final int TIME_TYPE_SECONDS = 1;
    public static final int TIME_TYPE_ROUNDS = 2;
    public static final int TIME_TYPE_NOTES  = 3;

    public static final int TIME_CONTINUE  = -1;

    public static final int WRAP_NONE = 0;
    public static final int WRAP_WRAP = 1;
    public static final int WRAP_CONTINUE_LAST = 2;
        
    protected ArrayList sequences = null;
    protected String name;
    protected int cursor = -1;
    protected int timeType = TIME_TYPE_SECONDS;  // defaults to seconds
    protected int numNotes = 0;
    protected int sequenceRounds = 0;
    protected int cycleOffset = 0;
    protected int wrapType = WRAP_WRAP;

    protected SubSequence currentSubSequence = null;
    
    public Sequence(String name)  {
        this.name = name;
        this.sequences = new ArrayList();
    }

    public Playable duplicate()   {
        Sequence clown = new Sequence(name);
        clown.setTimeType(this.timeType);
        for(Iterator it = sequences.iterator(); it.hasNext(); ) {
            SubSequence sub = (SubSequence)it.next();
            Playable pl = sub.sequence;
            clown.add(pl.duplicate(), sub.time);
        }
        return clown;
    }

    public void setTimeType(int timeType)  {
        this.timeType = timeType;
    }

    public void setTimeType(String timeTypeStr)  {
        if (timeTypeStr.equalsIgnoreCase("seconds")) {
            this.timeType = TIME_TYPE_SECONDS;
        } else if (timeTypeStr.equalsIgnoreCase("cycles")) {
            this.timeType = TIME_TYPE_CYCLES;
         } else if (timeTypeStr.equalsIgnoreCase("rounds")) {
            this.timeType = TIME_TYPE_ROUNDS;
         } else if (timeTypeStr.equalsIgnoreCase("notes")) {
            this.timeType = TIME_TYPE_NOTES;
         }
    }

    public String timeTypeString(int timeType) {
        switch (timeType) {
        case TIME_TYPE_SECONDS:
            return "seconds";
        case TIME_TYPE_CYCLES:
            return "cycles";
        case TIME_TYPE_ROUNDS:
            return "rounds";
        case TIME_TYPE_NOTES:
            return "notes";
        default:
            return "unknown";
        }
    }

    // note: melodies need to also be sequences. 
    public void add(Playable seq, int time)  {
        sequences.add(new SubSequence(seq, time));
    }

    public SubSequence getSequence(int cursor)  {
        return (SubSequence)sequences.get(cursor);
    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return "sequence";
    }
    
    // Playable api for getting the next note. 
    public Note nextNote(Body b, Orrery orrery) {
        if (cursor == -1) {
            cursor = 0;
            currentSubSequence = getSequence(cursor);
            cycleOffset = 0;
            sequenceRounds = 0;
            numNotes = 0;
        }
        //System.out.println("Sequence:nextNote. cursor: "+cursor + " subseq(cursor): " + getSequence(cursor));
        //System.out.println("   curseq.atEnd: " + currentSubSequence.sequence.atEnd() + " time: " + currentSubSequence.time);
        //System.out.println("   cycleOffset: " + cycleOffset);
        //System.out.println("   numCycles: " + orrery.numCycles + " cyc-offset: " + (orrery.numCycles - cycleOffset));
        //System.out.println("   numNotes: " + numNotes );
        //System.out.println("   sequnceRounds: " + sequenceRounds);
        boolean timeForNextSeq = false;
        if (currentSubSequence.time == TIME_CONTINUE) {
            timeForNextSeq = false;
        } else {
            switch (timeType) {
            case TIME_TYPE_SECONDS:
                int currentSubSequence_cycles = orrery.secondsToCycles(currentSubSequence.time);
                timeForNextSeq = (currentSubSequence_cycles <= orrery.numCycles - cycleOffset);
                break;
            case TIME_TYPE_CYCLES:
                timeForNextSeq = (currentSubSequence.time <= orrery.numCycles - cycleOffset);
                break;
            case TIME_TYPE_ROUNDS:
                timeForNextSeq = (currentSubSequence.time <= sequenceRounds);
                break;
            case TIME_TYPE_NOTES:
                timeForNextSeq = (currentSubSequence.time <= numNotes);
                break;
            }
        }
        //System.out.println("   time4Next: " + timeForNextSeq);
        if (timeForNextSeq) {
            cycleOffset = orrery.numCycles;
            sequenceRounds = 0;
            numNotes = 0;
            cursor++;
            if (cursor >= sequences.size()) {
                if (wrapType == WRAP_WRAP) {
                    cursor = 0;
                } else if (wrapType == WRAP_CONTINUE_LAST) {
                    cursor--;
                }
            }
            currentSubSequence = getSequence(cursor);
        }
        numNotes++;
        if (currentSubSequence.sequence.atEnd()) {
            sequenceRounds++;
        }
        Note n = currentSubSequence.sequence.nextNote(b, orrery); 
        //System.out.println("  >>  "+n);
        return n;
    }

    public void reset() {
        reset(0);
    }

    public void writeBody(StringBuffer buf) {
        writeSequenceMods(buf);
        buf.append("\n");
        buf.append("  start\n");
        writeContents(buf, false);
        buf.append("  end");
    }

    public void writeBodyXML(String indent, StringBuffer buf) {
        for(Iterator it = sequences.iterator(); it.hasNext(); ) {
            SubSequence sub = (SubSequence)it.next();
            Playable pl = sub.sequence;
            buf.append("<");
            buf.append(pl.getType());
            buf.append(" name=\"");
            buf.append(pl.getName());
            buf.append("\" ");
            if (timeType == TIME_TYPE_SECONDS) {
                buf.append(" time=\"");
            } else if (timeType == TIME_TYPE_ROUNDS) {
                buf.append(" rounds=\"");
            } else if (timeType == TIME_TYPE_CYCLES) {
                buf.append(" cycles=\"");
            } else if (timeType == TIME_TYPE_NOTES) {
                buf.append(" notes=\"");
            }
            buf.append(sub.time);
            buf.append("\"");
            buf.append(" />\n");
        }        
    }

    public void writeCurrentState(StringBuffer buf) {
        writeSequenceMods(buf);
        buf.append("\n");
        buf.append("  start\n");
        writeContents(buf, true);
        buf.append("  end");
    }

    public void writeContents(StringBuffer buf, boolean writeCurrentState) {
        int i = 0;
        for(Iterator it = sequences.iterator(); it.hasNext(); ) {
            SubSequence sub = (SubSequence)it.next();
            Playable pl = sub.sequence;
            buf.append("    ");
            buf.append(pl.getType());
            buf.append(" ");
            buf.append(pl.getName());
            buf.append(" ");
            buf.append(sub.time);
            buf.append("\n");
            if (writeCurrentState && cursor == i) {
                pl.writeCurrentState(buf);
            }
        }
    }
    ///////////////////////////////
    
    
    public void reset(int nth) {
        cursor = nth;
        currentSubSequence = getSequence(cursor);
    }

    public void writeSequenceMods(StringBuffer buf) {
        if (timeType != TIME_TYPE_SECONDS) {
            buf.append("timetype ");
            buf.append(timeTypeString(timeType));
            buf.append("\n");
        }
    }

    public boolean atEnd() {
        if (cursor >= sequences.size()) {
            if (currentSubSequence.time == TIME_CONTINUE) {
                return false;
            } else  {
                return currentSubSequence.sequence.atEnd();
            }
        }
        return false;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("SEQ(" + name + "){");
        for(Iterator it=sequences.iterator(); it.hasNext(); )  {
            buf.append(it.next().toString());
            if (it.hasNext()) {
                buf.append(", ");
            }
        }
        return buf.toString();
    }
    
    class SubSequence  {
        public Playable sequence;
        public int time;
        public SubSequence(Playable sequence, int time)  {
            this.sequence = sequence;
            this.time = time;
        }
        public String toString() {
            return "{" + sequence + ", " + time + "}";
        }
    }
}
